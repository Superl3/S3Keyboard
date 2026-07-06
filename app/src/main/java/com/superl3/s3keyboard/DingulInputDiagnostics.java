package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

final class DingulInputDiagnostics {
    private static final String PREF_NAME = "keyboard_preferences";

    final long learningEpoch;
    final int patternEvents;
    final int journalEvents;
    final int dingulProfileEntries;
    final String latestKeyCodePoints;
    final String latestAction;
    final String latestType;
    final int inputSamples;
    final int correctionSamples;
    final int correctionRatePermille;

    private DingulInputDiagnostics(
            long learningEpoch,
            int patternEvents,
            int journalEvents,
            int dingulProfileEntries,
            String latestKeyCodePoints,
            String latestAction,
            String latestType,
            int inputSamples,
            int correctionSamples,
            int correctionRatePermille) {
        this.learningEpoch = learningEpoch;
        this.patternEvents = patternEvents;
        this.journalEvents = journalEvents;
        this.dingulProfileEntries = dingulProfileEntries;
        this.latestKeyCodePoints = RuntimeDefaults.stringOrDefault(latestKeyCodePoints, "");
        this.latestAction = RuntimeDefaults.stringOrDefault(latestAction, "");
        this.latestType = RuntimeDefaults.stringOrDefault(latestType, "");
        this.inputSamples = Math.max(0, inputSamples);
        this.correctionSamples = Math.max(0, correctionSamples);
        this.correctionRatePermille = Math.max(0, Math.min(1000, correctionRatePermille));
    }

    static DingulInputDiagnostics load(Context context) {
        if (context == null) {
            return empty();
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long epoch = LearningEpoch.current(context);
        boolean currentEpoch = LearningEpoch.matches(
                prefs.getLong(TouchBiasStore.LEARNING_EPOCH_MARKER, 0L),
                epoch);
        JSONArray pattern = currentEpoch
                ? jsonArray(prefs.getString(TouchBiasStore.TYPING_PATTERN_LOG, ""))
                : new JSONArray();
        JSONArray journal = currentEpoch
                ? jsonArray(prefs.getString(TouchBiasStore.TYPING_EVENT_JOURNAL, ""))
                : new JSONArray();
        JSONObject latestJournal = latestObject(journal);
        JSONObject latestPattern = latestObject(pattern);
        TouchBiasStore.Bias bias = currentEpoch
                ? TouchBiasStore.Bias.decode(prefs.getString(TouchBiasStore.TOUCH_BIAS_STATS, ""))
                : TouchBiasStore.Bias.none();
        return new DingulInputDiagnostics(
                epoch,
                pattern.length(),
                journal.length(),
                currentEpoch
                        ? profileEntryCount(prefs.getString(TouchBiasStore.DINGUL_TOUCH_PROFILE, ""), epoch)
                        : 0,
                latestJournal.optString("keyCp"),
                latestJournal.optString("action", latestPattern.optString("action")),
                latestPattern.optString("type", latestJournal.optString("type")),
                bias.textSamples,
                bias.correctionSamples,
                bias.correctionRatePermille());
    }

    static DingulInputDiagnostics empty() {
        return new DingulInputDiagnostics(0L, 0, 0, 0, "", "", "", 0, 0, 0);
    }

    String summaryText(Context context) {
        String epochText = learningEpoch <= 0L ? "-" : Long.toString(learningEpoch);
        String recent = latestType.isEmpty()
                ? "-"
                : latestType + " " + (latestKeyCodePoints.isEmpty() ? "-" : latestKeyCodePoints)
                + "/" + (latestAction.isEmpty() ? "-" : latestAction);
        String rate = String.format(Locale.US, "%.1f%%", correctionRatePermille / 10f);
        if (context == null) {
            return "epoch " + epochText
                    + "\nrecent " + recent
                    + "\npattern " + patternEvents
                    + " / journal " + journalEvents
                    + " / dingul " + dingulProfileEntries
                    + "\ninput " + inputSamples
                    + " / correction " + correctionSamples
                    + " / rate " + rate;
        }
        return context.getString(
                R.string.dingul_input_diagnostics_summary_format,
                epochText,
                recent,
                patternEvents,
                journalEvents,
                dingulProfileEntries,
                inputSamples,
                correctionSamples,
                rate);
    }

    static JSONArray epochFilteredArray(String raw, long epoch) {
        JSONArray source = jsonArray(raw);
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            JSONObject event = source.optJSONObject(i);
            if (event != null && LearningEpoch.matches(event.optLong("learningEpoch", 0L), epoch)) {
                filtered.put(event);
            }
        }
        return filtered;
    }

    private static JSONObject latestObject(JSONArray events) {
        if (events == null || events.length() == 0) {
            return new JSONObject();
        }
        JSONObject object = events.optJSONObject(events.length() - 1);
        return object == null ? new JSONObject() : object;
    }

    private static int profileEntryCount(String raw, long epoch) {
        JSONObject root = jsonObject(raw);
        if (!LearningEpoch.matches(root.optLong("learningEpoch", 0L), epoch)) {
            return 0;
        }
        JSONObject entries = root.optJSONObject("entries");
        return entries == null ? 0 : entries.length();
    }

    private static JSONArray jsonArray(String raw) {
        try {
            return raw == null || raw.isEmpty() ? new JSONArray() : new JSONArray(raw);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static JSONObject jsonObject(String raw) {
        try {
            return raw == null || raw.isEmpty() ? new JSONObject() : new JSONObject(raw);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }
}
