package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.CommonConfig;
import info.stasha.proxywarrior.config.Model;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public Utils() {
    }

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
            String msg = "Failed to create new instance from " + cls;
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
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

    private static Map<String, List<String>> fillHeaders(Map<String, List<String>> container, Map<String, List<String>> map) {
        map.forEach((key, value) -> {
            if (value != null) {
                List<String> headers = container.get(key);
                headers = headers == null ? new ArrayList<>() : headers;
                for (String header : value) {
                    headers.add(0, header);
                };

                container.put(key, headers);
            } else {
                container.put(key, new ArrayList<>());
            }
        });

        return container;
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
    public static Map<String, List<String>> getMapList(Map<String, List<String>> childMap, Map<String, List<String>> parentMap) {
        Map<String, List<String>> h = new LinkedHashMap<>();
        if (parentMap != null) {
            h = fillHeaders(h, parentMap);
        }
        if (childMap != null) {
            h = fillHeaders(h, childMap);
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
            return null;
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
            return null;
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
            return null;
        });
    }

    public static Map<String, List<String>> getHeaders(HttpServletRequest req) {
        return Collections.list(req.getHeaderNames()).stream().collect(Collectors.toMap(String::valueOf, (name) -> {
            return Collections.list(req.getHeaders(name));
        }));
    }

    public static Map<String, List<String>> getHeaders(HttpServletResponse resp) {
        return resp.getHeaderNames().stream().collect(Collectors.toMap(String::valueOf, (name) -> {
            return new ArrayList(resp.getHeaders(name));
        }));
    }

    public static Map<String, List<String>> getHeaders(HttpMessage reqresp) {
        return Stream.of(reqresp.getAllHeaders()).map(Header::getName).collect(Collectors.toMap(String::valueOf, (name) -> {
            return Stream.of(reqresp.getHeaders(name)).map(Header::getValue).collect(Collectors.toList());
        }));
    }

    /**
     * This method will remove existing headers and add new headers specified in
     * passed map.
     *
     * @param message
     * @param headers
     */
    public static void setHeaders(HttpMessage message, Map<String, List<String>> headers) {
        headers.forEach((key, value) -> {
            message.removeHeaders(key);
            value.forEach((headerValue) -> {
                message.addHeader(key, headerValue);
            });
        });
    }

    public static void setHeaders(HttpMessage message, CommonConfig<CommonConfig> config) {

        if (config.getHeaders() != null) {
            for (String k : config.getHeaders().keySet()) {
                String key = Utils.getValue(k, config);

                // modifier can be: =, ~, +
                String modifier = key.trim().substring(0, 1);

                List<String> headers = config.getHeaders().get(k);
                key = key.replaceFirst("^(=|~|\\+)", "");
                key = Utils.getValue(key, config);

                // if there are no header values specified, we create one "artifical" value
                // so logic on header names is run
                headers = headers == null ? Arrays.asList(new String[]{"...."}) : headers;
                if (headers.isEmpty()) {
                    headers.add("....");
                }

                for (String v : headers) {
                    String value = Utils.getValue(v, config);

                    EXISTING_HEADERS:
                    for (Header header : message.getAllHeaders()) {
                        switch (modifier) {
                            case "=":
                                // do nothing
                                continue;
                            case "~":
                                // remove header
                                if (header.getName().equals(key)) {
                                    if ((value.equals("....") || header.getValue().equals(value))) {
                                        message.removeHeader(header);
                                    }
                                }
                                continue;
                            case "+":
                                // add header if it does not exist
                                if (!message.containsHeader(key)) {
                                    message.setHeader(key, value);
                                }
                                continue;
                            default:
                                // remove headers based on removeHeadersPattern
                                if (config.getRemoveHeaders() != null && config.getRemoveHeadersPattern().matcher(header.getName()).find()) {
                                    message.removeHeader(header);
                                    continue;
                                }

                                // replace or add header
                                message.setHeader(key, value);
                                continue;
                        }
                    }
                }
            }
        }
    }
}
