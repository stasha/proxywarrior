package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Utils;
import com.zaxxer.hikari.HikariDataSource;
import info.stasha.proxywarrior.config.CommonConfig;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.RequestConfig;
import info.stasha.proxywarrior.config.HttpClientConfig;
import info.stasha.proxywarrior.config.loader.ConfigLoader;
import info.stasha.proxywarrior.config.ResponseConfig;
import info.stasha.proxywarrior.logging.db.ConfigurationEntity;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.flywaydb.core.Flyway;
import org.javalite.activejdbc.Base;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

/**
 *
 * @author stasha
 */
public class ProxyWarrior extends ProxyServlet implements Filter {

    public static final String SYSTEM_CONFIG_LOCATION = "proxywarrior.config.location";
    public static final String FILTER_INIT_CONFIG_LOCATION = "config_location";
    public static final String LAST_CONFIG_USED = "LAST_CONFIG_USED";

    private static final Logger LOGGER = Logger.getLogger(ProxyWarrior.class.getName());
    private static final ThreadLocal<Metadata> REQUEST_METADATA = new ThreadLocal<>();

    private final Timer CONFIG_RELOAD_TIMER = new Timer(true);
    private String propsLocation;
    private RequestConfig config; //= new RequestConfig();
    private FilterConfig filterConfig;
    private HikariDataSource hikariDs;

    /**
     * Creates new proxy warrior instance.
     */
    public ProxyWarrior() {
    }

    /**
     * Creates new proxy warrior instance.
     *
     * @param hikariDs
     */
    public ProxyWarrior(HikariDataSource hikariDs) {
        this.hikariDs = hikariDs;
    }

    /**
     * Sets DS.
     *
     * @param ds
     */
    protected void setDataSource(HikariDataSource ds) {
        if (this.hikariDs == null) {
            if (ds == null) {
                File proxywarriorDbDir = Paths.get(System.getProperty("user.home"), ".proxywarrior").toFile();
                proxywarriorDbDir.mkdirs();

                String proxywarriorJdbcConnection = "jdbc:hsqldb:" + proxywarriorDbDir + File.separator + "proxywarrior";

                this.hikariDs = new HikariDataSource();
                this.hikariDs.setJdbcUrl(proxywarriorJdbcConnection);
                this.hikariDs.setUsername("SA");
                this.hikariDs.setPassword("");
            } else {
                this.hikariDs = ds;
            }
        }
    }

    /**
     * Initializes ProxyWarrior DB.
     *
     * @throws SQLException
     */
    public void initDb() throws SQLException {
        setDataSource(null);
        Flyway.configure().dataSource(this.hikariDs).load().migrate();
    }

    /**
     * Saves configuration into DB.
     *
     * @param config
     * @param path
     */
    protected void saveConfigToDb(RequestConfig config, String path) {
        if (hikariDs.isRunning()) {

            Base.openTransaction();

            try {
                ConfigurationEntity.update(LAST_CONFIG_USED + " = ?", null, false);

                ConfigurationEntity c = ConfigurationEntity.findFirst("CONFIG_ID = ?", config.getId());
                c = c == null ? new ConfigurationEntity() : c;

                c.setString("CONFIG_ID", config.getId())
                        .setBoolean("LAST_CONFIG_USED", true)
                        .setString("CONFIG_PATH", path)
                        .setString("CONFIG", ConfigLoader.getUserConfig())
                        .saveIt();

                Base.commitTransaction();

            } catch (Exception ex) {
                Base.rollbackTransaction();
                String msg = "Failed to save configuration with id \"" + config.getId() + "\" into DB";
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProxyWarriorException(msg, ex);
            }
        }
    }

