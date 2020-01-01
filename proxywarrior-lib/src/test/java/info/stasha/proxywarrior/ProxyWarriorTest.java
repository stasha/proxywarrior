package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Metadata;
import info.stasha.proxywarrior.config.TestModel;
import info.stasha.testosterone.annotation.Request;
import javax.servlet.http.HttpServletRequest;
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
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author stasha
 */
public class ProxyWarriorTest extends AbstractTest {

    @GET
    @Path("/proxy/test")
    public String passThroughProxyEndpoint(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        Metadata proxyRequest = (Metadata) req.getAttribute("proxy");
        Assert.assertNotNull("Request should have proxy attribute", proxyRequest);
        Assert.assertEquals("Model header should equal", TestModel.MODEL_HEADER, req.getHeader("xtra"));
        Assert.assertEquals("Model header should equal", TestModel.MODEL_2_HEADER, req.getHeader("Attribute-Header"));
        resp.setHeader("proxied", "true");

        return "get test";
    }

    @Test
    @Request(url = "/test")
    public void passThroughProxyTest(Response resp) {
        String proxiedHeaderValue = resp.getHeaderString("proxied");
        Assert.assertEquals("Proxied header should equal", "true", proxiedHeaderValue);
        Assert.assertEquals("Returned string should equal", "get test", resp.readEntity(String.class));
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
        String keepExisting = resp.getHeaderString("keep-eisting");
        Assert.assertEquals("Header value should equal", "add-if-not-present-original", addIfPresent);
        Assert.assertEquals("Header value should equal", "keep-existing-original", keepExisting);
        Assert.assertNull("Header should be null", resp.getHeaderString("delete-existing"));
        Assert.assertEquals("Response text should equal", "my file content", resp.readEntity(String.class));
    }

    @GET
    @Path("/proxy/textresponse")
    public String getTextResponse() {
        return "response text should be text thats specified in yaml response configuration";
    }

    @Test
    @Request(url = "/textresponse")
    public void testTextResponse(Response resp) {
        Assert.assertEquals("Response text should equal", "my text from yaml", resp.readEntity(String.class));
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
        Assert.assertEquals("Response text should equal", "my get text response", resp.readEntity(String.class));
    }

    @Test
    @Request(url = "/methodtextresponse?setHeader=true")
    public void testGetWithHeaderTextResponse(Response resp) {
        Assert.assertEquals("Response text should equal", "my get text with header response", resp.readEntity(String.class));
    }

    @POST
    @Path("/proxy/methodtextresponse")
    public String getPostTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "POST", entity = "")
    public void testPostTextResponse(Response resp) {
        Assert.assertEquals("Response text should equal", "my post text response", resp.readEntity(String.class));
    }

    @PUT
    @Path("/proxy/methodtextresponse")
    public String getPutTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "PUT", entity = "")
    public void testPutTextResponse(Response resp) {
        Assert.assertEquals("Response text should equal", "my put text response", resp.readEntity(String.class));
    }

    @DELETE
    @Path("/proxy/methodtextresponse")
    public String getDeleteTextResponse(@HeaderParam("x-delete-request") boolean header, @Context HttpServletResponse resp) {
        Assert.assertTrue("Header should be true", header);
        resp.setHeader("x-delete-response", "true");
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "DELETE", headerParams = "x-delete-request, true")
    public void testDeleteTextResponse(Response resp) {
        Assert.assertNotNull("Response header should not be null", resp.getHeaderString("x-delete-response"));
        Assert.assertEquals("Response text should equal", "my delete text response", resp.readEntity(String.class));
    }

    @HEAD
    @Path("/proxy/methodtextresponse")
    public String getHeadTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "HEAD")
    public void testHeadTextResponse(Response resp) {
        Assert.assertNotNull("Response header should not be null", resp.getHeaderString("x-default-header"));
    }

    @Test
    @Request(url = "/methodtextresponse", method = "HEAD", headerParams = "x-multy-method, true")
    public void testHeadOptionsTextResponse(Response resp) {
        Assert.assertNotNull("Response header should not be null", resp.getHeaderString("x-head-options-header"));
    }

    @OPTIONS
    @Path("/proxy/methodtextresponse")
    public String getOptionsTextResponse() {
        return "must return some text";
    }

    @Test
    @Request(url = "/methodtextresponse", method = "OPTIONS")
    public void testOptionsTextResponse(Response resp) {
        Assert.assertNotNull("Response header should not be null", resp.getHeaderString("x-default-header"));
        Assert.assertEquals("Response text should equal", "my default text response", resp.readEntity(String.class));
    }

    @Test
    @Request(url = "/methodtextresponse", method = "OPTIONS", headerParams = "x-multy-method, true")
    public void testOptionsHeadTextResponse(Response resp) {
        Assert.assertNotNull("Response header should not be null", resp.getHeaderString("x-head-options-header"));
        Assert.assertEquals("Response text should equal", "my head|options text response", resp.readEntity(String.class));
    }
}
