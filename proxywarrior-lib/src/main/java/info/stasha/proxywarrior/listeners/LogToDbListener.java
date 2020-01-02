package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.config.Metadata;

/**
 * Listener that logs data into DB.
 *
 * @author stasha
 */
public class LogToDbListener implements ProxyWarriorListener {

    @Override
    public void afterHttpRequest(Metadata metadata) {
        System.out.println("after http request");
    }

    @Override
    public void afterNotProxyRequest(Metadata metadata) {
        System.out.println("after http request that will be not proxied");
    }

    @Override
    public void beforeProxyRequest(Metadata metadata) {
        System.out.println("before proxy request");
    }

    @Override
    public void afterProxyResponse(Metadata metadata) {
        System.out.println("after proxy response");
    }

    @Override
    public void beforeHttpResponse(Metadata metadata) {
        System.out.println("before http response");
    }

}
