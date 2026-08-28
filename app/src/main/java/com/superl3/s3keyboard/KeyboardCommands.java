package com.superl3.s3keyboard;

final class KeyboardCommands {
    static final String CMD_DELETE = "__delete__";
    static final String CMD_DELETE_WORD = "__delete_word__";
    static final String CMD_SPACE = "__space__";
    static final String CMD_ENTER = "__enter__";
    static final String CMD_CORRECT_TEXT = "__correct_text__";
    static final String CMD_NEWLINE = "__newline__";
    static final String CMD_MOVE_LEFT = "__move_left__";
    static final String CMD_MOVE_RIGHT = "__move_right__";
    static final String CMD_INPUT_PICKER = "__input_picker__";
    static final String CMD_SETTINGS = "__settings__";
    static final String CMD_HIDE = "__hide__";
    static final String CMD_TOGGLE_LANGUAGE = "__toggle_language__";
    static final String CMD_RESERVED_PHRASES = "__reserved_phrases__";
    static final String CMD_RESERVED_LEFT = "__reserved_left__";
    static final String CMD_RESERVED_RIGHT = "__reserved_right__";
    static final String CMD_RESERVED_UP = "__reserved_up__";
    static final String CMD_DINGUL_CENTER_VOWEL = "__dingul_center_vowel__";
    static final String CMD_DINGUL_WIDE_VOWEL = "__dingul_wide_vowel__";
    static final String CMD_OPEN_OPTIONS = "__open_options__";
    static final String CMD_SHIFT_ONCE = "__shift_once__";
    static final String CMD_SHIFT_LOCK = "__shift_lock__";
    static final String CMD_HAND_LEFT = "__hand_left__";
    static final String CMD_HAND_RIGHT = "__hand_right__";
    static final String CMD_HAND_BALANCED = "__hand_balanced__";
    static final String CMD_QUICK_SETTINGS = "__quick_settings__";
    static final String CMD_CLIPBOARD_PANEL = "__clipboard_panel__";
    static final String CMD_VOICE_INPUT = "__voice_input__";
    static final String CMD_UNDO = "__undo__";
    static final String CMD_TOOLS = "__tools__";
    static final String CMD_REMOTE_ESC = "__remote_esc__";
    static final String CMD_REMOTE_TAB = "__remote_tab__";
    static final String CMD_REMOTE_SHIFT_TAB = "__remote_shift_tab__";
    static final String CMD_REMOTE_CTRL_TAB = "__remote_ctrl_tab__";
    static final String CMD_REMOTE_ALT_TAB = "__remote_alt_tab__";
    static final String CMD_REMOTE_CTRL_LATCH = "__remote_ctrl_latch__";
    static final String CMD_REMOTE_WIN_LATCH = "__remote_win_latch__";
    static final String CMD_REMOTE_ALT_LATCH = "__remote_alt_latch__";
    static final String CMD_REMOTE_CTRL_LOCK = "__remote_ctrl_lock__";
    static final String CMD_REMOTE_WIN_LOCK = "__remote_win_lock__";
    static final String CMD_REMOTE_ALT_LOCK = "__remote_alt_lock__";
    static final String CMD_REMOTE_IME_TOGGLE = "__remote_ime_toggle__";
    static final String CMD_REMOTE_ARROW_UP = "__remote_arrow_up__";
    static final String CMD_REMOTE_ARROW_DOWN = "__remote_arrow_down__";
    static final String CMD_REMOTE_ARROW_LEFT = "__remote_arrow_left__";
    static final String CMD_REMOTE_ARROW_RIGHT = "__remote_arrow_right__";
    static final String CMD_REMOTE_INSERT = "__remote_insert__";
    static final String CMD_REMOTE_FORWARD_DELETE = "__remote_forward_delete__";
    static final String CMD_REMOTE_HOME = "__remote_home__";
    static final String CMD_REMOTE_END = "__remote_end__";
    static final String CMD_REMOTE_PAGE_UP = "__remote_page_up__";
    static final String CMD_REMOTE_PAGE_DOWN = "__remote_page_down__";
    static final String CMD_REMOTE_CTRL_ENTER = "__remote_ctrl_enter__";
    static final String CMD_REMOTE_F1 = "__remote_f1__";
    static final String CMD_REMOTE_F2 = "__remote_f2__";
    static final String CMD_REMOTE_F3 = "__remote_f3__";
    static final String CMD_REMOTE_F4 = "__remote_f4__";
    static final String CMD_REMOTE_F5 = "__remote_f5__";
    static final String CMD_REMOTE_F6 = "__remote_f6__";
    static final String CMD_REMOTE_F7 = "__remote_f7__";
    static final String CMD_REMOTE_F8 = "__remote_f8__";
    static final String CMD_REMOTE_F9 = "__remote_f9__";
    static final String CMD_REMOTE_F10 = "__remote_f10__";
    static final String CMD_REMOTE_F11 = "__remote_f11__";
    static final String CMD_REMOTE_F12 = "__remote_f12__";
    static final String CMD_NOOP = "__noop__";

    private KeyboardCommands() {
    }

    static boolean isCommand(String value) {
        return value != null && value.startsWith("__") && value.endsWith("__");
    }

    static boolean isRemoteCommand(String value) {
        return value != null && value.startsWith("__remote_") && value.endsWith("__");
    }
}
