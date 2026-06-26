package com.superl3.s3keyboard;

import android.view.KeyEvent;

final class RemoteCommandResolver {
    private RemoteCommandResolver() {
    }

    static RemoteCommandAction resolve(String command) {
        if (command == null || !KeyboardCommands.isRemoteCommand(command)) {
            return RemoteCommandAction.NONE;
        }
        switch (command) {
            case KeyboardCommands.CMD_REMOTE_CTRL_LATCH:
                return RemoteCommandAction.metaTap(remoteCtrlMeta());
            case KeyboardCommands.CMD_REMOTE_WIN_LATCH:
                return RemoteCommandAction.metaTap(remoteWinMeta());
            case KeyboardCommands.CMD_REMOTE_ALT_LATCH:
                return RemoteCommandAction.metaTap(remoteAltMeta());
            case KeyboardCommands.CMD_REMOTE_CTRL_LOCK:
                return RemoteCommandAction.metaLock(remoteCtrlMeta());
            case KeyboardCommands.CMD_REMOTE_WIN_LOCK:
                return RemoteCommandAction.metaLock(remoteWinMeta());
            case KeyboardCommands.CMD_REMOTE_ALT_LOCK:
                return RemoteCommandAction.metaLock(remoteAltMeta());
            case KeyboardCommands.CMD_REMOTE_SHIFT_TAB:
                return RemoteCommandAction.key(KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON);
            case KeyboardCommands.CMD_REMOTE_CTRL_TAB:
                return RemoteCommandAction.key(KeyEvent.KEYCODE_TAB, KeyEvent.META_CTRL_ON);
            case KeyboardCommands.CMD_REMOTE_ALT_TAB:
                return RemoteCommandAction.key(KeyEvent.KEYCODE_TAB, KeyEvent.META_ALT_ON);
            case KeyboardCommands.CMD_REMOTE_CTRL_ENTER:
                return RemoteCommandAction.key(KeyEvent.KEYCODE_ENTER, KeyEvent.META_CTRL_ON);
            case KeyboardCommands.CMD_REMOTE_IME_TOGGLE:
                return RemoteCommandAction.imeToggle();
            default:
                return RemoteCommandAction.key(RemoteKeyEventMap.keyCodeFor(command), 0);
        }
    }

    static int remoteCtrlMeta() {
        return KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
    }

    static int remoteWinMeta() {
        return KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON;
    }

    static int remoteAltMeta() {
        return KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
    }
}
