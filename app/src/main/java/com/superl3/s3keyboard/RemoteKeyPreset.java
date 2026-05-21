package com.superl3.s3keyboard;

enum RemoteKeyPreset {
    PC_KEYBOARD("pc_keyboard", "PC keyboard");

    final String preferenceValue;
    final String displayName;

    RemoteKeyPreset(String preferenceValue, String displayName) {
        this.preferenceValue = preferenceValue;
        this.displayName = displayName;
    }

    static RemoteKeyPreset fromPreference(String value) {
        for (RemoteKeyPreset preset : values()) {
            if (preset.preferenceValue.equals(value)) {
                return preset;
            }
        }
        return PC_KEYBOARD;
    }
}
