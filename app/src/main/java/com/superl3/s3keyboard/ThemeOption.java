package com.superl3.s3keyboard;

final class ThemeOption {
    final String label;
    final KeyboardThemePreset preset;
    final String userThemeId;
    final String userThemeJson;

    private ThemeOption(
            String label,
            KeyboardThemePreset preset,
            String userThemeId,
            String userThemeJson) {
        this.label = label;
        this.preset = preset;
        this.userThemeId = userThemeId;
        this.userThemeJson = userThemeJson;
    }

    static ThemeOption[] buildOptions(
            UserThemeStore.UserTheme[] userThemes,
            boolean includeCurrentCustom) {
        int userCount = userThemes == null ? 0 : userThemes.length;
        int customOffset = includeCurrentCustom ? 1 : 0;
        ThemeOption[] options = new ThemeOption[
                KeyboardThemePreset.PRESETS.length + userCount + customOffset];

        if (includeCurrentCustom) {
            options[0] = new ThemeOption("Current custom", null, null, null);
        }

        for (int i = 0; i < KeyboardThemePreset.PRESETS.length; i++) {
            KeyboardThemePreset preset = KeyboardThemePreset.PRESETS[i];
            options[i + customOffset] = new ThemeOption(preset.displayName, preset, null, null);
        }
        for (int i = 0; i < userCount; i++) {
            UserThemeStore.UserTheme theme = userThemes[i];
            options[KeyboardThemePreset.PRESETS.length + customOffset + i] =
                    new ThemeOption(theme.name, null, theme.id, theme.json);
        }
        return options;
    }

    KeyboardSettings applyTo(KeyboardSettings settings) {
        KeyboardSettings base = settings == null ? KeyboardSettings.defaults() : settings;
        KeyboardSettings appearance = appearanceSettings();
        return appearance == null ? base : base.withAppearanceFrom(appearance);
    }

    KeyboardSettings appearanceSettings() {
        if (preset != null) {
            return preset.applyTo(KeyboardSettings.defaults());
        }
        if (userThemeJson != null && !userThemeJson.isEmpty()) {
            return KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), userThemeJson);
        }
        return null;
    }

    boolean locksUserAccentPlacement() {
        if (preset != null) {
            return KeyboardThemeJson.locksUserAccentPlacement(preset.json);
        }
        return KeyboardThemeJson.locksUserAccentPlacement(userThemeJson);
    }

    static KeyboardSettings resetToDefaultAppearance(KeyboardSettings settings) {
        KeyboardSettings base = settings == null ? KeyboardSettings.defaults() : settings;
        return base.withAppearanceFrom(KeyboardSettings.defaults());
    }

    String stableId() {
        if (preset != null) {
            return preset.id;
        }
        return userThemeId == null ? "" : userThemeId;
    }

    @Override
    public String toString() {
        return label;
    }
}
