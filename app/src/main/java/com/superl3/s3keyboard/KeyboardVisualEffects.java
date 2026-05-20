package com.superl3.s3keyboard;

final class KeyboardVisualEffects {
    static final KeyboardVisualEffects DEFAULT = new KeyboardVisualEffects(
            false,
            0,
            false,
            0,
            true);

    final boolean blurEnabled;
    final int blurRadiusDp;
    final boolean metallicEnabled;
    final int metallicStrengthPercent;
    final boolean angularPreviewBubble;

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble) {
        this.blurEnabled = blurEnabled;
        this.blurRadiusDp = clamp(blurRadiusDp, 0, 32);
        this.metallicEnabled = metallicEnabled;
        this.metallicStrengthPercent = clamp(metallicStrengthPercent, 0, 100);
        this.angularPreviewBubble = angularPreviewBubble;
    }

    boolean hasExportableEffects() {
        return blurEnabled
                || blurRadiusDp > 0
                || metallicEnabled
                || metallicStrengthPercent > 0
                || angularPreviewBubble != DEFAULT.angularPreviewBubble;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
