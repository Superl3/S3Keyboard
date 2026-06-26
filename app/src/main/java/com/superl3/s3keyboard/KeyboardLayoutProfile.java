package com.superl3.s3keyboard;

enum KeyboardLayoutProfile {
    DINGUL("dingul", R.string.keyboard_layout_profile_dingul),
    QWERTY("qwerty", R.string.keyboard_layout_profile_qwerty);

    final String preferenceValue;
    final int labelResId;

    KeyboardLayoutProfile(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    static KeyboardLayoutProfile fromPreference(String value, KeyboardLayoutProfile fallback) {
        for (KeyboardLayoutProfile profile : values()) {
            if (profile.preferenceValue.equals(value)) {
                return profile;
            }
        }
        return fallback == null ? QWERTY : fallback;
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
