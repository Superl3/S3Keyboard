package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;
import android.text.InputType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

public final class InputIssueReportTest {
    @Test
    public void typingPatternReportRedactsTextButKeepsActionAndGeometry() throws Exception {
        JSONArray source = new JSONArray()
                .put(new JSONObject()
                        .put("timeMs", 100)
                        .put("type", "correction")
                        .put("text", "secret")
                        .put("action", "LEFT")
                        .put("offsetXDp", 2.5)
                        .put("offsetYDp", -1.0));

        JSONArray redacted = InputIssueReport.redactTypingPatternLog(source.toString());
        JSONObject event = redacted.getJSONObject(0);

        assertFalse(event.has("text"));
        assertEquals("correction", event.getString("type"));
        assertEquals("LEFT", event.getString("action"));
        assertEquals(true, event.getBoolean("textRedacted"));
        assertEquals(6, event.getInt("textLength"));
        assertEquals(2.5, event.getDouble("offsetXDp"), 0.001);
    }

    @Test
    public void typingJournalReportRedactsCodePointFieldsButKeepsLabels() throws Exception {
        JSONArray source = new JSONArray()
                .put(new JSONObject()
                        .put("type", "input")
                        .put("id", "event-1")
                        .put("keyCp", "3131")
                        .put("valueCp", "AC00")
                        .put("shadowKeyCp", "3163")
                        .put("action", "TAP"))
                .put(new JSONObject()
                        .put("type", "label")
                        .put("targetEventId", "event-1")
                        .put("label", "missed_slide"));

        JSONArray redacted = InputIssueReport.redactTypingEventJournal(source.toString());
        JSONObject input = redacted.getJSONObject(0);
        JSONObject label = redacted.getJSONObject(1);

        assertFalse(input.has("keyCp"));
        assertFalse(input.has("valueCp"));
        assertFalse(input.has("shadowKeyCp"));
        assertTrue(input.getBoolean("keyCpRedacted"));
        assertTrue(input.getBoolean("valueCpRedacted"));
        assertTrue(input.getBoolean("shadowKeyCpRedacted"));
        assertEquals(4, input.getInt("keyCpLength"));
        assertEquals(4, input.getInt("valueCpLength"));
        assertEquals(4, input.getInt("shadowKeyCpLength"));
        assertEquals("TAP", input.getString("action"));
        assertEquals("missed_slide", label.getString("label"));
        assertEquals("event-1", label.getString("targetEventId"));
    }

    @Test
    public void typingJournalReportRedactsFutureSensitiveTextFields() throws Exception {
        JSONArray source = new JSONArray()
                .put(new JSONObject()
                        .put("type", "input")
                        .put("id", "event-1")
                        .put("committedText", "password")
                        .put("clipboardPreview", "copied secret")
                        .put("reservedPhrase", "private phrase")
                        .put("action", "TAP"));

        JSONArray redacted = InputIssueReport.redactTypingEventJournal(source.toString());
        JSONObject input = redacted.getJSONObject(0);

        assertFalse(input.has("committedText"));
        assertFalse(input.has("clipboardPreview"));
        assertFalse(input.has("reservedPhrase"));
        assertTrue(input.getBoolean("committedTextRedacted"));
        assertTrue(input.getBoolean("clipboardPreviewRedacted"));
        assertTrue(input.getBoolean("reservedPhraseRedacted"));
        assertEquals("TAP", input.getString("action"));
    }

