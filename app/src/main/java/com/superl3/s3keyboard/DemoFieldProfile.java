package com.superl3.s3keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

final class DemoFieldProfile {
    static final DemoFieldProfile STANDARD = new DemoFieldProfile(
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE,
            EditorInfo.IME_ACTION_NONE,
            false);

    final int inputType;
    final int imeOptions;
    final boolean singleLine;

    private DemoFieldProfile(int inputType, int imeOptions, boolean singleLine) {
        this.inputType = inputType;
        this.imeOptions = imeOptions;
        this.singleLine = singleLine;
    }

    static DemoFieldProfile fromName(String name) {
        if (name == null) {
            return STANDARD;
        }
        switch (name) {
            case "password":
                return singleLine(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
            case "number":
                return singleLine(InputType.TYPE_CLASS_NUMBER);
            case "phone":
                return singleLine(InputType.TYPE_CLASS_PHONE);
            case "datetime":
                return singleLine(InputType.TYPE_CLASS_DATETIME);
            case "url":
                return singleLine(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            case "email":
                return singleLine(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            case "web_edit":
                return new DemoFieldProfile(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT,
                        EditorInfo.IME_ACTION_NONE,
                        false);
            case "search":
                return new DemoFieldProfile(
                        InputType.TYPE_CLASS_TEXT,
                        EditorInfo.IME_ACTION_SEARCH,
                        true);
            case "multiline":
            case "standard":
            default:
                return STANDARD;
        }
    }

    void applyTo(EditText input) {
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setInputType(inputType);
        input.setImeOptions(imeOptions);
        input.setSingleLine(singleLine);
        if (!singleLine) {
            input.setMinLines(2);
        }
    }

    private static DemoFieldProfile singleLine(int inputType) {
        return new DemoFieldProfile(inputType, EditorInfo.IME_ACTION_NONE, true);
    }
}
