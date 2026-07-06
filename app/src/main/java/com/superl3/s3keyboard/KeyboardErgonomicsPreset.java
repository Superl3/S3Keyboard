package com.superl3.s3keyboard;

enum KeyboardErgonomicsPreset implements SettingsLabelOption {
    LEGACY(R.string.ergonomics_preset_legacy, KeyboardErgonomicsOptions.DEFAULT),
    STABLE(R.string.ergonomics_preset_stable, new KeyboardErgonomicsOptions(
            true,
            true,
            true,
            false,
            false,
            true,
            VisualConsistencyLevel.NONE)),
    ERGONOMIC(R.string.ergonomics_preset_ergonomic, new KeyboardErgonomicsOptions(
            true,
            true,
            true,
            true,
            true,
            true,
            VisualConsistencyLevel.SUBTLE)),
    AGGRESSIVE(R.string.ergonomics_preset_aggressive, new KeyboardErgonomicsOptions(
            true,
            true,
            true,
            true,
            true,
            true,
            VisualConsistencyLevel.BALANCED));

    final int labelResId;
    final KeyboardErgonomicsOptions options;
    private static final KeyboardErgonomicsPreset[] DISPLAY_ORDER = values();

    KeyboardErgonomicsPreset(int labelResId, KeyboardErgonomicsOptions options) {
        this.labelResId = labelResId;
        this.options = options;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    boolean matches(KeyboardErgonomicsOptions current) {
        return options.sameValues(current);
    }

    static KeyboardErgonomicsPreset findMatching(KeyboardErgonomicsOptions current) {
        for (KeyboardErgonomicsPreset preset : DISPLAY_ORDER) {
            if (preset.matches(current)) {
                return preset;
            }
        }
        return null;
    }

    static KeyboardErgonomicsPreset[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(KeyboardErgonomicsPreset selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
