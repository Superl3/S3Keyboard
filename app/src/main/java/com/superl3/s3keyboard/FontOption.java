package com.superl3.s3keyboard;

final class FontOption {
    static final FontOption[] BASIC_OPTIONS = {
            new FontOption("기본", KeyboardSettings.FONT_DEFAULT),
            new FontOption("Noto Sans KR", KeyboardSettings.FONT_NOTO_SANS_KR),
            new FontOption("Noto Serif KR", KeyboardSettings.FONT_NOTO_SERIF_KR),
            new FontOption("D2Coding", KeyboardSettings.FONT_D2CODING)
    };

    static final FontOption[] EDITOR_OPTIONS = {
            new FontOption("Default", KeyboardSettings.FONT_DEFAULT),
            new FontOption("Noto Sans KR", KeyboardSettings.FONT_NOTO_SANS_KR),
            new FontOption("Noto Serif KR", KeyboardSettings.FONT_NOTO_SERIF_KR),
            new FontOption("D2Coding", KeyboardSettings.FONT_D2CODING)
    };

    final String label;
    final String value;

    FontOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString() {
        return label;
    }
}
