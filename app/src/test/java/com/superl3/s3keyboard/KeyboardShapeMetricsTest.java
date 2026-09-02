package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardShapeMetricsTest {
    @Test
    public void cornerRadiusKeepsSameShortSideRatioAcrossKeyShapes() {
        float qwerty = KeyboardShapeMetrics.cornerRadiusPx(8, 32f, 50f);
        float dingul = KeyboardShapeMetrics.cornerRadiusPx(8, 82f, 50f);

        assertEquals(0.16f, qwerty / 32f, 0.0001f);
        assertEquals(0.16f, dingul / 50f, 0.0001f);
        assertTrue(dingul > qwerty);
    }

    @Test
    public void visualGapKeepsSameAreaScaleRatioAcrossKeyShapes() {
        float qwerty = KeyboardShapeMetrics.visualGapPx(5, 32f, 50f);
        float dingul = KeyboardShapeMetrics.visualGapPx(5, 82f, 50f);

        assertEquals(0.10f, qwerty / (float) Math.sqrt(32f * 50f), 0.0001f);
        assertEquals(0.10f, dingul / (float) Math.sqrt(82f * 50f), 0.0001f);
        assertTrue(dingul > qwerty);
    }
}
