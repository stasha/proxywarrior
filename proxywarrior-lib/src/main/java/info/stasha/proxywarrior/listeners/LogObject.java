package info.stasha.proxywarrior.listeners;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stasha
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogObject.class);

    private Long id;
    private Date time;
    private String type;
    private String url;
    private String proxyUrl;
    private String proxyUri;
    private String status;
    private String method;
    private String headers;
    @JsonIgnore
    private InputStream contentStream;

    public LogObject() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getProxyUri() {
        return proxyUri;
    }

    public void setProxyUri(String proxyUri) {
        this.proxyUri = proxyUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        if (contentStream != null) {
            this.contentStream = new BufferedInputStream(contentStream);
        }
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n\n------ " + this.getType() + " ------\n");

        if (this.getId() != null) {
            sb.append("id: ").append(this.getId()).append("\n");
        }
        if (this.getTime() != null) {
            sb.append("time: ").append(new SimpleDateFormat("dd-MMM yyyy HH:mm:ss:SSSS").format(this.getTime())).append("\n");
        }
        if (this.getUrl() != null) {
            sb.append("url: ").append(this.getUrl()).append("\n");
        }
        if (this.getProxyUri() != null) {
            sb.append("proxyUri: ").append(this.getProxyUri()).append("\n");
        }
        if (this.getProxyUrl() != null) {
            sb.append("proxyUrl: ").append(this.getProxyUrl()).append("\n");
        }
        if (this.getStatus() != null) {
            sb.append("status: ").append(this.getStatus()).append("\n");
        }
        if (this.getMethod() != null) {
            sb.append("method: ").append(this.getMethod()).append("\n");
        }
        if (this.getHeaders() != null) {
            sb.append("headers: ").append("\n").append(this.getHeaders());
        }
        //TODO: FIX STREAMING CONTENT TO CONSOLE
        if (this.getContentStream() != null) {
            sb.append("content: ").append("\n  ").append(
                    new BufferedReader(new InputStreamReader(this.getContentStream())).lines().collect(Collectors.joining("\n  "))
            ).append("\n");

            IOUtils.closeQuietly(this.getContentStream());
        }

        return sb.toString();
    }

}
