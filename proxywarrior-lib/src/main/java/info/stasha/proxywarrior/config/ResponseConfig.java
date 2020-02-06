package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.regex.Pattern;

/**
 * Response configuration.
 *
 * @author stasha
 */
public class ResponseConfig extends CommonConfig<ResponseConfig> {

    private String text;
    private String file;
    @JsonIgnore
    private Pattern filePattern;
    private String responseHeader;

    /**
     * Creates new ResponseConfig instance.
     */
    public ResponseConfig() {
    }

    /**
     * Creates new ResponseConfig instance with specified URL matcher.
     *
     * @param url
     */
    public ResponseConfig(String url) {
        super.setUrl(url);
    }

    /**
     * Returns text that should be returned in response.
     *
     * @return
     */
    public String getText() {
        return Utils.getValue(text, this, getParent(ResponseConfig.class), () -> getParent().getText(), null);
    }

    /**
     * Sets text that should be returned in response.
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns pattern that will be used to read file from disc and returned as
     * response.
     *
     * @return
     */
    public String getFile() {
        return Utils.getValue(file, this, getParent(ResponseConfig.class), () -> getParent().getFile(), null);
    }

    /**
     * Sets pattern that will be used to read file from disc and returned as
     * response.
     *
     * @param file
     */
    public void setFile(String file) {
        this.file = file;
        if (file.contains("$")) {
            this.setFilePattern(Utils.getDefaultPattern(file));
        }
    }

    /**
     * Returns compiled regex file pattern.
     *
     * @see #getFile()
     * @return
     */
    public Pattern getFilePattern() {
        return Utils.getValue(filePattern, this, getParent(ResponseConfig.class), () -> getParent().getFilePattern(), null);
    }

    /**
     * Sets compiled regex pattern.
     *
     * @see #setFile(java.lang.String)
     * @param filePattern
     */
    public void setFilePattern(Pattern filePattern) {
        this.filePattern = filePattern;
    }

    /**
     * Returns pattern that will be used to match response header.
     *
     * @return
     */
    public String getResponseHeader() {
        return Utils.getValue(responseHeader, this, getParent(ResponseConfig.class), () -> getParent().getResponseHeader(), null);
    }

    /**
     * Sets pattern that will be used to match response header.
     *
     * @param responseHeader
     */
    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
        this.setResponseHeaderPattern(Pattern.compile(responseHeader));
    }

    /**
     * Returns compiled regex pattern that will be used for matching response
     * header.
     *
     * @return
     */
    public Pattern getResponseHeaderPattern() {
        return Utils.getValue(responseHeaderPattern, this, getParent(ResponseConfig.class), () -> {
            if (this.urlPattern == null && this.methodPattern == null && this.requestHeaderPattern == null) {
                return getParent().getResponseHeaderPattern();
            }
            return null;
        }, null);
    }

    /**
     * Sets compiled regex pattern that will be used for matching response
     * header.
     *
     * @param responseHeaderPattern
     */
    public void setResponseHeaderPattern(Pattern responseHeaderPattern) {
        this.responseHeaderPattern = responseHeaderPattern;
    }

}