    /**
     * Loads configuration.
     *
     * @return
     */
    protected RequestConfig loadConfig() {
        ConfigurationEntity ce = null;

        // configuration is always loaded in next order
        // 1. from system path "proxywarrior.config.location"
        // 2. from filter init param "config_location"
        // 3. LAST_CONFIG_USED from DB
        propsLocation = System.getProperty(SYSTEM_CONFIG_LOCATION);
        LOGGER.log(Level.INFO, "System config path: {0}", propsLocation);

        String filterConfigPath = filterConfig.getInitParameter(FILTER_INIT_CONFIG_LOCATION);
        LOGGER.log(Level.INFO, "Filter config path: {0}", propsLocation);

        propsLocation = propsLocation != null ? propsLocation : filterConfigPath;

        if (propsLocation != null) {
            this.config = ConfigLoader.load(propsLocation);
        }

        if (this.config != null) {
            this.saveConfigToDb(this.config, propsLocation);
        } else {
            try {
                LOGGER.info("Loading config from DB");
                ce = ConfigurationEntity.findFirst(LAST_CONFIG_USED + " = ?", true);
            } catch (Exception ex) {
                String msg = "Failed to load configuration from DB";
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProxyWarriorException(msg, ex);
            }

            // loading configuration from DB
            if (ce != null) {
                String configString = ce.getString("CONFIG");
                this.config = ConfigLoader.setConfiguration(configString);
                propsLocation = ce.getString("CONFIG_PATH");

                if (propsLocation != null) {
                    LOGGER.log(Level.INFO, "Config path: {0}", propsLocation);
                    File file = new File(propsLocation);

                    if (!file.exists()) {
                        try {
                            LOGGER.log(Level.INFO, "Writing config to: {0}", propsLocation);
                            FileUtils.writeStringToFile(file, configString, "UTF-8");
                        } catch (Exception ex) {
                            String msg = "Failed to save config to location " + propsLocation;
                            LOGGER.log(Level.SEVERE, msg, ex);
                            throw new ProxyWarriorException(msg, ex);
                        }
                    }
                }
            }
        }

        // if config is not loaded from any source, default config is used
        if (this.config == null) {
            LOGGER.log(Level.INFO, "Loading default config");
            this.config = ConfigLoader.setConfiguration(null);
            this.saveConfigToDb(this.config, null);
        }

        // if propsLocation is not null, then we pull periodically to load new
        // configuration in case it has changed
        if (propsLocation != null) {
            CONFIG_RELOAD_TIMER.scheduleAtFixedRate(new ConfigLoader(propsLocation, (c) -> {
                this.config = c;
                this.saveConfigToDb(this.config, propsLocation);
            }), 0, 1000 * 10);
        }

        return this.config;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.filterConfig = filterConfig;

        try {
            initDb();
        } catch (Exception ex) {
            String msg = "Failed to initialize ProxyWarrior DB";
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }

        try {
            Base.open(hikariDs);
            RequestConfig c = loadConfig();
            c.getListeners().init(this);
        } finally {
            Base.close();
        }

    }

    protected String getConfigParam(Object value, String defaultValue) {
        return value != null ? String.valueOf(value) : defaultValue;
    }

    @Override
    protected String getConfigParam(String key) {
        RequestConfig req = REQUEST_METADATA.get().getRequestConfig();
        HttpClientConfig clientConfig = req.getClientConfig();

        switch (key) {
            case P_LOG:
                return "false";
            case P_FORWARDEDFOR:
                return getConfigParam(req.isForwardIp(), null);
            case P_PRESERVEHOST:
                return "true";
            case P_PRESERVECOOKIES:
                return "true";
            case P_HANDLEREDIRECTS:
                return getConfigParam(clientConfig.getHandleRedirects(), null);
            case P_CONNECTIONREQUESTTIMEOUT:
                return getConfigParam(clientConfig.getConnectionRequestTimeout(), null);
            case P_CONNECTTIMEOUT:
                return getConfigParam(clientConfig.getConnectTimeout(), null);
            case P_READTIMEOUT:
                return getConfigParam(clientConfig.getReadTimeout(), null);
            case P_MAXCONNECTIONS:
                return getConfigParam(clientConfig.getMaxConnections(), null);
            case P_USESYSTEMPROPERTIES:
                return getConfigParam(clientConfig.getUseSystemProperties(), null);
            case P_TARGET_URI:
                return "http://fdsafdfsafsdfsfasadfadsfsdf.com";
            default:
                throw new IllegalArgumentException("Config param: " + key + " is not supported");
        }
    }

