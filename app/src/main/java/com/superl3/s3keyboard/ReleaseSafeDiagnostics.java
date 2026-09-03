package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

final class ReleaseSafeDiagnostics {
    private static final String PREF_NAME = "keyboard_preferences";
    static final String KEY_STATE_V1 = "release_safe_diagnostics_v1";
    private static final int SCHEMA_VERSION = 1;
    private static final int MAX_ACTIONS = 12;

    private ReleaseSafeDiagnostics() {
    }

    static void recordSession(
            Context context,
            EditorInfo info,
            AppInputProfile profile,
            EditorInputPolicy policy,
            KeyboardSettings settings,
            KeyboardLayoutProfile layoutProfile,
            boolean oneFingerEnabled) {
        if (context == null) return;
        JSONObject root = loadState(context);
        JSONObject session = new JSONObject();        try {
            String packageName = info == null ? "" : info.packageName;
            session.put("packageCategory", packageCategory(packageName));
            session.put("packageHash", packageHash(packageName));
            session.put("profileId", safeToken(profile == null ? "standard" : profile.id, "standard"));
            int inputType = info == null ? 0 : info.inputType;
            session.put("inputClass", inputType & InputType.TYPE_MASK_CLASS);
            session.put("inputVariation", inputType & InputType.TYPE_MASK_VARIATION);
            session.put("inputFlags", inputType & ~(InputType.TYPE_MASK_CLASS | InputType.TYPE_MASK_VARIATION));
            session.put("imeAction", info == null ? 0 : info.imeOptions & EditorInfo.IME_MASK_ACTION);
            session.put("policy", policyJson(policy));
            KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
            session.put("keyboardMode", safeSettings.keyboardMode.preferenceValue);
            session.put("layoutId", layoutProfile == null ? "unknown" : layoutProfile.preferenceValue);
            session.put("remoteMode", safeSettings.remoteModeEnabled);
            session.put("oneFingerMode", oneFingerEnabled);
            session.put("themeId", safeThemeId(KeyboardPreferences.loadSelectedThemeId(context)));
            session.put("materialId", safeToken(safeSettings.visualEffects.materialStyle, "unknown"));
            session.put("capturedAtMs", System.currentTimeMillis());
            root.put("schemaVersion", SCHEMA_VERSION);
            root.put("session", session);
            root.put("actions", safeActions(root.optJSONArray("actions")));
            saveState(context, root);
        } catch (Exception ignored) {
            // Release-safe diagnostics must never affect input handling.
        }
        recordActionCategory(context, "session");
    }

    static void recordGesture(Context context, String value) {
        recordActionCategory(context, categoryForGesture(value));
    }
    static String buildReport(Context context) {
        JSONObject state = context == null ? new JSONObject() : loadState(context);
        LocalDataControlsController.Summary summary = context == null
                ? null : new LocalDataControlsController(context).summary();
        return buildReport(state, summary).toString();
    }

    static JSONObject buildReport(
            JSONObject state,
            LocalDataControlsController.Summary summary) {
        JSONObject report = new JSONObject();
        JSONObject session = state == null ? null : state.optJSONObject("session");
        try {
            report.put("schemaVersion", SCHEMA_VERSION);
            report.put("generatedAtMs", System.currentTimeMillis());
            report.put("appVersion", BuildConfig.VERSION_NAME);
            report.put("redaction", redactionJson());
            report.put("session", safeSession(session));
            report.put("recentActionCategories", safeActions(
                    state == null ? null : state.optJSONArray("actions")));
            report.put("inputLearning", learningSummary(summary));
        } catch (Exception ignored) {
            // Keep a partial but safe report instead of falling back to raw state.
        }
        return report;
    }

