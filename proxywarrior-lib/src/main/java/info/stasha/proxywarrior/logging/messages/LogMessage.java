package info.stasha.proxywarrior.logging.messages;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author stasha
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogMessage {

    private LogObject request;
    private LogObject proxyrequest;
    private LogObject response;
    private LogObject proxyresponse;

    public LogObject getRequest() {
        return request;
    }

    public void setRequest(LogObject request) {
        this.request = request;
    }

    public LogObject getProxyrequest() {
        return proxyrequest;
    }

    public void setProxyrequest(LogObject proxyrequest) {
        this.proxyrequest = proxyrequest;
    }

    public LogObject getResponse() {
        return response;
    }

    public void setResponse(LogObject response) {
        this.response = response;
    }

    public LogObject getProxyresponse() {
        return proxyresponse;
    }

    public void setProxyresponse(LogObject proxyresponse) {
        this.proxyresponse = proxyresponse;
    }

    @Override
    public String toString() {
        if (this.getRequest() != null) {
            return "\n-----\nrequest: \n" + this.getRequest().toString();
        } else if (this.getProxyrequest() != null) {
            return "\nproxyrequest: \n" + this.getProxyrequest().toString();
        } else if (this.getResponse() != null) {
            return "\nresponse: \n" + this.getResponse().toString();
        } else if (this.getProxyresponse() != null) {
            return "\nproxyresponse: \n" + this.getProxyresponse().toString() + "\n-----";
        }

        throw new IllegalStateException("You cant call toString on empty logMessage");
    }

}
