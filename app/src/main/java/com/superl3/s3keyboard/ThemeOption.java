package com.superl3.s3keyboard;

final class ThemeOption {
    final String label;
    final KeyboardThemePreset preset;
    final String userThemeId;
    final String userThemeJson;
    final boolean externalTheme;
    final String sourcePath;

    private ThemeOption(
            String label,
            KeyboardThemePreset preset,
            String userThemeId,
            String userThemeJson,
            boolean externalTheme,
            String sourcePath) {
        this.label = label;
        this.preset = preset;
        this.userThemeId = userThemeId;
        this.userThemeJson = userThemeJson;
        this.externalTheme = externalTheme;
        this.sourcePath = sourcePath == null ? "" : sourcePath;
    }

    static ThemeOption[] buildOptions(
            UserThemeStore.UserTheme[] userThemes,
            boolean includeCurrentCustom) {
        return buildOptions(userThemes, null, includeCurrentCustom);
    }

    static ThemeOption[] buildOptions(
            UserThemeStore.UserTheme[] userThemes,
            UserThemeStore.UserTheme[] externalThemes,
            boolean includeCurrentCustom) {
        int userCount = userThemes == null ? 0 : userThemes.length;
        int externalCount = externalThemes == null ? 0 : externalThemes.length;
        int customOffset = includeCurrentCustom ? 1 : 0;
        ThemeOption[] options = new ThemeOption[
                KeyboardThemePreset.PRESETS.length + userCount + externalCount + customOffset];

        if (includeCurrentCustom) {
            options[0] = new ThemeOption("Current custom", null, null, null, false, "");
        }

        for (int i = 0; i < KeyboardThemePreset.PRESETS.length; i++) {
            KeyboardThemePreset preset = KeyboardThemePreset.PRESETS[i];
            options[i + customOffset] = new ThemeOption(preset.displayName, preset, null, null, false, "");
        }
        for (int i = 0; i < userCount; i++) {
            UserThemeStore.UserTheme theme = userThemes[i];
            options[KeyboardThemePreset.PRESETS.length + customOffset + i] =
                    new ThemeOption(theme.name, null, theme.id, theme.json, theme.external, theme.sourcePath);
        }
        int externalOffset = KeyboardThemePreset.PRESETS.length + customOffset + userCount;
        for (int i = 0; i < externalCount; i++) {
            UserThemeStore.UserTheme theme = externalThemes[i];
            options[externalOffset + i] =
                    new ThemeOption("External: " + theme.name, null, theme.id, theme.json, true, theme.sourcePath);
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
        return base.withFullAppearanceFrom(KeyboardSettings.defaults());
    }

    String stableId() {
        if (preset != null) {
            return preset.id;
        }
        return userThemeId == null ? "" : userThemeId;
    }

    static int indexOfStableId(ThemeOption[] options, String stableId) {
        if (options == null || options.length == 0 || stableId == null || stableId.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i] != null && stableId.equals(options[i].stableId())) {
                return i;
            }
        }
        return 0;
    }

    boolean isDeletableUserTheme() {
        return userThemeId != null && !externalTheme;
    }

    @Override
    public String toString() {
        return label;
    }
}
