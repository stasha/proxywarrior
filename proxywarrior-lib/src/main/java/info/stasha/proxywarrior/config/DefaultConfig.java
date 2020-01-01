package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.config.logging.LogConfig;
import info.stasha.proxywarrior.config.logging.LogRequest;
import info.stasha.proxywarrior.config.logging.LogResponse;
import info.stasha.proxywarrior.logging.loggers.ConsoleLogger;

/**
 * Default ProxyWarrior configuration.
 *
 * @author stasha
 */
public class DefaultConfig extends RequestConfig {

    public DefaultConfig() {

        super.setId("default-configration");

        LogConfig log = new LogConfig();
        log.setHeaders(false);
        log.setMatchedUrl(true);
        log.setRequest(new LogRequest(true, true));
        log.setResponse(new LogResponse());
        log.setContent(false);

        HttpClientConfig cc = new HttpClientConfig();
        cc.setConnectTimeout(-1);
        cc.setConnectionRequestTimeout(-1);
        cc.setHandleRedirects(false);
        cc.setMaxConnections(-1);
        cc.setReadTimeout(-1);
        cc.setUseSystemProperties(true);

        super.setUrl("*");
        super.setAutoProxy(true);
        super.setLog(log);
        super.setLogger(ConsoleLogger.class.getName());
        super.setClientConfig(cc);
    }

}
