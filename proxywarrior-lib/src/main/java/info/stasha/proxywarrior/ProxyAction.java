package info.stasha.proxywarrior;

/**
 * ProxyWarrior actions.
 *
 * @author stasha
 */
public enum ProxyAction {
    /**
     * Request has just started.<br>
     * This is clean not modified request received from client.<br>
     * It is invoked when request enters doFilter method.
     */
    REQUEST_BEGIN,
    /**
     * Request is fully prepared to be proxied/forwarded.<br>
     * The request contains all modifications specified in configuration.
     */
    PROXY_REQUEST_BEGIN,
    /**
     * Proxied/forwarded response just arrived.<br>
     * This is clean not modified response from client.
     */
    PROXY_REQUEST_END,
    /**
     * Response is just to be sent to the client.<br>
     * The response contains all modifications specified in configuration.
     */
    RESPONSE_START
}
