package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class TypingEventJournalTest {
    @Test
    public void delayedRollbackLabelsOriginalTapAsMissedSlide() {
        String journal = "";
        journal = TypingEventJournal.appendInput(
                journal,
                input("a", "3131", "3131", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("b", "3145", "3145", GestureAction.TAP),
                50);

        journal = TypingEventJournal.appendDelete(journal, 10, 50);
        journal = TypingEventJournal.appendDelete(journal, 11, 50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("c", "3131", "3132", GestureAction.UP),
                50);

        assertEquals(
                TypingEventJournal.Label.MISSED_SLIDE,
                TypingEventJournal.latestLabelFor(journal, "a"));
    }

    @Test
    public void deeperRollbackAfterExtraInputLabelsOriginalInput() {
        String journal = "";
        journal = TypingEventJournal.appendInput(
                journal,
                input("a", "3131", "3131", GestureAction.TAP),
                80);
        journal = TypingEventJournal.appendInput(
                journal,
                input("b", "3145", "3145", GestureAction.TAP),
                80);
        journal = TypingEventJournal.appendInput(
                journal,
                input("c", "3147", "3147", GestureAction.TAP),
                80);

        journal = TypingEventJournal.appendDelete(journal, 10, 80);
        journal = TypingEventJournal.appendDelete(journal, 11, 80);
        journal = TypingEventJournal.appendDelete(journal, 12, 80);
        journal = TypingEventJournal.appendInput(
                journal,
                input("d", "3131", "3132", GestureAction.UP),
                80);

        assertEquals(
                TypingEventJournal.Label.MISSED_SLIDE,
                TypingEventJournal.latestLabelFor(journal, "a"));
    }

    @Test
    public void rollbackFromSlideToTapLabelsFalseSlide() {
        String journal = "";
        journal = TypingEventJournal.appendInput(
                journal,
                input("a", "3131", "3132", GestureAction.UP),
                50);

        journal = TypingEventJournal.appendDelete(journal, 20, 50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("b", "3131", "3131", GestureAction.TAP),
                50);

        assertEquals(
                TypingEventJournal.Label.FALSE_SLIDE,
                TypingEventJournal.latestLabelFor(journal, "a"));
    }

    @Test
    public void continuedTypingLabelsUnappliedShadowAsFalseAlarm() {
        String journal = "";
        journal = TypingEventJournal.appendInput(
                journal,
                input(
                        "a",
                        "3131",
                        "3131",
                        GestureAction.TAP,
                        "3131",
                        GestureAction.UP,
                        false),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("b", "3145", "3145", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("c", "3147", "3147", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("d", "3134", "3134", GestureAction.TAP),
                50);

        assertEquals(
                TypingEventJournal.Label.SHADOW_FALSE_ALARM,
                TypingEventJournal.latestLabelFor(journal, "a"));
    }

    @Test
    public void continuedTypingLabelsAcceptedSlide() {
        String journal = "";
        journal = TypingEventJournal.appendInput(
                journal,
                input("a", "3131", "3132", GestureAction.UP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("b", "3145", "3145", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("c", "3147", "3147", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("d", "3134", "3134", GestureAction.TAP),
                50);

        assertEquals(
                TypingEventJournal.Label.ACCEPTED_SLIDE,
                TypingEventJournal.latestLabelFor(journal, "a"));
    }

    @Test
    public void repeatedMissedSlideStatsAllowConservativeActiveCorrection() {
        String journal = appendMissedSlideEpisode("", "a", "b");
        journal = appendMissedSlideEpisode(journal, "c", "d");

        TypingEventJournal.CorrectionStats stats = TypingEventJournal.correctionStats(journal);

        assertEquals(2, stats.missedSlideCount("3131", "3131", GestureAction.UP));
        assertEquals(-2, stats.thresholdAdjustmentDp("3131", GestureAction.UP));
        assertEquals(
                true,
                stats.shouldApplyActiveSlide("3131", "3131", GestureAction.UP, 0.82f, true));
    }

    @Test
    public void shadowFalseAlarmBlocksActiveCorrection() {
        String journal = appendMissedSlideEpisode("", "a", "b");
        journal = appendMissedSlideEpisode(journal, "c", "d");
        journal = TypingEventJournal.appendInput(
                journal,
                input(
                        "e",
                        "3131",
                        "3131",
                        GestureAction.TAP,
                        "3131",
                        GestureAction.UP,
                        false),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("f", "3145", "3145", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("g", "3147", "3147", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendInput(
                journal,
                input("h", "3134", "3134", GestureAction.TAP),
                50);

        TypingEventJournal.CorrectionStats stats = TypingEventJournal.correctionStats(journal);

        assertEquals(1, stats.shadowFalseAlarmCount("3131", "3131", GestureAction.UP));
        assertEquals(0, stats.thresholdAdjustmentDp("3131", GestureAction.UP));
        assertEquals(
                false,
                stats.shouldApplyActiveSlide("3131", "3131", GestureAction.UP, 1.10f, true));
    }

    private static String appendMissedSlideEpisode(String journal, String tapId, String replacementId) {
        journal = TypingEventJournal.appendInput(
                journal,
                input(tapId, "3131", "3131", GestureAction.TAP),
                50);
        journal = TypingEventJournal.appendDelete(journal, 10, 50);
        return TypingEventJournal.appendInput(
                journal,
                input(replacementId, "3131", "3132", GestureAction.UP),
                50);
    }

    private static TypingEventJournal.Input input(
            String id,
            String keyCodePoints,
            String valueCodePoints,
            GestureAction action) {
        return input(id, keyCodePoints, valueCodePoints, action, "", null, false);
    }

    private static TypingEventJournal.Input input(
            String id,
            String keyCodePoints,
            String valueCodePoints,
            GestureAction action,
            String shadowKeyCodePoints,
            GestureAction shadowAction,
            boolean shadowApplied) {
        return new TypingEventJournal.Input(
                id,
                1L,
                KeyboardMode.HANGUL,
                keyCodePoints,
                valueCodePoints,
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
                shadowKeyCodePoints,
                shadowAction,
                1.2f,
                shadowApplied);
    }
}
