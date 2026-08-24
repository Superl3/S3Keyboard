package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class PreviewOverlaySpecTest {
    @Test
    public void setReusesInstanceAndClampsAnimationValues() {
        PreviewOverlaySpec spec = new PreviewOverlaySpec();

        PreviewOverlaySpec result = spec.set(
                "A",
                1,
                2,
                30,
                40,
                18f,
                0xFF111111,
                0xFFEEEEEE,
                0xFF222222,
                2,
                6,
                false,
                1f,
                1f,
                1f,
                2f,
                -1f,
                3f);

        assertSame(spec, result);
        assertEquals("A", spec.label);
        assertEquals(1.18f, spec.textScale, 0.0001f);
        assertEquals(0f, spec.commitGlowAlpha, 0.0001f);
        assertEquals(1f, spec.inputImpactAlpha, 0.0001f);
    }
}
