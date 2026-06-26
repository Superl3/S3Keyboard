package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

final class InputIssueReport {
    private static final String PREF_NAME = "keyboard_preferences";

    private InputIssueReport() {
    }

    static String build(
            Context context,
            String packageName,
            AppInputProfile inputProfile,
            KeyboardSettings settings,
            EditorInputPolicy policy) {
        JSONObject report = new JSONObject();
        SharedPreferences prefs = context == null
                ? null
                : context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        AppInputProfile safeProfile = inputProfile == null
                ? AppInputProfile.STANDARD
                : inputProfile;
        KeyboardErgonomicsOptions ergonomicsOptions = context == null
                ? KeyboardErgonomicsOptions.DEFAULT
                : KeyboardPreferences.loadErgonomicsOptions(context);
        List<RemoteCompatibilityLog.Entry> remoteEntries = context == null
                ? Collections.emptyList()
                : RemoteCompatibilityLog.load(context);
        try {
            report.put("schemaVersion", 2);
            report.put("generatedAtMs", System.currentTimeMillis());
            report.put("redaction", redactionSummary());
            report.put("settingsSchema", settingsSchemaSummary());
            report.put("packageName", packageName == null ? "" : packageName);
            report.put("inputProfile", safeProfile.id);
            report.put("inputProfileDetails", inputProfileSummary(safeProfile));
            report.put("appProfileOverrides", appProfileOverrideSummary(
                    packageName,
                    context == null
                            ? AppInputProfileOverrides.EMPTY
                            : KeyboardPreferences.loadAppInputProfileOverrides(context)));
            report.put("keyboardMode", settings == null ? "" : settings.keyboardMode.name());
            report.put("remoteModeEnabled", settings != null && settings.remoteModeEnabled);
            report.put("settings", KeyboardSettingsSnapshot.from(
                    settings,
                    ergonomicsOptions).toJson());
            report.put("userInputAssistance", userInputAssistance(
                    context,
                    settings,
                    ergonomicsOptions));
            report.put("layoutAccessibility", layoutAccessibilityReport(
                    settings,
                    ergonomicsOptions,
                    policy));
            report.put("localDataSummary", localDataSummary(context == null
                    ? null
                    : new LocalDataControlsController(context).summary()));
            report.put("surface", policy == null ? "" : policy.surface.name());
            report.put("allowComposingText", policy != null && policy.allowComposingText);
            report.put("allowTextConveniences", policy != null && policy.allowTextConveniences);
            report.put("touchBiasStats", parseObject(prefString(prefs, TouchBiasStore.TOUCH_BIAS_STATS)));
            report.put("dingulTouchProfile", parseObject(prefString(prefs, TouchBiasStore.DINGUL_TOUCH_PROFILE)));
            report.put("typingPatternLog", redactTypingPatternLog(
                    prefString(prefs, TouchBiasStore.TYPING_PATTERN_LOG)));
            report.put("typingEventJournal", redactTypingEventJournal(
                    prefString(prefs, TouchBiasStore.TYPING_EVENT_JOURNAL)));
            report.put("remoteCompatibilityLog", remoteLogArray(remoteEntries));
            report.put("remoteCompatibilitySummary", remoteCompatibilitySummary(
                    packageName,
                    remoteEntries));
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to build input issue report.", exception);
        }
        return report.toString();
    }

    static JSONObject localDataSummary(LocalDataControlsController.Summary summary) {
        JSONObject object = new JSONObject();
        LocalDataControlsController.Summary safe = summary == null
                ? new LocalDataControlsController.Summary(false, 0, false, 0, 0, false, 0)
                : summary;
        put(object, "clipboardHistoryEnabled", safe.clipboardHistoryEnabled);
        put(object, "clipboardEntryCount", safe.clipboardEntryCount);
        put(object, "clipboardEntriesIncluded", false);
        put(object, "touchBiasStatsPresent", safe.touchBiasStatsPresent);
        put(object, "typingPatternEventCount", safe.typingPatternEventCount);
        put(object, "typingEventJournalEventCount", safe.typingEventJournalEventCount);
        put(object, "dingulTouchProfilePresent", safe.dingulTouchProfilePresent);
        put(object, "remoteCompatibilityEntryCount", safe.remoteCompatibilityEntryCount);
        put(object, "clearPath", "LocalDataControlsController.clearAllLocalData");
        return object;
    }

