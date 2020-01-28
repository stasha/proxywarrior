package info.stasha.proxywarrior;

import com.zaxxer.hikari.HikariDataSource;
import info.stasha.proxywarrior.config.CommonConfig;
import info.stasha.proxywarrior.config.Headers;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.RequestConfig;
import info.stasha.proxywarrior.config.HttpClientConfig;
import info.stasha.proxywarrior.config.loader.ConfigLoader;
import info.stasha.proxywarrior.config.ResponseConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.flywaydb.core.Flyway;
import org.javalite.activejdbc.Base;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stasha
 */
public class ProxyWarrior extends ProxyServlet implements Filter {

    public static final String SYSTEM_CONFIG_LOCATION = "proxywarrior.config.location";
    public static final String FILTER_INIT_CONFIG_LOCATION = "config_location";
    public static final String LAST_CONFIG_USED = "LAST_CONFIG_USED";

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyWarrior.class.getName());
    private static final ThreadLocal<Metadata> REQUEST_METADATA = new ThreadLocal<>();

    private final Timer CONFIG_RELOAD_TIMER = new Timer(true);
    private String propsLocation;
    private RequestConfig config; //= new RequestConfig();
    private FilterConfig filterConfig;
    private HikariDataSource dataSource;

    /**
     * Creates new proxy warrior instance.
     */
    public ProxyWarrior() {
    }

    /**
     * Creates new proxy warrior instance.
     *
     * @param dataSource
     */
    public ProxyWarrior(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected void executeInTransaction(Runnable sup) {
        if (!Base.hasConnection()) {
            try {
                Base.open(dataSource);
                Base.openTransaction();
                sup.run();
                Base.commitTransaction();
            } finally {
                Base.rollbackTransaction();
                Base.close();
            }
        } else {
            sup.run();
        }
    }

    /**
     * Sets DS.
     *
     * @param ds
     */
    protected void setDataSource(HikariDataSource ds) {
        if (this.dataSource == null) {
            if (ds == null) {
                File proxywarriorDbDir = Paths.get(System.getProperty("user.home"), ".proxywarrior").toFile();
                proxywarriorDbDir.mkdirs();

                String proxywarriorJdbcConnection = "jdbc:hsqldb:" + proxywarriorDbDir + File.separator + "proxywarrior";

                this.dataSource = new HikariDataSource();
                this.dataSource.setJdbcUrl(proxywarriorJdbcConnection);
                this.dataSource.setUsername("SA");
                this.dataSource.setPassword("");
            } else {
                this.dataSource = ds;
            }
        }
    }

    /**
     * Returns DataSource.
     *
     * @return
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Returns RequestConfig.
     *
     * @return
     */
    public RequestConfig getConfig() {
        return this.config;
    }

    /**
     * Initializes ProxyWarrior DB.
     *
     * @throws SQLException
     */
    public void initDb() throws SQLException {
        setDataSource(null);
        Flyway.configure().dataSource(this.dataSource).load().migrate();
    }

    /**
     * Saves configuration into DB.
     *
     * @param config
     * @param path
     */
    protected void saveConfigToDb(RequestConfig config, String path) {
        if (dataSource.isRunning()) {

            Base.openTransaction();

            try {
                Base.exec("UPDATE CONFIGURATION SET LAST_CONFIG_USED = ?", false);
                Integer configExists = (Integer) Base.firstCell("SELECT 1 FROM CONFIGURATION WHERE CONFIG_ID = ?", config.getId());

                if (configExists == null) {
                    Base.exec("INSERT INTO CONFIGURATION (CONFIG_ID, LAST_CONFIG_USED, CONFIG_PATH, CONFIG) VALUES (?, ?, ?, ?)",
                            config.getId(), true, path, ConfigLoader.getUserConfig());
                } else {
                    Base.exec("UPDATE CONFIGURATION LAST_CONFIG_USED = ?, CONFIG_PATH = ?, CONFIG = ? WHERE CONFIG_ID = ?",
                            true, path, ConfigLoader.getUserConfig(), config.getId());
                }

                Base.commitTransaction();

            } catch (Exception ex) {
                Base.rollbackTransaction();
                String msg = "Failed to save configuration with id \"" + config.getId() + "\" into DB";
                LOGGER.error(msg, ex);
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
        String ce = null;

        // configuration is always loaded in next order
        // 1. from system path "proxywarrior.config.location"
        // 2. from filter init param "config_location"
        // 3. LAST_CONFIG_USED from DB
        propsLocation = System.getProperty(SYSTEM_CONFIG_LOCATION);
        LOGGER.info("System config path: {}", propsLocation);

        String filterConfigPath = filterConfig.getInitParameter(FILTER_INIT_CONFIG_LOCATION);
        LOGGER.info("Filter config path: {}", filterConfigPath);

        propsLocation = propsLocation != null ? propsLocation : filterConfigPath;

        if (propsLocation != null) {
            this.config = ConfigLoader.load(propsLocation);
        }

        if (this.config != null) {
            this.saveConfigToDb(this.config, propsLocation);
        } else {
            try {
                LOGGER.info("Loading config from DB");
                ce = (String) Base.firstCell("SELECT CONFIG FROM CONFIGURATION WHERE LAST_CONFIG_USED = ?", true);
            } catch (Exception ex) {
                String msg = "Failed to load configuration from DB";
                LOGGER.error(msg, ex);
                throw new ProxyWarriorException(msg, ex);
            }

            // loading configuration from DB
            if (ce != null) {
                this.config = ConfigLoader.setConfiguration(ce);
                propsLocation = (String) Base.firstCell("SELECT CONFIG FROM CONFIGURATION WHERE CONFIG_ID = ?", this.config.getId());

                if (propsLocation != null) {
                    LOGGER.info("Config path: {}", propsLocation);
                    File file = new File(propsLocation);

                    if (!file.exists()) {
                        try {
                            LOGGER.info("Writing config to: {}", propsLocation);
                            FileUtils.writeStringToFile(file, ce, "UTF-8");
                        } catch (Exception ex) {
                            String msg = "Failed to save config to location " + propsLocation;
                            LOGGER.error(msg, ex);
                            throw new ProxyWarriorException(msg, ex);
                        }
                    }
                }
            }
        }

        // if config is not loaded from any source, default config is used
        if (this.config == null) {
            LOGGER.info("Loading default config");
            this.config = ConfigLoader.setConfiguration(null);
            this.saveConfigToDb(this.config, null);
        }

        // if propsLocation is not null, then we pull periodically to load new
        // configuration in case it has changed
        if (propsLocation != null) {
            CONFIG_RELOAD_TIMER.scheduleAtFixedRate(new ConfigLoader(propsLocation, (c) -> {
                this.config = c;
                executeInTransaction(() -> {
                    this.saveConfigToDb(this.config, propsLocation);
                });
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
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }

        executeInTransaction(() -> {
            RequestConfig c = loadConfig();
            c.getListeners().init(this);
        });
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

    protected void overrideHeaders(HttpMessage message, Map<String, List<String>> headers, CommonConfig config) {
        Headers.setHeaders(message, headers, config);
    }

    @Override
    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        Metadata metadata = REQUEST_METADATA.get();
        metadata.setProxyRequest(proxyRequest);

        RequestConfig requestConfig = metadata.getRequestConfig();
        overrideHeaders(proxyRequest, requestConfig.getHeaders().getRequest(), requestConfig);
        requestConfig.getListeners().fire(ProxyAction.BEFORE_PROXY_REQUEST, metadata);
        BasicHttpResponseWrapper proxyResponse = null;

        // if caching is enabled then dont' send request but just return existing from DB
        Long cacheId = metadata.shouldUpdateDbRecord(requestConfig);
        if (cacheId != null && cacheId > 0) {
            String statusLine = (String) Base.firstCell("SELECT PROXY_RESPONSE_STATUS_LINE FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", cacheId);
            Blob data = (Blob) Base.firstCell("SELECT PROXY_RESPONSE_CONTENT FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", cacheId);
            String heders = (String) Base.firstCell("SELECT PROXY_RESPONSE_HEADERS FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", cacheId);

            String[] statusData = statusLine.split(" ");
            String[] protocol = statusData[0].split("/");
            String[] majorMinor = protocol[1].split("\\.");
            ProtocolVersion pv = new ProtocolVersion(protocol[0], Integer.parseInt(majorMinor[0]), Integer.parseInt(majorMinor[1]));
            HttpResponse resp = new BasicHttpResponse(new BasicStatusLine(pv, Integer.parseInt(statusData[1]), statusData[2]));

            Utils.setEntity(resp, new ByteArrayInputStream(new String().getBytes()), data);
            Headers.setHeaders(resp, MapperFactory.getMapper("yaml").readValue(heders, Map.class));

            proxyResponse = new BasicHttpResponseWrapper(resp);
        } else {
            proxyResponse = new BasicHttpResponseWrapper(super.doExecute(servletRequest, servletResponse, proxyRequest));
        }

        metadata.setProxyResponse(proxyResponse);
        return proxyResponse;
    }

    protected void copyResponseEntity(
            HttpResponse proxyResponse,
            HttpServletResponse servletResponse,
            HttpRequest proxyRequest,
            HttpServletRequest servletRequest
    ) throws IOException {
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        super.service(servletRequest, servletResponse);

        Metadata metadata = REQUEST_METADATA.get();
        metadata.setResponseConfig(metadata.getRequestConfig().getResponse(metadata));

        metadata.getRequestConfig().getListeners().fire(ProxyAction.AFTER_PROXY_RESPONSE, metadata);

        ResponseConfig responseConfig = metadata.getResponseConfig();
        Map<String, List<String>> headers = responseConfig.getHeaders().getResponse();
        BasicHttpResponseWrapper proxyResponse = (BasicHttpResponseWrapper) metadata.getProxyResponse();
        HttpEntity oldEntity = null;
        HttpEntity newEntity = null;

        InputStream content = null;

        try {
            servletResponse.reset();

            overrideHeaders(proxyResponse, headers, responseConfig);

            oldEntity = proxyResponse.getOriginalEntity();

            if (oldEntity != null) {
                String text = metadata.getResponseConfig().getText();
                String file = metadata.getResponseConfig().getFile();

                if (text != null) {

                    proxyResponse.setHeader("Content-Length", String.valueOf(text.getBytes().length));
                    proxyResponse.removeHeaders("Content-Encoding");

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

                    proxyResponse.setHeader("Content-Length", String.valueOf(f.length()));
                    proxyResponse.removeHeaders("Content-Encoding");

                    content = new FileInputStream(f);
                } else {
                    content = oldEntity.getContent();
                }

            }

            copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

            if (oldEntity != null && content != null) {
                Utils.setEntity(proxyResponse, content);
            }

            metadata.getRequestConfig().getListeners().fire(ProxyAction.BEFORE_HTTP_RESPONSE, metadata);

            if (content != null) {
                // new content could be set by listeners so we must pull it again
                newEntity = proxyResponse.getOriginalEntity();

                if (newEntity != null) {
                    content = newEntity.getContent();
                }

                if (content != null) {
                    IOUtils.copy(content, servletResponse.getOutputStream());
                }
            }

        } finally {
            EntityUtils.consumeQuietly(newEntity);
            EntityUtils.consumeQuietly(oldEntity);
        }

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest htReq = (HttpServletRequest) request;
        HttpServletResponse htResp = (HttpServletResponse) response;

        Metadata metadata = config.getMetadata(htReq, htResp);
        if (metadata == null) {
            htResp.sendError(406, "Requested url did not match any uri/url pattern specified in proxywarrior configuration.");
            return;
        }

        REQUEST_METADATA.set(metadata);

        String localAddress = htReq.getLocalAddr();
        int localPort = htReq.getLocalPort();
        String reqAddress = htReq.getRemoteAddr();
        int reqPort = htReq.getServerPort();

        String requestUrl = metadata.getFullUrl();
        String proxyUrl = metadata.getProxyUrl();

        RequestConfig req = metadata.getRequestConfig();

        boolean isSameRequest = (requestUrl.equals(proxyUrl) && localAddress.equals(reqAddress) && localPort == reqPort);
        boolean isAutoProxy = (req.isAutoProxy() != null && req.isAutoProxy() == true) && (req.getAutoProxyExpireTime() == null || req.getAutoProxyExpireTime().after(new Date()));

        if (isSameRequest || isAutoProxy == false) {
            request.setAttribute("proxy", metadata);
            req.getListeners().fire(ProxyAction.AFTER_NOT_PROXY_REQUEST, metadata);
            chain.doFilter(request, response);
        } else {
            try {
                Base.open(this.dataSource);
                htReq = new HttpServletRequestWrapperImpl(htReq);
                metadata.setHttpServletRequest(htReq);
                Long cacheId = metadata.shouldUpdateDbRecord(req);
                if (cacheId != null && cacheId < -1) {
                    Base.exec("DELETE FROM REQUEST WHERE CONFIG_ID = ? AND REQUEST_PATH = ? AND CACHED = true", req.getId(), metadata.getPath());
                }
                req.getListeners().fire(ProxyAction.AFTER_HTTP_REQUEST, metadata);

                getHttpClient(metadata);
                try {
                    super.service(htReq, response);
                } finally {
                    req.getListeners().fire(ProxyAction.AFTER_HTTP_RESPONSE, metadata);
                }
            } finally {
                Base.close();
            }
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
                    if (dataSource != null && !dataSource.isClosed()) {
                        try {
                            dataSource.getConnection().prepareStatement("shutdown").execute();
                        } catch (SQLException ex) {
                            String msg = "Failed to shutdown DB";
                            LOGGER.error(msg, ex);
                        } finally {
                            dataSource.close();
                        }
                    }
                } finally {
                    super.destroy();
                }
            }
        }
    }

}
