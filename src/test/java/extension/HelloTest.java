
package extension;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class HelloTest {

    @Test
    public void tenEqualToTen() {
        assertEquals(10, 10);
    }
}