package info.stasha.proxywarrior.config;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class.
 *
 * @author stasha
 */
public class Utils {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("_\\[(.*?)\\]_");

    /**
     * Returns value if it is not null.<br>
     * If value is null, searches value on parent.<br>
     * If parent is null, returns default value.<br>
     *
     * @param <T>
     * @param value
     * @param config
     * @param parent
     * @param parentValue
     * @param defaultValue
     * @return
     */
    public static <T> T getValue(T value, CommonConfig<? extends CommonConfig> config, Object parent, Supplier<T> parentValue, T defaultValue) {
        T val = value != null ? value : parent != null ? parentValue.get() : defaultValue;
        return config != null ? getValue(val, config) : val;
    }

    /**
     * Returns default regex pattern that matches everything.
     *
     * @param value
     * @return
     */
    public static Pattern getDefaultPattern(String value) {
        return getPattern(value, ".*");
    }

    /**
     * Creates new object based on fully qualified class name.
     *
     * @param <T>
     * @param cls
     * @return
     */
    public static <T> T newObject(String cls) {
        if (cls == null) {
            return null;
        }
        try {
            return (T) Class.forName(cls).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns pattern created based on passed arguments.
     *
     * @param value
     * @param pattern
     * @return
     */
    public static Pattern getPattern(String value, String pattern) {
        if (value == null) {
            return null;
        }
        return Pattern.compile("*".equals(value.trim()) ? pattern : value);
    }

    /**
     * Returns joined two maps where child map overwrites parent map entries if
     * they are equal.
     *
     * @param <T>
     * @param childMap
     * @param parentMap
     * @return
     */
    public static <T> Map<T, T> getMap(Map<T, T> childMap, Map<T, T> parentMap) {
        Map<T, T> h = new TreeMap<>();
        if (parentMap != null) {
            h.putAll(parentMap);
        }
        if (childMap != null) {
            h.putAll(childMap);
        }
        return h;
    }

    /**
     * Returns value from properties or model. If value is not found in
     * properties or model, then passed value is returned.
     *
     * @param <T>
     * @param value
     * @param config
     * @return
     */
    public static <T> T getValue(T value, CommonConfig config) {
        if (value == null) {
            return value;
        }
        if (value instanceof String) {
            String val = (String) value;
            if (val.contains("_[")) {
                Matcher m = TEMPLATE_PATTERN.matcher(val);
                if (m.find()) {
                    Map<String, String> properties = config.getProperties();
                    String key = m.group(1);
                    String replacement = null;
                    if (properties != null) {
                        replacement = properties.get(key);
                    }
                    if (replacement == null) {
                        Model model = config.getModelObject();
                        if (model != null) {
                            replacement = model.getValue(key);
                        }
                    }

                    if (replacement != null) {
                        val = val.replace(m.group(), replacement);
                    }

                    return (T) val;
                }
            }
        }
        return value;
    }
}
