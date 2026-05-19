package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class TouchResolverTest {
    @Test
    public void resolvesGapInsideHitSlopToNearestKey() {
        FakeTarget left = new FakeTarget("left", 0, 0, 50, 50, false);
        FakeTarget right = new FakeTarget("right", 60, 0, 110, 50, false);

        FakeTarget resolved = TouchResolver.resolve(Arrays.asList(left, right), 55, 25, 8, 0, 0, 0);

        assertEquals("left", resolved.name);
    }

    @Test
    public void choosesNearestCandidateInsteadOfFirstCandidate() {
        FakeTarget left = new FakeTarget("left", 0, 0, 50, 50, false);
        FakeTarget right = new FakeTarget("right", 60, 0, 110, 50, false);

        FakeTarget resolved = TouchResolver.resolve(Arrays.asList(left, right), 58, 25, 10, 0, 0, 0);

        assertEquals("right", resolved.name);
    }

    @Test
    public void ignoresTouchOutsideHitSlop() {
        FakeTarget key = new FakeTarget("key", 0, 0, 50, 50, false);

        assertNull(TouchResolver.resolve(Arrays.asList(key), 80, 80, 8, 0, 0, 0));
    }

    @Test
    public void primaryBottomControlGetsLargerEffectiveSlop() {
        FakeTarget normal = new FakeTarget("normal", 0, 0, 50, 50, false);
        FakeTarget primary = new FakeTarget("primary", 80, 0, 130, 50, true);
        List<FakeTarget> targets = Arrays.asList(normal, primary);

        FakeTarget resolved = TouchResolver.resolve(targets, 136, 25, 8, 0, 0, 0);

        assertEquals("primary", resolved.name);
    }

    @Test
    public void appliesTouchYOffset() {
        FakeTarget key = new FakeTarget("key", 0, 0, 50, 50, false);

        assertEquals(key, TouchResolver.resolve(Arrays.asList(key), 25, 54, 0, -4, 0, 0));
    }

    private static final class FakeTarget implements TouchResolver.Target {
        final String name;
        final float left;
        final float top;
        final float right;
        final float bottom;
        final boolean primary;

        FakeTarget(String name, float left, float top, float right, float bottom, boolean primary) {
            this.name = name;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.primary = primary;
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
        public boolean isPrimaryBottomControl() {
            return primary;
        }
    }
}