    static void clear(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_STATE_V1).apply();
    }

    private static void recordActionCategory(Context context, String category) {
        if (context == null) return;
        JSONObject root = loadState(context);
        JSONArray actions = safeActions(root.optJSONArray("actions"));        String safeCategory = safeActionCategory(category);
        if (actions.length() == 0 || !safeCategory.equals(actions.optString(actions.length() - 1))) {
            actions.put(safeCategory);
        }
        while (actions.length() > MAX_ACTIONS) actions.remove(0);
        try {
            root.put("schemaVersion", SCHEMA_VERSION);
            root.put("actions", actions);
            saveState(context, root);
        } catch (Exception ignored) {
            // Best-effort local category history only.
        }
    }

    static String categoryForGesture(String value) {
        if (!KeyboardCommands.isCommand(value)) return "text_input";
        if (KeyboardCommands.isRemoteCommand(value)) return "remote_command";
        if (KeyboardCommands.CMD_DELETE.equals(value) || KeyboardCommands.CMD_DELETE_WORD.equals(value)) return "delete";
        if (KeyboardCommands.CMD_ENTER.equals(value) || KeyboardCommands.CMD_NEWLINE.equals(value)) return "enter";
        if (KeyboardCommands.CMD_MOVE_LEFT.equals(value) || KeyboardCommands.CMD_MOVE_RIGHT.equals(value)) return "cursor";
        if (KeyboardCommands.CMD_CLIPBOARD_PANEL.equals(value)
                || KeyboardCommands.CMD_RESERVED_PHRASES.equals(value)
                || KeyboardCommands.CMD_RESERVED_LEFT.equals(value)
                || KeyboardCommands.CMD_RESERVED_RIGHT.equals(value)
                || KeyboardCommands.CMD_RESERVED_UP.equals(value)) return "text_tools";
        if (KeyboardCommands.CMD_CORRECT_TEXT.equals(value) || KeyboardCommands.CMD_TOOLS.equals(value)) return "text_action";
        if (KeyboardCommands.CMD_VOICE_INPUT.equals(value)) return "voice";
        if (KeyboardCommands.CMD_UNDO.equals(value)) return "undo";
        if (KeyboardCommands.CMD_SETTINGS.equals(value) || KeyboardCommands.CMD_QUICK_SETTINGS.equals(value)) return "settings";
        return "keyboard_command";
    }

    private static JSONObject safeSession(JSONObject source) {
        JSONObject safe = new JSONObject();        if (source == null) return safe;
        String[] strings = {"packageCategory", "packageHash", "profileId", "keyboardMode", "layoutId", "themeId", "materialId"};
        String[] integers = {"inputClass", "inputVariation", "inputFlags", "imeAction"};
        String[] booleans = {"remoteMode", "oneFingerMode"};
        try {
            for (String key : strings) safe.put(key, source.optString(key, ""));
            for (String key : integers) safe.put(key, source.optInt(key, 0));
            for (String key : booleans) safe.put(key, source.optBoolean(key, false));
            safe.put("capturedAtMs", source.optLong("capturedAtMs", 0L));
            safe.put("policy", safePolicy(source.optJSONObject("policy")));
        } catch (Exception ignored) {
            // Whitelist copy only.
        }
        return safe;
    }

    private static JSONObject policyJson(EditorInputPolicy policy) {
        EditorInputPolicy safe = RuntimeDefaults.editorInputPolicy(policy);
        JSONObject object = new JSONObject();
        try {
            object.put("surface", safe.surface.name());
            object.put("password", safe.password);
            object.put("numberLike", safe.numberLike);
            object.put("uriLike", safe.uriLike);
            object.put("emailLike", safe.emailLike);
            object.put("webEditLike", safe.webEditLike);
            object.put("rawKeyInput", safe.rawKeyInput);
            object.put("multiline", safe.multiline);
            object.put("allowComposingText", safe.allowComposingText);
            object.put("allowTextConveniences", safe.allowTextConveniences);
        } catch (Exception ignored) { }
        return object;
    }

    private static JSONObject safePolicy(JSONObject source) {
        JSONObject safe = new JSONObject();        if (source == null) return safe;
        try {
            safe.put("surface", safeToken(source.optString("surface"), "UNKNOWN"));
            String[] keys = {"password", "numberLike", "uriLike", "emailLike", "webEditLike",
                    "rawKeyInput", "multiline", "allowComposingText", "allowTextConveniences"};
            for (String key : keys) safe.put(key, source.optBoolean(key, false));
        } catch (Exception ignored) { }
        return safe;
    }

    private static JSONObject learningSummary(LocalDataControlsController.Summary summary) {
        JSONObject object = new JSONObject();
        try {
            object.put("includedContent", false);
            object.put("touchBiasPresent", summary != null && summary.touchBiasStatsPresent);
            object.put("typingPatternEventCount", summary == null ? 0 : summary.typingPatternEventCount);
            object.put("typingJournalEventCount", summary == null ? 0 : summary.typingEventJournalEventCount);
            object.put("dingulProfilePresent", summary != null && summary.dingulTouchProfilePresent);
        } catch (Exception ignored) { }
        return object;
    }

    private static JSONObject redactionJson() {
        JSONObject object = new JSONObject();
        try {
            object.put("rawTypedTextIncluded", false);
            object.put("clipboardContentIncluded", false);
            object.put("savedPhraseContentIncluded", false);
            object.put("aiRequestOrResultIncluded", false);
            object.put("providerCredentialsIncluded", false);
            object.put("rawPackageNameIncluded", false);
            object.put("touchGeometryIncluded", false);
            object.put("debugProbeDataIncluded", false);
        } catch (Exception ignored) { }
        return object;
    }

    private static JSONArray safeActions(JSONArray source) {
        JSONArray safe = new JSONArray();        if (source == null) return safe;
        int start = Math.max(0, source.length() - MAX_ACTIONS);
        for (int i = start; i < source.length(); i++) {
            safe.put(safeActionCategory(source.optString(i, "other")));
        }
        return safe;
    }

    private static String safeActionCategory(String category) {
        if (category == null) return "other";
        switch (category) {
            case "session": case "text_input": case "delete": case "enter":
            case "cursor": case "text_tools": case "text_action": case "voice":
            case "undo": case "settings": case "remote_command": case "keyboard_command":
                return category;
            default:
                return "other";
        }
    }

    static String packageCategory(String packageName) {
        String normalized = AppPackageCatalog.normalizePackageName(packageName);
        String remoteFamily = RemoteAppCatalog.familyForPackage(normalized);
        if (remoteFamily != null) return "remote:" + remoteFamily;
        if (AppPackageCatalog.isBrowserPackage(normalized)) return "browser";
        if (AppPackageCatalog.isWebViewPackage(normalized)) return "webview";
        if (AppPackageCatalog.isMessagingPackage(normalized)) return "messaging";
        if (BuildConfig.APPLICATION_ID.equals(normalized)) return "keyboard_app";
        return normalized.isEmpty() ? "unknown" : "other";
    }

    static String packageHash(String packageName) {
        String normalized = AppPackageCatalog.normalizePackageName(packageName);
        if (normalized.isEmpty() || !"other".equals(packageCategory(normalized))) return "";
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));            StringBuilder builder = new StringBuilder("sha256:");
            for (int i = 0; i < 6; i++) builder.append(String.format(Locale.US, "%02x", digest[i]));
            return builder.toString();
        } catch (Exception ignored) {
            return "hash-unavailable";
        }
    }

    private static String safeThemeId(String value) {
        return safeToken(value, "custom");
    }

    private static String safeToken(String value, String fallback) {
        String normalized = RuntimeDefaults.stringOrDefault(value, "").trim();
        if (normalized.matches("[A-Za-z0-9_.:+-]{1,80}")) return normalized;
        return fallback;
    }

    private static JSONObject loadState(Context context) {
        if (context == null) return new JSONObject();
        String raw = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_STATE_V1, "");
        try {
            return raw == null || raw.isEmpty() ? new JSONObject() : new JSONObject(raw);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private static void saveState(Context context, JSONObject state) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_STATE_V1, state.toString()).apply();
    }
}