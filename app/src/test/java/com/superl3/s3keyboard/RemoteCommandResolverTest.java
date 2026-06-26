package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.KeyEvent;

import org.junit.Test;

public final class RemoteCommandResolverTest {
    @Test
    public void resolvesModifierTapAndLockCommands() {
        RemoteCommandAction ctrlTap = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_CTRL_LATCH);
        RemoteCommandAction altLock = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_ALT_LOCK);

        assertEquals(RemoteCommandAction.Type.META_TAP, ctrlTap.type);
        assertTrue((ctrlTap.metaState & KeyEvent.META_CTRL_ON) != 0);
        assertEquals(RemoteCommandAction.Type.META_LOCK, altLock.type);
        assertTrue((altLock.metaState & KeyEvent.META_ALT_ON) != 0);
    }

    @Test
    public void resolvesShortcutCommands() {
        RemoteCommandAction shiftTab = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_SHIFT_TAB);
        RemoteCommandAction ctrlEnter = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_CTRL_ENTER);

        assertEquals(RemoteCommandAction.Type.KEY, shiftTab.type);
        assertEquals(KeyEvent.KEYCODE_TAB, shiftTab.keyCode);
        assertTrue((shiftTab.metaState & KeyEvent.META_SHIFT_ON) != 0);
        assertEquals(KeyEvent.KEYCODE_ENTER, ctrlEnter.keyCode);
        assertTrue((ctrlEnter.metaState & KeyEvent.META_CTRL_ON) != 0);
    }

    @Test
    public void resolvesImeToggleAsDedicatedAction() {
        RemoteCommandAction action = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_IME_TOGGLE);

        assertEquals(RemoteCommandAction.Type.IME_TOGGLE, action.type);
    }

    @Test
    public void fallsBackToRemoteKeyMapForPlainRemoteKeys() {
        RemoteCommandAction esc = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_ESC);
        RemoteCommandAction f12 = RemoteCommandResolver.resolve(
                KeyboardCommands.CMD_REMOTE_F12);

        assertEquals(RemoteCommandAction.Type.KEY, esc.type);
        assertEquals(KeyEvent.KEYCODE_ESCAPE, esc.keyCode);
        assertEquals(RemoteCommandAction.Type.KEY, f12.type);
        assertEquals(KeyEvent.KEYCODE_F12, f12.keyCode);
    }

    @Test
    public void unknownOrNonRemoteCommandsResolveToNone() {
        assertEquals(
                RemoteCommandAction.Type.NONE,
                RemoteCommandResolver.resolve(KeyboardCommands.CMD_ENTER).type);
        assertEquals(RemoteCommandAction.Type.NONE, RemoteCommandResolver.resolve("__remote_missing__").type);
        assertEquals(RemoteCommandAction.Type.NONE, RemoteCommandResolver.resolve(null).type);
    }
}
