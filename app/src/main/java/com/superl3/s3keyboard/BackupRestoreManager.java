package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/** Builds, validates, previews, and atomically applies portable backups. */
final class BackupRestoreManager {
    private static final String PREF_NAME = "keyboard_preferences";
    private static final String USER_THEME_PREF_NAME = "keyboard_user_themes";
    private static final String USER_THEME_KEY = "themes";

    private static final Set<String> APP_PROFILE_KEYS = setOf(
            KeyboardPreferences.APP_PROFILE_OVERRIDES_JSON,
            KeyboardPreferences.APP_PROFILE_ASCII_PACKAGES,
            KeyboardPreferences.APP_PROFILE_NUMBER_ROW_PACKAGES,
            KeyboardPreferences.APP_PROFILE_NO_COMPOSING_PACKAGES,
            KeyboardPreferences.APP_PROFILE_NO_TEXT_CONVENIENCES_PACKAGES);

    private static final Set<String> LOCAL_PREFERENCE_KEYS = setOf(
            KeyboardPreferences.AI_TEXT_ACTIONS_ENABLED,
            KeyboardPreferences.AI_TEXT_ACTION_PROVIDER_ID,
            KeyboardPreferences.AI_TEXT_ACTION_TIMEOUT_MS,
            KeyboardPreferences.AI_TEXT_ACTION_TRANSLATE_TARGET,
            KeyboardPreferences.CLIPBOARD_HISTORY_ENABLED);

    private BackupRestoreManager() {
    }
    static String exportBackup(Context context) {
        SharedPreferences prefs = prefs(context);
        JSONObject sections = new JSONObject();
        try {
            sections.put(BackupRestoreCodec.SECTION_SETTINGS, exportPreferenceSection(
                    prefs, BackupRestoreManager::isPortableSettingKey));
            sections.put(BackupRestoreCodec.SECTION_APP_PROFILES, exportPreferenceSection(
                    prefs, APP_PROFILE_KEYS::contains));
            sections.put(BackupRestoreCodec.SECTION_THEMES, exportThemes(context, prefs));
            sections.put(BackupRestoreCodec.SECTION_TEXT_TOOLS, exportTextTools(prefs));
            sections.put(BackupRestoreCodec.SECTION_LOCAL_PREFERENCES, exportPreferenceSection(
                    prefs, LOCAL_PREFERENCE_KEYS::contains));
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to build backup sections.", exception);
        }
        return BackupRestoreCodec.encode(BuildConfig.VERSION_NAME, generatedAtUtc(), sections);
    }

    static Preview preview(String raw) {
        BackupRestoreCodec.ParsedBackup parsed = BackupRestoreCodec.parse(raw);
        ValidatedSections validated = validate(parsed);
        return new Preview(
                parsed,
                validated,
                validated.settings.length(),
                validated.appProfiles.length(),
                validated.themeCount,
                validated.textToolCount,
                validated.localPreferences.length());
    }

    static void apply(Context context, Preview preview, Selection selection) {
        if (context == null || preview == null || selection == null) {
            throw new IllegalArgumentException("Restore request is incomplete.");
        }
        SharedPreferences main = prefs(context);
        SharedPreferences themes = context.getSharedPreferences(USER_THEME_PREF_NAME, Context.MODE_PRIVATE);
        Map<String, ?> beforeMain = main.getAll();
        Map<String, ?> beforeThemes = themes.getAll();
        try {
            SharedPreferences.Editor mainEditor = main.edit();
            if (selection.settings && hasSection(preview, BackupRestoreCodec.SECTION_SETTINGS)) {
                removeMatchingKeys(mainEditor, BackupRestoreManager::isPortableSettingKey);
                applyPreferenceSection(mainEditor, preview.validated.settings);
            }
            if (selection.appProfiles && hasSection(preview, BackupRestoreCodec.SECTION_APP_PROFILES)) {
                removeMatchingKeys(mainEditor, APP_PROFILE_KEYS::contains);
                applyPreferenceSection(mainEditor, preview.validated.appProfiles);
            }
            if (selection.themes && hasSection(preview, BackupRestoreCodec.SECTION_THEMES)) {
                removeMatchingKeys(mainEditor, key -> {
                    KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
                    return entry != null && entry.section == KeyboardSettingsSchema.Section.THEME;
                });
                applyThemeSection(mainEditor, themes, preview.validated.themes);
            }
            if (selection.textTools && hasSection(preview, BackupRestoreCodec.SECTION_TEXT_TOOLS)) {
                removeTextToolsKeys(mainEditor);
                applyTextToolsSection(mainEditor, preview.validated.textTools);
            }
            if (selection.localPreferences && hasSection(preview, BackupRestoreCodec.SECTION_LOCAL_PREFERENCES)) {
                removeMatchingKeys(mainEditor, LOCAL_PREFERENCE_KEYS::contains);
                applyPreferenceSection(mainEditor, preview.validated.localPreferences);
            }
            if (!mainEditor.commit()) throw new IllegalStateException("Failed to store restored settings.");
        } catch (RuntimeException exception) {
            restoreAll(main, beforeMain);
            restoreAll(themes, beforeThemes);
            throw exception;
        }
    }

