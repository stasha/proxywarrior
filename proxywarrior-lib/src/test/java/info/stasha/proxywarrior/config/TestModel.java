package info.stasha.proxywarrior.config;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author stasha
 */
public class TestModel implements Model {

    private final Map<String, String> props = new HashMap<>();
    public static final String MODEL_HEADER = "X-Model-Header";
    public static final String MODEL_2_HEADER = "X-Attribute-Model-Header";

    {
        props.put("xtra", MODEL_HEADER);
        props.put("attrval", MODEL_2_HEADER);
    }

    @Override
    public String getValue(String key) {
        return props.get(key);
    }

}
