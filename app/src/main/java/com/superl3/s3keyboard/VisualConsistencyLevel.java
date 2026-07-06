package com.superl3.s3keyboard;

enum VisualConsistencyLevel implements SettingsLabelOption {
    NONE("none", R.string.visual_consistency_none, 0f, 0.96f, 0.84f),
    SUBTLE("subtle", R.string.visual_consistency_subtle, 0.03f, 0.94f, 0.80f),
    BALANCED("balanced", R.string.visual_consistency_balanced, 0.05f, 0.90f, 0.76f),
    STRONG("strong", R.string.visual_consistency_strong, 0.08f, 0.86f, 0.72f);

    final String preferenceValue;
    final int labelResId;
    final float maxMainShiftRatio;
    final float backspaceVisualScale;
    final float functionVisualScale;
    private static final VisualConsistencyLevel[] DISPLAY_ORDER = values();

    VisualConsistencyLevel(
            String preferenceValue,
            int labelResId,
            float maxMainShiftRatio,
            float backspaceVisualScale,
            float functionVisualScale) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
        this.maxMainShiftRatio = maxMainShiftRatio;
        this.backspaceVisualScale = backspaceVisualScale;
        this.functionVisualScale = functionVisualScale;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    static VisualConsistencyLevel fromPreference(String value) {
        for (VisualConsistencyLevel level : DISPLAY_ORDER) {
            if (level.preferenceValue.equals(value)) {
                return level;
            }
        }
        return NONE;
    }

    static VisualConsistencyLevel[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(VisualConsistencyLevel selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return preferenceValue;
    }
}
