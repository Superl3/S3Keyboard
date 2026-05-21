package com.superl3.s3keyboard;

import java.util.EnumSet;

enum AccentPlacementTarget {
    SETTINGS_ENTER("settings_enter", "\uC124\uC815 / \uC804\uC1A1"),
    META("meta", "\uC608\uC57D\uC5B4 / \uD55C\uC601"),
    QWERTY_SHIFT("qwerty_shift", "QWERTY Shift"),
    BACKSPACE("backspace", "Backspace"),
    DINGUL_DOT("dingul_dot", "\uB529\uAD74 . visual Enter"),
    DINGUL_SLASH("dingul_slash", "\uB529\uAD74 / visual Shift"),
    ESC_POINT("esc_point", "ESC point keycap");

    final String preferenceValue;
    final String label;

    AccentPlacementTarget(String preferenceValue, String label) {
        this.preferenceValue = preferenceValue;
        this.label = label;
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
        for (AccentPlacementTarget target : values()) {
            if (target.preferenceValue.equals(value)) {
                targets.add(target);
                return;
            }
        }
    }

    String[] keysFor(KeyboardSettings settings) {
        KeyboardMode mode = settings == null ? KeyboardMode.HANGUL : settings.keyboardMode;
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
                if (settings != null && settings.showNumberRow) {
                    return new String[]{"1"};
                }
                return mode == KeyboardMode.ENGLISH ? new String[]{"q"} : new String[]{"\u3131"};
            default:
                return new String[0];
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
