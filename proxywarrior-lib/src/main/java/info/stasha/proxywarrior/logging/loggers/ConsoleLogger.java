package info.stasha.proxywarrior.logging.loggers;

import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.RequestConfig;
import info.stasha.proxywarrior.ProxyAction;
import info.stasha.proxywarrior.logging.LoggerWarrior;
import info.stasha.proxywarrior.logging.messages.LogMessage;
import info.stasha.proxywarrior.logging.messages.LogObject;
import info.stasha.proxywarrior.config.ResponseConfig;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 *
 * @author stasha
 */
public class ConsoleLogger implements LoggerWarrior {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(YamlConsoleLogger.class.getName());

    protected LogObject getLogMessage(String method, String url, String host, int port) {

        LogObject lo = new LogObject();
        lo.setTime(new Date());
        lo.setUrl(url);
        lo.setMethod(method);
        lo.setHost(host);
        lo.setPort(port);

        return lo;
    }

    protected String getContent(HttpServletRequest htReq) {
        try {
            return IOUtils.toString(htReq.getInputStream(), "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(ConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected String getContent(HttpResponse proxyResponse) {
        if (proxyResponse.getEntity() != null) {
            try {
                return IOUtils.toString(proxyResponse.getEntity().getContent(), "UTF-8");
            } catch (IOException ex) {
                Logger.getLogger(ConsoleLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public void log(ProxyAction action, Metadata metadata) {
        String id = metadata.getId();

        String requestUrl = metadata.getFullUrl();
        String proxyUrl = metadata.getProxyUrl();

        RequestConfig request = metadata.getRequestConfig();
        ResponseConfig response = metadata.getResponseConfig();

        HttpServletRequest htReq = metadata.getHttpServletRequest();
        HttpServletResponse htResp = metadata.getHttpServletResponse();

        HttpRequest proxyRequest = metadata.getProxyRequest();
        HttpResponse proxyResponse = metadata.getProxyResponse();

        LogMessage lm = new LogMessage();
        LogObject lo;

        switch (action) {
            case REQUEST_BEGIN:

                lo = getLogMessage(htReq.getMethod(), requestUrl, htReq.getServerName(), htReq.getServerPort());
                lo.setId(id);
                Enumeration<String> headerNames = htReq.getHeaderNames();

                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String value = htReq.getHeader(name);
                        lo.getHeaders().put(name, value);
                    }
                }

                if (Boolean.TRUE.equals(request.getLog().getRequest().getContent())) {
                    lo.setContent(getContent(htReq));
                }

                lm.setRequest(lo);

                break;
            case PROXY_REQUEST_BEGIN:
                URI uri = URI.create(proxyUrl);

                lo = getLogMessage(htReq.getMethod(), proxyUrl, uri.getHost(), htReq.getServerPort());
                lo.setId(id);
                lo.setUrl(requestUrl);
                lo.setProxyUrl(proxyUrl);

                for (Header header : proxyRequest.getAllHeaders()) {
                    String name = header.getName();
                    String value = header.getValue();
                    lo.getHeaders().put(name, value);
                }

                lm.setProxyrequest(lo);

                break;
            case PROXY_REQUEST_END:
                lo = new LogObject();
                lo.setId(id);
                lo.setTime(new Date());
                lo.setUrl(requestUrl);
                lo.setProxyUrl(proxyUrl);
                lo.setStatus(proxyResponse.getStatusLine().toString());
                lo.setCode(proxyResponse.getStatusLine().getStatusCode());

                for (Header header : proxyResponse.getAllHeaders()) {
                    String name = header.getName();
                    String value = header.getValue();
                    lo.getHeaders().put(name, value);
                }

                lm.setProxyresponse(lo);
                break;

            case RESPONSE_START:
                lo = new LogObject();
                lo.setId(id);

                if (Boolean.TRUE.equals(response.getLog().getRequest().getContent())) {
                    lo.setContent(getContent(proxyResponse));
                }
                break;
            default:
                throw new IllegalArgumentException("LogAction: " + action + " is not supported");

        }

        logMessage(lm);
    }

    protected void logMessage(LogMessage message) {
        LOGGER.log(Level.INFO, message.toString());
    }

}
