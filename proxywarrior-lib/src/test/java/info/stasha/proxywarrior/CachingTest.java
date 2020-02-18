package info.stasha.proxywarrior;

import info.stasha.testosterone.TestResponseBuilder.TestResponse;
import info.stasha.testosterone.annotation.Request;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Caching test.
 *
 * @author stasha
 */
public class CachingTest extends AbstractTest {

    private static Date responseDate;
    private static String responseContent;
    private static int updatedCount;

    @Override
    public void configure(ServletContainerConfig config) {
        this.configPath = "/caching-config.yaml";

        super.configure(config);
    }

    @BeforeEach
    public void setUp() {
        responseDate = null;
        responseContent = null;
    }

    @GET
    @Path("/proxy/responsecache")
    public String cacheTest() {
        responseDate = null;
        return "cache response" + Math.random();
    }

    @Disabled(value = "Caching requires refactoring")
    @Test
    @Request(url = "responsecache", repeat = 10)
    public void responsecache(TestResponse response) throws InterruptedException {
        if (response.getRepeatIndex() == 1) {
            updatedCount = 0;
        }
        Thread.sleep(100);

        Response resp = response.getResponse();
        String content = resp.readEntity(String.class);
        Date date = new Date(resp.getHeaderString("Date"));

        if (responseDate == null) {
            responseContent = content;
            responseDate = date;
        }

        Assertions.assertEquals(responseContent, content, "Response content should equal");
        Assertions.assertEquals(responseDate, date, "Response date should equal");
    }

    @Disabled(value = "Caching requires refactoring")
    @Test
    @Request(url = "responsecache", repeat = 10)
    public void cacheexpiration(TestResponse response) throws InterruptedException {
        if (response.getRepeatIndex() == 1) {
            updatedCount = 0;
        }
        Thread.sleep(1000);

        Response resp = response.getResponse();

        String content = resp.readEntity(String.class);
        Date date = new Date(resp.getHeaderString("Date"));

        if (responseDate == null) {
            updatedCount++;
            responseContent = content;
            responseDate = date;
        }

        Assertions.assertEquals(responseContent, content, "Response content should equal");
        Assertions.assertEquals(responseDate, date, "Response date should equal");

        if (response.getRepeatIndex() == 10) {
            Assertions.assertEquals(2, updatedCount, "Cache should expired equal times");
        }
    }

    @GET
    @Path("/proxy/disabledcache")
    public String disabledcache() {
        responseDate = null;
        return "cache response" + Math.random();
    }

    @Test
    @Request(url = "disabledcache", repeat = 10)
    public void disabledcache(TestResponse response) throws InterruptedException {
        if (response.getRepeatIndex() == 1) {
            updatedCount = 0;
        }
        Thread.sleep(100);

        Response resp = response.getResponse();

        String content = resp.readEntity(String.class);
        Date date = new Date(resp.getHeaderString("Date"));

        if (responseDate == null) {
            updatedCount++;
            responseContent = content;
            responseDate = date;
        }

        Assertions.assertEquals(responseContent, content, "Response content should equal");
        Assertions.assertEquals(responseDate, date, "Response date should equal");

        if (response.getRepeatIndex() == 10) {
            Assertions.assertEquals(10, updatedCount, "Cache should expired equal times");
        }
    }

    @GET
    @Path("/proxy/noexpiration")
    public String noexpiration() {
        responseDate = null;
        return "cache response" + Math.random();
    }

    @Test
    @Request(url = "noexpiration", repeat = 10)
    public void noexpiration(TestResponse response) throws InterruptedException {
        if (response.getRepeatIndex() == 1) {
            updatedCount = 0;
        }
        Thread.sleep(100);

        Response resp = response.getResponse();

        String content = resp.readEntity(String.class);
        Date date = new Date(resp.getHeaderString("Date"));

        if (responseDate == null) {
            updatedCount++;
            responseContent = content;
            responseDate = date;
        }

        Assertions.assertEquals(responseContent, content, "Response content should equal");
        Assertions.assertEquals(responseDate, date, "Response date should equal");

        if (response.getRepeatIndex() == 10) {
            Assertions.assertEquals(10, updatedCount, "Cache should expired equal times");
        }
    }

}
