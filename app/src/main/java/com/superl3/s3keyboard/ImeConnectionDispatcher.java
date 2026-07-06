package com.superl3.s3keyboard;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import java.util.function.IntBinaryOperator;

final class ImeConnectionDispatcher {
    private ImeConnectionDispatcher() {
    }

    static void performEnter(
            InputConnection inputConnection,
            ResolvedImeAction enterAction,
            boolean rawKeyInput,
            boolean remoteModeEnabled,
            IntBinaryOperator softKeySender,
            IntBinaryOperator remoteKeySender) {
        if (inputConnection == null) {
            return;
        }
        if (remoteModeEnabled) {
            send(remoteKeySender, KeyEvent.KEYCODE_ENTER, 0);
            return;
        }
        if (rawKeyInput) {
            if (!sendCompleteSoftKey(softKeySender, KeyEvent.KEYCODE_ENTER, 0)) {
                commitNewline(inputConnection);
            }
            return;
        }

        ResolvedImeAction action = enterAction == null
                ? ImeActionLabelResolver.defaultAction()
                : enterAction;
        if (action.performEditorAction
                && inputConnection.performEditorAction(action.editorActionId)) {
            return;
        }
        if (action.performEditorAction) {
            if (!sendCompleteSoftKey(softKeySender, KeyEvent.KEYCODE_ENTER, 0)) {
                commitNewline(inputConnection);
            }
            return;
        }
        if (!inputConnection.commitText("\n", 1)) {
            send(softKeySender, KeyEvent.KEYCODE_ENTER, 0);
        }
    }

    static void sendRawText(
            InputConnection inputConnection,
            String text,
            IntBinaryOperator softKeySender) {
        if (inputConnection == null || text == null || text.isEmpty()) {
            return;
        }
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '\n') {
                if (!sendCompleteSoftKey(softKeySender, KeyEvent.KEYCODE_ENTER, 0)) {
                    inputConnection.commitText("\n", 1);
                }
            } else if (codePoint >= 0x20 && codePoint <= 0x7E) {
                RemoteKeyStroke stroke = RemoteKeyStroke.forText(String.valueOf((char) codePoint));
                if (stroke == null) {
                    inputConnection.commitText(String.valueOf((char) codePoint), 1);
                } else if (!sendCompleteSoftKey(softKeySender, stroke.keyCode, stroke.metaState)) {
                    inputConnection.commitText(String.valueOf((char) codePoint), 1);
                }
            } else {
                inputConnection.commitText(new String(Character.toChars(codePoint)), 1);
            }
        }
    }

    static boolean moveCursor(
            InputConnection inputConnection,
            boolean right,
            IntBinaryOperator softKeySender) {
        if (inputConnection == null
                || InputConnectionTextOperator.isCursorAtBoundary(inputConnection, right)) {
            return false;
        }
        int keyCode = right ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
        return sendCompleteSoftKey(softKeySender, keyCode, 0);
    }

    static boolean performUndo(InputConnection inputConnection) {
        return inputConnection != null
                && inputConnection.performContextMenuAction(android.R.id.undo);
    }

    static int sendSoftKey(InputConnection inputConnection, int keyCode, int metaState) {
        if (inputConnection == null || keyCode == 0) {
            return 0;
        }
        return sendSoftKey(inputConnection, keyCode, metaState, RemoteKeyEventSequence.build(keyCode, metaState));
    }

    static int sendSoftKeyAt(
            InputConnection inputConnection,
            int keyCode,
            int metaState,
            long eventTimeMs) {
        if (inputConnection == null || keyCode == 0) {
            return 0;
        }
        return sendSoftKey(
                inputConnection,
                keyCode,
                metaState,
                RemoteKeyEventSequence.build(keyCode, metaState, eventTimeMs));
    }

    private static int sendSoftKey(
            InputConnection inputConnection,
            int keyCode,
            int metaState,
            Iterable<RemoteKeyEventSequence.EventSpec> events) {
        int sent = 0;
        for (RemoteKeyEventSequence.EventSpec event : events) {
            if (inputConnection.sendKeyEvent(event.toKeyEvent())) {
                sent++;
            }
        }
        return sent;
    }

    private static int send(IntBinaryOperator sender, int keyCode, int metaState) {
        if (sender != null) {
            return sender.applyAsInt(keyCode, metaState);
        }
        return 0;
    }

    private static boolean sendCompleteSoftKey(
            IntBinaryOperator sender,
            int keyCode,
            int metaState) {
        int expectedEvents = RemoteKeyEventSequence.eventCount(keyCode, metaState);
        return expectedEvents > 0 && send(sender, keyCode, metaState) >= expectedEvents;
    }

    private static void commitNewline(InputConnection inputConnection) {
        if (inputConnection != null) {
            inputConnection.commitText("\n", 1);
        }
    }
}
