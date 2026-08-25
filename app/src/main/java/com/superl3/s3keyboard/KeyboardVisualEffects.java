package com.superl3.s3keyboard;

final class KeyboardVisualEffects {
    static final String KEY_FACE_GRADIENT_CURVE_LINEAR = "linear";
    static final String KEY_FACE_GRADIENT_CURVE_SOFT = "soft";
    static final String KEY_FACE_GRADIENT_CURVE_TOP_GLOW = "top_glow";
    static final String KEY_FACE_GRADIENT_CURVE_BOTTOM_SHADE = "bottom_shade";
    static final String KEY_FACE_GRADIENT_CURVE_GLASS = "glass";
    static final int DEFAULT_KEY_FACE_GRADIENT_START_COLOR = 0xFFFFFFFF;
    static final int DEFAULT_KEY_FACE_GRADIENT_END_COLOR = 0xFF000000;
    static final int DEFAULT_GLASS_TINT_ALPHA_PERCENT = 86;
    static final int DEFAULT_GLASS_HIGHLIGHT_PERCENT = 18;
    static final int DEFAULT_GLASS_BORDER_ALPHA_PERCENT = 42;
    private static final String[] KEY_FACE_GRADIENT_CURVE_ORDER = {
            KEY_FACE_GRADIENT_CURVE_SOFT,
            KEY_FACE_GRADIENT_CURVE_LINEAR,
            KEY_FACE_GRADIENT_CURVE_TOP_GLOW,
            KEY_FACE_GRADIENT_CURVE_BOTTOM_SHADE,
            KEY_FACE_GRADIENT_CURVE_GLASS
    };
    private static final String[] KEY_FACE_GRADIENT_CURVE_LABELS = {
            "Soft",
            "Linear",
            "Top glow",
            "Bottom shade",
            "Glass highlight"
    };

    static final KeyboardVisualEffects DEFAULT = new KeyboardVisualEffects(
            false,
            0,
            false,
            0,
            true,
            true,
            22,
            DEFAULT_KEY_FACE_GRADIENT_START_COLOR,
            DEFAULT_KEY_FACE_GRADIENT_END_COLOR,
            KEY_FACE_GRADIENT_CURVE_SOFT,
            false,
            0xFFEBEBEB,
            0xFFEBEBEB,
            false,
            DEFAULT_GLASS_TINT_ALPHA_PERCENT,
            DEFAULT_GLASS_HIGHLIGHT_PERCENT,
            DEFAULT_GLASS_BORDER_ALPHA_PERCENT);

