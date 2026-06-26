package com.superl3.s3keyboard;

final class PreviewBubbleAnimation {
    private static final long POP_ANIMATION_MS = 120;
    private static final long MOTION_ANIMATION_MS = 360;
    private static final long RELEASE_ANIMATION_MS = 420;

    String label;
    final long sequence;
    final float anchorCenterX;
    final float anchorTop;
    final float anchorBottom;
    int textColor;
    int backgroundColor;
    int borderColor;
    int borderWidthPx;
    final long startTimeMs;
    long releaseTimeMs;
    boolean released;

    PreviewBubbleAnimation(
            String label,
            long sequence,
            float anchorCenterX,
            float anchorTop,
            float anchorBottom,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            long startTimeMs,
            boolean released) {
        this.label = label;
        this.sequence = sequence;
        this.anchorCenterX = anchorCenterX;
        this.anchorTop = anchorTop;
        this.anchorBottom = anchorBottom;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidthPx = borderWidthPx;
        this.startTimeMs = startTimeMs;
        this.releaseTimeMs = startTimeMs;
        this.released = released;
    }

    void update(
            String label,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            boolean released) {
        this.label = label;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidthPx = borderWidthPx;
        this.released = released;
    }

    void markReleased(long releaseTimeMs) {
        this.releaseTimeMs = releaseTimeMs;
        this.released = true;
    }

    float popProgress(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        return easeOut(clamp01((nowMs - startTimeMs) / (POP_ANIMATION_MS * safeScale(durationScale))));
    }

    float motionProgress(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        return clamp01((nowMs - startTimeMs) / (MOTION_ANIMATION_MS * safeScale(durationScale)));
    }

    float alpha(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled || !released) {
            return 1f;
        }
        float progress = releaseProgress(nowMs, true, durationScale);
        if (progress < 0.45f) {
            return 1f;
        }
        return 1f - smoothStep((progress - 0.45f) / 0.55f);
    }

    boolean expired(long nowMs, boolean motionEnabled, float durationScale) {
        return released && releaseProgress(nowMs, motionEnabled, durationScale) >= 1f;
    }

    private float releaseProgress(long nowMs, boolean motionEnabled, float durationScale) {
        if (!released || !motionEnabled) {
            return 0f;
        }
        return clamp01((nowMs - releaseTimeMs) / (RELEASE_ANIMATION_MS * safeScale(durationScale)));
    }

    private static float safeScale(float durationScale) {
        return Math.max(0.01f, durationScale);
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }

    private static float smoothStep(float value) {
        float clamped = clamp01(value);
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float easeOut(float value) {
        float clamped = clamp01(value);
        return 1f - (1f - clamped) * (1f - clamped);
    }
}
