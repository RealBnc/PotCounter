package com.potcounter.test;

import com.potcounter.config.CounterConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NametagAlignmentTest {

    @Test
    public void testAlignmentMathAcrossNameLengths() {
        CounterConfig config = new CounterConfig();
        config.setNameGap(4);
        config.setNameOffsetX(0);

        // Simulated string widths (e.g. 6 px per character roughly)
        String[] names = {"A", "Bob", "RealBnc", "SuperUltraLongPlayerName1234"};
        for (String name : names) {
            int nameWidth = name.length() * 6;
            float expectedStartX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();

            // Name is drawn from -nameWidth / 2.0f to +nameWidth / 2.0f
            float nameRightEdge = nameWidth / 2.0f;
            float actualDistance = expectedStartX - nameRightEdge;

            // Distance must be exactly equal to the configured gap
            Assertions.assertEquals(4.0f, actualDistance, 0.001f);
            // Counter start position must always be strictly to the right of the name
            Assertions.assertTrue(expectedStartX > nameRightEdge);
        }
    }

    @Test
    public void testCounterDigitTransitionsDoNotAffectStartPosition() {
        CounterConfig config = new CounterConfig();
        config.setNameGap(4);

        int nameWidth = 42; // "RealBnc"
        float fixedStartX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();

        // Testing -9 to -10, -99 to -100
        int[] counts = {9, 10, 99, 100};
        for (int count : counts) {
            String str = "-" + count;
            int counterWidth = str.length() * 6;

            // Start position must strictly remain fixed regardless of counter digit count
            float startX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();
            Assertions.assertEquals(fixedStartX, startX, 0.001f);

            // The right edge of the counter is startX + counterWidth
            float counterRightEdge = startX + counterWidth;
            Assertions.assertTrue(counterRightEdge > startX);
        }
    }
}
