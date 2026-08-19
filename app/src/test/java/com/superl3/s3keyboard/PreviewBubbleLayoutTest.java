package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void yPlacesBubbleDirectlyAboveAnchorWithGap() {
        assertEquals(36, PreviewBubbleLayout.yPx(100f, 61, 3));
    }

    @Test
    public void cornerRadiusFollowsKeyRadiusWithinStyleLimits() {
        assertEquals(3, PreviewBubbleLayout.cornerRadiusPx(0, true, 3, 10, 22));
        assertEquals(10, PreviewBubbleLayout.cornerRadiusPx(12, true, 3, 10, 22));
        assertEquals(12, PreviewBubbleLayout.cornerRadiusPx(12, false, 3, 10, 22));
        assertEquals(22, PreviewBubbleLayout.cornerRadiusPx(24, false, 3, 10, 22));
    }

    @Test
    public void nearAnchorUsesKeyRelativeThresholdForReleasedBubbleDedupe() {
        assertTrue(PreviewBubbleLayout.nearAnchor(
                118f,
                84f,
                100f,
                80f,
                48f,
                42f,
                32));
        assertTrue(PreviewBubbleLayout.nearAnchor(
                31f,
                80f,
                100f,
                80f,
                56f,
                42f,
                32));
        assertFalse(PreviewBubbleLayout.nearAnchor(
                29f,
                80f,
                100f,
                80f,
                56f,
                42f,
                32));
        assertFalse(PreviewBubbleLayout.nearAnchor(
                100f,
                26f,
                100f,
                80f,
                48f,
                42f,
                32));
    }
}
