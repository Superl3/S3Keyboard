package com.superl3.s3keyboard;

import android.os.Build;
import android.view.inputmethod.InputConnection;

final class InputConnectionTextOperator {
    private InputConnectionTextOperator() {
    }

    static HangulCommitOnlyEditor.Sink commitOnlySink(InputConnection inputConnection) {
        return new HangulCommitOnlyEditor.Sink() {
            @Override
            public void deleteBeforeCursorCodePoints(int count) {
                InputConnectionTextOperator.deleteBeforeCursorCodePoints(inputConnection, count);
            }

            @Override
            public void commitText(String text) {
                InputConnectionTextOperator.commitText(inputConnection, text);
            }
        };
    }

    static void deleteBeforeCursorCodePoints(InputConnection inputConnection, int count) {
        if (inputConnection == null || count <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && inputConnection.deleteSurroundingTextInCodePoints(count, 0)) {
            return;
        }
        inputConnection.deleteSurroundingText(count, 0);
    }

    static void deleteBeforeCursorCodePoint(InputConnection inputConnection) {
        deleteBeforeCursorCodePoints(inputConnection, 1);
    }

    static void finishComposing(InputConnection inputConnection) {
        if (inputConnection != null) {
            inputConnection.finishComposingText();
        }
    }

    static void deleteCommittedCodePoint(InputConnection inputConnection) {
        if (inputConnection == null) {
            return;
        }
        // Some editors keep stale composing spans after surrounding-text delete.
        finishComposing(inputConnection);
        deleteBeforeCursorCodePoints(inputConnection, 1);
        finishComposing(inputConnection);
    }

    static boolean isCursorAtBoundary(InputConnection inputConnection, boolean right) {
        if (inputConnection == null) {
            return true;
        }
        CharSequence surroundingText = right
                ? inputConnection.getTextAfterCursor(1, 0)
                : inputConnection.getTextBeforeCursor(1, 0);
        return surroundingText == null || surroundingText.length() == 0;
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

    static void commitText(InputConnection inputConnection, String text) {
        if (inputConnection != null && text != null && !text.isEmpty()) {
            finishComposing(inputConnection);
            inputConnection.commitText(text, 1);
            finishComposing(inputConnection);
        }
    }

    static void commitTextReplacingComposing(InputConnection inputConnection, String text) {
        if (inputConnection != null && text != null && !text.isEmpty()) {
            inputConnection.commitText(text, 1);
            finishComposing(inputConnection);
        }
    }
}
