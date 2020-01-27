package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import info.stasha.proxywarrior.listeners.Listeners;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Common configuration inherited by RequestConfig and ResponseConfig.
 *
 * @author stasha
 * @param <T>
 */
public abstract class CommonConfig<T extends CommonConfig> {

    @JsonIgnore
    private CommonConfig parent;

    private String url;
    private String method;
    private String requestHeader;
    @JsonIgnore
    private Pattern urlPattern;
    @JsonIgnore
    private Pattern methodPattern;
    @JsonIgnore
    private Pattern requestHeaderPattern;
    private String removeHeaders;
    @JsonIgnore
    private Pattern removeHeadersPattern;
    private Headers headers;

    private Listeners listeners;

    private Logging logging;

    private String model;
    @JsonIgnore
    private Model modelObject;
    private Map<String, String> properties;
    private Cache cache;

    /**
     * Returns instance information (used for debugging purposes).
     *
     * @return
     */
    public String getInstance() {
        return super.toString();
    }

    /**
     * Returns parent configuration.
     *
     * @return
     */
    public T getParent() {
        return (T) parent;
    }

    /**
     * Returns parent configuration only if it matches passed class.
     *
     * @return
     */
    public T getParent(Class<T> cls) {
        if (parent.getClass() == cls) {
            return (T) parent;
        }
        return null;
    }

    /**
     * Sets parent configuration.
     *
     * @param parent
     */
    public void setParent(CommonConfig parent) {
        this.parent = parent;
    }

    /**
     * Return request url pattern.<br>
     * This pattern will be used when matching request url.<br>
     * Default pattern to match all request urls is "*".
     *
     * @return
     */
    public String getUrl() {
        return Utils.getValue(url, this, getParent(), () -> getParent().getUrl(), null);
    }

    /**
     * Sets url pattern for matching request urls.<br>
     *
     * @see #getUrl()
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
        this.setUrlPattern(Utils.getDefaultPattern(url));
    }

    /**
     * Returns compiled request url regex pattern.<br>
     * This pattern will be used for matching request urls.
     *
     * @see #getUrl()
     * @return
     */
    public Pattern getUrlPattern() {
        return Utils.getValue(urlPattern, this, parent, () -> getParent().getUrlPattern(), null);
    }

    /**
     * Sets compiled url regex pattern.<br>
     * This pattern will be used for matching request urls.
     *
     * @param urlPattern
     */
    public void setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    /**
     * Returns request/response method matching pattern.<br>
     * This could be string like "POST" or "GET|POST|PUT".<br>
     * Pattern to match all request methods is "*".
     *
     * @return
     */
    public String getMethod() {
        return Utils.getValue(method, this, parent, () -> getParent().getMethod(), null);
    }

    /**
     * Sets request/response method matching pattern.<br>
     *
     * @see #getMethod()
     * @param method
     */
    public void setMethod(String method) {
        this.method = method;
        this.setMethodPattern(Utils.getDefaultPattern(method));
    }

    /**
     * Returns request/response compiled regex pattern.
     *
     * @see #getMethod()
     * @return
     */
    public Pattern getMethodPattern() {
        return Utils.getValue(methodPattern, this, parent, () -> getParent().getMethodPattern(), null);
    }

    /**
     * Sets compiled request/response method matching pattern.<br>
     *
     * @see #getMethod()
     * @param methodPattern
     */
    public void setMethodPattern(Pattern methodPattern) {
        this.methodPattern = methodPattern;
    }

    /**
     * Returns request/response requestHeader matching pattern.<br>
     * This pattern will be used when matching requests/responses.<br>
     * Example pattern: "Content-Type: application/json" - this pattern will
     * match only requests/responses that have this requestHeader.
     *
     * @return
     */
    public String getRequestHeader() {
        return Utils.getValue(requestHeader, this, parent, () -> getParent().getRequestHeader(), null);
    }

