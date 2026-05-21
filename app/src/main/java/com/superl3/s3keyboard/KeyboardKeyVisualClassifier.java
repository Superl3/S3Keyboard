package com.superl3.s3keyboard;

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
        if (isDingulVowelCommandKey(key)) {
            return KeyVisualRole.NORMAL;
        }
        if (settings != null && isPointKeycapCandidate(settings, key)) {
            return settings.pointKeycapStyleEnabled
                    ? KeyVisualRole.PRIMARY_FUNCTION
                    : KeyVisualRole.FUNCTION;
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
        if (role == KeyVisualRole.PRIMARY_FUNCTION) {
            return settings.primaryFunctionKeyColor;
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
            return additionalNumberRowRole(settings, key) == KeyVisualRole.NORMAL
                    ? settings.accentColor
                    : settings.secondaryColor;
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

    static boolean drawsDotLegendFor(KeyboardSettings settings, GestureKey key) {
        KeyDisplayOverride override = KeyDisplayOverrideResolver.resolve(settings, key);
        return override != null
                && override.isIcon()
                && ModifierIconCatalog.GLYPH_DOT.equals(override.value);
    }

    static boolean isPrimaryFunctionKey(GestureKey key) {
        if (key == null) {
            return false;
        }
        return KeyboardCommands.CMD_DELETE.equals(key.tap)
                || KeyboardCommands.CMD_ENTER.equals(key.tap)
                || KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)
                || KeyboardCommands.CMD_SHIFT_LOCK.equals(key.tap);
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
        if (". .".equals(key.label)) {
            color = findOverride(settings, "..");
            if (color != null) {
                return color;
            }
        }
        if (key.icon != KeyIcon.NONE) {
            color = findOverride(settings, "icon:" + key.icon);
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            color = findOverride(settings, "space");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            color = findOverride(settings, "backspace");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            color = findOverride(settings, "enter");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            color = findOverride(settings, "shift");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            color = findOverride(settings, "language");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SETTINGS.equals(key.tap)) {
            color = findOverride(settings, "settings");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)) {
            color = findOverride(settings, "options");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)) {
            color = findOverride(settings, "reserved");
            if (color != null) {
                return color;
            }
        }
        if (usesPunctuationPointOverrides(settings)) {
            if (isDingulEnterLikePunctuationKey(settings, key)) {
                color = findOverride(settings, "enter");
                if (color != null) {
                    return color;
                }
            }
            if (isDingulShiftLikePunctuationKey(settings, key)) {
                color = findOverride(settings, "shift");
                if (color != null) {
                    return color;
                }
            }
        }
        color = dingulSemanticOverrideColorFor(settings, key, false);
        if (color != null) {
            return color;
        }
        if (KeyDisplayOverrideResolver.isAlphaKey(key)) {
            color = findOverride(settings, "alpha");
            if (color != null) {
                return color;
            }
        }
        if (KeyDisplayOverrideResolver.isModifierKey(key)) {
            color = findOverride(settings, "modifiers");
            if (color != null) {
                return color;
            }
        }
        return null;
    }

    private static boolean usesPunctuationPointOverrides(KeyboardSettings settings) {
        return settings != null
                && settings.pointKeycapStyleEnabled
                && ModifierIconCatalog.PACK_METROPOLIS_POINTS.equals(ModifierIconCatalog.effectivePackId(settings));
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
        if (". .".equals(key.label)) {
            color = findBackgroundOverride(settings, "..");
            if (color != null) {
                return color;
            }
        }
        if (key.icon != KeyIcon.NONE) {
            color = findBackgroundOverride(settings, "icon:" + key.icon);
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            color = findBackgroundOverride(settings, "space");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            color = findBackgroundOverride(settings, "backspace");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            color = findBackgroundOverride(settings, "enter");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            color = findBackgroundOverride(settings, "shift");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            color = findBackgroundOverride(settings, "language");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_SETTINGS.equals(key.tap)) {
            color = findBackgroundOverride(settings, "settings");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)) {
            color = findBackgroundOverride(settings, "options");
            if (color != null) {
                return color;
            }
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)) {
            color = findBackgroundOverride(settings, "reserved");
            if (color != null) {
                return color;
            }
        }
        if (usesPunctuationPointOverrides(settings)) {
            if (isDingulEnterLikePunctuationKey(settings, key)) {
                color = findBackgroundOverride(settings, "enter");
                if (color != null) {
                    return color;
                }
            }
            if (isDingulShiftLikePunctuationKey(settings, key)) {
                color = findBackgroundOverride(settings, "shift");
                if (color != null) {
                    return color;
                }
            }
        }
        color = dingulSemanticOverrideColorFor(settings, key, true);
        if (color != null) {
            return color;
        }
        if (KeyDisplayOverrideResolver.isAlphaKey(key)) {
            color = findBackgroundOverride(settings, "alpha");
            if (color != null) {
                return color;
            }
        }
        if (KeyDisplayOverrideResolver.isModifierKey(key)) {
            color = findBackgroundOverride(settings, "modifiers");
            if (color != null) {
                return color;
            }
        }
        return null;
    }

    private static Integer dingulSemanticOverrideColorFor(
            KeyboardSettings settings,
            GestureKey key,
            boolean background) {
        if (settings == null || key == null || settings.keyboardMode != KeyboardMode.HANGUL) {
            return null;
        }
        if (isDingulModifierInvertedKey(key)) {
            return findSemanticOverride(settings, background, "modInv", "mod_inv", "modifierInverted");
        }
        if (isDingulSemanticModifierKey(settings, key)) {
            Integer color = findSemanticOverride(settings, background, "mod", "modifier", "modifiers");
            if (color != null) {
                return color;
            }
        }
        if (KeyDisplayOverrideResolver.isAlphaKey(key)) {
            return findSemanticOverride(settings, background, "alpha");
        }
        return null;
    }

    private static Integer findSemanticOverride(
            KeyboardSettings settings,
            boolean background,
            String... keys) {
        for (String key : keys) {
            Integer color = background ? findBackgroundOverride(settings, key) : findOverride(settings, key);
            if (color != null) {
                return color;
            }
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

    private static boolean isDingulVowelCommandKey(GestureKey key) {
        return KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(key.tap);
    }

    private static boolean isPointKeycapCandidate(KeyboardSettings settings, GestureKey key) {
        return isDingulModifierPunctuationKey(settings, key) || isMetaModifierCommandKey(key);
    }

    private static boolean isMetaModifierCommandKey(GestureKey key) {
        if (key == null) {
            return false;
        }
        return KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)
                || KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)
                || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)
                || KeyboardCommands.CMD_SETTINGS.equals(key.tap)
                || KeyboardCommands.CMD_INPUT_PICKER.equals(key.tap)
                || KeyboardCommands.CMD_QUICK_SETTINGS.equals(key.tap)
                || KeyboardCommands.CMD_HIDE.equals(key.tap)
                || KeyboardCommands.CMD_HAND_LEFT.equals(key.tap)
                || KeyboardCommands.CMD_HAND_RIGHT.equals(key.tap)
                || KeyboardCommands.CMD_HAND_BALANCED.equals(key.tap);
    }

    private static boolean isDingulModifierPunctuationKey(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyboardMode != KeyboardMode.HANGUL) {
            return false;
        }
        return "?".equals(key.label) || ".".equals(key.label) || "/".equals(key.label);
    }

    private static boolean isDingulModifierInvertedKey(GestureKey key) {
        return KeyboardCommands.CMD_SPACE.equals(key.tap)
                || KeyboardCommands.CMD_ENTER.equals(key.tap);
    }

    private static boolean isDingulSemanticModifierKey(KeyboardSettings settings, GestureKey key) {
        return isDingulModifierPunctuationKey(settings, key)
                || (!isDingulVowelCommandKey(key)
                && !isDingulModifierInvertedKey(key)
                && KeyDisplayOverrideResolver.isModifierKey(key));
    }

    private static boolean isDingulEnterLikePunctuationKey(KeyboardSettings settings, GestureKey key) {
        return isDingulModifierPunctuationKey(settings, key) && ".".equals(key.label);
    }

    private static boolean isDingulShiftLikePunctuationKey(KeyboardSettings settings, GestureKey key) {
        return isDingulModifierPunctuationKey(settings, key) && "/".equals(key.label);
    }

    private static boolean isAdditionalNumberRowKey(GestureKey key) {
        return key != null
                && key.tap != null
                && key.tap.length() == 1
                && key.tap.charAt(0) >= '0'
                && key.tap.charAt(0) <= '9';
    }

    private static int additionalNumberRowColor(KeyboardSettings settings, GestureKey key) {
        KeyVisualRole role = additionalNumberRowRole(settings, key);
        if (role == KeyVisualRole.ACCENT) {
            return settings.accentKeyColor;
        }
        if (role == KeyVisualRole.FUNCTION) {
            return settings.functionKeyColor;
        }
        return settings.keyIdleColor;
    }

    private static KeyVisualRole additionalNumberRowRole(KeyboardSettings settings, GestureKey key) {
        if (settings == null || settings.additionalNumberRowColorMode == null || !isAdditionalNumberRowKey(key)) {
            return KeyVisualRole.FUNCTION;
        }
        return settings.additionalNumberRowColorMode.roleForDigit(key.tap.charAt(0));
    }
}
