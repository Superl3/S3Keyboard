package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PreviewBubbleAnimationTest {
    private static final long START_MS = 1000L;
    private static final long RELEASE_MS = 1100L;
    private static final float MOTION_SCALE = 1f;
    private static final float EPSILON = 0.001f;

    @Test
    public void unreleasedBubbleStaysVisibleAndMovesTowardFinalState() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertTrue(bubble.scale(START_MS, true, MOTION_SCALE) < 1f);
        assertEquals(0f, bubble.motionProgress(START_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.alpha(START_MS, true, MOTION_SCALE), EPSILON);

        assertEquals(1f, bubble.scale(1070L, true, 1f), 0.001f);
        assertEquals(1f, bubble.motionProgress(1002L, true, 1f), 0.001f);
        assertEquals(1f, bubble.alpha(2000L, true, 1f), 0.001f);
        assertEquals(0f, bubble.commitGlowAlpha(START_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.commitGlowAlpha(1240L, true, 1f), EPSILON);
        assertFalse(bubble.expired(5000L, true, 1f));
    }

    @Test
    public void activeBubbleUsesStableKeycapScale() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertEquals(
                bubble.scale(START_MS, true, MOTION_SCALE),
                bubble.scaleX(START_MS, true, MOTION_SCALE),
                EPSILON);
        assertEquals(
                bubble.scale(START_MS, true, MOTION_SCALE),
                bubble.scaleY(START_MS, true, MOTION_SCALE),
                EPSILON);
        assertEquals(
                bubble.scale(1240L, true, 1f),
                bubble.scaleX(1240L, true, 1f),
                EPSILON);
        assertEquals(
                bubble.scale(1240L, true, 1f),
                bubble.scaleY(1240L, true, 1f),
                EPSILON);
    }

    @Test
    public void activeBubbleTextStaysStable() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertEquals(1f, bubble.textScale(START_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.textScale(1060L, true, 1f), EPSILON);
        assertEquals(1f, bubble.textScale(1240L, true, 1f), EPSILON);
    }

    @Test
    public void activeBubbleDoesNotAddExtraGlowOrPressure() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertEquals(0f, bubble.inputImpactAlpha(START_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(1080L, true, 1f), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(1240L, true, 1f), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(1300L, true, 1f), EPSILON);
    }

    @Test
    public void durationScaleSlowsSharedProgressBasedMotion() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertTrue(bubble.scale(1032L, true, 2f)
                < bubble.scale(1032L, true, 1f));
    }

    @Test
    public void releasedBubbleHoldsThenFadesAndExpires() {
        PreviewBubbleAnimation bubble = releasedBubble();

        assertEquals(1f, bubble.alpha(RELEASE_MS, true, MOTION_SCALE), EPSILON);
        assertTrue(bubble.alpha(1146L, true, 1f) < 1f);
        assertTrue(bubble.alpha(1180L, true, 1f) < bubble.alpha(1146L, true, 1f));
        assertFalse(bubble.expired(1191L, true, 1f));
        assertEquals(0f, bubble.alpha(1192L, true, 1f), 0.001f);
        assertTrue(bubble.expired(1192L, true, 1f));
        assertEquals(0f, bubble.commitGlowAlpha(RELEASE_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.releaseFloatProgress(1320L, true, 1f), EPSILON);
    }

    @Test
    public void releasedBubbleDoesNotAddTextConfirmationPulse() {
        PreviewBubbleAnimation bubble = releasedBubble();

        assertEquals(1f, bubble.textScale(1180L, true, 1f), EPSILON);
        assertEquals(1f, bubble.textScale(2000L, true, 1f), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(RELEASE_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(1180L, true, 1f), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(2000L, true, 1f), EPSILON);
    }

    @Test
    public void releaseDoesNotAddCommitImpulse() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        float activeScale = bubble.scale(RELEASE_MS, true, MOTION_SCALE);

        bubble.markReleased(RELEASE_MS);

        assertEquals(activeScale, bubble.scale(RELEASE_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.scale(1300L, true, 1f), EPSILON);
    }

    @Test
    public void releaseDoesNotAddCommitLiftPulse() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        assertEquals(0f, bubble.commitLiftProgress(RELEASE_MS - 1, true, MOTION_SCALE), EPSILON);
        bubble.markReleased(RELEASE_MS);

        assertEquals(0f, bubble.commitLiftProgress(RELEASE_MS, true, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.commitLiftProgress(1260L, true, 1f), EPSILON);
        assertEquals(0f, bubble.commitLiftProgress(1400L, true, 1f), EPSILON);
        assertEquals(0f, bubble.commitLiftProgress(1700L, true, 1f), EPSILON);
    }

    @Test
    public void labelChangeRestartsPopPulseWithoutMovingAnchor() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);

        float settled = bubble.scale(1300L, true, 1f);
        bubble.update(
                "B",
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
    public void supersededReleasedBubbleFadesBeforeFullReleaseLifetime() {
        PreviewBubbleAnimation bubble = releasedBubble();
        bubble.markSuperseded(1120L);

        assertTrue(bubble.alpha(1140L, true, 1f) < bubble.alpha(1120L, true, 1f));
        assertFalse(bubble.expired(1161L, true, 1f));
        assertTrue(bubble.expired(1162L, true, 1f));
    }

    @Test
    public void disabledMotionKeepsBubbleStableUntilExplicitlyReleased() {
        PreviewBubbleAnimation bubble = releasedBubble();

        assertEquals(1f, bubble.scale(START_MS, false, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.scaleX(START_MS, false, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.scaleY(START_MS, false, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.textScale(START_MS, false, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.motionProgress(START_MS, false, MOTION_SCALE), EPSILON);
        assertEquals(1f, bubble.alpha(9999L, false, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.commitGlowAlpha(9999L, false, MOTION_SCALE), EPSILON);
        assertEquals(0f, bubble.inputImpactAlpha(9999L, false, MOTION_SCALE), EPSILON);
        assertFalse(bubble.expired(9999L, false, 1f));
    }

    private static PreviewBubbleAnimation releasedBubble() {
        PreviewBubbleAnimation bubble = bubbleAt(START_MS);
        bubble.markReleased(RELEASE_MS);
        return bubble;
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
