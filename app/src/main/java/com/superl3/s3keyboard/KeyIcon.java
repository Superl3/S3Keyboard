package com.superl3.s3keyboard;

final class KeyIcon {
    static final int NONE = 0;
    static final int OPTIONS = 1;
    static final int RESERVED = 2;
    static final int SPACE = 3;
    static final int LANGUAGE = 4;
    static final int ENTER = 5;
    static final int SEARCH = 6;
    static final int DONE = 7;
    static final int NEXT = 8;
    static final int SHIFT = 9;
    static final int CAPS_LOCK = 10;
    static final int BACKSPACE = 11;
    static final int HIDE = 12;
    static final int SETTINGS = 13;
    static final int MOVE_LEFT = 14;
    static final int MOVE_RIGHT = 15;
    static final int KEYBOARD = 16;
    static final int RESET = 17;

    private KeyIcon() {
    }

    static int forCommand(String command) {
        return forCommand(command, null);
    }

    static int forCommand(String command, String label) {
        if (KeyboardCommands.CMD_OPEN_OPTIONS.equals(command)) {
            return OPTIONS;
        }
        if (KeyboardCommands.CMD_RESERVED_PHRASES.equals(command)) {
            return RESERVED;
        }
        if (KeyboardCommands.CMD_SPACE.equals(command)) {
            return SPACE;
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(command)) {
            return LANGUAGE;
        }
        if (KeyboardCommands.CMD_ENTER.equals(command)) {
            return enterForLabel(label);
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(command)) {
            return SHIFT;
        }
        if (KeyboardCommands.CMD_SHIFT_LOCK.equals(command)) {
            return CAPS_LOCK;
        }
        if (KeyboardCommands.CMD_DELETE.equals(command)) {
            return BACKSPACE;
        }
        if (KeyboardCommands.CMD_HIDE.equals(command)) {
            return HIDE;
        }
        if (KeyboardCommands.CMD_SETTINGS.equals(command)) {
            return SETTINGS;
        }
        if (KeyboardCommands.CMD_INPUT_PICKER.equals(command)) {
            return KEYBOARD;
        }
        if (KeyboardCommands.CMD_MOVE_LEFT.equals(command)) {
            return MOVE_LEFT;
        }
        if (KeyboardCommands.CMD_MOVE_RIGHT.equals(command)) {
            return MOVE_RIGHT;
        }
        return NONE;
    }

    static int enterForLabel(String label) {
        if ("검색".equals(label)) {
            return SEARCH;
        }
        if ("완료".equals(label)) {
            return DONE;
        }
        if ("다음".equals(label)) {
            return NEXT;
        }
        return ENTER;
    }
}
