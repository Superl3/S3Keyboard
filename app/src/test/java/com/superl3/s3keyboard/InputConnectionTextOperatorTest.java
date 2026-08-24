package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.InputConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class InputConnectionTextOperatorTest {
    @Test
    public void deleteCommittedGraphemeFinishesComposingAroundDelete() {
        FakeConnection fake = new FakeConnection();

        InputConnectionTextOperator.deleteCommittedGrapheme(fake.connection());

        assertEquals("finishComposingText", fake.calls.get(0));
        assertTrue(fake.calls.contains("deleteSurroundingText:1:0"));
        assertEquals("finishComposingText", fake.calls.get(fake.calls.size() - 1));
    }

    @Test
    public void singleCodePointDeleteAndFinishUseCentralOperatorPath() {
        FakeConnection fake = new FakeConnection();

        InputConnectionTextOperator.deleteBeforeCursorCodePoint(fake.connection());
        InputConnectionTextOperator.finishComposing(fake.connection());

        assertTrue(fake.calls.contains("deleteSurroundingText:1:0"));
        assertEquals("finishComposingText", fake.calls.get(fake.calls.size() - 1));
    }

    @Test
    public void cursorBoundaryChecksBeforeAndAfterCursor() {
        FakeConnection fake = new FakeConnection();
        fake.beforeCursor = "a";
        fake.afterCursor = "";

        assertFalse(InputConnectionTextOperator.isCursorAtBoundary(fake.connection(), false));
        assertTrue(InputConnectionTextOperator.isCursorAtBoundary(fake.connection(), true));
    }

    @Test
    public void unavailableSurroundingTextIsNotTreatedAsConfirmedBoundary() {
        FakeConnection fake = new FakeConnection();

        assertFalse(InputConnectionTextOperator.isCursorAtBoundary(fake.connection(), false));
        assertFalse(InputConnectionTextOperator.isCursorAtBoundary(fake.connection(), true));
        assertTrue(InputConnectionTextOperator.isCursorAtBoundary(null, true));
    }

    @Test
    public void updateComposingUsesComposingTextWhenAutomataHasDraft() {
        FakeConnection fake = new FakeConnection();
        HangulAutomata automata = new HangulAutomata();
        automata.input('\u3131');
        HangulCommitOnlyEditor commitOnlyEditor = new HangulCommitOnlyEditor();

        InputConnectionTextOperator.updateComposing(
                fake.connection(),
                automata,
                commitOnlyEditor);

        assertEquals("setComposingText", fake.calls.get(0));
        assertEquals("\u3131", fake.composingText.get(0));
    }

    @Test
    public void updateComposingFinishesWhenAutomataIsEmpty() {
        FakeConnection fake = new FakeConnection();

        InputConnectionTextOperator.updateComposing(
                fake.connection(),
                new HangulAutomata(),
                new HangulCommitOnlyEditor());

        assertEquals("finishComposingText", fake.calls.get(0));
    }

    @Test
    public void commitCurrentFlushesDraftTextByReplacingComposing() {
        FakeConnection fake = new FakeConnection();
        HangulAutomata automata = new HangulAutomata();
        automata.input('\u3131');

        InputConnectionTextOperator.commitCurrent(
                fake.connection(),
                automata,
                new HangulCommitOnlyEditor());

        assertEquals("commitText", fake.calls.get(0));
        assertEquals("finishComposingText", fake.calls.get(1));
        assertEquals("\u3131", fake.committedText.get(0));
    }

    @Test
    public void commitOnlySinkIgnoresEmptyCommitsAndDeletesPositiveCounts() {
        FakeConnection fake = new FakeConnection();
        HangulCommitOnlyEditor.Sink sink = InputConnectionTextOperator.commitOnlySink(fake.connection());

        sink.commitText("");
        sink.deleteBeforeCursorCodePoints(0);
        sink.commitText("x");
        sink.deleteBeforeCursorCodePoints(2);

        assertEquals("finishComposingText", fake.calls.get(0));
        assertEquals("commitText", fake.calls.get(1));
        assertEquals("finishComposingText", fake.calls.get(2));
        assertTrue(fake.calls.contains("deleteSurroundingText:2:0"));
        assertEquals("x", fake.committedText.get(0));
    }

    @Test
    public void finalTextCommitReportsWhetherTheEditorAcceptedIt() {
        FakeConnection accepted = new FakeConnection();
        FakeConnection rejected = new FakeConnection();
        rejected.commitTextResult = false;

        assertTrue(InputConnectionTextOperator.commitText(accepted.connection(), "voice"));
        assertFalse(InputConnectionTextOperator.commitText(rejected.connection(), "voice"));
        assertFalse(InputConnectionTextOperator.commitText(null, "voice"));
        assertFalse(InputConnectionTextOperator.commitText(accepted.connection(), ""));
    }

    @Test
    public void legacyDeleteConvertsCodePointsToUtf16Units() {
        assertEquals(2, EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount("a😀", 1));
        assertEquals(3, EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount("a😀", 2));
        assertEquals(1, EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount("가", 1));
        assertEquals(1, EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount(null, 1));
        assertEquals(0, EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount("a", 0));
    }

    @Test
    public void committedDeleteKeepsExtendedGraphemeClustersIntact() {
        String tonedThumb = "\uD83D\uDC4D\uD83C\uDFFD";
        String family = "\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66";
        String flag = "\uD83C\uDDFA\uD83C\uDDF8";
        String threeIndicators = "\uD83C\uDDFA\uD83C\uDDF8\uD83C\uDDE8";

        assertEquals(tonedThumb.length(),
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("a" + tonedThumb));
        assertEquals(family.length(),
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount(family));
        assertEquals(flag.length(),
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount(flag));
        assertEquals(2,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount(threeIndicators));
        assertEquals(3,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("1\uFE0F\u20E3"));
        assertEquals(2,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("a\u0301"));
        assertEquals(2,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("\r\n"));
        assertEquals(3,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("\u1100\u1161\u11A8"));
        assertEquals(1,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount("abc"));
        assertEquals(0,
                EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount(null));
    }

    @Test
    public void committedDeleteUsesTheWholeTrailingGraphemeWidth() {
        FakeConnection fake = new FakeConnection();
        fake.beforeCursor = "\uD83D\uDC4D\uD83C\uDFFD";

        InputConnectionTextOperator.deleteCommittedGrapheme(fake.connection());

        assertTrue(fake.calls.contains("deleteSurroundingText:4:0"));
    }

    @Test
    public void committedDeleteReplacesSelectionBeforeLookingBehindCursor() {
        FakeConnection fake = new FakeConnection();
        fake.selectedText = "selected";

        InputConnectionTextOperator.deleteCommittedGrapheme(fake.connection());

        assertTrue(fake.calls.contains("getSelectedText"));
        assertTrue(fake.calls.contains("commitText"));
        assertFalse(fake.calls.contains("getTextBeforeCursor"));
        assertEquals("", fake.committedText.get(0));
    }

    @Test
    public void selectionDetectionDistinguishesRangeFromCollapsedCursor() {
        FakeConnection selected = new FakeConnection();
        selected.selectedText = "range";
        FakeConnection collapsed = new FakeConnection();
        collapsed.selectedText = "";

        assertTrue(InputConnectionTextOperator.hasSelection(selected.connection()));
        assertFalse(InputConnectionTextOperator.hasSelection(collapsed.connection()));
        assertFalse(InputConnectionTextOperator.hasSelection(null));
    }

    private static final class FakeConnection implements InvocationHandler {
        String beforeCursor;
        String afterCursor;
        String selectedText;
        boolean commitTextResult = true;
        final List<String> calls = new ArrayList<>();
        final List<String> committedText = new ArrayList<>();
        final List<String> composingText = new ArrayList<>();

        InputConnection connection() {
            return (InputConnection) Proxy.newProxyInstance(
                    InputConnection.class.getClassLoader(),
                    new Class<?>[] {InputConnection.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "finishComposingText":
                    calls.add("finishComposingText");
                    return true;
                case "deleteSurroundingTextInCodePoints":
                    calls.add("deleteSurroundingTextInCodePoints:" + args[0] + ":" + args[1]);
                    return false;
                case "deleteSurroundingText":
                    calls.add("deleteSurroundingText:" + args[0] + ":" + args[1]);
                    return true;
                case "getTextBeforeCursor":
                    calls.add("getTextBeforeCursor");
                    return beforeCursor;
                case "getTextAfterCursor":
                    calls.add("getTextAfterCursor");
                    return afterCursor;
                case "getSelectedText":
                    calls.add("getSelectedText");
                    return selectedText;
                case "setComposingText":
                    calls.add("setComposingText");
                    composingText.add(String.valueOf(args[0]));
                    return true;
                case "commitText":
                    calls.add("commitText");
                    committedText.add(String.valueOf(args[0]));
                    return commitTextResult;
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == Boolean.TYPE) {
                return false;
            }
            if (returnType == Integer.TYPE) {
                return 0;
            }
            if (returnType == Long.TYPE) {
                return 0L;
            }
            if (returnType == Float.TYPE) {
                return 0f;
            }
            if (returnType == Double.TYPE) {
                return 0d;
            }
            if (returnType == Void.TYPE) {
                return null;
            }
            return null;
        }
    }
}
