package info.stasha.proxywarrior;

import info.stasha.testosterone.TestResponseBuilder.TestResponse;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.annotation.Requests;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import java.io.File;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class ResponseMatchingTest extends AbstractTest {

    private File file;

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/response-matching-config.yaml";

        super.configure(config);
    }

    @GET
    @Path("/proxy/{.*:path}")
    public String globalgetmatchingurl(@Context HttpServletResponse resp) {
        resp.setHeader("requested", "true");
        return "globalgetmatchingurl";
    }

    @OPTIONS
    @Path("/proxy/responseheadermatching")
    public String globaloptionsmatchingurl(@Context HttpServletResponse resp) {
        resp.setHeader("requested", "true");
        resp.setHeader("response-header-matching", "true");
        return "globalgetmatchingurl";
    }

    @Test
    @Requests(requests = {
        @Request(url = "urlmatching", method = "GET"),
        @Request(url = "methodmatching", method = "GET"),
        @Request(url = "requestheadermatching", method = "GET", headerParams = "request-header-matching, true"),
        @Request(url = "responseheadermatching", method = "OPTIONS")
    })
    public void globalMatching(TestResponse resp) {
        switch (resp.getRepeatIndex()) {
            case 1:
                assertResponse(resp, "global url matching");
                break;
            case 2:
                assertResponse(resp, "global get method matching");
                break;
            case 3:
                assertResponse(resp, "global request header matching");
                break;
            case 4:
                assertResponse(resp, "global response header matching");
                break;

        }
    }

    @Test
    @Requests(requests = {
        @Request(url = "matching/url", method = "GET"),
        @Request(url = "matching/method", method = "GET"),
        @Request(url = "matching/requestheader", method = "GET", headerParams = "request-header-matching, true"),
        @Request(url = "matching/responseheader", method = "OPTIONS")
    })
    public void localMatching(TestResponse resp) {
        switch (resp.getRepeatIndex()) {
            case 1:
                assertResponse(resp, "local url matching");
                break;
            case 2:
                assertResponse(resp, "local get method matching");
                break;
            case 3:
                assertResponse(resp, "local request header matching");
                break;
            case 4:
                assertResponse(resp, "local response header matching");
                break;

        }
    }

    public void assertResponse(TestResponse resp, String expectedContent) {
        String responseText = resp.getResponse().readEntity(String.class);
        String responseHeader = resp.getResponse().getHeaderString("requested");

        Assertions.assertEquals(expectedContent, responseText, "Response text should equal");
        Assertions.assertEquals("true", responseHeader, "Header text should equal");

    }

}
