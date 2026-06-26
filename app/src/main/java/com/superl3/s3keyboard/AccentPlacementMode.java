package com.superl3.s3keyboard;

enum AccentPlacementMode {
    THEME_DEFAULT("theme_default", R.string.accent_placement_mode_theme_default),
    NONE("none", R.string.accent_placement_mode_none),
    ENTER_SHIFT("enter_shift", R.string.accent_placement_mode_enter_shift),
    META("meta", R.string.accent_placement_mode_meta),
    COMMAND("command", R.string.accent_placement_mode_command),
    ALL_MODIFIERS("all_modifiers", R.string.accent_placement_mode_all_modifiers);

    static final AccentPlacementMode DEFAULT = THEME_DEFAULT;

    final String preferenceValue;
    final int labelResId;

    AccentPlacementMode(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    static AccentPlacementMode fromPreference(String value) {
        for (AccentPlacementMode mode : values()) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        return DEFAULT;
    }

    KeyboardSettings applyTo(KeyboardSettings settings) {
        return AccentPlacementPolicy.fromLegacyMode(this).applyTo(settings);
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
