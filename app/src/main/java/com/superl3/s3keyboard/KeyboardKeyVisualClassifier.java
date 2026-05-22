package com.superl3.s3keyboard;

final class KeyboardKeyVisualClassifier {
    private KeyboardKeyVisualClassifier() {
    }

    static KeyVisualRole roleFor(KeyboardSettings settings, GestureKey key) {
        if (key == null) {
            return KeyVisualRole.ALPHA;
        }
        if (isDingulVowelCommandKey(key)) {
            return KeyVisualRole.ALPHA;
        }
        if (settings != null && isPointKeycapCandidate(settings, key)) {
            return KeyVisualRole.MODIFIER;
        }
        if (key.icon != KeyIcon.NONE || KeyboardCommands.isCommand(key.tap)) {
            return KeyVisualRole.MODIFIER;
        }
        return KeyVisualRole.ALPHA;
    }

    static int colorFor(KeyboardSettings settings, GestureKey key) {
        if (isAdditionalNumberRowKey(key)) {
            Integer exactOverride = exactBackgroundOverrideColorFor(settings, key);
            if (exactOverride != null) {
                return exactOverride;
            }
            return additionalNumberRowColor(settings, key);
        }
        Integer override = backgroundOverrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        KeyVisualRole role = roleFor(settings, key);
        if (role == KeyVisualRole.ACCENT) {
            return settings.accentKeyColor;
        }
        return role == KeyVisualRole.ALPHA ? settings.keyIdleColor : settings.functionKeyColor;
    }

    static int textColorFor(KeyboardSettings settings, GestureKey key) {
        if (isAdditionalNumberRowKey(key)) {
            Integer exactOverride = exactOverrideColorFor(settings, key);
            if (exactOverride != null) {
                return exactOverride;
            }
            return textColorForRole(settings, additionalNumberRowRole(settings, key));
        }
        Integer override = overrideColorFor(settings, key);
        if (override != null) {
            return override;
        }
        KeyVisualRole role = roleFor(settings, key);
        return textColorForRole(settings, role);
    }

    static int hintColorFor(KeyboardSettings settings, GestureKey key) {
        int background = colorFor(settings, key);
        int foreground = textColorFor(settings, key);
        return softenedForegroundFor(foreground, background, 0.62f, 1.45);
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
        int alphaText = textColorForRole(settings, KeyVisualRole.ALPHA);
        int modifierText = textColorForRole(settings, KeyVisualRole.MODIFIER);
        int modifierBackground = backgroundColorForRole(settings, KeyVisualRole.MODIFIER);
        int base = override == null ? alphaText : blendColor(override, alphaText, 0.34f);
        return ensureContrast(
                base,
                modifierBackground,
                2.1,
                alphaText,
                modifierText,
                override == null ? alphaText : override,
                0xFF111827,
                0xFFFFFFFF);
    }

    static boolean drawsDotLegendFor(KeyboardSettings settings, GestureKey key) {
        KeyDisplayOverride override = KeyDisplayOverrideResolver.resolve(settings, key);
        return override != null
                && override.isIcon()
                && ModifierIconCatalog.GLYPH_DOT.equals(override.value);
    }

