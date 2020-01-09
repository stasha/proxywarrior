package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.config.Metadata;

/**
 * Listener that logs data into DB.
 *
 * @author stasha
 */
public class LogToDbListener implements ProxyWarriorListener {

    @Override
    public void init(ProxyWarrior proxyWarrior) {
        System.out.println("initialized");
    }

    @Override
    public void destroy(ProxyWarrior proxyWarrior) {
        System.out.println("destroyed");
    }

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

    @Override
    public boolean equals(Object obj) {
        return LogToDbListener.class.getName().equals(obj.getClass().getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

}
