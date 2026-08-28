package com.superl3.s3keyboard;

import android.content.Context;

final class KeyboardCommandLabels {
    private KeyboardCommandLabels() {
    }

    static String labelFor(Context context, String value) {
        if (value == null || value.isEmpty() || KeyboardCommands.CMD_NOOP.equals(value)) {
            return null;
        }
        int resId = labelResIdFor(value);
        if (resId != 0) {
            return context == null ? null : context.getString(resId);
        }
        return KeyboardCommands.isCommand(value) ? null : value;
    }

    static int labelResIdFor(String value) {
        if (value == null) {
            return 0;
        }
        switch (value) {
            case KeyboardCommands.CMD_DELETE:
                return R.string.command_delete;
            case KeyboardCommands.CMD_SPACE:
                return R.string.command_space;
            case KeyboardCommands.CMD_ENTER:
                return R.string.command_enter;
            case KeyboardCommands.CMD_CORRECT_TEXT:
                return R.string.command_correct_text;
            case KeyboardCommands.CMD_NEWLINE:
                return R.string.command_newline;
            case KeyboardCommands.CMD_MOVE_LEFT:
                return R.string.command_move_left;
            case KeyboardCommands.CMD_MOVE_RIGHT:
                return R.string.command_move_right;
            case KeyboardCommands.CMD_INPUT_PICKER:
                return R.string.command_input_picker;
            case KeyboardCommands.CMD_SETTINGS:
                return R.string.command_settings;
            case KeyboardCommands.CMD_HIDE:
                return R.string.command_hide;
            case KeyboardCommands.CMD_TOGGLE_LANGUAGE:
                return R.string.command_toggle_language;
            case KeyboardCommands.CMD_RESERVED_PHRASES:
                return R.string.command_reserved_phrases;
            case KeyboardCommands.CMD_DINGUL_CENTER_VOWEL:
                return R.string.command_dingul_center_vowel;
            case KeyboardCommands.CMD_DINGUL_WIDE_VOWEL:
                return R.string.command_dingul_wide_vowel;
            case KeyboardCommands.CMD_OPEN_OPTIONS:
                return R.string.command_open_options;
            case KeyboardCommands.CMD_SHIFT_ONCE:
                return R.string.command_shift_once;
            case KeyboardCommands.CMD_SHIFT_LOCK:
                return R.string.command_shift_lock;
            case KeyboardCommands.CMD_QUICK_SETTINGS:
                return R.string.command_quick_settings;
            case KeyboardCommands.CMD_CLIPBOARD_PANEL:
                return R.string.command_clipboard_panel;
            case KeyboardCommands.CMD_VOICE_INPUT:
                return R.string.command_voice_input;
            case KeyboardCommands.CMD_UNDO:
                return R.string.command_undo;
            case KeyboardCommands.CMD_TOOLS:
                return R.string.command_tools;
            case KeyboardCommands.CMD_REMOTE_ESC:
                return R.string.command_remote_esc;
            case KeyboardCommands.CMD_REMOTE_TAB:
                return R.string.command_remote_tab;
            case KeyboardCommands.CMD_REMOTE_SHIFT_TAB:
                return R.string.command_remote_shift_tab;
            case KeyboardCommands.CMD_REMOTE_CTRL_TAB:
                return R.string.command_remote_ctrl_tab;
            case KeyboardCommands.CMD_REMOTE_ALT_TAB:
                return R.string.command_remote_alt_tab;
            case KeyboardCommands.CMD_REMOTE_CTRL_LATCH:
                return R.string.command_remote_ctrl;
            case KeyboardCommands.CMD_REMOTE_WIN_LATCH:
                return R.string.command_remote_win;
            case KeyboardCommands.CMD_REMOTE_ALT_LATCH:
                return R.string.command_remote_alt;
            case KeyboardCommands.CMD_REMOTE_CTRL_LOCK:
                return R.string.command_remote_ctrl_lock;
            case KeyboardCommands.CMD_REMOTE_WIN_LOCK:
                return R.string.command_remote_win_lock;
            case KeyboardCommands.CMD_REMOTE_ALT_LOCK:
                return R.string.command_remote_alt_lock;
            case KeyboardCommands.CMD_REMOTE_IME_TOGGLE:
                return R.string.command_remote_ime;
            case KeyboardCommands.CMD_REMOTE_ARROW_UP:
                return R.string.command_remote_arrow_up;
            case KeyboardCommands.CMD_REMOTE_ARROW_DOWN:
                return R.string.command_remote_arrow_down;
            case KeyboardCommands.CMD_REMOTE_ARROW_LEFT:
                return R.string.command_remote_arrow_left;
            case KeyboardCommands.CMD_REMOTE_ARROW_RIGHT:
                return R.string.command_remote_arrow_right;
            case KeyboardCommands.CMD_REMOTE_INSERT:
                return R.string.command_remote_insert;
            case KeyboardCommands.CMD_REMOTE_FORWARD_DELETE:
                return R.string.command_remote_forward_delete;
            case KeyboardCommands.CMD_REMOTE_HOME:
                return R.string.command_remote_home;
            case KeyboardCommands.CMD_REMOTE_END:
                return R.string.command_remote_end;
            case KeyboardCommands.CMD_REMOTE_PAGE_UP:
                return R.string.command_remote_page_up;
            case KeyboardCommands.CMD_REMOTE_PAGE_DOWN:
                return R.string.command_remote_page_down;
            case KeyboardCommands.CMD_REMOTE_CTRL_ENTER:
                return R.string.command_remote_ctrl_enter;
            case KeyboardCommands.CMD_REMOTE_F1:
                return R.string.command_remote_f1;
            case KeyboardCommands.CMD_REMOTE_F2:
                return R.string.command_remote_f2;
            case KeyboardCommands.CMD_REMOTE_F3:
                return R.string.command_remote_f3;
            case KeyboardCommands.CMD_REMOTE_F4:
                return R.string.command_remote_f4;
            case KeyboardCommands.CMD_REMOTE_F5:
                return R.string.command_remote_f5;
            case KeyboardCommands.CMD_REMOTE_F6:
                return R.string.command_remote_f6;
            case KeyboardCommands.CMD_REMOTE_F7:
                return R.string.command_remote_f7;
            case KeyboardCommands.CMD_REMOTE_F8:
                return R.string.command_remote_f8;
            case KeyboardCommands.CMD_REMOTE_F9:
                return R.string.command_remote_f9;
            case KeyboardCommands.CMD_REMOTE_F10:
                return R.string.command_remote_f10;
            case KeyboardCommands.CMD_REMOTE_F11:
                return R.string.command_remote_f11;
            case KeyboardCommands.CMD_REMOTE_F12:
                return R.string.command_remote_f12;
            default:
                return 0;
        }
    }
}
