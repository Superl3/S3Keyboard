package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

public final class DingulSlideIntentResolverTest {
    private static final GestureKey UPPER_KEY =
            new GestureKey("upper", "u", "U", null, null, null, null);
    private static final GestureKey LOWER_KEY =
            new GestureKey("lower", "l", "L", null, null, null, null);
    private static final GestureKey LOWER_TAP_ONLY_KEY =
            new GestureKey("lower", "l", null, null, null, null, null);

    @Test
    public void reassignsBoundaryFlickToNearbyUpperSlideCandidate() {
        FakeTarget upper = new FakeTarget(UPPER_KEY, 0, 0, 60, 50);
        FakeTarget lower = new FakeTarget(LOWER_KEY, 0, 58, 60, 108);

        DingulSlideIntentResolver.Result<FakeTarget> result = DingulSlideIntentResolver.resolve(
                Arrays.asList(upper, lower),
                lower,
                30,
                59,
                30,
                59,
                30,
                43,
                10,
                policy(12, 30));

        assertSame(upper, result.target);
        assertEquals(GestureAction.UP, result.action);
    }

    @Test
    public void keepsCurrentKeyWhenItHasClearSlideIntent() {
        FakeTarget upper = new FakeTarget(UPPER_KEY, 0, 0, 60, 50);
        FakeTarget lower = new FakeTarget(LOWER_KEY, 0, 58, 60, 108);

        DingulSlideIntentResolver.Result<FakeTarget> result = DingulSlideIntentResolver.resolve(
                Arrays.asList(upper, lower),
                lower,
                30,
                59,
                30,
                59,
                30,
                43,
                10,
                policy(12, 12));

        assertSame(lower, result.target);
        assertEquals(GestureAction.UP, result.action);
    }

    @Test
    public void ignoresFarAwaySlideCandidates() {
        FakeTarget upper = new FakeTarget(UPPER_KEY, 0, 0, 60, 40);
        FakeTarget lower = new FakeTarget(LOWER_KEY, 0, 100, 60, 150);

        assertNull(DingulSlideIntentResolver.resolve(
                Arrays.asList(upper, lower),
                lower,
                30,
                101,
                30,
                101,
                30,
                85,
                10,
                policy(12, 30)));
    }

    @Test
    public void shadowResolverKeepsLowerConfidenceCandidateForLearning() {
        FakeTarget upper = new FakeTarget(UPPER_KEY, 0, 0, 60, 50);

        assertNull(DingulSlideIntentResolver.resolve(
                Arrays.asList(upper),
                upper,
                30,
                56,
                30,
                56,
                30,
                42,
                10,
                policy(60, 30, 12)));

        DingulSlideIntentResolver.Result<FakeTarget> shadow = DingulSlideIntentResolver.resolveShadow(
                Arrays.asList(upper),
                upper,
                30,
                56,
                30,
                56,
                30,
                42,
                10,
                policy(60, 30, 12));

        assertSame(upper, shadow.target);
        assertEquals(GestureAction.UP, shadow.action);
    }

    @Test
    public void shadowResolverUsesRelaxedThresholdForDatasetCapture() {
        FakeTarget upper = new FakeTarget(UPPER_KEY, 0, 0, 60, 50);

        assertNull(DingulSlideIntentResolver.resolve(
                Arrays.asList(upper),
                upper,
                30,
                40,
                30,
                40,
                30,
                22,
                10,
                policy(60, 60, 60, 16)));

        DingulSlideIntentResolver.Result<FakeTarget> shadow = DingulSlideIntentResolver.resolveShadow(
                Arrays.asList(upper),
                upper,
                30,
                40,
                30,
                40,
                30,
                22,
                10,
                policy(60, 60, 60, 16));

        assertSame(upper, shadow.target);
        assertEquals(GestureAction.UP, shadow.action);
    }

    private static DingulSlideIntentResolver.Policy policy(
            final float upperThreshold,
            final float lowerThreshold) {
        return policy(upperThreshold, lowerThreshold, 0);
    }

    private static DingulSlideIntentResolver.Policy policy(
            final float upperThreshold,
            final float lowerThreshold,
            final float actionThreshold) {
        return policy(upperThreshold, lowerThreshold, actionThreshold, actionThreshold);
    }

    private static DingulSlideIntentResolver.Policy policy(
            final float upperThreshold,
            final float lowerThreshold,
            final float actionThreshold,
            final float shadowActionThreshold) {
        return new DingulSlideIntentResolver.Policy() {
            @Override
            public boolean isDingulTypingKey(GestureKey key) {
                return key == UPPER_KEY || key == LOWER_KEY || key == LOWER_TAP_ONLY_KEY;
            }

            @Override
            public GestureAction actionFor(GestureKey key, float dx, float dy) {
                float threshold = actionThreshold > 0 ? actionThreshold : thresholdPx(key, GestureAction.UP);
                return new GestureState().release(
                        dx,
                        dy,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        1.15f);
            }

            @Override
            public GestureAction shadowActionFor(GestureKey key, float dx, float dy) {
                float threshold = shadowActionThreshold > 0 ? shadowActionThreshold : thresholdPx(key, GestureAction.UP);
                return new GestureState().release(
                        dx,
                        dy,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        1.15f);
            }

            @Override
            public float thresholdPx(GestureKey key, GestureAction action) {
                return key == UPPER_KEY ? upperThreshold : lowerThreshold;
            }

            @Override
            public float shadowThresholdPx(GestureKey key, GestureAction action) {
                return shadowActionThreshold > 0 ? shadowActionThreshold : thresholdPx(key, action);
            }

            @Override
            public boolean hasOutput(GestureKey key, GestureAction action) {
                if (key == LOWER_TAP_ONLY_KEY && action != GestureAction.TAP) {
                    return false;
                }
                String value = key.valueFor(action);
                return value != null && !value.isEmpty();
            }
        };
    }

    private static final class FakeTarget implements DingulSlideIntentResolver.Target {
        final GestureKey key;
        final float left;
        final float top;
        final float right;
        final float bottom;

        FakeTarget(GestureKey key, float left, float top, float right, float bottom) {
            this.key = key;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        public GestureKey key() {
            return key;
        }

        @Override
        public boolean contains(float x, float y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }

        @Override
        public boolean expandedContains(float x, float y, float slop) {
            return x >= left - slop && x <= right + slop && y >= top - slop && y <= bottom + slop;
        }

        @Override
        public float distanceSquaredTo(float x, float y) {
            float nearestX = Math.max(left, Math.min(right, x));
            float nearestY = Math.max(top, Math.min(bottom, y));
            float dx = x - nearestX;
            float dy = y - nearestY;
            return dx * dx + dy * dy;
        }

        @Override
        public float width() {
            return right - left;
        }

        @Override
        public float height() {
            return bottom - top;
        }

        @Override
        public float centerX() {
            return (left + right) / 2f;
        }

        @Override
        public float centerY() {
            return (top + bottom) / 2f;
        }
    }
}
