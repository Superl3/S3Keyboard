package com.superl3.s3keyboard;

import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RemoteKeyEventSequence {
    private RemoteKeyEventSequence() {
    }

    static List<EventSpec> build(int keyCode, int metaState) {
        return build(keyCode, metaState, SystemClock.uptimeMillis());
    }

    static List<EventSpec> build(int keyCode, int metaState, long eventTimeMs) {
        if (keyCode == 0) {
            return Collections.emptyList();
        }
        List<Modifier> modifiers = modifiersFor(metaState);
        List<EventSpec> events = new ArrayList<>();
        int activeMetaState = 0;
        for (Modifier modifier : modifiers) {
            activeMetaState |= modifier.metaState;
            events.add(event(KeyEvent.ACTION_DOWN, modifier.keyCode, activeMetaState, eventTimeMs));
        }
        int keyMetaState = normalizeMetaState(metaState);
        events.add(event(KeyEvent.ACTION_DOWN, keyCode, keyMetaState, eventTimeMs));
        events.add(event(KeyEvent.ACTION_UP, keyCode, keyMetaState, eventTimeMs));
        for (int i = modifiers.size() - 1; i >= 0; i--) {
            Modifier modifier = modifiers.get(i);
            events.add(event(KeyEvent.ACTION_UP, modifier.keyCode, activeMetaState, eventTimeMs));
            activeMetaState &= ~modifier.metaState;
        }
        return events;
    }

    private static EventSpec event(int action, int keyCode, int metaState, long eventTimeMs) {
        return new EventSpec(action, keyCode, metaState, Math.max(0L, eventTimeMs));
    }

    static final class EventSpec {
        final int action;
        final int keyCode;
        final int metaState;
        final long eventTimeMs;

        EventSpec(int action, int keyCode, int metaState, long eventTimeMs) {
            this.action = action;
            this.keyCode = keyCode;
            this.metaState = metaState;
            this.eventTimeMs = eventTimeMs;
        }

        KeyEvent toKeyEvent() {
            return toKeyEvent(eventTimeMs);
        }

        KeyEvent toKeyEvent(long downTimeMs) {
            long safeTime = Math.max(0L, eventTimeMs);
            return new KeyEvent(
                    Math.max(0L, downTimeMs),
                    safeTime,
                    action,
                    keyCode,
                    0,
                    metaState,
                    KeyCharacterMap.VIRTUAL_KEYBOARD,
                    0,
                    KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE);
        }
    }

    private static List<Modifier> modifiersFor(int metaState) {
        List<Modifier> modifiers = new ArrayList<>();
        if ((metaState & KeyEvent.META_CTRL_ON) != 0 || (metaState & KeyEvent.META_CTRL_LEFT_ON) != 0) {
            modifiers.add(new Modifier(
                    KeyEvent.KEYCODE_CTRL_LEFT,
                    KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON));
        }
        if ((metaState & KeyEvent.META_META_ON) != 0 || (metaState & KeyEvent.META_META_LEFT_ON) != 0) {
            modifiers.add(new Modifier(
                    KeyEvent.KEYCODE_META_LEFT,
                    KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON));
        }
        if ((metaState & KeyEvent.META_ALT_ON) != 0 || (metaState & KeyEvent.META_ALT_LEFT_ON) != 0) {
            modifiers.add(new Modifier(
                    KeyEvent.KEYCODE_ALT_LEFT,
                    KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON));
        }
        if ((metaState & KeyEvent.META_SHIFT_ON) != 0 || (metaState & KeyEvent.META_SHIFT_LEFT_ON) != 0) {
            modifiers.add(new Modifier(
                    KeyEvent.KEYCODE_SHIFT_LEFT,
                    KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON));
        }
        return modifiers;
    }

    private static int normalizeMetaState(int metaState) {
        int normalized = metaState;
        if ((metaState & (KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON)) != 0) {
            normalized |= KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        }
        if ((metaState & (KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON)) != 0) {
            normalized |= KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON;
        }
        if ((metaState & (KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON)) != 0) {
            normalized |= KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
        }
        if ((metaState & (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)) != 0) {
            normalized |= KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
        }
        return normalized;
    }

    private static final class Modifier {
        final int keyCode;
        final int metaState;

        Modifier(int keyCode, int metaState) {
            this.keyCode = keyCode;
            this.metaState = metaState;
        }
    }

}
