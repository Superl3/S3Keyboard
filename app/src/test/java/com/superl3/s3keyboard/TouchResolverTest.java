package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
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
    public void actualContainingKeyWinsOverEarlierExpandedCandidate() {
        FakeTarget dotAbove = new FakeTarget("dot", 0, 0, 100, 40, false);
        FakeTarget spaceBelow = new FakeTarget("space", 0, 48, 200, 100, true);

        FakeTarget resolved = TouchResolver.resolve(
                Arrays.asList(dotAbove, spaceBelow),
                50,
                50,
                12,
                0,
                0,
                0);

        assertEquals("space", resolved.name);
    }

    @Test
    public void rawPrimaryBottomHitWinsOverYOffsetIntoPreviousRow() {
        FakeTarget dotAbove = new FakeTarget("dot", 0, 0, 200, 40, false);
        FakeTarget spaceBelow = new FakeTarget("space", 0, 44, 200, 100, true);

        FakeTarget resolved = TouchResolver.resolve(
                Arrays.asList(dotAbove, spaceBelow),
                100,
                45,
                8,
                -6,
                0,
                0);

        assertEquals("space", resolved.name);
    }

    @Test
    public void dingulSpaceTopEdgeResolvesToSpaceInsteadOfDotKeyAbove() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                320f,
                settings.measuredHeightDp(),
                1f);
        KeyboardLayoutCalculator.Slot space = findSlot(slots, KeyboardCommands.CMD_SPACE);

        SlotTarget resolved = TouchResolver.resolve(
                slotTargets(slots),
                space.right - 8f,
                space.top + 1f,
                settings.hitSlopDp,
                settings.touchYOffsetDp,
                0f,
                0f);

        assertEquals(KeyboardCommands.CMD_SPACE, resolved.slot.key.tap);
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

    private static List<SlotTarget> slotTargets(List<KeyboardLayoutCalculator.Slot> slots) {
        List<SlotTarget> targets = new ArrayList<>();
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            targets.add(new SlotTarget(slot));
        }
        return targets;
    }

    private static KeyboardLayoutCalculator.Slot findSlot(
            List<KeyboardLayoutCalculator.Slot> slots,
            String tap) {
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            if (tap.equals(slot.key.tap)) {
                return slot;
            }
        }
        throw new AssertionError("Slot not found: " + tap);
    }

    private static final class SlotTarget implements TouchResolver.Target {
        final KeyboardLayoutCalculator.Slot slot;

        SlotTarget(KeyboardLayoutCalculator.Slot slot) {
            this.slot = slot;
        }

        @Override
        public boolean contains(float x, float y) {
            return x >= slot.left && x <= slot.right && y >= slot.top && y <= slot.bottom;
        }

        @Override
        public boolean expandedContains(float x, float y, float slop) {
            return x >= slot.left - slop && x <= slot.right + slop
                    && y >= slot.top - slop && y <= slot.bottom + slop;
        }

        @Override
        public float distanceSquaredTo(float x, float y) {
            float nearestX = Math.max(slot.left, Math.min(slot.right, x));
            float nearestY = Math.max(slot.top, Math.min(slot.bottom, y));
            float dx = x - nearestX;
            float dy = y - nearestY;
            return dx * dx + dy * dy;
        }

        @Override
        public boolean isPrimaryBottomControl() {
            return slot.primaryBottomControl;
        }
    }
}
