package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class AccentPlacementPolicy {
    static final String THEME_DEFAULT_VALUE = "theme_default";
    static final String NONE_VALUE = "none";

    private static final String[] MANAGED_KEYS = {
            "options", "settings", "enter", "reserved", "language", "shift", "backspace",
            ".", "/", "space", "?", "1", "q", "\u3131"
    };

    final boolean themeDefault;
    final EnumSet<AccentPlacementTarget> targets;
    final SpaceRole spaceRole;
    final QuestionRole questionRole;

    enum SpaceRole {
        DEFAULT("", "\uD14C\uB9C8 \uAE30\uBCF8"),
        ALPHA("space:alpha", "Space = Alpha"),
        MOD("space:mod", "Space = Mod"),
        ACCENT("space:accent", "Space = Accent");

        final String preferenceValue;
        final String label;

        SpaceRole(String preferenceValue, String label) {
            this.preferenceValue = preferenceValue;
            this.label = label;
        }

        static SpaceRole fromPreference(String value) {
            for (SpaceRole role : values()) {
                if (!role.preferenceValue.isEmpty() && role.preferenceValue.equals(value)) {
                    return role;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    enum QuestionRole {
        DEFAULT("", "\uD14C\uB9C8 \uAE30\uBCF8"),
        ALPHA("question:alpha", "? = Alpha"),
        MOD("question:mod", "? = Mod"),
        ACCENT("question:accent", "? = Accent");

        final String preferenceValue;
        final String label;

        QuestionRole(String preferenceValue, String label) {
            this.preferenceValue = preferenceValue;
            this.label = label;
        }

        static QuestionRole fromPreference(String value) {
            for (QuestionRole role : values()) {
                if (!role.preferenceValue.isEmpty() && role.preferenceValue.equals(value)) {
                    return role;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private AccentPlacementPolicy(
            boolean themeDefault,
            EnumSet<AccentPlacementTarget> targets,
            SpaceRole spaceRole,
            QuestionRole questionRole) {
        this.themeDefault = themeDefault;
        this.targets = targets == null || targets.isEmpty()
                ? EnumSet.noneOf(AccentPlacementTarget.class)
                : EnumSet.copyOf(targets);
        this.spaceRole = spaceRole == null ? SpaceRole.DEFAULT : spaceRole;
        this.questionRole = questionRole == null ? QuestionRole.DEFAULT : questionRole;
    }

    static AccentPlacementPolicy themeDefault() {
        return new AccentPlacementPolicy(
                true,
                EnumSet.noneOf(AccentPlacementTarget.class),
                SpaceRole.DEFAULT,
                QuestionRole.DEFAULT);
    }

    static AccentPlacementPolicy none() {
        return new AccentPlacementPolicy(
                false,
                EnumSet.noneOf(AccentPlacementTarget.class),
                SpaceRole.DEFAULT,
                QuestionRole.DEFAULT);
    }

    static AccentPlacementPolicy of(Set<AccentPlacementTarget> targets) {
        return of(targets, SpaceRole.DEFAULT);
    }

    static AccentPlacementPolicy of(Set<AccentPlacementTarget> targets, SpaceRole spaceRole) {
        return of(targets, spaceRole, QuestionRole.DEFAULT);
    }

    static AccentPlacementPolicy of(
            Set<AccentPlacementTarget> targets,
            SpaceRole spaceRole,
            QuestionRole questionRole) {
        EnumSet<AccentPlacementTarget> normalized = EnumSet.noneOf(AccentPlacementTarget.class);
        if (targets != null) {
            normalized.addAll(targets);
        }
        return new AccentPlacementPolicy(false, normalized, spaceRole, questionRole);
    }

    static AccentPlacementPolicy fromPreference(String value) {
        if (value == null || THEME_DEFAULT_VALUE.equals(value)) {
            return themeDefault();
        }
        if (value.trim().isEmpty() || NONE_VALUE.equals(value)) {
            return none();
        }
        EnumSet<AccentPlacementTarget> targets = EnumSet.noneOf(AccentPlacementTarget.class);
        SpaceRole spaceRole = SpaceRole.DEFAULT;
        QuestionRole questionRole = QuestionRole.DEFAULT;
        String[] parts = value.split(",");
        for (String part : parts) {
            String token = part.trim();
            SpaceRole role = SpaceRole.fromPreference(token);
            if (role != null) {
                spaceRole = role;
            } else {
                QuestionRole question = QuestionRole.fromPreference(token);
                if (question != null) {
                    questionRole = question;
                } else {
                    AccentPlacementTarget.addPreferenceTargets(token, targets);
                }
            }
        }
        return new AccentPlacementPolicy(false, targets, spaceRole, questionRole);
    }

    static AccentPlacementPolicy fromLegacyMode(AccentPlacementMode mode) {
        if (mode == null || mode == AccentPlacementMode.THEME_DEFAULT) {
            return themeDefault();
        }
        if (mode == AccentPlacementMode.NONE) {
            return none();
        }
        EnumSet<AccentPlacementTarget> targets = EnumSet.noneOf(AccentPlacementTarget.class);
        if (mode == AccentPlacementMode.ENTER_SHIFT || mode == AccentPlacementMode.ALL_MODIFIERS) {
            targets.add(AccentPlacementTarget.SETTINGS_ENTER);
            targets.add(AccentPlacementTarget.DINGUL_DOT);
            targets.add(AccentPlacementTarget.DINGUL_SLASH);
        }
        if (mode == AccentPlacementMode.META || mode == AccentPlacementMode.ALL_MODIFIERS) {
            targets.add(AccentPlacementTarget.META);
        }
        if (mode == AccentPlacementMode.COMMAND || mode == AccentPlacementMode.ALL_MODIFIERS) {
            targets.add(AccentPlacementTarget.QWERTY_SHIFT);
            targets.add(AccentPlacementTarget.BACKSPACE);
        }
        return new AccentPlacementPolicy(false, targets, SpaceRole.DEFAULT, QuestionRole.DEFAULT);
    }

    boolean contains(AccentPlacementTarget target) {
        return targets.contains(target);
    }

    boolean containsSpaceRole(SpaceRole role) {
        return spaceRole == role;
    }

    boolean containsQuestionRole(QuestionRole role) {
        return questionRole == role;
    }

    String toPreferenceValue() {
        if (themeDefault) {
            return THEME_DEFAULT_VALUE;
        }
        if (targets.isEmpty() && spaceRole == SpaceRole.DEFAULT && questionRole == QuestionRole.DEFAULT) {
            return NONE_VALUE;
        }
        StringBuilder builder = new StringBuilder();
        for (AccentPlacementTarget target : targets) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(target.preferenceValue);
        }
        if (spaceRole != SpaceRole.DEFAULT) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(spaceRole.preferenceValue);
        }
        if (questionRole != QuestionRole.DEFAULT) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(questionRole.preferenceValue);
        }
        return builder.toString();
    }

    KeyboardSettings applyTo(KeyboardSettings settings) {
        KeyboardSettings base = settings == null ? KeyboardSettings.defaults() : settings;
        if (themeDefault) {
            return base;
        }
        Map<String, Integer> overrides = new HashMap<>(base.keyColorOverrides);
        removeManagedAccentOverrides(overrides);
        applySpaceRole(overrides, base, spaceRole);
        applyQuestionRole(overrides, base, questionRole);
        if (!hasDistinctAccentSurface(base)) {
            return base.withKeyColorOverrides(overrides);
        }
        int foreground = accentForeground(base);
        int background = accentBackground(base);
        for (AccentPlacementTarget target : targets) {
            for (String key : target.keysFor(base)) {
                String normalized = KeyboardSettings.normalizeKeyOverrideName(key);
                overrides.put(normalized, foreground);
                overrides.put(KeyboardSettings.normalizeKeyOverrideName("background:" + normalized), background);
            }
        }
        return base.withKeyColorOverrides(overrides);
    }

    static KeyboardSettings applyThemeDefaultTo(
            KeyboardSettings settings,
            KeyboardSettings themeDefault) {
        KeyboardSettings base = settings == null ? KeyboardSettings.defaults() : settings;
        if (themeDefault == null) {
            return base;
        }
        Map<String, Integer> overrides = new HashMap<>(base.keyColorOverrides);
        removeManagedAccentOverrides(overrides);
        for (String key : MANAGED_KEYS) {
            copyManagedOverride(themeDefault.keyColorOverrides, overrides, key);
            copyManagedOverride(themeDefault.keyColorOverrides, overrides, "background:" + key);
        }
        return base.withKeyColorOverrides(overrides);
    }

    Set<AccentPlacementTarget> targetSet() {
        return Collections.unmodifiableSet(targets);
    }

    private static void removeManagedAccentOverrides(Map<String, Integer> overrides) {
        for (String key : MANAGED_KEYS) {
            String normalized = KeyboardSettings.normalizeKeyOverrideName(key);
            overrides.remove(normalized);
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName("background:" + normalized));
        }
    }

    private static void copyManagedOverride(
            Map<String, Integer> source,
            Map<String, Integer> target,
            String key) {
        String normalized = KeyboardSettings.normalizeKeyOverrideName(key);
        Integer color = source.get(normalized);
        if (color != null) {
            target.put(normalized, color);
        }
    }

    private static int accentForeground(KeyboardSettings settings) {
        Integer foreground = settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName("modInv"));
        if (foreground == null) {
            foreground = settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName("modifierInverted"));
        }
        return foreground == null ? settings.functionKeyColor : foreground;
    }

    private static int accentBackground(KeyboardSettings settings) {
        Integer background = settings.keyColorOverrides.get(
                KeyboardSettings.normalizeKeyOverrideName("background:modInv"));
        if (background == null) {
            background = settings.keyColorOverrides.get(
                    KeyboardSettings.normalizeKeyOverrideName("background:modifierInverted"));
        }
        return background == null ? settings.accentKeyColor : background;
    }

    private static void applySpaceRole(
            Map<String, Integer> overrides,
            KeyboardSettings settings,
            SpaceRole role) {
        if (role == null || role == SpaceRole.DEFAULT) {
            return;
        }
        int foreground;
        int background;
        switch (role) {
            case ALPHA:
                foreground = semanticColor(settings, false, settings.accentColor, "alpha");
                background = semanticColor(settings, true, settings.keyIdleColor, "alpha");
                break;
            case MOD:
                foreground = semanticColor(settings, false, settings.secondaryColor,
                        "mod", "modifier", "modifiers");
                background = semanticColor(settings, true, settings.functionKeyColor,
                        "mod", "modifier", "modifiers");
                break;
            case ACCENT:
                foreground = accentForeground(settings);
                background = accentBackground(settings);
                break;
            default:
                return;
        }
        String normalized = KeyboardSettings.normalizeKeyOverrideName("space");
        overrides.put(normalized, foreground);
        overrides.put(KeyboardSettings.normalizeKeyOverrideName("background:" + normalized), background);
    }

    private static void applyQuestionRole(
            Map<String, Integer> overrides,
            KeyboardSettings settings,
            QuestionRole role) {
        if (role == null || role == QuestionRole.DEFAULT) {
            return;
        }
        int foreground;
        int background;
        switch (role) {
            case ALPHA:
                foreground = semanticColor(settings, false, settings.accentColor, "alpha");
                background = semanticColor(settings, true, settings.keyIdleColor, "alpha");
                break;
            case MOD:
                foreground = semanticColor(settings, false, settings.secondaryColor,
                        "mod", "modifier", "modifiers");
                background = semanticColor(settings, true, settings.functionKeyColor,
                        "mod", "modifier", "modifiers");
                break;
            case ACCENT:
                foreground = accentForeground(settings);
                background = accentBackground(settings);
                break;
            default:
                return;
        }
        String normalized = KeyboardSettings.normalizeKeyOverrideName("?");
        overrides.put(normalized, foreground);
        overrides.put(KeyboardSettings.normalizeKeyOverrideName("background:" + normalized), background);
    }

    private static int semanticColor(
            KeyboardSettings settings,
            boolean background,
            int fallback,
            String... keys) {
        for (String key : keys) {
            Integer color = settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(
                    background ? "background:" + key : key));
            if (color == null && background) {
                color = settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName("bg:" + key));
            }
            if (color != null) {
                return color;
            }
        }
        return fallback;
    }

    private static boolean hasDistinctAccentSurface(KeyboardSettings settings) {
        int accent = accentBackground(settings);
        return colorDistance(accent, settings.keyIdleColor) >= 48
                && colorDistance(accent, settings.functionKeyColor) >= 48;
    }

    private static double colorDistance(int left, int right) {
        int lr = (left >> 16) & 0xFF;
        int lg = (left >> 8) & 0xFF;
        int lb = left & 0xFF;
        int rr = (right >> 16) & 0xFF;
        int rg = (right >> 8) & 0xFF;
        int rb = right & 0xFF;
        return Math.sqrt(
                Math.pow(lr - rr, 2)
                        + Math.pow(lg - rg, 2)
                        + Math.pow(lb - rb, 2));
    }
}
