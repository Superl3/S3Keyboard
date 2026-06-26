package com.superl3.s3keyboard;

enum KeyboardErgonomicsPreset {
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

    KeyboardErgonomicsPreset(int labelResId, KeyboardErgonomicsOptions options) {
        this.labelResId = labelResId;
        this.options = options;
    }

    boolean matches(KeyboardErgonomicsOptions current) {
        return options.sameValues(current);
    }

    static KeyboardErgonomicsPreset findMatching(KeyboardErgonomicsOptions current) {
        for (KeyboardErgonomicsPreset preset : values()) {
            if (preset.matches(current)) {
                return preset;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