    @Test
    public void issueReportBuildToleratesMissingAndroidContextForOfflineDiagnostics() throws Exception {
        String report = InputIssueReport.build(
                null,
                "com.example.editor",
                AppInputProfile.STANDARD,
                KeyboardSettings.defaults(),
                EditorInputPolicy.DEFAULT);

        JSONObject object = new JSONObject(report);

        assertEquals(2, object.getInt("schemaVersion"));
        assertEquals("com.example.editor", object.getString("packageName"));
        JSONObject layoutAccessibility = object.getJSONObject("layoutAccessibility");
        assertEquals(KeyboardAccessibilityAudit.MIN_TOUCH_TARGET_DP,
                layoutAccessibility.getDouble("hardMinimumTouchTargetDp"),
                0.001);
        assertEquals(KeyboardAccessibilityAudit.RECOMMENDED_TOUCH_TARGET_DP,
                layoutAccessibility.getDouble("recommendedTouchTargetDp"),
                0.001);
        assertTrue(layoutAccessibility.has("current"));
        assertTrue(layoutAccessibility.has("hangul"));
        assertTrue(layoutAccessibility.has("qwerty"));
        JSONObject localData = object.getJSONObject("localDataSummary");
        assertFalse(localData.getBoolean("clipboardHistoryEnabled"));
        assertFalse(localData.getBoolean("clipboardEntriesIncluded"));
        assertEquals(0, localData.getInt("clipboardEntryCount"));
        assertEquals(0, object.getJSONArray("remoteCompatibilityLog").length());
        assertFalse(object.getJSONObject("redaction").getBoolean("clipboardHistoryIncluded"));
    }

    @Test
    public void localDataSummaryExportsCountsAndClearPathWithoutClipboardContents() throws Exception {
        JSONObject summary = InputIssueReport.localDataSummary(
                new LocalDataControlsController.Summary(
                        true,
                        3,
                        true,
                        7,
                        11,
                        true,
                        5));

        assertTrue(summary.getBoolean("clipboardHistoryEnabled"));
        assertEquals(3, summary.getInt("clipboardEntryCount"));
        assertFalse(summary.getBoolean("clipboardEntriesIncluded"));
        assertTrue(summary.getBoolean("touchBiasStatsPresent"));
        assertEquals(7, summary.getInt("typingPatternEventCount"));
        assertEquals(11, summary.getInt("typingEventJournalEventCount"));
        assertTrue(summary.getBoolean("dingulTouchProfilePresent"));
        assertEquals(5, summary.getInt("remoteCompatibilityEntryCount"));
        assertEquals(
                "LocalDataControlsController.clearAllLocalData",
                summary.getString("clearPath"));
        assertFalse(summary.toString().contains("clipboardText"));
    }

    @Test
    public void layoutAccessibilityReportKeepsHardAndRecommendedTouchTargetsSeparate() throws Exception {
        JSONObject report = InputIssueReport.layoutAccessibilityReport(
                KeyboardSettings.defaults(),
                KeyboardErgonomicsOptions.DEFAULT,
                EditorInputPolicy.DEFAULT);

        JSONObject hangul = report.getJSONObject("hangul");
        JSONObject qwerty = report.getJSONObject("qwerty");

        assertEquals(0, hangul.getInt("hardIssueCount"));
        assertEquals(0, qwerty.getInt("hardIssueCount"));
        assertTrue(hangul.getInt("recommendedIssueCount") > 0);
        assertTrue(qwerty.getInt("recommendedIssueCount") > 0);
        assertTrue(hangul.getDouble("smallestHitWidthDp") > 0);
        assertTrue(qwerty.getDouble("smallestHitHeightDp") > 0);
        assertTrue(hangul.getJSONArray("hardIssueKeys").length() == 0);
        assertTrue(qwerty.getJSONArray("recommendedIssueKeys").length() > 0);
    }

    @Test
    public void layoutAccessibilityReportUsesCurrentSurfaceForCurrentSummary() throws Exception {
        JSONObject report = InputIssueReport.layoutAccessibilityReport(
                KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH),
                KeyboardErgonomicsOptions.DEFAULT,
                EditorInputPolicy.fromInputType(
                        InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD));

        JSONObject current = report.getJSONObject("current");

