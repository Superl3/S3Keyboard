package com.superl3.s3keyboard;

final class SingleTapCommitModePolicy {
    enum SelectedMoveDecision {
        WAIT,
        COMMIT,
        CANCEL
    }

    static final class SelectedRelease {
        final SelectedMoveDecision decision;
        final GestureAction action;
        final String value;

        SelectedRelease(SelectedMoveDecision decision, GestureAction action, String value) {
            this.decision = decision;
            this.action = action;
            this.value = value;
        }
    }

    private SingleTapCommitModePolicy() {
    }

    static boolean usesOneShotHoldCommit(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            String value) {
        return enabled
                && keyboardMode == KeyboardMode.HANGUL
                && activeLayoutIsDingul
                && hasCommitValue(value)
                && isInputRepeatKey(key)
                && !isCursorMove(value);
    }

    static boolean hasCommitValue(String value) {
        return value != null
                && !value.isEmpty()
                && !KeyboardCommands.CMD_NOOP.equals(value);
    }

    static SelectedMoveDecision selectedMoveDecision(
            boolean directionLocked,
            GestureKey key,
            GestureAction action) {
        if (key != null && isDingulSpecialKey(key)) {
            return SelectedMoveDecision.WAIT;
        }
        if (!directionLocked) {
            return SelectedMoveDecision.WAIT;
        }
        return key != null && hasCommitValue(key.mappedValueFor(action))
                ? SelectedMoveDecision.COMMIT
                : SelectedMoveDecision.CANCEL;
    }

    static SelectedRelease resolveSelectedRelease(
            GestureState gestureState,
            GestureKey key,
            float dx,
            float dy,
            float baseThreshold,
            float upThreshold,
            float downThreshold,
            float leftThreshold,
            float rightThreshold,
            float axisDominanceRatio) {
        if (gestureState == null || key == null) {
            return new SelectedRelease(
                    SelectedMoveDecision.CANCEL,
                    GestureAction.TAP,
                    null);
        }
        if (isDingulSpecialKey(key)) {
            return new SelectedRelease(SelectedMoveDecision.WAIT, GestureAction.TAP, null);
        }
        GestureAction action = gestureState.release(
                dx,
                dy,
                baseThreshold,
                upThreshold,
                downThreshold,
                leftThreshold,
                rightThreshold,
                axisDominanceRatio);
        SelectedMoveDecision decision = selectedMoveDecision(
                action != GestureAction.TAP,
                key,
                action);
        return new SelectedRelease(
                decision,
                action,
                decision == SelectedMoveDecision.COMMIT ? key.mappedValueFor(action) : null);
    }

    static boolean pausesTapHoldForMovement(
            float dx,
            float dy,
            float baseGestureThreshold,
            float minimumDeadZone) {
        float deadZone = Math.max(
                Math.max(0f, minimumDeadZone),
                Math.max(0f, baseGestureThreshold) * 0.32f);
        return dx * dx + dy * dy > deadZone * deadZone;
    }

    static boolean isContinuationMainKey(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            boolean dingulMainKey) {
        return isContinuationKey(enabled, keyboardMode, activeLayoutIsDingul, key, dingulMainKey);
    }

    static boolean isContinuationKey(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            boolean dingulMainKey) {
        return enabled
                && keyboardMode == KeyboardMode.HANGUL
                && activeLayoutIsDingul
                && key != null
                && (dingulMainKey || isDingulSpecialKey(key) || isBottomControlKey(key));
    }

    static boolean isInitialHoldKey(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            boolean dingulMainKey) {
        return isContinuationKey(enabled, keyboardMode, activeLayoutIsDingul, key, dingulMainKey)
                && !KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap)
                && !KeyboardCommands.CMD_DELETE.equals(key.tap);
    }

    static boolean usesDirectSameKeyTapAfterSlide(
            boolean enabled,
            KeyboardMode keyboardMode,
            boolean activeLayoutIsDingul,
            GestureKey key,
            boolean dingulMainKey,
            GestureAction lastCommittedAction) {
        return enabled
                && keyboardMode == KeyboardMode.HANGUL
                && activeLayoutIsDingul
                && dingulMainKey
                && isDirectionalAction(lastCommittedAction)
                && isHangulConsonantTap(key);
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

    private static boolean isDirectionalAction(GestureAction action) {
        return action == GestureAction.UP
                || action == GestureAction.DOWN
                || action == GestureAction.LEFT
                || action == GestureAction.RIGHT;
    }

    private static boolean isHangulConsonantTap(GestureKey key) {
        return key != null
                && key.tap != null
                && key.tap.length() == 1
                && HangulAutomata.isInitialConsonant(key.tap.charAt(0));
    }

    private static boolean isDingulSpecialKey(GestureKey key) {
        return KeyboardCommands.CMD_DELETE.equals(key.tap)
                || "?".equals(key.label)
                || ".".equals(key.label)
                || "/".equals(key.label);
    }

    private static boolean isBottomControlKey(GestureKey key) {
        return KeyboardCommands.CMD_SPACE.equals(key.tap)
                || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)
                || KeyboardCommands.CMD_ENTER.equals(key.tap)
                || KeyboardCommands.CMD_RESERVED_PHRASES.equals(key.tap)
                || KeyboardCommands.CMD_OPEN_OPTIONS.equals(key.tap);
    }
}
