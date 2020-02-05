package info.stasha.proxywarrior.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class CommonConfigTest {

    @Test
    public void getParentTest() {
        ResponseConfig config = new ResponseConfig();
        config.setParent(new RequestConfig());
        Assertions.assertNull(config.getParent(null));
        Assertions.assertNull(config.getParent(ResponseConfig.class));
        
        config.setParent(new ResponseConfig());
        Assertions.assertNull(config.getParent(null));
        Assertions.assertNotNull(config.getParent(ResponseConfig.class));
    }
}
