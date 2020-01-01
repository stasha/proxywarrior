package info.stasha.proxywarrior.logging.loggers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Logs proxywarrior messages in xml format.
 *
 * @author stasha
 */
public class XmlConsoleLogger extends YamlConsoleLogger {

    private ObjectMapper mapper;

    @Override
    protected ObjectMapper getObjectMapper() {
        if (mapper == null) {
            this.mapper = new XmlMapper();
        }
        return this.mapper;
    }

}
