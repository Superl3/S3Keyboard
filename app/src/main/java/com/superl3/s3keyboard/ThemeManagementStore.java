package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ThemeManagementStore {
    private static final String PREF_NAME = "theme_management";
    private static final String FAVORITES = "favorites";
    private static final String RECENTS = "recents";
    private static final String LIGHT_THEME = "light_theme";
    private static final String DARK_THEME = "dark_theme";
    private static final String PAIR_ENABLED = "pair_enabled";
    private static final int MAX_RECENTS = 8;

    private ThemeManagementStore() {
    }

    static Set<String> loadFavorites(Context context) {
        Set<String> stored = prefs(context).getStringSet(FAVORITES, Collections.emptySet());
        return new HashSet<>(stored == null ? Collections.emptySet() : stored);
    }
    static boolean isFavorite(Context context, String themeId) {
        return loadFavorites(context).contains(safeId(themeId));
    }

    static void toggleFavorite(Context context, String themeId) {
        String id = safeId(themeId);
        if (id.isEmpty()) return;
        Set<String> favorites = loadFavorites(context);
        if (!favorites.add(id)) favorites.remove(id);
        prefs(context).edit().putStringSet(FAVORITES, favorites).apply();
    }

    static List<String> loadRecents(Context context) {
        String raw = prefs(context).getString(RECENTS, "");
        ArrayList<String> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return result;
        for (String token : raw.split("\\n")) {
            String id = safeId(token);
            if (!id.isEmpty() && !result.contains(id)) result.add(id);
        }
        return result;
    }

    static void recordRecent(Context context, String themeId) {
        String id = safeId(themeId);
        if (id.isEmpty()) return;
        List<String> recents = loadRecents(context);
        recents.remove(id);
        recents.add(0, id);
        while (recents.size() > MAX_RECENTS) recents.remove(recents.size() - 1);
        prefs(context).edit().putString(RECENTS, join(recents)).apply();
    }
    static void savePair(Context context, String lightThemeId, String darkThemeId, boolean enabled) {
        prefs(context).edit()
                .putString(LIGHT_THEME, safeId(lightThemeId))
                .putString(DARK_THEME, safeId(darkThemeId))
                .putBoolean(PAIR_ENABLED, enabled)
                .apply();
    }

    static String loadLightThemeId(Context context) {
        return prefs(context).getString(LIGHT_THEME, "");
    }

    static String loadDarkThemeId(Context context) {
        return prefs(context).getString(DARK_THEME, "");
    }

    static boolean isPairEnabled(Context context) {
        return prefs(context).getBoolean(PAIR_ENABLED, false);
    }

    static void disablePairing(Context context) {
        prefs(context).edit().putBoolean(PAIR_ENABLED, false).apply();
    }

    static String resolveSystemThemeId(Context context, String manualThemeId) {
        int mask = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return ThemeManagementModel.resolvePairedThemeId(
                isPairEnabled(context),
                mask == Configuration.UI_MODE_NIGHT_YES,
                manualThemeId,
                loadLightThemeId(context),
                loadDarkThemeId(context));
    }
    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String safeId(String value) {
        return value == null ? "" : value.trim();
    }

    private static String join(List<String> values) {
        StringBuilder joined = new StringBuilder();
        for (String value : values) {
            if (joined.length() > 0) joined.append('\n');
            joined.append(value);
        }
        return joined.toString();
    }
}
