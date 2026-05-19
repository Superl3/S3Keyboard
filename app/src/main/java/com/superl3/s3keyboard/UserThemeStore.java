package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class UserThemeStore {
    private static final String PREF_NAME = "keyboard_user_themes";
    private static final String KEY_THEMES = "themes";
    private static final String USER_THEME_PREFIX = "user-theme-";

    private UserThemeStore() {
    }

    static UserTheme[] load(Context context) {
        return decode(prefs(context).getString(KEY_THEMES, "[]"));
    }

    static UserTheme saveCurrent(Context context, KeyboardSettings settings) {
        UserTheme[] existing = load(context);
        int nextIndex = nextCustomIndex(existing);
        String name = "Custom Theme " + nextIndex;
        String id = USER_THEME_PREFIX + System.currentTimeMillis();
        String json = KeyboardThemeJson.exportTheme(
                settings,
                name,
                "local",
                "Saved from current keyboard settings.");
        UserTheme saved = new UserTheme(id, name, json);
        UserTheme[] next = new UserTheme[existing.length + 1];
        System.arraycopy(existing, 0, next, 0, existing.length);
        next[existing.length] = saved;
        persist(context, next);
        return saved;
    }

    static void delete(Context context, String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        UserTheme[] existing = load(context);
        int keptCount = 0;
        for (UserTheme theme : existing) {
            if (!id.equals(theme.id)) {
                keptCount++;
            }
        }
        if (keptCount == existing.length) {
            return;
        }
        UserTheme[] next = new UserTheme[keptCount];
        int index = 0;
        for (UserTheme theme : existing) {
            if (!id.equals(theme.id)) {
                next[index++] = theme;
            }
        }
        persist(context, next);
    }

    static UserTheme[] decode(String json) {
        if (json == null || json.isEmpty()) {
            return new UserTheme[0];
        }
        try {
            JSONArray array = new JSONArray(json);
            UserTheme[] themes = new UserTheme[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    themes[i] = UserTheme.empty(i);
                    continue;
                }
                themes[i] = new UserTheme(
                        object.optString("id", USER_THEME_PREFIX + i),
                        object.optString("name", "Custom Theme " + (i + 1)),
                        object.optString("json", "{}"));
            }
            return compactValid(themes);
        } catch (JSONException exception) {
            return new UserTheme[0];
        }
    }

    static String encode(UserTheme[] themes) {
        JSONArray array = new JSONArray();
        if (themes != null) {
            for (UserTheme theme : themes) {
                if (theme == null || theme.id.isEmpty() || theme.json.isEmpty()) {
                    continue;
                }
                JSONObject object = new JSONObject();
                try {
                    object.put("id", theme.id);
                    object.put("name", theme.name);
                    object.put("json", theme.json);
                    array.put(object);
                } catch (JSONException exception) {
                    throw new IllegalStateException("Failed to encode user theme.", exception);
                }
            }
        }
        return array.toString();
    }

    private static void persist(Context context, UserTheme[] themes) {
        prefs(context).edit()
                .putString(KEY_THEMES, encode(themes))
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static int nextCustomIndex(UserTheme[] themes) {
        return themes == null ? 1 : themes.length + 1;
    }

    private static UserTheme[] compactValid(UserTheme[] themes) {
        int count = 0;
        for (UserTheme theme : themes) {
            if (theme != null && !theme.id.isEmpty() && !theme.json.isEmpty()) {
                count++;
            }
        }
        UserTheme[] compact = new UserTheme[count];
        int index = 0;
        for (UserTheme theme : themes) {
            if (theme != null && !theme.id.isEmpty() && !theme.json.isEmpty()) {
                compact[index++] = theme;
            }
        }
        return compact;
    }

    static final class UserTheme {
        final String id;
        final String name;
        final String json;

        UserTheme(String id, String name, String json) {
            this.id = id == null ? "" : id;
            this.name = name == null || name.isEmpty() ? "Custom Theme" : name;
            this.json = json == null ? "" : json;
        }

        private static UserTheme empty(int index) {
            return new UserTheme("", "Invalid Theme " + index, "");
        }
    }
}
