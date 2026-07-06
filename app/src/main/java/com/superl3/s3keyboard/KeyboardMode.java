package com.superl3.s3keyboard;

enum KeyboardMode implements SettingsLabelOption {
    HANGUL("hangul", R.string.keyboard_mode_hangul),
    ENGLISH("english", R.string.keyboard_mode_english);

    final String preferenceValue;
    final int labelResId;

    KeyboardMode(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    KeyboardMode next() {
        return this == HANGUL ? ENGLISH : HANGUL;
    }

    static KeyboardMode fromPreference(String value) {
        for (KeyboardMode mode : values()) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        return HANGUL;
    }
}
