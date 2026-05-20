package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class TouchBiasStore {
    private static final String PREF_NAME = "keyboard_preferences";
    static final String TOUCH_BIAS_STATS = "touch_bias_stats";
    static final String TYPING_PATTERN_LOG = "typing_pattern_log";
    static final float MAX_BIAS_DP = 6f;
    static final int MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP = 10;
    static final int MAX_TYPING_PATTERN_EVENTS = 240;
    private static final float LEARNING_RATE = 0.05f;
    private static final float GESTURE_LEARNING_RATE = 0.35f;

    private final SharedPreferences preferences;

    TouchBiasStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    Bias load() {
        return Bias.decode(preferences.getString(TOUCH_BIAS_STATS, ""));
    }

    void recordImmediateDelete(float touchOffsetXDp, float touchOffsetYDp, GestureAction action) {
        recordImmediateDelete(touchOffsetXDp, touchOffsetYDp, action, "");
    }

    void recordImmediateDelete(
            float touchOffsetXDp,
            float touchOffsetYDp,
            GestureAction action,
            String correctedText) {
        Bias next = load().recordImmediateDelete(touchOffsetXDp, touchOffsetYDp, action);
        preferences.edit()
                .putString(TOUCH_BIAS_STATS, next.encode())
                .putString(
                        TYPING_PATTERN_LOG,
                        appendTypingEvent(
                                preferences.getString(TYPING_PATTERN_LOG, ""),
                                "correction",
                                correctedText,
                                action,
                                touchOffsetXDp,
                                touchOffsetYDp))
                .apply();
    }

    void recordTextInput(GestureAction action) {
        recordTextInput("", action);
    }

    void recordTextInput(String text, GestureAction action) {
        Bias next = load().recordTextInput(action);
        preferences.edit()
                .putString(TOUCH_BIAS_STATS, next.encode())
                .putString(
                        TYPING_PATTERN_LOG,
                        appendTypingEvent(
                                preferences.getString(TYPING_PATTERN_LOG, ""),
                                "input",
                                text,
                                action,
                                0f,
                                0f))
                .apply();
    }

    static void reset(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TOUCH_BIAS_STATS)
                .remove(TYPING_PATTERN_LOG)
                .apply();
    }

    static String appendTypingEvent(
            String encodedLog,
            String type,
            String text,
            GestureAction action,
            float offsetXDp,
            float offsetYDp) {
        try {
            JSONArray events = encodedLog == null || encodedLog.isEmpty()
                    ? new JSONArray()
                    : new JSONArray(encodedLog);
            JSONObject event = new JSONObject();
            event.put("timeMs", System.currentTimeMillis());
            event.put("type", type == null ? "" : type);
            event.put("text", text == null ? "" : text);
            event.put("action", action == null ? GestureAction.TAP.name() : action.name());
            if ("correction".equals(type)) {
                event.put("offsetXDp", offsetXDp);
                event.put("offsetYDp", offsetYDp);
            }
            events.put(event);
            while (events.length() > MAX_TYPING_PATTERN_EVENTS) {
                events.remove(0);
            }
            return events.toString();
        } catch (JSONException exception) {
            return appendTypingEvent("", type, text, action, offsetXDp, offsetYDp);
        }
    }

    static final class Bias {
        final float xDp;
        final float yDp;
        final int samples;
        final int gestureThresholdAdjustmentDp;
        final int gestureSamples;
        final int gestureUpAdjDp;
        final int gestureDownAdjDp;
        final int gestureLeftAdjDp;
        final int gestureRightAdjDp;
        final int textSamples;
        final int correctionSamples;

        Bias(float xDp, float yDp, int samples) {
            this(xDp, yDp, samples, 0, 0, 0, 0, 0, 0);
        }

        Bias(
                float xDp,
                float yDp,
                int samples,
                int gestureThresholdAdjustmentDp,
                int gestureSamples) {
            this(xDp, yDp, samples, gestureThresholdAdjustmentDp, gestureSamples, 0, 0, 0, 0);
        }

        Bias(
                float xDp,
                float yDp,
                int samples,
                int gestureThresholdAdjustmentDp,
                int gestureSamples,
                int gestureUpAdjDp,
                int gestureDownAdjDp,
                int gestureLeftAdjDp,
                int gestureRightAdjDp) {
            this(
                    xDp,
                    yDp,
                    samples,
                    gestureThresholdAdjustmentDp,
                    gestureSamples,
                    gestureUpAdjDp,
                    gestureDownAdjDp,
                    gestureLeftAdjDp,
                    gestureRightAdjDp,
                    samples,
                    samples);
        }

        Bias(
                float xDp,
                float yDp,
                int samples,
                int gestureThresholdAdjustmentDp,
                int gestureSamples,
                int gestureUpAdjDp,
                int gestureDownAdjDp,
                int gestureLeftAdjDp,
                int gestureRightAdjDp,
                int textSamples,
                int correctionSamples) {
            this.xDp = clamp(xDp);
            this.yDp = clamp(yDp);
            this.samples = Math.max(0, samples);
            this.gestureThresholdAdjustmentDp = Math.max(
                    0,
                    Math.min(MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP, gestureThresholdAdjustmentDp));
            this.gestureSamples = Math.max(0, gestureSamples);
            this.gestureUpAdjDp = clampAdj(gestureUpAdjDp);
            this.gestureDownAdjDp = clampAdj(gestureDownAdjDp);
            this.gestureLeftAdjDp = clampAdj(gestureLeftAdjDp);
            this.gestureRightAdjDp = clampAdj(gestureRightAdjDp);
            this.textSamples = Math.max(0, textSamples);
            this.correctionSamples = Math.max(0, correctionSamples);
        }

        static Bias none() {
            return new Bias(0f, 0f, 0);
        }

        Bias recordImmediateDelete(float touchOffsetXDp, float touchOffsetYDp, GestureAction action) {
            boolean deletedSlide = action != null
                    && action != GestureAction.TAP
                    && action != GestureAction.LONG_PRESS;
            int nextGestureSamples = gestureSamples + (deletedSlide ? 1 : 0);
            int nextGestureThresholdAdjustmentDp = deletedSlide
                    ? Math.min(
                            MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP,
                            Math.round(nextGestureSamples * GESTURE_LEARNING_RATE))
                    : gestureThresholdAdjustmentDp;
            int nextUp = gestureUpAdjDp + (action == GestureAction.UP ? 1 : 0);
            int nextDown = gestureDownAdjDp + (action == GestureAction.DOWN ? 1 : 0);
            int nextLeft = gestureLeftAdjDp + (action == GestureAction.LEFT ? 1 : 0);
            int nextRight = gestureRightAdjDp + (action == GestureAction.RIGHT ? 1 : 0);
            return new Bias(
                    xDp - touchOffsetXDp * LEARNING_RATE,
                    yDp - touchOffsetYDp * LEARNING_RATE,
                    samples + 1,
                    nextGestureThresholdAdjustmentDp,
                    nextGestureSamples,
                    nextUp,
                    nextDown,
                    nextLeft,
                    nextRight,
                    textSamples,
                    correctionSamples + 1);
        }

        Bias recordTextInput(GestureAction action) {
            return new Bias(
                    xDp,
                    yDp,
                    samples,
                    gestureThresholdAdjustmentDp,
                    gestureSamples,
                    gestureUpAdjDp,
                    gestureDownAdjDp,
                    gestureLeftAdjDp,
                    gestureRightAdjDp,
                    textSamples + 1,
                    correctionSamples);
        }

        int correctionRatePermille() {
            if (textSamples <= 0) {
                return 0;
            }
            return Math.min(1000, Math.round((correctionSamples * 1000f) / textSamples));
        }

        String encode() {
            return xDp + "," + yDp + "," + samples
                    + "," + gestureThresholdAdjustmentDp + "," + gestureSamples
                    + "," + gestureUpAdjDp + "," + gestureDownAdjDp
                    + "," + gestureLeftAdjDp + "," + gestureRightAdjDp
                    + "," + textSamples + "," + correctionSamples;
        }

        static Bias decode(String encoded) {
            if (encoded == null || encoded.isEmpty()) {
                return none();
            }
            String[] parts = encoded.split(",");
            if (parts.length != 3 && parts.length != 5 && parts.length != 9 && parts.length != 11) {
                return none();
            }
            try {
                int thresholdAdjustment = parts.length >= 5 ? Integer.parseInt(parts[3]) : 0;
                int gestureSamples = parts.length >= 5 ? Integer.parseInt(parts[4]) : 0;
                int up = parts.length == 9 ? Integer.parseInt(parts[5]) : 0;
                int down = parts.length == 9 ? Integer.parseInt(parts[6]) : 0;
                int left = parts.length == 9 ? Integer.parseInt(parts[7]) : 0;
                int right = parts.length == 9 ? Integer.parseInt(parts[8]) : 0;
                if (parts.length == 11) {
                    up = Integer.parseInt(parts[5]);
                    down = Integer.parseInt(parts[6]);
                    left = Integer.parseInt(parts[7]);
                    right = Integer.parseInt(parts[8]);
                }
                int samples = Integer.parseInt(parts[2]);
                int textSamples = parts.length == 11 ? Integer.parseInt(parts[9]) : samples;
                int correctionSamples = parts.length == 11 ? Integer.parseInt(parts[10]) : samples;
                return new Bias(
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        samples,
                        thresholdAdjustment,
                        gestureSamples,
                        up,
                        down,
                        left,
                        right,
                        textSamples,
                        correctionSamples);
            } catch (NumberFormatException ex) {
                return none();
            }
        }

        int gestureThresholdAdjustmentForDirection(GestureAction action) {
            if (action == null) {
                return gestureThresholdAdjustmentDp;
            }
            switch (action) {
                case UP: return Math.max(gestureThresholdAdjustmentDp, gestureUpAdjDp);
                case DOWN: return Math.max(gestureThresholdAdjustmentDp, gestureDownAdjDp);
                case LEFT: return Math.max(gestureThresholdAdjustmentDp, gestureLeftAdjDp);
                case RIGHT: return Math.max(gestureThresholdAdjustmentDp, gestureRightAdjDp);
                default: return gestureThresholdAdjustmentDp;
            }
        }

        private static float clamp(float value) {
            return Math.max(-MAX_BIAS_DP, Math.min(MAX_BIAS_DP, value));
        }

        private static int clampAdj(int value) {
            return Math.max(0, Math.min(MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP, value));
        }
    }
}
