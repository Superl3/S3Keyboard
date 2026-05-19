package com.academic.hangulgestureime;

import java.util.List;

final class TouchResolver {
    private TouchResolver() {
    }

    static <T extends Target> T resolve(
            List<T> targets,
            float x,
            float y,
            float hitSlop,
            float touchYOffset,
            float biasX,
            float biasY) {
        float adjustedX = x + biasX;
        float adjustedY = y + touchYOffset + biasY;

        T best = null;
        float bestDistance = Float.MAX_VALUE;
        for (T target : targets) {
            float effectiveSlop = hitSlop + (target.isPrimaryBottomControl() ? hitSlop * 0.75f : 0f);
            if (!target.contains(adjustedX, adjustedY)
                    && !target.expandedContains(adjustedX, adjustedY, effectiveSlop)) {
                continue;
            }
            float distance = target.distanceSquaredTo(adjustedX, adjustedY);
            if (distance < bestDistance) {
                best = target;
                bestDistance = distance;
            }
        }
        return best;
    }

    interface Target {
        boolean contains(float x, float y);

        boolean expandedContains(float x, float y, float slop);

        float distanceSquaredTo(float x, float y);

        boolean isPrimaryBottomControl();
    }
}
