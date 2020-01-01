package info.stasha.proxywarrior.config.logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.stasha.proxywarrior.config.Utils;

/**
 *
 * @author stasha
 */
public abstract class AbstractLog {

    private String instance;

    @JsonIgnore
    protected AbstractLog parent;

    protected Boolean content;
    protected Boolean matchedUrl;
    protected Boolean headers;

    public AbstractLog() {
    }

    public String getInstance() {
        return super.toString();
    }

    public AbstractLog getParent() {
        return parent;
    }

    public void setParent(AbstractLog parent) {
        this.parent = parent;
    }

    public Boolean isMatchedUrl() {
        return Utils.getValue(matchedUrl, null, getParent(), () -> getParent().isMatchedUrl(), null);
    }

    public void setMatchedUrl(Boolean matchedUrl) {
        this.matchedUrl = matchedUrl;
    }

    public Boolean isHeaders() {
        return Utils.getValue(headers, null, getParent(), () -> getParent().isHeaders(), null);
    }

    public void setHeaders(Boolean headers) {
        this.headers = headers;
    }

    public Boolean getContent() {
        return Utils.getValue(content, null, getParent(), () -> getParent().getContent(), null);
    }

    public void setContent(Boolean content) {
        this.content = content;
    }

}
