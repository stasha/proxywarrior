package info.stasha.proxywarrior;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author stasha
 */
public class MapperFactory {

    private static ObjectMapper yamlMapper;

    private MapperFactory() {
    }

    public static ObjectMapper getMapper(String mapper) {
        if (yamlMapper == null) {
            yamlMapper = new ObjectMapper(new YAMLFactory());
        }
        return yamlMapper;
    }
}
