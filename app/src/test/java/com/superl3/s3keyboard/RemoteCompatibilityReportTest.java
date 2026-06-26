package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class RemoteCompatibilityReportTest {
    @Test
    public void matrixContainsExpectedRemoteCompatibilityCases() {
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("Esc"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("Alt+Tab"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("F1"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("F12"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("Win+Space"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("Lang"));
        assertNotNull(RemoteCompatibilityMatrix.findByLabel("Ctrl+Space"));
        assertEquals(22, RemoteCompatibilityMatrix.all().length);
        assertEquals(12, RemoteCompatibilityMatrix.group(
                RemoteCompatibilityMatrix.Group.FUNCTION).length);
    }

    @Test
    public void matrixLabelsAreUnique() {
        Set<String> labels = new HashSet<>();

        for (RemoteCompatibilityMatrix.Case testCase : RemoteCompatibilityMatrix.all()) {
            assertTrue("duplicate remote case label: " + testCase.label, labels.add(testCase.label));
        }
    }

    @Test
    public void summaryFiltersByPackageAndReportsMissingCases() {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2),
                new RemoteCompatibilityLog.Entry(
                        200L,
                        "tv.parsec.client",
                        "F1",
                        KeyEvent.KEYCODE_F1,
                        0,
                        2));

        RemoteCompatibilityReport.Summary summary =
                RemoteCompatibilityReport.summarize("com.limelight", entries);

        assertEquals(1, summary.sentCount);
        assertEquals(RemoteCompatibilityMatrix.all().length, summary.totalCount);
        assertEquals("moonlight", summary.appFamily);
        assertFalse(summary.missingLabels().contains("Esc"));
        assertTrue(summary.missingLabels().contains("F1"));
    }

    @Test
    public void reportJsonIncludesSentAndMissingStatus() throws Exception {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Win+Space",
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_META_ON,
                        4,
                        RemoteCompatibilityLog.RESULT_PASS));

        JSONObject report = new JSONObject(
                RemoteCompatibilityReport.toJson("com.limelight", entries));
        JSONArray cases = report.getJSONArray("cases");

        assertEquals(2, report.getInt("schemaVersion"));
        assertEquals("com.limelight", report.getString("packageName"));
        assertEquals("moonlight", report.getString("appFamily"));
        assertEquals(1, report.getInt("sentCount"));
        assertEquals(1, report.getInt("passCount"));
        assertEquals(0, report.getInt("failCount"));
        assertEquals(0, report.getInt("localIncompleteCount"));
        assertTrue(report.getBoolean("manualRemoteResultRequired"));
        assertEquals(0, report.getJSONArray("unknownLabels").length());
        assertEquals(0, report.getJSONArray("failedLabels").length());
        assertEquals(0, report.getJSONArray("localIncompleteLabels").length());
        assertEquals(RemoteCompatibilityMatrix.all().length, report.getJSONArray("requiredLabels").length());
        assertTrue(arrayContains(report.getJSONArray("requiredLabels"), "Alt+Tab"));
        assertTrue(arrayContains(report.getJSONArray("requiredLabels"), "F12"));
        assertTrue(arrayContains(report.getJSONArray("requiredLabels"), "Win+Space"));
        assertTrue(arrayContains(report.getJSONArray("requiredAppFamilies"), "parsec"));
        assertTrue(arrayContains(report.getJSONArray("requiredAppFamilies"), "moonlight"));
        assertTrue(arrayContains(report.getJSONArray("requiredAppFamilies"), "chrome_remote_desktop"));
        assertTrue(report.getJSONArray("missingLabels").length() > 0);
        JSONArray groupSummaries = report.getJSONArray("groupSummaries");
        JSONObject imeSummary = groupSummary(groupSummaries, "IME");
        assertEquals(4, imeSummary.getInt("totalCount"));
        assertEquals(1, imeSummary.getInt("sentCount"));
        assertEquals(1, imeSummary.getInt("passCount"));
        assertEquals(3, imeSummary.getInt("missingCount"));
        assertEquals(0, imeSummary.getInt("localIncompleteCount"));
        assertTrue(arrayContains(imeSummary.getJSONArray("missingLabels"), "Alt+Shift"));
        JSONObject functionSummary = groupSummary(groupSummaries, "FUNCTION");
        assertEquals(12, functionSummary.getInt("totalCount"));
        assertEquals(12, functionSummary.getInt("missingCount"));
        assertTrue(caseSent(cases, "Win+Space"));
        assertEquals(RemoteCompatibilityLog.RESULT_PASS, caseManualResult(cases, "Win+Space"));
        assertEquals(4, caseAcceptedEventCount(cases, "Win+Space"));
        assertEquals(4, caseExpectedEventCount(cases, "Win+Space"));
        assertTrue(caseAcceptedLocally(cases, "Win+Space"));
        assertFalse(caseSent(cases, "Alt+Tab"));
    }

    @Test
    public void reportJsonTreatsPartialShortcutAcceptanceAsIncompleteTransport() throws Exception {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Win+Space",
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_META_ON,
                        1,
                        RemoteCompatibilityLog.RESULT_PASS));

        JSONObject report = new JSONObject(
                RemoteCompatibilityReport.toJson("com.limelight", entries));
        JSONArray cases = report.getJSONArray("cases");

        assertEquals(1, report.getInt("localIncompleteCount"));
        assertTrue(report.getBoolean("manualRemoteResultRequired"));
        assertEquals("Win+Space", report.getJSONArray("localIncompleteLabels").getString(0));
        JSONObject imeSummary = groupSummary(report.getJSONArray("groupSummaries"), "IME");
        assertEquals(1, imeSummary.getInt("localIncompleteCount"));
        assertTrue(arrayContains(imeSummary.getJSONArray("localIncompleteLabels"), "Win+Space"));
        assertEquals(1, caseAcceptedEventCount(cases, "Win+Space"));
        assertEquals(4, caseExpectedEventCount(cases, "Win+Space"));
        assertFalse(caseAcceptedLocally(cases, "Win+Space"));
    }

    @Test
    public void summaryCountsManualPassFailAndUnknownResults() {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2,
                        RemoteCompatibilityLog.RESULT_PASS),
                new RemoteCompatibilityLog.Entry(
                        200L,
                        "com.limelight",
                        "Tab",
                        KeyEvent.KEYCODE_TAB,
                        0,
                        2,
                        RemoteCompatibilityLog.RESULT_FAIL),
                new RemoteCompatibilityLog.Entry(
                        300L,
                        "com.limelight",
                        "F1",
                        KeyEvent.KEYCODE_F1,
                        0,
                        2));

        RemoteCompatibilityReport.Summary summary =
                RemoteCompatibilityReport.summarize("com.limelight", entries);

        assertEquals(3, summary.sentCount);
        assertEquals(1, summary.passCount);
        assertEquals(1, summary.failCount);
        assertEquals(1, summary.unknownCount);
        assertEquals("F1", summary.unknownLabels().get(0));
        assertEquals("Tab", summary.failedLabels().get(0));
    }

    @Test
    public void reportDoesNotRequireManualResultWhenEveryCaseIsMarked() throws Exception {
        java.util.ArrayList<RemoteCompatibilityLog.Entry> entries = new java.util.ArrayList<>();
        for (RemoteCompatibilityMatrix.Case testCase : RemoteCompatibilityMatrix.all()) {
            entries.add(new RemoteCompatibilityLog.Entry(
                    100L,
                    "com.limelight",
                    testCase.label,
                    testCase.keyCode,
                    testCase.metaState,
                    RemoteKeyEventSequence.eventCount(testCase.keyCode, testCase.metaState),
                    RemoteCompatibilityLog.RESULT_PASS));
        }

        JSONObject report = new JSONObject(
                RemoteCompatibilityReport.toJson("com.limelight", entries));

        assertFalse(report.getBoolean("manualRemoteResultRequired"));
        assertEquals(RemoteCompatibilityMatrix.all().length, report.getInt("passCount"));
        assertEquals(0, report.getInt("localIncompleteCount"));
    }

    @Test
    public void nonAndroidDescriptionUsesAsciiFallbackOnly() {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        100L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2,
                        RemoteCompatibilityLog.RESULT_PASS));

        String description = RemoteCompatibilityReport.describe("com.limelight", entries);

        assertTrue(description.contains("Remote compatibility: com.limelight"));
        assertTrue(description.contains("family moonlight"));
        assertTrue(description.contains("Manual remote pass/fail confirmation required."));
        assertFalse(description.contains("원격"));
        assertFalse(description.contains("성공"));
    }

    @Test
    public void appFamilyClassifiesRemoteClientsForComparison() {
        assertEquals("parsec", RemoteCompatibilityReport.appFamily("tv.parsec.client"));
        assertEquals("moonlight", RemoteCompatibilityReport.appFamily("com.limelight"));
        assertEquals("microsoft_rdp", RemoteCompatibilityReport.appFamily("com.microsoft.rdc.android"));
        assertEquals("microsoft_rdp", RemoteCompatibilityReport.appFamily("com.microsoft.rdc.androidx"));
        assertEquals("chrome_remote_desktop",
                RemoteCompatibilityReport.appFamily("com.google.chromeremotedesktop"));
        assertEquals("steam_link", RemoteCompatibilityReport.appFamily("com.valvesoftware.steamlink"));
        assertEquals("anydesk", RemoteCompatibilityReport.appFamily("com.anydesk.anydeskandroid"));
        assertEquals("teamviewer",
                RemoteCompatibilityReport.appFamily("com.teamviewer.teamviewer.market.mobile"));
        assertEquals("teamviewer",
                RemoteCompatibilityReport.appFamily("com.teamviewer.quicksupport.market"));
        assertEquals("custom", RemoteCompatibilityReport.appFamily("com.example.remote"));
        assertEquals("unknown", RemoteCompatibilityReport.appFamily(""));
    }

    @Test
    public void reportFamiliesAreStableAndUniqueForManualMatrixPlanning() {
        Set<String> families = new HashSet<>();

        for (String family : RemoteAppCatalog.reportFamilies()) {
            assertTrue("duplicate remote family: " + family, families.add(family));
        }

        assertTrue(families.contains("parsec"));
        assertTrue(families.contains("moonlight"));
        assertTrue(families.contains("microsoft_rdp"));
        assertTrue(families.contains("chrome_remote_desktop"));
        assertTrue(families.contains("steam_link"));
        assertTrue(families.contains("anydesk"));
        assertTrue(families.contains("teamviewer"));
    }

    private static boolean caseSent(JSONArray cases, String label) {
        for (int i = 0; i < cases.length(); i++) {
            JSONObject item = cases.optJSONObject(i);
            if (item != null && label.equals(item.optString("label"))) {
                return item.optBoolean("sent");
            }
        }
        throw new AssertionError("Case not found: " + label);
    }

    private static JSONObject groupSummary(JSONArray summaries, String group) {
        for (int i = 0; i < summaries.length(); i++) {
            JSONObject item = summaries.optJSONObject(i);
            if (item != null && group.equals(item.optString("group"))) {
                return item;
            }
        }
        throw new AssertionError("Group summary not found: " + group);
    }

    private static String caseManualResult(JSONArray cases, String label) {
        for (int i = 0; i < cases.length(); i++) {
            JSONObject item = cases.optJSONObject(i);
            if (item != null && label.equals(item.optString("label"))) {
                return item.optString("manualResult");
            }
        }
        throw new AssertionError("Case not found: " + label);
    }

    private static int caseAcceptedEventCount(JSONArray cases, String label) {
        for (int i = 0; i < cases.length(); i++) {
            JSONObject item = cases.optJSONObject(i);
            if (item != null && label.equals(item.optString("label"))) {
                return item.optInt("acceptedEventCount");
            }
        }
        throw new AssertionError("Case not found: " + label);
    }

    private static int caseExpectedEventCount(JSONArray cases, String label) {
        for (int i = 0; i < cases.length(); i++) {
            JSONObject item = cases.optJSONObject(i);
            if (item != null && label.equals(item.optString("label"))) {
                return item.optInt("expectedEventCount");
            }
        }
        throw new AssertionError("Case not found: " + label);
    }

    private static boolean caseAcceptedLocally(JSONArray cases, String label) {
        for (int i = 0; i < cases.length(); i++) {
            JSONObject item = cases.optJSONObject(i);
            if (item != null && label.equals(item.optString("label"))) {
                return item.optBoolean("localInputConnectionAccepted");
            }
        }
        throw new AssertionError("Case not found: " + label);
    }

    private static boolean arrayContains(JSONArray array, String value) {
        for (int i = 0; i < array.length(); i++) {
            if (value.equals(array.optString(i))) {
                return true;
            }
        }
        return false;
    }
}
