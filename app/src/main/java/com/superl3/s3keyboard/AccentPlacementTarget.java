package com.superl3.s3keyboard;

import java.util.EnumSet;

enum AccentPlacementTarget {
    SETTINGS_ENTER("settings_enter", R.string.accent_target_settings_enter),
    META("meta", R.string.accent_target_meta),
    QWERTY_SHIFT("qwerty_shift", R.string.accent_target_qwerty_shift),
    BACKSPACE("backspace", R.string.accent_target_backspace),
    DINGUL_DOT("dingul_dot", R.string.accent_target_dingul_dot),
    DINGUL_SLASH("dingul_slash", R.string.accent_target_dingul_slash),
    ESC_POINT("esc_point", R.string.accent_target_esc_point);

    final String preferenceValue;
    final int labelResId;
    private static final AccentPlacementTarget[] DISPLAY_ORDER = values();

    AccentPlacementTarget(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    static void addPreferenceTargets(String value, EnumSet<AccentPlacementTarget> targets) {
        if (value == null || targets == null) {
            return;
        }
        if ("enter_shift".equals(value)) {
            targets.add(SETTINGS_ENTER);
            targets.add(DINGUL_DOT);
            targets.add(DINGUL_SLASH);
            return;
        }
        if ("command".equals(value)) {
            targets.add(QWERTY_SHIFT);
            targets.add(BACKSPACE);
            return;
        }
        for (AccentPlacementTarget target : DISPLAY_ORDER) {
            if (target.preferenceValue.equals(value)) {
                targets.add(target);
                return;
            }
        }
    }

    static AccentPlacementTarget[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static EnumSet<AccentPlacementTarget> allDisplayTargets() {
        EnumSet<AccentPlacementTarget> targets = EnumSet.noneOf(AccentPlacementTarget.class);
        for (AccentPlacementTarget target : DISPLAY_ORDER) {
            targets.add(target);
        }
        return targets;
    }

    String[] keysFor(KeyboardSettings settings) {
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        KeyboardMode mode = safeSettings.keyboardMode;
        switch (this) {
            case SETTINGS_ENTER:
                return new String[]{"options", "settings", "enter"};
            case META:
                return new String[]{"reserved", "language"};
            case QWERTY_SHIFT:
                return mode == KeyboardMode.ENGLISH ? new String[]{"shift"} : new String[0];
            case BACKSPACE:
                return new String[]{"backspace"};
            case DINGUL_DOT:
                return mode == KeyboardMode.HANGUL ? new String[]{"."} : new String[0];
            case DINGUL_SLASH:
                return mode == KeyboardMode.HANGUL ? new String[]{"/"} : new String[0];
            case ESC_POINT:
                if (safeSettings.showNumberRow) {
                    return new String[]{"1"};
                }
                return mode == KeyboardMode.ENGLISH ? new String[]{"q"} : new String[]{"ㄱ"};
            default:
                return new String[0];
        }
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
