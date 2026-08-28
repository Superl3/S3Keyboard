package com.superl3.s3keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A compact, local-only transition model. It records statistics for shadow evaluation only;
 * candidate selection is deliberately owned by a later policy stage.
 */
final class NextKeyTouchModel {
    private static final int VERSION = 1;
    private static final String VERSION_KEY = "version";
    private static final String EPOCH_KEY = "learningEpoch";
    private static final String CONTEXTS_KEY = "contexts";
    private static final String PREVIOUS_KEY = "previousKeyId";
    private static final String PREVIOUS_ACTION = "previousAction";
    private static final String CANDIDATES_KEY = "candidates";
    private static final String NEXT_KEY = "nextKeyId";
    private static final String NEXT_ACTION = "nextAction";
    private static final String ACCEPTED_COUNT = "acceptedCount";
    private static final String CORRECTED_TO_COUNT = "correctedToCount";
    private static final String CORRECTED_FROM_COUNT = "correctedFromCount";
    private static final String LAST_UPDATED_EPOCH = "lastUpdatedEpoch";
    private static final int MAX_COUNT = 200;

    private final long learningEpoch;
    private final Map<ContextKey, Map<CandidateKey, CandidateStats>> contexts = new HashMap<>();
    private ContextKey currentContext = ContextKey.initial();

    private NextKeyTouchModel(long learningEpoch) {
        this.learningEpoch = Math.max(0L, learningEpoch);
    }

    static NextKeyTouchModel empty(long learningEpoch) {
        return new NextKeyTouchModel(learningEpoch);
    }

    static NextKeyTouchModel decode(String encoded, long learningEpoch) {
        NextKeyTouchModel model = empty(learningEpoch);
        if (encoded == null || encoded.trim().isEmpty()) {
            return model;
        }
        try {
            JSONObject root = new JSONObject(encoded);
            if (!LearningEpoch.matches(root.optLong(EPOCH_KEY, 0L), learningEpoch)) {
                return model;
            }
            JSONArray contextArray = root.optJSONArray(CONTEXTS_KEY);
            if (contextArray == null) {
                return model;
            }
            for (int i = 0; i < contextArray.length(); i++) {
                JSONObject contextObject = contextArray.optJSONObject(i);
                if (contextObject == null) {
                    continue;
                }
                ContextKey context = ContextKey.decode(contextObject);
                JSONArray candidateArray = contextObject.optJSONArray(CANDIDATES_KEY);
                if (candidateArray == null) {
                    continue;
                }
                Map<CandidateKey, CandidateStats> candidates = new HashMap<>();
                for (int j = 0; j < candidateArray.length(); j++) {
                    JSONObject candidateObject = candidateArray.optJSONObject(j);
                    CandidateStats stats = CandidateStats.decode(candidateObject);
                    if (stats != null) {
                        candidates.put(stats.key, stats);
                    }
                }
                if (!candidates.isEmpty()) {
                    model.contexts.put(context, candidates);
                }
            }
            model.currentContext = ContextKey.decode(root.optJSONObject("currentContext"));
            return model;
        } catch (JSONException | RuntimeException ignored) {
            return empty(learningEpoch);
        }
    }

    void apply(TypingEventJournal.LearningEvent event) {
        if (event == null) {
            return;
        }
        if (event.label == TypingEventJournal.Label.ACCEPTED_TAP
                || event.label == TypingEventJournal.Label.ACCEPTED_SLIDE) {
            if (!NextKeyTouchPolicy.eligibleInput(event.target)) {
                return;
            }
            CandidateKey next = CandidateKey.from(event.target);
            CandidateStats stats = statsFor(currentContext, next, true);
            if (stats == null) {
                return;
            }
            stats.acceptedCount = boundedIncrement(stats.acceptedCount);
            stats.lastUpdatedEpoch = learningEpoch;
            currentContext = ContextKey.from(event.target);
            return;
        }
        if (!NextKeyTouchPolicy.eligibleCorrection(event)) {
            return;
        }
        CandidateStats original = statsFor(currentContext, CandidateKey.from(event.target), true);
        CandidateStats replacement = statsFor(currentContext, CandidateKey.from(event.replacement), true);
        if (original == null || replacement == null) {
            return;
        }
        original.correctedFromCount = boundedIncrement(original.correctedFromCount);
        replacement.correctedToCount = boundedIncrement(replacement.correctedToCount);
        original.lastUpdatedEpoch = learningEpoch;
        replacement.lastUpdatedEpoch = learningEpoch;
    }

