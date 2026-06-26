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
        assertEquals("finishComposingText", fake.calls.get(2));
        assertEquals("commitText", fake.calls.get(3));
        assertEquals("\uAC00", fake.committedText.get(0));
        assertEquals("finishComposingText", fake.calls.get(4));
        int performIndex = fake.calls.indexOf("performEditorAction");
        assertTrue("delete must happen before editor-action fallback", performIndex > 6);
        assertTrue("delete call missing before editor-action fallback", hasDeleteBetween(fake.calls, 5, performIndex));
        assertEquals("finishComposingText", fake.calls.get(performIndex - 1));
        assertEquals("sendKeyEvent", fake.calls.get(performIndex + 1));
        assertEquals("sendKeyEvent", fake.calls.get(performIndex + 2));
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
}
