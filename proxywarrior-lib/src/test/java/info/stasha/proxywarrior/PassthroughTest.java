package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Metadata;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests requests that are not proxied using proxywarrior but just passed
 * through to server as every other normal request.
 *
 * @author stasha
 */
public class PassthroughTest extends AbstractTest {

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/passthrough-config.yaml";

        super.configure(config);
    }

    @GET
    @Path("/proxy/passthrough")
    public String passThroughProxyEndpoint(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        Metadata proxyRequest = (Metadata) req.getAttribute("proxy");
        Assertions.assertNotNull(proxyRequest, "Request should have proxy attribute");

        resp.setHeader("proxied", "true");
        return "passthrough response";
    }

    @Test
    @Request(url = "/passthrough")
    public void passThroughProxyTest(Response resp) {
        String proxiedHeaderValue = resp.getHeaderString("proxied");
        Assertions.assertEquals("true", proxiedHeaderValue, "Proxied header should equal");
        Assertions.assertEquals("passthrough response", resp.readEntity(String.class), "Returned string should equal");
    }

    @GET
    @Path("/proxy/noproxy")
    public String noproxyEndpoint(@HeaderParam("notavailableheader") String header) {
        Assertions.assertNull(header, "Request should have proxy attribute");
        return "noproxy response";
    }

    @Test
    @Request(url = "/proxy/noproxy")
    public void noProxyTest(Response resp) {
        Assertions.assertEquals("noproxy response", resp.readEntity(String.class), "Returned string should equal");
    }
}
