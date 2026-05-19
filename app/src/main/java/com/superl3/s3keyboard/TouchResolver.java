package com.superl3.s3keyboard;

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
        T primaryBottomHit = resolvePrimaryBottomHit(targets, x, y);
        if (primaryBottomHit != null) {
            return primaryBottomHit;
        }

        float adjustedX = x + biasX;
        float adjustedY = y + touchYOffset + biasY;

        T bestContained = null;
        float bestContainedDistance = Float.MAX_VALUE;
        T bestExpanded = null;
        float bestExpandedDistance = Float.MAX_VALUE;
        for (T target : targets) {
            boolean contains = target.contains(adjustedX, adjustedY);
            float effectiveSlop = hitSlop + (target.isPrimaryBottomControl() ? hitSlop * 0.75f : 0f);
            boolean expandedContains = contains
                    || target.expandedContains(adjustedX, adjustedY, effectiveSlop);
            if (!expandedContains) {
                continue;
            }
            float distance = target.distanceSquaredTo(adjustedX, adjustedY);
            if (contains) {
                if (distance < bestContainedDistance) {
                    bestContained = target;
                    bestContainedDistance = distance;
                }
            } else if (distance < bestExpandedDistance) {
                bestExpanded = target;
                bestExpandedDistance = distance;
            }
        }
        return bestContained != null ? bestContained : bestExpanded;
    }

    private static <T extends Target> T resolvePrimaryBottomHit(List<T> targets, float x, float y) {
        T best = null;
        float bestDistance = Float.MAX_VALUE;
        for (T target : targets) {
            if (!target.isPrimaryBottomControl() || !target.contains(x, y)) {
                continue;
            }
            float distance = target.distanceSquaredTo(x, y);
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
