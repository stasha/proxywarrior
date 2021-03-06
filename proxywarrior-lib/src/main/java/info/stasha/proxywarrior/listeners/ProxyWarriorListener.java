package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.config.Metadata;

/**
 * Listener that will be invoked in
 * request/proxy-request/proxy-response/response stages
 *
 * @author stasha
 */
public interface ProxyWarriorListener {

    /**
     * Method invoked on ProxyWarrior initialization time.
     *
     * @param proxyWarrior
     */
    void init(ProxyWarrior proxyWarrior);

    /**
     * Method invoked when ProxyWarrior instance is destroyed.
     *
     * @param proxyWarrior
     */
    void destroy(ProxyWarrior proxyWarrior);

    /**
     * Invoked after HttpServletRequest occurs. In this stage request data are
     * not changed but could be changed by previously invoked listeners. The
     * method is invoked only if request will be proxy-ed.
     *
     * @param metadata
     */
    void afterHttpRequest(Metadata metadata);

    /**
     * Invoked after HttpServletRequest occurs. In this stage request data are
     * not changed but could be changed by previously invoked listeners. The
     * method is invoked only if request is not proxy (autoProxy = false) or if
     * autoProxy time expired.
     *
     * @param metadata
     */
    void afterNotProxyRequest(Metadata metadata);

    /**
     * Invoked before proxy request. In this stage request data may be changed
     * based on configuration or by previously invoked listeners.
     *
     * @param metadata
     */
    void beforeProxyRequest(Metadata metadata);

    /**
     * Invoked after proxy response. In this stage response data are not changed
     * but could be changed by previously invoked listeners.
     *
     * @param metadata
     */
    void afterProxyResponse(Metadata metadata);

    /**
     * Invoked before HttpServletResponse is returned to client. In this stage
     * response data may be changed based on configuration or by previously
     * invoked listeners.
     *
     * @param metadata
     */
    void beforeHttpResponse(Metadata metadata);

    /**
     * Method invoked after HttpServletResponse is returned to client. In this
     * stage it doesn't matter if request data are changed or not.
     *
     * @param metadata
     */
    void afterHttpResponse(Metadata metadata);
}
