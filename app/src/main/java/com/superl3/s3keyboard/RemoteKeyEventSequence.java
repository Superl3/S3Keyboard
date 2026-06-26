package com.superl3.s3keyboard;

import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RemoteKeyEventSequence {
    static final int KEY_EVENT_FLAGS = KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE;
    static final int KEY_EVENT_DEVICE_ID = KeyCharacterMap.VIRTUAL_KEYBOARD;

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
        List<PressedModifier> pressedModifiers = new ArrayList<>();
        List<EventSpec> events = new ArrayList<>();
        long nextEventTimeMs = Math.max(0L, eventTimeMs);
        int activeMetaState = 0;
        for (Modifier modifier : modifiers) {
            activeMetaState |= modifier.metaState;
            events.add(event(
                    KeyEvent.ACTION_DOWN,
                    modifier.keyCode,
                    activeMetaState,
                    nextEventTimeMs,
                    nextEventTimeMs));
            pressedModifiers.add(new PressedModifier(modifier, nextEventTimeMs));
            nextEventTimeMs++;
        }
        int keyMetaState = normalizeMetaState(metaState);
        long keyDownTimeMs = nextEventTimeMs;
        events.add(event(KeyEvent.ACTION_DOWN, keyCode, keyMetaState, nextEventTimeMs, keyDownTimeMs));
        nextEventTimeMs++;
        events.add(event(KeyEvent.ACTION_UP, keyCode, keyMetaState, nextEventTimeMs, keyDownTimeMs));
        nextEventTimeMs++;
        for (int i = pressedModifiers.size() - 1; i >= 0; i--) {
            PressedModifier pressed = pressedModifiers.get(i);
            events.add(event(
                    KeyEvent.ACTION_UP,
                    pressed.modifier.keyCode,
                    activeMetaState,
                    nextEventTimeMs,
                    pressed.downTimeMs));
            activeMetaState &= ~pressed.modifier.metaState;
            nextEventTimeMs++;
        }
        return events;
    }

    static int eventCount(int keyCode, int metaState) {
        return build(keyCode, metaState, 0L).size();
    }

    private static EventSpec event(
            int action,
            int keyCode,
            int metaState,
            long eventTimeMs,
            long downTimeMs) {
        return new EventSpec(
                action,
                keyCode,
                metaState,
                Math.max(0L, eventTimeMs),
                Math.max(0L, downTimeMs));
    }

    static final class EventSpec {
        final int action;
        final int keyCode;
        final int metaState;
        final long eventTimeMs;
        final long downTimeMs;

        EventSpec(int action, int keyCode, int metaState, long eventTimeMs, long downTimeMs) {
            this.action = action;
            this.keyCode = keyCode;
            this.metaState = metaState;
            this.eventTimeMs = eventTimeMs;
            this.downTimeMs = downTimeMs;
        }

        KeyEvent toKeyEvent() {
            return new KeyEvent(
                    downTimeMs,
                    eventTimeMs,
                    action,
                    keyCode,
                    0,
                    metaState,
                    KEY_EVENT_DEVICE_ID,
                    0,
                    KEY_EVENT_FLAGS);
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

    private static final class PressedModifier {
        final Modifier modifier;
        final long downTimeMs;

        PressedModifier(Modifier modifier, long downTimeMs) {
            this.modifier = modifier;
            this.downTimeMs = downTimeMs;
        }
    }

}
