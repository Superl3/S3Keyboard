package com.superl3.s3keyboard;

enum RemoteKeyPreset implements SettingsLabelOption {
    PC_KEYBOARD("pc_keyboard", R.string.remote_key_preset_pc_keyboard);

    final String preferenceValue;
    final int labelResId;
    private static final RemoteKeyPreset[] DISPLAY_ORDER = values();

    RemoteKeyPreset(String preferenceValue, int labelResId) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static RemoteKeyPreset fromPreference(String value) {
        for (RemoteKeyPreset preset : DISPLAY_ORDER) {
            if (preset.preferenceValue.equals(value)) {
                return preset;
            }
        }
        return PC_KEYBOARD;
    }

    static RemoteKeyPreset[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(RemoteKeyPreset selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }
}
