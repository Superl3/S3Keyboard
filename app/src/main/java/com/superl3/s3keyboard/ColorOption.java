package com.superl3.s3keyboard;

final class ColorOption {
    static final ColorOption[] BASIC_OPTIONS = {
            new ColorOption("기본 버튼", KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption("버튼 바깥", KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption("눌림 회색", KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption("보조 회색", KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption("검정", KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption("흰색", 0xFFFFFFFF),
            new ColorOption("파랑", 0xFF3F6EDB),
            new ColorOption("초록", 0xFF2E7D57),
            new ColorOption("청록", 0xFF00897B),
            new ColorOption("보라", 0xFF6D5BD0),
            new ColorOption("코랄", 0xFFE76F51),
            new ColorOption("노랑", 0xFFE9C46A)
    };

    static final ColorOption[] EDITOR_OPTIONS = {
            new ColorOption("기본 키", KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption("키보드 배경", KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption("눌림 회색", KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption("보조 회색", KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption("검정", KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption("흰색", 0xFFFFFFFF),
            new ColorOption("중립 테두리", 0xFF45484F),
            new ColorOption("중립 입체", 0xFF2F3339),
            new ColorOption("웜 테두리", 0xFF9F9488),
            new ColorOption("웜 입체", 0xFFB7AA9B),
            new ColorOption("파랑", 0xFF3F6EDB),
            new ColorOption("초록", 0xFF2E7D57),
            new ColorOption("청록", 0xFF00897B),
            new ColorOption("코랄", 0xFFE76F51),
            new ColorOption("노랑", 0xFFE9C46A),
            new ColorOption("메리골드 청록", 0xFF4DE4D2),
            new ColorOption("메리골드 핑크", 0xFFFF5DAE),
            new ColorOption("메리골드 오렌지", 0xFFFF9F32)
    };

    final String label;
    final int color;

    ColorOption(String label, int color) {
        this.label = label;
        this.color = 0xFF000000 | (color & 0x00FFFFFF);
    }

    @Override
    public String toString() {
        return label;
    }

    String hex() {
        return String.format("#%06X", color & 0x00FFFFFF);
    }
}
