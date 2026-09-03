package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class ThemeManagementModel {
    static final String TONE_ALL = "all";
    static final String TONE_LIGHT = "light";
    static final String TONE_DARK = "dark";

    private ThemeManagementModel() {
    }

    static ThemeOption[] filterAndOrder(
            ThemeOption[] options,
            String query,
            String materialStyle,
            String tone,
            Set<String> favorites,
            List<String> recents,
            boolean favoritesOnly,
            boolean recentsOnly) {
        ArrayList<ThemeOption> result = new ArrayList<>();
        if (options == null) return new ThemeOption[0];
        String normalizedQuery = normalize(query);
        String normalizedMaterial = normalize(materialStyle);
        String normalizedTone = normalize(tone);
        for (ThemeOption option : options) {
            if (option == null) continue;
            String id = option.stableId();
            boolean favorite = favorites != null && favorites.contains(id);
            boolean recent = recents != null && recents.contains(id);
            if (favoritesOnly && !favorite) continue;
            if (recentsOnly && !recent) continue;
            if (!normalizedQuery.isEmpty()
                    && !normalize(option.label).contains(normalizedQuery)
                    && !normalize(id).contains(normalizedQuery)) continue;
            KeyboardSettings appearance = option.appearanceSettings();
            String optionMaterial = appearance == null || appearance.visualEffects == null
                    ? KeyboardVisualEffects.MATERIAL_SOFT_KEYCAP
                    : appearance.visualEffects.materialStyle;
            if (!normalizedMaterial.isEmpty() && !normalizedMaterial.equals(optionMaterial)) continue;
            if (TONE_LIGHT.equals(normalizedTone) && isDark(option)) continue;
            if (TONE_DARK.equals(normalizedTone) && !isDark(option)) continue;
            result.add(option);
        }
        result.sort(Comparator.comparingInt(option -> rank(option, favorites, recents)));
        return result.toArray(new ThemeOption[0]);
    }

    static String resolvePairedThemeId(
            boolean enabled,
            boolean darkMode,
            String manualThemeId,
            String lightThemeId,
            String darkThemeId) {
        String manual = safe(manualThemeId);
        if (!enabled) return manual;
        String paired = darkMode ? safe(darkThemeId) : safe(lightThemeId);
        return paired.isEmpty() ? manual : paired;
    }

    static boolean isDark(ThemeOption option) {
        KeyboardSettings appearance = option == null ? null : option.appearanceSettings();
        int color = appearance == null ? KeyboardSettings.defaults().keyboardBackgroundColor
                : appearance.keyboardBackgroundColor;
        return luminance(color) < 0.46d;
    }
    private static int rank(ThemeOption option, Set<String> favorites, List<String> recents) {
        String id = option == null ? "" : option.stableId();
        if (favorites != null && favorites.contains(id)) return 0;
        if (recents != null) {
            int index = recents.indexOf(id);
            if (index >= 0) return 10 + index;
        }
        return 1000;
    }

    private static double luminance(int color) {
        double r = linear(((color >>> 16) & 0xFF) / 255d);
        double g = linear(((color >>> 8) & 0xFF) / 255d);
        double b = linear((color & 0xFF) / 255d);
        return 0.2126d * r + 0.7152d * g + 0.0722d * b;
    }

    private static double linear(double channel) {
        return channel <= 0.04045d
                ? channel / 12.92d
                : Math.pow((channel + 0.055d) / 1.055d, 2.4d);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalize(String value) {
        return safe(value).toLowerCase(Locale.ROOT);
    }
}