        assertEquals("ENGLISH", current.getString("keyboardMode"));
        assertEquals("PASSWORD_SAFE", current.getString("surface"));
        assertEquals(0, current.getInt("hardIssueCount"));
    }

    @Test
    public void remoteCompatibilitySummaryKeepsMatrixStatusWithoutTypedText() throws Exception {
        JSONObject summary = InputIssueReport.remoteCompatibilitySummary(
                "com.limelight",
                Arrays.asList(new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Alt+Tab",
                        KeyEvent.KEYCODE_TAB,
                        KeyEvent.META_ALT_ON,
                        4)));

        assertEquals("com.limelight", summary.getString("packageName"));
        assertEquals("moonlight", summary.getString("appFamily"));
        assertEquals(1, summary.getInt("sentCount"));
        assertTrue(summary.getBoolean("manualRemoteResultRequired"));
        assertTrue(summary.getJSONArray("cases").length() >= 12);
    }

    @Test
    public void inputProfileSummaryKeepsEffectiveOverridesForDiagnostics() throws Exception {
        JSONObject summary = InputIssueReport.inputProfileSummary(new AppInputProfile(
                "remote_desktop",
                true,
                true,
                true,
                false,
                false,
                "remote_auto_package"));

        assertEquals("remote_desktop", summary.getString("id"));
        assertEquals("remote_auto_package", summary.getString("source"));
        assertTrue(summary.getBoolean("remoteMode"));
        assertTrue(summary.getBoolean("preferAsciiLayout"));
        assertTrue(summary.getBoolean("forceNumberRow"));
        assertFalse(summary.getBoolean("allowComposingText"));
        assertFalse(summary.getBoolean("allowTextConveniences"));
    }

    @Test
    public void standardInputProfileSummaryKeepsNullOverridesVisible() throws Exception {
        JSONObject summary = InputIssueReport.inputProfileSummary(AppInputProfile.STANDARD);

        assertEquals("standard", summary.getString("id"));
        assertEquals("default", summary.getString("source"));
        assertFalse(summary.getBoolean("remoteMode"));
        assertTrue(summary.isNull("preferAsciiLayout"));
        assertTrue(summary.isNull("forceNumberRow"));
        assertTrue(summary.isNull("allowComposingText"));
        assertTrue(summary.isNull("allowTextConveniences"));
    }

    @Test
    public void appProfileOverrideSummaryShowsMatchedFlagsWithoutRawPackageLists() throws Exception {
        JSONObject summary = InputIssueReport.appProfileOverrideSummary(
                "com.example.editor",
                new AppInputProfileOverrides(
                        "com.example.editor, com.example.other",
                        "com.example.editor",
                        "com.example.editor.beta",
                        ""));

        assertTrue(summary.getBoolean("asciiPackageMatched"));
        assertTrue(summary.getBoolean("numberRowPackageMatched"));
        assertFalse(summary.getBoolean("noComposingPackageMatched"));
        assertFalse(summary.getBoolean("noTextConveniencesPackageMatched"));
        assertTrue(summary.getBoolean("matchedAny"));
        assertFalse(summary.getBoolean("rawPackageListsIncluded"));
        assertFalse(summary.toString().contains("com.example.other"));
        assertFalse(summary.toString().contains("com.example.editor.beta"));
    }

    @Test
    public void redactionSummaryDocumentsWhatTheReportDoesNotExport() throws Exception {
        JSONObject redaction = InputIssueReport.redactionSummary();

        assertEquals("local_redacted", redaction.getString("mode"));
        assertTrue(redaction.getBoolean("typedTextRedacted"));
        assertTrue(redaction.getBoolean("typingPatternTextRedacted"));
        assertTrue(redaction.getBoolean("typingEventCodePointsRedacted"));
        assertFalse(redaction.getBoolean("clipboardHistoryIncluded"));
        assertFalse(redaction.getBoolean("networkUpload"));
        assertTrue(redaction.getBoolean("settingsIncluded"));
        assertTrue(redaction.getBoolean("touchGeometryIncluded"));
        assertTrue(redaction.getBoolean("remoteCompatibilityManualResultsIncluded"));
    }

    @Test
    public void settingsSchemaSummaryExposesLocalRiskCounts() throws Exception {
        JSONObject summary = InputIssueReport.settingsSchemaSummary();

        assertEquals(KeyboardSettingsSchema.entries().size(), summary.getInt("preferenceKeyCount"));
        assertTrue(summary.getInt("userFacingKeyCount") > 0);
        assertTrue(summary.getInt("localTextKeyCount") >= 3);
        assertTrue(summary.getInt("localDiagnosticKeyCount") >= 3);
        assertTrue(summary.getInt("compatibilityKeyCount") >= 2);
    }

    @Test
    public void remoteLogReportIncludesManualResult() throws Exception {
        JSONArray log = InputIssueReport.remoteLogArray(Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Win+Space",
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_META_ON,
                        4,
                        RemoteCompatibilityLog.RESULT_FAIL)));

        JSONObject entry = log.getJSONObject(0);
        assertEquals("Win+Space", entry.getString("label"));
        assertEquals(4, entry.getInt("eventCount"));
        assertEquals(4, entry.getInt("acceptedEventCount"));
        assertEquals(4, entry.getInt("expectedEventCount"));
        assertTrue(entry.getBoolean("localInputConnectionAccepted"));
        assertTrue(entry.getBoolean("localTransportComplete"));
        assertEquals(RemoteCompatibilityLog.RESULT_FAIL, entry.getString("manualResult"));
    }

    @Test
    public void remoteLogReportDoesNotTreatPartialShortcutAcceptanceAsComplete() throws Exception {
        JSONArray log = InputIssueReport.remoteLogArray(Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Win+Space",
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_META_ON,
                        1,
                        RemoteCompatibilityLog.RESULT_UNKNOWN)));

        JSONObject entry = log.getJSONObject(0);
        assertEquals(1, entry.getInt("acceptedEventCount"));
        assertEquals(4, entry.getInt("expectedEventCount"));
        assertFalse(entry.getBoolean("localInputConnectionAccepted"));
        assertFalse(entry.getBoolean("localTransportComplete"));
    }

    @Test
    public void userInputAssistanceReportExposesEffectiveLearningMode() throws Exception {
        JSONObject assistance = InputIssueReport.userInputAssistance(
                null,
                KeyboardSettings.defaults().withHintVisibility(false, false, false));

        assertEquals("clean", assistance.getString("mode"));
        assertFalse(assistance.getBoolean("showHangulConsonantHints"));
        assertFalse(assistance.getBoolean("showHangulVowelHints"));
        assertFalse(assistance.getBoolean("showEnglishHints"));
        assertFalse(assistance.getBoolean("showSpacebarHints"));
        assertFalse(assistance.getBoolean("showInputPreview"));
        assertFalse(assistance.getBoolean("debugKeyBoundsOverlayEnabled"));
        assertEquals("legacy", assistance.getString("currentErgonomicsPreset"));
        assertEquals("legacy", assistance.getString("recommendedErgonomicsPreset"));
        assertTrue(assistance.getBoolean("appliesErgonomicsPreset"));
    }

    @Test
    public void userInputAssistanceReportShowsRecommendedAndCurrentErgonomicsPreset() throws Exception {
        JSONObject assistance = InputIssueReport.userInputAssistance(
                null,
                KeyboardSettings.defaults(),
                KeyboardErgonomicsPreset.AGGRESSIVE.options);

        assertEquals("learning", assistance.getString("mode"));
        assertEquals("aggressive", assistance.getString("currentErgonomicsPreset"));
        assertEquals("stable", assistance.getString("recommendedErgonomicsPreset"));
        assertFalse(assistance.getBoolean("appliesErgonomicsPreset"));
    }

    @Test
    public void userInputAssistanceReportMarksMatchingErgonomicsPresetApplied() throws Exception {
        JSONObject assistance = InputIssueReport.userInputAssistance(
                null,
                KeyboardSettings.defaults(),
                KeyboardErgonomicsPreset.STABLE.options);

        assertEquals("learning", assistance.getString("mode"));
        assertEquals("stable", assistance.getString("currentErgonomicsPreset"));
        assertEquals("stable", assistance.getString("recommendedErgonomicsPreset"));
        assertTrue(assistance.getBoolean("appliesErgonomicsPreset"));
    }
}
