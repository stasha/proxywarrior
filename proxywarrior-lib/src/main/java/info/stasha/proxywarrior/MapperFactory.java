package info.stasha.proxywarrior;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 *
 * @author stasha
 */
public class MapperFactory {

    private static ObjectMapper yamlMapper;

    public static ObjectMapper getMapper(String mapper) {
        switch (mapper) {
            default:
                if (yamlMapper == null) {
                    yamlMapper = new ObjectMapper(new YAMLFactory());
                }
                return yamlMapper;
        }
    }
}
