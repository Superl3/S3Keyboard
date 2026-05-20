package com.superl3.s3keyboard;

final class ColorOption {
    static final ColorOption[] BASIC_OPTIONS = {
            new ColorOption("\uAE30\uBCF8 \uBC84\uD2BC", KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption("\uD0A4\uBCF4\uB4DC \uBC30\uACBD", KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption("\uB20C\uB9BC \uC0C9\uC0C1", KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption("\uBCF4\uC870 \uC0C9\uC0C1", KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption("\uAC80\uC815", KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption("\uD770\uC0C9", 0xFFFFFFFF),
            new ColorOption("\uD30C\uB791", 0xFF3F6EDB),
            new ColorOption("\uCD08\uB85D", 0xFF2E7D57),
            new ColorOption("\uCCAD\uB85D", 0xFF00897B),
            new ColorOption("\uBCF4\uB77C", 0xFF6D5BD0),
            new ColorOption("\uCF54\uB784", 0xFFE76F51),
            new ColorOption("\uC610\uB85C", 0xFFE9C46A)
    };

    static final ColorOption[] EDITOR_OPTIONS = {
            new ColorOption("\uAE30\uBCF8 \uD0A4", KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
            new ColorOption("\uD0A4\uBCF4\uB4DC \uBC30\uACBD", KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
            new ColorOption("\uB20C\uB9BC \uC0C9\uC0C1", KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
            new ColorOption("\uBCF4\uC870 \uC0C9\uC0C1", KeyboardSettings.DEFAULT_SECONDARY_COLOR),
            new ColorOption("\uAC80\uC815", KeyboardSettings.DEFAULT_ACCENT_COLOR),
            new ColorOption("\uD770\uC0C9", 0xFFFFFFFF),
            new ColorOption("\uC911\uB9BD \uD14C\uB450\uB9AC", 0xFF45484F),
            new ColorOption("\uC911\uB9BD \uC785\uCCB4", 0xFF2F3339),
            new ColorOption("\uB530\uB73B\uD55C \uD14C\uB450\uB9AC", 0xFF9F9488),
            new ColorOption("\uB530\uB73B\uD55C \uC785\uCCB4", 0xFFB7AA9B),
            new ColorOption("\uD30C\uB791", 0xFF3F6EDB),
            new ColorOption("\uCD08\uB85D", 0xFF2E7D57),
            new ColorOption("\uCCAD\uB85D", 0xFF00897B),
            new ColorOption("\uCF54\uB784", 0xFFE76F51),
            new ColorOption("\uC610\uB85C", 0xFFE9C46A),
            new ColorOption("\uBA54\uB9AC\uACE8\uB4DC \uCCAD\uB85D", 0xFF4DE4D2),
            new ColorOption("\uBA54\uB9AC\uACE8\uB4DC \uD551\uD06C", 0xFFFF5DAE),
            new ColorOption("\uBA54\uB9AC\uACE8\uB4DC \uC624\uB80C\uC9C0", 0xFFFF9F32)
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
