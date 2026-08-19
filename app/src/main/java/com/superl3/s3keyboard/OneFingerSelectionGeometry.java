package com.superl3.s3keyboard;

final class OneFingerSelectionGeometry {
    private static final float ENTRY_RADIUS_FRACTION = 0.38f;
    private static final float EXIT_RADIUS_MULTIPLIER = 1.20f;

    private OneFingerSelectionGeometry() {
    }

    static boolean contains(
            float centerX,
            float centerY,
            float width,
            float height,
            float minimumRadius,
            float x,
            float y,
            boolean retainingSelection) {
        float radiusX = radiusX(width, minimumRadius, retainingSelection);
        float radiusY = radiusY(height, minimumRadius, retainingSelection);
        if (radiusX <= 0f || radiusY <= 0f) {
            return false;
        }
        float dx = x - centerX;
        float dy = y - centerY;
        float normalizedX = dx / radiusX;
        float normalizedY = dy / radiusY;
        return normalizedX * normalizedX + normalizedY * normalizedY <= 1f;
    }

    static float radiusX(float width, float minimumRadius, boolean retainingSelection) {
        return radiusForDimension(width, minimumRadius, retainingSelection);
    }

    static float radiusY(float height, float minimumRadius, boolean retainingSelection) {
        return radiusForDimension(height, minimumRadius, retainingSelection);
    }

    private static float radiusForDimension(
            float dimension,
            float minimumRadius,
            boolean retainingSelection) {
        float radius = Math.max(
                Math.max(0f, minimumRadius),
                Math.max(0f, dimension) * ENTRY_RADIUS_FRACTION);
        return retainingSelection ? radius * EXIT_RADIUS_MULTIPLIER : radius;
    }
}
