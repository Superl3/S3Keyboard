package com.superl3.s3keyboard;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.function.IntConsumer;

final class NumericStepperRow extends LinearLayout {
    private final EditText input;
    private final int maxValue;
    private final IntConsumer listener;

    NumericStepperRow(Context context, int initialValue, int maxValue, IntConsumer listener) {
        super(context);
        this.maxValue = maxValue;
        this.listener = RuntimeDefaults.intConsumer(listener);
        setOrientation(HORIZONTAL);
        input = SettingsRowBuilder.editText(context);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setSelectAllOnFocus(true);
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

        addView(minusButton, SettingsRowBuilder.weightedWrap(context, 0.9f, 0, 0));
        addView(input, SettingsRowBuilder.weightedWrap(context, 1.2f, 6, 6));
        addView(plusButton, SettingsRowBuilder.weightedWrap(context, 0.9f, 0, 0));
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
        if (notify) {
            listener.accept(clamped);
        }
    }

    private static Button stepperButton(Context context, String text) {
        return SettingsRowBuilder.button(context, text);
    }

}
