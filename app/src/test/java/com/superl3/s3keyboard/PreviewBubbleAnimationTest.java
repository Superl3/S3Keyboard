package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PreviewBubbleAnimationTest {
    private static final long START_MS = 1000L;
    private static final long RELEASE_MS = 1100L;
    private static final float EPSILON = 0.001f;

    @Test
    public void activeBubbleUsesOnlyASmallUniformScaleIn() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertTrue(bubble.scale(START_MS, true, 1f) < 1f);
        assertEquals(bubble.scale(START_MS, true, 1f), bubble.scaleX(START_MS, true, 1f), EPSILON);
        assertEquals(bubble.scale(START_MS, true, 1f), bubble.scaleY(START_MS, true, 1f), EPSILON);
        assertEquals(1f, bubble.scale(1064L, true, 1f), EPSILON);
        assertEquals(1f, bubble.textScale(START_MS, true, 1f), EPSILON);
        assertEquals(1f, bubble.alpha(5000L, true, 1f), EPSILON);
        assertFalse(bubble.expired(5000L, true, 1f));
    }

    @Test
    public void durationScaleSlowsTheScaleInWithoutChangingItsShape() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertTrue(bubble.scale(1032L, true, 2f) < bubble.scale(1032L, true, 1f));
    }

    @Test
    public void releasedBubbleFadesOnceAndExpires() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        bubble.markReleased(RELEASE_MS);

        assertEquals(1f, bubble.alpha(RELEASE_MS, true, 1f), EPSILON);
        assertTrue(bubble.alpha(1146L, true, 1f) < 1f);
        assertTrue(bubble.alpha(1180L, true, 1f) < bubble.alpha(1146L, true, 1f));
        assertFalse(bubble.expired(1191L, true, 1f));
        assertEquals(0f, bubble.alpha(1192L, true, 1f), EPSILON);
        assertTrue(bubble.expired(1192L, true, 1f));
    }

    @Test
    public void labelChangeRestartsScaleInWithoutMovingTheAnchor() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        float settled = bubble.scale(1300L, true, 1f);

        bubble.update("B", 0xFF111111, 0xFFFFFFFF, 0xFF222222, 1, 1300L, false);

        assertTrue(bubble.scale(1300L, true, 1f) < settled);
        assertEquals(50f, bubble.anchorCenterX, EPSILON);
    }

    @Test
    public void supersededBubbleUsesItsOwnShortFadeClock() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        bubble.markReleased(RELEASE_MS);
        bubble.markSuperseded(1120L);

        assertTrue(bubble.alpha(1140L, true, 1f) < bubble.alpha(1120L, true, 1f));
        assertFalse(bubble.expired(1161L, true, 1f));
        assertTrue(bubble.expired(1162L, true, 1f));
    }

    @Test
    public void simultaneousBubblesKeepIndependentReleaseClocks() {
        PreviewBubbleAnimation first = bubbleAt(START_MS);
        PreviewBubbleAnimation second = bubbleAt(START_MS + 40L);
        first.markReleased(RELEASE_MS);
        second.markReleased(RELEASE_MS + 40L);

        assertTrue(first.expired(1192L, true, 1f));
        assertFalse(second.expired(1192L, true, 1f));
        assertTrue(second.alpha(1192L, true, 1f) > 0f);
    }

    @Test
    public void disabledMotionKeepsThePreviewStable() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        bubble.markReleased(RELEASE_MS);

        assertEquals(1f, bubble.scale(START_MS, false, 1f), EPSILON);
        assertEquals(1f, bubble.alpha(9999L, false, 1f), EPSILON);
        assertFalse(bubble.expired(9999L, false, 1f));
    }

    private static PreviewBubbleAnimation bubbleAt(long startTimeMs) {
        return new PreviewBubbleAnimation(
                "A",
                50f,
                10f,
                0xFF111111,
                0xFFFFFFFF,
                0xFF222222,
                1,
                startTimeMs,
                false);
    }
}
