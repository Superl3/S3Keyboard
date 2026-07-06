package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SingleTapCommitModePolicyTest {
    @Test
    public void dingulSlideInputUsesOneShotHoldCommitInSingleTapMode() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);

        assertTrue(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                giyeok,
                giyeok.valueFor(GestureAction.RIGHT)));
    }

    @Test
    public void dingulVowelCommandUsesOneShotHoldCommitInSingleTapMode() {
        GestureKey centerVowel = new GestureKey(
                "\u3163.",
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                "\u3157",
                "\u315C",
                "\u3153",
                "\u314F",
                null);

        assertTrue(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                centerVowel,
                centerVowel.valueFor(GestureAction.TAP)));
        assertTrue(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                centerVowel,
                centerVowel.valueFor(GestureAction.RIGHT)));
    }

    @Test
    public void cursorRepeatStaysRepeatableInSingleTapMode() {
        GestureKey space = GestureKey.command("space", KeyboardCommands.CMD_SPACE);

        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                space,
                KeyboardCommands.CMD_MOVE_LEFT));
        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                space,
                KeyboardCommands.CMD_MOVE_RIGHT));
    }

    @Test
    public void policyIsInactiveOutsideSingleTapMode() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);

        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                false,
                giyeok,
                giyeok.valueFor(GestureAction.RIGHT)));
    }

    @Test
    public void continuationOnlyAcceptsDingulMainKeys() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);
        GestureKey backspace = GestureKey.command("delete", KeyboardCommands.CMD_DELETE);

        assertTrue(SingleTapCommitModePolicy.isContinuationMainKey(
                true,
                KeyboardMode.HANGUL,
                true,
                giyeok,
                true));
        assertFalse(SingleTapCommitModePolicy.isContinuationMainKey(
                true,
                KeyboardMode.HANGUL,
                true,
                backspace,
                false));
        assertFalse(SingleTapCommitModePolicy.isContinuationMainKey(
                true,
                KeyboardMode.ENGLISH,
                true,
                giyeok,
                true));
    }
}
