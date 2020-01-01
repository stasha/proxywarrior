package info.stasha.proxywarrior;

import info.stasha.testosterone.jersey.junit4.Testosterone;
import info.stasha.testosterone.junit4.TestosteroneRunner;
import info.stasha.testosterone.servlet.Filter;
import info.stasha.testosterone.servlet.ServletContainerConfig;
import org.junit.Assert;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stasha
 */
@RunWith(TestosteroneRunner.class)
public abstract class AbstractTest implements Testosterone {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class.getName());

    protected final ProxyWarrior PROXY_FILTER = Mockito.spy(new ProxyWarrior());
    protected static boolean failed = false;
    protected static boolean useConfig = true;

    @Override
    public void configure(ServletContainerConfig config) {
        Filter f = new Filter(PROXY_FILTER, "/*");
        if (useConfig == true) {
            f.getInitParams().put("PROPS_LOCATION", this.getClass().getResource("/config.yaml").getPath());
        }
        config.addFilter(f);

        try {
            Mockito.doAnswer((invocation) -> {
                try {
                    return invocation.callRealMethod();
                } catch (Exception ex) {
                    LOGGER.error("ProxyWarrior exception", ex);
                    failed = true;
                    throw ex;
                }
            }).when(PROXY_FILTER).doFilter(any(), any(), any());
        } catch (Exception ex) {
            LOGGER.error("ProxyWarrior exception", ex);
            Assert.fail("There should be no exception");
        }
    }

    @Override
    public void afterServerStop() throws Exception {
        Assert.assertFalse("Request should not fail", failed);
        Mockito.verify(PROXY_FILTER, atLeast(1)).doFilter(any(), any(), any());
    }
}
