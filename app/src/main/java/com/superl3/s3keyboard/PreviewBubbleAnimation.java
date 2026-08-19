package com.superl3.s3keyboard;

final class PreviewBubbleAnimation {
    private static final long POP_ANIMATION_MS = 64;
    private static final long RELEASE_ANIMATION_MS = 92;
    private static final long SUPERSEDE_FADE_MS = 42;
    private static final float POP_START_SCALE = 0.985f;

    String label;
    final float anchorCenterX;
    final float anchorTop;
    int textColor;
    int backgroundColor;
    int borderColor;
    int borderWidthPx;
    final long startTimeMs;
    long pulseTimeMs;
    long releaseTimeMs;
    long supersededTimeMs;
    boolean released;
    boolean superseded;

    PreviewBubbleAnimation(
            String label,
            float anchorCenterX,
            float anchorTop,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            long startTimeMs,
            boolean released) {
        this.label = label;
        this.anchorCenterX = anchorCenterX;
        this.anchorTop = anchorTop;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidthPx = borderWidthPx;
        this.startTimeMs = startTimeMs;
        this.pulseTimeMs = startTimeMs;
        this.releaseTimeMs = startTimeMs;
        this.supersededTimeMs = startTimeMs;
        this.released = released;
    }

    void update(
            String label,
            int textColor,
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            long nowMs,
            boolean released) {
        if (!this.label.equals(label)) {
            pulseTimeMs = nowMs;
        }
        this.label = label;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidthPx = borderWidthPx;
        this.released = released;
    }

    void markReleased(long releaseTimeMs) {
        this.releaseTimeMs = releaseTimeMs;
        released = true;
    }

    void markSuperseded(long supersededTimeMs) {
        if (superseded) {
            return;
        }
        this.supersededTimeMs = supersededTimeMs;
        superseded = true;
    }

    float scale(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        float progress = smoothStep(progressSince(
                nowMs,
                pulseTimeMs,
                POP_ANIMATION_MS,
                durationScale));
        return POP_START_SCALE + (1f - POP_START_SCALE) * progress;
    }

    float scaleX(long nowMs, boolean motionEnabled, float durationScale) {
        return scale(nowMs, motionEnabled, durationScale);
    }

    float scaleY(long nowMs, boolean motionEnabled, float durationScale) {
        return scale(nowMs, motionEnabled, durationScale);
    }

    float textScale(long nowMs, boolean motionEnabled, float durationScale) {
        return 1f;
    }

    float alpha(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled || !released) {
            return 1f;
        }
        float releaseAlpha = 1f - smoothStep(progressSince(
                nowMs,
                releaseTimeMs,
                RELEASE_ANIMATION_MS,
                durationScale));
        if (!superseded) {
            return releaseAlpha;
        }
        float supersededAlpha = 1f - smoothStep(progressSince(
                nowMs,
                supersededTimeMs,
                SUPERSEDE_FADE_MS,
                durationScale));
        return releaseAlpha * supersededAlpha;
    }

    boolean expired(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return false;
        }
        if (superseded && progressSince(
                nowMs,
                supersededTimeMs,
                SUPERSEDE_FADE_MS,
                durationScale) >= 1f) {
            return true;
        }
        return released && progressSince(
                nowMs,
                releaseTimeMs,
                RELEASE_ANIMATION_MS,
                durationScale) >= 1f;
    }

    private static float progressSince(
            long nowMs,
            long startMs,
            long durationMs,
            float durationScale) {
        float safeScale = Math.max(0.01f, durationScale);
        return clamp01((nowMs - startMs) / (durationMs * safeScale));
    }

    private static float smoothStep(float value) {
        float clamped = clamp01(value);
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
