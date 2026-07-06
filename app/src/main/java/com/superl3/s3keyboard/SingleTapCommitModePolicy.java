package com.superl3.s3keyboard;

final class SingleTapCommitModePolicy {
    private SingleTapCommitModePolicy() {
    }

    static boolean usesOneShotHoldCommit(boolean enabled, GestureKey key, String value) {
        return enabled
                && value != null
                && !value.isEmpty()
                && isInputRepeatKey(key)
                && !isCursorMove(value);
    }

    static boolean isContinuationMainKey(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            boolean dingulMainKey) {
        return enabled
                && keyboardMode == KeyboardMode.HANGUL
                && activeLayoutIsDingul
                && key != null
                && dingulMainKey;
    }

    private static boolean isInputRepeatKey(GestureKey key) {
        return key != null
                && (isRepeatableInputText(key.tap)
                || KeyboardCommands.CMD_SPACE.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(key.tap));
    }

    private static boolean isRepeatableInputText(String value) {
        return value != null && !value.isEmpty() && !KeyboardCommands.isCommand(value);
    }

    private static boolean isCursorMove(String value) {
        return KeyboardCommands.CMD_MOVE_LEFT.equals(value)
                || KeyboardCommands.CMD_MOVE_RIGHT.equals(value);
    }
}
