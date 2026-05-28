package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

public final class DingulTouchPlanTest {
    private static final float WIDTH_PX = 1080f;
    private static final float DENSITY = 2.625f;

    @Test
    public void selectedDingulKeysHaveDeterministicInputRanges() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                WIDTH_PX,
                settings.measuredHeightDp() * DENSITY,
                DENSITY);
        List<SlotTarget> targets = slotTargets(slots);

        for (Case testCase : cases()) {
            KeyboardLayoutCalculator.Slot slot = findSlotByLabel(slots, testCase.keyCodePoints);
            TouchPoint touchPoint = touchPoint(settings, slot, testCase.action);

            assertSame(
                    testCase.name(),
                    slot,
                    TouchResolver.resolve(
                            targets,
                            touchPoint.downX,
                            touchPoint.downY,
                            settings.hitSlopDp * DENSITY,
                            settings.touchYOffsetDp * DENSITY,
                            0f,
                            0f).slot);
            assertEquals(testCase.name(), testCase.expectedValueCodePoints, codePoints(slot.key.valueFor(testCase.action)));
            assertEquals(testCase.name(), testCase.action, releasedAction(settings, slot.key, touchPoint));
        }
    }

    @Test
    public void selectedDingulKeyInteriorRangesResolveToTheirOwnKeys() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                WIDTH_PX,
                settings.measuredHeightDp() * DENSITY,
                DENSITY);
        List<SlotTarget> targets = slotTargets(slots);

        for (Case testCase : cases()) {
            KeyboardLayoutCalculator.Slot slot = findSlotByLabel(slots, testCase.keyCodePoints);
            for (float xRatio : new float[] {0.25f, 0.5f, 0.75f}) {
                for (float yRatio : new float[] {0.25f, 0.5f, 0.75f}) {
                    float rawX = interpolate(slot.left, slot.right, xRatio);
                    float rawY = interpolate(slot.top, slot.bottom, yRatio)
                            - settings.touchYOffsetDp * DENSITY;

                    SlotTarget resolved = TouchResolver.resolve(
                            targets,
                            rawX,
                            rawY,
                            settings.hitSlopDp * DENSITY,
                            settings.touchYOffsetDp * DENSITY,
                            0f,
                            0f);

                    assertSame(testCase.name() + " @ " + xRatio + "," + yRatio, slot, resolved.slot);
                }
            }
        }
    }

    private static GestureAction releasedAction(
            KeyboardSettings settings,
            GestureKey key,
            TouchPoint touchPoint) {
        GestureState state = new GestureState();
        return state.release(
                touchPoint.upX - touchPoint.downX,
                touchPoint.upY - touchPoint.downY,
                GestureThresholdPolicy.baseThresholdDp(settings, key) * DENSITY,
                GestureThresholdPolicy.thresholdDp(settings, null, key, GestureAction.UP) * DENSITY,
                GestureThresholdPolicy.thresholdDp(settings, null, key, GestureAction.DOWN) * DENSITY,
                GestureThresholdPolicy.thresholdDp(settings, null, key, GestureAction.LEFT) * DENSITY,
                GestureThresholdPolicy.thresholdDp(settings, null, key, GestureAction.RIGHT) * DENSITY);
    }

    private static TouchPoint touchPoint(
            KeyboardSettings settings,
            KeyboardLayoutCalculator.Slot slot,
            GestureAction action) {
        float downX = (slot.left + slot.right) / 2f;
        float downY = (slot.top + slot.bottom) / 2f - settings.touchYOffsetDp * DENSITY;
        float upX = downX;
        float upY = downY;
        if (action != GestureAction.TAP) {
            float distance = Math.max(
                    18f * DENSITY,
                    GestureThresholdPolicy.thresholdDp(settings, null, slot.key, action) * DENSITY * 1.35f);
            switch (action) {
                case UP:
                    upY -= distance;
                    break;
                case DOWN:
                    upY += distance;
                    break;
                case LEFT:
                    upX -= distance;
                    break;
                case RIGHT:
                    upX += distance;
                    break;
                default:
                    break;
            }
        }
        return new TouchPoint(downX, downY, upX, upY);
    }

    private static KeyboardLayoutCalculator.Slot findSlotByLabel(
            List<KeyboardLayoutCalculator.Slot> slots,
            String keyCodePoints) {
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            if (keyCodePoints.equals(codePoints(slot.key.label))) {
                return slot;
            }
        }
        throw new AssertionError("Dingul key not found: " + keyCodePoints);
    }

    private static Case[] cases() {
        return new Case[] {
                new Case("3131", GestureAction.TAP, "3131"),
                new Case("3131", GestureAction.UP, "3132"),
                new Case("3131", GestureAction.DOWN, "23"),
                new Case("3131", GestureAction.LEFT, "314B"),
                new Case("3131", GestureAction.RIGHT, "314B"),
                new Case("3145", GestureAction.TAP, "3145"),
                new Case("3145", GestureAction.UP, "3146"),
                new Case("3163+2E", GestureAction.UP, "3157"),
                new Case("3163+2E", GestureAction.DOWN, "315C"),
                new Case("3163+2E", GestureAction.LEFT, "3153"),
                new Case("3163+2E", GestureAction.RIGHT, "314F"),
                new Case("3161+3150", GestureAction.UP, "3159"),
                new Case("3161+3150", GestureAction.DOWN, "315E"),
                new Case("3161+3150", GestureAction.LEFT, "3154"),
                new Case("3161+3150", GestureAction.RIGHT, "3150")
        };
    }

    private static float interpolate(float start, float end, float ratio) {
        return start + (end - start) * ratio;
    }

    private static String codePoints(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (builder.length() > 0) {
                builder.append('+');
            }
            builder.append(Integer.toHexString(codePoint).toUpperCase(Locale.US));
            i += Character.charCount(codePoint);
        }
        return builder.toString();
    }

    private static List<SlotTarget> slotTargets(List<KeyboardLayoutCalculator.Slot> slots) {
        List<SlotTarget> targets = new ArrayList<>();
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            targets.add(new SlotTarget(slot));
        }
        return targets;
    }

    private static final class Case {
        final String keyCodePoints;
        final GestureAction action;
        final String expectedValueCodePoints;

        Case(String keyCodePoints, GestureAction action, String expectedValueCodePoints) {
            this.keyCodePoints = keyCodePoints;
            this.action = action;
            this.expectedValueCodePoints = expectedValueCodePoints;
        }

        String name() {
            return keyCodePoints + " " + action.name();
        }
    }

    private static final class TouchPoint {
        final float downX;
        final float downY;
        final float upX;
        final float upY;

        TouchPoint(float downX, float downY, float upX, float upY) {
            this.downX = downX;
            this.downY = downY;
            this.upX = upX;
            this.upY = upY;
        }
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
        public boolean coreContains(float x, float y, float inset) {
            float insetX = Math.min(Math.max(0f, inset), (slot.right - slot.left) * 0.32f);
            float insetY = Math.min(Math.max(0f, inset), (slot.bottom - slot.top) * 0.32f);
            return x >= slot.left + insetX
                    && x <= slot.right - insetX
                    && y >= slot.top + insetY
                    && y <= slot.bottom - insetY;
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
