package com.superl3.s3keyboard;

final class GestureThresholdPolicy {
    private static final float DINGUL_VOWEL_THRESHOLD_SCALE = 0.64f;

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
        if (!isDingulVowelGestureKey(key)) {
            return thresholdDp;
        }
        int scaled = Math.round(thresholdDp * DINGUL_VOWEL_THRESHOLD_SCALE);
        return Math.max(KeyboardSettings.MIN_GESTURE_THRESHOLD_DP, scaled);
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
}
