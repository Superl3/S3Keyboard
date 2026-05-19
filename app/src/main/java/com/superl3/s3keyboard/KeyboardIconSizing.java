package com.superl3.s3keyboard;

final class KeyboardIconSizing {
    private static final float KEY_ICON_SIZE_DP = 20f;
    private static final float HINT_ICON_SIZE_DP = 14f;
    private static final float OVERLAY_ICON_SIZE_DP = 20f;

    private KeyboardIconSizing() {
    }

    static float keyIconSizePx(float density) {
        return dp(KEY_ICON_SIZE_DP, density);
    }

    static float hintIconSizePx(float density) {
        return dp(HINT_ICON_SIZE_DP, density);
    }

    static float overlayIconSizePx(float density) {
        return dp(OVERLAY_ICON_SIZE_DP, density);
    }

    private static float dp(float value, float density) {
        return value * density;
    }
}
