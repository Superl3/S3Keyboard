package com.superl3.s3keyboard;

final class KeyboardVisualEffects {
    static final KeyboardVisualEffects DEFAULT = new KeyboardVisualEffects(
            false,
            0,
            false,
            0,
            true,
            true,
            22,
            false,
            0xFFEBEBEB,
            0xFFEBEBEB);

    final boolean blurEnabled;
    final int blurRadiusDp;
    final boolean metallicEnabled;
    final int metallicStrengthPercent;
    final boolean angularPreviewBubble;
    final boolean keyFaceGradientEnabled;
    final int keyFaceGradientStrengthPercent;
    final boolean panelGradientEnabled;
    final int panelGradientStartColor;
    final int panelGradientEndColor;

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
                DEFAULT.keyFaceGradientStrengthPercent,
                DEFAULT.panelGradientEnabled,
                DEFAULT.panelGradientStartColor,
                DEFAULT.panelGradientEndColor);
    }

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble,
            boolean keyFaceGradientEnabled,
            int keyFaceGradientStrengthPercent) {
        this(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                DEFAULT.panelGradientEnabled,
                DEFAULT.panelGradientStartColor,
                DEFAULT.panelGradientEndColor);
    }

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble,
            boolean keyFaceGradientEnabled,
            int keyFaceGradientStrengthPercent,
            boolean panelGradientEnabled,
            int panelGradientStartColor,
            int panelGradientEndColor) {
        this.blurEnabled = blurEnabled;
        this.blurRadiusDp = clamp(blurRadiusDp, 0, 32);
        this.metallicEnabled = metallicEnabled;
        this.metallicStrengthPercent = clamp(metallicStrengthPercent, 0, 100);
        this.angularPreviewBubble = angularPreviewBubble;
        this.keyFaceGradientEnabled = keyFaceGradientEnabled;
        this.keyFaceGradientStrengthPercent = clamp(keyFaceGradientStrengthPercent, 0, 100);
        this.panelGradientEnabled = panelGradientEnabled;
        this.panelGradientStartColor = opaque(panelGradientStartColor);
        this.panelGradientEndColor = opaque(panelGradientEndColor);
    }

    boolean hasExportableEffects() {
        return blurEnabled
                || blurRadiusDp > 0
                || metallicEnabled
                || metallicStrengthPercent > 0
                || angularPreviewBubble != DEFAULT.angularPreviewBubble
                || keyFaceGradientEnabled != DEFAULT.keyFaceGradientEnabled
                || keyFaceGradientStrengthPercent != DEFAULT.keyFaceGradientStrengthPercent
                || panelGradientEnabled != DEFAULT.panelGradientEnabled
                || panelGradientStartColor != DEFAULT.panelGradientStartColor
                || panelGradientEndColor != DEFAULT.panelGradientEndColor;
    }

    KeyboardVisualEffects withKeyFaceGradient(boolean enabled, int strengthPercent) {
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                enabled,
                strengthPercent,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor);
    }

    KeyboardVisualEffects withPanelGradient(boolean enabled, int startColor, int endColor) {
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                enabled,
                startColor,
                endColor);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }
}
