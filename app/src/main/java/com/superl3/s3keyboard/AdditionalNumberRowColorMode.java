package com.superl3.s3keyboard;

enum AdditionalNumberRowColorMode {
    FULL_ALPHA("full_alpha", "\uC804\uCCB4 Alpha", KeyVisualRole.ALPHA, KeyVisualRole.ALPHA),
    HALF_MOD_4567("half_mod_4567", "\uAC00\uC6B4\uB370 Mod", KeyVisualRole.ALPHA, KeyVisualRole.MODIFIER),
    ALPHA_ACCENT("alpha_accent", "\uAC00\uC6B4\uB370 Accent", KeyVisualRole.ALPHA, KeyVisualRole.ACCENT),
    MOD_ALPHA("mod_alpha", "\uBC14\uAE65 Mod / \uAC00\uC6B4\uB370 Alpha", KeyVisualRole.MODIFIER, KeyVisualRole.ALPHA),
    FULL_MOD("full_mod", "\uC804\uCCB4 Mod", KeyVisualRole.MODIFIER, KeyVisualRole.MODIFIER),
    MOD_ACCENT("mod_accent", "\uBC14\uAE65 Mod / \uAC00\uC6B4\uB370 Accent", KeyVisualRole.MODIFIER, KeyVisualRole.ACCENT),
    ACCENT_ALPHA("accent_alpha", "\uBC14\uAE65 Accent / \uAC00\uC6B4\uB370 Alpha", KeyVisualRole.ACCENT, KeyVisualRole.ALPHA),
    ACCENT_MOD("accent_mod", "\uBC14\uAE65 Accent / \uAC00\uC6B4\uB370 Mod", KeyVisualRole.ACCENT, KeyVisualRole.MODIFIER),
    FULL_ACCENT("full_accent", "\uC804\uCCB4 Accent", KeyVisualRole.ACCENT, KeyVisualRole.ACCENT);

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
