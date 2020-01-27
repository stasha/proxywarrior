package info.stasha.proxywarrior.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.stasha.proxywarrior.Utils;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpMessage;

/**
 * Common and specific request/response headers
 *
 * @author stasha
 */
public class Headers {

    private Map<String, List<String>> common = new LinkedHashMap<>();
    private Map<String, List<String>> request = new LinkedHashMap<>();
    private Map<String, List<String>> response = new LinkedHashMap<>();

    /**
     * Gets common headers for request and response.
     *
     * @return
     */
    public Map<String, List<String>> getCommon() {
        return common;
    }

    /**
     * Sets common headers for request and response.
     *
     * @param common
     */
    public void setCommon(Map<String, List<String>> common) {
        this.common = common;
    }

    /**
     * Returns headers that will be used in request. This are combined common
     * headers with request specific headers. Request specific headers override
     * common headers if same headers are specified in both places.
     *
     * @return
     */
    public Map<String, List<String>> getRequest() {
        return getCombinedHeaders(request, common);
    }

    /**
     * Sets request specific headers.
     *
     * @param request
     */
    public void setRequest(Map<String, List<String>> request) {
        this.request = request;
    }

    /**
     * Returns headers that will be used in response. This are combined common
     * headers with response specific headers. Response specific headers
     * override common headers if same headers are specified in both places.
     *
     * @return
     */
    public Map<String, List<String>> getResponse() {
        return getCombinedHeaders(response, common);
    }

    /**
     * Sets response specific headers.
     *
     * @param response
     */
    public void setResponse(Map<String, List<String>> response) {
        this.response = response;
    }

    /**
     * Normalizes value by removing modificators.
     *
     * @param value
     * @return
     */
    private static String normalizeValue(String value) {
        return value.replaceAll("^(~|=|\\+)", "");
    }

    /**
     * Returns combined headers like common + request or common + response
     *
     * @param specific
     * @param common
     * @return
     */
    private static Map<String, List<String>> getCombinedHeaders(Map<String, List<String>> specific, Map<String, List<String>> common) {
        final Map<String, List<String>> combined = new LinkedHashMap<>();
        if (specific != null) {
            specific.forEach((key, list) -> {
                combined.put(key, list);
            });
        }

        if (common != null) {
            COMMON_LOOP:
            for (String commonKey : common.keySet()) {

                List<String> commonList = common.get(commonKey);
                String ncommonKey = normalizeValue(commonKey);

                COMBINED_LOOP:
                for (String combinedKey : combined.keySet()) {

                    List<String> combinedList = combined.get(combinedKey);
                    String ncombinedKey = normalizeValue(combinedKey);
                    if (ncommonKey.equals(ncombinedKey)) {
                        if (!combinedKey.startsWith("=") && !combinedKey.startsWith("+")) {
                            continue COMMON_LOOP;
                        }
                    }

                }
                combined.put(commonKey, commonList);
            }
        }

        return combined.isEmpty() ? null : combined;
    }

    /**
     * Combines child headers with parent headers.
     *
     * @param childHeaders
     * @param parentHeaders
     * @return
     */
    public static Headers getHeaders(Headers childHeaders, Headers parentHeaders) {
        Headers headers = new Headers();
        headers.setCommon(getCombinedHeaders(childHeaders != null ? childHeaders.getCommon() : null, parentHeaders != null ? parentHeaders.getCommon() : null));
        headers.setRequest(getCombinedHeaders(childHeaders != null ? childHeaders.getRequest() : null, parentHeaders != null ? parentHeaders.getRequest() : null));
        headers.setResponse(getCombinedHeaders(childHeaders != null ? childHeaders.getResponse() : null, parentHeaders != null ? parentHeaders.getResponse() : null));
        return headers;
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

    public static void setHeaders(HttpMessage message, Map<String, List<String>> headers, CommonConfig config) {

        if (headers != null) {
            for (String k : headers.keySet()) {
                String key = Utils.getValue(k, config);

                // modifier can be: =, ~, +
                String modifier = key.trim().substring(0, 1);

                List<String> values = headers.get(k);
                key = key.replaceFirst("^(=|~|\\+)", "");
                key = Utils.getValue(key, config);

                // if there are no header values specified, we create one "artifical" value
                // so logic on header names is run
                values = values == null ? Arrays.asList(new String[]{"...."}) : values;
                if (values.isEmpty()) {
                    values.add("....");
                }

                for (String v : values) {
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
