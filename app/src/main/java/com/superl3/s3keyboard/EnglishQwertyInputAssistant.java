package com.superl3.s3keyboard;

import android.view.inputmethod.InputConnection;

import java.util.Collections;
import java.util.List;

final class EnglishQwertyInputAssistant {
    private static final int MAX_SUGGESTIONS = 3;
    private static final int TEXT_BEFORE_CURSOR_LIMIT = 48;

    private final EnglishQwertyCorrectionEngine correctionEngine;
    private String currentWord = "";

    EnglishQwertyInputAssistant() {
        this(null);
    }

    EnglishQwertyInputAssistant(EnglishQwertyCorrectionEngine correctionEngine) {
        this.correctionEngine = RuntimeDefaults.englishQwertyCorrectionEngine(correctionEngine);
    }

    void reset() {
        currentWord = "";
    }

    String currentWord() {
        return currentWord;
    }

    List<EnglishQwertyCorrectionEngine.Candidate> suggestions() {
        if (currentWord.isEmpty()) {
            return Collections.emptyList();
        }
        return correctionEngine.suggest(currentWord, MAX_SUGGESTIONS);
    }

    void recordCommittedText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (continuesCurrentWord(text)) {
            currentWord += text;
        } else {
            reset();
        }
    }

    boolean continuesCurrentWord(String text) {
        return isAsciiLetters(text)
                || ("'".equals(text)
                && !currentWord.isEmpty()
                && currentWord.indexOf('\'') < 0);
    }

    void refreshFromEditor(InputConnection inputConnection) {
        if (inputConnection == null) {
            reset();
            return;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(TEXT_BEFORE_CURSOR_LIMIT, 0);
        currentWord = trailingAsciiWord(beforeCursor);
    }

    boolean autoCorrectCurrentWord(InputConnection inputConnection) {
        if (inputConnection == null || currentWord.isEmpty()) {
            return false;
        }
        String correction = correctionEngine.autoCorrection(currentWord);
        if (correction == null || correction.equals(currentWord)) {
            return false;
        }
        replaceCurrentWord(inputConnection, correction);
        return true;
    }

    boolean replaceCurrentWord(InputConnection inputConnection, String replacement) {
        if (inputConnection == null || replacement == null || replacement.isEmpty()) {
            return false;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(TEXT_BEFORE_CURSOR_LIMIT, 0);
        String editorWord = trailingAsciiWord(beforeCursor);
        if (currentWord.isEmpty()) {
            currentWord = editorWord;
        } else if (!currentWord.equals(editorWord)) {
            currentWord = editorWord;
            return false;
        }
        if (currentWord.isEmpty() || replacement.equals(currentWord)) {
            return false;
        }
        inputConnection.beginBatchEdit();
        try {
            InputConnectionTextOperator.deleteBeforeCursorCodePoints(inputConnection, currentWord.length());
            if (!InputConnectionTextOperator.commitText(inputConnection, replacement)) {
                return false;
            }
        } finally {
            inputConnection.endBatchEdit();
        }
        currentWord = replacement;
        return true;
    }

    private static boolean isAsciiLetters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!isAsciiLetter(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String trailingAsciiWord(CharSequence text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        int end = text.length();
        int start = end;
        while (start > 0 && isWordCharacter(text, start - 1, end)) {
            start--;
        }
        return start == end ? "" : text.subSequence(start, end).toString();
    }

    private static boolean isWordCharacter(CharSequence text, int index, int end) {
        char ch = text.charAt(index);
        if (isAsciiLetter(ch)) {
            return true;
        }
        return ch == '\''
                && index > 0
                && index < end - 1
                && isAsciiLetter(text.charAt(index - 1))
                && isAsciiLetter(text.charAt(index + 1));
    }

    private static boolean isAsciiLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }
}
