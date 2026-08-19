package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HangulCommitOnlyEditorTest {
    @Test
    public void composesSyllableByReplacingVisibleFallbackText() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();

        editor.input(automata, "\u3131", sink);
        editor.input(automata, "\u314F", sink);
        editor.input(automata, "\u3134", sink);

        assertEquals("\uAC04", sink.text.toString());
    }

    @Test
    public void carriesCommittedSyllableIntoNextSyllable() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();

        editor.input(automata, "\u3131\u314F\u3134\u3137\u314F", sink);

        assertEquals("\uAC04\uB2E4", sink.text.toString());
    }

    @Test
    public void backspaceEditsCurrentFallbackComposition() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131\u314F\u3134", sink);

        editor.backspace(automata, sink);
        assertEquals("\uAC00", sink.text.toString());

        editor.backspace(automata, sink);
        assertEquals("\u3131", sink.text.toString());

        editor.backspace(automata, sink);
        assertEquals("", sink.text.toString());
    }

    @Test
    public void finishDoesNotDuplicateAlreadyDisplayedComposition() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131\u314F\u3134", sink);

        editor.finish(automata, sink);

        assertEquals("\uAC04", sink.text.toString());
    }

    @Test
    public void ownCommitOnlySelectionUpdatesKeepFallbackCompositionAlive() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();

        editor.input(automata, "\u3131", sink);

        assertFalse(editor.shouldAcceptExternalSelectionChange(0, 0, 1, 1, -1, -1));

        editor.input(automata, "\u314F", sink);

        assertFalse(editor.shouldAcceptExternalSelectionChange(1, 1, 0, 0, -1, -1));
        assertFalse(editor.shouldAcceptExternalSelectionChange(0, 0, 1, 1, -1, -1));
        assertEquals("\uAC00", automata.getComposingText());
        assertEquals("\uAC00", sink.text.toString());
    }

    @Test
    public void externalSelectionChangeAcceptsDisplayedFallbackComposition() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131\u314F", sink);
        editor.shouldAcceptExternalSelectionChange(0, 0, 1, 1, -1, -1);
        editor.shouldAcceptExternalSelectionChange(1, 1, 0, 0, -1, -1);
        editor.shouldAcceptExternalSelectionChange(0, 0, 1, 1, -1, -1);

        assertTrue(editor.shouldAcceptExternalSelectionChange(1, 1, 0, 0, -1, -1));
        editor.acceptDisplayedComposition(automata);

        assertEquals("", automata.getComposingText());
        assertEquals("\uAC00", sink.text.toString());
    }

    @Test
    public void selectedRangeAlwaysBreaksFallbackCompositionEvenWithPendingOwnUpdates() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131", sink);

        assertTrue(editor.shouldAcceptExternalSelectionChange(
                0,
                0,
                0,
                2,
                -1,
                -1));
    }

    @Test
    public void unexpectedCaretMoveIsNotConsumedByPendingOwnCommit() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131", sink);

        assertTrue(editor.shouldAcceptExternalSelectionChange(
                1,
                1,
                0,
                0,
                -1,
                -1));
    }

    @Test
    public void coalescedDeleteAndReplacementCallbackConsumesBothExpectedDeltas() {
        HangulCommitOnlyEditor editor = new HangulCommitOnlyEditor();
        TestSink sink = new TestSink();
        HangulAutomata automata = new HangulAutomata();
        editor.input(automata, "\u3131", sink);
        assertFalse(editor.shouldAcceptExternalSelectionChange(0, 0, 1, 1, -1, -1));

        editor.input(automata, "\u314F", sink);

        assertFalse(editor.shouldAcceptExternalSelectionChange(1, 1, 1, 1, -1, -1));
        assertTrue(editor.shouldAcceptExternalSelectionChange(1, 1, 0, 0, -1, -1));
    }

    private static final class TestSink implements HangulCommitOnlyEditor.Sink {
        final StringBuilder text = new StringBuilder();

        @Override
        public void deleteBeforeCursorCodePoints(int count) {
            for (int i = 0; i < count && text.length() > 0; i++) {
                int offset = text.offsetByCodePoints(text.length(), -1);
                text.delete(offset, text.length());
            }
        }

        @Override
        public void commitText(String value) {
            text.append(value);
        }
    }
}
