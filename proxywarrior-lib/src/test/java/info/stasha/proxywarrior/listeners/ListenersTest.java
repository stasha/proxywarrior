package info.stasha.proxywarrior.listeners;

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

    }

}
