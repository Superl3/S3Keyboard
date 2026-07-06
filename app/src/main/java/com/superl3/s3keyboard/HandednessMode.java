package com.superl3.s3keyboard;

enum HandednessMode implements SettingsLabelOption {
    BALANCED("balanced", R.string.handedness_balanced),
    LEFT("left", R.string.handedness_left),
    RIGHT("right", R.string.handedness_right);

    private static final HandednessMode[] DISPLAY_ORDER = {
            BALANCED,
            LEFT,
            RIGHT
    };

    final String preferenceValue;
    final int labelResId;

    HandednessMode(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static HandednessMode fromPreference(String value) {
        for (HandednessMode mode : DISPLAY_ORDER) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        return BALANCED;
    }

    static HandednessMode[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(HandednessMode selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }
}
