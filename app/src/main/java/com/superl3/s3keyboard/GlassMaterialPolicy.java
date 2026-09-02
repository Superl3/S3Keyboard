package com.superl3.s3keyboard;

/** Pure source-free values for the lightweight Frosted material. */
final class GlassMaterialPolicy {
    private GlassMaterialPolicy() {
    }

    static int panelTintAlpha(int tintRetentionPercent) {
        float retention = clamp01(tintRetentionPercent / 100f);
        return Math.round(255f * (0.74f + 0.12f * retention));
    }





    static int keyBorderAlpha(int configuredPercent) {
        return Math.round(16f + 0.42f * clamp(configuredPercent, 0, 100));
    }

    static int panelBorderAlpha(int configuredPercent) {
        return Math.round(10f + 0.30f * clamp(configuredPercent, 0, 100));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
