package com.superl3.s3keyboard;

final class ThemePreviewSettings {
    private ThemePreviewSettings() {
    }

    static KeyboardSettings forOption(
            ThemeOption option,
            KeyboardSettings baseSettings,
            KeyboardMode mode) {
        return option.applyTo(baseSettings)
                .withKeyboardMode(mode)
                .withHintVisibility(false, false, false)
                .withHangulNumberRow(false)
                .withEnglishNumberRow(true);
    }
}
