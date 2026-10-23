package com.potcounter.test;

import com.potcounter.render.ColorHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ColorInterpolationTest {

    @Test
    public void testColorValuesAtKeyThresholds() {
        // 0 -> Pure Green (0x00FF00)
        Assertions.assertEquals(0x00FF00, ColorHelper.getPotColor(0, true, 0));

        // 5 -> Light Yellow (0xFFFF55)
        Assertions.assertEquals(0xFFFF55, ColorHelper.getPotColor(5, true, 0));

        // 10 -> Light Red (0xFF5555)
        Assertions.assertEquals(0xFF5555, ColorHelper.getPotColor(10, true, 0));

        // 16 -> Full Red (0xFF0000)
        Assertions.assertEquals(0xFF0000, ColorHelper.getPotColor(16, true, 0));

        // 17+ -> Full Red (0xFF0000)
        Assertions.assertEquals(0xFF0000, ColorHelper.getPotColor(17, true, 0));
        Assertions.assertEquals(0xFF0000, ColorHelper.getPotColor(99, true, 0));
    }

    @Test
    public void testColorSmoothMonotonicTransitions() {
        int prevRed = -1;
        // From 0 to 5, Red must increase monotonically from 0 to 255
        for (int i = 0; i <= 5; i++) {
            int c = ColorHelper.getPotColor(i, true, 0);
            int red = (c >> 16) & 0xFF;
            Assertions.assertTrue(red >= prevRed, "Red component should increase from 0 to 5");
            prevRed = red;
        }

        // From 5 to 16, Green must decrease monotonically from 255 to 0
        int prevGreen = 256;
        for (int i = 5; i <= 16; i++) {
            int c = ColorHelper.getPotColor(i, true, 0);
            int green = (c >> 8) & 0xFF;
            Assertions.assertTrue(green <= prevGreen, "Green component should decrease from 5 to 16");
            prevGreen = green;
        }
    }

    @Test
    public void testCustomColorBypassesAutoColor() {
        int custom = 0x123456;
        Assertions.assertEquals(custom, ColorHelper.getPotColor(10, false, custom));
    }
}
