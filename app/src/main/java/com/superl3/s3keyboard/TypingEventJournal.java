package com.superl3.s3keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class TypingEventJournal {
    private static final String TYPE = "type";
    private static final String TYPE_INPUT = "input";
    private static final String TYPE_DELETE = "delete";
    private static final String TYPE_LABEL = "label";
    private static final String ID = "id";
    private static final String TIME_MS = "timeMs";
    private static final String TARGET_EVENT_ID = "targetEventId";
    private static final String REPLACEMENT_EVENT_ID = "replacementEventId";
    private static final String LABEL = "label";
    private static final String KEY_CP = "keyCp";
    private static final String VALUE_CP = "valueCp";
    private static final String ACTION = "action";
    private static final String SHADOW_ACTION = "shadowAction";
    private static final String SHADOW_KEY_CP = "shadowKeyCp";
    private static final String SHADOW_APPLIED = "shadowApplied";
    private static final int ACCEPTED_INPUT_HORIZON = 3;
    private static final float SAME_KEY_ACTIVE_SCORE_FLOOR = 0.80f;
    private static final float REASSIGNED_ACTIVE_SCORE_FLOOR = 0.96f;

    private TypingEventJournal() {
    }

    static String appendInput(String encodedJournal, Input input, int maxEvents) {
        if (input == null || input.id.isEmpty()) {
            return encodedJournal == null ? "" : encodedJournal;
        }
        JSONArray events = decode(encodedJournal);
        events.put(input.toJson());
        appendReplacementLabel(events, input);
        appendAcceptedLabels(events);
        trim(events, maxEvents);
        return events.toString();
    }

    static String appendDelete(String encodedJournal, long timeMs, int maxEvents) {
        JSONArray events = decode(encodedJournal);
        JSONObject event = new JSONObject();
        put(event, TYPE, TYPE_DELETE);
        put(event, ID, "d-" + Math.max(0L, timeMs) + "-" + events.length());
        put(event, TIME_MS, Math.max(0L, timeMs));
        String targetEventId = lastLiveInputId(events);
        if (!targetEventId.isEmpty()) {
            put(event, TARGET_EVENT_ID, targetEventId);
        }
        events.put(event);
        trim(events, maxEvents);
        return events.toString();
    }

    static Label latestLabelFor(String encodedJournal, String targetEventId) {
        JSONArray events = decode(encodedJournal);
        Label latest = null;
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null || !TYPE_LABEL.equals(event.optString(TYPE))) {
                continue;
            }
            if (safe(targetEventId).equals(event.optString(TARGET_EVENT_ID))) {
                latest = Label.fromId(event.optString(LABEL));
            }
        }
        return latest;
    }

    static int labelCount(String encodedJournal, Label label) {
        JSONArray events = decode(encodedJournal);
        int count = 0;
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null
                    && TYPE_LABEL.equals(event.optString(TYPE))
                    && label.id.equals(event.optString(LABEL))) {
                count++;
            }
        }
        return count;
    }

    static CorrectionStats correctionStats(String encodedJournal) {
        JSONArray events = decode(encodedJournal);
        Map<String, JSONObject> inputs = new HashMap<>();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null && TYPE_INPUT.equals(event.optString(TYPE))) {
                String id = event.optString(ID);
                if (!id.isEmpty()) {
                    inputs.put(id, event);
                }
            }
        }

        CorrectionStats stats = new CorrectionStats();
        for (int i = 0; i < events.length(); i++) {
            JSONObject labelEvent = events.optJSONObject(i);
            if (labelEvent == null || !TYPE_LABEL.equals(labelEvent.optString(TYPE))) {
                continue;
            }
            Label label = Label.fromId(labelEvent.optString(LABEL));
            if (label == null) {
                continue;
            }
            JSONObject target = inputs.get(labelEvent.optString(TARGET_EVENT_ID));
            JSONObject replacement = inputs.get(labelEvent.optString(REPLACEMENT_EVENT_ID));
            stats.record(label, target, replacement);
        }
        return stats;
    }

    private static JSONArray decode(String encodedJournal) {
        if (encodedJournal == null || encodedJournal.isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(encodedJournal);
        } catch (JSONException exception) {
            return new JSONArray();
        }
    }

    private static void appendReplacementLabel(JSONArray events, Input replacement) {
        JSONObject original = deepestPendingDeletedInput(events);
        if (original == null) {
            return;
        }
        String originalId = original.optString(ID);
        if (originalId.isEmpty()) {
            return;
        }
        appendLabel(
                events,
                originalId,
                replacement.id,
                classifyReplacement(original, replacement),
                "rollback_replacement");
    }

    private static JSONObject deepestPendingDeletedInput(JSONArray events) {
        List<String> pendingTargets = new ArrayList<>();
        Set<String> consumedTargets = replacementLabelTargetIds(events);
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null || !TYPE_DELETE.equals(event.optString(TYPE))) {
                continue;
            }
            String target = event.optString(TARGET_EVENT_ID);
            if (!target.isEmpty() && !consumedTargets.contains(target)) {
                pendingTargets.add(target);
            }
        }
        for (int i = pendingTargets.size() - 1; i >= 0; i--) {
            JSONObject input = inputById(events, pendingTargets.get(i));
            if (input != null) {
                return input;
            }
        }
        return null;
    }

    private static void appendAcceptedLabels(JSONArray events) {
        Set<String> labelledTargets = allLabelTargetIds(events);
        Set<String> deletedTargets = deleteTargetIds(events);
        List<JSONObject> labelsToAppend = new ArrayList<>();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null || !TYPE_INPUT.equals(event.optString(TYPE))) {
                continue;
            }
            String id = event.optString(ID);
            if (id.isEmpty() || labelledTargets.contains(id) || deletedTargets.contains(id)) {
                continue;
            }
            if (subsequentInputCount(events, i) < ACCEPTED_INPUT_HORIZON) {
                continue;
            }
            Label label = hasUnappliedShadow(event)
                    ? Label.SHADOW_FALSE_ALARM
                    : acceptedLabel(actionFrom(event.optString(ACTION)));
            labelsToAppend.add(labelEvent(id, "", label, "continued_typing"));
            labelledTargets.add(id);
        }
        for (JSONObject label : labelsToAppend) {
            events.put(label);
        }
    }

    private static Label classifyReplacement(JSONObject original, Input replacement) {
        GestureAction originalAction = actionFrom(original.optString(ACTION));
        GestureAction replacementAction = safeAction(replacement.action);
        boolean originalDirectional = isDirectional(originalAction);
        boolean replacementDirectional = isDirectional(replacementAction);
        boolean sameKey = original.optString(KEY_CP).equals(replacement.keyCodePoints);
        if (originalAction == GestureAction.TAP && replacementDirectional) {
            return Label.MISSED_SLIDE;
        }
        if (originalDirectional && replacementAction == GestureAction.TAP) {
            return Label.FALSE_SLIDE;
        }
        if (!sameKey) {
            return Label.WRONG_ORIGIN_KEY;
        }
        if (originalDirectional && replacementDirectional && originalAction != replacementAction) {
            return Label.WRONG_DIRECTION;
        }
        return Label.UNKNOWN_CORRECTION;
    }

    private static Label acceptedLabel(GestureAction action) {
        return isDirectional(action) ? Label.ACCEPTED_SLIDE : Label.ACCEPTED_TAP;
    }

    private static boolean hasUnappliedShadow(JSONObject event) {
        return !event.optBoolean(SHADOW_APPLIED, false)
                && !event.optString(SHADOW_ACTION).isEmpty()
                && !GestureAction.TAP.name().equals(event.optString(SHADOW_ACTION));
    }

    private static int subsequentInputCount(JSONArray events, int index) {
        int count = 0;
        for (int i = index + 1; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null && TYPE_INPUT.equals(event.optString(TYPE))) {
                count++;
            }
        }
        return count;
    }

    private static String lastLiveInputId(JSONArray events) {
        Set<String> deletedTargets = deleteTargetIds(events);
        for (int i = events.length() - 1; i >= 0; i--) {
            JSONObject event = events.optJSONObject(i);
            if (event == null || !TYPE_INPUT.equals(event.optString(TYPE))) {
                continue;
            }
            String id = event.optString(ID);
            if (!id.isEmpty() && !deletedTargets.contains(id)) {
                return id;
            }
        }
        return "";
    }

    private static JSONObject inputById(JSONArray events, String id) {
        for (int i = events.length() - 1; i >= 0; i--) {
            JSONObject event = events.optJSONObject(i);
            if (event != null
                    && TYPE_INPUT.equals(event.optString(TYPE))
                    && safe(id).equals(event.optString(ID))) {
                return event;
            }
        }
        return null;
    }

    private static Set<String> deleteTargetIds(JSONArray events) {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null && TYPE_DELETE.equals(event.optString(TYPE))) {
                String target = event.optString(TARGET_EVENT_ID);
                if (!target.isEmpty()) {
                    ids.add(target);
                }
            }
        }
        return ids;
    }

    private static Set<String> replacementLabelTargetIds(JSONArray events) {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null
                    && TYPE_LABEL.equals(event.optString(TYPE))
                    && !event.optString(REPLACEMENT_EVENT_ID).isEmpty()) {
                String target = event.optString(TARGET_EVENT_ID);
                if (!target.isEmpty()) {
                    ids.add(target);
                }
            }
        }
        return ids;
    }

    private static Set<String> allLabelTargetIds(JSONArray events) {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event != null && TYPE_LABEL.equals(event.optString(TYPE))) {
                String target = event.optString(TARGET_EVENT_ID);
                if (!target.isEmpty()) {
                    ids.add(target);
                }
            }
        }
        return ids;
    }

    private static void appendLabel(
            JSONArray events,
            String targetEventId,
            String replacementEventId,
            Label label,
            String source) {
        events.put(labelEvent(targetEventId, replacementEventId, label, source));
    }

    private static JSONObject labelEvent(
            String targetEventId,
            String replacementEventId,
            Label label,
            String source) {
        JSONObject event = new JSONObject();
        put(event, TYPE, TYPE_LABEL);
        put(event, TARGET_EVENT_ID, safe(targetEventId));
        put(event, REPLACEMENT_EVENT_ID, safe(replacementEventId));
        put(event, LABEL, label == null ? Label.UNKNOWN_CORRECTION.id : label.id);
        put(event, "source", safe(source));
        return event;
    }

    private static void trim(JSONArray events, int maxEvents) {
        int limit = Math.max(1, maxEvents);
        while (events.length() > limit) {
            events.remove(0);
        }
    }

    private static GestureAction actionFrom(String action) {
        if (action == null || action.isEmpty()) {
            return GestureAction.TAP;
        }
        try {
            return GestureAction.valueOf(action);
        } catch (IllegalArgumentException exception) {
            return GestureAction.TAP;
        }
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException exception) {
            // Keep journaling best-effort; input handling must never depend on logs.
        }
    }

    enum Label {
        ACCEPTED_TAP("accepted_tap"),
        ACCEPTED_SLIDE("accepted_slide"),
        MISSED_SLIDE("missed_slide"),
        FALSE_SLIDE("false_slide"),
        WRONG_DIRECTION("wrong_direction"),
        WRONG_ORIGIN_KEY("wrong_origin_key"),
        UNKNOWN_CORRECTION("unknown_correction"),
        SHADOW_FALSE_ALARM("shadow_false_alarm");

        final String id;

        Label(String id) {
            this.id = id;
        }

        static Label fromId(String id) {
            for (Label label : values()) {
                if (label.id.equals(id)) {
                    return label;
                }
            }
            return null;
        }
    }

    static final class CorrectionStats {
        private static final int MAX_THRESHOLD_DISCOUNT_DP = 4;
        private static final int MAX_THRESHOLD_PENALTY_DP = 4;

        private final Map<String, CandidateStats> candidates = new HashMap<>();

        boolean shouldApplyActiveSlide(
                String originKeyCodePoints,
                String candidateKeyCodePoints,
                GestureAction action,
                float score,
                boolean sameKey) {
            if (!isDirectional(action)) {
                return false;
            }
            CandidateStats stats = candidate(originKeyCodePoints, candidateKeyCodePoints, action);
            if (stats.missedSlideCount < 2) {
                return false;
            }
            int negative = stats.shadowFalseAlarmCount + stats.falseSlideCount;
            if (negative > 0 && negative * 2 >= stats.missedSlideCount) {
                return false;
            }
            float floor = sameKey ? SAME_KEY_ACTIVE_SCORE_FLOOR : REASSIGNED_ACTIVE_SCORE_FLOOR;
            if (stats.missedSlideCount >= 4 && negative <= 1) {
                floor -= 0.10f;
            }
            if (stats.acceptedSlideCount >= 3 && negative == 0) {
                floor -= 0.05f;
            }
            return score >= floor;
        }

        int thresholdAdjustmentDp(String keyCodePoints, GestureAction action) {
            if (!isDirectional(action)) {
                return 0;
            }
            CandidateStats stats = candidate(keyCodePoints, keyCodePoints, action);
            int negative = stats.shadowFalseAlarmCount + stats.falseSlideCount;
            if (stats.falseSlideCount >= 2 && stats.falseSlideCount >= stats.missedSlideCount) {
                return Math.min(MAX_THRESHOLD_PENALTY_DP, stats.falseSlideCount);
            }
            if (stats.missedSlideCount < 2 || negative * 2 >= stats.missedSlideCount) {
                return 0;
            }
            int discount = stats.missedSlideCount >= 4 ? 3 : 2;
            if (stats.missedSlideCount >= 7 && negative == 0) {
                discount = MAX_THRESHOLD_DISCOUNT_DP;
            }
            return -discount;
        }

        int missedSlideCount(String originKeyCodePoints, String candidateKeyCodePoints, GestureAction action) {
            return candidate(originKeyCodePoints, candidateKeyCodePoints, action).missedSlideCount;
        }

        int shadowFalseAlarmCount(
                String originKeyCodePoints,
                String candidateKeyCodePoints,
                GestureAction action) {
            return candidate(originKeyCodePoints, candidateKeyCodePoints, action).shadowFalseAlarmCount;
        }

        private void record(Label label, JSONObject target, JSONObject replacement) {
            if (target == null) {
                return;
            }
            switch (label) {
                case MISSED_SLIDE:
                    if (replacement != null) {
                        candidate(
                                target.optString(KEY_CP),
                                replacement.optString(KEY_CP),
                                actionFrom(replacement.optString(ACTION))).missedSlideCount++;
                    }
                    break;
                case FALSE_SLIDE:
                    candidate(
                            target.optString(KEY_CP),
                            target.optString(KEY_CP),
                            actionFrom(target.optString(ACTION))).falseSlideCount++;
                    break;
                case WRONG_DIRECTION:
                    candidate(
                            target.optString(KEY_CP),
                            target.optString(KEY_CP),
                            actionFrom(target.optString(ACTION))).wrongDirectionCount++;
                    if (replacement != null) {
                        candidate(
                                target.optString(KEY_CP),
                                replacement.optString(KEY_CP),
                                actionFrom(replacement.optString(ACTION))).missedSlideCount++;
                    }
                    break;
                case WRONG_ORIGIN_KEY:
                    if (replacement != null) {
                        candidate(
                                target.optString(KEY_CP),
                                replacement.optString(KEY_CP),
                                actionFrom(replacement.optString(ACTION))).wrongOriginCount++;
                    }
                    break;
                case SHADOW_FALSE_ALARM:
                    candidate(
                            target.optString(KEY_CP),
                            target.optString(SHADOW_KEY_CP),
                            actionFrom(target.optString(SHADOW_ACTION))).shadowFalseAlarmCount++;
                    break;
                case ACCEPTED_SLIDE:
                    candidate(
                            target.optString(KEY_CP),
                            target.optString(KEY_CP),
                            actionFrom(target.optString(ACTION))).acceptedSlideCount++;
                    break;
                case ACCEPTED_TAP:
                case UNKNOWN_CORRECTION:
                default:
                    break;
            }
        }

        private CandidateStats candidate(
                String originKeyCodePoints,
                String candidateKeyCodePoints,
                GestureAction action) {
            String id = candidateId(originKeyCodePoints, candidateKeyCodePoints, action);
            CandidateStats stats = candidates.get(id);
            if (stats == null) {
                stats = new CandidateStats();
                candidates.put(id, stats);
            }
            return stats;
        }

        private static String candidateId(
                String originKeyCodePoints,
                String candidateKeyCodePoints,
                GestureAction action) {
            return safe(originKeyCodePoints) + "|"
                    + safe(candidateKeyCodePoints) + "|"
                    + safeAction(action).name();
        }
    }

    private static final class CandidateStats {
        int missedSlideCount;
        int falseSlideCount;
        int wrongDirectionCount;
        int wrongOriginCount;
        int shadowFalseAlarmCount;
        int acceptedSlideCount;
    }

    static final class Input {
        final String id;
        final long timeMs;
        final KeyboardMode keyboardMode;
        final String keyCodePoints;
        final String valueCodePoints;
        final GestureAction action;
        final GestureAction fallbackAction;
        final float downXDp;
        final float downYDp;
        final float upXDp;
        final float upYDp;
        final long durationMs;
        final int thresholdDp;
        final int hitSlopDp;
        final int keyGapDp;
        final int touchYOffsetDp;
        final float biasXDp;
        final float biasYDp;
        final String shadowKeyCodePoints;
        final GestureAction shadowAction;
        final float shadowScore;
        final boolean shadowApplied;

        Input(
                String id,
                long timeMs,
                KeyboardMode keyboardMode,
                String keyCodePoints,
                String valueCodePoints,
                GestureAction action,
                GestureAction fallbackAction,
                float downXDp,
                float downYDp,
                float upXDp,
                float upYDp,
                long durationMs,
                int thresholdDp,
                int hitSlopDp,
                int keyGapDp,
                int touchYOffsetDp,
                float biasXDp,
                float biasYDp,
                String shadowKeyCodePoints,
                GestureAction shadowAction,
                float shadowScore,
                boolean shadowApplied) {
            this.id = safe(id);
            this.timeMs = Math.max(0L, timeMs);
            this.keyboardMode = keyboardMode == null ? KeyboardMode.HANGUL : keyboardMode;
            this.keyCodePoints = safe(keyCodePoints);
            this.valueCodePoints = safe(valueCodePoints);
            this.action = safeAction(action);
            this.fallbackAction = safeAction(fallbackAction);
            this.downXDp = downXDp;
            this.downYDp = downYDp;
            this.upXDp = upXDp;
            this.upYDp = upYDp;
            this.durationMs = Math.max(0L, durationMs);
            this.thresholdDp = Math.max(0, thresholdDp);
            this.hitSlopDp = Math.max(0, hitSlopDp);
            this.keyGapDp = Math.max(0, keyGapDp);
            this.touchYOffsetDp = touchYOffsetDp;
            this.biasXDp = biasXDp;
            this.biasYDp = biasYDp;
            this.shadowKeyCodePoints = safe(shadowKeyCodePoints);
            this.shadowAction = shadowAction;
            this.shadowScore = shadowScore;
            this.shadowApplied = shadowApplied;
        }

        JSONObject toJson() {
            JSONObject event = new JSONObject();
            put(event, TYPE, TYPE_INPUT);
            put(event, ID, id);
            put(event, TIME_MS, timeMs);
            put(event, "mode", keyboardMode.name().toLowerCase(Locale.US));
            put(event, KEY_CP, keyCodePoints);
            put(event, VALUE_CP, valueCodePoints);
            put(event, ACTION, action.name());
            put(event, "fallbackAction", fallbackAction.name());
            put(event, "downXDp", downXDp);
            put(event, "downYDp", downYDp);
            put(event, "upXDp", upXDp);
            put(event, "upYDp", upYDp);
            put(event, "dxDp", upXDp - downXDp);
            put(event, "dyDp", upYDp - downYDp);
            put(event, "durationMs", durationMs);
            put(event, "thresholdDp", thresholdDp);
            put(event, "hitSlopDp", hitSlopDp);
            put(event, "keyGapDp", keyGapDp);
            put(event, "touchYOffsetDp", touchYOffsetDp);
            put(event, "biasXDp", biasXDp);
            put(event, "biasYDp", biasYDp);
            if (shadowAction != null && shadowAction != GestureAction.TAP) {
                put(event, SHADOW_KEY_CP, shadowKeyCodePoints);
                put(event, SHADOW_ACTION, shadowAction.name());
                put(event, "shadowScore", shadowScore);
                put(event, SHADOW_APPLIED, shadowApplied);
            }
            return event;
        }
    }
}
