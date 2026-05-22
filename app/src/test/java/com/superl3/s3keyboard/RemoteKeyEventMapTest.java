package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import android.view.KeyEvent;

import org.junit.Test;

public final class RemoteKeyEventMapTest {
    @Test
    public void mapsRemoteCommandsToAndroidKeyCodes() {
        assertEquals(KeyEvent.KEYCODE_ESCAPE, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_ESC));
        assertEquals(KeyEvent.KEYCODE_TAB, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_TAB));
        assertEquals(KeyEvent.KEYCODE_DPAD_UP, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_ARROW_UP));
        assertEquals(KeyEvent.KEYCODE_DPAD_DOWN, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_ARROW_DOWN));
        assertEquals(KeyEvent.KEYCODE_DPAD_LEFT, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_ARROW_LEFT));
        assertEquals(KeyEvent.KEYCODE_DPAD_RIGHT, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_ARROW_RIGHT));
        assertEquals(KeyEvent.KEYCODE_INSERT, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_INSERT));
        assertEquals(
                KeyEvent.KEYCODE_FORWARD_DEL,
                RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_FORWARD_DELETE));
        assertEquals(KeyEvent.KEYCODE_MOVE_HOME, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_HOME));
        assertEquals(KeyEvent.KEYCODE_PAGE_UP, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_PAGE_UP));
        assertEquals(KeyEvent.KEYCODE_F1, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_F1));
        assertEquals(KeyEvent.KEYCODE_F12, RemoteKeyEventMap.keyCodeFor(KeyboardCommands.CMD_REMOTE_F12));
    }

    @Test
    public void unknownRemoteCommandMapsToZero() {
        assertEquals(0, RemoteKeyEventMap.keyCodeFor("__remote_unknown__"));
    }
}
