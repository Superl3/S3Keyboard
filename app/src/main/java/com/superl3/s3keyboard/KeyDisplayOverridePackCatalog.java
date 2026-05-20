package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class KeyDisplayOverridePackCatalog {
    static final String PACK_THEME_DEFAULT = "";
    static final String PACK_NONE = "none";
    static final String PACK_SIMPLE_TEXT = "simple-text";
    static final String LEGACY_PACK_OLIVIA_SCRIPT_TEXT = "olivia-script-text";

    private static final Map<String, KeyDisplayOverride> OLIVIA_SCRIPT_OVERRIDES =
            createSimpleTextOverrides();

    private KeyDisplayOverridePackCatalog() {
    }

    static String normalizePackId(String packId) {
        if (PACK_SIMPLE_TEXT.equals(packId) || LEGACY_PACK_OLIVIA_SCRIPT_TEXT.equals(packId)) {
            return PACK_SIMPLE_TEXT;
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
        return PACK_SIMPLE_TEXT.equals(normalizePackId(packId));
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
        if (!PACK_SIMPLE_TEXT.equals(normalizePackId(packId))) {
            return Collections.emptyMap();
        }
        return OLIVIA_SCRIPT_OVERRIDES;
    }

    static String displayName(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_SIMPLE_TEXT.equals(normalized)) {
            return "Simple Text";
        }
        return "None";
    }

    static String[] selectablePackIds(boolean includeThemeDefault) {
        if (includeThemeDefault) {
            return new String[] {
                    PACK_THEME_DEFAULT,
                    PACK_NONE,
                    PACK_SIMPLE_TEXT
            };
        }
        return new String[] {
                PACK_NONE,
                PACK_SIMPLE_TEXT
        };
    }

    private static Map<String, KeyDisplayOverride> createSimpleTextOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putIconText(overrides, KeyIcon.ENTER, "hihihi");
        putIconText(overrides, KeyIcon.DONE, "hihihi");
        putIconText(overrides, KeyIcon.NEXT, "hihihi");
        putText(overrides, "enter", "hihihi");
        putIconText(overrides, KeyIcon.BACKSPACE, "del");
        putText(overrides, "backspace", "del");
        putIconText(overrides, KeyIcon.SHIFT, "shift");
        putIconText(overrides, KeyIcon.CAPS_LOCK, "caps");
        putText(overrides, "shift", "shift");
        putIconText(overrides, KeyIcon.SPACE, "space");
        putText(overrides, "space", "space");
        putIconText(overrides, KeyIcon.LANGUAGE, "lang");
        putText(overrides, "language", "lang");
        putIconText(overrides, KeyIcon.OPTIONS, "opts");
        putText(overrides, "options", "opts");
        putIconText(overrides, KeyIcon.SETTINGS, "set");
        putText(overrides, "settings", "set");
        putIconText(overrides, KeyIcon.RESERVED, "memo");
        putText(overrides, "reserved", "memo");
        putIconText(overrides, KeyIcon.SEARCH, "find");
        putIconText(overrides, KeyIcon.HIDE, "hide");
        putIconText(overrides, KeyIcon.KEYBOARD, "kbd");
        putIconText(overrides, KeyIcon.MOVE_LEFT, "left");
        putIconText(overrides, KeyIcon.MOVE_RIGHT, "right");
        putIconText(overrides, KeyIcon.RESET, "reset");
        return Collections.unmodifiableMap(overrides);
    }

    private static void putIconText(Map<String, KeyDisplayOverride> overrides, int icon, String text) {
        putText(overrides, "icon:" + icon, text);
    }

    private static void putText(Map<String, KeyDisplayOverride> overrides, String key, String text) {
        overrides.put(KeyboardSettings.normalizeKeyOverrideName(key), KeyDisplayOverride.text(text));
    }
}
