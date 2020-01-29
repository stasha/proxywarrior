package info.stasha.proxywarrior;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class BasicHttpResponseWrapperTest {

    private HttpResponse resp;
    private BasicHttpResponseWrapper wrapper;

    @BeforeEach
    public void setUp() {
        resp = Mockito.mock(HttpResponse.class);
        Mockito.doReturn(new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK")).when(resp).getStatusLine();
        wrapper = Mockito.spy(new BasicHttpResponseWrapper(resp));
    }

    @Test
    public void getProtocolVersion() {
        wrapper.getProtocolVersion();
        Mockito.verify(resp).getProtocolVersion();
    }

    @Test
    public void getStatusLine() {
        Mockito.verify(resp).getStatusLine();
    }

    @Test
    public void getEntity() {
        wrapper.getEntity();
        Mockito.verify(resp, Mockito.times(0)).getEntity();

        wrapper.getOriginalEntity();
        Mockito.verify(resp).getEntity();
    }

    @Test
    public void getLocale() {
        wrapper.getLocale();
        Mockito.verify(resp).getLocale();
    }

    @Test
    public void setStatusLine() {
        wrapper.setStatusLine(null);
        Mockito.verify(resp).setStatusLine(null);
    }

    @Test
    public void setStatusLine2() {
        wrapper.setStatusLine(null, 1);
        Mockito.verify(resp).setStatusLine(null, 1);
    }

    @Test
    public void setStatusLine3() {
        wrapper.setStatusLine(null, 1, "ok");
        Mockito.verify(resp).setStatusLine(null, 1, "ok");
    }

    @Test
    public void setStatusCode() {
        wrapper.setStatusCode(1);
        Mockito.verify(resp).setStatusCode(1);
    }

    @Test
    public void setReasonPhrase() {
        wrapper.setReasonPhrase("ok");
        Mockito.verify(resp).setReasonPhrase("ok");
    }

    @Test
    public void setEntity() {
        wrapper.setEntity(null);
        Mockito.verify(resp).setEntity(null);
    }

    @Test
    public void setLocale() {
        wrapper.setLocale(null);
        Mockito.verify(resp).setLocale(null);
    }

    @Test
    public void toStringTest() {
        Mockito.doReturn("test").when(resp).toString();
        Assertions.assertEquals(wrapper.toString(), "test");
    }

    @Test
    public void containsHeader() {
        wrapper.containsHeader("test");
        Mockito.verify(resp).containsHeader("test");
    }

    @Test
    public void getHeaders() {
        wrapper.getHeaders("test");
        Mockito.verify(resp).getHeaders("test");
    }

    @Test
    public void getFirstHeader() {
        wrapper.getFirstHeader("test");
        Mockito.verify(resp).getFirstHeader("test");
    }

    @Test
    public void getLastHeader() {
        wrapper.getLastHeader("test");
        Mockito.verify(resp).getLastHeader("test");
    }

    @Test
    public void getAllHeaders() {
        wrapper.getAllHeaders();
        Mockito.verify(resp).getAllHeaders();
    }

    @Test
    public void addHeader() {
        wrapper.addHeader(null);
        Mockito.verify(resp).addHeader(null);
    }

    @Test
    public void addHeader2() {
        wrapper.addHeader("test", "testvalue");
        Mockito.verify(resp).addHeader("test", "testvalue");
    }

    @Test
    public void setHeader() {
        wrapper.setHeader(null);
        Mockito.verify(resp).setHeader(null);
    }

    @Test
    public void setHeader2() {
        wrapper.setHeader("test", "testvalue");
        Mockito.verify(resp).setHeader("test", "testvalue");
    }

    @Test
    public void setHeaders() {
        wrapper.setHeaders(null);
        Mockito.verify(resp).setHeaders(null);
    }

    @Test
    public void removeHeader() {
        wrapper.removeHeader(null);
        Mockito.verify(resp).removeHeader(null);
    }

    @Test
    public void removeHeaders() {
        wrapper.removeHeaders("test");
        Mockito.verify(resp).removeHeaders("test");
    }

    @Test
    public void headerIterator() {
        wrapper.headerIterator();
        Mockito.verify(resp).headerIterator();
    }

    @Test
    public void headerIterator2() {
        wrapper.headerIterator("test");
        Mockito.verify(resp).headerIterator("test");
    }

    @Test
    public void getParams() {
        wrapper.getParams();
        Mockito.verify(resp).getParams();
    }

    @Test
    public void setParams() {
        wrapper.setParams(null);
        Mockito.verify(resp).setParams(null);
    }
}
