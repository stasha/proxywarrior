package info.stasha.proxywarrior.listeners;

import info.stasha.proxywarrior.ProxyAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class ListenersTest {

    @Test
    public void removeListenerTest() {
        Listeners l = new Listeners();
        Assertions.assertFalse(l.add(null), "No listeners should be added");

        l.add("info.stasha.proxywarrior.listeners.LogToDbListener");

        Assertions.assertEquals(1, l.size(), "There should be 1 listener");
        
        l.remove("info.stasha.proxywarrior.listeners.LogToDbListener");
        Assertions.assertEquals(0, l.size(), "There should be 0 listeners");
        
        l.fire(ProxyAction.AFTER_HTTP_REQUEST, null);
        l.fire(ProxyAction.AFTER_NOT_PROXY_REQUEST, null);
        l.fire(ProxyAction.BEFORE_PROXY_REQUEST, null);
        l.fire(ProxyAction.AFTER_PROXY_RESPONSE, null);
        l.fire(ProxyAction.BEFORE_HTTP_RESPONSE, null);
        l.fire(ProxyAction.AFTER_HTTP_RESPONSE, null);

    }

}
