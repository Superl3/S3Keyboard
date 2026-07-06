package com.superl3.s3keyboard;

import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

final class RemoteInputController {
    interface KeySender {
        int send(InputConnection inputConnection, int keyCode, int metaState, long eventTimeMs);
    }

    private final RemoteKeySession session = new RemoteKeySession();
    private final Supplier<RemoteImeShortcut> remoteImeShortcut;
    private final BiConsumer<Integer, Integer> metaStateListener;
    private final LongSupplier clock;
    private final KeySender keySender;

    RemoteInputController(
            Supplier<RemoteImeShortcut> remoteImeShortcut,
            BiConsumer<Integer, Integer> metaStateListener) {
        this(remoteImeShortcut, metaStateListener, SystemClock::uptimeMillis);
    }

    RemoteInputController(
            Supplier<RemoteImeShortcut> remoteImeShortcut,
            BiConsumer<Integer, Integer> metaStateListener,
            LongSupplier clock) {
        this(remoteImeShortcut, metaStateListener, clock, ImeConnectionDispatcher::sendSoftKeyAt);
    }

    RemoteInputController(
            Supplier<RemoteImeShortcut> remoteImeShortcut,
            BiConsumer<Integer, Integer> metaStateListener,
            LongSupplier clock,
            KeySender keySender) {
        this.remoteImeShortcut = RuntimeDefaults.remoteImeShortcutSupplier(remoteImeShortcut);
        this.metaStateListener = RuntimeDefaults.integerPairConsumer(metaStateListener);
        this.clock = RuntimeDefaults.longSupplier(clock);
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
                clock.getAsLong());
    }

    int sendCompatibilityKey(InputConnection inputConnection, int keyCode, int metaState) {
        reset();
        return sendKey(inputConnection, keyCode, metaState);
    }

    private void sendImeToggle(InputConnection inputConnection) {
        reset();
        switch (RuntimeDefaults.remoteImeShortcut(remoteImeShortcut.get())) {
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

    private void notifyMetaStateChanged() {
        metaStateListener.accept(session.pendingMetaState(), session.lockedMetaState());
    }
}
