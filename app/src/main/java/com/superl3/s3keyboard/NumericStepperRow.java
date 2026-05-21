package com.superl3.s3keyboard;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

final class NumericStepperRow extends LinearLayout {
    interface Listener {
        void onValueChanged(int value);
    }

    private final EditText input;
    private final int maxValue;
    private final Listener listener;

    NumericStepperRow(Context context, int initialValue, int maxValue, Listener listener) {
        super(context);
        this.maxValue = maxValue;
        this.listener = listener;
        setOrientation(HORIZONTAL);
        input = new EditText(context);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setSelectAllOnFocus(true);
        SettingsViewStyler.editText(input, context);
        input.setText(String.valueOf(initialValue));

        Button minusButton = stepperButton(context, "-");
        Button plusButton = stepperButton(context, "+");
        minusButton.setOnClickListener(v -> step(-2));
        plusButton.setOnClickListener(v -> step(2));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                apply();
                input.clearFocus();
                return true;
            }
            return false;
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                apply();
            }
        });

        addView(minusButton, buttonParams());
        addView(input, inputParams(context));
        addView(plusButton, buttonParams());
    }

    EditText input() {
        return input;
    }

    private void step(int delta) {
        setValue(parseValue() + delta, true);
    }

    private void apply() {
        setValue(parseValue(), true);
    }

    private int parseValue() {
        try {
            return Integer.parseInt(input.getText().toString().trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void setValue(int value, boolean notify) {
        int clamped = Math.max(0, Math.min(maxValue, value));
        input.setText(String.valueOf(clamped));
        if (notify && listener != null) {
            listener.onValueChanged(clamped);
        }
    }

    private static Button stepperButton(Context context, String text) {
        Button button = new Button(context);
        button.setText(text);
        SettingsViewStyler.button(button, context, false);
        return button;
    }

    private static LayoutParams buttonParams() {
        return new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.9f);
    }

    private static LayoutParams inputParams(Context context) {
        LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.2f);
        params.leftMargin = dp(context, 6);
        params.rightMargin = dp(context, 6);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
