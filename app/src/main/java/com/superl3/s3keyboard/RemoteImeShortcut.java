package com.superl3.s3keyboard;

enum RemoteImeShortcut implements SettingsLabelOption {
    ALT_SHIFT("alt_shift", R.string.remote_ime_shortcut_alt_shift),
    CTRL_SPACE("ctrl_space", R.string.remote_ime_shortcut_ctrl_space),
    WIN_SPACE("win_space", R.string.remote_ime_shortcut_win_space),
    LANGUAGE_SWITCH("language_switch", R.string.remote_ime_shortcut_language_switch);

    final String preferenceValue;
    final int labelResId;
    private static final RemoteImeShortcut[] DISPLAY_ORDER = values();

    RemoteImeShortcut(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static RemoteImeShortcut fromPreference(String value) {
        for (RemoteImeShortcut shortcut : DISPLAY_ORDER) {
            if (shortcut.preferenceValue.equals(value)) {
                return shortcut;
            }
        }
        return ALT_SHIFT;
    }

    static RemoteImeShortcut[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(RemoteImeShortcut selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }
}
