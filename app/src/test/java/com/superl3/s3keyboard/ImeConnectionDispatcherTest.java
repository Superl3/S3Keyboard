package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class ImeConnectionDispatcherTest {
    @Test
    public void acceptedEditorActionDoesNotFallBackToEnterKey() {
        FakeConnection fake = new FakeConnection();
        fake.performEditorActionResult = true;
        RecordingKeySender softSender = new RecordingKeySender();
        RecordingKeySender remoteSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                softSender,
                remoteSender);

        assertEquals(1, fake.editorActions.size());
        assertEquals(EditorInfo.IME_ACTION_SEARCH, (int) fake.editorActions.get(0));
        assertEquals(0, softSender.sent.size());
        assertEquals(0, remoteSender.sent.size());
    }

    @Test
    public void rejectedEditorActionFallsBackToSoftEnterKey() {
        FakeConnection fake = new FakeConnection();
        fake.performEditorActionResult = false;
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                softSender,
                new RecordingKeySender());

        assertEquals(1, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_ENTER, softSender.sent.get(0).keyCode);
        assertEquals("performEditorAction", fake.calls.get(0));
    }

    @Test
    public void rejectedEditorActionPerformsActionBeforeFallbackKey() {
        FakeConnection fake = new FakeConnection();
        fake.performEditorActionResult = false;

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                (keyCode, metaState) -> {
                    fake.calls.add("softKeyFallback");
                    return RemoteKeyEventSequence.eventCount(keyCode, metaState);
                },
                new RecordingKeySender());

        assertEquals("performEditorAction", fake.calls.get(0));
        assertEquals("softKeyFallback", fake.calls.get(1));
    }

    @Test
    public void rejectedEditorActionCommitsNewlineWhenSoftEnterIsRejected() {
        FakeConnection fake = new FakeConnection();
        fake.performEditorActionResult = false;

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                (keyCode, metaState) -> 0,
                new RecordingKeySender());

        assertEquals("performEditorAction", fake.calls.get(0));
        assertEquals("commitText", fake.calls.get(1));
        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
    }

    @Test
    public void rejectedEditorActionCommitsNewlineWhenSoftEnterIsPartiallyAccepted() {
        FakeConnection fake = new FakeConnection();
        fake.performEditorActionResult = false;

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_search, EditorInfo.IME_ACTION_SEARCH, true),
                false,
                false,
                (keyCode, metaState) -> 1,
                new RecordingKeySender());

        assertEquals("performEditorAction", fake.calls.get(0));
        assertEquals("commitText", fake.calls.get(1));
        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
    }

    @Test
    public void multilineEnterCommitsNewlineBeforeSoftKeyFallback() {
        FakeConnection fake = new FakeConnection();
        fake.commitTextResult = true;
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_newline, EditorInfo.IME_ACTION_NONE, false),
                false,
                false,
                softSender,
                new RecordingKeySender());

        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
        assertEquals(0, softSender.sent.size());
    }

    @Test
    public void failedNewlineCommitFallsBackToSoftEnterKey() {
        FakeConnection fake = new FakeConnection();
        fake.commitTextResult = false;
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_newline, EditorInfo.IME_ACTION_NONE, false),
                false,
                false,
                softSender,
                new RecordingKeySender());

        assertEquals(1, fake.committedText.size());
        assertEquals(1, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_ENTER, softSender.sent.get(0).keyCode);
        assertEquals("commitText", fake.calls.get(0));
    }

    @Test
    public void failedNewlineCommitFallsBackAfterCommitAttempt() {
        FakeConnection fake = new FakeConnection();
        fake.commitTextResult = false;

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_newline, EditorInfo.IME_ACTION_NONE, false),
                false,
                false,
                (keyCode, metaState) -> {
                    fake.calls.add("softKeyFallback");
                    return RemoteKeyEventSequence.eventCount(keyCode, metaState);
                },
                new RecordingKeySender());

        assertEquals("commitText", fake.calls.get(0));
        assertEquals("softKeyFallback", fake.calls.get(1));
    }

    @Test
    public void remoteEnterUsesRemoteSenderOnly() {
        FakeConnection fake = new FakeConnection();
        RecordingKeySender softSender = new RecordingKeySender();
        RecordingKeySender remoteSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true),
                false,
                true,
                softSender,
                remoteSender);

        assertEquals(0, softSender.sent.size());
        assertEquals(1, remoteSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_ENTER, remoteSender.sent.get(0).keyCode);
        assertEquals(0, fake.editorActions.size());
    }

    @Test
    public void rawKeyInputEnterUsesSoftKeyOnly() {
        FakeConnection fake = new FakeConnection();
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true),
                true,
                false,
                softSender,
                new RecordingKeySender());

        assertEquals(1, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_ENTER, softSender.sent.get(0).keyCode);
        assertEquals(0, fake.editorActions.size());
        assertEquals(0, fake.committedText.size());
    }

    @Test
    public void rawKeyInputEnterCommitsNewlineWhenSoftSenderIsMissing() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true),
                true,
                false,
                null,
                new RecordingKeySender());

        assertEquals(0, fake.editorActions.size());
        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
    }

    @Test
    public void rawKeyInputEnterCommitsNewlineWhenSoftSenderPartiallyAccepts() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true),
                true,
                false,
                (keyCode, metaState) -> 1,
                new RecordingKeySender());

        assertEquals(0, fake.editorActions.size());
        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
    }

    @Test
    public void rawKeyInputEnterCommitsNewlineWhenSoftSenderRejects() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.performEnter(
                fake.connection(),
                new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true),
                true,
                false,
                (keyCode, metaState) -> 0,
                new RecordingKeySender());

        assertEquals(0, fake.editorActions.size());
        assertEquals(1, fake.committedText.size());
        assertEquals("\n", fake.committedText.get(0));
    }

    @Test
    public void rawTextUsesKeyEventsForAsciiAndCommitsNonAscii() {
        FakeConnection fake = new FakeConnection();
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.sendRawText(fake.connection(), "A\n한", softSender);

        assertEquals(2, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_A, softSender.sent.get(0).keyCode);
        assertTrue((softSender.sent.get(0).metaState & KeyEvent.META_SHIFT_ON) != 0);
        assertEquals(KeyEvent.KEYCODE_ENTER, softSender.sent.get(1).keyCode);
        assertEquals(1, fake.committedText.size());
        assertEquals("한", fake.committedText.get(0));
    }

    @Test
    public void rawTextKeepsAsciiKeyEventsBeforeNonAsciiCommit() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.sendRawText(
                fake.connection(),
                "a한",
                (keyCode, metaState) -> {
                    fake.calls.add("softKey:" + keyCode);
                    return RemoteKeyEventSequence.eventCount(keyCode, metaState);
                });

        assertEquals("softKey:" + KeyEvent.KEYCODE_A, fake.calls.get(0));
        assertEquals("commitText", fake.calls.get(1));
    }

    @Test
    public void rawTextFallsBackToCommitWhenSoftSenderIsMissing() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.sendRawText(fake.connection(), "A\n", null);

        assertEquals(2, fake.committedText.size());
        assertEquals("A", fake.committedText.get(0));
        assertEquals("\n", fake.committedText.get(1));
        assertEquals(0, fake.keyEventCount);
    }

    @Test
    public void rawTextFallsBackToCommitWhenSoftSenderRejectsKey() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.sendRawText(
                fake.connection(),
                "a.",
                (keyCode, metaState) -> 0);

        assertEquals(2, fake.committedText.size());
        assertEquals("a", fake.committedText.get(0));
        assertEquals(".", fake.committedText.get(1));
    }

    @Test
    public void rawTextFallsBackToCommitWhenSoftSenderPartiallyAcceptsAsciiKey() {
        FakeConnection fake = new FakeConnection();

        ImeConnectionDispatcher.sendRawText(
                fake.connection(),
                "a\n",
                (keyCode, metaState) -> 1);

        assertEquals(2, fake.committedText.size());
        assertEquals("a", fake.committedText.get(0));
        assertEquals("\n", fake.committedText.get(1));
    }

    @Test
    public void rawTextDoesNotCommitAsciiWhenSoftSenderAcceptsKey() {
        FakeConnection fake = new FakeConnection();
        RecordingKeySender softSender = new RecordingKeySender();

        ImeConnectionDispatcher.sendRawText(fake.connection(), "a.", softSender);

        assertEquals(2, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_A, softSender.sent.get(0).keyCode);
        assertEquals(KeyEvent.KEYCODE_PERIOD, softSender.sent.get(1).keyCode);
        assertEquals(0, fake.committedText.size());
    }

    @Test
    public void moveCursorLeftUsesSoftDpadWhenTextExistsBeforeCursor() {
        FakeConnection fake = new FakeConnection();
        fake.textBeforeCursor = "x";
        RecordingKeySender softSender = new RecordingKeySender();

        boolean moved = ImeConnectionDispatcher.moveCursor(fake.connection(), false, softSender);

        assertTrue(moved);
        assertEquals(1, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_DPAD_LEFT, softSender.sent.get(0).keyCode);
        assertEquals(0, fake.committedText.size());
    }

    @Test
    public void moveCursorRightUsesSoftDpadWhenTextExistsAfterCursor() {
        FakeConnection fake = new FakeConnection();
        fake.textAfterCursor = "x";
        RecordingKeySender softSender = new RecordingKeySender();

        boolean moved = ImeConnectionDispatcher.moveCursor(fake.connection(), true, softSender);

        assertTrue(moved);
        assertEquals(1, softSender.sent.size());
        assertEquals(KeyEvent.KEYCODE_DPAD_RIGHT, softSender.sent.get(0).keyCode);
    }

    @Test
    public void moveCursorDoesNotSendKeyAtBoundary() {
        FakeConnection fake = new FakeConnection();
        RecordingKeySender softSender = new RecordingKeySender();

        boolean movedLeft = ImeConnectionDispatcher.moveCursor(fake.connection(), false, softSender);
        boolean movedRight = ImeConnectionDispatcher.moveCursor(fake.connection(), true, softSender);

        assertEquals(false, movedLeft);
        assertEquals(false, movedRight);
        assertEquals(0, softSender.sent.size());
    }

    @Test
    public void moveCursorReportsFalseWhenSoftSenderPartiallyAcceptsDpad() {
        FakeConnection fake = new FakeConnection();
        fake.textBeforeCursor = "x";

        boolean moved = ImeConnectionDispatcher.moveCursor(
                fake.connection(),
                false,
                (keyCode, metaState) -> 1);

        assertEquals(false, moved);
    }

    @Test
    public void performUndoUsesConventionalContextMenuAction() {
        FakeConnection fake = new FakeConnection();
        fake.performContextMenuActionResult = true;

        boolean handled = ImeConnectionDispatcher.performUndo(fake.connection());

        assertTrue(handled);
        assertEquals(1, fake.contextMenuActions.size());
        assertEquals(android.R.id.undo, (int) fake.contextMenuActions.get(0));
        assertEquals("performContextMenuAction", fake.calls.get(0));
    }

    @Test
    public void performUndoReportsFalseWhenEditorRejectsUndo() {
        FakeConnection fake = new FakeConnection();
        fake.performContextMenuActionResult = false;

        boolean handled = ImeConnectionDispatcher.performUndo(fake.connection());

        assertEquals(false, handled);
        assertEquals(1, fake.contextMenuActions.size());
        assertEquals(android.R.id.undo, (int) fake.contextMenuActions.get(0));
        assertEquals(0, fake.committedText.size());
        assertEquals(0, fake.keyEventCount);
    }

    @Test
    public void softKeySenderReturnsGeneratedEventCount() {
        FakeConnection fake = new FakeConnection();

        int count = ImeConnectionDispatcher.sendSoftKeyAt(
                fake.connection(),
                KeyEvent.KEYCODE_A,
                KeyEvent.META_SHIFT_ON,
                100L);

        assertEquals(4, count);
        assertEquals(4, fake.keyEventCount);
    }

    @Test
    public void softKeySenderCountsOnlyAcceptedEvents() {
        FakeConnection fake = new FakeConnection();
        fake.sendKeyEventResult = false;

        int count = ImeConnectionDispatcher.sendSoftKeyAt(
                fake.connection(),
                KeyEvent.KEYCODE_F1,
                0,
                200L);

        assertEquals(0, count);
        assertEquals(2, fake.keyEventCount);
    }

    private static final class RecordingKeySender implements ImeConnectionDispatcher.KeySender {
        final List<SentKey> sent = new ArrayList<>();

        @Override
        public int send(int keyCode, int metaState) {
            sent.add(new SentKey(keyCode, metaState));
            return RemoteKeyEventSequence.eventCount(keyCode, metaState);
        }
    }

    private static final class SentKey {
        final int keyCode;
        final int metaState;

        SentKey(int keyCode, int metaState) {
            this.keyCode = keyCode;
            this.metaState = metaState;
        }
    }

    private static final class FakeConnection implements InvocationHandler {
        boolean performEditorActionResult;
        boolean commitTextResult = true;
        boolean sendKeyEventResult = true;
        boolean performContextMenuActionResult;
        String textBeforeCursor = "";
        String textAfterCursor = "";
        int keyEventCount;
        final List<Integer> editorActions = new ArrayList<>();
        final List<Integer> contextMenuActions = new ArrayList<>();
        final List<String> committedText = new ArrayList<>();
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
                case "performEditorAction":
                    calls.add("performEditorAction");
                    editorActions.add((Integer) args[0]);
                    return performEditorActionResult;
                case "performContextMenuAction":
                    calls.add("performContextMenuAction");
                    contextMenuActions.add((Integer) args[0]);
                    return performContextMenuActionResult;
                case "commitText":
                    calls.add("commitText");
                    committedText.add(String.valueOf(args[0]));
                    return commitTextResult;
                case "sendKeyEvent":
                    calls.add("sendKeyEvent");
                    keyEventCount++;
                    return sendKeyEventResult;
                case "getTextBeforeCursor":
                    calls.add("getTextBeforeCursor");
                    return textBeforeCursor;
                case "getTextAfterCursor":
                    calls.add("getTextAfterCursor");
                    return textAfterCursor;
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
