package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import java.util.List;

import org.junit.Test;

public final class RemoteKeyEventSequenceTest {
    @Test
    public void ctrlChordSendsExplicitModifierDownAndUp() {
        List<RemoteKeyEventSequence.EventSpec> events = RemoteKeyEventSequence.build(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON,
                100L);

        assertEquals(4, events.size());
        assertEvent(events.get(0), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT);
        assertEvent(events.get(1), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A);
        assertEvent(events.get(2), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A);
        assertEvent(events.get(3), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT);
        assertEquals(100L, events.get(0).eventTimeMs);
        assertEquals(101L, events.get(1).eventTimeMs);
        assertEquals(102L, events.get(2).eventTimeMs);
        assertEquals(103L, events.get(3).eventTimeMs);
        assertEquals(events.get(1).downTimeMs, events.get(2).downTimeMs);
        assertEquals(events.get(0).downTimeMs, events.get(3).downTimeMs);
        assertTrue((events.get(1).metaState & KeyEvent.META_CTRL_ON) != 0);
    }

    @Test
    public void winShiftChordUsesBothModifierKeysAroundMainKey() {
        List<RemoteKeyEventSequence.EventSpec> events = RemoteKeyEventSequence.build(
                KeyEvent.KEYCODE_SPACE,
                KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON
                        | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON,
                100L);

        assertEquals(6, events.size());
        assertEvent(events.get(0), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_META_LEFT);
        assertEvent(events.get(1), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT);
        assertEvent(events.get(2), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE);
        assertEvent(events.get(3), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE);
        assertEvent(events.get(4), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
        assertEvent(events.get(5), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_META_LEFT);
    }

    @Test
    public void fourModifierChordReleasesEveryModifierInReverseOrder() {
        int allModifiers = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON
                | KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON
                | KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
                | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;

        List<RemoteKeyEventSequence.EventSpec> events = RemoteKeyEventSequence.build(
                KeyEvent.KEYCODE_A,
                allModifiers,
                400L);

        assertEquals(10, events.size());
        assertEvent(events.get(0), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT);
        assertEvent(events.get(1), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_META_LEFT);
        assertEvent(events.get(2), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT);
        assertEvent(events.get(3), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT);
        assertEvent(events.get(4), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A);
        assertEvent(events.get(5), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A);
        assertEvent(events.get(6), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT);
        assertEvent(events.get(7), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT);
        assertEvent(events.get(8), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_META_LEFT);
        assertEvent(events.get(9), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT);
        assertEquals(events.get(0).downTimeMs, events.get(9).downTimeMs);
        assertEquals(events.get(3).downTimeMs, events.get(6).downTimeMs);
        assertTrue((events.get(4).metaState & allModifiers) == allModifiers);
    }

    @Test
    public void keyEventsUseSoftKeyboardFlags() {
        assertTrue((RemoteKeyEventSequence.KEY_EVENT_FLAGS & KeyEvent.FLAG_SOFT_KEYBOARD) != 0);
        assertTrue((RemoteKeyEventSequence.KEY_EVENT_FLAGS & KeyEvent.FLAG_KEEP_TOUCH_MODE) != 0);
    }

    @Test
    public void eventSpecCarriesConventionalVirtualKeyboardFields() {
        RemoteKeyEventSequence.EventSpec spec = RemoteKeyEventSequence.build(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON,
                200L).get(1);

        assertEquals(KeyEvent.ACTION_DOWN, spec.action);
        assertEquals(KeyEvent.KEYCODE_A, spec.keyCode);
        assertEquals(201L, spec.eventTimeMs);
        assertEquals(201L, spec.downTimeMs);
        assertTrue((spec.metaState & KeyEvent.META_SHIFT_ON) != 0);
        assertTrue((spec.metaState & KeyEvent.META_SHIFT_LEFT_ON) != 0);
    }

    @Test
    public void eventSpecDefinesSoftVirtualKeyboardKeyEventFields() {
        RemoteKeyEventSequence.EventSpec spec = RemoteKeyEventSequence.build(
                KeyEvent.KEYCODE_F1,
                0,
                300L).get(0);

        assertEquals(KeyEvent.ACTION_DOWN, spec.action);
        assertEquals(KeyEvent.KEYCODE_F1, spec.keyCode);
        assertEquals(300L, spec.eventTimeMs);
        assertEquals(300L, spec.downTimeMs);
        assertEquals(android.view.KeyCharacterMap.VIRTUAL_KEYBOARD, RemoteKeyEventSequence.KEY_EVENT_DEVICE_ID);
        assertTrue((RemoteKeyEventSequence.KEY_EVENT_FLAGS & KeyEvent.FLAG_SOFT_KEYBOARD) != 0);
        assertTrue((RemoteKeyEventSequence.KEY_EVENT_FLAGS & KeyEvent.FLAG_KEEP_TOUCH_MODE) != 0);
    }

    @Test
    public void eventCountMatchesGeneratedSequenceLength() {
        assertEquals(2, RemoteKeyEventSequence.eventCount(KeyEvent.KEYCODE_ENTER, 0));
        assertEquals(4, RemoteKeyEventSequence.eventCount(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON));
        assertEquals(6, RemoteKeyEventSequence.eventCount(
                KeyEvent.KEYCODE_SPACE,
                KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON
                        | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON));
        assertEquals(0, RemoteKeyEventSequence.eventCount(0, 0));
    }

    private static void assertEvent(RemoteKeyEventSequence.EventSpec event, int action, int keyCode) {
        assertEquals(action, event.action);
        assertEquals(keyCode, event.keyCode);
    }
}
