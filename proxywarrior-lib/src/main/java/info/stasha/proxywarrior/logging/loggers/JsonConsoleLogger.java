package info.stasha.proxywarrior.logging.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author stasha
 */
public class JsonConsoleLogger extends YamlConsoleLogger {

    private ObjectMapper mapper;

    @Override
    protected ObjectMapper getObjectMapper() {
        if (this.mapper == null) {
            this.mapper = new ObjectMapper();
        }
        return this.mapper;
    }

}
