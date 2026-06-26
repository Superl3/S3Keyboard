package com.superl3.s3keyboard;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

final class RemoteInputController {
    interface Host {
        RemoteImeShortcut remoteImeShortcut();

        void onRemoteMetaStateChanged(int pendingMetaState, int lockedMetaState);
    }

    interface Clock {
        long eventTimeMs();
    }

    interface KeySender {
        int send(InputConnection inputConnection, int keyCode, int metaState, long eventTimeMs);
    }

    private final RemoteKeySession session = new RemoteKeySession();
    private final Host host;
    private final Clock clock;
    private final KeySender keySender;

    RemoteInputController(Host host) {
        this(host, SystemClock::uptimeMillis);
    }

    RemoteInputController(Host host, Clock clock) {
        this(host, clock, ImeConnectionDispatcher::sendSoftKeyAt);
    }

    RemoteInputController(Host host, Clock clock, KeySender keySender) {
        this.host = host;
        this.clock = clock == null ? SystemClock::uptimeMillis : clock;
        this.keySender = keySender == null ? ImeConnectionDispatcher::sendSoftKeyAt : keySender;
    }

    int pendingMetaState() {
        return session.pendingMetaState();
    }

    int lockedMetaState() {
        return session.lockedMetaState();
    }

    void reset() {
        session.reset();
        notifyMetaStateChanged();
    }

    void handleCommand(InputConnection inputConnection, String command) {
        RemoteCommandAction action = RemoteCommandResolver.resolve(command);
        switch (action.type) {
            case META_TAP:
                session.tapModifier(action.metaState);
                notifyMetaStateChanged();
                return;
            case META_LOCK:
                session.toggleLockedModifier(action.metaState);
                notifyMetaStateChanged();
                return;
            case KEY:
                sendKey(inputConnection, action.keyCode, action.metaState);
                return;
            case IME_TOGGLE:
                sendImeToggle(inputConnection);
                return;
            case NONE:
            default:
        }
    }

    int sendKey(InputConnection inputConnection, int keyCode, int metaState) {
        int combinedMetaState = session.consumeForKey(metaState);
        notifyMetaStateChanged();
        return keySender.send(
                inputConnection,
                keyCode,
                combinedMetaState,
                clock.eventTimeMs());
    }

    int sendCompatibilityKey(InputConnection inputConnection, int keyCode, int metaState) {
        reset();
        return sendKey(inputConnection, keyCode, metaState);
    }

    private void sendImeToggle(InputConnection inputConnection) {
        reset();
        switch (remoteImeShortcut()) {
            case CTRL_SPACE:
                sendKey(inputConnection, KeyEvent.KEYCODE_SPACE, KeyEvent.META_CTRL_ON);
                return;
            case WIN_SPACE:
                sendKey(inputConnection, KeyEvent.KEYCODE_SPACE, KeyEvent.META_META_ON);
                return;
            case LANGUAGE_SWITCH:
                sendKey(inputConnection, KeyEvent.KEYCODE_LANGUAGE_SWITCH, 0);
                return;
            case ALT_SHIFT:
            default:
                sendKey(
                        inputConnection,
                        KeyEvent.KEYCODE_SHIFT_LEFT,
                        KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON);
        }
    }

    private RemoteImeShortcut remoteImeShortcut() {
        RemoteImeShortcut shortcut = host == null ? null : host.remoteImeShortcut();
        return shortcut == null ? RemoteImeShortcut.ALT_SHIFT : shortcut;
    }

    private void notifyMetaStateChanged() {
        if (host != null) {
            host.onRemoteMetaStateChanged(session.pendingMetaState(), session.lockedMetaState());
        }
    }
}
