package com.superl3.s3keyboard;

final class PreviewBubbleAnimation {
    private static final long POP_ANIMATION_MS = 64;
    private static final long MOTION_ANIMATION_MS = 1;
    private static final long RELEASE_ANIMATION_MS = 92;
    private static final long SUPERSEDE_FADE_MS = 42;
    private static final float RELEASE_HOLD_FRACTION = 0.0f;
    private static final float POP_OVERSHOOT = 0f;
    private static final float RELEASE_IMPULSE_SCALE = 0f;
    private static final float PRESS_SQUASH_X = 0f;
    private static final float PRESS_SQUASH_Y = 0f;
    private static final float RELEASE_STRETCH_Y = 0f;
    private static final float RELEASE_IMPULSE_END = 0.10f;
    private static final float PRESS_SQUASH_END = 0.18f;
    private static final float RELEASE_STRETCH_END = 0.08f;
    private static final float COMMIT_GLOW_AFTERGLOW_END = 0.16f;
    private static final float HELD_PRESSURE_ALPHA = 0f;
    private static final float INPUT_IMPACT_ATTACK_END = 0.26f;
    private static final long INPUT_IMPACT_SETTLE_DURATION_MULTIPLIER = 4L;
    private static final float COMMIT_LIFT_RISE_END = 0.18f;
    private static final float COMMIT_LIFT_SETTLE_START = 0.38f;
    private static final float COMMIT_LIFT_SETTLE_DURATION = 0.46f;
    private static final float RELEASE_FLOAT_RISE_END = 0.36f;
    private static final float RELEASE_FLOAT_SETTLE_START = 0.58f;
    private static final float RELEASE_FLOAT_SETTLE_DURATION = 0.20f;
    private static final float CONFIRMATION_ATTACK_END = 0.055f;
    private static final float CONFIRMATION_RELEASE_START = 0.030f;
    private static final float CONFIRMATION_RELEASE_DURATION = 0.42f;
    private static final float POP_START_SCALE = 0.985f;
    private static final float POP_SCALE_RANGE = 0.015f;
    private static final float POP_OVERSHOOT_SCALE = 0.0f;
    private static final float TEXT_START_SCALE = 1f;
    private static final float TEXT_SCALE_RANGE = 0f;
    private static final float TEXT_CONFIRMATION_SCALE = 0f;
    private static final float ACTIVE_GLOW_SETTLED_ALPHA = 0f;
    private static final float ACTIVE_GLOW_MIN_ALPHA = 0f;
    private static final float ACTIVE_GLOW_ATTACK_ALPHA = 0f;
    private static final float RELEASE_GLOW_AFTERGLOW_ALPHA = 0f;
    private static final float RELEASE_GLOW_CONFIRMATION_ALPHA = 0f;
    private static final float INPUT_IMPACT_ALPHA = 0f;
    private static final float RELEASE_FLOAT_SETTLE_AMOUNT = 0f;

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
        this.released = true;
    }

    void markSuperseded(long supersededTimeMs) {
        if (superseded) {
            return;
        }
        this.supersededTimeMs = supersededTimeMs;
        this.superseded = true;
    }

    private float popProgress(long nowMs, float durationScale) {
        return easeOutBack(progressSince(nowMs, pulseTimeMs, POP_ANIMATION_MS, durationScale));
    }

    float scale(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        return baseScale(nowMs, durationScale);
    }

    private float baseScale(long nowMs, float durationScale) {
        float progress = popProgress(nowMs, durationScale);
        float scale = popScale(progress);
        if (released) {
            float releaseProgress = releaseProgress(nowMs, durationScale);
            scale += releaseImpulseScale(releaseProgress);
        }
        return scale;
    }

    private float popScale(float progress) {
        if (progress <= 1f) {
            return POP_START_SCALE + POP_SCALE_RANGE * progress;
        }
        return 1f + (progress - 1f) * POP_OVERSHOOT_SCALE;
    }

    private float releaseImpulseScale(float releaseProgress) {
        float releaseImpulse = 1f - smoothStep(releaseProgress / RELEASE_IMPULSE_END);
        return RELEASE_IMPULSE_SCALE * positiveOnly(releaseImpulse);
    }

    float scaleX(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        float scale = baseScale(nowMs, durationScale);
        float squash = pressSquash(nowMs, durationScale);
        float releaseStretch = releaseStretch(nowMs, durationScale);
        return scale * (1f + PRESS_SQUASH_X * squash - RELEASE_STRETCH_Y * releaseStretch * 0.45f);
    }

    float scaleY(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        float scale = baseScale(nowMs, durationScale);
        float squash = pressSquash(nowMs, durationScale);
        float releaseStretch = releaseStretch(nowMs, durationScale);
        return scale * (1f - PRESS_SQUASH_Y * squash + RELEASE_STRETCH_Y * releaseStretch);
    }

    float textScale(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        float pop = popProgress(nowMs, durationScale);
        float scale = TEXT_START_SCALE + TEXT_SCALE_RANGE * Math.min(1.08f, pop);
        if (released) {
            scale += TEXT_CONFIRMATION_SCALE * confirmationPulse(releaseProgress(nowMs, durationScale));
        }
        return scale;
    }

    float motionProgress(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        return progressSince(nowMs, startTimeMs, MOTION_ANIMATION_MS, durationScale);
    }

    float alpha(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 1f;
        }
        if (!released) {
            return 1f;
        }
        float progress = releaseProgress(nowMs, durationScale);
        if (progress < RELEASE_HOLD_FRACTION) {
            return supersededAlpha(1f, nowMs, durationScale);
        }
        float releaseAlpha = 1f
                - smoothStep((progress - RELEASE_HOLD_FRACTION) / (1f - RELEASE_HOLD_FRACTION));
        return supersededAlpha(releaseAlpha, nowMs, durationScale);
    }

    float commitGlowAlpha(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 0f;
        }
        if (released) {
            return releasedCommitGlowAlpha(releaseProgress(nowMs, durationScale));
        }
        return activeCommitGlowAlpha(nowMs, durationScale);
    }

    private float releasedCommitGlowAlpha(float releaseProgress) {
        float afterglow = 1f - smoothStep(releaseProgress / COMMIT_GLOW_AFTERGLOW_END);
        float impact = confirmationPulse(releaseProgress);
        return Math.max(
                RELEASE_GLOW_AFTERGLOW_ALPHA * afterglow,
                RELEASE_GLOW_CONFIRMATION_ALPHA * impact);
    }

    private float activeCommitGlowAlpha(long nowMs, float durationScale) {
        float progress = progressSince(nowMs, pulseTimeMs, POP_ANIMATION_MS, durationScale);
        if (progress >= 1f) {
            return ACTIVE_GLOW_SETTLED_ALPHA;
        }
        return Math.max(
                ACTIVE_GLOW_MIN_ALPHA,
                ACTIVE_GLOW_ATTACK_ALPHA * (1f - smoothStep(progress)));
    }

    float inputImpactAlpha(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return 0f;
        }
        if (released) {
            return confirmationPulse(releaseProgress(nowMs, durationScale));
        }
        return activeInputImpactAlpha(nowMs, durationScale);
    }

    private float activeInputImpactAlpha(long nowMs, float durationScale) {
        float pressProgress = progressSince(nowMs, pulseTimeMs, POP_ANIMATION_MS, durationScale);
        float attack = 1f - smoothStep(pressProgress / INPUT_IMPACT_ATTACK_END);
        float settle = progressSince(
                nowMs,
                pulseTimeMs,
                POP_ANIMATION_MS * INPUT_IMPACT_SETTLE_DURATION_MULTIPLIER,
                durationScale);
        return Math.max(
                HELD_PRESSURE_ALPHA,
                attack * (1f - smoothStep(settle)) * INPUT_IMPACT_ALPHA);
    }

    float commitLiftProgress(long nowMs, boolean motionEnabled, float durationScale) {
        return 0f;
    }

    float releaseFloatProgress(long nowMs, boolean motionEnabled, float durationScale) {
        return 0f;
    }

    boolean expired(long nowMs, boolean motionEnabled, float durationScale) {
        if (!motionEnabled) {
            return false;
        }
        if (superseded && supersededProgress(nowMs, durationScale) >= 1f) {
            return true;
        }
        return released && releaseProgress(nowMs, durationScale) >= 1f;
    }

    private float releaseProgress(long nowMs, float durationScale) {
        if (!released) {
            return 0f;
        }
        return progressSince(nowMs, releaseTimeMs, RELEASE_ANIMATION_MS, durationScale);
    }

    private float supersededAlpha(float baseAlpha, long nowMs, float durationScale) {
        if (!superseded) {
            return baseAlpha;
        }
        return baseAlpha * (1f - smoothStep(supersededProgress(nowMs, durationScale)));
    }

    private float supersededProgress(long nowMs, float durationScale) {
        if (!superseded) {
            return 0f;
        }
        return progressSince(nowMs, supersededTimeMs, SUPERSEDE_FADE_MS, durationScale);
    }

    private float pressSquash(long nowMs, float durationScale) {
        float progress = progressSince(nowMs, pulseTimeMs, POP_ANIMATION_MS, durationScale);
        return 1f - smoothStep(progress / PRESS_SQUASH_END);
    }

    private float releaseStretch(long nowMs, float durationScale) {
        if (!released) {
            return 0f;
        }
        float progress = releaseProgress(nowMs, durationScale);
        return 1f - smoothStep(progress / RELEASE_STRETCH_END);
    }

    private static float safeScale(float durationScale) {
        return Math.max(0.01f, durationScale);
    }

    private static float progressSince(
            long nowMs,
            long startMs,
            long durationMs,
            float durationScale) {
        return clamp01((nowMs - startMs) / (durationMs * safeScale(durationScale)));
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

    private static float easeOutBack(float value) {
        float clamped = clamp01(value);
        float shifted = clamped - 1f;
        return 1f + shifted * shifted * ((POP_OVERSHOOT + 1f) * shifted + POP_OVERSHOOT);
    }

    private static float confirmationPulse(float value) {
        float clamped = clamp01(value);
        float attack = smoothStep(clamped / CONFIRMATION_ATTACK_END);
        float release = 1f - smoothStep(
                (clamped - CONFIRMATION_RELEASE_START) / CONFIRMATION_RELEASE_DURATION);
        return positiveOnly(attack * release);
    }

    private static float positiveOnly(float value) {
        return Math.max(0f, value);
    }
}
