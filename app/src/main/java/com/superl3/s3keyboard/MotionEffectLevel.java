package com.superl3.s3keyboard;

enum MotionEffectLevel implements SettingsLabelOption {
    OFF("off", R.string.motion_effect_off, 0),
    SUBTLE("subtle", R.string.motion_effect_subtle, 1),
    NORMAL("normal", R.string.motion_effect_normal, 2);

    final String preferenceValue;
    final int labelResId;
    final int intensity;
    private static final MotionEffectLevel[] DISPLAY_ORDER = values();

    MotionEffectLevel(String preferenceValue, int labelResId, int intensity) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
        this.intensity = intensity;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static MotionEffectLevel fromPreference(String value) {
        for (MotionEffectLevel level : DISPLAY_ORDER) {
            if (level.preferenceValue.equals(value)) {
                return level;
            }
        }
        return NORMAL;
    }

    static MotionEffectLevel[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(MotionEffectLevel selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return indexOf(NORMAL);
    }
}
