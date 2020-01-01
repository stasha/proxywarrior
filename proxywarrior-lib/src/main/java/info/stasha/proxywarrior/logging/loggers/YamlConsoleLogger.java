package info.stasha.proxywarrior.logging.loggers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.stasha.proxywarrior.logging.messages.LogMessage;
import java.util.logging.Level;

/**
 *
 * @author stasha
 */
public class YamlConsoleLogger extends ConsoleLogger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(YamlConsoleLogger.class.getName());
    private ObjectMapper mapper;

    protected ObjectMapper getObjectMapper() {
        if (this.mapper == null) {
            this.mapper = new ObjectMapper(new YAMLFactory());
        }
        return this.mapper;
    }

    @Override
    protected void logMessage(LogMessage message) {
        try {
            String msg = getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(message);
            LOGGER.log(Level.INFO, "\n{0}", msg);
        } catch (JsonProcessingException ex) {
            LOGGER.log(Level.SEVERE, "Error serializing log message", ex);
        }
    }

}
