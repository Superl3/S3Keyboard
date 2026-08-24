package com.superl3.s3keyboard;

enum TransparentOverlayStyle implements SettingsLabelOption {
    TRANSLUCENT_KEYS("translucent_keys", R.string.transparent_overlay_style_translucent),
    EXTREME_FLOATING("extreme_floating", R.string.transparent_overlay_style_extreme);

    private static final TransparentOverlayStyle[] DISPLAY_ORDER = values();

    final String preferenceValue;
    private final int labelResId;

    TransparentOverlayStyle(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static TransparentOverlayStyle fromPreference(String value) {
        for (TransparentOverlayStyle style : values()) {
            if (style.preferenceValue.equals(value)) {
                return style;
            }
        }
        return TRANSLUCENT_KEYS;
    }

    static TransparentOverlayStyle[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(TransparentOverlayStyle selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }
}
