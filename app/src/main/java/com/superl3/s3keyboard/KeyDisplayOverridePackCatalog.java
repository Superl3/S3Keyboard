package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class KeyDisplayOverridePackCatalog {
    static final String PACK_THEME_DEFAULT = "";
    static final String PACK_NONE = "none";
    static final String PACK_SIMPLE_TEXT = "simple-text";
    static final String PACK_GIT_COMMANDS = "git-commands";
    static final String LEGACY_PACK_OLIVIA_SCRIPT_TEXT = "olivia-script-text";

    private static final Map<String, KeyDisplayOverride> SIMPLE_TEXT_OVERRIDES =
            createSimpleTextOverrides();
    private static final Map<String, KeyDisplayOverride> GIT_COMMAND_OVERRIDES =
            createGitCommandOverrides();

    private KeyDisplayOverridePackCatalog() {
    }

    static String normalizePackId(String packId) {
        if (PACK_SIMPLE_TEXT.equals(packId) || LEGACY_PACK_OLIVIA_SCRIPT_TEXT.equals(packId)) {
            return PACK_SIMPLE_TEXT;
        }
        if (PACK_GIT_COMMANDS.equals(packId)) {
            return PACK_GIT_COMMANDS;
        }
        return PACK_NONE;
    }

    static String normalizeOverridePackId(String packId) {
        if (packId == null || packId.isEmpty() || PACK_THEME_DEFAULT.equals(packId)) {
            return PACK_THEME_DEFAULT;
        }
        return normalizePackId(packId);
    }

    static String effectivePackId(KeyboardSettings settings) {
        if (settings == null) {
            return PACK_NONE;
        }
        if (settings.keyDisplayOverridePackId != null && !settings.keyDisplayOverridePackId.isEmpty()) {
            return normalizePackId(settings.keyDisplayOverridePackId);
        }
        return normalizePackId(settings.keyDisplayThemePackId);
    }

    static boolean isKnownNonEmptyPackId(String packId) {
        String normalized = normalizePackId(packId);
        return PACK_SIMPLE_TEXT.equals(normalized) || PACK_GIT_COMMANDS.equals(normalized);
    }

    static boolean isSimpleTextPack(String packId) {
        return PACK_SIMPLE_TEXT.equals(normalizePackId(packId));
    }

    static boolean shouldRenderSimpleText(KeyboardSettings settings, KeyDisplayOverride override) {
        return override != null
                && override.isText()
                && isSimpleTextPack(effectivePackId(settings));
    }

    static Map<String, KeyDisplayOverride> overridesForEffectivePack(KeyboardSettings settings) {
        return overridesForPack(effectivePackId(settings));
    }

    static Map<String, KeyDisplayOverride> overridesForPack(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_SIMPLE_TEXT.equals(normalized)) {
            return SIMPLE_TEXT_OVERRIDES;
        }
        if (PACK_GIT_COMMANDS.equals(normalized)) {
            return GIT_COMMAND_OVERRIDES;
        }
        return Collections.emptyMap();
    }

    static String displayName(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_SIMPLE_TEXT.equals(normalized)) {
            return "Simple Text";
        }
        if (PACK_GIT_COMMANDS.equals(normalized)) {
            return "Git Commands";
        }
        return "None";
    }

    static String[] selectablePackIds(boolean includeThemeDefault) {
        if (includeThemeDefault) {
            return new String[] {
                    PACK_THEME_DEFAULT,
                    PACK_NONE,
                    PACK_SIMPLE_TEXT,
                    PACK_GIT_COMMANDS
            };
        }
        return new String[] {
                PACK_NONE,
                PACK_SIMPLE_TEXT,
                PACK_GIT_COMMANDS
        };
    }

    private static Map<String, KeyDisplayOverride> createSimpleTextOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putText(overrides, "label:.", "hihihi");
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGitCommandOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putIconText(overrides, KeyIcon.ENTER, "exec");
        putIconText(overrides, KeyIcon.DONE, "exec");
        putIconText(overrides, KeyIcon.NEXT, "exec");
        putText(overrides, "enter", "exec");
        putIconText(overrides, KeyIcon.BACKSPACE, "reset");
        putText(overrides, "backspace", "reset");
        putIconText(overrides, KeyIcon.SHIFT, "rebase");
        putIconText(overrides, KeyIcon.CAPS_LOCK, "rebase");
        putText(overrides, "shift", "rebase");
        putIconText(overrides, KeyIcon.SPACE, "pull");
        putText(overrides, "space", "pull");
        putIconText(overrides, KeyIcon.LANGUAGE, "fetch");
        putText(overrides, "language", "fetch");
        putIconText(overrides, KeyIcon.OPTIONS, "stash");
        putText(overrides, "options", "stash");
        putIconText(overrides, KeyIcon.SETTINGS, "config");
        putText(overrides, "settings", "config");
        putIconText(overrides, KeyIcon.RESERVED, "commit");
        putText(overrides, "reserved", "commit");
        putIconText(overrides, KeyIcon.SEARCH, "grep");
        putIconText(overrides, KeyIcon.HIDE, "hide");
        putIconText(overrides, KeyIcon.KEYBOARD, "branch");
        putIconText(overrides, KeyIcon.MOVE_LEFT, "prev");
        putIconText(overrides, KeyIcon.MOVE_RIGHT, "next");
        putIconText(overrides, KeyIcon.RESET, "reset");
        putText(overrides, ".", "diff");
        putText(overrides, "/", "log");
        return Collections.unmodifiableMap(overrides);
    }

    private static void putIconText(Map<String, KeyDisplayOverride> overrides, int icon, String text) {
        putText(overrides, "icon:" + icon, text);
    }

    private static void putText(Map<String, KeyDisplayOverride> overrides, String key, String text) {
        overrides.put(KeyboardSettings.normalizeKeyOverrideName(key), KeyDisplayOverride.text(text));
    }
}
