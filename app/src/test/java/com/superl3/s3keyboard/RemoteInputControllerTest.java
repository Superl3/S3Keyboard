package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class RemoteInputControllerTest {
    @Test
    public void tappedModifierAppliesToNextRemoteKeyThenClears() {
        FakeRemoteState state = new FakeRemoteState();
        RecordingKeySender sender = new RecordingKeySender();
        RemoteInputController controller = newController(state, sender);

        controller.handleCommand(null, KeyboardCommands.CMD_REMOTE_CTRL_LATCH);
        assertTrue((state.lastPendingMetaState & KeyEvent.META_CTRL_ON) != 0);

        controller.sendKey(null, KeyEvent.KEYCODE_A, 0);

        assertEquals(0, controller.pendingMetaState());
        assertEquals(0, state.lastPendingMetaState);
        assertEquals(KeyEvent.KEYCODE_A, sender.last().keyCode);
        assertTrue((sender.last().metaState & KeyEvent.META_CTRL_ON) != 0);
    }

    @Test
    public void lockedModifierPersistsAcrossRemoteKeys() {
        FakeRemoteState state = new FakeRemoteState();
        RecordingKeySender sender = new RecordingKeySender();
        RemoteInputController controller = newController(state, sender);

        controller.handleCommand(null, KeyboardCommands.CMD_REMOTE_ALT_LOCK);
        controller.sendKey(null, KeyEvent.KEYCODE_A, 0);
        controller.sendKey(null, KeyEvent.KEYCODE_B, 0);

        assertTrue((controller.lockedMetaState() & KeyEvent.META_ALT_ON) != 0);
        assertTrue((sender.sent.get(0).metaState & KeyEvent.META_ALT_ON) != 0);
        assertTrue((sender.sent.get(1).metaState & KeyEvent.META_ALT_ON) != 0);
    }

    @Test
    public void imeToggleUsesConfiguredShortcut() {
        FakeRemoteState state = new FakeRemoteState();
        state.shortcut = RemoteImeShortcut.WIN_SPACE;
        RecordingKeySender sender = new RecordingKeySender();
        RemoteInputController controller = newController(state, sender);

        controller.handleCommand(null, KeyboardCommands.CMD_REMOTE_IME_TOGGLE);

        assertEquals(KeyEvent.KEYCODE_SPACE, sender.last().keyCode);
        assertTrue((sender.last().metaState & KeyEvent.META_META_ON) != 0);
    }

    @Test
    public void compatibilityKeyClearsPreviousModifierState() {
        FakeRemoteState state = new FakeRemoteState();
        RecordingKeySender sender = new RecordingKeySender();
        RemoteInputController controller = newController(state, sender);

        controller.handleCommand(null, KeyboardCommands.CMD_REMOTE_CTRL_LATCH);
        controller.sendCompatibilityKey(null, KeyEvent.KEYCODE_F1, 0);

        assertEquals(0, controller.pendingMetaState());
        assertEquals(0, controller.lockedMetaState());
        assertEquals(KeyEvent.KEYCODE_F1, sender.last().keyCode);
        assertEquals(0, sender.last().metaState & KeyEvent.META_CTRL_ON);
    }

    private static final class FakeRemoteState {
        RemoteImeShortcut shortcut = RemoteImeShortcut.ALT_SHIFT;
        int lastPendingMetaState;
        int lastLockedMetaState;

        RemoteImeShortcut remoteImeShortcut() {
            return shortcut;
        }

        void onRemoteMetaStateChanged(int pendingMetaState, int lockedMetaState) {
            lastPendingMetaState = pendingMetaState;
            lastLockedMetaState = lockedMetaState;
        }
    }

    private static RemoteInputController newController(FakeRemoteState state, RecordingKeySender sender) {
        return new RemoteInputController(
                state::remoteImeShortcut,
                state::onRemoteMetaStateChanged,
                () -> 100L,
                sender);
    }

    private static final class RecordingKeySender implements RemoteInputController.KeySender {
        final List<SentKey> sent = new ArrayList<>();

        @Override
        public int send(android.view.inputmethod.InputConnection inputConnection, int keyCode, int metaState, long eventTimeMs) {
            sent.add(new SentKey(keyCode, metaState, eventTimeMs));
            return 1;
        }

        SentKey last() {
            return sent.get(sent.size() - 1);
        }
    }

    private static final class SentKey {
        final int keyCode;
        final int metaState;
        final long eventTimeMs;

        SentKey(int keyCode, int metaState, long eventTimeMs) {
            this.keyCode = keyCode;
            this.metaState = metaState;
            this.eventTimeMs = eventTimeMs;
        }
    }
}
