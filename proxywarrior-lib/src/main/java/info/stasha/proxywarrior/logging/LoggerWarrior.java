package info.stasha.proxywarrior.logging;

import info.stasha.proxywarrior.ProxyAction;
import info.stasha.proxywarrior.config.Metadata;

/**
 *
 * @author stasha
 */
public interface LoggerWarrior {

    public void log(ProxyAction action, Metadata metadata);
}
