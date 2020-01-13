package info.stasha.proxywarrior;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author stasha
 */
public class MapperFactory {

    private static ObjectMapper yamlMapper;
    private static ObjectMapper jsonMapper;
    private static ObjectMapper xmlMapper;

    public static ObjectMapper getMapper(String mapper) {
        switch (mapper) {
            case "json":
                if (jsonMapper == null) {
                    jsonMapper = new ObjectMapper();
                }
                return jsonMapper;
            case "xml":
                if (xmlMapper == null) {
                    xmlMapper = new XmlMapper();
                }
                return xmlMapper;
            default:
                if (yamlMapper == null) {
                    yamlMapper = new ObjectMapper(new YAMLFactory());
                }
                return yamlMapper;
        }
    }
}
