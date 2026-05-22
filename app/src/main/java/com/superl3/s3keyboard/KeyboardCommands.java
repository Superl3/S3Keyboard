package com.superl3.s3keyboard;

final class KeyboardCommands {
    static final String CMD_DELETE = "__delete__";
    static final String CMD_SPACE = "__space__";
    static final String CMD_ENTER = "__enter__";
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

    static String labelFor(String value) {
        if (value == null) {
            return null;
        }
        switch (value) {
            case CMD_DELETE:
                return "삭제";
            case CMD_SPACE:
                return "스페이스";
            case CMD_ENTER:
                return "전송";
            case CMD_MOVE_LEFT:
                return "←";
            case CMD_MOVE_RIGHT:
                return "→";
            case CMD_INPUT_PICKER:
                return "키보드";
            case CMD_SETTINGS:
                return "설정";
            case CMD_HIDE:
                return "숨김";
            case CMD_TOGGLE_LANGUAGE:
                return "한/영";
            case CMD_RESERVED_PHRASES:
                return "예약어";
            case CMD_RESERVED_LEFT:
            case CMD_RESERVED_RIGHT:
            case CMD_RESERVED_UP:
                return null;
            case CMD_DINGUL_CENTER_VOWEL:
                return "ㅣ";
            case CMD_DINGUL_WIDE_VOWEL:
                return "ㅡ";
            case CMD_OPEN_OPTIONS:
                return "옵션";
            case CMD_SHIFT_ONCE:
                return "Shift";
            case CMD_SHIFT_LOCK:
                return "Caps";
            case CMD_HAND_LEFT:
            case CMD_HAND_RIGHT:
            case CMD_HAND_BALANCED:
                return null;
            case CMD_QUICK_SETTINGS:
                return "Quick";
            case CMD_REMOTE_ESC:
                return "Esc";
            case CMD_REMOTE_TAB:
                return "Tab";
            case CMD_REMOTE_SHIFT_TAB:
                return "S⇥";
            case CMD_REMOTE_CTRL_TAB:
                return "C⇥";
            case CMD_REMOTE_ALT_TAB:
                return "A⇥";
            case CMD_REMOTE_CTRL_LATCH:
                return "Ctrl";
            case CMD_REMOTE_WIN_LATCH:
                return "Win";
            case CMD_REMOTE_ALT_LATCH:
                return "Alt";
            case CMD_REMOTE_CTRL_LOCK:
                return "Ctrl Lock";
            case CMD_REMOTE_WIN_LOCK:
                return "Win Lock";
            case CMD_REMOTE_ALT_LOCK:
                return "Alt Lock";
            case CMD_REMOTE_IME_TOGGLE:
                return "IME";
            case CMD_REMOTE_ARROW_UP:
                return "↑";
            case CMD_REMOTE_ARROW_DOWN:
                return "↓";
            case CMD_REMOTE_ARROW_LEFT:
                return "←";
            case CMD_REMOTE_ARROW_RIGHT:
                return "→";
            case CMD_REMOTE_INSERT:
                return "Ins";
            case CMD_REMOTE_FORWARD_DELETE:
                return "Del";
            case CMD_REMOTE_HOME:
                return "Home";
            case CMD_REMOTE_END:
                return "End";
            case CMD_REMOTE_PAGE_UP:
                return "PgUp";
            case CMD_REMOTE_PAGE_DOWN:
                return "PgDn";
            case CMD_REMOTE_CTRL_ENTER:
                return "Ctrl+Enter";
            case CMD_REMOTE_F1:
                return "F1";
            case CMD_REMOTE_F2:
                return "F2";
            case CMD_REMOTE_F3:
                return "F3";
            case CMD_REMOTE_F4:
                return "F4";
            case CMD_REMOTE_F5:
                return "F5";
            case CMD_REMOTE_F6:
                return "F6";
            case CMD_REMOTE_F7:
                return "F7";
            case CMD_REMOTE_F8:
                return "F8";
            case CMD_REMOTE_F9:
                return "F9";
            case CMD_REMOTE_F10:
                return "F10";
            case CMD_REMOTE_F11:
                return "F11";
            case CMD_REMOTE_F12:
                return "F12";
            case CMD_NOOP:
                return null;
            default:
                return value;
        }
    }
}