    @Override
    protected void initTarget() throws ServletException {
        targetUri = REQUEST_METADATA.get().getProxyUri();
        try {
            targetUriObj = new URI(targetUri);
        } catch (Exception e) {
            throw new ServletException("Trying to process targetUri init parameter: " + e, e);
        }
        targetHost = URIUtils.extractHost(targetUriObj);
    }

    protected HttpClient getHttpClient(Metadata metadata) throws ServletException {
        HttpClient client = metadata.getRequestConfig().getClientConfig().getHttpClient();
        if (client == null) {
            synchronized (this) {
                super.init();
                client = getProxyClient();
                metadata.getRequestConfig().getClientConfig().setHttpClient(client);
            }
        } else {
            initTarget();
        }

        return client;
    }

    @Override
    protected String getCookieNamePrefix(String name) {
        return "!Proxy!" + this.filterConfig.getFilterName();
    }

    protected void overrideHeaders(HttpMessage message, CommonConfig<CommonConfig> config) {

        if (config.getHeaders() != null) {
            for (String key : config.getHeaders().keySet()) {
                for (Header h : message.getAllHeaders()) {

                    if (key.startsWith("=")) {
                        // keep header as is
                        continue;
                    }

                    if (key.startsWith("~")) {
                        // remove header
                        String name = key.replaceFirst("~", "");
                        name = Utils.getValue(name, config);
                        if (name.equals(h.getName())) {
                            message.removeHeader(h);
                            continue;
                        }
                    }

                    if (key.startsWith("+")) {
                        // add new header if it does not exist
                        String name = key.replaceFirst("\\+", "");
                        name = Utils.getValue(name, config);
                        if (!message.containsHeader(name)) {
                            message.setHeader(name, Utils.getValue(config.getHeaders().get(key), config));
                            continue;
                        }
                    }
                    // add or replace existing header
                    if (!key.startsWith("=") && !key.startsWith("+") && !key.startsWith("~")) {
                        message.setHeader(Utils.getValue(key, config), Utils.getValue(config.getHeaders().get(key), config));
                        continue;
                    }

                    // remove headers based on removeHeaders pattern
                    if (config.getRemoveHeaders() != null) {
                        if (config.getRemoveHeadersPattern().matcher(h.getName()).find()) {
                            message.removeHeader(h);
                            continue;
                        }
                    }
                }
            }

        }

    }

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        Metadata metadata = REQUEST_METADATA.get();
        metadata.setProxyRequest(proxyRequest);

        RequestConfig requestConfig = metadata.getRequestConfig();
        overrideHeaders(proxyRequest, (CommonConfig) requestConfig);
        requestConfig.getListeners().fire(ProxyAction.BEFORE_PROXY_REQUEST, metadata);

        BasicHttpResponseWrapper proxyResponse = new BasicHttpResponseWrapper(super.doExecute(servletRequest, servletResponse, proxyRequest));

        metadata.setProxyResponse(proxyResponse);
        requestConfig.getListeners().fire(ProxyAction.AFTER_PROXY_RESPONSE, metadata);

