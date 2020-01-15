package info.stasha.proxywarrior.listeners;

import com.fasterxml.jackson.databind.ObjectWriter;
import info.stasha.proxywarrior.BasicHttpResponseWrapper;
import info.stasha.proxywarrior.Executable;
import info.stasha.proxywarrior.MapperFactory;
import info.stasha.proxywarrior.ProxyWarrior;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.Utils;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
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

        long id = getId(metadata);
        HttpServletRequest req = metadata.getHttpServletRequest();
        InputStream content = Utils.getContent(req);
        Utils.getHeaders(req);

        runInTrunsaction(metadata, "After http request", () -> {

            PreparedStatement ps = Base.startBatch(
                    "INSERT INTO REQUEST ("
                    + "REQUEST_ID, "
                    + "CONFIG_ID, "
                    + "REQUEST_URL, "
                    + "PROXY_URL, "
                    + "REQUEST_METHOD, "
                    + "REQUEST_HEADERS, "
                    + "REQUEST_CONTENT"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?)");

            ps.setLong(1, id);
            ps.setString(2, metadata.getRequestConfig().getId());
            ps.setString(3, metadata.getFullUrl());
            ps.setString(4, metadata.getProxyUrl());
            ps.setString(5, metadata.getHttpServletRequest().getMethod());
            ps.setString(6, WRITER.writeValueAsString(Utils.getHeaders(metadata.getHttpServletRequest())));
            setBlob(ps, 7, content);
            ps.execute();
            return null;
        });

        Blob blob = (Blob) Base.firstCell("SELECT REQUEST_CONTENT FROM REQUEST WHERE REQUEST_ID = ?", id);
        Utils.setEntity(req, content, blob);

    }

    @Override
    public void afterNotProxyRequest(Metadata metadata) {
        // do nothing
    }

    @Override
    public void beforeProxyRequest(Metadata metadata) {
        long id = getId(metadata);
        InputStream content = Utils.getContent(metadata.getProxyRequest());

        Utils.getHeaders(metadata.getProxyRequest());

        runInTrunsaction(metadata, "Before proxy request", () -> {
            PreparedStatement ps = Base.startBatch(
                    "INSERT INTO PROXY_REQUEST ("
                    + "REQUEST_ID, "
                    + "PROXY_REQUEST_HEADERS, "
                    + "PROXY_REQUEST_CONTENT"
                    + ") VALUES (?, ?, ?)");

            ps.setLong(1, id);
            ps.setString(2, WRITER.writeValueAsString(Utils.getHeaders(metadata.getProxyRequest())));
            setBlob(ps, 3, content);
            ps.execute();
            return null;
        });

        Blob blob = (Blob) Base.firstCell("SELECT PROXY_REQUEST_CONTENT FROM PROXY_REQUEST WHERE REQUEST_ID = ?", id);
        Utils.setEntity(metadata.getProxyRequest(), content, blob);
    }

    @Override
    public void afterProxyResponse(Metadata metadata) {
        long id = getId(metadata);
        BasicHttpResponseWrapper resp = metadata.getProxyResponse();
        InputStream content = Utils.getContent(resp);

        runInTrunsaction(metadata, "After proxy response", () -> {

            PreparedStatement ps = Base.startBatch(
                    "INSERT INTO PROXY_RESPONSE ("
                    + "REQUEST_ID, "
                    + "PROXY_RESPONSE_STATUS_CODE, "
                    + "PROXY_RESPONSE_STATUS_LINE, "
                    + "PROXY_RESPONSE_HEADERS, "
                    + "PROXY_RESPONSE_CONTENT"
                    + ") VALUES (?, ?, ?, ?, ?)");

            ps.setLong(1, id);
            ps.setInt(2, metadata.getProxyResponse().getStatusLine().getStatusCode());
            ps.setString(3, metadata.getProxyResponse().getStatusLine().toString());
            ps.setString(4, WRITER.writeValueAsString(Utils.getHeaders(metadata.getProxyResponse())));
            setBlob(ps, 5, content);
            ps.execute();
            return null;
        });

        Blob blob = (Blob) Base.firstCell("SELECT PROXY_RESPONSE_CONTENT FROM PROXY_RESPONSE WHERE REQUEST_ID = ?", id);
        Utils.setEntity(resp, content, blob);
    }

    @Override
    public void beforeHttpResponse(Metadata metadata) {
        long id = getId(metadata);
        BasicHttpResponseWrapper resp = metadata.getProxyResponse();
        InputStream content = Utils.getContent(resp);

        runInTrunsaction(metadata, "Before http response", () -> {

            PreparedStatement ps = Base.startBatch(
                    "INSERT INTO RESPONSE ("
                    + "REQUEST_ID, "
                    + "RESPONSE_HEADERS, "
                    + "RESPONSE_CONTENT"
                    + ") VALUES (?, ?, ?)");

            ps.setLong(1, id);
            ps.setString(2, WRITER.writeValueAsString(Utils.getHeaders(metadata.getHttpServletResponse())));
            setBlob(ps, 3, content);
            ps.execute();
            return null;
        });

        Blob blob = (Blob) Base.firstCell("SELECT RESPONSE_CONTENT FROM RESPONSE WHERE REQUEST_ID = ?", id);
        Utils.setEntity(resp, content, blob);
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
