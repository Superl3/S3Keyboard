package com.superl3.s3keyboard;

import android.os.Build;
import android.view.inputmethod.InputConnection;

final class InputConnectionTextOperator {
    private InputConnectionTextOperator() {
    }

    static HangulCommitOnlyEditor.Sink commitOnlySink(InputConnection inputConnection) {
        return new CommitOnlySink(inputConnection);
    }

    private static final class CommitOnlySink implements HangulCommitOnlyEditor.Sink {
        private final InputConnection inputConnection;

        CommitOnlySink(InputConnection inputConnection) {
            this.inputConnection = inputConnection;
        }

        @Override
        public void deleteBeforeCursorCodePoints(int count) {
            InputConnectionTextOperator.deleteBeforeCursorCodePoints(inputConnection, count);
        }

        @Override
        public void commitText(String text) {
            InputConnectionTextOperator.commitText(inputConnection, text);
        }
    }

    static void deleteBeforeCursorCodePoints(InputConnection inputConnection, int count) {
        if (inputConnection == null || count <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && inputConnection.deleteSurroundingTextInCodePoints(count, 0)) {
            return;
        }
        int requestedChars = count > Integer.MAX_VALUE / 2 ? Integer.MAX_VALUE : count * 2;
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(requestedChars, 0);
        inputConnection.deleteSurroundingText(
                EditorTextBoundaryPolicy.trailingCodePointUtf16UnitCount(beforeCursor, count),
                0);
    }

    static void deleteBeforeCursorCodePoint(InputConnection inputConnection) {
        deleteBeforeCursorCodePoints(inputConnection, 1);
    }

    static void finishComposing(InputConnection inputConnection) {
        if (inputConnection != null) {
            inputConnection.finishComposingText();
        }
    }

    static void deleteCommittedGrapheme(InputConnection inputConnection) {
        if (inputConnection == null) {
            return;
        }
        // Some editors keep stale composing spans after surrounding-text delete.
        finishComposing(inputConnection);
        if (!deleteSelectedText(inputConnection)) {
            deleteBeforeCursorGrapheme(inputConnection);
        }
        finishComposing(inputConnection);
    }

    static boolean deleteSelectedText(InputConnection inputConnection) {
        if (inputConnection == null) {
            return false;
        }
        CharSequence selected = inputConnection.getSelectedText(0);
        if (selected == null || selected.length() == 0) {
            return false;
        }
        // Never delete an adjacent grapheme when an editor rejects selection replacement.
        inputConnection.commitText("", 1);
        return true;
    }

    static boolean hasSelection(InputConnection inputConnection) {
        if (inputConnection == null) {
            return false;
        }
        CharSequence selected = inputConnection.getSelectedText(0);
        return selected != null && selected.length() > 0;
    }

    static void deleteBeforeCursorGrapheme(InputConnection inputConnection) {
        if (inputConnection == null) {
            return;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(128, 0);
        int utf16Units = EditorTextBoundaryPolicy.trailingGraphemeUtf16UnitCount(beforeCursor);
        if (utf16Units > 0 && inputConnection.deleteSurroundingText(utf16Units, 0)) {
            return;
        }
        deleteBeforeCursorCodePoints(inputConnection, 1);
    }

    static boolean isCursorAtBoundary(InputConnection inputConnection, boolean right) {
        if (inputConnection == null) {
            return true;
        }
        CharSequence surroundingText = right
                ? inputConnection.getTextAfterCursor(1, 0)
                : inputConnection.getTextBeforeCursor(1, 0);
        // Some WebView and remote editors do not expose surrounding text but still accept DPAD keys.
        return surroundingText != null && surroundingText.length() == 0;
    }

    static void updateComposing(
            InputConnection inputConnection,
            HangulAutomata automata,
            HangulCommitOnlyEditor commitOnlyEditor) {
        if (inputConnection == null || automata == null) {
            return;
        }
        if (commitOnlyEditor != null) {
            commitOnlyEditor.reset();
        }
        String composing = automata.getComposingText();
        if (composing.isEmpty()) {
            finishComposing(inputConnection);
        } else {
            inputConnection.setComposingText(composing, 1);
        }
    }

    static void commitCurrent(
            InputConnection inputConnection,
            HangulAutomata automata,
            HangulCommitOnlyEditor commitOnlyEditor) {
        if (inputConnection == null || automata == null) {
            return;
        }
        if (commitOnlyEditor != null && commitOnlyEditor.hasDisplayedComposing()) {
            commitOnlyEditor.finish(automata, commitOnlySink(inputConnection));
            finishComposing(inputConnection);
            return;
        }
        String composing = automata.flush();
        if (composing.isEmpty()) {
            finishComposing(inputConnection);
        } else {
            commitTextReplacingComposing(inputConnection, composing);
        }
        if (commitOnlyEditor != null) {
            commitOnlyEditor.reset();
        }
    }

    static boolean commitText(InputConnection inputConnection, String text) {
        if (inputConnection != null && text != null && !text.isEmpty()) {
            finishComposing(inputConnection);
            boolean committed = inputConnection.commitText(text, 1);
            finishComposing(inputConnection);
            return committed;
        }
        return false;
    }

    static void commitTextReplacingComposing(InputConnection inputConnection, String text) {
        if (inputConnection != null && text != null && !text.isEmpty()) {
            inputConnection.commitText(text, 1);
            finishComposing(inputConnection);
        }
    }
}
