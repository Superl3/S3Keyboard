package com.superl3.s3keyboard;

/** Pure material values shared by the live Glass renderer and its source-free fallback. */
final class GlassMaterialPolicy {
    private GlassMaterialPolicy() {
    }

    static int panelTintAlpha(int tintRetentionPercent) {
        float retention = clamp01(tintRetentionPercent / 100f);
        return Math.round(255f * (0.74f + 0.12f * retention));
    }

    static int fallbackKeyTintAlpha(int tintRetentionPercent, float pressProgress) {
        float retention = clamp01(tintRetentionPercent / 100f);
        // Product Frosted keeps the key body almost opaque; the host is perceived through the
        // blurred panel around the keys rather than through a noisy translucent key face.
        float opacity = 0.84f + 0.10f * retention + 0.02f * clamp01(pressProgress);
        return Math.round(255f * Math.min(0.95f, opacity));
    }

    static float keyCenterSourceMix(int tintRetentionPercent) {
        float retention = clamp01(tintRetentionPercent / 100f);
        return 0.14f - 0.08f * retention;
    }

    static float keyEdgeSourceMix(int tintRetentionPercent) {
        float retention = clamp01(tintRetentionPercent / 100f);
        return 0.31f - 0.11f * retention;
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
