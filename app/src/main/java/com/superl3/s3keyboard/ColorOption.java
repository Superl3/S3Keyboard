package com.superl3.s3keyboard;

final class ColorOption {
    static final ColorOption[] BASIC_OPTIONS = {
            new ColorOption(R.string.color_option_default_button, KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption(R.string.color_option_keyboard_background, KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption(R.string.color_option_pressed, KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption(R.string.color_option_secondary, KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption(R.string.color_option_black, KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption(R.string.color_option_white, 0xFFFFFFFF),
            new ColorOption(R.string.color_option_blue, 0xFF3F6EDB),
            new ColorOption(R.string.color_option_green, 0xFF2E7D57),
            new ColorOption(R.string.color_option_teal, 0xFF00897B),
            new ColorOption(R.string.color_option_purple, 0xFF6D5BD0),
            new ColorOption(R.string.color_option_coral, 0xFFE76F51),
            new ColorOption(R.string.color_option_yellow, 0xFFE9C46A)
    };

    static final ColorOption[] EDITOR_OPTIONS = {
            new ColorOption(R.string.color_option_default_key, KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption(R.string.color_option_keyboard_background, KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption(R.string.color_option_pressed, KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption(R.string.color_option_secondary, KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption(R.string.color_option_black, KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption(R.string.color_option_white, 0xFFFFFFFF),
            new ColorOption(R.string.color_option_neutral_border, 0xFF45484F),
            new ColorOption(R.string.color_option_neutral_depth, 0xFF2F3339),
            new ColorOption(R.string.color_option_warm_border, 0xFF9F9488),
            new ColorOption(R.string.color_option_warm_depth, 0xFFB7AA9B),
            new ColorOption(R.string.color_option_blue, 0xFF3F6EDB),
            new ColorOption(R.string.color_option_green, 0xFF2E7D57),
            new ColorOption(R.string.color_option_teal, 0xFF00897B),
            new ColorOption(R.string.color_option_coral, 0xFFE76F51),
            new ColorOption(R.string.color_option_yellow, 0xFFE9C46A),
            new ColorOption(R.string.color_option_marigold_teal, 0xFF4DE4D2),
            new ColorOption(R.string.color_option_marigold_pink, 0xFFFF5DAE),
            new ColorOption(R.string.color_option_marigold_orange, 0xFFFF9F32)
    };

    final int labelResId;
    final int color;

    ColorOption(int labelResId, int color) {
        this.labelResId = labelResId;
        this.color = 0xFF000000 | (color & 0x00FFFFFF);
    }

    @Override
    public String toString() {
        return hex();
    }

    String hex() {
        return String.format("#%06X", color & 0x00FFFFFF);
    }
}
