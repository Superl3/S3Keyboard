package com.superl3.s3keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.function.IntConsumer;

@SuppressLint("ViewConstructor")
final class NumericStepperRow extends LinearLayout {
    private final Context context;
    private final EditText input;
    private final ImageButton minusButton;
    private final ImageButton plusButton;
    private final int minValue;
    private final int maxValue;
    private final int step;
    private final int valueDescriptionResId;
    private final IntConsumer listener;
    private int committedValue;

    NumericStepperRow(
            Context context,
            int initialValue,
            int minValue,
            int maxValue,
            int step,
            int valueDescriptionResId,
            IntConsumer listener) {
        super(context);
        this.context = context;
        this.minValue = Math.min(minValue, maxValue);
        this.maxValue = Math.max(minValue, maxValue);
        this.step = Math.max(1, step);
        this.valueDescriptionResId = valueDescriptionResId;
        this.listener = RuntimeDefaults.intConsumer(listener);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        input = SettingsRowBuilder.editText(context);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setSelectAllOnFocus(true);
        input.setGravity(Gravity.CENTER);

        minusButton = stepperButton(context, R.drawable.ic_settings_minus);
        plusButton = stepperButton(context, R.drawable.ic_settings_plus);
        minusButton.setOnClickListener(v -> step(-this.step));
        plusButton.setOnClickListener(v -> step(this.step));
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

        addView(minusButton, SettingsRowBuilder.fixedSize(context, 48, 48));
        addView(input, SettingsRowBuilder.weightedWrap(context, 1f, 8, 8));
        addView(plusButton, SettingsRowBuilder.fixedSize(context, 48, 48));
        syncValue(initialValue);
    }

    void syncValue(int value) {
        setValue(value, false);
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
            return minValue;
        }
    }

    private void setValue(int value, boolean notify) {
        int clamped = Math.max(minValue, Math.min(maxValue, value));
        String normalized = String.valueOf(clamped);
        boolean valueChanged = committedValue != clamped;
        if (!normalized.equals(input.getText().toString().trim())) {
            input.setText(normalized);
            input.setSelection(normalized.length());
        }
        updateAccessibility(clamped);
        if (notify && valueChanged) {
            listener.accept(clamped);
        }
        committedValue = clamped;
    }

    private void updateAccessibility(int value) {
        String valueDescription = valueDescriptionResId == 0
                ? String.valueOf(value)
                : context.getString(valueDescriptionResId, value);
        input.setContentDescription(valueDescription);
        minusButton.setContentDescription(context.getString(
                R.string.settings_stepper_decrease_format,
                valueDescription));
        plusButton.setContentDescription(context.getString(
                R.string.settings_stepper_increase_format,
                valueDescription));
        setButtonEnabled(minusButton, value > minValue);
        setButtonEnabled(plusButton, value < maxValue);
    }

    private static void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.38f);
    }

    private static ImageButton stepperButton(Context context, int iconResId) {
        ImageButton button = new ImageButton(context);
        SettingsViewStyler.iconButton(button, context, iconResId);
        return button;
    }

}
