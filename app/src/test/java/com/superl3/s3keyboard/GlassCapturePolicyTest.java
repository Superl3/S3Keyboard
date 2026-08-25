package com.superl3.s3keyboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class GlassCapturePolicyTest {
    @Test
    public void captureRequiresActiveConsumerWindowAndIdleRequest() {
        assertFalse(GlassCapturePolicy.shouldCapture(false, 7, false, 1_000, 0));
        assertFalse(GlassCapturePolicy.shouldCapture(true, -1, false, 1_000, 0));
        assertFalse(GlassCapturePolicy.shouldCapture(true, 7, true, 1_000, 0));
        assertTrue(GlassCapturePolicy.shouldCapture(true, 7, false, 1_000, 0));
    }

    @Test
    public void captureIsRateLimited() {
        long last = 1_000L;
        assertFalse(GlassCapturePolicy.shouldCapture(
                true,
                7,
                false,
                last + GlassCapturePolicy.MIN_CAPTURE_INTERVAL_MS - 1,
                last));
        assertTrue(GlassCapturePolicy.shouldCapture(
                true,
                7,
                false,
                last + GlassCapturePolicy.MIN_CAPTURE_INTERVAL_MS,
                last));
    }

    @Test
    public void fullHdSourceIsReducedToConfiguredPixelBudget() {
        float scale = GlassCapturePolicy.downscaleFor(1080, 2400);
        int width = Math.round(1080 * scale);
        int height = Math.round(2400 * scale);

        assertTrue(scale > 0f && scale < 1f);
        assertTrue((long) width * height <= GlassCapturePolicy.MAX_SOURCE_PIXELS + 2_000L);
        assertEquals(1f, GlassCapturePolicy.downscaleFor(300, 300), 0f);
    }
}
