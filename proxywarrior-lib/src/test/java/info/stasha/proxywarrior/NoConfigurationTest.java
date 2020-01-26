package info.stasha.proxywarrior;

import info.stasha.proxywarrior.config.Metadata;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class NoConfigurationTest extends AbstractTest {

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = null;
        super.configure(config);
    }

    @Override
    public void afterServerStop() throws Exception {
        super.afterServerStop();
    }

    @GET
    @Path("/passthrough")
    public String noConfigTest(@Context HttpServletRequest req, @Context HttpServletResponse resp) {
        Metadata metadata = (Metadata) req.getAttribute("proxy");
        Assertions.assertNotNull(metadata, "Request should have proxy attribute");

        resp.setHeader("proxied", "true");

        return "get test";
    }

    @Test
    @Request(url = "/passthrough")
    public void noConfigTest(Response resp) {
        String proxiedHeaderValue = resp.getHeaderString("proxied");
        Assertions.assertEquals("true", proxiedHeaderValue, "Proxied header should equal");
        Assertions.assertEquals("get test", resp.readEntity(String.class), "Returned string should equal");
    }

}
