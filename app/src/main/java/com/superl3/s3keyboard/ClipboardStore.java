package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple clipboard history manager.
 * Stores recent clipboard entries in SharedPreferences for quick re-insertion.
 *
 * <p>Privacy considerations:
 * <ul>
 *   <li>Maximum {@link #MAX_ENTRIES} entries stored (default 10).</li>
 *   <li>Entries from password fields are never stored.</li>
 *   <li>A preference toggle allows users to disable clipboard storage entirely.</li>
 *   <li>Users can clear all history from settings.</li>
 * </ul>
 */
final class ClipboardStore {
    private static final String PREF_NAME = "keyboard_preferences";
    private static final String KEY_ENTRIES = "entries";
    private static final String KEY_ENABLED = KeyboardPreferences.CLIPBOARD_HISTORY_ENABLED;
    private static final String SEPARATOR = "\u001F"; // Unit separator
    static final int MAX_ENTRIES = 10;

    private final SharedPreferences preferences;

    ClipboardStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns whether clipboard history is enabled.
     */
    boolean isEnabled() {
        return preferences.getBoolean(KEY_ENABLED, false);
    }

    /**
     * Sets whether clipboard history is enabled.
     */
    void setEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (!enabled) {
            clear();
        }
    }

    /**
     * Adds a text entry to the clipboard history.
     * Duplicates are moved to the front.
     */
    void add(String text) {
        if (text == null || text.isEmpty() || !isEnabled()) {
            return;
        }
        List<String> entries = load();
        entries.remove(text);
        entries.add(0, text);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        save(entries);
    }

    /**
     * Returns the current clipboard history entries, most recent first.
     */
    List<String> getEntries() {
        if (!isEnabled()) {
            return new ArrayList<>();
        }
        return load();
    }

    /**
     * Removes a single entry from the history.
     */
    void remove(String text) {
        List<String> entries = load();
        entries.remove(text);
        save(entries);
    }

    /**
     * Clears all clipboard history.
     */
    void clear() {
        preferences.edit().remove(KEY_ENTRIES).apply();
    }

    private List<String> load() {
        String raw = preferences.getString(KEY_ENTRIES, "");
        List<String> entries = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return entries;
        }
        String[] parts = raw.split(SEPARATOR, -1);
        for (String part : parts) {
            if (!part.isEmpty()) {
                entries.add(part);
            }
        }
        return entries;
    }

    private void save(List<String> entries) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(entries.get(i).replace(SEPARATOR, " "));
        }
        preferences.edit().putString(KEY_ENTRIES, sb.toString()).apply();
    }
}
