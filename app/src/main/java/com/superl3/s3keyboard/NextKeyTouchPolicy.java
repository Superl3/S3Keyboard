package com.superl3.s3keyboard;

/** Stage-one eligibility and safety limits for adaptive next-key statistics. */
final class NextKeyTouchPolicy {
    static final int AMBIGUOUS_BAND_DP = 8;
    static final int MINIMUM_SAMPLES = 8;
    static final int MINIMUM_CORRECTIONS = 3;
    static final float MINIMUM_ODDS_RATIO = 2.5f;
    static final int MAXIMUM_PRIOR_SHIFT_DP = 6;
    static final int MAX_CONTEXTS = 128;
    static final int MAX_CANDIDATES_PER_CONTEXT = 8;

    private NextKeyTouchPolicy() {
    }

    static boolean eligibleInput(TypingEventJournal.Input input) {
        return input != null
                && input.keyboardMode == KeyboardMode.HANGUL
                && input.keyCodePoints != null
                && !input.keyCodePoints.isEmpty()
                && isTapOrDirectional(input.action);
    }

    static boolean eligibleCorrection(TypingEventJournal.LearningEvent event) {
        return event != null
                && event.label == TypingEventJournal.Label.WRONG_ORIGIN_KEY
                && eligibleInput(event.target)
                && eligibleInput(event.replacement);
    }

    static boolean isTapOrDirectional(GestureAction action) {
        return action == GestureAction.TAP
                || action == GestureAction.UP
                || action == GestureAction.DOWN
                || action == GestureAction.LEFT
                || action == GestureAction.RIGHT;
    }
}
