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
    public void deleteCommittedCodePointFinishesComposingAroundDelete() {
        FakeConnection fake = new FakeConnection();

        InputConnectionTextOperator.deleteCommittedCodePoint(fake.connection());

        assertEquals("finishComposingText", fake.calls.get(0));
        assertTrue(fake.calls.get(1).startsWith("deleteSurroundingText"));
        assertEquals("finishComposingText", fake.calls.get(2));
    }

    @Test
    public void singleCodePointDeleteAndFinishUseCentralOperatorPath() {
        FakeConnection fake = new FakeConnection();

        InputConnectionTextOperator.deleteBeforeCursorCodePoint(fake.connection());
        InputConnectionTextOperator.finishComposing(fake.connection());

        assertTrue(fake.calls.get(0).startsWith("deleteSurroundingText"));
        assertEquals("finishComposingText", fake.calls.get(1));
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
    public void commitCurrentFlushesDraftTextAndFinishesComposingFirst() {
        FakeConnection fake = new FakeConnection();
        HangulAutomata automata = new HangulAutomata();
        automata.input('\u3131');

        InputConnectionTextOperator.commitCurrent(
                fake.connection(),
                automata,
                new HangulCommitOnlyEditor());

        assertEquals("finishComposingText", fake.calls.get(0));
        assertEquals("commitText", fake.calls.get(1));
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
        assertTrue(fake.calls.get(3).startsWith("deleteSurroundingText"));
        assertEquals("x", fake.committedText.get(0));
    }

    private static final class FakeConnection implements InvocationHandler {
        String beforeCursor;
        String afterCursor;
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
                case "setComposingText":
                    calls.add("setComposingText");
                    composingText.add(String.valueOf(args[0]));
                    return true;
                case "commitText":
                    calls.add("commitText");
                    committedText.add(String.valueOf(args[0]));
                    return true;
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
