package com.academic.hangulgestureime;

final class GestureState {
    private GestureAction lockedAction = GestureAction.TAP;

    GestureAction update(float dx, float dy, float threshold) {
        if (lockedAction != GestureAction.TAP) {
            return lockedAction;
        }

        GestureAction action = actionFor(dx, dy, threshold);
        if (action != GestureAction.TAP) {
            lockedAction = action;
        }
        return lockedAction;
    }

    GestureAction release(float dx, float dy, float threshold) {
        if (lockedAction != GestureAction.TAP) {
            return lockedAction;
        }
        return actionFor(dx, dy, threshold);
    }

    boolean isLocked() {
        return lockedAction != GestureAction.TAP;
    }

    void reset() {
        lockedAction = GestureAction.TAP;
    }

    private GestureAction actionFor(float dx, float dy, float threshold) {
        if (Math.hypot(dx, dy) < threshold) {
            return GestureAction.TAP;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? GestureAction.LEFT : GestureAction.RIGHT;
        }
        return dy < 0 ? GestureAction.UP : GestureAction.DOWN;
    }
}
