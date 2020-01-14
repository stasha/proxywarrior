package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyAction;
import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.Utils;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Container for storing ProxyWarriorListener-s.
 *
 * @author stasha
 */
public class Listeners extends HashSet<String> {

    private static final Set<ProxyWarriorListener> ALL_LISTENERS = new LinkedHashSet<>();

    private final Map<String, ProxyWarriorListener> LISTENERS = new LinkedHashMap<>();

    @Override
    public boolean add(String listener) {
        if (listener == null) {
            return false;
        }

        ProxyWarriorListener l = Utils.newObject(listener);

        LISTENERS.put(listener, l);
        ALL_LISTENERS.add(l);
        return super.add(listener);
    }

    public boolean remove(String listener) {
        ALL_LISTENERS.remove(LISTENERS.remove(listener));
        return super.remove(listener);
    }

    public void init(ProxyWarrior proxyWarrior) {
        ALL_LISTENERS.forEach((listener) -> {
            listener.init(proxyWarrior);
        });
    }

    public void destroy(ProxyWarrior proxyWarrior) {
        ALL_LISTENERS.forEach((listener) -> {
            listener.destroy(proxyWarrior);
        });
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
        }
    }

}
