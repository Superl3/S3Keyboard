package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public final class DingulOneFingerLayoutSequenceTest {
    private final List<KeyboardRow> rows = KeyboardLayoutFactory.build(
            KeyboardSettings.defaults(),
            KeyboardSurface.NORMAL,
            KeyboardLayoutProfiles.defaults().withDingulDotEnterKeyEnabled(false));

    @Test
    public void continuousFlowCanEnterThePracticePhraseUsingRealLayoutActions() {
        StringBuilder input = new StringBuilder();

        add(input, "ㅁ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㄴ", GestureAction.TAP);
        add(input, "ㄴ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㅅ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.LEFT);
        input.append(' ');

        add(input, "ㅁ", GestureAction.DOWN);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㄴ", GestureAction.TAP);
        add(input, "ㄱ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㅇ", GestureAction.TAP);
        add(input, "ㅢ", GestureAction.LEFT);
        add(input, "ㅇ", GestureAction.TAP);
        add(input, ". .", GestureAction.UP);
        input.append(' ');

        add(input, "ㄴ", GestureAction.LEFT);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㄱ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.RIGHT);
        add(input, "ㄱ", GestureAction.TAP);
        add(input, "ㄱ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.TAP);
        input.append(' ');

        add(input, "ㅇ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.LEFT);
        add(input, "ㄴ", GestureAction.UP);
        add(input, "ㅡㅐ", GestureAction.RIGHT);
        add(input, "ㅇ", GestureAction.TAP);
        add(input, ". .", GestureAction.UP);

        assertEquals("만나서 반가워요 타각기 어때요", compose(input));
    }

    @Test
    public void centerVowelRightSlideProducesGaAfterInitialHold() {
        StringBuilder input = new StringBuilder();
        add(input, "ㄱ", GestureAction.TAP);
        add(input, "ㅣ.", GestureAction.RIGHT);

        assertEquals("가", compose(input));
    }

    @Test
    public void centerVowelFastReleaseUsesTheRealLayoutMapping() {
        GestureKey centerVowel = find("ㅣ.");
        SingleTapCommitModePolicy.SelectedRelease release =
                SingleTapCommitModePolicy.resolveSelectedRelease(
                        new GestureState(),
                        centerVowel,
                        40f,
                        2f,
                        24f,
                        24f,
                        24f,
                        24f,
                        24f,
                        0.6f);
        StringBuilder input = new StringBuilder();
        add(input, "ㄱ", GestureAction.TAP);
        appendInputValue(input, release.value);

        assertEquals(SingleTapCommitModePolicy.SelectedMoveDecision.COMMIT, release.decision);
        assertEquals(GestureAction.RIGHT, release.action);
        assertEquals("가", compose(input));
    }

    @Test
    public void stateMachineCanEnterSentenceWithSameKeyReentryAndSpace() {
        OneFingerInputSession<GestureKey> session = new OneFingerInputSession<>();
        StringBuilder input = new StringBuilder();
        int reentryCount = 0;

        flowAdd(session, input, find("ㅁ"), GestureAction.TAP, true);
        flowAdd(session, input, find("ㅣ."), GestureAction.RIGHT, false);
        flowAdd(session, input, find("ㄴ"), GestureAction.TAP, false);
        if (flowAdd(session, input, find("ㄴ"), GestureAction.TAP, false)) {
            reentryCount++;
        }
        flowAdd(session, input, find("ㅣ."), GestureAction.RIGHT, false);
        flowAdd(session, input, find("ㅅ"), GestureAction.TAP, false);
        flowAdd(session, input, find("ㅣ."), GestureAction.LEFT, false);
        flowAdd(session, input, findByIcon(KeyIcon.SPACE), GestureAction.TAP, false);

        flowAdd(session, input, find("ㅁ"), GestureAction.DOWN, false);
        flowAdd(session, input, find("ㅣ."), GestureAction.RIGHT, false);
        flowAdd(session, input, find("ㄴ"), GestureAction.TAP, false);
        flowAdd(session, input, find("ㄱ"), GestureAction.TAP, false);
        flowAdd(session, input, find("ㅣ."), GestureAction.RIGHT, false);
        flowAdd(session, input, find("ㅇ"), GestureAction.TAP, false);
        flowAdd(session, input, find("ㅢ"), GestureAction.LEFT, false);
        flowAdd(session, input, find("ㅇ"), GestureAction.TAP, false);
        flowAdd(session, input, find(". ."), GestureAction.UP, false);

        assertEquals("만나서 반가워요", compose(input));
        assertEquals(1, reentryCount);
        assertEquals(OneFingerInputSession.State.COMMITTED_FREE_ROAM, session.state);
        assertTrue(session.hasCommitted);
        assertNull(session.targetSlot);
        assertFalse(session.hasPending());
    }

    @Test
    public void everyPracticeLessonIsReachableFromTheRealDingulMapping() {
        StringBuilder center = new StringBuilder();
        addSyllable(center, "ㅣ.", GestureAction.LEFT);
        addSyllable(center, "ㅣ.", GestureAction.RIGHT);
        addSyllable(center, "ㅣ.", GestureAction.UP);
        addSyllable(center, "ㅣ.", GestureAction.DOWN);
        assertEquals("어아오우", compose(center));

        StringBuilder top = new StringBuilder();
        addSyllable(top, "ㅢ", GestureAction.LEFT);
        addSyllable(top, "ㅢ", GestureAction.RIGHT);
        addSyllable(top, "ㅢ", GestureAction.UP);
        addSyllable(top, "ㅢ", GestureAction.DOWN);
        assertEquals("워와외위", compose(top));

        StringBuilder wide = new StringBuilder();
        addSyllable(wide, "ㅡㅐ", GestureAction.TAP);
        addSyllable(wide, "ㅡㅐ", GestureAction.LEFT);
        addSyllable(wide, "ㅡㅐ", GestureAction.RIGHT);
        addSyllable(wide, "ㅡㅐ", GestureAction.UP);
        addSyllable(wide, "ㅡㅐ", GestureAction.DOWN);
        assertEquals("으에애왜웨", compose(wide));

        StringBuilder dot = new StringBuilder();
        addSyllable(dot, ". .", GestureAction.UP);
        addSyllable(dot, ". .", GestureAction.RIGHT);
        addSyllable(dot, ". .", GestureAction.DOWN);
        addSyllable(dot, ". .", GestureAction.LEFT);
        assertEquals("요야유여", compose(dot));

        assertEquals("?.,/", specialRailTaps());
    }

    @Test
    public void continuationCommandKeysKeepTheirDingulActions() {
        GestureKey backspace = findByTap(KeyboardCommands.CMD_DELETE);
        assertEquals(KeyboardCommands.CMD_DELETE, backspace.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_DELETE_WORD, backspace.valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_DELETE_WORD, backspace.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_DELETE, backspace.valueFor(GestureAction.RIGHT));
        assertNull(backspace.mappedValueFor(GestureAction.RIGHT));

        GestureKey space = findByIcon(KeyIcon.SPACE);
        assertEquals(KeyboardCommands.CMD_SPACE, space.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_MOVE_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_MOVE_RIGHT, space.valueFor(GestureAction.RIGHT));

        assertContinuation(findByTap(KeyboardCommands.CMD_TOGGLE_LANGUAGE));
        assertContinuation(findByTap(KeyboardCommands.CMD_ENTER));
        assertContinuation(findByTap(KeyboardCommands.CMD_RESERVED_PHRASES));

        GestureKey options = findByTap(KeyboardCommands.CMD_OPEN_OPTIONS);
        assertContinuation(options);
        assertFalse(SingleTapCommitModePolicy.isInitialHoldKey(
                true,
                KeyboardMode.HANGUL,
                true,
                options,
                false));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, options.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, options.valueFor(GestureAction.LONG_PRESS));
    }

    private void assertContinuation(GestureKey key) {
        assertTrue(SingleTapCommitModePolicy.isContinuationKey(
                true,
                KeyboardMode.HANGUL,
                true,
                key,
                false));
    }

    private void addSyllable(StringBuilder input, String vowelLabel, GestureAction action) {
        add(input, "ㅇ", GestureAction.TAP);
        add(input, vowelLabel, action);
    }

    private String specialRailTaps() {
        StringBuilder output = new StringBuilder();
        add(output, "?", GestureAction.TAP);
        add(output, ".", GestureAction.TAP);
        add(output, ".", GestureAction.LEFT);
        add(output, "/", GestureAction.TAP);
        return output.toString();
    }

    private void add(StringBuilder input, String label, GestureAction action) {
        GestureKey key = find(label);
        appendInputValue(input, key.valueFor(action));
    }

    private boolean flowAdd(
            OneFingerInputSession<GestureKey> session,
            StringBuilder input,
            GestureKey key,
            GestureAction action,
            boolean initialKey) {
        boolean requiredSameKeyReentry = false;
        if (initialKey) {
            session.selectKey(key, 10f, 10f);
        } else {
            requiredSameKeyReentry = session.blocksCandidate(key);
            session.allowLastCommittedReentry();
            session.hoverCandidate(key, 20f, 20f);
            long selectionGeneration = session.beginPending(
                    OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                    100L,
                    140L);
            assertTrue(session.isCurrentPending(
                    OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                    selectionGeneration));
            session.selectHoveredCandidate(key);
            assertFalse(session.isCurrentPending(
                    OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                    selectionGeneration));
        }

        long actionGeneration = session.beginPending(
                OneFingerInputSession.PendingPhase.ACTION_COMMIT,
                300L,
                300L);
        if (action != GestureAction.TAP) {
            session.markSlideLocked();
        }
        String value = key.mappedValueFor(action);
        assertTrue(SingleTapCommitModePolicy.hasCommitValue(value));
        appendInputValue(input, value);
        session.markCommitted(key);
        assertFalse(session.isCurrentPending(
                OneFingerInputSession.PendingPhase.ACTION_COMMIT,
                actionGeneration));
        return requiredSameKeyReentry;
    }

    private static void appendInputValue(StringBuilder input, String value) {
        if (KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(value)) {
            input.append('ㅣ');
        } else if (KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(value)) {
            input.append('ㅡ');
        } else if (KeyboardCommands.CMD_SPACE.equals(value)) {
            input.append(' ');
        } else {
            input.append(value);
        }
    }

    private GestureKey find(String label) {
        for (KeyboardRow row : rows) {
            for (GestureKey key : row.keys) {
                if (label.equals(key.label)) {
                    return key;
                }
            }
        }
        throw new AssertionError("Missing key: " + label);
    }

    private GestureKey findByTap(String value) {
        for (KeyboardRow row : rows) {
            for (GestureKey key : row.keys) {
                if (value.equals(key.tap)) {
                    return key;
                }
            }
        }
        throw new AssertionError("Missing tap value: " + value);
    }

    private GestureKey findByIcon(int icon) {
        for (KeyboardRow row : rows) {
            for (GestureKey key : row.keys) {
                if (key.icon == icon) {
                    return key;
                }
            }
        }
        throw new AssertionError("Missing icon: " + icon);
    }

    private static String compose(CharSequence input) {
        HangulAutomata automata = new HangulAutomata();
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            output.append(automata.input(input.charAt(i)));
        }
        output.append(automata.flush());
        return output.toString();
    }
}
