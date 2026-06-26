package com.superl3.s3keyboard;

enum HandednessMode {
    BALANCED("balanced", R.string.handedness_balanced),
    LEFT("left", R.string.handedness_left),
    RIGHT("right", R.string.handedness_right);

    final String preferenceValue;
    final int labelResId;

    HandednessMode(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    static HandednessMode fromPreference(String value) {
        for (HandednessMode mode : values()) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        return BALANCED;
    }
}
