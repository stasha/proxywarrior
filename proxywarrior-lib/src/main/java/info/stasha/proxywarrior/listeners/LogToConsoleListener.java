package info.stasha.proxywarrior.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.stasha.proxywarrior.Executable;
import info.stasha.proxywarrior.MapperFactory;
import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.proxywarrior.Utils;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.Logging;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Date;
import java.util.regex.Pattern;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs request/proxy request/proxy response/response into console
 *
 * @author stasha
 */
public class LogToConsoleListener extends LoggingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogToConsoleListener.class);
    private static final ObjectMapper MAPPER = MapperFactory.getMapper("yaml");
    private static final Pattern HEADER_INDENT = Pattern.compile("(^.)", Pattern.MULTILINE);
    private static final Pattern HEADER_VALUE_INDENT = Pattern.compile("(^-)", Pattern.MULTILINE);

    @Override
    public void init(ProxyWarrior proxyWarrior) {
        // do nothing
    }

    @Override
    public void destroy(ProxyWarrior proxyWarrior) {
        // do nothing
    }

    String getHeaders(Executable exec) {
        try {
            String headers = (String) exec.execute();
            headers = headers.replaceAll("\\s*---\\s*\n", "");
            headers = HEADER_VALUE_INDENT.matcher(headers).replaceAll("   $1");
            return HEADER_INDENT.matcher(headers).replaceAll("  $1");
        } catch (Exception ex) {
            String msg = "Failed to process headers";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    InputStream getInputStream(Executable exec) {
        try {
            Blob blob = (Blob) exec.execute();
            if (blob != null) {
                return blob.getBinaryStream();
            }
            return null;
        } catch (Exception ex) {
            String msg = "Failed to read content from DB";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    private LogObject getLogObject(String type, Long id) {
        LogObject lo = new LogObject();
        lo.setType(type);
        lo.setId(id);
        lo.setTime(new Date());
        return lo;
    }

    @Override
    public void afterHttpRequest(Metadata metadata) {
        Logging logging = metadata.getRequestConfig().getLogging();
        if (!Boolean.TRUE.equals(logging.getEnabled())) {
            return;
        }

        long id = metadata.getId();
        LogObject lo = getLogObject("REQUEST", id);
        lo.setUrl(metadata.getFullUrl());
        lo.setProxyUrl(metadata.getProxyUrl());
        lo.setProxyUri(metadata.getProxyUri());
        lo.setMethod(metadata.getHttpServletRequest().getMethod());
        lo.setHeaders(getHeaders(() -> MAPPER.writeValueAsString(Utils.getHeaders(metadata.getHttpServletRequest()))));

        if (Boolean.TRUE.equals(logging.getHttpRequestContent())) {
            lo.setContentStream(getInputStream(() -> ((Blob) Base.firstCell("SELECT REQUEST_CONTENT FROM REQUEST WHERE REQUEST_ID = ?", id))));
        }

        LOGGER.info(lo.toString());
    }

    @Override
    public void afterNotProxyRequest(Metadata metadata) {
        // do nothing
    }

    @Override
    public void beforeProxyRequest(Metadata metadata) {
        Logging logging = metadata.getRequestConfig().getLogging();
        if (!Boolean.TRUE.equals(logging.getEnabled())) {
            return;
        }

        long id = metadata.getId();
        LogObject lo = getLogObject("PROXY REQUEST", id);
        lo.setHeaders(getHeaders(() -> MAPPER.writeValueAsString(Utils.getHeaders(metadata.getProxyRequest()))));

        if (Boolean.TRUE.equals(logging.getProxyRequestContent())) {
            lo.setContentStream(getInputStream(() -> ((Blob) Base.firstCell("SELECT PROXY_REQUEST_CONTENT FROM PROXY_REQUEST WHERE REQUEST_ID = ?", id))));
        }

        LOGGER.info(lo.toString());
    }

    @Override
    public void afterProxyResponse(Metadata metadata) {
        Logging logging = metadata.getResponseConfig().getLogging();
        if (!Boolean.TRUE.equals(logging.getEnabled())) {
            return;
        }

        long id = metadata.getId();
        LogObject lo = getLogObject("PROXY RESPONSE", id);
        lo.setStatus(metadata.getProxyResponse().getStatusLine().toString());
        lo.setHeaders(getHeaders(() -> MAPPER.writeValueAsString(Utils.getHeaders(metadata.getProxyResponse()))));

        if (Boolean.TRUE.equals(logging.getProxyResponseContent())) {
            lo.setContentStream(getInputStream(() -> ((Blob) Base.firstCell("SELECT PROXY_RESPONSE_CONTENT FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", id))));
        }

        LOGGER.info(lo.toString());
    }

    @Override
    public void beforeHttpResponse(Metadata metadata) {
        Logging logging = metadata.getResponseConfig().getLogging();
        if (!Boolean.TRUE.equals(logging.getEnabled())) {
            return;
        }

        long id = metadata.getId();
        LogObject lo = getLogObject("RESPONSE", id);
        lo.setHeaders(getHeaders(() -> MAPPER.writeValueAsString(Utils.getHeaders(metadata.getProxyResponse()))));

        if (Boolean.TRUE.equals(logging.getHttpResponseContent())) {
            lo.setContentStream(getInputStream(() -> ((Blob) Base.firstCell("SELECT RESPONSE_CONTENT FROM RESPONSE WHERE REQUEST_ID = ?", id))));
        }

        LOGGER.info(lo.toString());
    }

    @Override
    public void afterHttpResponse(Metadata metadata) {
        // do nothing
    }

}
