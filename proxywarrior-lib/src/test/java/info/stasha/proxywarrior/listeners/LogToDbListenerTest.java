package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarriorException;
import org.junit.Test;

/**
 *
 * @author stasha
 */
public class LogToDbListenerTest {

    @Test(expected = ProxyWarriorException.class)
    public void runInTransactionExceptionTest() {
        new LogToDbListener().runInTrunsaction(null, null, null);
    }
}
