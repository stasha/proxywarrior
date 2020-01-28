package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.AbstractTest;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.testosterone.annotation.DontIntercept;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class LogToConsoleListenerTest extends AbstractTest {

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/logging-config.yaml";

        super.configure(config);
    }

    @Test
    @DontIntercept
    public void getHeadersExceptionTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            new LogToConsoleListener().getHeaders(null);
        });
    }

    @Test
    @DontIntercept
    public void getContentStreamExceptionTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            new LogToConsoleListener().getInputStream(null);
        });
    }

    @GET
    @Path("proxy/disabledlogging")
    public String logging() {
        return "logging response";
    }

    @Test
    @Request(url = "/disabledlogging")
    public void loggingTest(Response resp) {
        Assertions.assertEquals("logging response", resp.readEntity(String.class), "Response should equal");
    }

    @GET
    @Path("proxy/disablecontentlogging")
    public String disablecontentlogging() {
        return "disabled content logging response";
    }

    @Test
    @Request(url = "/disablecontentlogging")
    public void disablecontentlogging(Response resp) {
        Assertions.assertEquals("disabled content logging response", resp.readEntity(String.class), "Response should equal");
    }

    @GET
    @Path("proxy/enablecontentlogging")
    public String enablecontentlogging() {
        return "enabled content logging response";
    }

    @Test
    @Request(url = "/enablecontentlogging")
    public void enablecontentlogging(Response resp) {
        Assertions.assertEquals("enabled content logging response", resp.readEntity(String.class), "Response should equal");
    }
}
