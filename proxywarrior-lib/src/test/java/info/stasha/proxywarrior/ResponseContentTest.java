package info.stasha.proxywarrior;

import info.stasha.testosterone.TestResponseBuilder.TestResponse;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class ResponseContentTest extends AbstractTest {
    
    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/resp-content-config.yaml";
        
        super.configure(config);
    }

    @GET
    @Path("/proxy/fileresponse")
    public String getFileResponse() {
        return "response text should be text read from file thats on hd";
    }

    @Test
    @Request(url = "/fileresponse")
    public void testFileResponse(Response resp) {
        String addIfPresent = resp.getHeaderString("add-if-not-present");
        String keepExisting = resp.getHeaderString("keep-existing");
        Assertions.assertEquals("add-if-not-present-original", addIfPresent, "Header value should equal");
        Assertions.assertEquals("keep-existing-original", keepExisting, "Header value should equal");
        Assertions.assertNull(resp.getHeaderString("delete-existing"), "Header should be null");
        Assertions.assertEquals("my file content", resp.readEntity(String.class), "Response text should equal");
    }

    @GET
    @Path("/proxy/textresponse")
    public String getTextResponse() {
        return "response text should be text thats specified in yaml response configuration";
    }

    @Test
    @Request(url = "/textresponse")
    public void testTextResponse(Response resp) {
        Assertions.assertEquals("my text from yaml", resp.readEntity(String.class), "Response text should equal");
    }

    @GET
    @Path("/proxy/methodtextresponse")
    public String getGetTextResponse(@QueryParam("setHeader") boolean header, @Context HttpServletResponse resp) {
        if (header) {
            resp.setHeader("x-get-header", "true");
        }
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse")
    public void testGetTextResponse(Response resp) {
        Assertions.assertEquals("my get text response", resp.readEntity(String.class), "Response text should equal");
    }

    @Test
    @Request(url = "/methodtextresponse?setHeader=true")
    public void testGetWithHeaderTextResponse(Response resp) {
        Assertions.assertEquals("my get text with header response", resp.readEntity(String.class), "Response text should equal");
    }

    @POST
    @Path("/proxy/methodtextresponse")
    public String getPostTextResponse(String text) {
        Assertions.assertEquals("post text", text, "Text argument should equal");
        return "must return some text";
    }

    public static String entityText = "post text";

    @Test
    @Request(url = "/methodtextresponse", method = "POST", entity = "entityText")
    public void testPostTextResponse(TestResponse resp) {
        Assertions.assertEquals("my post text response", resp.getResponse().readEntity(String.class), "Response text should equal");
    }

    @PUT
    @Path("/proxy/methodtextresponse")
    public String getPutTextResponse(String text) {
        Assertions.assertEquals("put text", text, "Text argument should equal");
        return "must return some text";
    }

    public static String entityText2 = "put text";

    @Test
    @Request(url = "/methodtextresponse", method = "PUT", entity = "entityText2")
    public void testPutTextResponse(Response resp) {
        Assertions.assertEquals("my put text response", resp.readEntity(String.class), "Response text should equal");
    }

    @DELETE
    @Path("/proxy/methodtextresponse")
    public String getDeleteTextResponse(@HeaderParam("x-delete-request") boolean header, @Context HttpServletResponse resp) {
        Assertions.assertTrue(header, "Header should be true");
        resp.setHeader("x-delete-response", "true");
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "DELETE", headerParams = "x-delete-request, true")
    public void testDeleteTextResponse(Response resp) {
        Assertions.assertNotNull(resp.getHeaderString("x-delete-response"), "Response header should not be null");
        Assertions.assertEquals("my delete text response", resp.readEntity(String.class), "Response text should equal");
    }

    @HEAD
    @Path("/proxy/methodtextresponse")
    public String getHeadTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "HEAD")
    public void testHeadTextResponse(Response resp) {
        Assertions.assertNotNull(resp.getHeaderString("x-default-header"), "Response header should not be null");
    }

    @Test
    @Request(url = "/methodtextresponse", method = "HEAD", headerParams = "x-multy-method, true")
    public void testHeadOptionsTextResponse(Response resp) {
        Assertions.assertNotNull(resp.getHeaderString("x-head-options-header"), "Response header should not be null");
    }

    @OPTIONS
    @Path("/proxy/methodtextresponse")
    public String getOptionsTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "OPTIONS")
    public void testOptionsTextResponse(Response resp) {
        Assertions.assertNotNull(resp.getHeaderString("x-default-header"), "Response header should not be null");
        Assertions.assertEquals("my default text response", resp.readEntity(String.class), "Response text should equal");
    }

    @Test
    @Request(url = "/methodtextresponse", method = "OPTIONS", headerParams = "x-multy-method, true")
    public void testOptionsHeadTextResponse(Response resp) {
        Assertions.assertNotNull(resp.getHeaderString("x-head-options-header"), "Response header should not be null");
        Assertions.assertEquals("my head|options text response", resp.readEntity(String.class), "Response text should equal");
    }
}
