package info.stasha.proxywarrior;

import info.stasha.testosterone.TestResponseBuilder.TestResponse;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.annotation.Requests;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class HeadersTest extends AbstractTest {

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/headers-config.yaml";

        super.configure(config);
    }

    private String assertDefaultRequest(String defaultHeader, String defaultRequestHeader, String defaultResponseHeader) {
        Assertions.assertEquals("default-header-value", defaultHeader, "Header value should equal");
        Assertions.assertEquals("default-request-header-value", defaultRequestHeader, "Header value should equal");
        Assertions.assertNull(defaultResponseHeader, "Header value should be null");
        return "default header response";
    }

    private void assertDefaultResponse(Response resp) {
        Assertions.assertEquals("default-header-value", resp.getHeaderString("default-header"), "Header value should equal");
        Assertions.assertEquals("default-response-header-value", resp.getHeaderString("default-response-header"), "Header value should equal");
        Assertions.assertNull(resp.getHeaderString("default-request-header"), "Header value should be null");
        Assertions.assertEquals("default header response", resp.readEntity(String.class), "Response value should equal");
    }

    @GET
    @Path("/proxy/defaultheader")
    public String defaultHeader(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertDefaultRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @Test
    @Request(url = "/defaultheader")
    public void defaultHeaderTest(Response resp) {
        assertDefaultResponse(resp);
    }

    @GET
    @Path("/proxy/keepdefaultheader")
    public String keepDefaultHeader(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertDefaultRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @Test
    @Request(url = "/keepdefaultheader")
    public void keepDefaultHeaderTest(Response resp) {
        assertDefaultResponse(resp);
    }

    @GET
    @Path("/proxy/addheaderifitdoesnotexist")
    public String addDefaultHeaderIfItDoesNotExist(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertDefaultRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @Test
    @Request(url = "/addheaderifitdoesnotexist")
    public void addDefaultHeaderIfItDoesNotExist(Response resp) {
        assertDefaultResponse(resp);
    }

    @GET
    @Path("/proxy/removedefaultheader")
    public String removeDefaultHeader(@HeaderParam("default-header") String defaultHeader) {
        Assertions.assertNull(defaultHeader, "Header value should be null");
        return "removed default header response";
    }

    @Test
    @Request(url = "/removedefaultheader")
    public void removeDefaultHeaderTest(Response resp) {
        Assertions.assertNull(resp.getHeaderString("default-header"), "Header value should be null");
        Assertions.assertEquals("removed default header response", resp.readEntity(String.class), "Response value should equal");
    }

    @GET
    @Path("/proxy/addorreplaceheader")
    public String addOrReplaceDefaultHeader(@HeaderParam("default-header") String defaultHeader) {
        Assertions.assertEquals("replacing-default-header-value", defaultHeader, "Header value should equal");
        return "default header response";
    }

    @Test
    @Request(url = "/addorreplaceheader")
    public void addOrReplaceDefaultHeader(Response resp) {
        Assertions.assertEquals("replacing-default-header-value", resp.getHeaderString("default-header"), "Header value should equal");
        Assertions.assertEquals("default header response", resp.readEntity(String.class), "Response value should equal");
    }

    @GET
    @Path("/proxy/overridedefaultheader")
    public String overrideDefaultHeader(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        Assertions.assertEquals("request-overriding-default-header-value", defaultHeader, "Header value should equal");
        Assertions.assertEquals("default-request-header-value", defaultRequestHeader, "Header value should equal");
        Assertions.assertNull(defaultResponseHeader, "Header value should be null");
        return "overriding header response";
    }

    @Test
    @Request(url = "/overridedefaultheader")
    public void overrideDefaultHeader(Response resp) {
        Assertions.assertEquals("response-overriding-default-header-value", resp.getHeaderString("default-header"), "Header value should equal");
        Assertions.assertEquals("default-response-header-value", resp.getHeaderString("default-response-header"), "Header value should equal");
        Assertions.assertNull(resp.getHeaderString("default-request-header"), "Header value should be null");
        Assertions.assertEquals("overriding header response", resp.readEntity(String.class), "Response value should equal");
    }

    private String assertOverrideRequestHeaderRequest(String method, String defaultHeader, String defaultRequestHeader, String defaultResponseHeader) {
        String expected = "";
        switch (method) {
            case "GET":
                expected = "get-overriding-default-header";
                break;
            case "OPTIONS":
                expected = "options-overriding-default-header";
                break;
            default:
                expected = "overriding-common-default-header";

        }
        Assertions.assertEquals(expected, defaultHeader, "Header value should equal");
        Assertions.assertEquals("default-request-header-value", defaultRequestHeader, "Header value should equal");
        Assertions.assertNull(defaultResponseHeader, "Header value should be null");
        return "response overriding header response";
    }

    @GET
    @Path("/proxy/overriderequestheaderGet")
    public String overriderequestheaderGet(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideRequestHeaderRequest("GET", defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @OPTIONS
    @Path("/proxy/overriderequestheaderOptions")
    public String overriderequestheaderOptions(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideRequestHeaderRequest("OPTIONS", defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @DELETE
    @Path("/proxy/overriderequestheaderDelete")
    public String overriderequestheaderDelete(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideRequestHeaderRequest("DELETE", defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @Test
    @Requests(requests = {
        @Request(url = "/overriderequestheaderGet"),
        @Request(url = "/overriderequestheaderOptions", method = "OPTIONS"),
        @Request(url = "/overriderequestheaderDelete", method = "DELETE")})
    public void overrideRequestHeader(TestResponse response) {
        Response resp = response.getResponse();

        Assertions.assertEquals("default-header-value", resp.getHeaderString("default-header"), "Header value should equal");
        Assertions.assertEquals("default-response-header-value", resp.getHeaderString("default-response-header"), "Header value should equal");
        Assertions.assertNull(resp.getHeaderString("default-request-header"), "Header value should be null");
        Assertions.assertEquals("response overriding header response", resp.readEntity(String.class), "Response value should equal");
    }

    private String assertOverrideResponseHeaderRequest(String defaultHeader, String defaultRequestHeader, String defaultResponseHeader) {
        Assertions.assertEquals("default-header-value", defaultHeader, "Header value should equal");
        Assertions.assertEquals("default-request-header-value", defaultRequestHeader, "Header value should equal");
        Assertions.assertNull(defaultResponseHeader, "Header value should be null");
        return "response overriding header response";
    }

    @GET
    @Path("/proxy/overrideresponseheaderGet")
    public String overrideResponseHeaderGet(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideResponseHeaderRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @OPTIONS
    @Path("/proxy/overrideresponseheaderOptions")
    public String overrideResponseHeaderOptions(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideResponseHeaderRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @DELETE
    @Path("/proxy/overrideresponseheaderDelete")
    public String overrideResponseHeaderDelete(
            @HeaderParam("default-header") String defaultHeader,
            @HeaderParam("default-request-header") String defaultRequestHeader,
            @HeaderParam("default-response-header") String defaultResponseHeader) {
        return assertOverrideResponseHeaderRequest(defaultHeader, defaultRequestHeader, defaultResponseHeader);
    }

    @Test
    @Requests(requests = {
        @Request(url = "/overrideresponseheaderGet"),
        @Request(url = "/overrideresponseheaderOptions", method = "OPTIONS"),
        @Request(url = "/overrideresponseheaderDelete", method = "DELETE")})
    public void overrideResponseHeader(TestResponse response) {
        String expected = "";
        Response resp = response.getResponse();
        Request req = response.getRequest();
        switch (req.method()) {
            case "GET":
                expected = "get-overriding-default-header";
                break;
            case "OPTIONS":
                expected = "options-overriding-default-header";
                break;
            default:
                expected = "overriding-common-default-header";
        }

        Assertions.assertEquals(expected, resp.getHeaderString("default-header"), "Header value should equal");
        Assertions.assertEquals("default-response-header-value", resp.getHeaderString("default-response-header"), "Header value should equal");
        Assertions.assertNull(resp.getHeaderString("default-request-header"), "Header value should be null");
        Assertions.assertEquals("response overriding header response", resp.readEntity(String.class), "Response value should equal");
    }

    @GET
    @Path("/proxy/addnewheader")
    public String addNewHeader(
            @HeaderParam("new-common-request-header") String newCommonHeader,
            @HeaderParam("new-specific-request-header") String newSpecificHeader) {
        Assertions.assertEquals("new-common-request-header-value", newCommonHeader, "Header value should equal");
        Assertions.assertEquals("new-specific-request-header-value", newSpecificHeader, "Header value should equal");
        return "add new header response";
    }

    @Test
    @Request(url = "/addnewheader")
    public void addNewHeader(Response resp) {
        Assertions.assertEquals("new-common-response-header-value", resp.getHeaderString("new-common-response-header"), "Header value should equal");
        Assertions.assertEquals("new-specific-response-header-value", resp.getHeaderString("new-specific-response-header"), "Header value should equal");
        Assertions.assertEquals("default-response-header-value", resp.getHeaderString("default-response-header"), "Header value should equal");
        Assertions.assertEquals("add new header response", resp.readEntity(String.class), "Response value should equal");
    }
    
    @GET
    @Path("/proxy/removeallheaders")
    public String removeallheaders(
            @HeaderParam("default-header") String newCommonHeader,
            @HeaderParam("default-request-header") String newSpecificHeader) {
        Assertions.assertNull(newCommonHeader, "Header should be null");
        Assertions.assertNull(newSpecificHeader, "Header should be null");
        return "remove all headers response";
    }

    @Test
    @Request(url = "/removeallheaders")
    public void removeallheaders(Response resp) {
        Assertions.assertNull(resp.getHeaderString("default-header"), "Header should be null");
        Assertions.assertNull(resp.getHeaderString("default-response-header"), "Header should be null");
        Assertions.assertEquals("remove all headers response", resp.readEntity(String.class), "Response value should equal");
    }
    
}
