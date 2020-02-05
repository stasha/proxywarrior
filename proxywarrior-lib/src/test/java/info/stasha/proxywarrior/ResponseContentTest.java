package info.stasha.proxywarrior;

import info.stasha.testosterone.TestResponseBuilder.TestResponse;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.annotation.Requests;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class ResponseContentTest extends AbstractTest {

    private File file;

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/response-content-config.yaml";

        super.configure(config);
    }

    private Response getResponse(String type, String testType) {
        file = new File("/tmp/" + type + ".txt");
        file.deleteOnExit();
        String content = type + " " + testType + " response";

        try {
            FileUtils.writeStringToFile(file, content, "UTF-8");
        } catch (IOException ex) {
            System.out.println(ex);
            throw new RuntimeException();
        }
        return Response.ok(type + " response").header("type", content).build();
    }

    @GET
    @Path("/proxy/{type}")
    public Response getTextTest(@PathParam("type") String type) {
        return getResponse("get", type);
    }

    @POST
    @Path("/proxy/{type}")
    public Response postTextTest(@PathParam("type") String type) {
        return getResponse("post", type);
    }

    @PUT
    @Path("/proxy/{type}")
    public Response putTextTest(@PathParam("type") String type) {
        return getResponse("put", type);
    }

    @DELETE
    @Path("/proxy/{type}")
    public Response deleteTextTest(@PathParam("type") String type) {
        return getResponse("delete", type);
    }

    @Test
    @Requests(requests = {
        @Request(url = "text", method = "GET"),
        @Request(url = "text", method = "POST"),
        @Request(url = "text", method = "PUT"),
        @Request(url = "text", method = "DELETE")
    })
    public void textResponseTest(TestResponse resp) {
        assertResponse(resp, "text");
    }

    @Test
    @Requests(requests = {
        @Request(url = "file", method = "GET"),
        @Request(url = "file", method = "POST"),
        @Request(url = "file", method = "PUT"),
        @Request(url = "file", method = "DELETE")
    })
    public void fileResponseTest(TestResponse resp) {
        assertResponse(resp, "file");
    }

    @Test
    @Requests(requests = {
        @Request(url = "classpath", method = "GET"),
        @Request(url = "classpath", method = "POST"),
        @Request(url = "classpath", method = "PUT"),
        @Request(url = "classpath", method = "DELETE")
    })
    public void classpathResponseTest(TestResponse resp) {
        assertResponse(resp, "classpath");
    }

    @Test
    @Requests(requests = {
        @Request(url = "global", method = "GET"),
        @Request(url = "global", method = "POST"),
        @Request(url = "global", method = "PUT"),
        @Request(url = "global", method = "DELETE")
    })
    public void globalResponseTest(TestResponse resp) {
        assertResponse(resp, "global");
    }

    @Test
    @Requests(requests = {
        @Request(url = "filename/get.txt", method = "GET"),
        @Request(url = "filename/post.txt", method = "POST"),
        @Request(url = "filename/put.txt", method = "PUT"),
        @Request(url = "filename/delete.txt", method = "DELETE")
    })
    public void filenameResponseTest(TestResponse resp) {
        assertResponse(resp, "classpath");
    }

    @Test
    @Requests(requests = {
        @Request(url = "precedence", method = "GET"),
        @Request(url = "precedence", method = "POST")
    })
    public void precedenceResponseTest(TestResponse resp) {
        assertResponse(resp, "precedence");
    }

    public void assertResponse(TestResponse resp, String testType) {
        String responseText = resp.getResponse().readEntity(String.class);
        String responseHeader = resp.getResponse().getHeaderString("type");
        String type = null;
        switch (resp.getRepeatIndex()) {
            case 1:
                type = "get";
                break;
            case 2:
                type = "post";
                break;
            case 3:
                type = "put";
                break;
            case 4:
                type = "delete";
                break;
        }

        String expected = type + " " + testType + " response";

        Assertions.assertEquals(expected, responseText, "Response text should equal");

        if (!resp.getRequest().url().contains("filename/")) {
            Assertions.assertEquals(expected, responseHeader, "Header text should equal");
        }
    }

}