    private static boolean hasSection(Preview preview, String name) {
        return preview != null
                && preview.parsed != null
                && preview.parsed.sections.optJSONObject(name) != null;
    }

    private static void removeMatchingKeys(SharedPreferences.Editor editor, KeyPredicate predicate) {
        for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entries()) {
            if (predicate.accept(entry.key)) editor.remove(entry.key);
        }
    }

    private static void removeTextToolsKeys(SharedPreferences.Editor editor) {
        editor.remove(KeyboardPreferences.RESERVED_TAP_TEXT)
                .remove(KeyboardPreferences.RESERVED_LEFT_TEXT)
                .remove(KeyboardPreferences.RESERVED_RIGHT_TEXT)
                .remove(KeyboardPreferences.RESERVED_UP_TEXT)
                .remove(TextToolsStore.KEY_DATA_V1);
    }

    static void resetPortableData(Context context, Selection selection) {
        if (context == null || selection == null) return;
        SharedPreferences main = prefs(context);
        SharedPreferences.Editor editor = main.edit();
        for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entries()) {
            if (selection.settings && isPortableSettingKey(entry.key)) editor.remove(entry.key);
            if (selection.appProfiles && APP_PROFILE_KEYS.contains(entry.key)) editor.remove(entry.key);
            if (selection.localPreferences && LOCAL_PREFERENCE_KEYS.contains(entry.key)) editor.remove(entry.key);
        }
        if (selection.themes) {
            for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entriesFor(
                    KeyboardSettingsSchema.Section.THEME)) editor.remove(entry.key);
            context.getSharedPreferences(USER_THEME_PREF_NAME, Context.MODE_PRIVATE)
                    .edit().clear().commit();
        }
        if (selection.textTools) {
            editor.remove(KeyboardPreferences.RESERVED_TAP_TEXT)
                    .remove(KeyboardPreferences.RESERVED_LEFT_TEXT)
                    .remove(KeyboardPreferences.RESERVED_RIGHT_TEXT)
                    .remove(KeyboardPreferences.RESERVED_UP_TEXT)
                    .remove(TextToolsStore.KEY_DATA_V1);
        }
        editor.commit();
    }

    private static JSONObject exportPreferenceSection(
            SharedPreferences prefs,
            KeyPredicate predicate) throws JSONException {
        JSONObject object = new JSONObject();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (predicate.accept(entry.getKey())) putPreferenceValue(object, entry.getKey(), entry.getValue());
        }
        return object;
    }

    private static JSONObject exportThemes(Context context, SharedPreferences prefs) throws JSONException {
        JSONObject object = exportPreferenceSection(
                prefs,
                key -> {
                    KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
                    return entry != null && entry.section == KeyboardSettingsSchema.Section.THEME;
                });
        object.put("userThemes", new JSONArray(UserThemeStore.encode(UserThemeStore.load(context))));
        JSONArray external = new JSONArray();
        for (UserThemeStore.UserTheme theme : ExternalThemeStore.load(context)) {
            JSONObject item = new JSONObject();
            item.put("id", theme.id);
            item.put("name", theme.name);
            item.put("json", theme.json);
            external.put(item);
        }
        object.put("externalThemes", external);
        return object;
    }
    private static JSONObject exportTextTools(SharedPreferences prefs) throws JSONException {
        JSONObject object = new JSONObject();
        for (String key : new String[]{
                KeyboardPreferences.RESERVED_TAP_TEXT,
                KeyboardPreferences.RESERVED_LEFT_TEXT,
                KeyboardPreferences.RESERVED_RIGHT_TEXT,
                KeyboardPreferences.RESERVED_UP_TEXT,
                TextToolsStore.KEY_DATA_V1}) {
            if (prefs.contains(key)) putPreferenceValue(object, key, prefs.getAll().get(key));
        }
        return object;
    }

    static String backupSectionForPreferenceKey(String key) {
        if (APP_PROFILE_KEYS.contains(key)) return BackupRestoreCodec.SECTION_APP_PROFILES;
        if (LOCAL_PREFERENCE_KEYS.contains(key)) return BackupRestoreCodec.SECTION_LOCAL_PREFERENCES;
        if (TextToolsStore.KEY_DATA_V1.equals(key)
                || KeyboardPreferences.RESERVED_TAP_TEXT.equals(key)
                || KeyboardPreferences.RESERVED_LEFT_TEXT.equals(key)
                || KeyboardPreferences.RESERVED_RIGHT_TEXT.equals(key)
                || KeyboardPreferences.RESERVED_UP_TEXT.equals(key)) {
            return BackupRestoreCodec.SECTION_TEXT_TOOLS;
        }
        KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
        if (entry != null && entry.section == KeyboardSettingsSchema.Section.THEME) {
            return BackupRestoreCodec.SECTION_THEMES;
        }
        return isPortableSettingKey(key) ? BackupRestoreCodec.SECTION_SETTINGS : null;
    }

    static boolean isPortableSettingKey(String key) {
        KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
        if (entry == null || !entry.userFacing) return false;
        if (entry.section == KeyboardSettingsSchema.Section.THEME
                || entry.section == KeyboardSettingsSchema.Section.RESERVED_PHRASES
                || entry.section == KeyboardSettingsSchema.Section.PRIVACY_DEBUG
                || entry.section == KeyboardSettingsSchema.Section.LEGACY) return false;
        return !APP_PROFILE_KEYS.contains(key);
    }

    private static void putPreferenceValue(JSONObject object, String key, Object value) throws JSONException {
        if (value instanceof Boolean || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double || value instanceof String) {
            object.put(key, value);
        }
    }

    private static ValidatedSections validate(BackupRestoreCodec.ParsedBackup parsed) {
        JSONObject settings = validatePreferenceSection(parsed.section(BackupRestoreCodec.SECTION_SETTINGS),
                BackupRestoreManager::isPortableSettingKey);
        JSONObject appProfiles = validatePreferenceSection(parsed.section(BackupRestoreCodec.SECTION_APP_PROFILES),
                APP_PROFILE_KEYS::contains);
        validateAppProfiles(appProfiles);
        JSONObject localPreferences = validatePreferenceSection(
                parsed.section(BackupRestoreCodec.SECTION_LOCAL_PREFERENCES),
                LOCAL_PREFERENCE_KEYS::contains);
        JSONObject themes = validateThemes(parsed.section(BackupRestoreCodec.SECTION_THEMES));
        JSONObject textTools = validateTextTools(parsed.section(BackupRestoreCodec.SECTION_TEXT_TOOLS));
        return new ValidatedSections(settings, appProfiles, themes, textTools, localPreferences,
                countThemes(themes), countTextTools(textTools));
    }
    private static void validateAppProfiles(JSONObject appProfiles) {
        if (appProfiles == null || !appProfiles.has(KeyboardPreferences.APP_PROFILE_OVERRIDES_JSON)) return;
        String raw = appProfiles.optString(KeyboardPreferences.APP_PROFILE_OVERRIDES_JSON, "");
        if (raw.isEmpty()) return;
        try {
            JSONObject root = new JSONObject(raw);
            if (root.optInt("version", -1) != AppInputProfileOverrides.SCHEMA_VERSION
                    || root.optJSONObject("apps") == null) {
                throw new IllegalArgumentException("Unsupported app-profile backup data.");
            }
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid app-profile backup data.", exception);
        }
    }

    private static JSONObject validatePreferenceSection(JSONObject source, KeyPredicate allowed) {
        JSONObject validated = new JSONObject();
        if (source == null) return validated;
        try {
            java.util.Iterator<String> keys = source.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!allowed.accept(key)) continue;
                Object value = source.opt(key);
                if (!(value instanceof Boolean || value instanceof Integer || value instanceof Long
                        || value instanceof Double || value instanceof String)) {
                    throw new IllegalArgumentException("Unsupported preference value for " + key);
                }
                validated.put(key, value);
            }
            return validated;
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid preference section.", exception);
        }
    }

    private static JSONObject validateThemes(JSONObject source) {
        JSONObject validated = validatePreferenceSection(source, key -> {
            KeyboardSettingsSchema.Entry entry = KeyboardSettingsSchema.find(key);
            return entry != null && entry.section == KeyboardSettingsSchema.Section.THEME;
        });
        if (source == null) return validated;
        try {
            JSONArray userThemes = source.optJSONArray("userThemes");
            JSONArray externalThemes = source.optJSONArray("externalThemes");
            validated.put("userThemes", validateThemeArray(userThemes, false));
            validated.put("externalThemes", validateThemeArray(externalThemes, true));
            return validated;
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid theme backup section.", exception);
        }
    }

    private static JSONArray validateThemeArray(JSONArray source, boolean external) throws JSONException {
        JSONArray validated = new JSONArray();
        if (source == null) return validated;
        int count = Math.min(source.length(), ExternalThemeStore.MAX_THEME_FILES);
        for (int i = 0; i < count; i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null) throw new IllegalArgumentException("Malformed theme entry.");
            String id = item.optString("id", "").trim();
            String name = item.optString("name", "").trim();
            String json = item.optString("json", "");
            if (id.isEmpty() || json.isEmpty()) throw new IllegalArgumentException("Theme entry is incomplete.");
            KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);
            JSONObject normalized = new JSONObject();
            normalized.put("id", id);
            normalized.put("name", name.isEmpty() ? "Restored Theme" : name);
            normalized.put("json", json);
            normalized.put("external", external);
            validated.put(normalized);
        }
        return validated;
    }
    private static JSONObject validateTextTools(JSONObject source) {
        JSONObject validated = validatePreferenceSection(source, key ->
                KeyboardPreferences.RESERVED_TAP_TEXT.equals(key)
                        || KeyboardPreferences.RESERVED_LEFT_TEXT.equals(key)
                        || KeyboardPreferences.RESERVED_RIGHT_TEXT.equals(key)
                        || KeyboardPreferences.RESERVED_UP_TEXT.equals(key)
                        || TextToolsStore.KEY_DATA_V1.equals(key));
        String encoded = validated.optString(TextToolsStore.KEY_DATA_V1, "");
        if (!encoded.isEmpty()) validateTextToolsPayload(encoded);
        return validated;
    }

    private static void validateTextToolsPayload(String raw) {
        try {
            JSONObject root = new JSONObject(raw);
            if (root.optInt("version", 0) != TextToolsStore.SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported Text Tools schema.");
            }
            JSONArray items = root.optJSONArray("items");
            if (items == null) throw new IllegalArgumentException("Text Tools items are missing.");
            if (items.length() > TextToolsStore.MAX_ITEMS) {
                throw new IllegalArgumentException("Text Tools item count exceeds the limit.");
            }
            List<TextToolsStore.Item> decoded = TextToolsStore.decode(raw);
            if (decoded.size() != items.length()) {
                throw new IllegalArgumentException("Text Tools backup contains invalid items.");
            }
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid Text Tools backup data.", exception);
        }
    }

    private static int countThemes(JSONObject themes) {
        if (themes == null) return 0;
        JSONArray users = themes.optJSONArray("userThemes");
        JSONArray external = themes.optJSONArray("externalThemes");
        return (users == null ? 0 : users.length()) + (external == null ? 0 : external.length());
    }

    private static int countTextTools(JSONObject textTools) {
        if (textTools == null) return 0;
        int count = 0;
        for (String key : new String[]{KeyboardPreferences.RESERVED_TAP_TEXT,
                KeyboardPreferences.RESERVED_LEFT_TEXT, KeyboardPreferences.RESERVED_RIGHT_TEXT,
                KeyboardPreferences.RESERVED_UP_TEXT}) if (textTools.has(key)) count++;
        String encoded = textTools.optString(TextToolsStore.KEY_DATA_V1, "");
        if (!encoded.isEmpty()) count += TextToolsStore.decode(encoded).size();
        return count;
    }
    private static void applyPreferenceSection(SharedPreferences.Editor editor, JSONObject section) {
        if (section == null) return;
        java.util.Iterator<String> keys = section.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = section.opt(key);
            if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) editor.putInt(key, (Integer) value);
            else if (value instanceof Long) editor.putLong(key, (Long) value);
            else if (value instanceof Number) editor.putFloat(key, ((Number) value).floatValue());
            else if (value instanceof String) editor.putString(key, (String) value);
        }
    }

    private static void applyThemeSection(
            SharedPreferences.Editor mainEditor,
            SharedPreferences themePrefs,
            JSONObject themes) {
        if (themes == null) return;
        JSONObject preferences = new JSONObject();
        java.util.Iterator<String> keys = themes.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                if (!"userThemes".equals(key) && !"externalThemes".equals(key)) {
                    preferences.put(key, themes.opt(key));
                }
            }
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid theme preferences.", exception);
        }
        applyPreferenceSection(mainEditor, preferences);

        JSONArray userThemes = themes.optJSONArray("userThemes");
        JSONArray externalThemes = themes.optJSONArray("externalThemes");
        JSONArray merged = new JSONArray();
        appendRestoredThemes(merged, userThemes, false);
        appendRestoredThemes(merged, externalThemes, true);
        if (!themePrefs.edit().putString(USER_THEME_KEY, merged.toString()).commit()) {
            throw new IllegalStateException("Failed to restore custom themes.");
        }
    }

    private static void appendRestoredThemes(JSONArray target, JSONArray source, boolean external) {
        if (source == null) return;
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null) continue;
            JSONObject restored = new JSONObject();
            try {
                String id = item.optString("id", "");
                restored.put("id", id);
                restored.put("name", item.optString("name", "Restored Theme"));
                restored.put("json", item.optString("json", "{}"));
                target.put(restored);
            } catch (JSONException exception) {
                throw new IllegalArgumentException("Failed to normalize restored theme.", exception);
            }
        }
    }
    private static void applyTextToolsSection(SharedPreferences.Editor editor, JSONObject textTools) {
        if (textTools == null) return;
        applyPreferenceSection(editor, textTools);
    }

    private static void restoreAll(SharedPreferences preferences, Map<String, ?> values) {
        SharedPreferences.Editor editor = preferences.edit().clear();
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
                else if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
                else if (value instanceof Long) editor.putLong(entry.getKey(), (Long) value);
                else if (value instanceof Float) editor.putFloat(entry.getKey(), (Float) value);
                else if (value instanceof String) editor.putString(entry.getKey(), (String) value);
                else if (value instanceof Set) {
                    @SuppressWarnings("unchecked") Set<String> strings = (Set<String>) value;
                    editor.putStringSet(entry.getKey(), strings);
                }
            }
        }
        editor.commit();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String generatedAtUtc() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    private static Set<String> setOf(String... values) {
        Set<String> result = new HashSet<>();
        if (values != null) {
            for (String value : values) if (value != null) result.add(value);
        }
        return result;
    }

    interface KeyPredicate {
        boolean accept(String key);
    }

    static final class Selection {
        final boolean settings;
        final boolean appProfiles;
        final boolean themes;
        final boolean textTools;
        final boolean localPreferences;

        Selection(boolean settings, boolean appProfiles, boolean themes, boolean textTools, boolean localPreferences) {
            this.settings = settings;
            this.appProfiles = appProfiles;
            this.themes = themes;
            this.textTools = textTools;
            this.localPreferences = localPreferences;
        }

        static Selection all() {
            return new Selection(true, true, true, true, true);
        }
    }
    static final class Preview {
        final BackupRestoreCodec.ParsedBackup parsed;
        final ValidatedSections validated;
        final int settingCount;
        final int appProfileCount;
        final int themeCount;
        final int textToolCount;
        final int localPreferenceCount;

        Preview(
                BackupRestoreCodec.ParsedBackup parsed,
                ValidatedSections validated,
                int settingCount,
                int appProfileCount,
                int themeCount,
                int textToolCount,
                int localPreferenceCount) {
            this.parsed = parsed;
            this.validated = validated;
            this.settingCount = settingCount;
            this.appProfileCount = appProfileCount;
            this.themeCount = themeCount;
            this.textToolCount = textToolCount;
            this.localPreferenceCount = localPreferenceCount;
        }
    }

    private static final class ValidatedSections {
        final JSONObject settings;
        final JSONObject appProfiles;
        final JSONObject themes;
        final JSONObject textTools;
        final JSONObject localPreferences;
        final int themeCount;
        final int textToolCount;

        ValidatedSections(
                JSONObject settings,
                JSONObject appProfiles,
                JSONObject themes,
                JSONObject textTools,
                JSONObject localPreferences,
                int themeCount,
                int textToolCount) {
            this.settings = settings;
            this.appProfiles = appProfiles;
            this.themes = themes;
            this.textTools = textTools;
            this.localPreferences = localPreferences;
            this.themeCount = themeCount;
            this.textToolCount = textToolCount;
        }
    }
}
