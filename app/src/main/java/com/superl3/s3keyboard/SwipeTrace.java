package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class SwipeTrace {
    final List<Point> points;
    final List<String> keyLabels;
    private final Bounds normalizationBounds;

    SwipeTrace(List<Point> points, List<String> keyLabels) {
        this(points, keyLabels, null);
    }

    SwipeTrace(List<Point> points, List<String> keyLabels, Bounds normalizationBounds) {
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
        this.keyLabels = Collections.unmodifiableList(new ArrayList<>(keyLabels));
        this.normalizationBounds = normalizationBounds == null
                ? observedBounds(points)
                : normalizationBounds.normalized();
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

    int pointCount() {
        return points.size();
    }

    float[] normalizedXArray() {
        float[] values = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            values[i] = normalizationBounds.normalizeX(points.get(i).x);
        }
        return values;
    }

    float[] normalizedYArray() {
        float[] values = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            values[i] = normalizationBounds.normalizeY(points.get(i).y);
        }
        return values;
    }

    float[] relativeTimeMsArray() {
        float[] values = new float[points.size()];
        long start = points.isEmpty() ? 0L : points.get(0).timeMs;
        for (int i = 0; i < points.size(); i++) {
            values[i] = Math.max(0L, points.get(i).timeMs - start);
        }
        return values;
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

    static final class Bounds {
        private static final float MIN_SPAN = 1f;
        final float left;
        final float top;
        final float right;
        final float bottom;

        Bounds(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        Bounds normalized() {
            float safeRight = right > left ? right : left + MIN_SPAN;
            float safeBottom = bottom > top ? bottom : top + MIN_SPAN;
            return new Bounds(left, top, safeRight, safeBottom);
        }

        float normalizeX(float x) {
            return clamp01((x - left) / Math.max(MIN_SPAN, right - left));
        }

        float normalizeY(float y) {
            return clamp01((y - top) / Math.max(MIN_SPAN, bottom - top));
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
    }

    static final class Builder {
        private static final float MIN_POINT_DISTANCE_PX = 3f;
        private final List<Point> points = new ArrayList<>();
        private final List<String> keyLabels = new ArrayList<>();
        private final Bounds normalizationBounds;

        Builder() {
            this.normalizationBounds = null;
        }

        Builder(float left, float top, float right, float bottom) {
            this.normalizationBounds = new Bounds(left, top, right, bottom);
        }

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
            return new SwipeTrace(points, keyLabels, normalizationBounds);
        }
    }

    private static Bounds observedBounds(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new Bounds(0f, 0f, 1f, 1f);
        }
        float left = points.get(0).x;
        float right = points.get(0).x;
        float top = points.get(0).y;
        float bottom = points.get(0).y;
        for (Point point : points) {
            left = Math.min(left, point.x);
            right = Math.max(right, point.x);
            top = Math.min(top, point.y);
            bottom = Math.max(bottom, point.y);
        }
        return new Bounds(left, top, right, bottom).normalized();
    }
}
