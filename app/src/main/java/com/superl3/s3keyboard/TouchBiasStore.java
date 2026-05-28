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
    static final String TYPING_EVENT_JOURNAL = "typing_event_journal_v1";
    static final String DINGUL_TOUCH_PROFILE = "dingul_touch_profile_v1";
    static final float MAX_BIAS_DP = 6f;
    static final int MAX_GESTURE_THRESHOLD_ADJUSTMENT_DP = 6;
    static final int MAX_GLOBAL_GESTURE_THRESHOLD_ADJUSTMENT_DP = 4;
    static final int MAX_DINGUL_GESTURE_PENALTY_DP = 8;
    static final int MAX_TYPING_PATTERN_EVENTS = 240;
    private static final float LEARNING_RATE = 0.05f;
    private static final float GESTURE_LEARNING_RATE = 0.25f;

    private final SharedPreferences preferences;

    TouchBiasStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    Bias load() {
        return Bias.decode(preferences.getString(TOUCH_BIAS_STATS, ""));
    }

    DingulTouchProfile loadDingulTouchProfile() {
        return DingulTouchProfile.decode(preferences.getString(DINGUL_TOUCH_PROFILE, ""));
    }

    TypingEventJournal.CorrectionStats loadTypingCorrectionStats() {
        return TypingEventJournal.correctionStats(preferences.getString(TYPING_EVENT_JOURNAL, ""));
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

    void recordDingulTextInput(String keyCodePoints, GestureAction action) {
        DingulTouchProfile next = loadDingulTouchProfile().recordInput(keyCodePoints, action);
        preferences.edit()
                .putString(DINGUL_TOUCH_PROFILE, next.encode())
                .apply();
    }

    void recordDingulCorrection(
            String keyCodePoints,
            GestureAction action,
            float offsetXDp,
            float offsetYDp) {
        DingulTouchProfile next = loadDingulTouchProfile()
                .recordCorrection(keyCodePoints, action, offsetXDp, offsetYDp);
        preferences.edit()
                .putString(DINGUL_TOUCH_PROFILE, next.encode())
                .apply();
    }

    int dingulPenaltyDp(String keyCodePoints, GestureAction action) {
        return loadDingulTouchProfile().penaltyDp(keyCodePoints, action);
    }

    void recordTypingJournalInput(TypingEventJournal.Input input) {
        if (input == null) {
            return;
        }
        preferences.edit()
                .putString(
                        TYPING_EVENT_JOURNAL,
                        TypingEventJournal.appendInput(
                                preferences.getString(TYPING_EVENT_JOURNAL, ""),
                                input,
                                MAX_TYPING_PATTERN_EVENTS))
                .apply();
    }

    void recordTypingJournalDelete(long timeMs) {
        preferences.edit()
                .putString(
                        TYPING_EVENT_JOURNAL,
                        TypingEventJournal.appendDelete(
                                preferences.getString(TYPING_EVENT_JOURNAL, ""),
                                timeMs,
                                MAX_TYPING_PATTERN_EVENTS))
                .apply();
    }

    static void reset(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TOUCH_BIAS_STATS)
                .remove(TYPING_PATTERN_LOG)
                .remove(TYPING_EVENT_JOURNAL)
                .remove(DINGUL_TOUCH_PROFILE)
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

    static final class DingulTouchProfile {
        private static final int VERSION = 1;
        private static final int MAX_PROFILE_SAMPLES = 200;
        private static final float OFFSET_EMA_ALPHA = 0.25f;
        private static final String ENTRIES = "entries";
        private static final String INPUT_COUNT = "inputCount";
        private static final String CORRECTION_COUNT = "correctionCount";
        private static final String OFFSET_X_DP = "offsetXDp";
        private static final String OFFSET_Y_DP = "offsetYDp";

        private final JSONObject root;
        private final JSONObject entries;

        private DingulTouchProfile(JSONObject root) {
            this.root = root == null ? new JSONObject() : root;
            JSONObject loadedEntries = this.root.optJSONObject(ENTRIES);
            this.entries = loadedEntries == null ? new JSONObject() : loadedEntries;
            try {
                this.root.put("version", VERSION);
                this.root.put(ENTRIES, this.entries);
            } catch (JSONException exception) {
                // Keep the in-memory profile usable even if the version marker fails.
            }
        }

        static DingulTouchProfile empty() {
            return new DingulTouchProfile(new JSONObject());
        }

        static DingulTouchProfile decode(String encoded) {
            if (encoded == null || encoded.isEmpty()) {
                return empty();
            }
            try {
                return new DingulTouchProfile(new JSONObject(encoded));
            } catch (JSONException exception) {
                return empty();
            }
        }

        DingulTouchProfile recordInput(String keyCodePoints, GestureAction action) {
            String id = entryId(keyCodePoints, action);
            if (id.isEmpty()) {
                return this;
            }
            JSONObject entry = entryFor(id);
            decayIfNeeded(entry);
            putInt(entry, INPUT_COUNT, entry.optInt(INPUT_COUNT, 0) + 1);
            putEntry(id, entry);
            return this;
        }

        DingulTouchProfile recordCorrection(
                String keyCodePoints,
                GestureAction action,
                float offsetXDp,
                float offsetYDp) {
            String id = entryId(keyCodePoints, action);
            if (id.isEmpty()) {
                return this;
            }
            JSONObject entry = entryFor(id);
            decayIfNeeded(entry);
            if (entry.optInt(INPUT_COUNT, 0) <= 0) {
                putInt(entry, INPUT_COUNT, 1);
            }
            putInt(entry, CORRECTION_COUNT, entry.optInt(CORRECTION_COUNT, 0) + 1);
            putFloat(entry, OFFSET_X_DP, ema(entry, OFFSET_X_DP, offsetXDp));
            putFloat(entry, OFFSET_Y_DP, ema(entry, OFFSET_Y_DP, offsetYDp));
            putEntry(id, entry);
            return this;
        }

        int penaltyDp(String keyCodePoints, GestureAction action) {
            if (!isDirectional(action)) {
                return 0;
            }
            String id = entryId(keyCodePoints, action);
            if (id.isEmpty()) {
                return 0;
            }
            JSONObject entry = entries.optJSONObject(id);
            if (entry == null) {
                return 0;
            }
            int inputCount = Math.max(1, entry.optInt(INPUT_COUNT, 0));
            int correctionCount = entry.optInt(CORRECTION_COUNT, 0);
            if (correctionCount < 2) {
                return 0;
            }
            int ratePermille = Math.min(1000, Math.round((correctionCount * 1000f) / inputCount));
            if (correctionCount >= 8 && ratePermille >= 650) {
                return MAX_DINGUL_GESTURE_PENALTY_DP;
            }
            if (correctionCount >= 5 && ratePermille >= 500) {
                return 6;
            }
            if (correctionCount >= 3 && ratePermille >= 350) {
                return 4;
            }
            if (ratePermille >= 200) {
                return 2;
            }
            return 0;
        }

        int inputCount(String keyCodePoints, GestureAction action) {
            JSONObject entry = entries.optJSONObject(entryId(keyCodePoints, action));
            return entry == null ? 0 : entry.optInt(INPUT_COUNT, 0);
        }

        int correctionCount(String keyCodePoints, GestureAction action) {
            JSONObject entry = entries.optJSONObject(entryId(keyCodePoints, action));
            return entry == null ? 0 : entry.optInt(CORRECTION_COUNT, 0);
        }

        String encode() {
            return root.toString();
        }

        private JSONObject entryFor(String id) {
            JSONObject entry = entries.optJSONObject(id);
            return entry == null ? new JSONObject() : entry;
        }

        private void putEntry(String id, JSONObject entry) {
            try {
                entries.put(id, entry);
            } catch (JSONException exception) {
                // Ignore a malformed id; callers will fall back to no penalty.
            }
        }

        private static String entryId(String keyCodePoints, GestureAction action) {
            if (keyCodePoints == null || keyCodePoints.isEmpty()) {
                return "";
            }
            return keyCodePoints + "|" + safeAction(action).name();
        }

        private static GestureAction safeAction(GestureAction action) {
            return action == null ? GestureAction.TAP : action;
        }

        private static boolean isDirectional(GestureAction action) {
            return action == GestureAction.UP
                    || action == GestureAction.DOWN
                    || action == GestureAction.LEFT
                    || action == GestureAction.RIGHT;
        }

        private static void decayIfNeeded(JSONObject entry) {
            int inputCount = entry.optInt(INPUT_COUNT, 0);
            int correctionCount = entry.optInt(CORRECTION_COUNT, 0);
            if (Math.max(inputCount, correctionCount) < MAX_PROFILE_SAMPLES) {
                return;
            }
            putInt(entry, INPUT_COUNT, Math.max(1, inputCount / 2));
            putInt(entry, CORRECTION_COUNT, correctionCount / 2);
        }

        private static float ema(JSONObject entry, String key, float value) {
            if (!entry.has(key)) {
                return value;
            }
            float previous = (float) entry.optDouble(key, value);
            return previous + (value - previous) * OFFSET_EMA_ALPHA;
        }

        private static void putInt(JSONObject object, String key, int value) {
            try {
                object.put(key, value);
            } catch (JSONException exception) {
                // Ignore malformed in-memory state.
            }
        }

        private static void putFloat(JSONObject object, String key, float value) {
            try {
                object.put(key, value);
            } catch (JSONException exception) {
                // Ignore malformed in-memory state.
            }
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
                    Math.min(MAX_GLOBAL_GESTURE_THRESHOLD_ADJUSTMENT_DP, gestureThresholdAdjustmentDp));
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
                            MAX_GLOBAL_GESTURE_THRESHOLD_ADJUSTMENT_DP,
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
            boolean acceptedSlide = action != null
                    && action != GestureAction.TAP
                    && action != GestureAction.LONG_PRESS;
            int nextGlobal = acceptedSlide ? Math.max(0, gestureThresholdAdjustmentDp - 1)
                    : gestureThresholdAdjustmentDp;
            int nextUp = gestureUpAdjDp;
            int nextDown = gestureDownAdjDp;
            int nextLeft = gestureLeftAdjDp;
            int nextRight = gestureRightAdjDp;
            if (acceptedSlide) {
                switch (action) {
                    case UP:
                        nextUp = Math.max(0, nextUp - 1);
                        break;
                    case DOWN:
                        nextDown = Math.max(0, nextDown - 1);
                        break;
                    case LEFT:
                        nextLeft = Math.max(0, nextLeft - 1);
                        break;
                    case RIGHT:
                        nextRight = Math.max(0, nextRight - 1);
                        break;
                    default:
                        break;
                }
            }
            return new Bias(
                    xDp,
                    yDp,
                    samples,
                    nextGlobal,
                    gestureSamples,
                    nextUp,
                    nextDown,
                    nextLeft,
                    nextRight,
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
