package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.inputmethod.InputConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class EnglishQwertyInputAssistantTest {
    @Test
    public void tracksCurrentWordAndSuggestsCorrection() {
        EnglishQwertyInputAssistant assistant = new EnglishQwertyInputAssistant();

        assistant.recordCommittedText("t");
        assistant.recordCommittedText("e");
        assistant.recordCommittedText("h");

        assertEquals("teh", assistant.currentWord());
        assertEquals("the", assistant.suggestions().get(0).text);
    }

    @Test
    public void nullCorrectionEngineUsesDefaultSuggestions() {
        EnglishQwertyInputAssistant assistant = new EnglishQwertyInputAssistant(null);

        assistant.recordCommittedText("t");
        assistant.recordCommittedText("e");
        assistant.recordCommittedText("h");

        assertEquals("the", assistant.suggestions().get(0).text);
    }

    @Test
    public void autoCorrectsCurrentWordThroughInputConnection() {
        EnglishQwertyInputAssistant assistant = new EnglishQwertyInputAssistant();
        FakeConnection fake = new FakeConnection();
        assistant.recordCommittedText("t");
        assistant.recordCommittedText("e");
        assistant.recordCommittedText("h");

        assertTrue(assistant.autoCorrectCurrentWord(fake.connection()));

        assertEquals("deleteSurroundingText:3:0", fake.calls.get(0));
        assertEquals("commitText:the", fake.calls.get(1));
        assertEquals("the", assistant.currentWord());
    }

    @Test
    public void refreshesTrailingWordFromEditorAfterDelete() {
        EnglishQwertyInputAssistant assistant = new EnglishQwertyInputAssistant();
        FakeConnection fake = new FakeConnection();
        fake.beforeCursor = "hello wor";

        assistant.refreshFromEditor(fake.connection());

        assertEquals("wor", assistant.currentWord());
    }

    @Test
    public void punctuationResetsCurrentWord() {
        EnglishQwertyInputAssistant assistant = new EnglishQwertyInputAssistant();

        assistant.recordCommittedText("h");
        assistant.recordCommittedText("i");
        assistant.recordCommittedText(".");

        assertEquals("", assistant.currentWord());
    }

    private static final class FakeConnection implements InvocationHandler {
        String beforeCursor;
        final List<String> calls = new ArrayList<>();

        InputConnection connection() {
            return (InputConnection) Proxy.newProxyInstance(
                    InputConnection.class.getClassLoader(),
                    new Class<?>[] {InputConnection.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "deleteSurroundingTextInCodePoints":
                    calls.add("deleteSurroundingTextInCodePoints:" + args[0] + ":" + args[1]);
                    return true;
                case "deleteSurroundingText":
                    calls.add("deleteSurroundingText:" + args[0] + ":" + args[1]);
                    return true;
                case "commitText":
                    calls.add("commitText:" + args[0]);
                    return true;
                case "getTextBeforeCursor":
                    return beforeCursor;
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
