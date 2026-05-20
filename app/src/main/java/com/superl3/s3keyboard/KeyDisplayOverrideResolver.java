package com.superl3.s3keyboard;

import java.util.Map;

final class KeyDisplayOverrideResolver {
    private KeyDisplayOverrideResolver() {
    }

    static KeyDisplayOverride resolve(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null) {
            return null;
        }

        Map<String, KeyDisplayOverride> packOverrides =
                KeyDisplayOverridePackCatalog.overridesForEffectivePack(settings);
        KeyDisplayOverride exact = exactOverride(settings.keyDisplayOverrides, key);
        if (exact == null) {
            exact = exactOverride(packOverrides, key);
        }
        if (exact != null) {
            return exact;
        }
        if (isAlphaKey(key)) {
            KeyDisplayOverride group = settings.keyDisplayOverrides.get("alpha");
            return group == null ? packOverrides.get("alpha") : group;
        }
        if (isModifierKey(key)) {
            KeyDisplayOverride group = settings.keyDisplayOverrides.get("modifiers");
            return group == null ? packOverrides.get("modifiers") : group;
        }
        return null;
    }

    static boolean isModifierKey(GestureKey key) {
        return key != null && (key.icon != KeyIcon.NONE || KeyboardCommands.isCommand(key.tap));
    }

    static boolean isAlphaKey(GestureKey key) {
        if (key == null || key.tap == null || key.tap.isEmpty()) {
            return false;
        }
        if (isDingulActionAlphaKey(key)) {
            return true;
        }
        if (KeyboardCommands.isCommand(key.tap)) {
            return false;
        }
        if (isPunctuationAlphaKey(key.tap) || isPunctuationAlphaKey(key.label)) {
            return true;
        }
        if (key.tap.length() == 1) {
            char c = key.tap.charAt(0);
            return (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '\u3131' && c <= '\u318E')
                    || (c >= '\uAC00' && c <= '\uD7A3');
        }
        return false;
    }

    private static boolean isDingulActionAlphaKey(GestureKey key) {
        return KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(key.tap)
                || "\u3163.".equals(key.label)
                || "\u3161\u3150".equals(key.label)
                || ". .".equals(key.label)
                || "..".equals(key.label);
    }

    private static boolean isPunctuationAlphaKey(String value) {
        return "?".equals(value) || ".".equals(value) || "/".equals(value);
    }

    private static KeyDisplayOverride exactOverride(Map<String, KeyDisplayOverride> overrides, GestureKey key) {
        KeyDisplayOverride override = find(overrides, "label:" + key.label);
        if (override != null) {
            return override;
        }
        override = find(overrides, "tap:" + key.tap);
        if (override != null) {
            return override;
        }
        override = find(overrides, key.tap);
        if (override != null) {
            return override;
        }
        override = find(overrides, key.label);
        if (override != null) {
            return override;
        }
        if (". .".equals(key.label)) {
            override = find(overrides, "..");
            if (override != null) {
                return override;
            }
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return find(overrides, "space");
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            return find(overrides, "backspace");
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            return find(overrides, "enter");
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)
                || KeyboardCommands.CMD_SHIFT_LOCK.equals(key.tap)) {
            return find(overrides, "shift");
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            return find(overrides, "language");
        }
        if (KeyboardCommands.CMD_SETTINGS.equals(key.tap)) {
            return find(overrides, "settings");
        }
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)) {
            return find(overrides, "options");
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)) {
            return find(overrides, "reserved");
        }
        if (key.icon != KeyIcon.NONE) {
            return find(overrides, "icon:" + key.icon);
        }
        return null;
    }

    private static KeyDisplayOverride find(Map<String, KeyDisplayOverride> overrides, String key) {
        return overrides == null ? null : overrides.get(KeyboardSettings.normalizeKeyOverrideName(key));
    }
}
