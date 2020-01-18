package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.BasicHttpResponseWrapper;
import info.stasha.proxywarrior.ProxyWarrior;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;

/**
 * Central DTO used in all proxywarrior actions.
 *
 * @author stasha
 */
public class Metadata {

    private final long id;
    private String fullUrl;
    private String proxyUrl;
    private String proxyUri;
    private RequestConfig requestConfig;
    private ResponseConfig responseConfig;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private HttpRequest proxyRequest;
    private BasicHttpResponseWrapper proxyResponse;
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
}
