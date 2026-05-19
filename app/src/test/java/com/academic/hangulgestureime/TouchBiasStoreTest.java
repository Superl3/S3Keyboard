package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class TouchBiasStoreTest {
    @Test
    public void biasRecordsImmediateDeleteConservatively() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none()
                .recordImmediateDelete(20f, -10f, GestureAction.TAP);

        assertEquals(-1f, bias.xDp, 0.001f);
        assertEquals(0.5f, bias.yDp, 0.001f);
        assertEquals(1, bias.samples);
        assertEquals(0, bias.gestureThresholdAdjustmentDp);
    }

    @Test
    public void biasIsClamped() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none();
        for (int i = 0; i < 200; i++) {
            bias = bias.recordImmediateDelete(50f, 50f, GestureAction.TAP);
        }

        assertEquals(-TouchBiasStore.MAX_BIAS_DP, bias.xDp, 0.001f);
        assertEquals(-TouchBiasStore.MAX_BIAS_DP, bias.yDp, 0.001f);
    }

    @Test
    public void biasRoundTripsThroughEncodedStats() {
        TouchBiasStore.Bias bias = new TouchBiasStore.Bias(1.25f, -2.5f, 3, 4, 5);
        TouchBiasStore.Bias decoded = TouchBiasStore.Bias.decode(bias.encode());

        assertEquals(1.25f, decoded.xDp, 0.001f);
        assertEquals(-2.5f, decoded.yDp, 0.001f);
        assertEquals(3, decoded.samples);
        assertEquals(4, decoded.gestureThresholdAdjustmentDp);
        assertEquals(5, decoded.gestureSamples);
    }

    @Test
    public void slideDeletesRaiseGestureThresholdConservatively() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none();
        for (int i = 0; i < 100; i++) {
            bias = bias.recordImmediateDelete(0f, 0f, GestureAction.LEFT);
        }

        assertEquals(TouchBiasStore.MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP, bias.gestureThresholdAdjustmentDp);
        assertEquals(100, bias.gestureSamples);
        assertEquals(
                TouchBiasStore.MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP,
                bias.gestureThresholdAdjustmentForDirection(GestureAction.LEFT));
        assertEquals(
                TouchBiasStore.MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP,
                bias.gestureThresholdAdjustmentForDirection(GestureAction.RIGHT));
    }

    @Test
    public void legacyEncodedBiasStillDecodes() {
        TouchBiasStore.Bias decoded = TouchBiasStore.Bias.decode("1.0,-2.0,3");

        assertEquals(1f, decoded.xDp, 0.001f);
        assertEquals(-2f, decoded.yDp, 0.001f);
        assertEquals(3, decoded.samples);
        assertEquals(0, decoded.gestureThresholdAdjustmentDp);
    }
}
