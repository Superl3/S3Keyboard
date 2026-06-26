package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class SwipeTrace {
    final List<Point> points;
    final List<String> keyLabels;

    SwipeTrace(List<Point> points, List<String> keyLabels) {
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
        this.keyLabels = Collections.unmodifiableList(new ArrayList<>(keyLabels));
    }

    String collapsedKeySequence() {
        StringBuilder builder = new StringBuilder();
        String previous = null;
        for (String keyLabel : keyLabels) {
            String normalized = normalizeKeyLabel(keyLabel);
            if (normalized == null || normalized.equals(previous)) {
                continue;
            }
            builder.append(normalized);
            previous = normalized;
        }
        return builder.toString();
    }

    int distinctKeyCount() {
        String collapsed = collapsedKeySequence();
        return collapsed.length();
    }

    static String normalizeKeyLabel(String label) {
        if (label == null || label.length() != 1) {
            return null;
        }
        char ch = Character.toLowerCase(label.charAt(0));
        if (ch < 'a' || ch > 'z') {
            return null;
        }
        return String.valueOf(ch).toLowerCase(Locale.US);
    }

    static final class Point {
        final float x;
        final float y;
        final long timeMs;

        Point(float x, float y, long timeMs) {
            this.x = x;
            this.y = y;
            this.timeMs = Math.max(0L, timeMs);
        }
    }

    static final class Builder {
        private static final float MIN_POINT_DISTANCE_PX = 3f;
        private final List<Point> points = new ArrayList<>();
        private final List<String> keyLabels = new ArrayList<>();

        void add(float x, float y, long timeMs, GestureKey key) {
            if (!points.isEmpty()) {
                Point last = points.get(points.size() - 1);
                float dx = x - last.x;
                float dy = y - last.y;
                if (dx * dx + dy * dy < MIN_POINT_DISTANCE_PX * MIN_POINT_DISTANCE_PX) {
                    addKeyLabel(key);
                    return;
                }
            }
            points.add(new Point(x, y, timeMs));
            addKeyLabel(key);
        }

        private void addKeyLabel(GestureKey key) {
            String label = key == null ? null : SwipeTrace.normalizeKeyLabel(key.label);
            if (label == null) {
                return;
            }
            if (!keyLabels.isEmpty() && label.equals(keyLabels.get(keyLabels.size() - 1))) {
                return;
            }
            keyLabels.add(label);
        }

        SwipeTrace build() {
            return new SwipeTrace(points, keyLabels);
        }
    }
}
