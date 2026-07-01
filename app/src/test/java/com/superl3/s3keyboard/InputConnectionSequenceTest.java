package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class InputConnectionSequenceTest {
    @Test
    public void textSessionUsesConventionalComposingDeleteAndEnterFallbackOrder() {
        FakeConnection fake = new FakeConnection();
        HangulAutomata automata = new HangulAutomata();
        HangulCommitOnlyEditor commitOnlyEditor = new HangulCommitOnlyEditor();

        automata.input('\u3131');
        InputConnectionTextOperator.updateComposing(
                fake.connection(),
                automata,
                commitOnlyEditor);
        automata.input('\u314F');
        InputConnectionTextOperator.updateComposing(
                fake.connection(),
                automata,
                commitOnlyEditor);
        InputConnectionTextOperator.commitCurrent(
                fake.connection(),
                automata,
                commitOnlyEditor);
        InputConnectionTextOperator.deleteCommittedCodePoint(fake.connection());
        fake.performEditorActionResult = false;
        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                (keyCode, metaState) -> ImeConnectionDispatcher.sendSoftKeyAt(
                        fake.connection(),
                        keyCode,
                        metaState,
                        100L),
                null);

        assertEquals("setComposingText", fake.calls.get(0));
        assertEquals("setComposingText", fake.calls.get(1));
        assertEquals("commitText", fake.calls.get(2));
        assertEquals("\uAC00", fake.committedText.get(0));
        assertEquals("finishComposingText", fake.calls.get(3));
        int performIndex = fake.calls.indexOf("performEditorAction");
        assertTrue("delete must happen before editor-action fallback", performIndex > 5);
        assertTrue("delete call missing before editor-action fallback", hasDeleteBetween(fake.calls, 4, performIndex));
        assertEquals("finishComposingText", fake.calls.get(performIndex - 1));
        assertEquals("sendKeyEvent", fake.calls.get(performIndex + 1));
        assertEquals("sendKeyEvent", fake.calls.get(performIndex + 2));
    }

    @Test
    public void hangulComposingReplacementDoesNotAppendIntermediateJamoAndSyllables() {
        StatefulConnection fake = new StatefulConnection();
        HangulAutomata automata = new HangulAutomata();
        HangulCommitOnlyEditor commitOnlyEditor = new HangulCommitOnlyEditor();

        automata.input('\u3131');
        InputConnectionTextOperator.updateComposing(fake.connection(), automata, commitOnlyEditor);
        automata.input('\u314F');
        InputConnectionTextOperator.updateComposing(fake.connection(), automata, commitOnlyEditor);
        automata.input('\u3147');
        InputConnectionTextOperator.updateComposing(fake.connection(), automata, commitOnlyEditor);
        InputConnectionTextOperator.commitCurrent(fake.connection(), automata, commitOnlyEditor);

        assertEquals("\uAC15", fake.text.toString());
    }

    @Test
    public void hangulCommittedChunkReplacesPreviousComposingWhenStartingNextSyllable() {
        StatefulConnection fake = new StatefulConnection();
        HangulAutomata automata = new HangulAutomata();
        HangulCommitOnlyEditor commitOnlyEditor = new HangulCommitOnlyEditor();

        for (char ch : new char[] {'\u3131', '\u314F', '\u3134'}) {
            String committed = automata.input(ch);
            if (!committed.isEmpty()) {
                InputConnectionTextOperator.commitTextReplacingComposing(fake.connection(), committed);
            }
            InputConnectionTextOperator.updateComposing(fake.connection(), automata, commitOnlyEditor);
        }
        String committed = automata.input('\u3137');
        if (!committed.isEmpty()) {
            InputConnectionTextOperator.commitTextReplacingComposing(fake.connection(), committed);
        }
        InputConnectionTextOperator.updateComposing(fake.connection(), automata, commitOnlyEditor);

        assertEquals("\uAC04\u3137", fake.text.toString());
    }

    private static boolean hasDeleteBetween(List<String> calls, int startInclusive, int endExclusive) {
        for (int i = startInclusive; i < endExclusive; i++) {
            if (calls.get(i).startsWith("deleteSurroundingText")) {
                return true;
            }
        }
        return false;
    }

    private static final class FakeConnection implements InvocationHandler {
        boolean performEditorActionResult;
        final List<String> calls = new ArrayList<>();
        final List<String> committedText = new ArrayList<>();

        InputConnection connection() {
            return (InputConnection) Proxy.newProxyInstance(
                    InputConnection.class.getClassLoader(),
                    new Class<?>[] {InputConnection.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "setComposingText":
                    calls.add("setComposingText");
                    return true;
                case "finishComposingText":
                    calls.add("finishComposingText");
                    return true;
                case "commitText":
                    calls.add("commitText");
                    committedText.add(String.valueOf(args[0]));
                    return true;
                case "deleteSurroundingTextInCodePoints":
                    calls.add("deleteSurroundingTextInCodePoints:" + args[0] + ":" + args[1]);
                    return false;
                case "deleteSurroundingText":
                    calls.add("deleteSurroundingText:" + args[0] + ":" + args[1]);
                    return true;
                case "performEditorAction":
                    calls.add("performEditorAction");
                    return performEditorActionResult;
                case "sendKeyEvent":
                    calls.add("sendKeyEvent");
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

    private static final class StatefulConnection implements InvocationHandler {
        final StringBuilder text = new StringBuilder();
        int composingStart = -1;
        int composingEnd = -1;

        InputConnection connection() {
            return (InputConnection) Proxy.newProxyInstance(
                    InputConnection.class.getClassLoader(),
                    new Class<?>[] {InputConnection.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "setComposingText":
                    replaceComposing(String.valueOf(args[0]), true);
                    return true;
                case "commitText":
                    replaceComposing(String.valueOf(args[0]), false);
                    return true;
                case "finishComposingText":
                    composingStart = -1;
                    composingEnd = -1;
                    return true;
                case "deleteSurroundingTextInCodePoints":
                    return false;
                case "deleteSurroundingText":
                    deleteBeforeCursor((Integer) args[0]);
                    return true;
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private void replaceComposing(String value, boolean keepComposing) {
            int start = composingStart >= 0 ? composingStart : text.length();
            int end = composingEnd >= 0 ? composingEnd : text.length();
            text.replace(start, end, value);
            if (keepComposing) {
                composingStart = start;
                composingEnd = start + value.length();
            } else {
                composingStart = -1;
                composingEnd = -1;
            }
        }

        private void deleteBeforeCursor(int count) {
            int end = text.length();
            int start = Math.max(0, end - Math.max(0, count));
            text.delete(start, end);
            composingStart = -1;
            composingEnd = -1;
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
