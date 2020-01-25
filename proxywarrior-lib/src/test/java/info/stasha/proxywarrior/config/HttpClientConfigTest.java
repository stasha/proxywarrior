package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.ProxyWarriorException;
import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class HttpClientConfigTest {

    @Test
    public void httpClientCloseExceptionTest() throws IOException {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
            Mockito.doThrow(IOException.class).when(client).close();
            HttpClientConfig cc = new HttpClientConfig();
            cc.setHttpClient(client);
            cc.dispose();
        });
    }
}
