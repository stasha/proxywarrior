package info.stasha.proxywarrior.listeners;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class LogObjectTest {

    @Test
    public void toStringTest() {
        LogObject l = new LogObject();
        l.setContentStream(null);
        Assertions.assertEquals("", l.toString(), "To string should be empty string");
    }
}
