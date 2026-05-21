package com.superl3.s3keyboard;

enum RemoteImeShortcut {
    ALT_SHIFT("alt_shift", "Alt+Shift"),
    CTRL_SPACE("ctrl_space", "Ctrl+Space"),
    WIN_SPACE("win_space", "Win+Space"),
    LANGUAGE_SWITCH("language_switch", "LanguageSwitch");

    final String preferenceValue;
    final String displayName;

    RemoteImeShortcut(String preferenceValue, String displayName) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
    }

    static RemoteImeShortcut fromPreference(String value) {
        for (RemoteImeShortcut shortcut : values()) {
            if (shortcut.preferenceValue.equals(value)) {
                return shortcut;
            }
        }
        return ALT_SHIFT;
    }
}