        return proxyResponse;
    }

    protected void copyResponseEntity(
            HttpResponse proxyResponse,
            HttpServletResponse servletResponse,
            HttpRequest proxyRequest,
            HttpServletRequest servletRequest
    ) throws IOException {
    }

    private long getContentLength(HttpMessage reqresp) {
        Header contentLength = reqresp.getFirstHeader("Content-Length");

        if (contentLength != null) {
            return Long.parseLong(contentLength.getValue());
        }
        return -1L;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        super.service(servletRequest, servletResponse);

        Metadata metadata = REQUEST_METADATA.get();
        metadata.setResponse(metadata.getRequestConfig().getResponse(metadata));
        ResponseConfig responseConfig = metadata.getResponseConfig();
        BasicHttpResponseWrapper proxyResponse = (BasicHttpResponseWrapper) metadata.getProxyResponse();
        HttpEntity entity = null;

        InputStream content = null;

        try {

            entity = proxyResponse.getOriginalEntity();

            if (entity != null) {
                String text = metadata.getResponseConfig().getText();
                String file = metadata.getResponseConfig().getFile();

                if (text != null) {
                    overrideHeaders(proxyResponse, (CommonConfig) responseConfig);

                    proxyResponse.setHeader("Content-Length", String.valueOf(text.getBytes().length));
                    proxyResponse.removeHeaders("Content-Encoding");

                    copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

                    content = new ByteArrayInputStream(text.getBytes());
                } else if (file != null) {
                    File f = new File(file);

                    if (file.startsWith("classpath:")) {
                        String path = Paths.get(file.replace("classpath:", File.separator)).normalize().toString();
                        URL resource = ProxyWarrior.class
                                .getResource(path);
                        if (resource != null) {
                            f = new File(resource.getFile());
                        }
                    }

                    if (!f.exists()) {
                        servletResponse.setStatus(404);
                        servletResponse.getWriter().println("File can not be found: \"" + f.getPath() + "\"");
                    }

                    overrideHeaders(proxyResponse, (CommonConfig) responseConfig);

                    proxyResponse.setHeader("Content-Length", String.valueOf(f.length()));
                    proxyResponse.removeHeaders("Content-Encoding");

                    copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

                    content = new FileInputStream(f);
                } else {
                    content = entity.getContent();
                }

            } else {
                overrideHeaders(proxyResponse, (CommonConfig) responseConfig);
                copyResponseHeaders(proxyResponse, servletRequest, servletResponse);
            }

            if (entity != null && content != null) {
                proxyResponse.setEntity(new InputStreamEntity(content, getContentLength(proxyResponse)));
            }

            metadata.getRequestConfig().getListeners().fire(ProxyAction.BEFORE_HTTP_RESPONSE, metadata);

            if (content != null) {
                IOUtils.copy(content, servletResponse.getOutputStream());
            }

        } finally {
            if (entity != null) {
                EntityUtils.consumeQuietly(entity);
            }
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest htReq = (HttpServletRequest) request;
        HttpServletResponse htResp = (HttpServletResponse) response;

        System.out.println("config: " + config);

        Metadata metadata = config.getMetadata(htReq, htResp);
        if (metadata == null) {
            htResp.sendError(406, "Requested url did not match any uri/url pattern specified in proxywarrior configuration.");
            return;
        }

        REQUEST_METADATA.set(metadata);

        metadata.setDataSource(this.hikariDs);

        String localAddress = htReq.getLocalAddr();
        int localPort = htReq.getLocalPort();
        String reqAddress = htReq.getRemoteAddr();
        int reqPort = htReq.getServerPort();

        String requestUrl = metadata.getFullUrl();
        String proxyUrl = metadata.getProxyUrl();

        RequestConfig req = metadata.getRequestConfig();

        boolean isSameRequest = (requestUrl.equals(proxyUrl) && localAddress.equals(reqAddress) && localPort == reqPort);
        boolean isAutoProxy = (req.isAutoProxy() != null && req.isAutoProxy() == true) && (req.getAutoProxyExpireTime() == null || req.getAutoProxyExpireTime().after(new Date()));

        try {
            Base.open(this.hikariDs);

            if (isSameRequest || isAutoProxy == false) {
                request.setAttribute("proxy", metadata);
                req.getListeners().fire(ProxyAction.AFTER_NOT_PROXY_REQUEST, metadata);
                chain.doFilter(request, response);
            } else {
                req.getListeners().fire(ProxyAction.AFTER_HTTP_REQUEST, metadata);

                getHttpClient(metadata);

                super.service(request, response);
            }
        } finally {
            Base.close();
        }
    }

    @Override
    public void destroy() {
        try {
            CONFIG_RELOAD_TIMER.cancel();
        } finally {
            try {
                if (config != null) {
                    try {
                        config.dispose();
                    } finally {
                        config.getListeners().destroy(this);
                    }
                }
            } finally {
                try {
                    if (hikariDs != null && !hikariDs.isClosed()) {
                        try {
                            hikariDs.getConnection().prepareStatement("shutdown").execute();
                        } catch (SQLException ex) {
                            String msg = "Failed to shutdown DB";
                            LOGGER.log(Level.SEVERE, msg, ex);
                        } finally {
                            hikariDs.close();
                        }
                    }
                } finally {
                    super.destroy();
                }
            }
        }
    }

}
