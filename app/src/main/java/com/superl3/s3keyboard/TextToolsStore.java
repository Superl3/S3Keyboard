package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Local, versioned persistence for user-saved Text Tools items. */
final class TextToolsStore {
    private static final String PREF_NAME = "keyboard_preferences";
    static final String KEY_DATA_V1 = "text_tools_data_v1";
    static final int SCHEMA_VERSION = 1;
    static final int MAX_ITEMS = 32;
    static final int MAX_NAME_LENGTH = 80;

    private final SharedPreferences preferences;

    TextToolsStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    List<Item> getItems() {
        return decode(preferences.getString(KEY_DATA_V1, ""));
    }

    void saveClipboardItem(String text) {
        List<Item> items = withSavedClipboardItem(getItems(), text, System.currentTimeMillis());
        save(items);
    }

    void setPinned(String id, boolean pinned) {
        save(withPinnedState(getItems(), id, pinned, System.currentTimeMillis()));
    }

    void rename(String id, String name) {
        save(withRenamedItem(getItems(), id, name, System.currentTimeMillis()));
    }

    void delete(String id) {
        List<Item> next = new ArrayList<>();
        for (Item item : getItems()) {
            if (item != null && !item.id.equals(id)) {
                next.add(item);
            }
        }
        save(next);
    }

    void clear() {
        preferences.edit().remove(KEY_DATA_V1).apply();
    }

    private void save(List<Item> items) {
        preferences.edit().putString(KEY_DATA_V1, encode(items)).apply();
    }

    static List<Item> withSavedClipboardItem(List<Item> existing, String text, long now) {
        String storable = ClipboardStore.storableEntry(text);
        if (storable == null) {
            return ordered(existing);
        }
        List<Item> next = new ArrayList<>();
        Item matched = null;
        if (existing != null) {
            for (Item item : existing) {
                if (item == null) continue;
                if (matched == null && item.text.equals(storable)) {
                    matched = item;
                } else {
                    next.add(item);
                }
            }
        }
        String id = matched == null ? buildId(storable, now) : matched.id;
        String name = matched == null ? defaultName(storable) : matched.name;
        next.add(new Item(id, name, storable, true, now));
        return ordered(next);
    }

    static List<Item> withPinnedState(List<Item> existing, String id, boolean pinned, long now) {
        List<Item> next = new ArrayList<>();
        if (existing == null) return next;
        for (Item item : existing) {
            if (item == null) continue;
            next.add(item.id.equals(id)
                    ? new Item(item.id, item.name, item.text, pinned, now)
                    : item);
        }
        return ordered(next);
    }

    static List<Item> withRenamedItem(List<Item> existing, String id, String name, long now) {
        List<Item> next = new ArrayList<>();
        if (existing == null) return next;
        for (Item item : existing) {
            if (item == null) continue;
            if (item.id.equals(id)) {
                String normalized = normalizeName(name, item.text);
                next.add(new Item(item.id, normalized, item.text, item.pinned, now));
            } else {
                next.add(item);
            }
        }
        return ordered(next);
    }

    static String encode(List<Item> items) {
        JSONObject root = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            root.put("version", SCHEMA_VERSION);
            for (Item item : ordered(items)) {
                JSONObject object = new JSONObject();
                object.put("id", item.id);
                object.put("name", item.name);
                object.put("text", item.text);
                object.put("pinned", item.pinned);
                object.put("updatedAt", item.updatedAtMs);
                array.put(object);
            }
            root.put("items", array);
        } catch (JSONException ignored) {
            return "";
        }
        return root.toString();
    }

    static List<Item> decode(String raw) {
        List<Item> items = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return items;
        try {
            JSONObject root = new JSONObject(raw);
            if (root.optInt("version", 0) != SCHEMA_VERSION) return items;
            JSONArray array = root.optJSONArray("items");
            if (array == null) return items;
            for (int i = 0; i < array.length() && items.size() < MAX_ITEMS; i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) continue;
                String id = object.optString("id", "").trim();
                String text = ClipboardStore.storableEntry(object.optString("text", ""));
                long updatedAt = object.optLong("updatedAt", 0L);
                if (id.isEmpty() || text == null || updatedAt <= 0L) continue;
                String name = normalizeName(object.optString("name", ""), text);
                items.add(new Item(
                        id,
                        name,
                        text,
                        object.optBoolean("pinned", false),
                        updatedAt));
            }
        } catch (JSONException ignored) {
            items.clear();
        }
        return ordered(items);
    }

    static List<Item> ordered(List<Item> items) {
        List<Item> ordered = new ArrayList<>();
        if (items != null) {
            for (Item item : items) if (item != null) ordered.add(item);
        }
        Collections.sort(ordered, new Comparator<Item>() {
            @Override
            public int compare(Item left, Item right) {
                if (left.pinned != right.pinned) return left.pinned ? -1 : 1;
                return Long.compare(right.updatedAtMs, left.updatedAtMs);
            }
        });
        if (ordered.size() > MAX_ITEMS) {
            return new ArrayList<>(ordered.subList(0, MAX_ITEMS));
        }
        return ordered;
    }

    private static String normalizeName(String name, String text) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty()) normalized = defaultName(text);
        if (normalized.length() > MAX_NAME_LENGTH) {
            normalized = normalized.substring(0, MAX_NAME_LENGTH);
        }
        return normalized;
    }

    private static String defaultName(String text) {
        if (text == null) return "";
        String singleLine = text.replace('\n', ' ').replace('\r', ' ').trim();
        return singleLine.length() <= 32 ? singleLine : singleLine.substring(0, 32);
    }

    private static String buildId(String text, long now) {
        return Long.toHexString(now) + "-" + Integer.toHexString(text.hashCode());
    }

    static final class Item {
        final String id;
        final String name;
        final String text;
        final boolean pinned;
        final long updatedAtMs;

        Item(String id, String name, String text, boolean pinned, long updatedAtMs) {
            this.id = id == null ? "" : id;
            this.name = name == null ? "" : name;
            this.text = text == null ? "" : text;
            this.pinned = pinned;
            this.updatedAtMs = updatedAtMs;
        }
    }
}
