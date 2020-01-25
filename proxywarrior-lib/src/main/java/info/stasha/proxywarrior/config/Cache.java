package info.stasha.proxywarrior.config;

/**
 * Cache configuration. Cache applies only to responses.
 *
 * If caching is disabled and expiration is set to number greater then 0, cached
 * resource will be updated with new content. This allows updating cached
 * content when needed. Setting enabled=true will then start returning cached
 * content again.
 *
 * @author stasha
 */
public class Cache {

    private Boolean enabled;
    private Long expirationTime;

    /**
     * Returns true/false if caching is enabled.
     *
     * @return
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enables/disables caching.
     *
     * @param enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns cache expiration time in seconds.
     *
     * @return
     */
    public Long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets cache expiration time in seconds.<br>
     *
     * @param expiration
     */
    public void setExpirationTime(Long expiration) {
        this.expirationTime = expiration;
    }

}
