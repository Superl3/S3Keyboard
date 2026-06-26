package com.superl3.s3keyboard;

enum RemoteImeShortcut {
    ALT_SHIFT("alt_shift", R.string.remote_ime_shortcut_alt_shift),
    CTRL_SPACE("ctrl_space", R.string.remote_ime_shortcut_ctrl_space),
    WIN_SPACE("win_space", R.string.remote_ime_shortcut_win_space),
    LANGUAGE_SWITCH("language_switch", R.string.remote_ime_shortcut_language_switch);

    final String preferenceValue;
    final int labelResId;

    RemoteImeShortcut(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
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
