package com.superl3.s3keyboard;

final class FontOption implements SettingsLabelOption {
    static final FontOption[] BASIC_OPTIONS = {
            new FontOption(R.string.font_option_default, KeyboardSettings.FONT_DEFAULT),
            new FontOption(R.string.font_option_noto_sans_kr, KeyboardSettings.FONT_NOTO_SANS_KR),
            new FontOption(R.string.font_option_noto_serif_kr, KeyboardSettings.FONT_NOTO_SERIF_KR),
            new FontOption(R.string.font_option_d2coding, KeyboardSettings.FONT_D2CODING)
    };

    static final FontOption[] EDITOR_OPTIONS = {
            new FontOption(R.string.font_option_default, KeyboardSettings.FONT_DEFAULT),
            new FontOption(R.string.font_option_noto_sans_kr, KeyboardSettings.FONT_NOTO_SANS_KR),
            new FontOption(R.string.font_option_noto_serif_kr, KeyboardSettings.FONT_NOTO_SERIF_KR),
            new FontOption(R.string.font_option_d2coding, KeyboardSettings.FONT_D2CODING)
    };

    final int labelResId;
    final String value;

    FontOption(int labelResId, String value) {
        this.labelResId = labelResId;
        this.value = value;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    @Override
    public String toString() {
        return value;
    }

    static int indexOf(FontOption[] options, String fontFamily) {
        String normalized = KeyboardSettings.normalizeFontFamily(fontFamily);
        if (options == null || options.length == 0) {
            return 0;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].value.equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    static int basicIndexOf(String fontFamily) {
        return indexOf(BASIC_OPTIONS, fontFamily);
    }

    static int editorIndexOf(String fontFamily) {
        return indexOf(EDITOR_OPTIONS, fontFamily);
    }

}
