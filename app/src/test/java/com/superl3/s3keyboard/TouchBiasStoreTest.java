package com.superl3.s3keyboard;

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
        TouchBiasStore.Bias bias = new TouchBiasStore.Bias(1.25f, -2.5f, 3, 4, 5)
                .recordTextInput(GestureAction.TAP);
        TouchBiasStore.Bias decoded = TouchBiasStore.Bias.decode(bias.encode());

        assertEquals(1.25f, decoded.xDp, 0.001f);
        assertEquals(-2.5f, decoded.yDp, 0.001f);
        assertEquals(3, decoded.samples);
        assertEquals(4, decoded.gestureThresholdAdjustmentDp);
        assertEquals(5, decoded.gestureSamples);
        assertEquals(4, decoded.textSamples);
        assertEquals(3, decoded.correctionSamples);
    }

    @Test
    public void slideDeletesRaiseGestureThresholdWithSmallGlobalCap() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none();
        for (int i = 0; i < 100; i++) {
            bias = bias.recordImmediateDelete(0f, 0f, GestureAction.LEFT);
        }

        assertEquals(TouchBiasStore.MAX_GLOBAL_GESTURE_THRESHOLD_ADJUSTMENT_DP, bias.gestureThresholdAdjustmentDp);
        assertEquals(100, bias.gestureSamples);
        assertEquals(
                TouchBiasStore.MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP,
                bias.gestureThresholdAdjustmentForDirection(GestureAction.LEFT));
        assertEquals(
                TouchBiasStore.MAX_GLOBAL_GESTURE_THRESHOLD_ADJUSTMENT_DP,
                bias.gestureThresholdAdjustmentForDirection(GestureAction.RIGHT));
    }

    @Test
    public void acceptedSlidesDecayStuckGestureThresholdPenalty() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none();
        for (int i = 0; i < 100; i++) {
            bias = bias.recordImmediateDelete(0f, 0f, GestureAction.RIGHT);
        }

        bias = bias.recordTextInput(GestureAction.RIGHT)
                .recordTextInput(GestureAction.RIGHT);

        assertEquals(2, bias.gestureThresholdAdjustmentDp);
        assertEquals(4, bias.gestureThresholdAdjustmentForDirection(GestureAction.RIGHT));
        assertEquals(2, bias.gestureThresholdAdjustmentForDirection(GestureAction.LEFT));
    }

    @Test
    public void legacyEncodedBiasStillDecodes() {
        TouchBiasStore.Bias decoded = TouchBiasStore.Bias.decode("1.0,-2.0,3");

        assertEquals(1f, decoded.xDp, 0.001f);
        assertEquals(-2f, decoded.yDp, 0.001f);
        assertEquals(3, decoded.samples);
        assertEquals(0, decoded.gestureThresholdAdjustmentDp);
    }

    @Test
    public void typingPatternStatsTrackInputAndCorrectionRate() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none()
                .recordTextInput(GestureAction.TAP)
                .recordTextInput(GestureAction.LEFT)
                .recordImmediateDelete(1f, 1f, GestureAction.TAP);

        assertEquals(2, bias.textSamples);
        assertEquals(1, bias.correctionSamples);
        assertEquals(500, bias.correctionRatePermille());
    }

    @Test
    public void typingPatternLogKeepsRawTextForLocalLearning() {
        String log = TouchBiasStore.appendTypingEvent(
                "",
                "input",
                "ㄱ",
                GestureAction.TAP,
                0f,
                0f);
        log = TouchBiasStore.appendTypingEvent(
                log,
                "correction",
                "ㄱ",
                GestureAction.TAP,
                2f,
                -1f);

        org.junit.Assert.assertTrue(log.contains("\"text\":\"ㄱ\""));
        org.junit.Assert.assertTrue(log.contains("\"type\":\"correction\""));
    }

    @Test
    public void dingulProfileAddsPenaltyOnlyAfterRepeatedDirectionalCorrections() {
        TouchBiasStore.DingulTouchProfile profile = TouchBiasStore.DingulTouchProfile.empty();
        for (int i = 0; i < 10; i++) {
            profile.recordInput("3131", GestureAction.LEFT);
        }
        profile.recordCorrection("3131", GestureAction.LEFT, 4f, -2f);

        assertEquals(10, profile.inputCount("3131", GestureAction.LEFT));
        assertEquals(1, profile.correctionCount("3131", GestureAction.LEFT));
        assertEquals(0, profile.penaltyDp("3131", GestureAction.LEFT));

        for (int i = 0; i < 3; i++) {
            profile.recordCorrection("3131", GestureAction.LEFT, 4f, -2f);
        }
        profile.recordCorrection("3131", GestureAction.LEFT, 4f, -2f);
        assertEquals(6, profile.penaltyDp("3131", GestureAction.LEFT));
    }

    @Test
    public void dingulProfileKeepsTapCorrectionsOutOfSlidePenalty() {
        TouchBiasStore.DingulTouchProfile profile = TouchBiasStore.DingulTouchProfile.empty();
        for (int i = 0; i < 6; i++) {
            profile.recordInput("3145", GestureAction.TAP);
            profile.recordCorrection("3145", GestureAction.TAP, 0f, 0f);
        }

        assertEquals(0, profile.penaltyDp("3145", GestureAction.TAP));
        assertEquals(0, profile.penaltyDp("3145", GestureAction.UP));
    }

    @Test
    public void dingulProfileRoundTripsWithoutRawText() {
        TouchBiasStore.DingulTouchProfile profile = TouchBiasStore.DingulTouchProfile.empty()
                .recordInput("3163+2E", GestureAction.RIGHT)
                .recordCorrection("3163+2E", GestureAction.RIGHT, 1f, 2f);
        String encoded = profile.encode();
        TouchBiasStore.DingulTouchProfile decoded =
                TouchBiasStore.DingulTouchProfile.decode(encoded);

        assertEquals(1, decoded.inputCount("3163+2E", GestureAction.RIGHT));
        assertEquals(1, decoded.correctionCount("3163+2E", GestureAction.RIGHT));
        org.junit.Assert.assertFalse(encoded.contains("ㄱ"));
    }
}
