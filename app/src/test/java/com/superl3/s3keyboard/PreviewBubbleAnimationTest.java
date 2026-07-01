package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PreviewBubbleAnimationTest {
    @Test
    public void unreleasedBubbleStaysVisibleAndMovesTowardFinalState() {
        PreviewBubbleAnimation bubble = bubbleAt(1000L);

        assertTrue(bubble.scale(1000L, true, 1f) < 1f);
        assertEquals(0f, bubble.motionProgress(1000L, true, 1f), 0.001f);
        assertEquals(1f, bubble.alpha(1000L, true, 1f), 0.001f);

        assertTrue(bubble.scale(1060L, true, 1f) > bubble.scale(1000L, true, 1f));
        assertTrue(bubble.motionProgress(1180L, true, 1f) > 0f);
        assertEquals(1f, bubble.alpha(2000L, true, 1f), 0.001f);
        assertFalse(bubble.expired(5000L, true, 1f));
    }

    @Test
    public void releasedBubbleHoldsThenFadesAndExpires() {
        PreviewBubbleAnimation bubble = bubbleAt(1000L);
        bubble.markReleased(1100L);

        assertEquals(1f, bubble.alpha(1100L, true, 1f), 0.001f);
        assertEquals(1f, bubble.alpha(1250L, true, 1f), 0.001f);
        assertTrue(bubble.alpha(1390L, true, 1f) < 1f);
        assertFalse(bubble.expired(1619L, true, 1f));
        assertTrue(bubble.expired(1620L, true, 1f));
    }

    @Test
    public void labelChangeRestartsPopPulseWithoutMovingAnchor() {
        PreviewBubbleAnimation bubble = bubbleAt(1000L);

        float settled = bubble.scale(1300L, true, 1f);
        bubble.update(
                "A",
                0xFF111111,
                0xFFFFFFFF,
                0xFF222222,
                1,
                1300L,
                false);

        assertTrue(bubble.scale(1300L, true, 1f) < settled);
        assertEquals(50f, bubble.anchorCenterX, 0.001f);
    }

    @Test
    public void disabledMotionKeepsBubbleStableUntilExplicitlyReleased() {
        PreviewBubbleAnimation bubble = bubbleAt(1000L);
        bubble.markReleased(1100L);

        assertEquals(1f, bubble.scale(1000L, false, 1f), 0.001f);
        assertEquals(1f, bubble.motionProgress(1000L, false, 1f), 0.001f);
        assertEquals(1f, bubble.alpha(9999L, false, 1f), 0.001f);
        assertFalse(bubble.expired(9999L, false, 1f));
    }

    private static PreviewBubbleAnimation bubbleAt(long startTimeMs) {
        return new PreviewBubbleAnimation(
                "가",
                1L,
                50f,
                10f,
                60f,
                0xFF111111,
                0xFFFFFFFF,
                0xFF222222,
                1,
                startTimeMs,
                false);
    }
}
