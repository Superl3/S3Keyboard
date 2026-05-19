package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class TouchBiasStoreTest {
    @Test
    public void biasRecordsImmediateDeleteConservatively() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none()
                .recordImmediateDelete(20f, -10f);

        assertEquals(-1f, bias.xDp, 0.001f);
        assertEquals(0.5f, bias.yDp, 0.001f);
        assertEquals(1, bias.samples);
    }

    @Test
    public void biasIsClamped() {
        TouchBiasStore.Bias bias = TouchBiasStore.Bias.none();
        for (int i = 0; i < 200; i++) {
            bias = bias.recordImmediateDelete(50f, 50f);
        }

        assertEquals(-TouchBiasStore.MAX_BIAS_DP, bias.xDp, 0.001f);
        assertEquals(-TouchBiasStore.MAX_BIAS_DP, bias.yDp, 0.001f);
    }

    @Test
    public void biasRoundTripsThroughEncodedStats() {
        TouchBiasStore.Bias bias = new TouchBiasStore.Bias(1.25f, -2.5f, 3);
        TouchBiasStore.Bias decoded = TouchBiasStore.Bias.decode(bias.encode());

        assertEquals(1.25f, decoded.xDp, 0.001f);
        assertEquals(-2.5f, decoded.yDp, 0.001f);
        assertEquals(3, decoded.samples);
    }
}