    CandidateStats statsFor(String previousKeyId, GestureAction previousAction,
            String nextKeyId, GestureAction nextAction) {
        ContextKey context = new ContextKey(
                safe(previousKeyId),
                safeAction(previousAction));
        CandidateKey candidate = new CandidateKey(safe(nextKeyId), safeAction(nextAction));
        Map<CandidateKey, CandidateStats> candidates = contexts.get(context);
        return candidates == null ? null : candidates.get(candidate);
    }

    int contextCount() {
        return contexts.size();
    }

    int candidateCount(String previousKeyId, GestureAction previousAction) {
        Map<CandidateKey, CandidateStats> candidates = contexts.get(new ContextKey(
                safe(previousKeyId), safeAction(previousAction)));
        return candidates == null ? 0 : candidates.size();
    }

    String encode() {
        JSONObject root = new JSONObject();
        put(root, VERSION_KEY, VERSION);
        put(root, EPOCH_KEY, learningEpoch);
        JSONArray contextArray = new JSONArray();
        List<ContextKey> orderedContexts = new ArrayList<>(contexts.keySet());
        orderedContexts.sort(ContextKey.ORDER);
        for (ContextKey context : orderedContexts) {
            JSONObject contextObject = new JSONObject();
            context.write(contextObject);
            JSONArray candidateArray = new JSONArray();
            List<CandidateStats> orderedCandidates = new ArrayList<>(contexts.get(context).values());
            orderedCandidates.sort(CandidateStats.ORDER);
            for (CandidateStats stats : orderedCandidates) {
                candidateArray.put(stats.encode());
            }
            put(contextObject, CANDIDATES_KEY, candidateArray);
            contextArray.put(contextObject);
        }
        put(root, CONTEXTS_KEY, contextArray);
        JSONObject current = new JSONObject();
        currentContext.write(current);
        put(root, "currentContext", current);
        return root.toString();
    }

    private CandidateStats statsFor(ContextKey context, CandidateKey candidate, boolean create) {
        Map<CandidateKey, CandidateStats> candidates = contexts.get(context);
        if (candidates == null) {
            if (!create && !contexts.containsKey(context)) {
                return null;
            }
            if (contexts.size() >= NextKeyTouchPolicy.MAX_CONTEXTS) {
                return null;
            }
            candidates = new HashMap<>();
            contexts.put(context, candidates);
        }
        CandidateStats stats = candidates.get(candidate);
        if (stats == null && create) {
            if (candidates.size() >= NextKeyTouchPolicy.MAX_CANDIDATES_PER_CONTEXT) {
                return null;
            }
            stats = new CandidateStats(candidate);
            candidates.put(candidate, stats);
        }
        return stats;
    }

