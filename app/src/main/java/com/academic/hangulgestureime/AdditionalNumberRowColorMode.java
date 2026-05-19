package com.academic.hangulgestureime;

enum AdditionalNumberRowColorMode {
    FULL_DIMMED("full_dimmed", "Full dimmed"),
    FULL_DEFAULT("full_default", "Full default"),
    CENTER_DIMMED("center_dimmed", "Half dimmed (4-7)");

    final String preferenceValue;
    final String label;

    AdditionalNumberRowColorMode(String preferenceValue, String label) {
        this.preferenceValue = preferenceValue;
        this.label = label;
    }

    static AdditionalNumberRowColorMode fromPreference(String value) {
        for (AdditionalNumberRowColorMode mode : values()) {
            if (mode.preferenceValue.equals(value)) {
                return mode;
            }
        }
        return FULL_DIMMED;
    }

    @Override
    public String toString() {
        return label;
    }
}
