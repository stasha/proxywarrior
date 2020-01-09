package info.stasha.proxywarrior.config.loader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.proxywarrior.config.RequestConfig;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Configuration loader.
 *
 * @author stasha
 */
public class ConfigLoader extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    public static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private static String defaultConfigString;
    private static String compareConfig;
    private static String userConfig;
    private static RequestConfig effectiveConfig;

    private final String propsLocation;
    private final ConfigChangeListener listener;

    /**
     * Creates new ConfigLoader instance.
     *
     * @param propsLocation
     * @param listener
     */
    public ConfigLoader(String propsLocation, ConfigChangeListener listener) {
        this.propsLocation = propsLocation;
        this.listener = listener;
    }

    /**
     * Timer task that periodically checks if configuration file has changed.
     */
    @Override
    public void run() {
        RequestConfig p = new RequestConfig();
        try {
            RequestConfig config = ConfigLoader.load(this.propsLocation);
            if (config != null) {
                listener.notify(config);
            }
        } catch (Exception ex) {
            String msg = "Failed to load config.props from " + this.propsLocation;
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    /**
     * Loads configuration from specified path.
     *
     * @param path
     * @return
     */
    public static RequestConfig load(String path) {
        if (path != null) {
            return setConfiguration(loadConfiguration(path));
        }

        return null;
    }

    /**
     * Loads configuration from file.
     *
     * @param path
     * @return
     */
    private static String loadConfiguration(String path) {
        try {
            return FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException ex) {
            String msg = "Failed to load configuration from: " + path;
            LOGGER.log(Level.SEVERE, msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    /**
     * Returns user specific configuration.
     *
     * @return
     */
    public static String getUserConfig() {
        return userConfig;
    }

    /**
     * Returns effective configuration. Effective configuration is calculated
     * based on user specific configuration.
     *
     * @return
     * @throws JsonProcessingException
     */
    public static String getEffectiveConfig() throws JsonProcessingException {
        return MAPPER.writeValueAsString(effectiveConfig);
    }

    /**
     * Returns default-config.yaml string.
     *
     * @return
     */
    public static String getDefaultConfigString() {
        if (defaultConfigString == null) {
            try {
                defaultConfigString = FileUtils.readFileToString(new File(ConfigLoader.class.getResource("/default-config.yaml").getPath()), "UTF-8");
            } catch (IOException ex) {
                String msg = "Failed to load default-config.yaml";
                LOGGER.severe(msg);
                throw new ProxyWarriorException(msg, ex);
            }
        }

        return defaultConfigString;
    }

    /**
     * Returns parsed default-config.yaml configuration.
     *
     * @return
     */
    public static RequestConfig getDefaultConfiguration() {
        try {
            return MAPPER.readValue(getDefaultConfigString(), RequestConfig.class);
        } catch (Exception ex) {
            String msg = "Failed to parse default config";
            LOGGER.severe(msg);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    /**
     * Sets configuration that should be used by proxywarrior.
     *
     * @param userConfigString
     * @return
     */
    public static RequestConfig setConfiguration(String userConfigString) {

        userConfigString = userConfigString != null ? userConfigString : getDefaultConfigString();

        String compareconfig = null;

        if (userConfigString != null) {
            compareconfig = userConfigString.replaceAll("\\s*", "");
        }

        if (compareConfig == null || !compareconfig.equals(compareConfig)) {
            compareConfig = compareconfig;
            userConfig = userConfigString;

            LOGGER.log(Level.INFO, "ProxyWarrior configuration changed to:\n{0}", userConfigString);

            ObjectReader reader = MAPPER.readerForUpdating(getDefaultConfiguration());

            try {
                effectiveConfig = reader.readValue(userConfigString);
                return effectiveConfig;
            } catch (JsonParseException ex) {
                String msg = "Failed to parse configuration\n---\n" + userConfigString;
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProxyWarriorException(msg, ex);
            } catch (JsonProcessingException ex) {
                String msg = "Failed to process configuration\n---\n" + userConfigString;
                LOGGER.log(Level.SEVERE, msg, ex);
                throw new ProxyWarriorException(msg, ex);
            }
        }
        return null;
    }

}
