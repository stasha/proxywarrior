package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarriorException;
import org.junit.Test;

/**
 *
 * @author stasha
 */
public class LogToConsoleListenerTest {

    @Test(expected = ProxyWarriorException.class)
    public void getHeadersExceptionTest() {
        new LogToConsoleListener().getHeaders(null);
    }

    @Test(expected = ProxyWarriorException.class)
    public void getContentStreamExceptionTest() {
        new LogToConsoleListener().getInputStream(null);
    }
}
