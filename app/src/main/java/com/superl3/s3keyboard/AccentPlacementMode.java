package com.superl3.s3keyboard;

enum AccentPlacementMode {
    THEME_DEFAULT("theme_default", "테마 기본"),
    NONE("none", "Accent 없음"),
    ENTER_SHIFT("enter_shift", "전송 / Shift 계열"),
    META("meta", "예약어 / 한영"),
    COMMAND("command", "Shift / Backspace"),
    ALL_MODIFIERS("all_modifiers", "모든 modifier");

    static final AccentPlacementMode DEFAULT = THEME_DEFAULT;

    final String preferenceValue;
    private final String label;

    AccentPlacementMode(String preferenceValue, String label) {
        this.preferenceValue = preferenceValue;
        this.label = label;
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
        return label;
    }
}
