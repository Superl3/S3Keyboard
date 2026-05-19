package com.academic.hangulgestureime;

final class KeyboardKeyVisualClassifier {
    private KeyboardKeyVisualClassifier() {
    }

    static KeyVisualRole roleFor(KeyboardSettings settings, GestureKey key) {
        if (key == null) {
            return KeyVisualRole.NORMAL;
        }
        if (isAccentFunctionKey(key)) {
            return KeyVisualRole.ACCENT;
        }
        if (isHangulAccentKey(settings, key)) {
            return KeyVisualRole.ACCENT;
        }
        if (isDingulVowelCommandKey(key)) {
            return KeyVisualRole.NORMAL;
        }
        if (key.icon != KeyIcon.NONE || KeyboardCommands.isCommand(key.tap)) {
            return KeyVisualRole.FUNCTION;
        }
        return KeyVisualRole.NORMAL;
    }

    static int colorFor(KeyboardSettings settings, GestureKey key) {
        Integer override = backgroundOverrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        KeyVisualRole role = roleFor(settings, key);
        if (role == KeyVisualRole.ACCENT) {
            return settings.accentKeyColor;
        }
        if (isAdditionalNumberRowKey(key)) {
            return additionalNumberRowColor(settings, key);
        }
        return role == KeyVisualRole.NORMAL ? settings.keyIdleColor : settings.functionKeyColor;
    }

    static int textColorFor(KeyboardSettings settings, GestureKey key) {
        Integer override = overrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        if (isAdditionalNumberRowKey(key)) {
            return additionalNumberRowUsesAccent(settings, key)
                    ? settings.secondaryColor
                    : settings.accentColor;
        }
        KeyVisualRole role = roleFor(settings, key);
        return role == KeyVisualRole.NORMAL ? settings.accentColor : settings.secondaryColor;
    }

    static int hintColorFor(KeyboardSettings settings, GestureKey key) {
        if (isAdditionalNumberRowKey(key)) {
            return textColorFor(settings, key);
        }
        return settings.secondaryColor;
    }

    static int iconColorFor(KeyboardSettings settings, GestureKey key, boolean selected) {
        Integer override = overrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        return selected ? settings.secondaryColor : textColorFor(settings, key);
    }

    static int shiftIndicatorColorFor(KeyboardSettings settings) {
        Integer override = findOverride(settings, "shiftindicator");
        if (override == null) {
            override = findOverride(settings, "shift_indicator");
        }
        return override == null ? 0xFF06B6D4 : override;
    }

    static boolean isAccentFunctionKey(GestureKey key) {
        if (key == null) {
            return false;
        }
        return KeyboardCommands.CMD_ENTER.equals(key.tap);
    }

    private static Integer overrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        color = findOverride(settings, "tap:" + key.tap);
        if (color != null) {
            return color;
        }
        color = findOverride(settings, key.tap);
        if (color != null) {
            return color;
        }
        color = findOverride(settings, key.label);
        if (color != null) {
            return color;
        }
        if (key.icon != KeyIcon.NONE) {
            color = findOverride(settings, "icon:" + key.icon);
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return findOverride(settings, "space");
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            return findOverride(settings, "backspace");
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            return findOverride(settings, "enter");
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            return findOverride(settings, "shift");
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            return findOverride(settings, "language");
        }
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)) {
            return findOverride(settings, "options");
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)) {
            return findOverride(settings, "reserved");
        }
        return null;
    }

    private static Integer backgroundOverrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findBackgroundOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        color = findBackgroundOverride(settings, "tap:" + key.tap);
        if (color != null) {
            return color;
        }
        color = findBackgroundOverride(settings, key.tap);
        if (color != null) {
            return color;
        }
        color = findBackgroundOverride(settings, key.label);
        if (color != null) {
            return color;
        }
        if (key.icon != KeyIcon.NONE) {
            color = findBackgroundOverride(settings, "icon:" + key.icon);
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return findBackgroundOverride(settings, "space");
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            return findBackgroundOverride(settings, "backspace");
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            return findBackgroundOverride(settings, "enter");
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            return findBackgroundOverride(settings, "shift");
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            return findBackgroundOverride(settings, "language");
        }
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)) {
            return findBackgroundOverride(settings, "options");
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)) {
            return findBackgroundOverride(settings, "reserved");
        }
        return null;
    }

    private static Integer findOverride(KeyboardSettings settings, String key) {
        return settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(key));
    }

    private static Integer findBackgroundOverride(KeyboardSettings settings, String key) {
        Integer color = settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName("background:" + key));
        if (color != null) {
            return color;
        }
        return settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName("bg:" + key));
    }

    private static boolean isHangulAccentKey(KeyboardSettings settings, GestureKey key) {
        return settings != null
                && settings.keyboardMode == KeyboardMode.HANGUL
                && (".".equals(key.label) || "/".equals(key.label));
    }

    private static boolean isDingulVowelCommandKey(GestureKey key) {
        return KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(key.tap);
    }

    private static boolean isAdditionalNumberRowKey(GestureKey key) {
        return key != null
                && key.tap != null
                && key.tap.length() == 1
                && key.tap.charAt(0) >= '0'
                && key.tap.charAt(0) <= '9';
    }

    private static int additionalNumberRowColor(KeyboardSettings settings, GestureKey key) {
        return additionalNumberRowUsesAccent(settings, key)
                ? settings.accentKeyColor
                : settings.keyIdleColor;
    }

    private static boolean additionalNumberRowUsesAccent(KeyboardSettings settings, GestureKey key) {
        switch (settings.additionalNumberRowColorMode) {
            case FULL_DEFAULT:
                return false;
            case CENTER_DIMMED:
                return key.tap.charAt(0) >= '4' && key.tap.charAt(0) <= '7';
            case FULL_DIMMED:
            default:
                return true;
        }
    }
}
