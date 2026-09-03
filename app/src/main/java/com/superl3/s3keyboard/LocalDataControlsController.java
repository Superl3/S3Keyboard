package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

final class LocalDataControlsController {
    private static final String PREF_NAME = "keyboard_preferences";

    private final Context context;

    LocalDataControlsController(Context context) {
        this.context = context;
    }

    boolean clipboardHistoryEnabled() {
        return KeyboardPreferences.loadClipboardHistoryEnabled(context);
    }

    void setClipboardHistoryEnabled(boolean enabled) {
        KeyboardPreferences.saveClipboardHistoryEnabled(context, enabled);
        if (!enabled) {
            clearClipboardHistory();
        }
    }

    void clearClipboardHistory() {
        new ClipboardStore(context).clear();
    }

    void resetTouchCorrectionAndInputLogs() {
        TouchBiasStore.reset(context);
    }

    void clearInputLogsOnly() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TouchBiasStore.TYPING_PATTERN_LOG)
                .remove(TouchBiasStore.TYPING_EVENT_JOURNAL)
                .remove(TouchBiasStore.NEXT_KEY_TOUCH_MODEL)
                .apply();
    }

    void clearTouchBiasOnly() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TouchBiasStore.TOUCH_BIAS_STATS)
                .remove(TouchBiasStore.DINGUL_TOUCH_PROFILE)
                .remove(TouchBiasStore.NEXT_KEY_TOUCH_MODEL)
                .apply();
    }

    void clearRemoteCompatibilityLog() {
        RemoteCompatibilityLog.clear(context);
    }

    void resetDiagnosticsAndInputLearning() {
        ReleaseSafeDiagnostics.clear(context);
        TouchBiasStore.reset(context);
    }

    void clearAllLocalData() {
        clearClipboardHistory();
        new TextToolsStore(context).clear();
        resetTouchCorrectionAndInputLogs();
        ReleaseSafeDiagnostics.clear(context);
        clearRemoteCompatibilityLog();
    }

    static String[] managedLocalDataKeys() {
        return new String[] {
                ClipboardStore.KEY_ENTRIES,
                ClipboardStore.KEY_ENTRIES_V2,
                TextToolsStore.KEY_DATA_V1,
                TouchBiasStore.TOUCH_BIAS_STATS,
                TouchBiasStore.TYPING_PATTERN_LOG,
                TouchBiasStore.TYPING_EVENT_JOURNAL,
                TouchBiasStore.NEXT_KEY_TOUCH_MODEL,
                TouchBiasStore.DINGUL_TOUCH_PROFILE,
                TouchBiasStore.LEARNING_EPOCH_MARKER,
                ReleaseSafeDiagnostics.KEY_STATE_V1,
                RemoteCompatibilityLog.KEY_ENTRIES
        };
    }

    Summary summary() {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return new Summary(
                clipboardHistoryEnabled(),
                new ClipboardStore(context).getEntries().size(),
                hasMeaningfulStoredValue(preferences.getString(TouchBiasStore.TOUCH_BIAS_STATS, "")),
                countJsonArray(preferences.getString(TouchBiasStore.TYPING_PATTERN_LOG, "")),
                countJsonArray(preferences.getString(TouchBiasStore.TYPING_EVENT_JOURNAL, "")),
                hasMeaningfulStoredValue(preferences.getString(TouchBiasStore.DINGUL_TOUCH_PROFILE, "")),
                RemoteCompatibilityLog.load(context).size());
    }

    String summaryText() {
        Summary summary = summary();
        return context.getString(
                R.string.local_data_summary_format,
                context.getString(summary.clipboardHistoryEnabled ? R.string.state_on : R.string.state_off),
                summary.clipboardEntryCount,
                summary.typingPatternEventCount,
                summary.typingEventJournalEventCount,
                summary.remoteCompatibilityEntryCount,
                context.getString(summary.touchBiasStatsPresent
                        ? R.string.local_data_present
                        : R.string.local_data_absent),
                context.getString(summary.dingulTouchProfilePresent
                        ? R.string.local_data_present
                        : R.string.local_data_absent));
    }

    static int countJsonArray(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0;
        }
        try {
            return new JSONArray(raw).length();
        } catch (JSONException ignored) {
            return 0;
        }
    }

    static boolean hasMeaningfulStoredValue(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        return !trimmed.isEmpty()
                && !"{}".equals(trimmed)
                && !"[]".equals(trimmed);
    }

    static final class Summary {
        final boolean clipboardHistoryEnabled;
        final int clipboardEntryCount;
        final boolean touchBiasStatsPresent;
        final int typingPatternEventCount;
        final int typingEventJournalEventCount;
        final boolean dingulTouchProfilePresent;
        final int remoteCompatibilityEntryCount;

        Summary(
                boolean clipboardHistoryEnabled,
                int clipboardEntryCount,
                boolean touchBiasStatsPresent,
                int typingPatternEventCount,
                int typingEventJournalEventCount,
                boolean dingulTouchProfilePresent,
                int remoteCompatibilityEntryCount) {
            this.clipboardHistoryEnabled = clipboardHistoryEnabled;
            this.clipboardEntryCount = clipboardEntryCount;
            this.touchBiasStatsPresent = touchBiasStatsPresent;
            this.typingPatternEventCount = typingPatternEventCount;
            this.typingEventJournalEventCount = typingEventJournalEventCount;
            this.dingulTouchProfilePresent = dingulTouchProfilePresent;
            this.remoteCompatibilityEntryCount = remoteCompatibilityEntryCount;
        }

    }
}
