package info.stasha.proxywarrior.config;

/**
 * Logging configuration.
 *
 * @author stasha
 */
public class Logging {

    private Boolean enabled;
    private Boolean httpRequestContent;
    private Boolean proxyRequestContent;
    private Boolean proxyResponseContent;
    private Boolean httpResponseContent;

    /**
     * Returns true/false if logging of all data (URLs, headers and content) is
     * enabled.
     *
     * @return
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enables/disables logging of all data (URLs, headers and content).
     *
     * @param enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns true/false if HttpServletRequest content should be logged.
     *
     * @return
     */
    public Boolean getHttpRequestContent() {
        return httpRequestContent;
    }

    /**
     * Enables/disables HttpServletRequest content logging.
     *
     * @param httpRequestContent
     */
    public void setHttpRequestContent(Boolean httpRequestContent) {
        this.httpRequestContent = httpRequestContent;
    }

    /**
     * Returns true/false if proxy request content should be logged.
     *
     * @return
     */
    public Boolean getProxyRequestContent() {
        return proxyRequestContent;
    }

    /**
     * Enables/disables proxy request content logging.
     *
     * @param proxyRequestContent
     */
    public void setProxyRequestContent(Boolean proxyRequestContent) {
        this.proxyRequestContent = proxyRequestContent;
    }

    /**
     * Returns true/false if proxy response content should be logged.
     *
     * @return
     */
    public Boolean getProxyResponseContent() {
        return proxyResponseContent;
    }

    /**
     * Enables/disables proxy response content logging.
     *
     * @param proxyResponseContent
     */
    public void setProxyResponseContent(Boolean proxyResponseContent) {
        this.proxyResponseContent = proxyResponseContent;
    }

    /**
     * Returns true/false if HttpServletResponse content should be logged.
     *
     * @return
     */
    public Boolean getHttpResponseContent() {
        return httpResponseContent;
    }

    /**
     * Enables/disables HttpServletResponse content logging.
     *
     * @param httpResponseContent
     */
    public void setHttpResponseContent(Boolean httpResponseContent) {
        this.httpResponseContent = httpResponseContent;
    }

    /**
     * Combines parent child and parent logging configurations.
     *
     * @param childLogging
     * @param parentLogging
     * @return
     */
    public static Logging getLogging(Logging childLogging, Logging parentLogging) {
        Logging logging = childLogging != null ? childLogging : new Logging();
        if (parentLogging != null) {
            if (logging.getEnabled() == null) {
                logging.setEnabled(parentLogging.getEnabled());
            }
            if (logging.getHttpRequestContent() == null) {
                logging.setHttpRequestContent(parentLogging.getHttpRequestContent());
            }
            if (logging.getHttpResponseContent() == null) {
                logging.setHttpResponseContent(parentLogging.getHttpResponseContent());
            }
            if (logging.getProxyRequestContent() == null) {
                logging.setProxyRequestContent(parentLogging.getProxyRequestContent());
            }
            if (logging.getProxyResponseContent() == null) {
                logging.setProxyResponseContent(parentLogging.getProxyResponseContent());
            }
        }

        return logging;
    }

}
