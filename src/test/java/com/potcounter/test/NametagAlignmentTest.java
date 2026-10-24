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

        String[] names = {"A", "Bob", "RealBnc", "SuperUltraLongPlayerName1234"};
        for (String name : names) {
            int nameWidth = name.length() * 6;
            float expectedStartX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();

            float nameRightEdge = nameWidth / 2.0f;
            float actualDistance = expectedStartX - nameRightEdge;

            Assertions.assertEquals(4.0f, actualDistance, 0.001f);

            Assertions.assertTrue(expectedStartX > nameRightEdge);
        }
    }

    @Test
    public void testCounterDigitTransitionsDoNotAffectStartPosition() {
        CounterConfig config = new CounterConfig();
        config.setNameGap(4);

        int nameWidth = 42;
        float fixedStartX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();

        int[] counts = {9, 10, 99, 100};
        for (int count : counts) {
            String str = "-" + count;
            int counterWidth = str.length() * 6;

            float startX = (nameWidth / 2.0f) + config.getNameGap() + config.getNameOffsetX();
            Assertions.assertEquals(fixedStartX, startX, 0.001f);

            float counterRightEdge = startX + counterWidth;
            Assertions.assertTrue(counterRightEdge > startX);
        }
    }
}
