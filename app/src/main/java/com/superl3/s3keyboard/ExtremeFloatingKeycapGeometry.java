package com.superl3.s3keyboard;

final class ExtremeFloatingKeycapGeometry {
    private static final float STANDARD_WIDTH_FRACTION = 0.70f;
    private static final float COMPACT_RAIL_WIDTH_FRACTION = 0.82f;
    private static final float HEIGHT_FRACTION = 0.72f;

    private ExtremeFloatingKeycapGeometry() {
    }

    static float width(float availableWidth, float minimumWidth, boolean compactRail) {
        float safeAvailable = Math.max(0f, availableWidth);
        float fraction = compactRail
                ? COMPACT_RAIL_WIDTH_FRACTION
                : STANDARD_WIDTH_FRACTION;
        return Math.min(safeAvailable, Math.max(Math.max(0f, minimumWidth), safeAvailable * fraction));
    }

    static float height(float availableHeight, float minimumHeight) {
        float safeAvailable = Math.max(0f, availableHeight);
        return Math.min(
                safeAvailable,
                Math.max(Math.max(0f, minimumHeight), safeAvailable * HEIGHT_FRACTION));
    }
}
