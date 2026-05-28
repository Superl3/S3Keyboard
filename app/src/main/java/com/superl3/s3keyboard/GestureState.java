package com.superl3.s3keyboard;

final class GestureState {
    private static final float LEFT_LOCK_FACTOR = 0.78f;
    private static final float RIGHT_LOCK_FACTOR = 0.70f;
    private static final float VERTICAL_LOCK_FACTOR = 0.78f;
    private static final float HORIZONTAL_AXIS_RATIO = 0.55f;
    private static final float VERTICAL_AXIS_RATIO = 0.65f;

    private GestureAction lockedAction = GestureAction.TAP;

    GestureAction update(float dx, float dy, float threshold) {
        return update(dx, dy, threshold, threshold, threshold, threshold, threshold);
    }

    GestureAction update(
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold) {
        return update(
                dx,
                dy,
                baseThreshold,
                upThreshold,
                downThreshold,
                leftThreshold,
                rightThreshold,
                0f);
    }

    GestureAction update(
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold,
            float axisDominanceRatio) {
        if (lockedAction != GestureAction.TAP) {
            return lockedAction;
        }

        GestureAction action = actionFor(
                dx,
                dy,
                baseThreshold,
                upThreshold,
                downThreshold,
                leftThreshold,
                rightThreshold,
                axisDominanceRatio);
        if (action != GestureAction.TAP) {
            lockedAction = action;
        }
        return lockedAction;
    }

    GestureAction release(float dx, float dy, float threshold) {
        return release(dx, dy, threshold, threshold, threshold, threshold, threshold);
    }

    GestureAction release(
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold) {
        return release(
                dx,
                dy,
                baseThreshold,
                upThreshold,
                downThreshold,
                leftThreshold,
                rightThreshold,
                0f);
    }

    GestureAction release(
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold,
            float axisDominanceRatio) {
        if (lockedAction != GestureAction.TAP) {
            return lockedAction;
        }
        return actionFor(
                dx,
                dy,
                baseThreshold,
                upThreshold,
                downThreshold,
                leftThreshold,
                rightThreshold,
                axisDominanceRatio);
    }

    boolean isLocked() {
        return lockedAction != GestureAction.TAP;
    }

    void reset() {
        lockedAction = GestureAction.TAP;
    }

    private GestureAction actionFor(float dx, float dy, float threshold) {
        return actionFor(dx, dy, threshold, threshold, threshold, threshold, threshold, 0f);
    }

    private GestureAction actionFor(
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold,
            float axisDominanceRatio) {
        GestureAction candidate;
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);
        float strictRatio = Math.max(0f, axisDominanceRatio);
        boolean horizontalDominates = strictRatio <= 0f || absDx >= absDy * strictRatio;
        boolean verticalDominates = strictRatio <= 0f || absDy >= absDx * strictRatio;
        if (horizontalDominates && absDx >= absDy * HORIZONTAL_AXIS_RATIO) {
            candidate = dx < 0 ? GestureAction.LEFT : GestureAction.RIGHT;
            float factor = candidate == GestureAction.RIGHT ? RIGHT_LOCK_FACTOR : LEFT_LOCK_FACTOR;
            float threshold = thresholdFor(
                    candidate,
                    baseThreshold,
                    upThreshold,
                    downThreshold,
                    leftThreshold,
                    rightThreshold) * factor;
            if (absDx >= threshold) {
                return candidate;
            }
        }
        if (verticalDominates && absDy >= absDx * VERTICAL_AXIS_RATIO) {
            candidate = dy < 0 ? GestureAction.UP : GestureAction.DOWN;
            float threshold = thresholdFor(
                    candidate,
                    baseThreshold,
                    upThreshold,
                    downThreshold,
                    leftThreshold,
                    rightThreshold) * VERTICAL_LOCK_FACTOR;
            if (absDy >= threshold) {
                return candidate;
            }
        }
        return GestureAction.TAP;
    }

    private float thresholdFor(
            GestureAction action,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold) {
        switch (action) {
            case UP:
                return Math.max(baseThreshold, upThreshold);
            case DOWN:
                return Math.max(baseThreshold, downThreshold);
            case LEFT:
                return Math.max(baseThreshold, leftThreshold);
            case RIGHT:
                return Math.max(baseThreshold, rightThreshold);
            default:
                return baseThreshold;
        }
    }
}
