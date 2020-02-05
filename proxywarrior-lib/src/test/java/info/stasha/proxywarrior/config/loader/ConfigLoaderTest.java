package info.stasha.proxywarrior.config.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import info.stasha.proxywarrior.ProxyWarriorException;
import info.stasha.proxywarrior.config.RequestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class ConfigLoaderTest {

    private ConfigLoader loader;
    private ConfigChangeListener listener;

    @BeforeEach
    public void setUp() {
        listener = Mockito.mock(ConfigChangeListener.class);
        loader = Mockito.spy(new ConfigLoader(null, listener));
        ConfigLoader.setUserConfigString(null);
        ConfigLoader.setDefaultConfigPath(null);
        ConfigLoader.setDefaultConfigString(null);
        ConfigLoader.setEffectiveConfig(null);
        ConfigLoader.setCompareConfig(null);
    }

    @AfterEach
    public void tearDown() {
        ConfigLoader.setDefaultConfigPath("/default-config.yaml");
    }

    @Test
    public void runTest() throws Exception {
        Mockito.doReturn(null).when(loader).loadConfig(Mockito.any());
        loader.run();
    }

    @Test
    public void notifyListenerTest() throws Exception {
        Mockito.doReturn(new RequestConfig()).when(loader).loadConfig(Mockito.any());
        loader.run();
        Mockito.verify(listener).notify(Mockito.any());
    }

    @Test
    public void runExceptionTest() throws Exception {
        Mockito.doReturn(new RequestConfig()).when(loader).loadConfig(Mockito.any());
        Mockito.doThrow(RuntimeException.class).when(listener).notify(Mockito.any());
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            loader.run();
        }, "Run should throw exception");
    }

    @Test
    public void loadTest() {
        Assertions.assertNull(ConfigLoader.load(null));
    }

    @Test
    public void getEffectiveConfigTest() throws JsonProcessingException {
        Assertions.assertNotNull(ConfigLoader.getEffectiveConfig());
    }

    @Test
    public void loadConfigurationTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            ConfigLoader.load("path");
        }, "Loading configuration should throw exception");
    }

    @Test
    public void getDefaultConfigStringTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            ConfigLoader.getDefaultConfigString();
        }, "Loading configuration should throw exception");
    }

    @Test
    public void getDefaultConfigTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            ConfigLoader.getDefaultConfiguration();
        }, "Loading configuration should throw exception");
    }

    @Test
    public void setConfigurationTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            ConfigLoader.setConfiguration("fdsaf");
        }, "Loading configuration should throw exception");
    }
}
