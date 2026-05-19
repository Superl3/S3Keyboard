package com.superl3.s3keyboard;

enum LegendStylePreset {
    DEFAULT("default"),
    DOTS("dots");

    final String preferenceValue;

    LegendStylePreset(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    static LegendStylePreset fromPreference(String value) {
        if (value != null) {
            for (LegendStylePreset preset : values()) {
                if (preset.preferenceValue.equals(value)) {
                    return preset;
                }
            }
        }
        return DEFAULT;
    }

    boolean hidesSlideHints() {
        return this == DOTS;
    }

    boolean replacesMainLegend() {
        return this == DOTS;
    }

    boolean drawsDotLegend() {
        return this == DOTS;
    }
}
