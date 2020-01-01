package info.stasha.proxywarrior.config.logging;

import info.stasha.proxywarrior.config.logging.AbstractLog;

/**
 *
 * @author stasha
 */
//@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
public class LogConfig extends AbstractLog {

    private LogRequest request;
    private LogResponse response;

    public LogConfig() {
    }

    public LogRequest getRequest() {
        if (request == null) {
            request = new LogRequest(true, true);
        }

        request.setParent(this);

        return request;
    }

    public void setRequest(LogRequest request) {
        this.request = request;
    }

    public LogResponse getResponse() {
        if (response == null) {
            response = new LogResponse();
        }

        response.setParent(this);

        return response;
    }

    public void setResponse(LogResponse response) {
        this.response = response;
    }

}
