package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public final class KeyboardSettingsSchemaTest {
    @Test
    public void schemaCoversEveryPackagePreferenceKey() throws Exception {
        for (Field field : KeyboardPreferences.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (field.getType() != String.class
                    || !Modifier.isStatic(modifiers)
                    || !Modifier.isFinal(modifiers)
                    || Modifier.isPrivate(modifiers)
                    || field.getName().startsWith("DEFAULT_")) {
                continue;
            }
            String key = (String) field.get(null);
            assertTrue(
                    "Preference key missing from KeyboardSettingsSchema: " + field.getName() + "=" + key,
                    KeyboardSettingsSchema.contains(key));
        }
    }

    @Test
    public void schemaKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entries()) {
            assertFalse("Duplicate settings schema key: " + entry.key, keys.contains(entry.key));
            keys.add(entry.key);
        }
    }

    @Test
    public void schemaClassifiesPrivacyAndCompatibilityRisks() {
        assertEntry(
                KeyboardPreferences.CLIPBOARD_HISTORY_ENABLED,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_TEXT);
        assertEntry(
                KeyboardPreferences.REMOTE_AUTO_MODE_PACKAGES,
                KeyboardSettingsSchema.Section.REMOTE,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
        assertEntry(
                KeyboardPreferences.APP_PROFILE_ASCII_PACKAGES,
                KeyboardSettingsSchema.Section.REMOTE,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
        assertEntry(
                KeyboardPreferences.APP_PROFILE_NUMBER_ROW_PACKAGES,
                KeyboardSettingsSchema.Section.REMOTE,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
        assertEntry(
                KeyboardPreferences.APP_PROFILE_NO_COMPOSING_PACKAGES,
                KeyboardSettingsSchema.Section.REMOTE,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
        assertEntry(
                KeyboardPreferences.APP_PROFILE_NO_TEXT_CONVENIENCES_PACKAGES,
                KeyboardSettingsSchema.Section.REMOTE,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
        assertEntry(
                KeyboardPreferences.DEBUG_KEY_BOUNDS_OVERLAY_ENABLED,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_DIAGNOSTIC);
        assertEntry(
                KeyboardPreferences.GESTURE_THRESHOLD_DP,
                KeyboardSettingsSchema.Section.INPUT_FEEL,
                KeyboardSettingsSchema.StorageRisk.NONE);
        assertEntry(
                KeyboardPreferences.KEY_DISPLAY_OVERRIDES,
                KeyboardSettingsSchema.Section.THEME,
                KeyboardSettingsSchema.StorageRisk.NONE);
        assertInternalEntry(
                ClipboardStore.KEY_ENTRIES,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_TEXT);
        assertInternalEntry(
                TouchBiasStore.TYPING_PATTERN_LOG,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_DIAGNOSTIC);
        assertInternalEntry(
                TouchBiasStore.TYPING_EVENT_JOURNAL,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_TEXT);
        assertInternalEntry(
                TouchBiasStore.TOUCH_BIAS_STATS,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_DIAGNOSTIC);
        assertInternalEntry(
                TouchBiasStore.DINGUL_TOUCH_PROFILE,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.LOCAL_DIAGNOSTIC);
        assertInternalEntry(
                RemoteCompatibilityLog.KEY_ENTRIES,
                KeyboardSettingsSchema.Section.PRIVACY_DEBUG,
                KeyboardSettingsSchema.StorageRisk.COMPATIBILITY);
    }

    @Test
    public void sectionLookupExposesSettingsGroups() {
        assertTrue(KeyboardSettingsSchema.entriesFor(KeyboardSettingsSchema.Section.REMOTE).size() >= 5);
        assertTrue(KeyboardSettingsSchema.entriesFor(KeyboardSettingsSchema.Section.APPEARANCE).size() >= 10);
        assertTrue(KeyboardSettingsSchema.entriesFor(KeyboardSettingsSchema.Section.INPUT_FEEL).size() >= 10);
    }

    private static void assertEntry(
            String key,
            KeyboardSettingsSchema.Section section,
            KeyboardSettingsSchema.StorageRisk risk) {
        KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
        assertNotNull("Missing schema entry for " + key, entry);
        assertEquals(section, entry.section);
        assertEquals(risk, entry.risk);
        assertTrue(entry.userFacing);
    }

    private static void assertInternalEntry(
            String key,
            KeyboardSettingsSchema.Section section,
            KeyboardSettingsSchema.StorageRisk risk) {
        KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
        assertNotNull("Missing schema entry for " + key, entry);
        assertEquals(section, entry.section);
        assertEquals(risk, entry.risk);
        assertFalse(entry.userFacing);
    }
}
