package com.superl3.s3keyboard;

/** Single draft state shared by the layout editor handles and numeric controls. */
final class LayoutEditorState {
    enum Control {
        LEFT_PADDING,
        RIGHT_PADDING,
        KEY_GAP,
        HEIGHT,
        HANGUL_SPECIAL_GAP,
        HANGUL_SPECIAL_COLUMN,
        TOP_PADDING,
        BOTTOM_ROW_PADDING,
        BOTTOM_PADDING,
        NUMBER_ROW_GAP
    }

    final KeyboardSettings settings;
    final KeyboardMode mode;
    final Control selectedControl;

    private LayoutEditorState(KeyboardSettings settings, KeyboardMode mode, Control selectedControl) {
        this.settings = RuntimeDefaults.keyboardSettings(settings);
        this.mode = mode == null ? KeyboardMode.HANGUL : mode;
        this.selectedControl = selectedControl == null ? Control.LEFT_PADDING : selectedControl;
    }

    static LayoutEditorState from(KeyboardSettings settings, KeyboardMode mode) {
        return new LayoutEditorState(settings, mode, Control.LEFT_PADDING);
    }

    LayoutEditorState select(Control control) {
        return new LayoutEditorState(settings, mode, control);
    }

    LayoutEditorState withMode(KeyboardMode nextMode) {
        return new LayoutEditorState(settings.withKeyboardMode(nextMode), nextMode, selectedControl);
    }

    LayoutEditorState withValue(int value) {
        KeyboardSettings next = settings;
        switch (selectedControl) {
            case LEFT_PADDING:
                next = mode == KeyboardMode.HANGUL
                        ? settings.withHangulSidePadding(value, settings.hangulRightPaddingDp)
                        : settings.withEnglishSidePadding(value, settings.englishRightPaddingDp);
                break;
            case RIGHT_PADDING:
                next = mode == KeyboardMode.HANGUL
                        ? settings.withHangulSidePadding(settings.hangulLeftPaddingDp, value)
                        : settings.withEnglishSidePadding(settings.englishLeftPaddingDp, value);
                break;
            case KEY_GAP:
                next = mode == KeyboardMode.HANGUL
                        ? settings.withHangulKeyGap(value)
                        : settings.withEnglishKeyGap(value);
                break;
            case HEIGHT:
                next = mode == KeyboardMode.HANGUL
                        ? settings.withHangulHeight(value)
                        : settings.withEnglishHeight(value);
                break;
            case HANGUL_SPECIAL_GAP:
                next = settings.withLayoutSpacing(
                        value,
                        settings.keyboardTopPaddingDp,
                        settings.keyboardBottomPaddingDp,
                        settings.bottomRowTopPaddingDp);
                break;
            case HANGUL_SPECIAL_COLUMN:
                next = settings.withHangulSpecialColumnPercent(value);
                break;
            case TOP_PADDING:
                next = settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        value,
                        settings.keyboardBottomPaddingDp,
                        settings.bottomRowTopPaddingDp);
                break;
            case BOTTOM_ROW_PADDING:
                next = settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        settings.keyboardTopPaddingDp,
                        settings.keyboardBottomPaddingDp,
                        value);
                break;
            case BOTTOM_PADDING:
                next = settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        settings.keyboardTopPaddingDp,
                        value,
                        settings.bottomRowTopPaddingDp);
                break;
            case NUMBER_ROW_GAP:
                next = settings.withNumberRowBottomGap(value);
                break;
        }
        return new LayoutEditorState(next, mode, selectedControl);
    }

    int value() {
        switch (selectedControl) {
            case LEFT_PADDING:
                return mode == KeyboardMode.HANGUL
                        ? settings.hangulLeftPaddingDp : settings.englishLeftPaddingDp;
            case RIGHT_PADDING:
                return mode == KeyboardMode.HANGUL
                        ? settings.hangulRightPaddingDp : settings.englishRightPaddingDp;
            case KEY_GAP:
                return mode == KeyboardMode.HANGUL
                        ? settings.hangulKeyGapDp : settings.englishKeyGapDp;
            case HEIGHT:
                return mode == KeyboardMode.HANGUL
                        ? settings.hangulKeyboardHeightDp : settings.englishKeyboardHeightDp;
            case HANGUL_SPECIAL_GAP:
                return settings.hangulMainSpecialGapDp;
            case HANGUL_SPECIAL_COLUMN:
                return settings.hangulSpecialColumnPercent;
            case TOP_PADDING:
                return settings.keyboardTopPaddingDp;
            case BOTTOM_ROW_PADDING:
                return settings.bottomRowTopPaddingDp;
            case BOTTOM_PADDING:
                return settings.keyboardBottomPaddingDp;
            case NUMBER_ROW_GAP:
                return settings.numberRowBottomGapDp;
            default:
                return 0;
        }
    }

    int minValue() {
        switch (selectedControl) {
            case HEIGHT:
                return KeyboardSettings.MIN_HEIGHT_DP;
            case HANGUL_SPECIAL_COLUMN:
                return KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT;
            default:
                return 0;
        }
    }

    int maxValue() {
        switch (selectedControl) {
            case HEIGHT:
                return KeyboardSettings.MAX_HEIGHT_DP;
            case HANGUL_SPECIAL_COLUMN:
                return KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT;
            case KEY_GAP:
                return KeyboardSettings.MAX_KEY_GAP_DP;
            case HANGUL_SPECIAL_GAP:
                return KeyboardSettings.MAX_HANGUL_MAIN_SPECIAL_GAP_DP;
            case TOP_PADDING:
                return KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP;
            case BOTTOM_ROW_PADDING:
                return KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP;
            case BOTTOM_PADDING:
                return KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP;
            case NUMBER_ROW_GAP:
                return KeyboardSettings.MAX_NUMBER_ROW_BOTTOM_GAP_DP;
            default:
                return KeyboardSettings.MAX_MARGIN_DP;
        }
    }
}
