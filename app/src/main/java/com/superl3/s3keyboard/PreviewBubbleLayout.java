package com.superl3.s3keyboard;

final class PreviewBubbleLayout {
    private static final float LIFT_RISE_END = 0.34f;
    private static final float LIFT_HOLD_END = 0.70f;

    private PreviewBubbleLayout() {
    }

    static int widthPx(float measuredTextWidthPx, int minWidthPx, int maxWidthPx, int horizontalPaddingPx) {
        int contentWidth = Math.round(measuredTextWidthPx) + horizontalPaddingPx;
        return Math.min(maxWidthPx, Math.max(minWidthPx, contentWidth));
    }

    static int xPx(float anchorCenterX, int popupWidthPx, int viewWidthPx, int horizontalInsetPx) {
        float minimumX = horizontalInsetPx;
        float maximumX = Math.max(horizontalInsetPx, viewWidthPx - popupWidthPx - horizontalInsetPx);
        return Math.round(clamp(anchorCenterX - popupWidthPx / 2f, minimumX, maximumX));
    }

    static int yPx(float anchorTop, int popupHeightPx, int topGapPx, int liftPx) {
        return Math.round(anchorTop - topGapPx - popupHeightPx - liftPx);
    }

    static int liftPx(boolean motionEnabled, float progress, int peakLiftPx, int settleLiftPx) {
        if (!motionEnabled) {
            return 0;
        }
        if (progress < LIFT_RISE_END) {
            return Math.round(peakLiftPx * smoothStep(progress / LIFT_RISE_END));
        }
        if (progress < LIFT_HOLD_END) {
            return peakLiftPx;
        }
        if (progress < 1f) {
            float descend = smoothStep((progress - LIFT_HOLD_END) / (1f - LIFT_HOLD_END));
            return Math.round(peakLiftPx + (settleLiftPx - peakLiftPx) * descend);
        }
        return settleLiftPx;
    }

    static int cornerRadiusPx(int keyRadiusPx, boolean angular, int minRadiusPx, int angularMaxPx, int roundedMaxPx) {
        int maxRadius = angular ? angularMaxPx : roundedMaxPx;
        return Math.max(minRadiusPx, Math.min(maxRadius, keyRadiusPx));
    }

    static boolean nearAnchor(
            float bubbleCenterX,
            float bubbleTop,
            float anchorCenterX,
            float anchorTop,
            float anchorWidth,
            float anchorHeight,
            int minimumThresholdPx) {
        float horizontalThreshold = Math.max(anchorWidth * 1.25f, minimumThresholdPx);
        float verticalThreshold = Math.max(anchorHeight * 1.25f, minimumThresholdPx);
        return Math.abs(bubbleCenterX - anchorCenterX) <= horizontalThreshold
                && Math.abs(bubbleTop - anchorTop) <= verticalThreshold;
    }

    private static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static float smoothStep(float value) {
        float clamped = clamp(value, 0f, 1f);
        return clamped * clamped * (3f - 2f * clamped);
    }
}
