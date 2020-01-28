package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration used for configuring request.
 *
 * @author stasha
 */
public class RequestConfig extends CommonConfig<RequestConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestConfig.class.getName());

    public static final String REQUEST_METADATA = "proxywarriormetadata";
    public static final String TARGET_URI_REGEX = ".*:\\/\\/.*?[\\w.:-]*";

    private static RequestConfig PROXY_WARRIOR_REQUEST_CONFIG;

    private List<RequestConfig> requests = new ArrayList<>();
    private List<ResponseConfig> responses = new ArrayList<>();

    private HttpClientConfig clientConfig;

    private String id;

    private String targetUri;
    @JsonIgnore
    private Pattern targetUriPattern;
    private String targetUrl;
    @JsonIgnore
    private Pattern targetUrlPattern;
    private Boolean autoProxy;
    private Date autoProxyExpireTime;
    private Boolean forwardIp;

    /**
     * Creates new RequestConfig instance.
     */
    public RequestConfig() {
        if (PROXY_WARRIOR_REQUEST_CONFIG == null) {
            PROXY_WARRIOR_REQUEST_CONFIG = new RequestConfig("/proxywarrior.*");
            PROXY_WARRIOR_REQUEST_CONFIG.setAutoProxy(false);
            requests.add(PROXY_WARRIOR_REQUEST_CONFIG);
        }
    }

    /**
     * Creates new RequestConfig instance and sets matching url pattern.
     *
     * @param url
     */
    public RequestConfig(String url) {
        super.setUrl(url);
    }

    /**
     * Returns configuration id.
     *
     * @return
     */
    public String getId() {
        return getParent() != null ? getParent().getId() : id;
    }

    /**
     * Sets request configuration id.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns HttpClient configuration.
     *
     * @return
     */
    public HttpClientConfig getClientConfig() {
        return Utils.getValue(clientConfig, this, getParent(), () -> getParent().getClientConfig(), null);
    }

    /**
     * Sets HttpClient configuration.
     *
     * @param clientConfig
     */
    public void setClientConfig(HttpClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    /**
     * Returns if IP address should be forwarded to remote host or not.
     *
     * @return
     */
    public Boolean isForwardIp() {
        return Utils.getValue(forwardIp, this, getParent(), () -> getParent().isForwardIp(), null);
    }

    /**
     * Sets if IP address should be forwarded to remote host or not.
     *
     * @param forwardIp
     */
    public void setForwardIp(Boolean forwardIp) {
        this.forwardIp = forwardIp;
    }

    /**
     * Returns target URI.<br>
     * If target URI is set, request will be re-created by replacing request URL
     * <protocol><host><port> with this URI.
     *
     * @return
     */
    public String getTargetUri() {
        return Utils.getValue(Utils.getValue(targetUri, this, getParent(), () -> getParent().getTargetUri(), null), this);
    }

    /**
     * Set target URI.<br>
     * If target URI is set, request will be re-created by replacing request URL
     * <protocol><host><port> with this URI.
     *
     * @param targetUri
     */
    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
        this.targetUriPattern = Utils.getDefaultPattern(targetUri);
    }

    /**
     * Returns compiled regex target URI pattern.
     *
     * @see #getTargetUri()
     * @return
     */
    public Pattern getTargetUriPattern() {
        return Utils.getValue(targetUriPattern, this, getParent(), () -> getParent().getTargetUriPattern(), null);
    }

    /**
     * Sets compiled regex target URI pattern.
     *
     * @param targetUriPattern
     */
    public void setTargetUriPattern(Pattern targetUriPattern) {
        this.targetUriPattern = targetUriPattern;
    }

    /**
     * Returns target URL.<br>
     * If target URL is set, request will be forwarded to this URL.
     *
     * @return
     */
    public String getTargetUrl() {
        return Utils.getValue(targetUrl, this, getParent(), () -> getParent().getTargetUrl(), null);
    }

    /**
     * Sets target URL.<br>
     * If target URL is set, request will be forwarded to this URL.
     *
     * @param targetUrl
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
        this.targetUrlPattern = Utils.getPattern(targetUrl, "$0");
    }

    /**
     * Returns compiled regex target URL pattern.
     *
     * @return
     */
    public Pattern getTargetUrlPattern() {
        return Utils.getValue(targetUrlPattern, this, getParent(), () -> getParent().getTargetUrlPattern(), null);
    }

    /**
     * Sets compiled regex target URL pattern.
     *
     * @param targetUrlPattern
     */
    public void setTargetUrlPattern(Pattern targetUrlPattern) {
        this.targetUrlPattern = targetUrlPattern;
    }

    /**
     * Returns if request should be auto-proxied or not.<br>
     * If autoProxy is set to false, then request will be "ignored" by
     * proxywarrior. Request will then hit your controller where you can do with
     * request what you need. Note that request will have attribute
     * "proxywarrior" where you have access to whole proxywarrior configuration.
     * This adds additional flexibility if proxywarrior must be dynamically
     * configured before forwarding request.
     *
     * @return
     */
    public Boolean isAutoProxy() {
        return Utils.getValue(autoProxy, this, getParent(), () -> getParent().isAutoProxy(), null);
    }

    /**
     * Sets autoProxy to true/false.
     *
     * @see #isAutoProxy()
     * @param autoProxy
     */
    public void setAutoProxy(Boolean autoProxy) {
        this.autoProxy = autoProxy;
    }

    /**
     * Auto proxy expiration time in milliseconds.<br>
     * In case requests have some expiration element like authorization token,
     * you can set autoProxy expiration time. After time expires, request will
     * be ignored by proxywarrior, so your request controller will be hit.
     *
     * @see #isAutoProxy()
     * @return
     */
    public Date getAutoProxyExpireTime() {
        return Utils.getValue(autoProxyExpireTime, this, getParent(), () -> getParent().getAutoProxyExpireTime(), null);
    }

    /**
     * Sets auto proxy expiration time.
     *
     * @see #setAutoProxy(java.lang.Boolean)
     * @see #isAutoProxy()
     * @param autoProxyExpireTime
     */
    public void setAutoProxyExpireTime(Integer autoProxyExpireTime) {
        if (this.autoProxyExpireTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, autoProxyExpireTime);
            this.autoProxyExpireTime = calendar.getTime();
        }
    }

    /**
     * Returns proxy url based on passed HttpServletRequest.<br>
     * Proxy url is url where request will be forwarded.
     *
     * @param request
     * @return
     */
    public String getProxyUrl(HttpServletRequest request) {
        String requestUrl = getFullUrl(request);

        if (getTargetUrl() != null) {
            if ("*".equals(getTargetUrl())) {
                return requestUrl;
            }

            return requestUrl.replaceAll(getUrlPattern().pattern(), getTargetUrlPattern().pattern());

        } else if (getTargetUri() != null) {
            if ("*".equals(getTargetUri())) {
                return requestUrl;
            }

            return requestUrl.replaceFirst(TARGET_URI_REGEX, getTargetUri());
        }

        return requestUrl;
    }

    /**
     * Returns proxy URI.<br>
     * If targeUri is set, then returns targetUri otherwise it will return
     * <protocol><host><port> from proxy url.
     *
     * @param request
     * @return
     */
    public String getProxyUri(HttpServletRequest request) {
        String proxyUrl = getProxyUrl(request);
        if (this.getTargetUri() != null && !this.getTargetUri().equals("*")) {
            return this.getTargetUri();
        }
        try {
            URL url = new URL(proxyUrl);
            return url.getProtocol() + "://" + url.getAuthority();
        } catch (MalformedURLException ex) {
            String msg = "Malformed URL " + proxyUrl;
            LOGGER.error(msg);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns full request URL.
     *
     * @param request
     * @return
     */
    public String getFullUrl(final HttpServletRequest request) {
        return request.getQueryString() == null ? request.getRequestURL().toString()
                : request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }

    /**
     * Returns request path including query params.
     *
     * @param req
     * @param url
     * @return
     */
    public String getPath(final HttpServletRequest req, String url) {
        StringBuffer u = req.getRequestURL();
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String base = u.substring(0, u.length() - uri.length() + ctx.length()) + "/";
        return url.replace(base, "");
    }

    /**
     * Returns all configured requests.
     *
     * @return
     */
    public List<RequestConfig> getRequests() {
        if (requests.size() == 1 && requests.iterator().next().getUrl().contains("proxywarrior")) {
            RequestConfig req = new RequestConfig("*");
            if (this.getTargetUri() == null && this.getTargetUrl() == null) {
                req.setTargetUrl("*");
            }
            requests.add(req);
        }
        requests.forEach((r) -> {
            r.setParent(this);
        });

        return requests;
    }

    private boolean matchesToPatterns(String requestUrl, Pattern urlPattern, String method, Pattern methodPattern) {
        boolean matches = false;
        if (urlPattern != null) {
            if (urlPattern.matcher(requestUrl).find()) {
                matches = true;
            }
        }

        if (methodPattern != null) {
            matches = false;
            if (methodPattern.matcher(method).find()) {
                matches = true;
            }
        }
        return matches;
    }

    private boolean matchesRequestHeaderPattern(Metadata metadata, Pattern requestHeaderPattern) {
        HttpServletRequest request = metadata.getHttpServletRequest();

        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            StringBuilder headerBuilder = new StringBuilder();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = request.getHeader(key);
                headerBuilder.append(key).append(":").append(value).append(" ");
            }
            String headers = headerBuilder.toString();
            if (requestHeaderPattern.matcher(headers).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesResonseHeaderPattern(Metadata metadata, Pattern responseHeaderPattern) {
        HttpServletResponse response = metadata.getHttpServletResponse();

        Collection<String> headerNames = response.getHeaderNames();

        if (headerNames != null) {
            StringBuilder headerBuilder = new StringBuilder();
            Iterator<String> iterator = headerNames.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = response.getHeader(key);
                headerBuilder.append(key).append(":").append(value).append(" ");
            }
            String headers = headerBuilder.toString();
            if (responseHeaderPattern.matcher(headers).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns metadata based on HttpServletRequest.<br>
     * Metadata is central object containing all needed info for all actions
     * performed by proxywarrior.
     *
     * @param request
     * @param response
     * @return
     */
    public Metadata getMetadata(HttpServletRequest request, HttpServletResponse response) {
        Metadata metadata = (Metadata) request.getAttribute(REQUEST_METADATA);
        if (metadata != null) {
            return metadata;
        }

        String requestUrl = getFullUrl(request);

        metadata = new Metadata(System.nanoTime() + ThreadLocalRandom.current().nextInt());
        metadata.setHttpServletRequest(request);
        metadata.setHttpServletResponse(response);
        metadata.setFullUrl(requestUrl);
        metadata.setPath(getPath(request, requestUrl));

        for (RequestConfig req : getRequests()) {

            Pattern urlPattern = req.getUrlPattern();
            Pattern methodPattern = req.getMethodPattern();
            Pattern requestHeaderPattern = req.getRequestHeaderPattern();

            boolean matched = matchesToPatterns(requestUrl, urlPattern, request.getMethod(), methodPattern);

            if (requestHeaderPattern != null) {
                matched = matchesRequestHeaderPattern(metadata, requestHeaderPattern);
            }

            if (matched) {
                if (!req.getRequests().isEmpty()) {
                    return req.getMetadata(request, response);
                }
                metadata.setRequest(req);
                metadata.setProxyUrl(req.getProxyUrl(request));
                metadata.setProxyUri(req.getProxyUri(request));
                request.setAttribute(REQUEST_METADATA, metadata);
                return metadata;
            }
        }

        return null;
    }

    /**
     * Returns all configured responses.
     *
     * @return
     */
    public List<ResponseConfig> getResponses() {
        List<ResponseConfig> rsps = this.responses;
        if (this.responses.isEmpty() && getParent() != null) {
            rsps = getParent().getResponses();
        }
        if (rsps.isEmpty()) {
            ResponseConfig resp = new ResponseConfig("*");
            resp.setParent(this);
            rsps.add(resp);
        }
        rsps.forEach((r) -> {
            r.setParent(this);
        });
        return rsps;
    }

    public void setRequests(List<RequestConfig> requests) {
        this.requests = requests;
    }

    public void setResponses(List<ResponseConfig> responses) {
        this.responses = responses;
    }

    /**
     * Returns response for matched Request.
     *
     * @param metadata
     * @return
     */
    public ResponseConfig getResponse(Metadata metadata) {
        HttpServletRequest request = metadata.getHttpServletRequest();

        String requestUrl = metadata.getFullUrl();

        for (ResponseConfig resp : this.getResponses()) {
            Pattern urlPattern = resp.getUrlPattern();
            Pattern methodPattern = resp.getMethodPattern();
            Pattern requestHeaderPattern = resp.getRequestHeaderPattern();
            Pattern responseHeaderPattern = resp.getResponseHeaderPattern();

            boolean matched = matchesToPatterns(requestUrl, urlPattern, request.getMethod(), methodPattern);

            if (requestHeaderPattern != null) {
                matched = matchesRequestHeaderPattern(metadata, requestHeaderPattern);
            }

            if (responseHeaderPattern != null) {
                matched = matchesResonseHeaderPattern(metadata, responseHeaderPattern);
            }

            if (matched) {
                metadata.setResponseConfig(resp);
                resp.setParent(this);
                return resp;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            try {
                HttpClientConfig cc = getClientConfig();
                if (cc != null) {
                    getClientConfig().dispose();
                }
            } finally {
                try {
                    getRequests().forEach((req) -> {
                        if (!req.equals(this)) {
                            req.dispose();
                        }
                    });
                } finally {
                    getResponses().forEach((resp) -> {
                        resp.dispose();
                    });

                    getRequests().clear();
                    getResponses().clear();
                }
            }
        }
    }
}
