package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyWarriorException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author stasha
 */
public class LogToDbListenerTest {
    
    private LogToDbListener listener;
    
    @Before
    public void setUp(){
        listener = Mockito.spy(new LogToDbListener());
        
        Mockito.doNothing().when(listener).beginTransaction();
        Mockito.doNothing().when(listener).commitTransaction();
        Mockito.doNothing().when(listener).rollbackTransaction();
    }

    @Test(expected = ProxyWarriorException.class)
    public void runInTransactionExceptionTest() {
        listener.runInTrunsaction(null, null, null);
    }
}
