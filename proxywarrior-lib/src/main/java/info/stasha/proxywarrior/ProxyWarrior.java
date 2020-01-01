package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Utils;
import com.zaxxer.hikari.HikariDataSource;
import info.stasha.proxywarrior.config.CommonConfig;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.RequestConfig;
import info.stasha.proxywarrior.config.HttpClientConfig;
import info.stasha.proxywarrior.config.loader.ConfigLoader;
import info.stasha.proxywarrior.config.ResponseConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.util.EntityUtils;
import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCClobClient;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.RowListener;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

/**
 *
 * @author stasha
 */
public class ProxyWarrior extends ProxyServlet implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ProxyWarrior.class.getName());
    private static final ThreadLocal<Metadata> REQUEST_METADATA = new ThreadLocal<>();

    private final Timer CONFIG_RELOAD_TIMER = new Timer(true);
    private String propsLocation;
    private RequestConfig config; //= new RequestConfig();
    private FilterConfig filterConfig;
    private HikariDataSource hikariDs;

    protected void initDb() throws SQLException {
        File proxywarriorDbDir = Paths.get(System.getProperty("user.home"), ".proxywarrior").toFile();
        proxywarriorDbDir.mkdirs();

        String proxywarriorJdbcConnection = "jdbc:hsqldb:" + proxywarriorDbDir + File.separator + "proxywarrior";

        this.hikariDs = new HikariDataSource();
        this.hikariDs.setJdbcUrl(proxywarriorJdbcConnection);
        this.hikariDs.setUsername("SA");
        this.hikariDs.setPassword("");

        Flyway.configure().dataSource(this.hikariDs).load().migrate();

        Base.open(hikariDs);

//        List<Configuration> c = Configuration.findAll();

//        try {
//            System.out.println(IOUtils.toString(((JDBCClobClient) c.get(0).get("configuration")).getCharacterStream()));
//        } catch (IOException ex) {
//            Logger.getLogger(ProxyWarrior.class.getName()).log(Level.SEVERE, null, ex);
//        }

        Base.exec("insert into CONFIGURATIONS (CONFIGURATION) values ('stasha')");
        Base.find("select * from CONFIGURATIONS").with(new RowListener() {
            @Override
            public boolean next(Map<String, Object> row) {
                System.out.println(row);
                return true;
            }
        });
        Base.commitTransaction();
        Base.close();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
        this.filterConfig = filterConfig;

//        try {
//            initDb();
//        } catch (SQLException ex) {
//            Logger.getLogger(ProxyWarrior.class.getName()).log(Level.SEVERE, null, ex);
//        }
        propsLocation = System.getProperty("proxywarrior.config");
        if (propsLocation == null) {
            propsLocation = filterConfig.getInitParameter("PROPS_LOCATION");
        }
        try {
            this.config = ConfigLoader.load(propsLocation);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load configuration from path: " + propsLocation, ex);
        }
        if (propsLocation != null) {
            CONFIG_RELOAD_TIMER.scheduleAtFixedRate(new ConfigLoader(propsLocation, (c) -> {
                this.config = c;
            }), 0, 1000 * 10);
        }
    }

    private String getConfigParam(Object value, String defaultValue) {
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
        requestConfig.getLoggingLogger().log(ProxyAction.PROXY_REQUEST_BEGIN, metadata);

        BasicHttpResponseWrapper proxyResponse = new BasicHttpResponseWrapper(super.doExecute(servletRequest, servletResponse, proxyRequest));

        metadata.setProxyResponse(proxyResponse);
        requestConfig.getLoggingLogger().log(ProxyAction.PROXY_REQUEST_END, metadata);

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
        metadata.setResponse(metadata.getRequestConfig().getResponse(metadata));
        ResponseConfig responseConfig = metadata.getResponseConfig();
        BasicHttpResponseWrapper proxyResponse = (BasicHttpResponseWrapper) metadata.getProxyResponse();
        HttpEntity entity = null;
        try {

            entity = proxyResponse.getOriginalEntity();

            if (entity != null) {
                String text = metadata.getResponseConfig().getText();
                String file = metadata.getResponseConfig().getFile();

                if (text != null) {
                    ByteArrayInputStream bas = new ByteArrayInputStream(text.getBytes());
                    overrideHeaders(proxyResponse, (CommonConfig) responseConfig);

                    proxyResponse.setHeader("Content-Length", String.valueOf(text.getBytes().length));
                    proxyResponse.removeHeaders("Content-Encoding");

                    copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

                    IOUtils.copy(bas, servletResponse.getOutputStream());
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

                    IOUtils.copy(new FileInputStream(f), servletResponse.getOutputStream());
                } else {
                    IOUtils.copy(entity.getContent(), servletResponse.getOutputStream());
                }

            } else {
                overrideHeaders(proxyResponse, (CommonConfig) responseConfig);
                copyResponseHeaders(proxyResponse, servletRequest, servletResponse);
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
            chain.doFilter(request, response);
        } else {
            req.getLoggingLogger().log(ProxyAction.REQUEST_BEGIN, metadata);

            getHttpClient(metadata);

            super.service(request, response);
        }
    }

    @Override
    public void destroy() {
        try {
            if (hikariDs != null) {
                hikariDs.close();
            }
        } finally {
            try {
                if (config != null) {
                    config.dispose();
                }
            } finally {
                try {
                    CONFIG_RELOAD_TIMER.cancel();
                } finally {
                    super.destroy();
                }
            }
        }
    }

}
