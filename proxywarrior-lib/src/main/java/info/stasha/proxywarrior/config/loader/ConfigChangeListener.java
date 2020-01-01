package info.stasha.proxywarrior.config.loader;

import info.stasha.proxywarrior.config.RequestConfig;

/**
 * Configuration change listener.
 *
 * @author stasha
 */
public interface ConfigChangeListener {

    public void notify(RequestConfig config) throws Exception;
}
