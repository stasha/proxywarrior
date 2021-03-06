package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.config.RequestConfig;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class RequestConfigTest {

    private static final String URI = "https://www.test.com:8910";
    private static final String URL = URI + "/test?name=1&id=2";

    private RequestConfig requestConfig;
    private HttpServletRequest req;

    @BeforeEach
    public void setUp() {
        requestConfig = new RequestConfig();
        req = Mockito.mock(HttpServletRequest.class);

        Mockito.doReturn("https").when(req).getScheme();
        Mockito.doReturn("test.com").when(req).getRequestURI();
        Mockito.doReturn(8080).when(req).getServerPort();
        Mockito.doReturn(new StringBuffer(URL)).when(req).getRequestURL();
    }

    @Test
    public void wildcardUrlWildcartTargetUrlTest() {
        requestConfig.setUrl("*");
        requestConfig.setTargetUrl("*");

        String url = requestConfig.getProxyUrl(req);

        Assertions.assertEquals(URL, url, "Url's should equal");
    }

    @Test
    public void regexUrlWildcartTargetUrlPattern() {
        requestConfig.setUrl("(http)(://.*$)");
        requestConfig.setTargetUrl("*");

        String url = requestConfig.getProxyUrl(req);

        Assertions.assertEquals(URL, url, "Url's should equal");
    }

    @Test
    public void regexUrlRegexTargetUrlTest() {
        Mockito.doReturn(new StringBuffer("http://www.test.com:8080/mydomain?name=1&id=2")).when(req).getRequestURL();

        requestConfig.setUrl("^(http[s]*)(:\\/\\/.*?:)([0-9]*)\\/(.*?)(\\?.*?)$");
        requestConfig.setTargetUrl("https$28910/test$5");

        String url = requestConfig.getProxyUrl(req);

        Assertions.assertEquals(URL, url, "Url's should equal");
    }

    @Test
    public void wildcardUrlWildcardTargetUriTest() {
        requestConfig.setUrl("*");
        requestConfig.setTargetUri("*");

        String url = requestConfig.getProxyUrl(req);
        Assertions.assertEquals(URL, url, "Url's should equal");
    }

    @Test
    public void regexUrlWildcardTargetUriTest() {
        requestConfig.setUrl("(http)(://.*$)");
        requestConfig.setTargetUri("*");

        String url = requestConfig.getProxyUrl(req);
        Assertions.assertEquals(URL, url, "Url's should equal");
    }

    @Test
    public void regexUrlRegexTargetUriTest() {
        Mockito.doReturn(new StringBuffer("http://www.mydomain.com:8080/test?name=1&id=2")).when(req).getRequestURL();

        requestConfig.setUrl("^(http[s]*)(:\\/\\/.*?:)([0-9]*)\\/(.*?)(\\?.*?)$");
        requestConfig.setTargetUri("http://www.test.com:8910/context");

        String url = requestConfig.getProxyUrl(req);

        Assertions.assertEquals("http://www.test.com:8910/context/test?name=1&id=2", url, "Url's should equal");
    }

    @Test
    public void getProxyUriTest() {
        requestConfig.setUrl("*");
        requestConfig.setTargetUri("*");

        String uri = requestConfig.getProxyUri(req);
        Assertions.assertEquals(URI, uri, "Url's should equal");
    }

}
