package com.superl3.s3keyboard;

final class ThemePreviewSettings {
    private ThemePreviewSettings() {
    }

    static KeyboardSettings forOption(
            ThemeOption option,
            KeyboardSettings baseSettings,
            KeyboardMode mode) {
        return forOption(option, baseSettings, mode, AccentPlacementPolicy.themeDefault());
    }

    static KeyboardSettings forOption(
            ThemeOption option,
            KeyboardSettings baseSettings,
            KeyboardMode mode,
            AccentPlacementPolicy accentPlacementPolicy) {
        KeyboardSettings preview = option.applyTo(baseSettings)
                .withKeyboardMode(mode)
                .withHintVisibility(false, false, false)
                .withHangulNumberRow(false)
                .withEnglishNumberRow(true);
        AccentPlacementPolicy policy = accentPlacementPolicy == null
                ? AccentPlacementPolicy.themeDefault()
                : accentPlacementPolicy;
        boolean themeLocksAccentPlacement = option != null && option.locksUserAccentPlacement();
        return policy.themeDefault || themeLocksAccentPlacement ? preview : policy.applyTo(preview);
    }
}
