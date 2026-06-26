package com.superl3.s3keyboard;

final class PreviewOverlaySpec {
    final String label;
    final int x;
    final int y;
    final int width;
    final int height;
    final float textSizePx;
    final int textColor;
    final int backgroundColor;
    final int borderColor;
    final int borderWidthPx;
    final int cornerRadiusPx;
    final boolean angularBubble;
    final float alpha;
    final float scale;

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
            float scale) {
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
        this.scale = scale;
    }
}
