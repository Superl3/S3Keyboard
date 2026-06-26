package com.superl3.s3keyboard;

enum MotionEffectLevel {
    OFF("off", R.string.motion_effect_off, 0),
    SUBTLE("subtle", R.string.motion_effect_subtle, 1),
    NORMAL("normal", R.string.motion_effect_normal, 2);

    final String preferenceValue;
    final int labelResId;
    final int intensity;

    MotionEffectLevel(String preferenceValue, int labelResId, int intensity) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
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
