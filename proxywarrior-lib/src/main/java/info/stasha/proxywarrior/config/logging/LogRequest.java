package info.stasha.proxywarrior.config.logging;

import info.stasha.proxywarrior.config.logging.AbstractLog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author stasha
 */
public class LogRequest extends AbstractLog {

    private Boolean url;
    private Boolean proxyUrl;

    public LogRequest() {
    }

    public LogRequest(Boolean url, Boolean proxyUrl) {
        this.url = url;
        this.proxyUrl = proxyUrl;
    }

    public boolean isUrl() {
        return url;
    }

    public void setUrl(Boolean url) {
        this.url = url;
    }

    public boolean isProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(Boolean proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

}
