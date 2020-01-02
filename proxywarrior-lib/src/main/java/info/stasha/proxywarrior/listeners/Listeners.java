package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyAction;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.Utils;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container for storing ProxyWarriorListener-s.
 *
 * @author stasha
 */
public class Listeners extends HashSet<String> {

    private final Map<String, ProxyWarriorListener> LISTENERS = new TreeMap<>();

    @Override
    public boolean add(String listener) {
        if (listener == null) {
            return false;
        }

        LISTENERS.put(listener, Utils.newObject(listener));
        return super.add(listener);
    }

    @Override
    public boolean remove(Object listener) {
        LISTENERS.remove(listener);
        return super.remove(listener);
    }

    public void fire(ProxyAction action, final Metadata metadata) {
        switch (action) {
            case AFTER_HTTP_REQUEST:
                LISTENERS.forEach((key, listener) -> {
                    listener.afterHttpRequest(metadata);
                });
                break;
            case AFTER_NOT_PROXY_REQUEST:
                LISTENERS.forEach((key, listener) -> {
                    listener.afterNotProxyRequest(metadata);
                });
                break;
            case BEFORE_PROXY_REQUEST:
                LISTENERS.forEach((key, listener) -> {
                    listener.beforeProxyRequest(metadata);
                });
                break;
            case AFTER_PROXY_RESPONSE:
                LISTENERS.forEach((key, listener) -> {
                    listener.afterProxyResponse(metadata);
                });
                break;
            case BEFORE_HTTP_RESPONSE:
                LISTENERS.forEach((key, listener) -> {
                    listener.beforeHttpResponse(metadata);
                });
                break;
            default:
                throw new UnsupportedOperationException("Action: " + action + " is not supported");
        }
    }

}
