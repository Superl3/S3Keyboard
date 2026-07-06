package com.superl3.s3keyboard;

enum KeyboardLayoutProfile implements SettingsLabelOption {
    DINGUL("dingul", R.string.keyboard_layout_profile_dingul),
    QWERTY("qwerty", R.string.keyboard_layout_profile_qwerty);

    private static final KeyboardLayoutProfile[] DISPLAY_ORDER = values();

    final String preferenceValue;
    final int labelResId;

    KeyboardLayoutProfile(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static KeyboardLayoutProfile fromPreference(String value, KeyboardLayoutProfile fallback) {
        for (KeyboardLayoutProfile profile : DISPLAY_ORDER) {
            if (profile.preferenceValue.equals(value)) {
                return profile;
            }
        }
        return fallback == null ? QWERTY : fallback;
    }

    static KeyboardLayoutProfile[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(KeyboardLayoutProfile selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
