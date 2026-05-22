package com.superl3.s3keyboard;

final class KeyboardVisualEffects {
    static final KeyboardVisualEffects DEFAULT = new KeyboardVisualEffects(
            false,
            0,
            false,
            0,
            true,
            true,
            22);

    final boolean blurEnabled;
    final int blurRadiusDp;
    final boolean metallicEnabled;
    final int metallicStrengthPercent;
    final boolean angularPreviewBubble;
    final boolean keyFaceGradientEnabled;
    final int keyFaceGradientStrengthPercent;

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble) {
        this(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                DEFAULT.keyFaceGradientEnabled,
                DEFAULT.keyFaceGradientStrengthPercent);
    }

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble,
            boolean keyFaceGradientEnabled,
            int keyFaceGradientStrengthPercent) {
        this.blurEnabled = blurEnabled;
        this.blurRadiusDp = clamp(blurRadiusDp, 0, 32);
        this.metallicEnabled = metallicEnabled;
        this.metallicStrengthPercent = clamp(metallicStrengthPercent, 0, 100);
        this.angularPreviewBubble = angularPreviewBubble;
        this.keyFaceGradientEnabled = keyFaceGradientEnabled;
        this.keyFaceGradientStrengthPercent = clamp(keyFaceGradientStrengthPercent, 0, 100);
    }

    boolean hasExportableEffects() {
        return blurEnabled
                || blurRadiusDp > 0
                || metallicEnabled
                || metallicStrengthPercent > 0
                || angularPreviewBubble != DEFAULT.angularPreviewBubble
                || keyFaceGradientEnabled != DEFAULT.keyFaceGradientEnabled
                || keyFaceGradientStrengthPercent != DEFAULT.keyFaceGradientStrengthPercent;
    }

    KeyboardVisualEffects withKeyFaceGradient(boolean enabled, int strengthPercent) {
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                enabled,
                strengthPercent);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
