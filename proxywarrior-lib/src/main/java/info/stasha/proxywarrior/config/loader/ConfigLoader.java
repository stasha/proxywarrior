package info.stasha.proxywarrior.config.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.stasha.proxywarrior.config.DefaultConfig;
import info.stasha.proxywarrior.config.RequestConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author stasha
 */
public class ConfigLoader extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private static String compareConfig;
    private static String userConfig;
    private static DefaultConfig effectiveConfig;

    public static String getUserConfig() {
        return userConfig;
    }

    public static String getEffectiveConfig() throws JsonProcessingException {
        return MAPPER.writeValueAsString(effectiveConfig);
    }

    private final String propsLocation;
    private final ConfigChangeListener listener;

    public ConfigLoader(String propsLocation, ConfigChangeListener listener) {
        this.propsLocation = propsLocation;
        this.listener = listener;
    }

    @Override
    public void run() {
        RequestConfig p = new RequestConfig();
        try {
            RequestConfig config = ConfigLoader.load(this.propsLocation);
            if (config != null) {
                listener.notify(config);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load config.props from " + this.propsLocation, ex);
        }
    }

    public static RequestConfig load(String path) throws IOException {
        if (path != null) {
            String configstring = IOUtils.toString(new FileInputStream(new File(path)), "UTF-8");
            String compareconfig = configstring.replaceAll("\\s*", "");

            if (compareConfig == null || !compareconfig.equals(compareConfig)) {
                compareConfig = compareconfig;
                userConfig = configstring;

                LOGGER.log(Level.INFO, "ProxyWarrior configuration changed to:\n{0}", configstring);

                DefaultConfig defaultConfig = new DefaultConfig();
                ObjectReader reader = MAPPER.readerForUpdating(defaultConfig);
                effectiveConfig = reader.readValue(configstring);
                return effectiveConfig;
            }
        } else if(path == null && effectiveConfig == null) {
            effectiveConfig = new DefaultConfig();
            return effectiveConfig;
        }

        return null;

    }

}
