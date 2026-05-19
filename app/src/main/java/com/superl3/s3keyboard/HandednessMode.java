package com.superl3.s3keyboard;

enum HandednessMode {
    BALANCED("balanced", "↔"),
    LEFT("left", "←"),
    RIGHT("right", "→");

    final String preferenceValue;
    final String displayName;

    HandednessMode(String preferenceValue, String displayName) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
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
