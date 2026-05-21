package com.superl3.s3keyboard;

enum AdditionalNumberRowColorMode {
    FULL_ALPHA("full_alpha", "All alpha", KeyVisualRole.NORMAL, KeyVisualRole.NORMAL),
    HALF_MOD_4567("half_mod_4567", "123890 alpha / 4567 mod", KeyVisualRole.NORMAL, KeyVisualRole.FUNCTION),
    ALPHA_ACCENT("alpha_accent", "123890 alpha / 4567 accent", KeyVisualRole.NORMAL, KeyVisualRole.ACCENT),
    MOD_ALPHA("mod_alpha", "123890 mod / 4567 alpha", KeyVisualRole.FUNCTION, KeyVisualRole.NORMAL),
    FULL_MOD("full_mod", "All mod", KeyVisualRole.FUNCTION, KeyVisualRole.FUNCTION),
    MOD_ACCENT("mod_accent", "123890 mod / 4567 accent", KeyVisualRole.FUNCTION, KeyVisualRole.ACCENT),
    ACCENT_ALPHA("accent_alpha", "123890 accent / 4567 alpha", KeyVisualRole.ACCENT, KeyVisualRole.NORMAL),
    ACCENT_MOD("accent_mod", "123890 accent / 4567 mod", KeyVisualRole.ACCENT, KeyVisualRole.FUNCTION),
    FULL_ACCENT("full_accent", "All accent", KeyVisualRole.ACCENT, KeyVisualRole.ACCENT);

    final String preferenceValue;
    final String label;
    private final KeyVisualRole outerRole;
    private final KeyVisualRole innerRole;

    AdditionalNumberRowColorMode(
            String preferenceValue,
            String label,
            KeyVisualRole outerRole,
            KeyVisualRole innerRole) {
        this.preferenceValue = preferenceValue;
        this.label = label;
        this.outerRole = outerRole;
        this.innerRole = innerRole;
    }

    static AdditionalNumberRowColorMode fromPreference(String value) {
        for (AdditionalNumberRowColorMode mode : values()) {
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

    KeyVisualRole roleForDigit(char digit) {
        return digit >= '4' && digit <= '7' ? innerRole : outerRole;
    }

    @Override
    public String toString() {
        return label;
    }
}
