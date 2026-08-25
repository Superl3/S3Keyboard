package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImeWindowBlurControllerTest {
    @Test
    public void radiusUsesDensityAndRejectsInvalidValues() {
        assertEquals(30, ImeWindowBlurController.radiusPx(12, 2.5f));
        assertEquals(1, ImeWindowBlurController.radiusPx(1, 0.1f));
        assertEquals(0, ImeWindowBlurController.radiusPx(0, 3f));
        assertEquals(0, ImeWindowBlurController.radiusPx(12, 0f));
    }
}