    /**
     * Sets request/response requestHeader matching pattern.<br>
     *
     * @see #getRequestHeader()
     * @param requestHeader
     */
    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
        this.setRequestHeaderPattern(Utils.getDefaultPattern(this.requestHeader));
    }

    /**
     * Returns compiled regex request/response requestHeader matching pattern.
     *
     * @see #getRequestHeader()
     * @return
     */
    public Pattern getRequestHeaderPattern() {
        return Utils.getValue(requestHeaderPattern, this, parent, () -> getParent().getRequestHeaderPattern(), null);
    }

    /**
     * Sets compiled regex request/response requestHeader matching pattern.
     *
     * @see #getRequestHeader()
     * @param requestHeaderPattern
     */
    public void setRequestHeaderPattern(Pattern requestHeaderPattern) {
        this.requestHeaderPattern = requestHeaderPattern;
    }

    /**
     * Returns pattern for removing headers.<br>
     * Pattern for removing all headers is "*".
     *
     * @return
     */
    public String getRemoveHeaders() {
        return Utils.getValue(removeHeaders, this, getParent(), () -> getParent().getRemoveHeaders(), null);
    }

    /**
     * Sets pattern for removing readers.
     *
     * @see #getRemoveHeaders()
     * @param removeHeaders
     */
    public void setRemoveHeaders(String removeHeaders) {
        this.removeHeaders = removeHeaders;
        this.setRemoveHeadersPattern(Utils.getDefaultPattern(removeHeaders));
    }

    /**
     * Sets compiled regex pattern for removing headers.
     *
     * @see #getRemoveHeaders()
     * @param removeHeadersPattern
     */
    public void setRemoveHeadersPattern(Pattern removeHeadersPattern) {
        this.removeHeadersPattern = removeHeadersPattern;
    }

    /**
     * Returns compiled regex pattern for removing headers.
     *
     * @see #getRemoveHeaders()
     * @return
     */
    public Pattern getRemoveHeadersPattern() {
        return Utils.getValue(removeHeadersPattern, this, getParent(), () -> getParent().getRemoveHeadersPattern(), null);
    }

    /**
     * Returns headers configuration<br>
     *
     * @return
     */
    public Headers getHeaders() {
        return Headers.getHeaders(headers, getParent() != null ? getParent().getHeaders() : null);
    }

    /**
     * Sets headers configuration.
     *
     * @param headers
     */
    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    /**
     * Returns Set of listeners that will be invoked on different proxy actions.
     *
     * @return
     */
    public Listeners getListeners() {
        return Utils.getValue(listeners, this, getParent(), () -> getParent().getListeners(), null);
    }

    /**
     * Sets listeners that will be invoked on different proxy actions.
     *
     * @param listeners
     */
    public void setListeners(Listeners listeners) {
        this.listeners = listeners;
    }

    /**
     * Returns logging configuration.
     *
     * @return
     */
    public Logging getLogging() {
        return Utils.getValue(logging, this, getParent(), () -> getParent().getLogging(), null);
    }

    /**
     * Sets logging configuration.
     *
     * @param logging
     */
    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    /**
     * Returns fully qualified class name that will be used for creating Model
     * instance.
     *
     * @return
     */
    public String getModel() {
        return Utils.getValue(model, this, getParent(), () -> getParent().getModel(), null);
    }

    /**
     * Sets fully qualified class name that will be used for creating Model
     * instance.
     *
     * @param model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Returns Model object.
     *
     * @return
     */
    public Model getModelObject() {
        return modelObject == null ? (modelObject = Utils.newObject(getModel())) : modelObject;
    }

    /**
     * Returns configuration properties. Configuration properties are used for
     * "grouping" common properties in one place so there is no need for using
     * same keys/values over configuration.<br>
     * Example:<br>
     * <p>
     * properties:<br>
     * &nbsp;&nbsp;host: 192.168.2.1<br>
     * </p>
     * <p>
     * can be used in configuration like: _[host]_.<br>
     * _[host]_ will be replaced by 192.168.2.1</p>
     *
     *
     * @return
     */
    public Map<String, String> getProperties() {
        return Utils.getMap(properties, getParent() != null ? getParent().getProperties() : null);
    }

    /**
     * Returns response cache configuration.
     *
     * @return
     */
    public Cache getCache() {
        return Utils.getValue(cache, this, getParent(), () -> getParent().getCache(), null);
    }

    /**
     * Sets response cache configuration.
     *
     * @param cache
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * Disposes resources.
     */
    public void dispose() {

    }

    @Override
    public String toString() {
        return "CommonsConfig{" + "url=" + url + ", method=" + method + ", header=" + requestHeader + '}';
    }

}
