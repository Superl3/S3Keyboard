package com.academic.hangulgestureime;

enum KeyboardMode {
    HANGUL("hangul", "한글"),
    ENGLISH("english", "English");

    final String preferenceValue;
    final String displayName;

    KeyboardMode(String preferenceValue, String displayName) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
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
