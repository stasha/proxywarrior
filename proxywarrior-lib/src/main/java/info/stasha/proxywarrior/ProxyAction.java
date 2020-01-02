package info.stasha.proxywarrior;

/**
 * ProxyWarrior actions.
 *
 * @author stasha
 */
public enum ProxyAction {
    AFTER_HTTP_REQUEST,
    AFTER_NOT_PROXY_REQUEST,
    BEFORE_PROXY_REQUEST,
    AFTER_PROXY_RESPONSE,
    BEFORE_HTTP_RESPONSE
}
