package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.BasicHttpResponseWrapper;
import info.stasha.proxywarrior.ProxyWarrior;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;
import org.javalite.activejdbc.Base;

/**
 * Central DTO used in all proxywarrior actions.
 *
 * @author stasha
 */
public class Metadata {

    private long id;
    private String fullUrl;
    private String path;
    private String proxyUrl;
    private String proxyUri;
    private RequestConfig requestConfig;
    private ResponseConfig responseConfig;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private HttpRequest proxyRequest;
    private BasicHttpResponseWrapper proxyResponse;
    private CacheResult cacheResult;
    private ProxyWarrior proxywarrior;

    /**
     * Creates Metadata object with unique id.<br>
     * This id is used to bind together all needed request/response
     * proxyRequest/proxyResponse data.
     *
     * @param id
     */
    public Metadata(long id) {
        this.id = id;
    }

    /**
     * Returns metadata id.
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Returns request full URL.
     *
     * @return
     */
    public String getFullUrl() {
        return fullUrl;
    }

    /**
     * Sets request full URL.
     *
     * @param fullUrl
     */
    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    /**
     * Returns URL path. The path is calculated from end of context till the url
     * end including query params.
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets request path.
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns proxy URL.
     *
     * @return
     */
    public String getProxyUrl() {
        return proxyUrl;
    }

    /**
     * Sets proxy URL.
     *
     * @param proxyUrl
     */
    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    /**
     * Returns proxy URI.
     *
     * @return
     */
    public String getProxyUri() {
        return proxyUri;
    }

    /**
     * Sets proxy URI.
     *
     * @param proxyUri
     */
    public void setProxyUri(String proxyUri) {
        this.proxyUri = proxyUri;
    }

    /**
     * Returns RequestConfig.
     *
     * @return
     */
    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    /**
     * Sets RequestConfig.
     *
     * @param requestConfig
     */
    public void setRequest(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    /**
     * Returns ResponseConfig.
     *
     * @return
     */
    public ResponseConfig getResponseConfig() {
        return responseConfig;
    }

    /**
     * Sets ResponseConfig.
     *
     * @param responseConfig
     */
    public void setResponseConfig(ResponseConfig responseConfig) {
        this.responseConfig = responseConfig;
    }

    /**
     * Returns HttpServletRequest.
     *
     * @return
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /**
     * Sets HttpServletRequest.
     *
     * @param httpServletRequest
     */
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * Returns HttpServletResponse.
     *
     * @return
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    /**
     * Sets HttpServletResponse.
     *
     * @param httpServletResponse
     */
    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    /**
     * Returns HttpRequest (HttpClient request)
     *
     * @return
     */
    public HttpRequest getProxyRequest() {
        return proxyRequest;
    }

    /**
     * Sets HttpRequest (HttpClient request)
     *
     * @param proxyRequest
     */
    public void setProxyRequest(HttpRequest proxyRequest) {
        this.proxyRequest = proxyRequest;
    }

    /**
     * Returns HttpResponse (HttpClient responseConfig)
     *
     * @return
     */
    public BasicHttpResponseWrapper getProxyResponse() {
        return proxyResponse;
    }

    /**
     * Sets HttpResponse (HttpClient responseConfig)
     *
     * @param proxyResponse
     */
    public void setProxyResponse(BasicHttpResponseWrapper proxyResponse) {
        this.proxyResponse = proxyResponse;
    }

    public CacheResult getCacheResult() {
        return cacheResult;
    }

    public void setCacheResult(CacheResult cacheResult) {
        this.cacheResult = cacheResult;
    }

    public static class CacheResult {

        private Timestamp ts;
        private Long id;
        private String method;
        private String path;

        public Long getId() {
            return id;
        }

    }

    /**
     * If DB record needs to be inserted/updated:<br>
     * a. returns -1 if record needs to be inserted into DB<br>
     * b. returns ID of the record that needs to be updated<br>
     * c. returns null if record should not be inserted/updated<br>
     * d. returns -ID of the record if record has expired<br>
     *
     * Insert/update record:<br>
     * a. if cache is enabled<br>
     * b. if cache is disabled but expiration time is greater then 0<br>
     * c. if logging is enabled<br>
     *
     * @param config
     * @return
     */
    public Long shouldUpdateDbRecord(CommonConfig config) {
        Cache cache = config.getCache();
        Logging logging = config.getLogging();

        if (cacheResult == null) {
            this.setCacheResult(new CacheResult());

            if (Boolean.TRUE.equals(logging.getEnabled())) {
                cacheResult.id = new Long(-1);
            }

            if (cache.getExpirationTime() > 0) {
                Base.find("SELECT REQUEST_ID, REQUEST_TIME, REQUEST_METHOD, REQUEST_PATH FROM REQUEST WHERE CONFIG_ID = ? AND REQUEST_PATH = ? AND REQUEST_METHOD = ?",
                        this.getRequestConfig().getId(), this.getPath(), this.getHttpServletRequest().getMethod()).with((row) -> {
                    cacheResult.id = (Long) row.get("REQUEST_ID");
                    cacheResult.ts = (Timestamp) row.get("REQUEST_TIME");
                    cacheResult.method = (String) row.get("REQUEST_METHOD");
                    cacheResult.path = (String) row.get("REQUEST_PATH");
                    return false;
                });
            }
        } else {
            return cacheResult.id;
        }

        // 1. if cache is enabled
        //      a. insert cache if there is no record
        //      b. update cache if cache expired
        if (Boolean.TRUE.equals(cache.getEnabled())) {
            if (cacheResult.ts == null) {
                cacheResult.id = new Long(-1);
                return cacheResult.id;
            } else {
                // setting request time + expiration time as future time
                Date date = new Date(cacheResult.ts.getTime() + (cache.getExpirationTime() * 1000));
                // in case current time is greater then future time then update cache
                if (Calendar.getInstance().getTime().after(date)) {
                    cacheResult.id = -cacheResult.id;
                }
                return cacheResult.id;
            }
        } // 2. if cache is disabled and expiration time is set
        //      a. update cache
        else {
            if (cache.getExpirationTime() > 0) {
                if (cacheResult.ts == null) {
                    cacheResult.id = new Long(-1);
                } else {
                    cacheResult.id = -cacheResult.id;
                }
                return cacheResult.id;
            }
        }

        // 3. if logging is enabled
        if (Boolean.TRUE.equals(logging.getEnabled())) {
            return new Long(-1);
        }

        return null;
    }
}
