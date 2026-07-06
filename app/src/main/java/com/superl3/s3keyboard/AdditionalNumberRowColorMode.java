package com.superl3.s3keyboard;

enum AdditionalNumberRowColorMode implements SettingsLabelOption {
    FULL_ALPHA("full_alpha", R.string.number_row_color_full_alpha, KeyVisualRole.ALPHA, KeyVisualRole.ALPHA),
    HALF_MOD_4567(
            "half_mod_4567",
            R.string.number_row_color_half_mod_4567,
            KeyVisualRole.ALPHA,
            KeyVisualRole.MODIFIER),
    ALPHA_ACCENT(
            "alpha_accent",
            R.string.number_row_color_alpha_accent,
            KeyVisualRole.ALPHA,
            KeyVisualRole.ACCENT),
    MOD_ALPHA(
            "mod_alpha",
            R.string.number_row_color_mod_alpha,
            KeyVisualRole.MODIFIER,
            KeyVisualRole.ALPHA),
    FULL_MOD("full_mod", R.string.number_row_color_full_mod, KeyVisualRole.MODIFIER, KeyVisualRole.MODIFIER),
    MOD_ACCENT(
            "mod_accent",
            R.string.number_row_color_mod_accent,
            KeyVisualRole.MODIFIER,
            KeyVisualRole.ACCENT),
    ACCENT_ALPHA(
            "accent_alpha",
            R.string.number_row_color_accent_alpha,
            KeyVisualRole.ACCENT,
            KeyVisualRole.ALPHA),
    ACCENT_MOD(
            "accent_mod",
            R.string.number_row_color_accent_mod,
            KeyVisualRole.ACCENT,
            KeyVisualRole.MODIFIER),
    FULL_ACCENT("full_accent", R.string.number_row_color_full_accent, KeyVisualRole.ACCENT, KeyVisualRole.ACCENT);

    private static final AdditionalNumberRowColorMode[] DISPLAY_ORDER = values();

    final String preferenceValue;
    final int labelResId;
    private final KeyVisualRole outerRole;
    private final KeyVisualRole innerRole;

    AdditionalNumberRowColorMode(
            String preferenceValue,
            int labelResId,
            KeyVisualRole outerRole,
            KeyVisualRole innerRole) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
        this.outerRole = outerRole;
        this.innerRole = innerRole;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static AdditionalNumberRowColorMode fromPreference(String value) {
        for (AdditionalNumberRowColorMode mode : DISPLAY_ORDER) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        if ("full_default".equals(value)) {
            return FULL_ALPHA;
        }
        if ("center_dimmed".equals(value)) {
            return HALF_MOD_4567;
        }
        if ("full_dimmed".equals(value)) {
            return FULL_MOD;
        }
        return FULL_MOD;
    }

    static AdditionalNumberRowColorMode[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(AdditionalNumberRowColorMode selected) {
        int fallbackIndex = 0;
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == FULL_MOD) {
                fallbackIndex = i;
            }
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return fallbackIndex;
    }

    KeyVisualRole roleForDigit(char digit) {
        return digit >= '4' && digit <= '7' ? innerRole : outerRole;
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
