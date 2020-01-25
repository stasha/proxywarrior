package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarriorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class LogToConsoleListenerTest {

    @Test
    public void getHeadersExceptionTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            new LogToConsoleListener().getHeaders(null);
        });
    }

    @Test
    public void getContentStreamExceptionTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            new LogToConsoleListener().getInputStream(null);
        });
    }
}
