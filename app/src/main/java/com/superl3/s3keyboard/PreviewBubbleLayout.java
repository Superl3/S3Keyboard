package com.superl3.s3keyboard;

final class PreviewBubbleLayout {
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

    static int yPx(float anchorTop, int popupHeightPx, int topGapPx) {
        return Math.round(anchorTop - topGapPx - popupHeightPx);
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

}