    static JSONObject layoutAccessibilityReport(
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions,
            EditorInputPolicy policy) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        KeyboardErgonomicsOptions safeErgonomics = ergonomicsOptions == null
                ? KeyboardErgonomicsOptions.DEFAULT
                : ergonomicsOptions;
        KeyboardSurface currentSurface = policy == null ? KeyboardSurface.NORMAL : policy.surface;
        KeyboardSettings normalSettings = safeSettings.withRemoteOptions(
                false,
                safeSettings.remoteKeyPreset,
                safeSettings.remoteImeShortcut);

        JSONObject object = new JSONObject();
        put(object, "hardMinimumTouchTargetDp", KeyboardAccessibilityAudit.MIN_TOUCH_TARGET_DP);
        put(object, "recommendedTouchTargetDp", KeyboardAccessibilityAudit.RECOMMENDED_TOUCH_TARGET_DP);
        put(object, "current", layoutAccessibilitySummary(
                safeSettings,
                safeErgonomics,
                currentSurface));
        put(object, "hangul", layoutAccessibilitySummary(
                normalSettings.withKeyboardMode(KeyboardMode.HANGUL),
                safeErgonomics,
                KeyboardSurface.NORMAL));
        put(object, "qwerty", layoutAccessibilitySummary(
                normalSettings.withKeyboardMode(KeyboardMode.ENGLISH),
                safeErgonomics,
                KeyboardSurface.NORMAL));
        return object;
    }

    static JSONObject layoutAccessibilitySummary(
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions,
            KeyboardSurface surface) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        KeyboardErgonomicsOptions safeErgonomics = ergonomicsOptions == null
                ? KeyboardErgonomicsOptions.DEFAULT
                : ergonomicsOptions;
        KeyboardSurface safeSurface = surface == null ? KeyboardSurface.NORMAL : surface;
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(safeSettings, safeSurface),
                safeSettings,
                safeErgonomics,
                360f,
                safeSettings.measuredHeightDp(),
                1f);
        List<KeyboardAccessibilityAudit.Issue> hardIssues =
                KeyboardAccessibilityAudit.audit(slots);
        List<KeyboardAccessibilityAudit.Issue> advisoryIssues =
                KeyboardAccessibilityAudit.advisoryAudit(slots);

        JSONObject object = new JSONObject();
        put(object, "keyboardMode", safeSettings.keyboardMode.name());
        put(object, "surface", safeSurface.name());
        put(object, "keyCount", slots.size());
        put(object, "hardIssueCount", hardIssues.size());
        put(object, "recommendedIssueCount", advisoryIssues.size());
        put(object, "smallestHitWidthDp", smallestHitWidth(slots));
        put(object, "smallestHitHeightDp", smallestHitHeight(slots));
        put(object, "hardIssueKeys", issueKeyLabels(hardIssues, 12));
        put(object, "recommendedIssueKeys", issueKeyLabels(advisoryIssues, 12));
        return object;
    }

    static JSONObject inputProfileSummary(AppInputProfile profile) {
        AppInputProfile safe = profile == null ? AppInputProfile.STANDARD : profile;
        JSONObject object = new JSONObject();
        put(object, "id", safe.id);
        put(object, "source", safe.source);
        put(object, "remoteMode", safe.remoteMode);
        putNullableBoolean(object, "preferAsciiLayout", safe.preferAsciiLayout);
        putNullableBoolean(object, "forceNumberRow", safe.forceNumberRow);
        putNullableBoolean(object, "allowComposingText", safe.allowComposingText);
        putNullableBoolean(object, "allowTextConveniences", safe.allowTextConveniences);
        return object;
    }

    static JSONObject appProfileOverrideSummary(
            String packageName,
            AppInputProfileOverrides overrides) {
        AppInputProfileOverrides safe = overrides == null ? AppInputProfileOverrides.EMPTY : overrides;
        JSONObject object = new JSONObject();
        boolean ascii = KeyboardPreferences.packageListContains(safe.asciiPackages, packageName);
        boolean numberRow = KeyboardPreferences.packageListContains(safe.numberRowPackages, packageName);
        boolean noComposing = KeyboardPreferences.packageListContains(safe.noComposingPackages, packageName);
        boolean noTextConveniences =
                KeyboardPreferences.packageListContains(safe.noTextConveniencesPackages, packageName);
        put(object, "asciiPackageMatched", ascii);
        put(object, "numberRowPackageMatched", numberRow);
        put(object, "noComposingPackageMatched", noComposing);
        put(object, "noTextConveniencesPackageMatched", noTextConveniences);
        put(object, "matchedAny", ascii || numberRow || noComposing || noTextConveniences);
        put(object, "rawPackageListsIncluded", false);
        return object;
    }

    static JSONArray redactTypingPatternLog(String encodedLog) {
        JSONArray source = parseArray(encodedLog);
        JSONArray redacted = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            JSONObject sourceEvent = source.optJSONObject(i);
            if (sourceEvent == null) {
                continue;
            }
            JSONObject event = copyWithoutText(sourceEvent);
            String text = sourceEvent.optString("text", "");
            if (!text.isEmpty()) {
                put(event, "textRedacted", true);
                put(event, "textLength", text.length());
            }
            redacted.put(event);
        }
        return redacted;
    }

    static JSONArray redactTypingEventJournal(String encodedJournal) {
        JSONArray source = parseArray(encodedJournal);
        JSONArray redacted = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            JSONObject sourceEvent = source.optJSONObject(i);
            if (sourceEvent == null) {
                continue;
            }
            JSONObject event = copyWithoutSensitiveJournalFields(sourceEvent);
            redacted.put(event);
        }
        return redacted;
    }

    static JSONObject remoteCompatibilitySummary(
            String packageName,
            java.util.List<RemoteCompatibilityLog.Entry> entries) {
        return parseObject(RemoteCompatibilityReport.toJson(packageName, entries));
    }

    static JSONObject redactionSummary() {
        JSONObject object = new JSONObject();
        put(object, "mode", "local_redacted");
        put(object, "typedTextRedacted", true);
        put(object, "typingPatternTextRedacted", true);
        put(object, "typingEventCodePointsRedacted", true);
        put(object, "clipboardHistoryIncluded", false);
        put(object, "networkUpload", false);
        put(object, "settingsIncluded", true);
        put(object, "touchGeometryIncluded", true);
        put(object, "remoteCompatibilityManualResultsIncluded", true);
        return object;
    }

    static JSONObject settingsSchemaSummary() {
        JSONObject object = new JSONObject();
        int localText = 0;
        int localDiagnostic = 0;
        int compatibility = 0;
        int userFacing = 0;
        for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entries()) {
            if (entry.userFacing) {
                userFacing++;
            }
            switch (entry.risk) {
                case LOCAL_TEXT:
                    localText++;
                    break;
                case LOCAL_DIAGNOSTIC:
                    localDiagnostic++;
                    break;
                case COMPATIBILITY:
                    compatibility++;
                    break;
                case NONE:
                default:
                    break;
            }
        }
        put(object, "preferenceKeyCount", KeyboardSettingsSchema.entries().size());
        put(object, "userFacingKeyCount", userFacing);
        put(object, "localTextKeyCount", localText);
        put(object, "localDiagnosticKeyCount", localDiagnostic);
        put(object, "compatibilityKeyCount", compatibility);
        return object;
    }

    static JSONObject userInputAssistance(Context context, KeyboardSettings settings) {
        return userInputAssistance(context, settings, context == null
                ? KeyboardErgonomicsOptions.DEFAULT
                : KeyboardPreferences.loadErgonomicsOptions(context));
    }

    static JSONObject userInputAssistance(
            Context context,
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        boolean showHangulConsonantHints = context == null
                ? safeSettings.showHangulSlideHints
                : KeyboardPreferences.loadShowHangulConsonantSlideHints(context);
        boolean showHangulVowelHints = context == null
                ? safeSettings.showHangulSlideHints
                : KeyboardPreferences.loadShowHangulVowelSlideHints(context);
        boolean showSpacebarHints = context == null
                ? safeSettings.showHangulSlideHints
                : KeyboardPreferences.loadShowSpacebarSlideHints(context);
        boolean showDebugOverlay = context != null
                && KeyboardPreferences.loadDebugKeyBoundsOverlayEnabled(context);
        boolean showEnglishHints = safeSettings.showEnglishSlideHints;
        boolean showPreview = safeSettings.showBeginnerTooltipPreview;
        InputAssistanceMode mode = InputAssistanceMode.match(
                showHangulConsonantHints,
                showHangulVowelHints,
                showEnglishHints,
                showSpacebarHints,
                showPreview,
                showDebugOverlay);
        KeyboardErgonomicsPreset currentErgonomicsPreset =
                KeyboardErgonomicsPreset.findMatching(ergonomicsOptions == null
                        ? KeyboardErgonomicsOptions.DEFAULT
                        : ergonomicsOptions);
        KeyboardErgonomicsPreset recommendedErgonomicsPreset = mode.profile == null
                ? null
                : mode.profile.recommendedErgonomicsPreset;

        JSONObject object = new JSONObject();
        put(object, "mode", mode.name().toLowerCase(java.util.Locale.ROOT));
        put(object, "showHangulConsonantHints", showHangulConsonantHints);
        put(object, "showHangulVowelHints", showHangulVowelHints);
        put(object, "showEnglishHints", showEnglishHints);
        put(object, "showSpacebarHints", showSpacebarHints);
        put(object, "showInputPreview", showPreview);
        put(object, "debugKeyBoundsOverlayEnabled", showDebugOverlay);
        put(object, "currentErgonomicsPreset", currentErgonomicsPreset == null
                ? "custom"
                : currentErgonomicsPreset.name().toLowerCase(java.util.Locale.ROOT));
        put(object, "recommendedErgonomicsPreset", recommendedErgonomicsPreset == null
                ? "custom"
                : recommendedErgonomicsPreset.name().toLowerCase(java.util.Locale.ROOT));
        put(object, "appliesErgonomicsPreset",
                recommendedErgonomicsPreset != null
                        && recommendedErgonomicsPreset == currentErgonomicsPreset);
        put(object, "clipboardHistoryEnabled",
                context != null && KeyboardPreferences.loadClipboardHistoryEnabled(context));
        put(object, "palmRejectionEnabled",
                context != null && KeyboardPreferences.loadPalmRejectionEnabled(context));
        put(object, "touchBiasAutoCorrectionEnabled",
                context != null && KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(context));
        return object;
    }

    private static float smallestHitWidth(List<KeyboardLayoutCalculator.Slot> slots) {
        if (slots == null || slots.isEmpty()) {
            return 0f;
        }
        float result = Float.MAX_VALUE;
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            result = Math.min(result, slot.hitRight - slot.hitLeft);
        }
        return result == Float.MAX_VALUE ? 0f : result;
    }

    private static float smallestHitHeight(List<KeyboardLayoutCalculator.Slot> slots) {
        if (slots == null || slots.isEmpty()) {
            return 0f;
        }
        float result = Float.MAX_VALUE;
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            result = Math.min(result, slot.hitBottom - slot.hitTop);
        }
        return result == Float.MAX_VALUE ? 0f : result;
    }

    private static JSONArray issueKeyLabels(
            List<KeyboardAccessibilityAudit.Issue> issues,
            int limit) {
        JSONArray array = new JSONArray();
        if (issues == null || issues.isEmpty() || limit <= 0) {
            return array;
        }
        int count = Math.min(limit, issues.size());
        for (int i = 0; i < count; i++) {
            KeyboardAccessibilityAudit.Issue issue = issues.get(i);
            JSONObject object = new JSONObject();
            put(object, "key", issue.keyLabel);
            put(object, "reason", issue.reason);
            put(object, "widthDp", issue.width);
            put(object, "heightDp", issue.height);
            array.put(object);
        }
        return array;
    }

    static JSONArray remoteLogArray(java.util.List<RemoteCompatibilityLog.Entry> entries) {
        JSONArray array = new JSONArray();
        if (entries == null) {
            return array;
        }
        for (RemoteCompatibilityLog.Entry entry : entries) {
            JSONObject object = new JSONObject();
            try {
                object.put("timestampMs", entry.timestampMs);
                object.put("packageName", entry.packageName);
                object.put("label", entry.label);
                object.put("keyCode", entry.keyCode);
                object.put("metaState", entry.metaState);
                object.put("eventCount", entry.acceptedEventCount);
                object.put("acceptedEventCount", entry.acceptedEventCount);
                object.put("expectedEventCount", entry.expectedEventCount());
                object.put("localInputConnectionAccepted", entry.localInputConnectionAccepted());
                object.put("localTransportComplete", entry.localInputConnectionAccepted());
                object.put("manualResult", entry.manualResult);
                array.put(object);
            } catch (JSONException ignored) {
                // Keep report export best-effort.
            }
        }
        return array;
    }

    private static JSONObject copyWithoutText(JSONObject source) {
        JSONObject copy = new JSONObject();
        JSONArray names = source.names();
        if (names == null) {
            return copy;
        }
        for (int i = 0; i < names.length(); i++) {
            String name = names.optString(i);
            if ("text".equals(name)) {
                continue;
            }
            put(copy, name, source.opt(name));
        }
        return copy;
    }

    private static JSONObject copyWithoutSensitiveJournalFields(JSONObject source) {
        JSONObject copy = new JSONObject();
        JSONArray names = source.names();
        if (names == null) {
            return copy;
        }
        for (int i = 0; i < names.length(); i++) {
            String name = names.optString(i);
            if (isSensitiveJournalField(name)) {
                String value = source.optString(name, "");
                if (!value.isEmpty()) {
                    put(copy, name + "Redacted", true);
                    put(copy, name + "Length", value.length());
                }
                continue;
            }
            put(copy, name, source.opt(name));
        }
        return copy;
    }

    private static boolean isSensitiveJournalField(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return "valuecp".equals(lower)
                || "keycp".equals(lower)
                || "shadowkeycp".equals(lower)
                || lower.contains("text")
                || lower.contains("clipboard")
                || lower.contains("phrase");
    }

    private static String prefString(SharedPreferences prefs, String key) {
        return prefs == null ? "" : prefs.getString(key, "");
    }

    private static JSONObject parseObject(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(raw);
        } catch (JSONException exception) {
            return new JSONObject();
        }
    }

    private static JSONArray parseArray(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(raw);
        } catch (JSONException exception) {
            return new JSONArray();
        }
    }

    private static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException ignored) {
            // Report generation must not fail because one optional field failed.
        }
    }

    private static void putNullableBoolean(JSONObject object, String key, Boolean value) {
        try {
            if (value == null) {
                object.put(key, JSONObject.NULL);
            } else {
                object.put(key, value.booleanValue());
            }
        } catch (JSONException ignored) {
            // Report generation must not fail because one optional field failed.
        }
    }
}
