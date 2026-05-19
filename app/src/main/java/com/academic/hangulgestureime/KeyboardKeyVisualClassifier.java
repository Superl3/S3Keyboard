package com.academic.hangulgestureime;

final class KeyboardKeyVisualClassifier {
    private KeyboardKeyVisualClassifier() {
    }

    static KeyVisualRole roleFor(KeyboardSettings settings, GestureKey key) {
        if (key == null) {
            return KeyVisualRole.NORMAL;
        }
        if (isPrimaryFunctionKey(key)) {
            return KeyVisualRole.PRIMARY_FUNCTION;
        }
        if (isHangulAccentKey(settings, key)) {
            return KeyVisualRole.ACCENT;
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return KeyVisualRole.NORMAL;
        }
        if (key.icon != KeyIcon.NONE || KeyboardCommands.isCommand(key.tap)) {
            return KeyVisualRole.FUNCTION;
        }
        return KeyVisualRole.NORMAL;
    }

    static int colorFor(KeyboardSettings settings, GestureKey key) {
        switch (roleFor(settings, key)) {
            case PRIMARY_FUNCTION:
                return settings.primaryFunctionKeyColor;
            case ACCENT:
                return settings.accentKeyColor;
            case FUNCTION:
                return settings.functionKeyColor;
            case NORMAL:
            default:
                return settings.keyIdleColor;
        }
    }

    static int textColorFor(KeyboardSettings settings, GestureKey key) {
        Integer override = overrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        return settings.accentColor;
    }

    static int iconColorFor(KeyboardSettings settings, GestureKey key, boolean selected) {
        Integer override = overrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        return selected ? settings.accentColor : settings.secondaryColor;
    }

    static boolean isPrimaryFunctionKey(GestureKey key) {
        if (key == null) {
            return false;
        }
        return KeyboardCommands.CMD_DELETE.equals(key.tap)
                || KeyboardCommands.CMD_ENTER.equals(key.tap)
                || KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap);
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
        return null;
    }

    private static Integer findOverride(KeyboardSettings settings, String key) {
        return settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(key));
    }

    private static boolean isHangulAccentKey(KeyboardSettings settings, GestureKey key) {
        return settings != null
                && settings.keyboardMode == KeyboardMode.HANGUL
                && (".".equals(key.label) || "/".equals(key.label));
    }
}
