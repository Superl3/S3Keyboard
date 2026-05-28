package com.superl3.s3keyboard;

final class GestureThresholdPolicy {
    private static final float DINGUL_CONSONANT_THRESHOLD_SCALE = 0.82f;
    private static final float DINGUL_VOWEL_THRESHOLD_SCALE = 0.72f;

    private GestureThresholdPolicy() {
    }

    static int baseThresholdDp(KeyboardSettings settings, GestureKey key) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        return thresholdForKey(safeSettings.gestureThresholdDp, key);
    }

    static int thresholdDp(
            KeyboardSettings settings,
            TouchBiasStore.Bias touchBias,
            GestureKey key,
            GestureAction action) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        int adjustment = touchBias == null ? 0 : touchBias.gestureThresholdAdjustmentForDirection(action);
        return thresholdForKey(safeSettings.gestureThresholdDp + adjustment, key);
    }

    private static int thresholdForKey(int thresholdDp, GestureKey key) {
        float scale = dingulThresholdScale(key);
        if (scale >= 1f) {
            return thresholdDp;
        }
        int scaled = Math.round(thresholdDp * scale);
        return Math.max(KeyboardSettings.MIN_GESTURE_THRESHOLD_DP, scaled);
    }

    private static float dingulThresholdScale(GestureKey key) {
        if (isDingulVowelGestureKey(key)) {
            return DINGUL_VOWEL_THRESHOLD_SCALE;
        }
        if (isDingulConsonantGestureKey(key)) {
            return DINGUL_CONSONANT_THRESHOLD_SCALE;
        }
        return 1f;
    }

    private static boolean isDingulVowelGestureKey(GestureKey key) {
        return key != null
                && (isDingulVowelCommand(key.tap)
                || hasHangulVowel(key.tap)
                || hasHangulVowel(key.upSlide)
                || hasHangulVowel(key.downSlide)
                || hasHangulVowel(key.leftSlide)
                || hasHangulVowel(key.rightSlide)
                || hasHangulVowel(key.longPress));
    }

    private static boolean isDingulConsonantGestureKey(GestureKey key) {
        return key != null
                && (hasHangulConsonant(key.tap)
                || hasHangulConsonant(key.upSlide)
                || hasHangulConsonant(key.downSlide)
                || hasHangulConsonant(key.leftSlide)
                || hasHangulConsonant(key.rightSlide)
                || hasHangulConsonant(key.longPress));
    }

    private static boolean isDingulVowelCommand(String value) {
        return KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(value)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(value);
    }

    private static boolean hasHangulVowel(String value) {
        if (value == null || KeyboardCommands.isCommand(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (HangulAutomata.isVowel(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHangulConsonant(String value) {
        if (value == null || KeyboardCommands.isCommand(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (HangulAutomata.isInitialConsonant(ch) || HangulAutomata.canBeFinalConsonant(ch)) {
                return true;
            }
        }
        return false;
    }
}
