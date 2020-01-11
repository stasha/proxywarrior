package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.BasicHttpResponseWrapper;
import info.stasha.proxywarrior.Executable;
import info.stasha.proxywarrior.HttpServletRequestWrapperImpl;
import info.stasha.proxywarrior.ProxyWarriorException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class.
 *
 * @author stasha
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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
            LOGGER.error("Failed to create new instance from " + cls, ex);
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

    public static InputStream getContent(HttpRequest req) {

        if (req instanceof BasicHttpEntityEnclosingRequest) {
            BasicHttpEntityEnclosingRequest r = (BasicHttpEntityEnclosingRequest) req;
            try {
                long length = r.getEntity().getContentLength();
                if (length > 0) {
                    return ((BasicHttpEntityEnclosingRequest) req).getEntity().getContent();
                }
            } catch (Exception ex) {
                String msg = "Failed to get content from proxy request entity";
                LOGGER.error(msg, ex);
                throw new ProxyWarriorException(msg, ex);
            }
        }
        return null;
    }

    public static InputStream getContent(HttpResponse resp) {
        HttpEntity entity = resp.getEntity();
        if (resp instanceof BasicHttpResponseWrapper) {
            entity = ((BasicHttpResponseWrapper) resp).getOriginalEntity();
        }

        try {
            return entity != null ? entity.getContent() : null;
        } catch (Exception ex) {
            String msg = "Failed to get content from proxy response entity";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    public static InputStream getContent(HttpServletRequest req) {
        try {
            return req.getInputStream();
        } catch (IOException ex) {
            String msg = "Failed to get content from http servlet request";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    public static long getContentLength(HttpMessage reqresp) {
        Header contentLength = reqresp.getFirstHeader("Content-Length");

        if (contentLength != null) {
            return Long.parseLong(contentLength.getValue());
        }
        return -1L;
    }

    public static void setEntity(Executable execute) {
        try {
            execute.execute();
        } catch (Exception ex) {
            String msg = "Failed to set InputStream entity";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    public static void setEntity(HttpServletRequest req, InputStream content, Blob blob) {
        setEntity(() -> {
            IOUtils.closeQuietly(content);
            if (req instanceof HttpServletRequestWrapperImpl) {
                ((HttpServletRequestWrapperImpl) req).setInputStream(new BufferedInputStream(blob.getBinaryStream()));
            }
        });
    }

    public static void setEntity(HttpResponse resp, InputStream content) {
        setEntity(resp, content, null);
    }

    public static void setEntity(HttpResponse resp, InputStream content, Blob blob) {
        setEntity(() -> {
            if (content != null) {
                if (blob != null) {
                    IOUtils.closeQuietly(content);
                    if (resp.getEntity() != null) {
                        EntityUtils.consumeQuietly(resp.getEntity());
                    }
                }
                resp.setEntity(new InputStreamEntity(blob != null ? new BufferedInputStream(blob.getBinaryStream()) : content, getContentLength(resp)));
            }
        });
    }

    public static void setEntity(HttpRequest req, InputStream content, Blob blob) {
        setEntity(() -> {
            if (content != null && req instanceof BasicHttpEntityEnclosingRequest) {
                BasicHttpEntityEnclosingRequest r = ((BasicHttpEntityEnclosingRequest) req);
                if (blob != null) {
                    IOUtils.closeQuietly(content);
                    if (r.getEntity() != null) {
                        EntityUtils.consumeQuietly(r.getEntity());
                    }
                    r.setEntity(new InputStreamEntity(new BufferedInputStream(blob.getBinaryStream()), getContentLength(req)));
                }
            }
        });
    }

}
