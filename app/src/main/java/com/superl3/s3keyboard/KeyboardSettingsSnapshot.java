package com.superl3.s3keyboard;

import org.json.JSONException;
import org.json.JSONObject;

final class KeyboardSettingsSnapshot {
    final KeyboardSettingsSections sections;

    private KeyboardSettingsSnapshot(KeyboardSettingsSections sections) {
        this.sections = sections;
    }

    static KeyboardSettingsSnapshot from(KeyboardSettings settings) {
        return new KeyboardSettingsSnapshot(KeyboardSettingsSections.from(settings));
    }

    static KeyboardSettingsSnapshot from(
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions) {
        return new KeyboardSettingsSnapshot(KeyboardSettingsSections.from(settings, ergonomicsOptions));
    }

    JSONObject toJson() {
        JSONObject object = new JSONObject();
        put(object, "appearance", sections.appearance.toJson());
        put(object, "layout", sections.layout.toJson());
        put(object, "input", sections.input.toJson());
        put(object, "remote", sections.remote.toJson());
        put(object, "ergonomics", sections.ergonomics.toJson());
        return object;
    }

    private static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode settings snapshot.", exception);
        }
    }
}
