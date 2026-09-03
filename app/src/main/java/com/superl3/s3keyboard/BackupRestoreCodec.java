package com.superl3.s3keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure JSON contract for portable, versioned keyboard backups. */
final class BackupRestoreCodec {
    static final int SCHEMA_VERSION = 1;
    static final int LEGACY_SCHEMA_VERSION = 0;
    static final int MAX_BACKUP_CHARS = 2 * 1024 * 1024;

    static final String SECTION_SETTINGS = "settings";
    static final String SECTION_APP_PROFILES = "appProfiles";
    static final String SECTION_THEMES = "themes";
    static final String SECTION_TEXT_TOOLS = "textTools";
    static final String SECTION_LOCAL_PREFERENCES = "localPreferences";

    private BackupRestoreCodec() {
    }

    static String encode(
            String appVersion,
            String generatedAt,
            JSONObject sections) {
        try {
            JSONObject root = new JSONObject();
            root.put("schemaVersion", SCHEMA_VERSION);
            root.put("appVersion", safeString(appVersion));
            root.put("generatedAt", safeString(generatedAt));
            root.put("sections", sections == null ? new JSONObject() : sections);
            return root.toString(2);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode backup.", exception);
        }
    }
    static ParsedBackup parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Backup file is empty.");
        }
        if (raw.length() > MAX_BACKUP_CHARS) {
            throw new IllegalArgumentException("Backup file exceeds the size limit.");
        }
        try {
            JSONObject root = new JSONObject(raw);
            int version = root.optInt("schemaVersion", LEGACY_SCHEMA_VERSION);
            if (version == LEGACY_SCHEMA_VERSION) {
                root = migrateLegacyV0(root);
                version = SCHEMA_VERSION;
            }
            if (version != SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported backup schemaVersion: " + version);
            }
            JSONObject sections = root.optJSONObject("sections");
            if (sections == null) {
                throw new IllegalArgumentException("Backup sections are missing.");
            }
            validateSections(sections);
            return new ParsedBackup(
                    version,
                    root.optString("appVersion", ""),
                    root.optString("generatedAt", ""),
                    copyObject(sections));
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid backup JSON.", exception);
        }
    }

    private static JSONObject migrateLegacyV0(JSONObject root) throws JSONException {
        JSONObject migrated = new JSONObject();
        migrated.put("schemaVersion", SCHEMA_VERSION);
        migrated.put("appVersion", root.optString("appVersion", "legacy"));
        migrated.put("generatedAt", root.optString("generatedAt", ""));
        JSONObject sections = root.optJSONObject("sections");
        if (sections == null) {
            sections = new JSONObject();
            copyIfObject(root, sections, "settings");
            copyIfObject(root, sections, "appProfiles");
            copyIfObject(root, sections, "themes");
            copyIfObject(root, sections, "textTools");
            copyIfObject(root, sections, "localPreferences");
        }
        migrated.put("sections", sections);
        return migrated;
    }

    private static void copyIfObject(JSONObject source, JSONObject target, String name) throws JSONException {
        JSONObject object = source.optJSONObject(name);
        if (object != null) target.put(name, object);
    }
    private static void validateSections(JSONObject sections) {
        validateObjectSection(sections, SECTION_SETTINGS);
        validateObjectSection(sections, SECTION_APP_PROFILES);
        validateObjectSection(sections, SECTION_THEMES);
        validateObjectSection(sections, SECTION_TEXT_TOOLS);
        validateObjectSection(sections, SECTION_LOCAL_PREFERENCES);
    }

    private static void validateObjectSection(JSONObject sections, String name) {
        if (sections.has(name) && sections.optJSONObject(name) == null) {
            throw new IllegalArgumentException("Backup section is malformed: " + name);
        }
    }

    static JSONObject copyObject(JSONObject source) {
        if (source == null) return new JSONObject();
        try {
            return new JSONObject(source.toString());
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid backup section.", exception);
        }
    }

    static String safeString(String value) {
        return value == null ? "" : value;
    }

    static final class ParsedBackup {
        final int schemaVersion;
        final String appVersion;
        final String generatedAt;
        final JSONObject sections;

        ParsedBackup(int schemaVersion, String appVersion, String generatedAt, JSONObject sections) {
            this.schemaVersion = schemaVersion;
            this.appVersion = safeString(appVersion);
            this.generatedAt = safeString(generatedAt);
            this.sections = sections == null ? new JSONObject() : sections;
        }

        List<String> presentSections() {
            List<String> names = new ArrayList<>();
            for (String name : new String[]{
                    SECTION_SETTINGS,
                    SECTION_APP_PROFILES,
                    SECTION_THEMES,
                    SECTION_TEXT_TOOLS,
                    SECTION_LOCAL_PREFERENCES}) {
                if (sections.optJSONObject(name) != null) names.add(name);
            }
            return Collections.unmodifiableList(names);
        }

        JSONObject section(String name) {
            return copyObject(sections.optJSONObject(name));
        }
    }
}
