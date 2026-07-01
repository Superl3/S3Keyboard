package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PreviewBubbleLayoutTest {
    @Test
    public void widthUsesTextPaddingAndClampsToBounds() {
        assertEquals(48, PreviewBubbleLayout.widthPx(4f, 48, 92, 28));
        assertEquals(68, PreviewBubbleLayout.widthPx(40f, 48, 92, 28));
        assertEquals(92, PreviewBubbleLayout.widthPx(120f, 48, 92, 28));
    }

    @Test
    public void xKeepsBubbleWithinHorizontalInsets() {
        assertEquals(2, PreviewBubbleLayout.xPx(10f, 48, 240, 2));
        assertEquals(96, PreviewBubbleLayout.xPx(120f, 48, 240, 2));
        assertEquals(190, PreviewBubbleLayout.xPx(235f, 48, 240, 2));
    }

    @Test
    public void yPlacesBubbleAboveAnchorWithGapAndLift() {
        assertEquals(26, PreviewBubbleLayout.yPx(100f, 61, 3, 10));
    }

    @Test
    public void liftRisesThenSettles() {
        assertEquals(0, PreviewBubbleLayout.liftPx(false, 1f, 14, 8));
        assertEquals(0, PreviewBubbleLayout.liftPx(true, 0f, 14, 8));
        assertEquals(14, PreviewBubbleLayout.liftPx(true, 0.34f, 14, 8));
        int descending = PreviewBubbleLayout.liftPx(true, 0.50f, 14, 8);
        assertTrue(descending > 8);
        assertTrue(descending < 14);
        assertEquals(8, PreviewBubbleLayout.liftPx(true, 0.62f, 14, 8));
    }

    @Test
    public void cornerRadiusFollowsKeyRadiusWithinStyleLimits() {
        assertEquals(2, PreviewBubbleLayout.cornerRadiusPx(0, true, 2, 6, 18));
        assertEquals(6, PreviewBubbleLayout.cornerRadiusPx(12, true, 2, 6, 18));
        assertEquals(12, PreviewBubbleLayout.cornerRadiusPx(12, false, 2, 6, 18));
        assertEquals(18, PreviewBubbleLayout.cornerRadiusPx(24, false, 2, 6, 18));
    }
}
