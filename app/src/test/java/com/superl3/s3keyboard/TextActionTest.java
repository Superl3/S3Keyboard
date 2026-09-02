package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class TextActionTest {
    @Test
    public void selectedRangeWinsOverSentenceExtraction() {
        TextActionRange range = TextActionRange.resolve("One. teh  word here. Three", 100, 105, 119);
        assertNotNull(range);
        assertEquals(105, range.start);
        assertEquals(119, range.end);
        assertEquals("teh  word here", range.text);
    }

    @Test
    public void collapsedCursorTargetsOnlyCurrentSentence() {
        TextActionRange range = TextActionRange.resolve("First.  teh  thing here! Last", 20, 31, 31);
        assertNotNull(range);
        assertEquals("teh  thing here", range.text);
        assertEquals(28, range.start);
        assertEquals(43, range.end);
        assertEquals(31, range.selectionStart);
        assertEquals(31, range.selectionEnd);
    }

    @Test
    public void deterministicCorrectionIsLocalAndRepeatable() {
        assertEquals("The word, and don't.", TextActionEngine.correct("teh  word , adn dont."));
        assertEquals("이미 정상", TextActionEngine.correct("이미 정상"));
        assertTrue(TextAction.CORRECT.isLocallyAvailable());
        assertFalse(TextAction.POLISH.isLocallyAvailable());
    }

    @Test
    public void transactionRestoresExactOriginalSelection() {
        RecordingEditor editor = new RecordingEditor();
        TextActionRange range = TextActionRange.resolve("xx teh yy", 10, 13, 16);
        TextActionTransaction transaction = TextActionTransaction.apply(editor, "pkg", range, "the");
        assertNotNull(transaction);
        assertEquals("select:13:16", editor.calls.get(0));
        assertEquals("commit:the", editor.calls.get(1));

        assertTrue(transaction.restore(editor, "pkg"));
        assertEquals("select:13:16", editor.calls.get(2));
        assertEquals("commit:teh", editor.calls.get(3));
        assertEquals("select:13:16", editor.calls.get(4));
        assertFalse(transaction.restore(editor, "other.pkg"));
    }

    @Test
    public void unchangedCorrectionDoesNotCreateUndoSnapshot() {
        RecordingEditor editor = new RecordingEditor();
        TextActionRange range = TextActionRange.resolve("Already fine", 0, 7, 7);
        assertNull(TextActionTransaction.apply(editor, "pkg", range, range.text));
        assertTrue(editor.calls.isEmpty());
    }

    private static final class RecordingEditor implements TextActionTransaction.Editor {
        final List<String> calls = new ArrayList<>();

        @Override
        public boolean setSelection(int start, int end) {
            calls.add("select:" + start + ":" + end);
            return true;
        }

        @Override
        public boolean commitText(String text) {
            calls.add("commit:" + text);
            return true;
        }
    }
}
