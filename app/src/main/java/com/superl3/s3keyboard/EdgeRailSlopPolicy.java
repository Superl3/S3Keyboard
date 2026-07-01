package com.superl3.s3keyboard;

final class EdgeRailSlopPolicy {
    private EdgeRailSlopPolicy() {
    }

    static float leftSlop(int edgeRailDirection, float slop) {
        return edgeRailDirection < 0 ? 0f : Math.max(0f, slop);
    }

    static float rightSlop(int edgeRailDirection, float slop) {
        return edgeRailDirection > 0 ? 0f : Math.max(0f, slop);
    }

    static boolean expandedContains(
            float left,
            float top,
            float right,
            float bottom,
            float x,
            float y,
            float slop,
            int edgeRailDirection) {
        float safeSlop = Math.max(0f, slop);
        return x >= left - leftSlop(edgeRailDirection, safeSlop)
                && x <= right + rightSlop(edgeRailDirection, safeSlop)
                && y >= top - safeSlop
                && y <= bottom + safeSlop;
    }
}
