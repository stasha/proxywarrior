package info.stasha.proxywarrior.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.HttpClient;

/**
 * HttpClient configuration
 *
 * @author stasha
 */
public class HttpClientConfig {

    private static final Logger LOGGER = Logger.getLogger(HttpClientConfig.class.getName());

    private String instance;

    @JsonIgnore
    private HttpClient httpClient;

    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer connectionRequestTimeout;
    private Integer maxConnections;
    private Boolean useSystemProperties;
    private Boolean handleRedirects;

    public HttpClientConfig() {
    }

    /**
     * Returns class instance (used for debugging purposes)
     *
     * @return
     */
    public String getInstance() {
        return super.toString();
    }

    /**
     * Returns HttpClient used for performing requests.
     *
     * @return
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Sets HttpCient used for performing requests.
     *
     * @param httpClient
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns if HttpClient should handle redirects.
     *
     * @return
     */
    public Boolean getHandleRedirects() {
        return handleRedirects;
    }

    /**
     * Sets if HttpClient should handle redirects.
     *
     * @param handleRedirects
     */
    public void setHandleRedirects(Boolean handleRedirects) {
        this.handleRedirects = handleRedirects;
    }

    /**
     * Returns if HttpClient should use system properties.
     *
     * @return
     */
    public Boolean getUseSystemProperties() {
        return useSystemProperties;
    }

    /**
     * Sets if HttpCliet should use system properties.
     *
     * @param useSystemProperties
     */
    public void setUseSystemProperties(Boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
    }

    /**
     * Returns HttpClient connection timeout.
     *
     * @return
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets HttpClient connection timeout.
     *
     * @param connectTimeout
     */
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Returns HttpClient read timeout.
     *
     * @return
     */
    public Integer getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets HttpClient read timeout.
     *
     * @param readTimeout
     */
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Returns HttpClient connection request timeout.
     *
     * @return
     */
    public Integer getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    /**
     * Sets HttpClient connection request timeout.
     *
     * @param connectionRequestTimeout
     */
    public void setConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * Returns HttpClient max connection.
     *
     * @return
     */
    public Integer getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets HttpClient max connections.
     *
     * @param maxConnections
     */
    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Closes HttpClient.
     */
    public void dispose() {
        if (httpClient instanceof Closeable) {
            try {
                ((Closeable) httpClient).close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to close http client while disposing request");
            }
        } else {
            //Older releases require we do this:
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.connectTimeout);
        hash = 43 * hash + Objects.hashCode(this.readTimeout);
        hash = 43 * hash + Objects.hashCode(this.connectionRequestTimeout);
        hash = 43 * hash + Objects.hashCode(this.maxConnections);
        hash = 43 * hash + Objects.hashCode(this.useSystemProperties);
        hash = 43 * hash + Objects.hashCode(this.handleRedirects);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpClientConfig other = (HttpClientConfig) obj;
        if (!Objects.equals(this.connectTimeout, other.connectTimeout)) {
            return false;
        }
        if (!Objects.equals(this.readTimeout, other.readTimeout)) {
            return false;
        }
        if (!Objects.equals(this.connectionRequestTimeout, other.connectionRequestTimeout)) {
            return false;
        }
        if (!Objects.equals(this.maxConnections, other.maxConnections)) {
            return false;
        }
        if (!Objects.equals(this.useSystemProperties, other.useSystemProperties)) {
            return false;
        }
        if (!Objects.equals(this.handleRedirects, other.handleRedirects)) {
            return false;
        }
        return true;
    }

}
