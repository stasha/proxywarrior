package info.stasha.proxywarrior.logging.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author stasha
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogObject {

    private static final Logger LOGGER = Logger.getLogger(LogObject.class.getName());

    private String id;
    @JsonIgnore
    private String url;
    private String proxyUrl;
    private String status;
    private Integer code;
    private String method;
    private String host;
    private Integer port;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String content;
    @JsonIgnore
    private InputStream contentStream;
    private Date time;

    public LogObject() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContent() {
        try {
            return content != null ? content : contentStream != null ? IOUtils.toString(contentStream, "UTF-8") : null;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to read contentStream", ex);
        }
        return null;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.getId() != null) {
            sb.append("  id: ").append(this.getId()).append("\n");
        }
        if (this.getTime() != null) {
            sb.append("  time: ").append(new SimpleDateFormat("dd-MMM yyyy HH:mm:ss:SSSS").format(this.getTime())).append("\n");
        }
        if (this.getUrl() != null) {
            sb.append("  url: ").append(this.getUrl()).append("\n");
        }
        if (this.getProxyUrl() != null) {
            sb.append("  proxyUrl: ").append(this.getProxyUrl()).append("\n");
        }
        if (this.getStatus() != null) {
            sb.append("  status: ").append(this.getStatus()).append("\n");
        }
        if (this.getCode() != null) {
            sb.append("  code: ").append(this.getCode()).append("\n");
        }
        if (this.getMethod() != null) {
            sb.append("  method: ").append(this.getMethod()).append("\n");
        }
        if (this.getHost() != null) {
            sb.append("  host: ").append(this.getHost()).append("\n");
        }
        if (this.getPort() != null) {
            sb.append("  port: ").append(this.getPort()).append("\n");
        }
        if (!this.getHeaders().isEmpty()) {
            sb.append("  headers: ").append("\n");
            for (String key : this.getHeaders().keySet()) {
                sb.append("    ").append(key).append(": ").append(this.getHeaders().get(key)).append("\n");
            }
        }
        if (this.getContent() != null) {
            sb.append("content: ").append(this.getContent());
        }

        return sb.toString();
    }

}
