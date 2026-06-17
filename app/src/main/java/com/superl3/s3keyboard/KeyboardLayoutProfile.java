package com.superl3.s3keyboard;

enum KeyboardLayoutProfile {
    DINGUL("dingul", "\uB529\uAD74"),
    QWERTY("qwerty", "QWERTY");

    final String preferenceValue;
    final String displayName;

    KeyboardLayoutProfile(String preferenceValue, String displayName) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
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
        return displayName;
    }
}
