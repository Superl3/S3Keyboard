package com.superl3.s3keyboard;

final class KeyboardCommandRouter {
    private KeyboardCommandRouter() {
    }

    static KeyboardCommandRoute route(String value) {
        if (value == null || value.isEmpty()) {
            return KeyboardCommandRoute.NOOP;
        }
        switch (value) {
            case KeyboardCommands.CMD_NOOP:
                return KeyboardCommandRoute.NOOP;
            case KeyboardCommands.CMD_DELETE:
                return KeyboardCommandRoute.DELETE;
            case KeyboardCommands.CMD_DELETE_WORD:
                return KeyboardCommandRoute.DELETE_WORD;
            case KeyboardCommands.CMD_SPACE:
                return KeyboardCommandRoute.SPACE;
            case KeyboardCommands.CMD_ENTER:
                return KeyboardCommandRoute.ENTER;
            case KeyboardCommands.CMD_CORRECT_TEXT:
                return KeyboardCommandRoute.CORRECT_TEXT;
            case KeyboardCommands.CMD_NEWLINE:
                return KeyboardCommandRoute.NEWLINE;
            case KeyboardCommands.CMD_MOVE_LEFT:
                return KeyboardCommandRoute.MOVE_LEFT;
            case KeyboardCommands.CMD_MOVE_RIGHT:
                return KeyboardCommandRoute.MOVE_RIGHT;
            case KeyboardCommands.CMD_TOGGLE_LANGUAGE:
                return KeyboardCommandRoute.TOGGLE_LANGUAGE;
            case KeyboardCommands.CMD_SHIFT_ONCE:
                return KeyboardCommandRoute.SHIFT_ONCE;
            case KeyboardCommands.CMD_SHIFT_LOCK:
                return KeyboardCommandRoute.SHIFT_LOCK;
            case KeyboardCommands.CMD_RESERVED_PHRASES:
            case KeyboardCommands.CMD_RESERVED_LEFT:
            case KeyboardCommands.CMD_RESERVED_RIGHT:
            case KeyboardCommands.CMD_RESERVED_UP:
                return KeyboardCommandRoute.RESERVED_PHRASE;
            case KeyboardCommands.CMD_DINGUL_CENTER_VOWEL:
                return KeyboardCommandRoute.DINGUL_CENTER_VOWEL;
            case KeyboardCommands.CMD_DINGUL_WIDE_VOWEL:
                return KeyboardCommandRoute.DINGUL_WIDE_VOWEL;
            case KeyboardCommands.CMD_OPEN_OPTIONS:
                return KeyboardCommandRoute.OPEN_OPTIONS;
            case KeyboardCommands.CMD_QUICK_SETTINGS:
                return KeyboardCommandRoute.QUICK_SETTINGS;
            case KeyboardCommands.CMD_CLIPBOARD_PANEL:
                return KeyboardCommandRoute.CLIPBOARD_PANEL;
            case KeyboardCommands.CMD_VOICE_INPUT:
                return KeyboardCommandRoute.VOICE_INPUT;
            case KeyboardCommands.CMD_UNDO:
                return KeyboardCommandRoute.UNDO;
            case KeyboardCommands.CMD_TOOLS:
                return KeyboardCommandRoute.TOOLS;
            case KeyboardCommands.CMD_HAND_LEFT:
                return KeyboardCommandRoute.HAND_LEFT;
            case KeyboardCommands.CMD_HAND_RIGHT:
                return KeyboardCommandRoute.HAND_RIGHT;
            case KeyboardCommands.CMD_HAND_BALANCED:
                return KeyboardCommandRoute.HAND_BALANCED;
            case KeyboardCommands.CMD_INPUT_PICKER:
                return KeyboardCommandRoute.INPUT_PICKER;
            case KeyboardCommands.CMD_SETTINGS:
                return KeyboardCommandRoute.SETTINGS;
            case KeyboardCommands.CMD_HIDE:
                return KeyboardCommandRoute.HIDE;
            default:
                if (KeyboardCommands.isRemoteCommand(value)) {
                    return KeyboardCommandRoute.REMOTE;
                }
                return KeyboardCommands.isCommand(value)
                        ? KeyboardCommandRoute.NOOP
                        : KeyboardCommandRoute.TEXT;
        }
    }
}
