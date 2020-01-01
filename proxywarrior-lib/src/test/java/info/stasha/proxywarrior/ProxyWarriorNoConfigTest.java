package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Metadata;
import info.stasha.testosterone.annotation.Request;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author stasha
 */
public class ProxyWarriorNoConfigTest extends AbstractTest {

    static {
        AbstractTest.useConfig = false;
    }

    @Override
    public void afterServerStop() throws Exception {
        super.afterServerStop();
        AbstractTest.useConfig = true;
    }

    @GET
    @Path("/test")
    public String passThroughProxyEndpoint(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        Metadata metadata = (Metadata) req.getAttribute("proxy");
        Assert.assertNotNull("Request should have proxy attribute", metadata);

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

}
