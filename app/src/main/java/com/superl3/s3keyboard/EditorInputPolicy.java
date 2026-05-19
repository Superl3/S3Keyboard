package com.superl3.s3keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

final class EditorInputPolicy {
    static final EditorInputPolicy DEFAULT = new EditorInputPolicy(
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            true);

    final boolean password;
    final boolean numberLike;
    final boolean uriLike;
    final boolean emailLike;
    final boolean multiline;
    final boolean searchAction;
    final boolean rawKeyInput;
    final boolean preferAsciiLayout;
    final boolean forceNumberRow;
    final boolean allowComposingText;
    final boolean allowTextConveniences;

    private EditorInputPolicy(
            boolean password,
            boolean numberLike,
            boolean uriLike,
            boolean emailLike,
            boolean multiline,
            boolean searchAction,
            boolean rawKeyInput,
            boolean preferAsciiLayout,
            boolean forceNumberRow,
            boolean allowComposingText,
            boolean allowTextConveniences) {
        this.password = password;
        this.numberLike = numberLike;
        this.uriLike = uriLike;
        this.emailLike = emailLike;
        this.multiline = multiline;
        this.searchAction = searchAction;
        this.rawKeyInput = rawKeyInput;
        this.preferAsciiLayout = preferAsciiLayout;
        this.forceNumberRow = forceNumberRow;
        this.allowComposingText = allowComposingText;
        this.allowTextConveniences = allowTextConveniences;
    }

    static EditorInputPolicy from(EditorInfo info) {
        if (info == null) {
            return DEFAULT;
        }
        return fromEditorInfo(info.inputType, info.imeOptions);
    }

    static EditorInputPolicy fromInputType(int inputType) {
        return fromEditorInfo(inputType, 0);
    }

    static EditorInputPolicy fromEditorInfo(int inputType, int imeOptions) {
        boolean rawKeyInput = inputType == InputType.TYPE_NULL;
        int inputClass = inputType & InputType.TYPE_MASK_CLASS;
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        boolean password = isPassword(inputClass, variation);
        boolean numberLike = inputClass == InputType.TYPE_CLASS_NUMBER
                || inputClass == InputType.TYPE_CLASS_PHONE
                || inputClass == InputType.TYPE_CLASS_DATETIME;
        boolean uriLike = inputClass == InputType.TYPE_CLASS_TEXT
                && variation == InputType.TYPE_TEXT_VARIATION_URI;
        boolean emailLike = inputClass == InputType.TYPE_CLASS_TEXT
                && (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        boolean multiline = (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        boolean searchAction = (imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_SEARCH;
        boolean asciiPreferred = rawKeyInput || password || uriLike || emailLike;
        boolean forceNumberRow = password || numberLike;
        boolean allowComposingText = !rawKeyInput && !password && !numberLike;
        boolean allowTextConveniences = !rawKeyInput && !password && !numberLike && !uriLike && !emailLike;
        return new EditorInputPolicy(
                password,
                numberLike,
                uriLike,
                emailLike,
                multiline,
                searchAction,
                rawKeyInput,
                asciiPreferred,
                forceNumberRow,
                allowComposingText,
                allowTextConveniences);
    }

    KeyboardMode initialKeyboardMode(KeyboardMode storedMode) {
        if (numberLike || preferAsciiLayout) {
            return KeyboardMode.ENGLISH;
        }
        return storedMode == null ? KeyboardMode.HANGUL : storedMode;
    }

    boolean locksLanguageToggle() {
        return numberLike;
    }

    private static boolean isPassword(int inputClass, int variation) {
        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            return variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        }
        if (inputClass != InputType.TYPE_CLASS_TEXT) {
            return false;
        }
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
    }
}
