package info.stasha.proxywarrior.listeners;

import com.fasterxml.jackson.databind.ObjectWriter;
import info.stasha.proxywarrior.BasicHttpResponseWrapper;
import info.stasha.proxywarrior.Executable;
import info.stasha.proxywarrior.MapperFactory;
import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.Utils;
import info.stasha.proxywarrior.config.Logging;
import info.stasha.proxywarrior.config.RequestConfig;
import info.stasha.proxywarrior.config.ResponseConfig;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpRequest;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that logs data into DB.
 *
 * @author stasha
 */
public class LogToDbListener extends LoggingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogToDbListener.class);
    private static final ObjectWriter WRITER = MapperFactory.getMapper("yaml").writerWithDefaultPrettyPrinter();

    private ProxyWarrior proxyWarrior;

    void beginTransaction() {
        Base.openTransaction();
    }

    void commitTransaction() {
        Base.commitTransaction();
    }

    void rollbackTransaction() {
        Base.rollbackTransaction();
    }

    /**
     * Executes code inside begin/commit/rollback transaction.
     *
     * @param metadata
     * @param action
     * @param runnable
     */
    public void runInTrunsaction(Metadata metadata, String action, Executable runnable) {
        try {
            beginTransaction();
            runnable.execute();
            commitTransaction();
        } catch (Exception ex) {
            rollbackTransaction();
            String msg = action + " failed to be saved into DB.";
            LOGGER.error(msg, ex);
            throw new ProxyWarriorException(msg, ex);
        }
    }

    /**
     * Sets blob content.
     *
     * @param ps
     * @param position
     * @param content
     * @throws SQLException
     */
    public void setBlob(PreparedStatement ps, int position, InputStream content) throws SQLException {
        if (content != null) {
            ps.setBlob(position, content);
        } else {
            ps.setNull(position, java.sql.Types.BLOB);
        }
    }

    @Override
    public void init(ProxyWarrior proxyWarrior) {
        this.proxyWarrior = proxyWarrior;
    }

    @Override
    public void destroy(ProxyWarrior proxyWarrior) {
        System.out.println("destroyed");
    }

    private long getId(Metadata metadata) {
        return (long) metadata.getId();
    }

    @Override
    public void afterHttpRequest(Metadata metadata) {
        RequestConfig rc = metadata.getRequestConfig();
        Logging logging = rc.getLogging();
        Long updateRecord = metadata.shouldUpdateDbRecord(rc);

        if (updateRecord == null) {
            return;
        }

        boolean contentLogging = Boolean.TRUE.equals(logging.getHttpRequestContent());
        HttpServletRequest req = metadata.getHttpServletRequest();
        long[] id = new long[]{getId(metadata)};
        InputStream content = contentLogging ? Utils.getContent(req) : null;

        runInTrunsaction(metadata, "After http request", () -> {
            if (updateRecord < 0) {
                PreparedStatement ps = Base.startBatch(
                        "INSERT INTO REQUEST ("
                        + "REQUEST_ID, "
                        + "CONFIG_ID, "
                        + "CACHED, "
                        + "REQUEST_TIME, "
                        + "REQUEST_URL, "
                        + "REQUEST_PATH, "
                        + "PROXY_URL, "
                        + "REQUEST_METHOD, "
                        + "REQUEST_HEADERS, "
                        + "REQUEST_CONTENT"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                ps.setLong(1, id[0]);
                ps.setString(2, metadata.getRequestConfig().getId());
                ps.setBoolean(3, metadata.getRequestConfig().getCache().getExpirationTime() > 0);
                ps.setTimestamp(4, new Timestamp(Calendar.getInstance().getTime().getTime()));
                ps.setString(5, metadata.getFullUrl());
                ps.setString(6, metadata.getPath());
                ps.setString(7, metadata.getProxyUrl());
                ps.setString(8, metadata.getHttpServletRequest().getMethod());
                ps.setString(9, WRITER.writeValueAsString(Utils.getHeaders(req)));
                setBlob(ps, 10, content);
                ps.execute();

            } else {
                id[0] = metadata.getCacheResult().getId();
            }

            if (contentLogging) {
                Blob blob = (Blob) Base.firstCell("SELECT REQUEST_CONTENT FROM REQUEST WHERE REQUEST_ID = ?", id[0]);
                Utils.setEntity(req, content, blob);
            }

            return null;
        });

    }

    @Override
    public void afterNotProxyRequest(Metadata metadata) {
        // do nothing
    }

    @Override
    public void beforeProxyRequest(Metadata metadata) {
        RequestConfig rc = metadata.getRequestConfig();
        Logging logging = rc.getLogging();
        Long updateRecord = metadata.shouldUpdateDbRecord(rc);

        if (updateRecord == null) {
            return;
        }

        boolean contentLogging = Boolean.TRUE.equals(logging.getProxyRequestContent());
        HttpRequest req = metadata.getProxyRequest();
        long[] id = new long[]{getId(metadata)};
        InputStream content = contentLogging ? Utils.getContent(req) : null;

        runInTrunsaction(metadata, "Before proxy request", () -> {
            if (updateRecord < 0) {
                PreparedStatement ps = Base.startBatch(
                        "INSERT INTO PROXY_REQUEST ("
                        + "REQUEST_ID, "
                        + "PROXY_REQUEST_HEADERS, "
                        + "PROXY_REQUEST_CONTENT"
                        + ") VALUES (?, ?, ?)");

                ps.setLong(1, id[0]);
                ps.setString(2, WRITER.writeValueAsString(Utils.getHeaders(metadata.getProxyRequest())));
                setBlob(ps, 3, content);
                ps.execute();

            } else {
                id[0] = metadata.getCacheResult().getId();
            }
            return null;
        });

        if (contentLogging) {
            Blob blob = (Blob) Base.firstCell("SELECT PROXY_REQUEST_CONTENT FROM PROXY_REQUEST WHERE REQUEST_ID = ?", id[0]);
            Utils.setEntity(metadata.getProxyRequest(), content, blob);
        }

    }

    @Override
    public void afterProxyResponse(Metadata metadata) {
        ResponseConfig rc = metadata.getResponseConfig();
        Logging logging = rc.getLogging();
        Long updateRecord = metadata.shouldUpdateDbRecord(rc);

        if (updateRecord == null) {
            return;
        }

        boolean contentLogging = Boolean.TRUE.equals(logging.getProxyResponseContent()) || updateRecord == -1;
        BasicHttpResponseWrapper resp = metadata.getProxyResponse();
        long[] id = new long[]{getId(metadata)};
        InputStream content = contentLogging ? Utils.getContent(resp) : null;

        runInTrunsaction(metadata, "After proxy response", () -> {
            if (updateRecord < 0) {
                PreparedStatement ps = Base.startBatch(
                        "INSERT INTO PROXY_RESPONSE ("
                        + "REQUEST_ID, "
                        + "PROXY_RESPONSE_STATUS_CODE, "
                        + "PROXY_RESPONSE_STATUS_LINE, "
                        + "PROXY_RESPONSE_HEADERS, "
                        + "PROXY_RESPONSE_CONTENT"
                        + ") VALUES (?, ?, ?, ?, ?)");

                ps.setLong(1, id[0]);
                ps.setInt(2, metadata.getProxyResponse().getStatusLine().getStatusCode());
                ps.setString(3, metadata.getProxyResponse().getStatusLine().toString());
                ps.setString(4, WRITER.writeValueAsString(Utils.getHeaders(metadata.getProxyResponse())));
                setBlob(ps, 5, content);
                ps.execute();

            } else {
                id[0] = metadata.getCacheResult().getId();
            }
            return null;
        });

        if (contentLogging) {
            Blob blob = (Blob) Base.firstCell("SELECT PROXY_RESPONSE_CONTENT FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", id[0]);
            Utils.setEntity(resp, content, blob);
        }

    }

    @Override
    public void beforeHttpResponse(Metadata metadata) {
        ResponseConfig rc = metadata.getResponseConfig();
        Logging logging = rc.getLogging();
        Long updateRecord = metadata.shouldUpdateDbRecord(rc);

        if (updateRecord == null) {
            return;
        }

        boolean contentLogging = Boolean.TRUE.equals(logging.getProxyResponseContent());
        BasicHttpResponseWrapper resp = metadata.getProxyResponse();
        long[] id = new long[]{getId(metadata)};
        InputStream content = contentLogging ? Utils.getContent(resp) : null;

        runInTrunsaction(metadata, "Before http response", () -> {
            if (updateRecord < 0) {
                PreparedStatement ps = Base.startBatch(
                        "INSERT INTO RESPONSE ("
                        + "REQUEST_ID, "
                        + "RESPONSE_HEADERS, "
                        + "RESPONSE_CONTENT"
                        + ") VALUES (?, ?, ?)");

                ps.setLong(1, id[0]);
                ps.setString(2, WRITER.writeValueAsString(Utils.getHeaders(metadata.getHttpServletResponse())));
                setBlob(ps, 3, content);
                ps.execute();

            } else {
                id[0] = metadata.getCacheResult().getId();
            }

            if (contentLogging) {
                Blob blob = (Blob) Base.firstCell("SELECT RESPONSE_CONTENT FROM RESPONSE WHERE REQUEST_ID = ?", id[0]);
                Utils.setEntity(resp, content, blob);
            }

            return null;
        });

    }

    @Override
    public void afterHttpResponse(Metadata metadata) {

    }

    @Override
    public boolean equals(Object obj) {
        return LogToDbListener.class
                .getName().equals(obj.getClass().getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

}
