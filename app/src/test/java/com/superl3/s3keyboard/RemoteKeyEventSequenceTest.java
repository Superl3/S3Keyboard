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

    private static void assertEvent(RemoteKeyEventSequence.EventSpec event, int action, int keyCode) {
        assertEquals(action, event.action);
        assertEquals(keyCode, event.keyCode);
    }
}
