package com.superl3.s3keyboard;

enum DingulVowelGestureProfile {
    STANDARD("standard", R.string.dingul_vowel_gesture_profile_standard, 0.72f, 1.15f),
    STABLE("stable", R.string.dingul_vowel_gesture_profile_stable, 0.64f, 1.28f),
    AGGRESSIVE("aggressive", R.string.dingul_vowel_gesture_profile_aggressive, 0.56f, 1.38f);

    static final DingulVowelGestureProfile DEFAULT = STABLE;

    final String preferenceValue;
    final int labelResId;
    final float thresholdScale;
    final float axisDominanceRatio;

    DingulVowelGestureProfile(
            String preferenceValue,
            int labelResId,
            float thresholdScale,
            float axisDominanceRatio) {
        this.preferenceValue = preferenceValue;
        this.labelResId = labelResId;
        this.thresholdScale = thresholdScale;
        this.axisDominanceRatio = axisDominanceRatio;
    }

    static DingulVowelGestureProfile fromPreference(String value) {
        if (value != null) {
            for (DingulVowelGestureProfile profile : values()) {
                if (profile.preferenceValue.equals(value)) {
                    return profile;
                }
            }
        }
        return DEFAULT;
    }
}
