package info.stasha.proxywarrior;

import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;

/**
 *
 * @author stasha
 */
public class BasicHttpResponseWrapper extends BasicHttpResponse {

    private final HttpResponse response;

    public BasicHttpResponseWrapper(HttpResponse response) {
        super(response.getStatusLine());
        this.response = response;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return response.getProtocolVersion();
    }

    @Override
    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }

    @Override
    public HttpEntity getEntity() {
        return null;
    }

    public HttpEntity getOriginalEntity() {
        return response.getEntity();
    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        response.setStatusLine(statusline);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        response.setStatusLine(ver, code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        response.setStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(int code) {
        response.setStatusCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) {
        response.setReasonPhrase(reason);
    }

    @Override
    public void setEntity(HttpEntity entity) {
        response.setEntity(entity);
    }

    @Override
    public void setLocale(Locale locale) {
        response.setLocale(locale);
    }

    @Override
    public String toString() {
        return response.toString();
    }

    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return response.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return response.getLastHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
        return response.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
        response.addHeader(header);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
        response.setHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        response.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
        response.removeHeader(header);
    }

    @Override
    public void removeHeaders(String name) {
        response.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
        return response.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        return response.headerIterator(name);
    }

    @Override
    public HttpParams getParams() {
        return response.getParams();
    }

    @Override
    public void setParams(HttpParams params) {
        response.setParams(params);
    }

}
