package com.superl3.s3keyboard;

final class KeyboardEdgeGestureGuard {
    static final int DEFAULT_EDGE_GESTURE_GUARD_DP = 18;

    private KeyboardEdgeGestureGuard() {
    }

    static boolean shouldIgnoreTouch(float x, float width, float density) {
        if (width <= 0f) {
            return false;
        }
        float safeDensity = Math.max(0.1f, density);
        float guardPx = DEFAULT_EDGE_GESTURE_GUARD_DP * safeDensity;
        float boundedGuardPx = Math.min(guardPx, width / 3f);
        return x <= boundedGuardPx || x >= width - boundedGuardPx;
    }
}
