package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
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
    static final String KEY_ENTRIES = "entries";
    static final String KEY_ENTRIES_V2 = "clipboard_entries_v2";
    private static final String KEY_ENABLED = KeyboardPreferences.CLIPBOARD_HISTORY_ENABLED;
    private static final String SEPARATOR = "\u001F"; // Unit separator
    static final int MAX_ENTRIES = 10;
    static final int MAX_ENTRY_LENGTH = 4096;
    static final long MAX_ENTRY_AGE_MS = 7L * 24L * 60L * 60L * 1000L;

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
        String storable = storableEntry(text);
        if (storable == null || !isEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        List<Entry> entries = load(now);
        removeText(entries, storable);
        entries.add(0, new Entry(storable, now));
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
        List<Entry> stored = load(System.currentTimeMillis());
        List<String> entries = new ArrayList<>(stored.size());
        for (Entry entry : stored) {
            entries.add(entry.text);
        }
        return entries;
    }

    /**
     * Removes a single entry from the history.
     */
    void remove(String text) {
        List<Entry> entries = load(System.currentTimeMillis());
        removeText(entries, text);
        save(entries);
    }

    /**
     * Clears all clipboard history.
     */
    void clear() {
        preferences.edit().remove(KEY_ENTRIES).remove(KEY_ENTRIES_V2).apply();
    }

    private List<Entry> load(long now) {
        List<Entry> entries = decodeV2(preferences.getString(KEY_ENTRIES_V2, ""));
        boolean migrated = false;
        if (entries.isEmpty()) {
            entries = decodeLegacy(preferences.getString(KEY_ENTRIES, ""), now);
            migrated = !entries.isEmpty();
        }
        int previousSize = entries.size();
        entries = pruneExpired(entries, now);
        if (migrated || entries.size() != previousSize) {
            save(entries);
        }
        return entries;
    }

    private List<Entry> decodeLegacy(String raw, long now) {
        List<Entry> entries = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return entries;
        }
        String[] parts = raw.split(SEPARATOR, -1);
        for (String part : parts) {
            String storable = storableEntry(part);
            if (storable != null) {
                entries.add(new Entry(storable, now));
                if (entries.size() >= MAX_ENTRIES) {
                    break;
                }
            }
        }
        return entries;
    }

    private List<Entry> decodeV2(String raw) {
        List<Entry> entries = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return entries;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length() && entries.size() < MAX_ENTRIES; i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String text = storableEntry(object.optString("text", ""));
                long createdAt = object.optLong("createdAt", 0L);
                if (text != null && createdAt > 0L) {
                    entries.add(new Entry(text, createdAt));
                }
            }
        } catch (JSONException ignored) {
            entries.clear();
        }
        return entries;
    }

    private void save(List<Entry> entries) {
        JSONArray array = new JSONArray();
        int written = 0;
        for (Entry entry : entries) {
            String storable = storableEntry(entry.text);
            if (storable == null) {
                continue;
            }
            try {
                JSONObject object = new JSONObject();
                object.put("text", storable);
                object.put("createdAt", entry.createdAtMs);
                array.put(object);
            } catch (JSONException ignored) {
                continue;
            }
            written++;
            if (written >= MAX_ENTRIES) {
                break;
            }
        }
        preferences.edit()
                .putString(KEY_ENTRIES_V2, array.toString())
                .remove(KEY_ENTRIES)
                .apply();
    }

    static List<Entry> pruneExpired(List<Entry> entries, long now) {
        List<Entry> retained = new ArrayList<>();
        if (entries == null) {
            return retained;
        }
        for (Entry entry : entries) {
            if (entry != null
                    && entry.createdAtMs <= now
                    && now - entry.createdAtMs <= MAX_ENTRY_AGE_MS) {
                retained.add(entry);
                if (retained.size() >= MAX_ENTRIES) {
                    break;
                }
            }
        }
        return retained;
    }

    private static void removeText(List<Entry> entries, String text) {
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().text.equals(text)) {
                iterator.remove();
            }
        }
    }

    static String storableEntry(String text) {
        if (text == null || text.isEmpty() || text.length() > MAX_ENTRY_LENGTH) {
            return null;
        }
        return text.replace(SEPARATOR, " ");
    }

    static final class Entry {
        final String text;
        final long createdAtMs;

        Entry(String text, long createdAtMs) {
            this.text = text;
            this.createdAtMs = createdAtMs;
        }
    }
}
