package com.superl3.s3keyboard;

final class PreviewOverlaySpec {
    String label;
    int x;
    int y;
    int width;
    int height;
    float textSizePx;
    int textColor;
    int backgroundColor;
    int borderColor;
    int borderWidthPx;
    int cornerRadiusPx;
    boolean angularBubble;
    float alpha;
    float scaleX;
    float scaleY;
    float textScale;
    float commitGlowAlpha;
    float inputImpactAlpha;

    PreviewOverlaySpec() {
        set("", 0, 0, 1, 1, 1f, 0, 0, 0, 0, 0,
                false, 0f, 1f, 1f, 1f, 0f, 0f);
    }

    PreviewOverlaySpec(
            String label,
            int x,
            int y,
            int width,
            int height,
            float textSizePx,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            int cornerRadiusPx,
            boolean angularBubble,
            float alpha,
            float scaleX,
            float scaleY,
            float textScale,
            float commitGlowAlpha,
            float inputImpactAlpha) {
        set(label, x, y, width, height, textSizePx, textColor, backgroundColor,
                borderColor, borderWidthPx, cornerRadiusPx, angularBubble, alpha,
                scaleX, scaleY, textScale, commitGlowAlpha, inputImpactAlpha);
    }

    PreviewOverlaySpec set(
            String label,
            int x,
            int y,
            int width,
            int height,
            float textSizePx,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            int cornerRadiusPx,
            boolean angularBubble,
            float alpha,
            float scaleX,
            float scaleY,
            float textScale,
            float commitGlowAlpha,
            float inputImpactAlpha) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textSizePx = textSizePx;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidthPx = borderWidthPx;
        this.cornerRadiusPx = cornerRadiusPx;
        this.angularBubble = angularBubble;
        this.alpha = alpha;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.textScale = Math.max(0.72f, Math.min(1.18f, textScale));
        this.commitGlowAlpha = Math.max(0f, Math.min(1f, commitGlowAlpha));
        this.inputImpactAlpha = Math.max(0f, Math.min(1f, inputImpactAlpha));
        return this;
    }
}