    private static Integer overrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        if (isDingulDotDotKey(key)) {
            color = findOverride(settings, "..");
            if (color != null) {
                return color;
            }
            return dingulAlphaOverrideColorFor(settings, false);
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
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)
                || KeyboardCommands.CMD_QUICK_SETTINGS.equals(key.tap)) {
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

    private static Integer exactOverrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        if (isDingulDotDotKey(key)) {
            color = findOverride(settings, "..");
            return color == null ? null : color;
        }
        color = findOverride(settings, "tap:" + key.tap);
        if (color != null) {
            return color;
        }
        color = findOverride(settings, key.tap);
        if (color != null) {
            return color;
        }
        return findOverride(settings, key.label);
    }

    private static Integer exactBackgroundOverrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findBackgroundOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        if (isDingulDotDotKey(key)) {
            color = findBackgroundOverride(settings, "..");
            return color == null ? null : color;
        }
        color = findBackgroundOverride(settings, "tap:" + key.tap);
        if (color != null) {
            return color;
        }
        color = findBackgroundOverride(settings, key.tap);
        if (color != null) {
            return color;
        }
        return findBackgroundOverride(settings, key.label);
    }

    private static Integer backgroundOverrideColorFor(KeyboardSettings settings, GestureKey key) {
        if (settings == null || key == null || settings.keyColorOverrides.isEmpty()) {
            return null;
        }
        Integer color = findBackgroundOverride(settings, "label:" + key.label);
        if (color != null) {
            return color;
        }
        if (isDingulDotDotKey(key)) {
            color = findBackgroundOverride(settings, "..");
            if (color != null) {
                return color;
            }
            return dingulAlphaOverrideColorFor(settings, true);
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
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)
                || KeyboardCommands.CMD_QUICK_SETTINGS.equals(key.tap)) {
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
        if (KeyDisplayOverrideResolver.isAlphaKey(key)) {
            return findSemanticOverride(settings, background, "alpha");
        }
        if (isDingulSemanticModifierKey(settings, key)) {
            Integer color = findSemanticOverride(settings, background, "mod", "modifier", "modifiers");
            if (color != null) {
                return color;
            }
        }
        return null;
    }

    private static Integer dingulAlphaOverrideColorFor(KeyboardSettings settings, boolean background) {
        if (settings == null || settings.keyboardMode != KeyboardMode.HANGUL) {
            return null;
        }
        return findSemanticOverride(settings, background, "alpha");
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
        return ".".equals(key.label) || "/".equals(key.label);
    }

    private static boolean isDingulDotDotKey(GestureKey key) {
        return key != null && ". .".equals(key.label);
    }

    private static boolean isDingulModifierInvertedKey(GestureKey key) {
        return KeyboardCommands.CMD_SPACE.equals(key.tap);
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
        return backgroundColorForRole(settings, additionalNumberRowRole(settings, key));
    }

    private static int backgroundColorForRole(KeyboardSettings settings, KeyVisualRole role) {
        if (role == KeyVisualRole.ACCENT) {
            Integer color = findSemanticOverride(settings, true, "modInv", "mod_inv", "modifierInverted");
            return color == null ? settings.accentKeyColor : color;
        }
        if (role == KeyVisualRole.MODIFIER) {
            Integer color = findSemanticOverride(settings, true, "mod", "modifier", "modifiers");
            return color == null ? settings.functionKeyColor : color;
        }
        Integer color = findSemanticOverride(settings, true, "alpha");
        return color == null ? settings.keyIdleColor : color;
    }

    private static KeyVisualRole additionalNumberRowRole(KeyboardSettings settings, GestureKey key) {
        if (settings == null || settings.additionalNumberRowColorMode == null || !isAdditionalNumberRowKey(key)) {
            return KeyVisualRole.MODIFIER;
        }
        return settings.additionalNumberRowColorMode.roleForDigit(key.tap.charAt(0));
    }

    private static int textColorForRole(KeyboardSettings settings, KeyVisualRole role) {
        if (role == KeyVisualRole.ACCENT) {
            Integer color = findSemanticOverride(settings, false, "modInv", "mod_inv", "modifierInverted");
            return color == null ? settings.functionKeyColor : color;
        }
        if (role == KeyVisualRole.ALPHA) {
            Integer color = findSemanticOverride(settings, false, "alpha");
            return color == null ? settings.accentColor : color;
        }
        Integer color = findSemanticOverride(settings, false, "mod", "modifier", "modifiers");
        return color == null ? settings.secondaryColor : color;
    }

    private static double contrastRatio(int foreground, int background) {
        double lighter = Math.max(relativeLuminance(foreground), relativeLuminance(background));
        double darker = Math.min(relativeLuminance(foreground), relativeLuminance(background));
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static int softenedForegroundFor(
            int foreground,
            int background,
            float foregroundAmount,
            double minimumContrast) {
        float amount = Math.max(0f, Math.min(1f, foregroundAmount));
        int color = blendColor(foreground, background, amount);
        while (amount < 1f && contrastRatio(color, background) < minimumContrast) {
            amount = Math.min(1f, amount + 0.08f);
            color = blendColor(foreground, background, amount);
        }
        return contrastRatio(color, background) >= minimumContrast ? color : foreground;
    }

    private static int ensureContrast(
            int preferred,
            int background,
            double minimumContrast,
            int... fallbacks) {
        if (contrastRatio(preferred, background) >= minimumContrast) {
            return preferred;
        }
        int best = preferred;
        double bestContrast = contrastRatio(preferred, background);
        for (int fallback : fallbacks) {
            double contrast = contrastRatio(fallback, background);
            if (contrast > bestContrast) {
                best = fallback;
                bestContrast = contrast;
            }
        }
        return best;
    }

    private static int blendColor(int foreground, int background, float foregroundAmount) {
        float amount = Math.max(0f, Math.min(1f, foregroundAmount));
        float inverse = 1f - amount;
        int a = Math.round(((foreground >>> 24) & 0xFF) * amount
                + ((background >>> 24) & 0xFF) * inverse);
        int r = Math.round(((foreground >> 16) & 0xFF) * amount
                + ((background >> 16) & 0xFF) * inverse);
        int g = Math.round(((foreground >> 8) & 0xFF) * amount
                + ((background >> 8) & 0xFF) * inverse);
        int b = Math.round((foreground & 0xFF) * amount
                + (background & 0xFF) * inverse);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static double relativeLuminance(int color) {
        double r = linearChannel((color >> 16) & 0xFF);
        double g = linearChannel((color >> 8) & 0xFF);
        double b = linearChannel(color & 0xFF);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double linearChannel(int channel) {
        double value = channel / 255.0;
        return value <= 0.03928
                ? value / 12.92
                : Math.pow((value + 0.055) / 1.055, 2.4);
    }
}
