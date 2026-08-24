package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SingleTapCommitModePolicyTest {
    @Test
    public void directionalConsonantCanUseOneDwellForSameKeyTapContinuation() {
        GestureKey nieun = new GestureKey("ㄴ", "ㄴ", "ㄸ", "ㄷ", "ㅌ", "ㅌ", null);

        assertTrue(SingleTapCommitModePolicy.usesDirectSameKeyTapAfterSlide(
                true,
                KeyboardMode.HANGUL,
                true,
                nieun,
                true,
                GestureAction.LEFT));
        assertFalse(SingleTapCommitModePolicy.usesDirectSameKeyTapAfterSlide(
                true,
                KeyboardMode.HANGUL,
                true,
                nieun,
                true,
                GestureAction.TAP));
    }

    @Test
    public void sameKeyTapShortcutDoesNotAffectVowelsOrQwerty() {
        GestureKey vowel = new GestureKey("ㅣ.", "__dingul_center_vowel__", "ㅗ", "ㅜ", "ㅓ", "ㅏ", null);
        GestureKey latin = new GestureKey("a", "a", "A", null, null, null, null);

        assertFalse(SingleTapCommitModePolicy.usesDirectSameKeyTapAfterSlide(
                true,
                KeyboardMode.HANGUL,
                true,
                vowel,
                true,
                GestureAction.RIGHT));
        assertFalse(SingleTapCommitModePolicy.usesDirectSameKeyTapAfterSlide(
                true,
                KeyboardMode.ENGLISH,
                true,
                latin,
                true,
                GestureAction.UP));
    }

    @Test
    public void dingulSlideInputUsesOneShotHoldCommitInSingleTapMode() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);

        assertTrue(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.HANGUL,
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
                KeyboardMode.HANGUL,
                true,
                centerVowel,
                centerVowel.valueFor(GestureAction.TAP)));
        assertTrue(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.HANGUL,
                true,
                centerVowel,
                centerVowel.valueFor(GestureAction.RIGHT)));
    }

    @Test
    public void cursorRepeatStaysRepeatableInSingleTapMode() {
        GestureKey space = GestureKey.command("space", KeyboardCommands.CMD_SPACE);

        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.HANGUL,
                true,
                space,
                KeyboardCommands.CMD_MOVE_LEFT));
        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.HANGUL,
                true,
                space,
                KeyboardCommands.CMD_MOVE_RIGHT));
    }

    @Test
    public void policyIsInactiveOutsideSingleTapMode() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);

        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                false,
                KeyboardMode.HANGUL,
                true,
                giyeok,
                giyeok.valueFor(GestureAction.RIGHT)));
    }

    @Test
    public void oneFingerHoldPolicyNeverChangesQwertyOrNonDingulLayouts() {
        GestureKey qwertyA = new GestureKey("a", "a", "A", "@", null, null, null);

        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.ENGLISH,
                true,
                qwertyA,
                qwertyA.valueFor(GestureAction.UP)));
        assertFalse(SingleTapCommitModePolicy.usesOneShotHoldCommit(
                true,
                KeyboardMode.HANGUL,
                false,
                qwertyA,
                qwertyA.valueFor(GestureAction.UP)));
    }

    @Test
    public void continuationAcceptsDingulMainSpecialAndBottomControls() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);
        GestureKey backspace = GestureKey.command("delete", KeyboardCommands.CMD_DELETE);
        GestureKey space = GestureKey.command("space", KeyboardCommands.CMD_SPACE);

        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                giyeok,
                true));
        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                backspace,
                false));
        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                space,
                false));
        assertFalse(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.ENGLISH,
                true,
                giyeok,
                true));
    }

    @Test
    public void unmappedSlideCannotCommitOrConsumeASelection() {
        assertFalse(SingleTapCommitModePolicy.hasCommitValue(null));
        assertFalse(SingleTapCommitModePolicy.hasCommitValue(""));
        assertFalse(SingleTapCommitModePolicy.hasCommitValue(KeyboardCommands.CMD_NOOP));
        assertTrue(SingleTapCommitModePolicy.hasCommitValue("\u314F"));
        assertTrue(SingleTapCommitModePolicy.hasCommitValue(KeyboardCommands.CMD_DELETE));
    }

    @Test
    public void narrowSpecialKeysNeverCommitADirectionAndAlwaysWaitForTap() {
        GestureKey narrowSpecialKey = new GestureKey(
                "?",
                "?",
                "!",
                "*",
                "+",
                KeyboardCommands.CMD_NOOP,
                null);

        assertEquals(
                SingleTapCommitModePolicy.SelectedMoveDecision.WAIT,
                SingleTapCommitModePolicy.selectedMoveDecision(
                        false,
                        narrowSpecialKey,
                        GestureAction.RIGHT));
        assertEquals(
                SingleTapCommitModePolicy.SelectedMoveDecision.WAIT,
                SingleTapCommitModePolicy.selectedMoveDecision(
                        true,
                        narrowSpecialKey,
                        GestureAction.LEFT));
        assertEquals(
                SingleTapCommitModePolicy.SelectedMoveDecision.WAIT,
                SingleTapCommitModePolicy.selectedMoveDecision(
                        true,
                        narrowSpecialKey,
                        GestureAction.RIGHT));
    }

    @Test
    public void ordinaryKeyStillLocksAndCommitsADirection() {
        GestureKey giyeok = new GestureKey("ㄱ", "ㄱ", "ㄲ", "#", "ㅋ", "ㅋ", null);

        assertEquals(
                SingleTapCommitModePolicy.SelectedMoveDecision.WAIT,
                SingleTapCommitModePolicy.selectedMoveDecision(
                        false,
                        giyeok,
                        GestureAction.RIGHT));
        assertEquals(
                SingleTapCommitModePolicy.SelectedMoveDecision.COMMIT,
                SingleTapCommitModePolicy.selectedMoveDecision(
                        true,
                        giyeok,
                        GestureAction.RIGHT));
    }

    @Test
    public void narrowSpecialKeyReleaseIgnoresMovementAndKeepsWaitingForTapHold() {
        GestureKey backspace = GestureKey.command("delete", KeyboardCommands.CMD_DELETE);

        SingleTapCommitModePolicy.SelectedRelease release =
                SingleTapCommitModePolicy.resolveSelectedRelease(
                        new GestureState(),
                        backspace,
                        60f,
                        60f,
                        24f,
                        24f,
                        24f,
                        24f,
                        24f,
                        0.6f);

        assertEquals(SingleTapCommitModePolicy.SelectedMoveDecision.WAIT, release.decision);
        assertEquals(GestureAction.TAP, release.action);
        assertNull(release.value);
    }

    @Test
    public void backspaceIsNotAnInitialHoldKeySoItKeepsTheClassicLongPressRepeat() {
        GestureKey backspace = GestureKey.command("delete", KeyboardCommands.CMD_DELETE);

        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                backspace,
                false));
        assertFalse(SingleTapCommitModePolicy.isInitialHoldKey(
                true,
                KeyboardMode.HANGUL,
                true,
                backspace,
                false));
    }

    @Test
    public void deliberateMovementPausesTapHoldBeforeSlideLock() {
        assertFalse(SingleTapCommitModePolicy.pausesTapHoldForMovement(
                3f,
                4f,
                24f,
                6f));
        assertTrue(SingleTapCommitModePolicy.pausesTapHoldForMovement(
                8f,
                0f,
                24f,
                6f));
    }

    @Test
    public void fastReleaseCanCommitASelectedKeysSlideWithoutAPriorMoveLock() {
        GestureKey centerVowel = new GestureKey(
                "\u3163.",
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                "\u3157",
                "\u315C",
                "\u3153",
                "\u314F",
                null);
        GestureState gestureState = new GestureState();

        SingleTapCommitModePolicy.SelectedRelease release =
                SingleTapCommitModePolicy.resolveSelectedRelease(
                        gestureState,
                        centerVowel,
                        40f,
                        2f,
                        24f,
                        24f,
                        24f,
                        24f,
                        24f,
                        0.6f);

        assertEquals(SingleTapCommitModePolicy.SelectedMoveDecision.COMMIT, release.decision);
        assertEquals(GestureAction.RIGHT, release.action);
        assertEquals("\u314F", release.value);
        assertFalse(gestureState.isLocked());
    }

    @Test
    public void selectedReleaseWithoutDirectionKeepsWaitingForTapHold() {
        GestureKey giyeok = new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null);

        SingleTapCommitModePolicy.SelectedRelease release =
                SingleTapCommitModePolicy.resolveSelectedRelease(
                        new GestureState(),
                        giyeok,
                        3f,
                        4f,
                        24f,
                        24f,
                        24f,
                        24f,
                        24f,
                        0.6f);

        assertEquals(SingleTapCommitModePolicy.SelectedMoveDecision.WAIT, release.decision);
        assertEquals(GestureAction.TAP, release.action);
        assertNull(release.value);
    }

    @Test
    public void optionsKeyKeepsItsDirectTapSlideAndLongPressEscapePath() {
        GestureKey options = GestureKey.command("options", KeyboardCommands.CMD_OPEN_OPTIONS);

        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                options,
                false));
        assertFalse(SingleTapCommitModePolicy.isInitialHoldKey(
                true,
                KeyboardMode.HANGUL,
                true,
                options,
                false));
    }
}
