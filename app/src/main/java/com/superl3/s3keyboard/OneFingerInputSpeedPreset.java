package com.superl3.s3keyboard;

enum OneFingerInputSpeedPreset implements SettingsLabelOption {
    STABLE(R.string.one_finger_speed_stable, 380, 190),
    BALANCED(R.string.one_finger_speed_balanced, 300, 140),
    FAST(R.string.one_finger_speed_fast, 220, 100),
    CUSTOM(R.string.settings_custom_state, -1, -1);

    private static final OneFingerInputSpeedPreset[] DISPLAY_ORDER = values();

    final int labelResId;
    final int actionHoldMs;
    final int targetDwellMs;

    OneFingerInputSpeedPreset(int labelResId, int actionHoldMs, int targetDwellMs) {
        this.labelResId = labelResId;
        this.actionHoldMs = actionHoldMs;
        this.targetDwellMs = targetDwellMs;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    boolean isConcrete() {
        return this != CUSTOM;
    }

    boolean matches(int currentActionHoldMs, int currentTargetDwellMs) {
        return isConcrete()
                && actionHoldMs == currentActionHoldMs
                && targetDwellMs == currentTargetDwellMs;
    }

    static OneFingerInputSpeedPreset findMatching(int actionHoldMs, int targetDwellMs) {
        for (OneFingerInputSpeedPreset preset : DISPLAY_ORDER) {
            if (preset.matches(actionHoldMs, targetDwellMs)) {
                return preset;
            }
        }
        return CUSTOM;
    }

    static OneFingerInputSpeedPreset[] displayOrder() {
        return DISPLAY_ORDER.clone();
    }

    static int indexOf(OneFingerInputSpeedPreset selected) {
        for (int i = 0; i < DISPLAY_ORDER.length; i++) {
            if (DISPLAY_ORDER[i] == selected) {
                return i;
            }
        }
        return DISPLAY_ORDER.length - 1;
    }
}
