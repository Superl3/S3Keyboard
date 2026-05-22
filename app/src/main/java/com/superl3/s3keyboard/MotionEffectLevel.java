package com.superl3.s3keyboard;

enum MotionEffectLevel {
    OFF("off", "끄기", 0),
    SUBTLE("subtle", "약하게", 1),
    NORMAL("normal", "보통", 2);

    final String preferenceValue;
    final String displayName;
    final int intensity;

    MotionEffectLevel(String preferenceValue, String displayName, int intensity) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
        this.intensity = intensity;
    }

    static MotionEffectLevel fromPreference(String value) {
        for (MotionEffectLevel level : values()) {
            if (level.preferenceValue.equals(value)) {
                return level;
            }
        }
        return NORMAL;
    }
}
