package com.superl3.s3keyboard;

enum RemoteKeyPreset {
    PC_KEYBOARD("pc_keyboard", R.string.remote_key_preset_pc_keyboard);

    final String preferenceValue;
    final int labelResId;

    RemoteKeyPreset(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
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
