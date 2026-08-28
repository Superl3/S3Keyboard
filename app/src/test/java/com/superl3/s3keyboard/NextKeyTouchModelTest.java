package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class NextKeyTouchModelTest {
    @Test
    public void acceptedInputBuildsOneStepTransitionFromPreviousAcceptedKey() {
        NextKeyTouchModel model = NextKeyTouchModel.empty(11L);
        TypingEventJournal.Input first = input("first", "ㄱ", GestureAction.TAP);
        TypingEventJournal.Input second = input("second", "ㅏ", GestureAction.TAP);

        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP,
                first,
                null));
        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP,
                second,
                null));

        NextKeyTouchModel.CandidateStats stats = model.statsFor(
                "dingul:ㄱ",
                GestureAction.TAP,
                "dingul:ㅏ",
                GestureAction.TAP);
        assertNotNull(stats);
        assertEquals(1, stats.acceptedCount);
        assertEquals(2, model.contextCount());
    }

    @Test
    public void wrongOriginUpdatesCorrectionDirectionWithoutAdvancingContext() {
        NextKeyTouchModel model = NextKeyTouchModel.empty(12L);
        TypingEventJournal.Input previous = input("previous", "ㄱ", GestureAction.TAP);
        TypingEventJournal.Input wrong = input("wrong", "ㅎ", GestureAction.TAP);
        TypingEventJournal.Input replacement = input("replacement", "space", GestureAction.TAP);

        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP,
                previous,
                null));
        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.WRONG_ORIGIN_KEY,
                wrong,
                replacement));

        NextKeyTouchModel.CandidateStats wrongStats = model.statsFor(
                "dingul:ㄱ", GestureAction.TAP, "dingul:ㅎ", GestureAction.TAP);
        NextKeyTouchModel.CandidateStats replacementStats = model.statsFor(
                "dingul:ㄱ", GestureAction.TAP, "dingul:space", GestureAction.TAP);
        assertNotNull(wrongStats);
        assertNotNull(replacementStats);
        assertEquals(1, wrongStats.correctedFromCount);
        assertEquals(1, replacementStats.correctedToCount);
        assertNull(model.statsFor(
                "dingul:ㅎ", GestureAction.TAP, "dingul:space", GestureAction.TAP));
    }

    @Test
    public void ineligibleModesAndEmptyKeysDoNotEnterModel() {
        NextKeyTouchModel model = NextKeyTouchModel.empty(13L);
        TypingEventJournal.Input english = input(KeyboardMode.ENGLISH, "english", "a", GestureAction.TAP);
        TypingEventJournal.Input empty = input("empty", "", GestureAction.TAP);

        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP, english, null));
        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP, empty, null));

        assertEquals(0, model.contextCount());
    }

    @Test
    public void codecIsDeterministicAndLearningEpochIsolated() {
        NextKeyTouchModel model = NextKeyTouchModel.empty(14L);
        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_TAP,
                input("first", "ㄱ", GestureAction.TAP),
                null));
        model.apply(new TypingEventJournal.LearningEvent(
                TypingEventJournal.Label.ACCEPTED_SLIDE,
                input("second", "ㅏ", GestureAction.RIGHT),
                null));

        String encoded = model.encode();
        assertEquals(encoded, NextKeyTouchModel.decode(encoded, 14L).encode());
        assertEquals(0, NextKeyTouchModel.decode(encoded, 15L).contextCount());
    }

    private static TypingEventJournal.Input input(String id, String key, GestureAction action) {
        return input(KeyboardMode.HANGUL, id, key, action);
    }

    private static TypingEventJournal.Input input(
            KeyboardMode mode,
            String id,
            String key,
            GestureAction action) {
        return new TypingEventJournal.Input(
                id,
                1L,
                mode,
                key,
                key,
                action,
                action,
                10f,
                10f,
                12f,
                12f,
                40L,
                18,
                8,
                2,
                4,
                0f,
                0f,
                "",
                null,
                0f,
                false);
    }
}
