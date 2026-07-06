package com.superl3.s3keyboard;

final class KeyboardColorMath {
    private KeyboardColorMath() {
    }

    static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    static int perceivedLuminance(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    static int contrastTextColor(int backgroundColor, int lightBackgroundThreshold) {
        return perceivedLuminance(backgroundColor) > lightBackgroundThreshold
                ? 0xFF111827
                : 0xFFFFFFFF;
    }
}
