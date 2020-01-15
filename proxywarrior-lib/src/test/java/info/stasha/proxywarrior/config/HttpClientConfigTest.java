package info.stasha.proxywarrior.config;

import info.stasha.proxywarrior.ProxyWarriorException;
import java.io.IOException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class HttpClientConfigTest {

    @Test(expected = ProxyWarriorException.class)
    public void httpClientCloseExceptionTest() throws IOException {
        CloseableHttpClient client = Mockito.mock(CloseableHttpClient.class);
        Mockito.doThrow(IOException.class).when(client).close();
        HttpClientConfig cc = new HttpClientConfig();
        cc.setHttpClient(client);
        cc.dispose();
    }
}
