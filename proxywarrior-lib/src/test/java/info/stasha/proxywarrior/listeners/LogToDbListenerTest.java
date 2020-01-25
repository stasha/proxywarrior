package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarriorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class LogToDbListenerTest {

    private LogToDbListener listener;

    @BeforeEach
    public void setUp() {
        listener = Mockito.spy(new LogToDbListener());

        Mockito.doNothing().when(listener).beginTransaction();
        Mockito.doNothing().when(listener).commitTransaction();
        Mockito.doNothing().when(listener).rollbackTransaction();
    }

    @Test
    public void runInTransactionExceptionTest() {
        Assertions.assertThrows(ProxyWarriorException.class, () -> {
            listener.runInTrunsaction(null, null, null);
        });
    }
}
