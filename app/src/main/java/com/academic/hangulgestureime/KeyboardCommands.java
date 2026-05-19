package com.academic.hangulgestureime;

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
    static final String CMD_NOOP = "__noop__";

    private KeyboardCommands() {
    }

    static boolean isCommand(String value) {
        return value != null && value.startsWith("__") && value.endsWith("__");
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
            case CMD_NOOP:
                return null;
            default:
                return value;
        }
    }
}