    private static int boundedIncrement(int value) {
        int next = value >= MAX_COUNT ? Math.max(1, value / 2) : value + 1;
        return Math.min(MAX_COUNT, next);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static GestureAction safeAction(GestureAction action) {
        return action == null ? GestureAction.TAP : action;
    }

    private static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException ignored) {
            // Best-effort local model persistence.
        }
    }

    static final class CandidateStats {
        final CandidateKey key;
        int acceptedCount;
        int correctedToCount;
        int correctedFromCount;
        long lastUpdatedEpoch;

        private CandidateStats(CandidateKey key) {
            this.key = key;
        }

        static CandidateStats decode(JSONObject object) {
            if (object == null || object.optString(NEXT_KEY).isEmpty()) {
                return null;
            }
            CandidateStats stats = new CandidateStats(new CandidateKey(
                    object.optString(NEXT_KEY),
                    safeActionFrom(object.optString(NEXT_ACTION))));
            stats.acceptedCount = Math.max(0, object.optInt(ACCEPTED_COUNT, 0));
            stats.correctedToCount = Math.max(0, object.optInt(CORRECTED_TO_COUNT, 0));
            stats.correctedFromCount = Math.max(0, object.optInt(CORRECTED_FROM_COUNT, 0));
            stats.lastUpdatedEpoch = object.optLong(LAST_UPDATED_EPOCH, 0L);
            return stats;
        }

        JSONObject encode() {
            JSONObject object = new JSONObject();
            put(object, NEXT_KEY, key.keyId);
            put(object, NEXT_ACTION, key.action.name());
            put(object, ACCEPTED_COUNT, acceptedCount);
            put(object, CORRECTED_TO_COUNT, correctedToCount);
            put(object, CORRECTED_FROM_COUNT, correctedFromCount);
            put(object, LAST_UPDATED_EPOCH, lastUpdatedEpoch);
            return object;
        }

        static final Comparator<CandidateStats> ORDER = (left, right) -> left.key.compareTo(right.key);
    }

    private static final class CandidateKey implements Comparable<CandidateKey> {
        final String keyId;
        final GestureAction action;

        CandidateKey(String keyId, GestureAction action) {
            this.keyId = safe(keyId);
            this.action = safeAction(action);
        }

        static CandidateKey from(TypingEventJournal.Input input) {
            return new CandidateKey(semanticId(input), input.action);
        }

        @Override
        public int compareTo(CandidateKey other) {
            int keyComparison = keyId.compareTo(other.keyId);
            return keyComparison != 0 ? keyComparison : action.name().compareTo(other.action.name());
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof CandidateKey)) {
                return false;
            }
            CandidateKey that = (CandidateKey) other;
            return keyId.equals(that.keyId) && action == that.action;
        }

        @Override
        public int hashCode() {
            return 31 * keyId.hashCode() + action.hashCode();
        }
    }

    private static final class ContextKey {
        final String keyId;
        final GestureAction action;

        ContextKey(String keyId, GestureAction action) {
            this.keyId = safe(keyId);
            this.action = safeAction(action);
        }

        static ContextKey initial() {
            return new ContextKey("", GestureAction.TAP);
        }

        static ContextKey from(TypingEventJournal.Input input) {
            return new ContextKey(semanticId(input), input.action);
        }

        static ContextKey decode(JSONObject object) {
            if (object == null) {
                return initial();
            }
            return new ContextKey(object.optString(PREVIOUS_KEY), safeActionFrom(
                    object.optString(PREVIOUS_ACTION)));
        }

        void write(JSONObject object) {
            put(object, PREVIOUS_KEY, keyId);
            put(object, PREVIOUS_ACTION, action.name());
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ContextKey)) {
                return false;
            }
            ContextKey that = (ContextKey) other;
            return keyId.equals(that.keyId) && action == that.action;
        }

        @Override
        public int hashCode() {
            return 31 * keyId.hashCode() + action.hashCode();
        }

        static final Comparator<ContextKey> ORDER = (left, right) -> {
            int keyComparison = left.keyId.compareTo(right.keyId);
            return keyComparison != 0 ? keyComparison : left.action.name().compareTo(right.action.name());
        };
    }

    private static String semanticId(TypingEventJournal.Input input) {
        if (input == null || input.keyCodePoints == null || input.keyCodePoints.isEmpty()) {
            return "";
        }
        return "dingul:" + input.keyCodePoints;
    }

    private static GestureAction safeActionFrom(String value) {
        try {
            return value == null || value.isEmpty() ? GestureAction.TAP : GestureAction.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return GestureAction.TAP;
        }
    }
}
