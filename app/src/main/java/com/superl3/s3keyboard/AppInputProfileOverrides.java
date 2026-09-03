package com.superl3.s3keyboard;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class AppInputProfileOverrides {
    static final int SCHEMA_VERSION = 1;
    static final AppInputProfileOverrides EMPTY = new AppInputProfileOverrides("", "", "", "");

    final String asciiPackages;
    final String numberRowPackages;
    final String noComposingPackages;
    final String noTextConveniencesPackages;
    private final Map<String, AppInputProfileOverride> records;

    AppInputProfileOverrides(
            String asciiPackages,
            String numberRowPackages,
            String noComposingPackages,
            String noTextConveniencesPackages) {
        this(asciiPackages, numberRowPackages, noComposingPackages,
                noTextConveniencesPackages, Collections.emptyMap());
    }

    private AppInputProfileOverrides(
            String asciiPackages,
            String numberRowPackages,
            String noComposingPackages,
            String noTextConveniencesPackages,
            Map<String, AppInputProfileOverride> records) {
        this.asciiPackages = RuntimeDefaults.stringOrDefault(asciiPackages, "");
        this.numberRowPackages = RuntimeDefaults.stringOrDefault(numberRowPackages, "");
        this.noComposingPackages = RuntimeDefaults.stringOrDefault(noComposingPackages, "");
        this.noTextConveniencesPackages = RuntimeDefaults.stringOrDefault(noTextConveniencesPackages, "");
        this.records = Collections.unmodifiableMap(new LinkedHashMap<>(records));
    }

    static AppInputProfileOverrides decode(
            String json,
            String asciiPackages,
            String numberRowPackages,
            String noComposingPackages,
            String noTextConveniencesPackages) {
        Map<String, AppInputProfileOverride> records = new LinkedHashMap<>();
        String safeJson = RuntimeDefaults.trimmedStringOrEmpty(json);
        if (!safeJson.isEmpty()) {
            try {
                JSONObject root = new JSONObject(safeJson);
                if (root.optInt("version", -1) == SCHEMA_VERSION) {
                    JSONObject apps = root.optJSONObject("apps");
                    if (apps != null) {
                        java.util.Iterator<String> keys = apps.keys();
                        while (keys.hasNext()) {
                            String packageName = AppPackageCatalog.normalizePackageName(keys.next());
                            JSONObject object = apps.optJSONObject(packageName);
                            AppInputProfileOverride value = decodeRecord(object);
                            if (!packageName.isEmpty() && !value.isAuto()) {
                                records.put(packageName, value);
                            }
                        }
                    }
                }
            } catch (JSONException ignored) {
                records.clear();
            }
        }
        return new AppInputProfileOverrides(
                asciiPackages,
                numberRowPackages,
                noComposingPackages,
                noTextConveniencesPackages,
                records);
    }

    AppInputProfileOverride forPackage(String packageName) {
        String normalized = AppPackageCatalog.normalizePackageName(packageName);
        AppInputProfileOverride record = records.get(normalized);
        return record == null ? AppInputProfileOverride.AUTO : record;
    }

    AppInputProfileOverrides withOverride(String packageName, AppInputProfileOverride override) {
        String normalized = AppPackageCatalog.normalizePackageName(packageName);
        if (normalized.isEmpty()) {
            return this;
        }
        Map<String, AppInputProfileOverride> next = new LinkedHashMap<>(records);
        AppInputProfileOverride safe = override == null ? AppInputProfileOverride.AUTO : override;
        if (safe.isAuto()) {
            next.remove(normalized);
        } else {
            next.put(normalized, safe);
        }
        return new AppInputProfileOverrides(
                asciiPackages, numberRowPackages, noComposingPackages,
                noTextConveniencesPackages, next);
    }

    AppInputProfileOverrides withoutOverride(String packageName) {
        return withOverride(packageName, AppInputProfileOverride.AUTO);
    }

    boolean hasStoredOverride(String packageName) {
        return records.containsKey(AppPackageCatalog.normalizePackageName(packageName));
    }

    int storedOverrideCount() {
        return records.size();
    }

    AppInputProfile apply(String packageName, AppInputProfile profile) {
        AppInputProfile safeProfile = RuntimeDefaults.appInputProfile(profile);
        Boolean preferAscii = KeyboardPreferences.packageListContains(asciiPackages, packageName)
                ? Boolean.TRUE : null;
        Boolean forceNumberRow = KeyboardPreferences.packageListContains(numberRowPackages, packageName)
                ? Boolean.TRUE : null;
        Boolean allowComposing = KeyboardPreferences.packageListContains(noComposingPackages, packageName)
                ? Boolean.FALSE : null;
        Boolean allowTextConveniences = KeyboardPreferences.packageListContains(
                noTextConveniencesPackages, packageName) ? Boolean.FALSE : null;
        AppInputProfileOverride record = forPackage(packageName);
        if (record.keyboardMode != null) {
            preferAscii = record.keyboardMode == KeyboardMode.ENGLISH;
        }
        if (record.numberRowVisible != null) {
            forceNumberRow = record.numberRowVisible;
        }
        if (record.allowComposingText != null) {
            allowComposing = record.allowComposingText;
        }
        if (record.allowTextConveniences != null) {
            allowTextConveniences = record.allowTextConveniences;
        }
        if (preferAscii == null && forceNumberRow == null
                && allowComposing == null && allowTextConveniences == null) {
            return safeProfile;
        }
        return safeProfile.withPolicyOverrides(
                preferAscii,
                forceNumberRow,
                allowComposing,
                allowTextConveniences,
                "user_app_profile_override");
    }

    String encode() {
        JSONObject apps = new JSONObject();
        try {
            for (Map.Entry<String, AppInputProfileOverride> entry : records.entrySet()) {
                apps.put(entry.getKey(), encodeRecord(entry.getValue()));
            }
            return new JSONObject()
                    .put("version", SCHEMA_VERSION)
                    .put("apps", apps)
                    .toString();
        } catch (JSONException ignored) {
            return "{\"version\":1,\"apps\":{}}";
        }
    }

    private static AppInputProfileOverride decodeRecord(JSONObject object) {
        if (object == null) {
            return AppInputProfileOverride.AUTO;
        }
        KeyboardMode keyboardMode = null;
        String language = object.optString("language", "auto");
        if ("hangul".equals(language)) {
            keyboardMode = KeyboardMode.HANGUL;
        } else if ("english".equals(language)) {
            keyboardMode = KeyboardMode.ENGLISH;
        }
        return new AppInputProfileOverride(
                keyboardMode,
                nullableBoolean(object, "numberRow"),
                nullableBoolean(object, "composing"),
                nullableBoolean(object, "textConveniences"),
                nullableBoolean(object, "remote"));
    }
    private static JSONObject encodeRecord(AppInputProfileOverride value) throws JSONException {
        JSONObject object = new JSONObject();
        if (value.keyboardMode != null) {
            object.put("language", value.keyboardMode == KeyboardMode.ENGLISH ? "english" : "hangul");
        }
        putNullable(object, "numberRow", value.numberRowVisible);
        putNullable(object, "composing", value.allowComposingText);
        putNullable(object, "textConveniences", value.allowTextConveniences);
        putNullable(object, "remote", value.remoteMode);
        return object;
    }

    private static Boolean nullableBoolean(JSONObject object, String key) {
        if (!object.has(key) || object.isNull(key)) {
            return null;
        }
        Object value = object.opt(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    private static void putNullable(JSONObject object, String key, Boolean value)
            throws JSONException {
        if (value != null) {
            object.put(key, value);
        }
    }
}