    final boolean blurEnabled;
    final int blurRadiusDp;
    final boolean metallicEnabled;
    final int metallicStrengthPercent;
    final boolean angularPreviewBubble;
    final boolean keyFaceGradientEnabled;
    final int keyFaceGradientStrengthPercent;
    final int keyFaceGradientStartColor;
    final int keyFaceGradientEndColor;
    final String keyFaceGradientCurve;
    final boolean panelGradientEnabled;
    final int panelGradientStartColor;
    final int panelGradientEndColor;
    final boolean glassEnabled;
    final int glassTintAlphaPercent;
    final int glassHighlightPercent;
    final int glassBorderAlphaPercent;

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
                DEFAULT.keyFaceGradientStartColor,
                DEFAULT.keyFaceGradientEndColor,
                DEFAULT.keyFaceGradientCurve,
                DEFAULT.panelGradientEnabled,
                DEFAULT.panelGradientStartColor,
                DEFAULT.panelGradientEndColor,
                DEFAULT.glassEnabled,
                DEFAULT.glassTintAlphaPercent,
                DEFAULT.glassHighlightPercent,
                DEFAULT.glassBorderAlphaPercent);
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
                DEFAULT.keyFaceGradientStartColor,
                DEFAULT.keyFaceGradientEndColor,
                DEFAULT.keyFaceGradientCurve,
                DEFAULT.panelGradientEnabled,
                DEFAULT.panelGradientStartColor,
                DEFAULT.panelGradientEndColor,
                DEFAULT.glassEnabled,
                DEFAULT.glassTintAlphaPercent,
                DEFAULT.glassHighlightPercent,
                DEFAULT.glassBorderAlphaPercent);
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
        this(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                DEFAULT.keyFaceGradientStartColor,
                DEFAULT.keyFaceGradientEndColor,
                DEFAULT.keyFaceGradientCurve,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor,
                DEFAULT.glassEnabled,
                DEFAULT.glassTintAlphaPercent,
                DEFAULT.glassHighlightPercent,
                DEFAULT.glassBorderAlphaPercent);
    }

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble,
            boolean keyFaceGradientEnabled,
            int keyFaceGradientStrengthPercent,
            int keyFaceGradientStartColor,
            int keyFaceGradientEndColor,
            String keyFaceGradientCurve,
            boolean panelGradientEnabled,
            int panelGradientStartColor,
            int panelGradientEndColor) {
        this(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                keyFaceGradientStartColor,
                keyFaceGradientEndColor,
                keyFaceGradientCurve,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor,
                DEFAULT.glassEnabled,
                DEFAULT.glassTintAlphaPercent,
                DEFAULT.glassHighlightPercent,
                DEFAULT.glassBorderAlphaPercent);
    }

    KeyboardVisualEffects(
            boolean blurEnabled,
            int blurRadiusDp,
            boolean metallicEnabled,
            int metallicStrengthPercent,
            boolean angularPreviewBubble,
            boolean keyFaceGradientEnabled,
            int keyFaceGradientStrengthPercent,
            int keyFaceGradientStartColor,
            int keyFaceGradientEndColor,
            String keyFaceGradientCurve,
            boolean panelGradientEnabled,
            int panelGradientStartColor,
            int panelGradientEndColor,
            boolean glassEnabled,
            int glassTintAlphaPercent,
            int glassHighlightPercent,
            int glassBorderAlphaPercent) {
        this.blurEnabled = blurEnabled;
        this.blurRadiusDp = clamp(blurRadiusDp, 0, 32);
        this.metallicEnabled = metallicEnabled;
        this.metallicStrengthPercent = clamp(metallicStrengthPercent, 0, 100);
        this.angularPreviewBubble = angularPreviewBubble;
        this.keyFaceGradientEnabled = keyFaceGradientEnabled;
        this.keyFaceGradientStrengthPercent = clamp(keyFaceGradientStrengthPercent, 0, 100);
        this.keyFaceGradientStartColor = opaque(keyFaceGradientStartColor);
        this.keyFaceGradientEndColor = opaque(keyFaceGradientEndColor);
        this.keyFaceGradientCurve = normalizeKeyFaceGradientCurve(keyFaceGradientCurve);
        this.panelGradientEnabled = panelGradientEnabled;
        this.panelGradientStartColor = opaque(panelGradientStartColor);
        this.panelGradientEndColor = opaque(panelGradientEndColor);
        this.glassEnabled = glassEnabled;
        this.glassTintAlphaPercent = clamp(glassTintAlphaPercent, 60, 98);
        this.glassHighlightPercent = clamp(glassHighlightPercent, 0, 60);
        this.glassBorderAlphaPercent = clamp(glassBorderAlphaPercent, 0, 100);
    }

    boolean hasExportableEffects() {
        return blurEnabled
                || blurRadiusDp > 0
                || metallicEnabled
                || metallicStrengthPercent > 0
                || angularPreviewBubble != DEFAULT.angularPreviewBubble
                || keyFaceGradientEnabled != DEFAULT.keyFaceGradientEnabled
                || keyFaceGradientStrengthPercent != DEFAULT.keyFaceGradientStrengthPercent
                || keyFaceGradientStartColor != DEFAULT.keyFaceGradientStartColor
                || keyFaceGradientEndColor != DEFAULT.keyFaceGradientEndColor
                || !keyFaceGradientCurve.equals(DEFAULT.keyFaceGradientCurve)
                || panelGradientEnabled != DEFAULT.panelGradientEnabled
                || panelGradientStartColor != DEFAULT.panelGradientStartColor
                || panelGradientEndColor != DEFAULT.panelGradientEndColor
                || glassEnabled
                || glassTintAlphaPercent != DEFAULT.glassTintAlphaPercent
                || glassHighlightPercent != DEFAULT.glassHighlightPercent
                || glassBorderAlphaPercent != DEFAULT.glassBorderAlphaPercent;
    }

    KeyboardVisualEffects withKeyFaceGradient(boolean enabled, int strengthPercent) {
        return withKeyFaceGradient(
                enabled,
                strengthPercent,
                keyFaceGradientStartColor,
                keyFaceGradientEndColor,
                keyFaceGradientCurve);
    }

    KeyboardVisualEffects withKeyFaceGradient(
            boolean enabled,
            int strengthPercent,
            int startColor,
            int endColor,
            String curve) {
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                enabled,
                strengthPercent,
                startColor,
                endColor,
                curve,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor,
                glassEnabled,
                glassTintAlphaPercent,
                glassHighlightPercent,
                glassBorderAlphaPercent);
    }

    KeyboardVisualEffects withBlur(boolean enabled, int radiusDp) {
        return new KeyboardVisualEffects(
                enabled,
                radiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                keyFaceGradientStartColor,
                keyFaceGradientEndColor,
                keyFaceGradientCurve,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor,
                glassEnabled,
                glassTintAlphaPercent,
                glassHighlightPercent,
                glassBorderAlphaPercent);
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
                keyFaceGradientStartColor,
                keyFaceGradientEndColor,
                keyFaceGradientCurve,
                enabled,
                startColor,
                endColor,
                glassEnabled,
                glassTintAlphaPercent,
                glassHighlightPercent,
                glassBorderAlphaPercent);
    }

    KeyboardVisualEffects withGlass(
            boolean enabled,
            int tintAlphaPercent,
            int highlightPercent,
            int borderAlphaPercent) {
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrengthPercent,
                angularPreviewBubble,
                keyFaceGradientEnabled,
                keyFaceGradientStrengthPercent,
                keyFaceGradientStartColor,
                keyFaceGradientEndColor,
                keyFaceGradientCurve,
                panelGradientEnabled,
                panelGradientStartColor,
                panelGradientEndColor,
                enabled,
                tintAlphaPercent,
                highlightPercent,
                borderAlphaPercent);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }

    static String normalizeKeyFaceGradientCurve(String curve) {
        if (KEY_FACE_GRADIENT_CURVE_LINEAR.equals(curve)
                || KEY_FACE_GRADIENT_CURVE_SOFT.equals(curve)
                || KEY_FACE_GRADIENT_CURVE_TOP_GLOW.equals(curve)
                || KEY_FACE_GRADIENT_CURVE_BOTTOM_SHADE.equals(curve)
                || KEY_FACE_GRADIENT_CURVE_GLASS.equals(curve)) {
            return curve;
        }
        return KEY_FACE_GRADIENT_CURVE_SOFT;
    }

    static String[] keyFaceGradientCurveOrder() {
        return KEY_FACE_GRADIENT_CURVE_ORDER.clone();
    }

    static String[] keyFaceGradientCurveLabels() {
        return KEY_FACE_GRADIENT_CURVE_LABELS.clone();
    }

    static String keyFaceGradientCurveAt(int position) {
        if (position < 0 || position >= KEY_FACE_GRADIENT_CURVE_ORDER.length) {
            return KEY_FACE_GRADIENT_CURVE_ORDER[0];
        }
        return KEY_FACE_GRADIENT_CURVE_ORDER[position];
    }

    static int keyFaceGradientCurveIndexOf(String curve) {
        String normalized = normalizeKeyFaceGradientCurve(curve);
        for (int i = 0; i < KEY_FACE_GRADIENT_CURVE_ORDER.length; i++) {
            if (KEY_FACE_GRADIENT_CURVE_ORDER[i].equals(normalized)) {
                return i;
            }
        }
        return 0;
    }
}
