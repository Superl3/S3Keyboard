package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class ReleaseSafeDiagnosticsTest {
    @Test
    public void reportWhitelistsSafeFieldsAndDropsForbiddenPayloads() throws Exception {
        JSONObject session = new JSONObject();
        session.put("packageCategory", "other");
        session.put("packageHash", "sha256:123456789abc");
        session.put("profileId", "standard");
        session.put("keyboardMode", "english");
        session.put("layoutId", "qwerty");
        session.put("rawTypedText", "RAW_TYPED_SECRET");
        session.put("clipboardText", "CLIPBOARD_SECRET");
        session.put("providerToken", "PROVIDER_SECRET");
        JSONObject state = new JSONObject();
        state.put("session", session);
        state.put("actions", new JSONArray().put("text_input").put("RAW_ACTION_SECRET"));
        String report = ReleaseSafeDiagnostics.buildReport(state, null).toString();

        assertTrue(report.contains("\"rawTypedTextIncluded\":false"));
        assertTrue(report.contains("\"recentActionCategories\":[\"text_input\",\"other\"]"));
        assertFalse(report.contains("RAW_TYPED_SECRET"));
        assertFalse(report.contains("CLIPBOARD_SECRET"));
        assertFalse(report.contains("PROVIDER_SECRET"));
        assertFalse(report.contains("RAW_ACTION_SECRET"));
    }
    @Test
    public void packageIdentityIsCategorizedOrHashedWithoutRawName() {
        assertTrue(ReleaseSafeDiagnostics.packageCategory("com.android.chrome").equals("browser"));
        assertTrue(ReleaseSafeDiagnostics.packageHash("com.android.chrome").isEmpty());

        String customPackage = "com.example.private.customer.editor";
        String hash = ReleaseSafeDiagnostics.packageHash(customPackage);
        assertTrue(ReleaseSafeDiagnostics.packageCategory(customPackage).equals("other"));
        assertTrue(hash.startsWith("sha256:"));
        assertFalse(hash.contains(customPackage));
    }

    @Test
    public void gestureCategoriesNeverEchoTextOrCommands() {
        assertTrue(ReleaseSafeDiagnostics.categoryForGesture("typed secret").equals("text_input"));
        assertTrue(ReleaseSafeDiagnostics.categoryForGesture(KeyboardCommands.CMD_DELETE).equals("delete"));
        assertTrue(ReleaseSafeDiagnostics.categoryForGesture(KeyboardCommands.CMD_REMOTE_F12).equals("remote_command"));
        assertFalse(ReleaseSafeDiagnostics.categoryForGesture("typed secret").contains("secret"));
    }
}
