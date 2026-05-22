package com.superl3.s3keyboard;

import android.view.KeyEvent;

final class RemoteKeyEventMap {
    private RemoteKeyEventMap() {
    }

    static int keyCodeFor(String command) {
        switch (command) {
            case KeyboardCommands.CMD_REMOTE_ESC:
                return KeyEvent.KEYCODE_ESCAPE;
            case KeyboardCommands.CMD_REMOTE_TAB:
                return KeyEvent.KEYCODE_TAB;
            case KeyboardCommands.CMD_REMOTE_ARROW_UP:
                return KeyEvent.KEYCODE_DPAD_UP;
            case KeyboardCommands.CMD_REMOTE_ARROW_DOWN:
                return KeyEvent.KEYCODE_DPAD_DOWN;
            case KeyboardCommands.CMD_REMOTE_ARROW_LEFT:
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case KeyboardCommands.CMD_REMOTE_ARROW_RIGHT:
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            case KeyboardCommands.CMD_REMOTE_INSERT:
                return KeyEvent.KEYCODE_INSERT;
            case KeyboardCommands.CMD_REMOTE_FORWARD_DELETE:
                return KeyEvent.KEYCODE_FORWARD_DEL;
            case KeyboardCommands.CMD_REMOTE_HOME:
                return KeyEvent.KEYCODE_MOVE_HOME;
            case KeyboardCommands.CMD_REMOTE_END:
                return KeyEvent.KEYCODE_MOVE_END;
            case KeyboardCommands.CMD_REMOTE_PAGE_UP:
                return KeyEvent.KEYCODE_PAGE_UP;
            case KeyboardCommands.CMD_REMOTE_PAGE_DOWN:
                return KeyEvent.KEYCODE_PAGE_DOWN;
            case KeyboardCommands.CMD_REMOTE_F1:
                return KeyEvent.KEYCODE_F1;
            case KeyboardCommands.CMD_REMOTE_F2:
                return KeyEvent.KEYCODE_F2;
            case KeyboardCommands.CMD_REMOTE_F3:
                return KeyEvent.KEYCODE_F3;
            case KeyboardCommands.CMD_REMOTE_F4:
                return KeyEvent.KEYCODE_F4;
            case KeyboardCommands.CMD_REMOTE_F5:
                return KeyEvent.KEYCODE_F5;
            case KeyboardCommands.CMD_REMOTE_F6:
                return KeyEvent.KEYCODE_F6;
            case KeyboardCommands.CMD_REMOTE_F7:
                return KeyEvent.KEYCODE_F7;
            case KeyboardCommands.CMD_REMOTE_F8:
                return KeyEvent.KEYCODE_F8;
            case KeyboardCommands.CMD_REMOTE_F9:
                return KeyEvent.KEYCODE_F9;
            case KeyboardCommands.CMD_REMOTE_F10:
                return KeyEvent.KEYCODE_F10;
            case KeyboardCommands.CMD_REMOTE_F11:
                return KeyEvent.KEYCODE_F11;
            case KeyboardCommands.CMD_REMOTE_F12:
                return KeyEvent.KEYCODE_F12;
            default:
                return 0;
        }
    }
}
