package info.stasha.proxywarrior.listeners;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author stasha
 */
public class ListenersTest {

    @Test
    public void removeListenerTest() {
        Listeners l = new Listeners();
        Assert.assertFalse("No listeners should be added", l.add(null));

        l.add("info.stasha.proxywarrior.listeners.LogToDbListener");

        Assert.assertEquals("There should be 1 listener", 1, l.size());

        l.remove("info.stasha.proxywarrior.listeners.LogToDbListener");
        Assert.assertEquals("There should be 0 listeners", 0, l.size());

    }

}
