package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class BackupRestoreCodecTest {
    @Test
    public void roundTripPreservesKnownSections() throws Exception {
        JSONObject sections = new JSONObject();
        sections.put(BackupRestoreCodec.SECTION_SETTINGS,
                new JSONObject().put(KeyboardPreferences.HAPTIC_FEEDBACK_ENABLED, false));
        sections.put(BackupRestoreCodec.SECTION_APP_PROFILES,
                new JSONObject().put(KeyboardPreferences.APP_PROFILE_OVERRIDES_JSON,
                        "{\"version\":1,\"apps\":{}}"));
        sections.put(BackupRestoreCodec.SECTION_TEXT_TOOLS,
                new JSONObject().put(KeyboardPreferences.RESERVED_TAP_TEXT, "hello"));

        String encoded = BackupRestoreCodec.encode("0.1.0", "2026-09-03T00:00:00Z", sections);
        BackupRestoreManager.Preview preview = BackupRestoreManager.preview(encoded);

        assertEquals(1, preview.settingCount);
        assertEquals(1, preview.appProfileCount);
        assertEquals(1, preview.textToolCount);
        assertEquals("0.1.0", preview.parsed.appVersion);
    }
    @Test
    public void unknownFieldsAreIgnoredWithoutBreakingKnownData() throws Exception {
        JSONObject sections = new JSONObject();
        sections.put(BackupRestoreCodec.SECTION_SETTINGS,
                new JSONObject()
                        .put(KeyboardPreferences.HAPTIC_FEEDBACK_ENABLED, true)
                        .put("future_unknown_setting", "ignored"));
        sections.put("futureSection", new JSONObject().put("x", 1));
        JSONObject root = new JSONObject(BackupRestoreCodec.encode("x", "", sections));
        root.put("futureRootField", 42);

        BackupRestoreManager.Preview preview = BackupRestoreManager.preview(root.toString());
        assertEquals(1, preview.settingCount);
        assertTrue(preview.parsed.sections.has("futureSection"));
    }

    @Test
    public void legacyV0FixtureMigratesToCurrentSchema() throws Exception {
        String raw = readResource("/backup-v0.json");
        BackupRestoreManager.Preview preview = BackupRestoreManager.preview(raw);

        assertEquals(BackupRestoreCodec.SCHEMA_VERSION, preview.parsed.schemaVersion);
        assertEquals(2, preview.settingCount);
        assertEquals(1, preview.appProfileCount);
        assertEquals(1, preview.textToolCount);
        assertEquals(1, preview.localPreferenceCount);
    }

    @Test
    public void malformedSectionIsRejectedBeforeApply() throws Exception {
        JSONObject root = new JSONObject();
        root.put("schemaVersion", BackupRestoreCodec.SCHEMA_VERSION);
        root.put("sections", new JSONObject().put(BackupRestoreCodec.SECTION_SETTINGS, "not-an-object"));
        expectIllegalArgument(root.toString());
    }
    @Test
    public void malformedTextToolsPayloadIsRejectedBeforeApply() throws Exception {
        JSONObject sections = new JSONObject();
        sections.put(BackupRestoreCodec.SECTION_TEXT_TOOLS,
                new JSONObject().put(TextToolsStore.KEY_DATA_V1,
                        "{\"version\":1,\"items\":[{\"id\":\"bad\",\"text\":\"x\",\"updatedAt\":0}]}"));
        expectIllegalArgument(BackupRestoreCodec.encode("x", "", sections));
    }

    @Test
    public void sensitiveAndTransientKeysAreExcludedByPolicy() {
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey(ClipboardStore.KEY_ENTRIES));
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey(ClipboardStore.KEY_ENTRIES_V2));
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey(TouchBiasStore.TYPING_PATTERN_LOG));
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey(TouchBiasStore.TYPING_EVENT_JOURNAL));
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey(RemoteCompatibilityLog.KEY_ENTRIES));
        assertNull(BackupRestoreManager.backupSectionForPreferenceKey("ai_text_action_api_key"));
        assertEquals(BackupRestoreCodec.SECTION_LOCAL_PREFERENCES,
                BackupRestoreManager.backupSectionForPreferenceKey(KeyboardPreferences.AI_TEXT_ACTIONS_ENABLED));
        assertEquals(BackupRestoreCodec.SECTION_TEXT_TOOLS,
                BackupRestoreManager.backupSectionForPreferenceKey(TextToolsStore.KEY_DATA_V1));
    }

    private static void expectIllegalArgument(String raw) {
        try {
            BackupRestoreManager.preview(raw);
            fail("Expected malformed backup to be rejected");
        } catch (IllegalArgumentException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }

    private static String readResource(String name) throws Exception {
        InputStream input = BackupRestoreCodecTest.class.getResourceAsStream(name);
        if (input == null) throw new IllegalStateException("Missing fixture " + name);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        input.close();
        return output.toString(StandardCharsets.UTF_8.name());
    }
}
