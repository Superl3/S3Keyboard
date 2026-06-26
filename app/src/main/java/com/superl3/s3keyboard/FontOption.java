package com.superl3.s3keyboard;

final class FontOption {
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
    public String toString() {
        return value;
    }
}
