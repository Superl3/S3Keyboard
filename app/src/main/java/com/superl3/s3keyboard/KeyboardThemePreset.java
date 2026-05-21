package com.superl3.s3keyboard;

final class KeyboardThemePreset {
    static final KeyboardThemePreset[] PRESETS = GeneratedKeyboardThemePresets.PRESETS;

    final String id;
    final String displayName;
    final String json;

    KeyboardThemePreset(String id, String displayName, String json) {
        this.id = id;
        this.displayName = displayName;
        this.json = json;
    }

    KeyboardSettings applyTo(KeyboardSettings base) {
        return KeyboardThemeJson.importTheme(base, json);
    }

    static KeyboardThemePreset find(String id) {
        if (id == null) {
            return null;
        }
        for (KeyboardThemePreset preset : PRESETS) {
            if (preset.id.equals(id)) {
                return preset;
            }
        }
        return null;
    }
}
