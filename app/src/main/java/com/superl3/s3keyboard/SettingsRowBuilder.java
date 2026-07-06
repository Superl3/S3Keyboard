package com.superl3.s3keyboard;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

final class SettingsRowBuilder {
    private SettingsRowBuilder() {
    }

    static TextView label(Context context, String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextSize(14);
        label.setGravity(Gravity.START);
        SettingsViewStyler.label(label, context, false);
        return label;
    }

    static TextView label(Context context, int textResId) {
        return label(context, context.getString(textResId));
    }

    static TextView labelRow(
            Context context,
            LinearLayout root,
            String text,
            int topMarginDp) {
        TextView label = label(context, text);
        root.addView(label, matchWrapWithTop(context, topMarginDp));
        return label;
    }

    static TextView labelRow(
            Context context,
            LinearLayout root,
            int textResId,
            int topMarginDp) {
        return labelRow(context, root, context.getString(textResId), topMarginDp);
    }

    static TextView secondaryLabel(Context context, String text) {
        TextView label = label(context, text);
        SettingsViewStyler.label(label, context, true);
        return label;
    }

    static TextView secondaryLabel(Context context, int textResId) {
        return secondaryLabel(context, context.getString(textResId));
    }

    static TextView secondaryLabelRow(
            Context context,
            LinearLayout root,
            int textResId,
            int topMarginDp) {
        TextView label = secondaryLabel(context, textResId);
        root.addView(label, matchWrapWithTop(context, topMarginDp));
        return label;
    }

    static TextView sectionLabel(Context context, String text) {
        TextView label = label(context, text);
        label.setTextSize(16);
        return label;
    }

    static TextView sectionLabelRow(
            Context context,
            LinearLayout root,
            int textResId,
            int topMarginDp) {
        TextView label = sectionLabel(context, context.getString(textResId));
        root.addView(label, matchWrapWithTop(context, topMarginDp));
        return label;
    }

    static TextView bodyLabel(Context context, String text) {
        TextView label = label(context, text);
        label.setLineSpacing(dp(context, 2), 1.0f);
        return label;
    }

    static TextView bodyLabelRow(
            Context context,
            LinearLayout root,
            String text,
            int topMarginDp) {
        TextView label = bodyLabel(context, text);
        root.addView(label, matchWrapWithTop(context, topMarginDp));
        return label;
    }

    static TextView bodyLabelRow(
            Context context,
            LinearLayout root,
            int textResId,
            int topMarginDp) {
        return bodyLabelRow(context, root, context.getString(textResId), topMarginDp);
    }

    static Button button(Context context, int labelResId) {
        return button(context, context.getString(labelResId));
    }

    static Button button(Context context, int labelResId, View.OnClickListener listener) {
        return button(context, context.getString(labelResId), listener);
    }

    static Button button(Context context, String label) {
        return button(context, label, false);
    }

    static Button button(Context context, String label, View.OnClickListener listener) {
        Button button = button(context, label);
        button.setOnClickListener(listener);
        return button;
    }

    static Button button(Context context, String label, boolean selected) {
        Button button = new Button(context);
        button.setText(label);
        SettingsViewStyler.button(button, context, selected);
        return button;
    }

    static Button button(
            Context context,
            String label,
            boolean selected,
            View.OnClickListener listener) {
        Button button = button(context, label, selected);
        button.setOnClickListener(listener);
        return button;
    }

    static Button buttonRow(
            Context context,
            LinearLayout root,
            int labelResId,
            int topMarginDp,
            View.OnClickListener listener) {
        return buttonRow(context, root, context.getString(labelResId), topMarginDp, listener);
    }

    static Button buttonRow(
            Context context,
            LinearLayout root,
            String label,
            int topMarginDp,
            View.OnClickListener listener) {
        Button button = button(context, label, listener);
        root.addView(button, matchWrapWithTop(context, topMarginDp));
        return button;
    }

    static Button weightedButton(
            Context context,
            LinearLayout row,
            int labelResId,
            int leftMarginDp,
            int rightMarginDp,
            View.OnClickListener listener) {
        Button button = button(context, labelResId, listener);
        row.addView(button, weightedWrap(context, leftMarginDp, rightMarginDp));
        return button;
    }

    static Button iconButton(Context context, int labelResId, int iconResId) {
        Button button = button(context, labelResId);
        SettingsViewStyler.buttonIcon(button, context, iconResId);
        return button;
    }

    static Button iconButtonRow(
            Context context,
            LinearLayout root,
            int labelResId,
            int iconResId,
            int topMarginDp,
            View.OnClickListener listener) {
        Button button = iconButton(context, labelResId, iconResId);
        button.setOnClickListener(listener);
        root.addView(button, matchWrapWithTop(context, topMarginDp));
        return button;
    }

    static EditText editText(Context context) {
        EditText input = new EditText(context);
        SettingsViewStyler.editText(input, context);
        return input;
    }

    static EditText editText(
            Context context,
            String initialValue,
            BooleanSupplier canHandleUserChange,
            Consumer<String> onUserTextChanged) {
        EditText input = editText(context);
        input.setText(RuntimeDefaults.stringOrDefault(initialValue, ""));
        input.addTextChangedListener(
                UserInputListeners.text(canHandleUserChange, onUserTextChanged));
        return input;
    }

    static CheckBox checkBox(Context context, int labelResId) {
        return checkBox(context, context.getString(labelResId));
    }

    static CheckBox checkBox(Context context, String label) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(label);
        SettingsViewStyler.compoundButton(checkBox, context);
        return checkBox;
    }

    static CheckBox checkBox(
            Context context,
            int labelResId,
            BooleanSupplier canHandleUserChange,
            Consumer<Boolean> onUserCheckedChanged) {
        return checkBox(
                context,
                context.getString(labelResId),
                canHandleUserChange,
                onUserCheckedChanged);
    }

    static CheckBox checkBox(
            Context context,
            String label,
            BooleanSupplier canHandleUserChange,
            Consumer<Boolean> onUserCheckedChanged) {
        CheckBox checkBox = checkBox(context, label);
        checkBox.setOnCheckedChangeListener(
                UserInputListeners.checked(canHandleUserChange, onUserCheckedChanged));
        return checkBox;
    }

    static CheckBox checkBoxRow(
            Context context,
            LinearLayout root,
            int labelResId,
            int topMarginDp,
            BooleanSupplier canHandleUserChange,
            Consumer<Boolean> onUserCheckedChanged) {
        return checkBoxRow(
                context,
                root,
                context.getString(labelResId),
                topMarginDp,
                canHandleUserChange,
                onUserCheckedChanged);
    }

    static CheckBox checkBoxRow(
            Context context,
            LinearLayout root,
            String label,
            int topMarginDp,
            BooleanSupplier canHandleUserChange,
            Consumer<Boolean> onUserCheckedChanged) {
        CheckBox checkBox = checkBox(context, label, canHandleUserChange, onUserCheckedChanged);
        root.addView(checkBox, matchWrapWithTop(context, topMarginDp));
        return checkBox;
    }

    static RadioButton radioButton(Context context, int id, String label) {
        RadioButton button = new RadioButton(context);
        button.setId(id);
        button.setText(label);
        SettingsViewStyler.compoundButton(button, context);
        return button;
    }

    static RadioButton radioButton(Context context, int id, int labelResId) {
        return radioButton(context, id, context.getString(labelResId));
    }

    static Spinner spinner(Context context, String[] labels) {
        Spinner spinner = spinner(context);
        spinner.setAdapter(new SettingsArrayAdapter<>(context, labels));
        return spinner;
    }

    static Spinner spinner(
            Context context,
            String[] labels,
            BooleanSupplier canHandleSelection,
            IntConsumer onUserItemSelected) {
        Spinner spinner = spinner(context, labels);
        spinner.setOnItemSelectedListener(
                UserInputListeners.itemSelected(canHandleSelection, onUserItemSelected));
        return spinner;
    }

    static Spinner spinnerAfterInitialSelection(
            Context context,
            String[] labels,
            BooleanSupplier canHandleSelection,
            IntConsumer onUserItemSelected) {
        Spinner spinner = spinner(context, labels);
        spinner.setTag(Boolean.FALSE);
        BooleanSupplier safeCanHandleSelection =
                RuntimeDefaults.trueBooleanSupplier(canHandleSelection);
        spinner.setOnItemSelectedListener(
                UserInputListeners.itemSelected(
                        () -> shouldHandleAfterInitialSelection(spinner, safeCanHandleSelection),
                        onUserItemSelected));
        return spinner;
    }

    static <T> Spinner spinner(Context context, T[] options) {
        Spinner spinner = spinner(context);
        spinner.setAdapter(new SettingsArrayAdapter<>(context, options));
        return spinner;
    }

    static <T> Spinner spinner(
            Context context,
            T[] options,
            BooleanSupplier canHandleSelection,
            IntConsumer onUserItemSelected) {
        Spinner spinner = spinner(context, options);
        spinner.setOnItemSelectedListener(
                UserInputListeners.itemSelected(canHandleSelection, onUserItemSelected));
        return spinner;
    }

    static <T extends SettingsLabelOption> Spinner optionSpinner(
            Context context,
            T[] options,
            BooleanSupplier canHandleSelection,
            Consumer<T> onUserOptionSelected) {
        Consumer<T> safeConsumer = RuntimeDefaults.consumer(onUserOptionSelected);
        return spinner(
                context,
                SettingsDisplayLabels.labels(context, options),
                canHandleSelection,
                position -> {
                    if (position >= 0 && position < options.length) {
                        safeConsumer.accept(options[position]);
                    }
                });
    }

    static void setSelectionIfValid(Spinner spinner, int position) {
        if (spinner != null && position >= 0) {
            spinner.setSelection(position, false);
        }
    }

    static void setProgressIfPresent(SeekBar seekBar, int progress) {
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }

    static void setCheckedIfPresent(CheckBox checkBox, boolean checked) {
        if (checkBox != null) {
            checkBox.setChecked(checked);
        }
    }

    static void setTextIfPresent(TextView view, CharSequence text) {
        if (view != null) {
            view.setText(text);
        }
    }

    static void setEnabledIfPresent(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
        }
    }

    static SeekBar seekBar(Context context, int max) {
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(max);
        return seekBar;
    }

    static SeekBar seekBar(
            Context context,
            int max,
            BooleanSupplier canHandleUserChange,
            IntConsumer onUserProgressChanged) {
        SeekBar seekBar = seekBar(context, max);
        seekBar.setOnSeekBarChangeListener(
                UserInputListeners.seekBar(canHandleUserChange, onUserProgressChanged));
        return seekBar;
    }

    static TextView valueLabel(Context context) {
        return label(context, "");
    }

    static TextView valueLabelRow(Context context, LinearLayout root, int topMarginDp) {
        TextView valueLabel = valueLabel(context);
        root.addView(valueLabel, matchWrapWithTop(context, topMarginDp));
        return valueLabel;
    }

    static SeekBar seekBarRow(
            Context context,
            LinearLayout root,
            TextView valueLabel,
            int max,
            int topMarginDp,
            BooleanSupplier canHandleUserChange,
            IntConsumer onUserProgressChanged) {
        SeekBar seekBar = seekBar(context, max, canHandleUserChange, onUserProgressChanged);
        root.addView(valueLabel, matchWrapWithTop(context, topMarginDp));
        root.addView(seekBar, matchWrap());
        return seekBar;
    }

    static <T extends View> T labeledControl(
            Context context,
            LinearLayout root,
            int labelResId,
            T control,
            int topMarginDp) {
        root.addView(label(context, labelResId), matchWrapWithTop(context, topMarginDp));
        root.addView(control, matchWrap());
        return control;
    }

    static LinearLayout horizontal(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    static LinearLayout vertical(Context context) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    static <T extends View> T addView(LinearLayout root, T view) {
        root.addView(view, matchWrap());
        return view;
    }

    static <T extends View> T addViewWithTop(
            Context context,
            LinearLayout root,
            T view,
            int topMarginDp) {
        root.addView(view, matchWrapWithTop(context, topMarginDp));
        return view;
    }

    static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    static FrameLayout.LayoutParams frameMatchWrap() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
    }

    static FrameLayout.LayoutParams frameMatchMatch() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
    }

    static LinearLayout.LayoutParams wrapContent() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    static LinearLayout.LayoutParams wrapContentWithLeft(Context context, int leftMarginDp) {
        LinearLayout.LayoutParams params = wrapContent();
        params.leftMargin = dp(context, leftMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams fixedSize(Context context, int widthDp, int heightDp) {
        return new LinearLayout.LayoutParams(dp(context, widthDp), dp(context, heightDp));
    }

    static LinearLayout.LayoutParams fixedSizeWithLeft(
            Context context,
            int widthDp,
            int heightDp,
            int leftMarginDp) {
        LinearLayout.LayoutParams params = fixedSize(context, widthDp, heightDp);
        params.leftMargin = dp(context, leftMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams fixedWidthWrapWithLeft(
            Context context,
            int widthDp,
            int leftMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(context, widthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dp(context, leftMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams matchWrapWithTop(Context context, int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(context, topMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams matchHeightWithTop(
            Context context,
            int heightDp,
            int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, heightDp));
        params.topMargin = dp(context, topMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams matchHeightWithVerticalMargins(
            Context context,
            int heightDp,
            int verticalMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, heightDp));
        int margin = dp(context, verticalMarginDp);
        params.topMargin = margin;
        params.bottomMargin = margin;
        return params;
    }

    static LinearLayout.LayoutParams weightedWrap(
            Context context,
            int leftMarginDp,
            int rightMarginDp) {
        return weightedWrap(context, 1f, leftMarginDp, rightMarginDp);
    }

    static LinearLayout.LayoutParams weightedWrap(
            Context context,
            float weight,
            int leftMarginDp,
            int rightMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                weight);
        params.leftMargin = dp(context, leftMarginDp);
        params.rightMargin = dp(context, rightMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams weightedHeight(Context context, int heightDp, int leftMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                dp(context, heightDp),
                1f);
        params.leftMargin = dp(context, leftMarginDp);
        return params;
    }

    static LinearLayout.LayoutParams weightedSpacer() {
        return new LinearLayout.LayoutParams(0, 0, 1f);
    }

    static LinearLayout.LayoutParams matchWeightedFill() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f);
    }

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static float dp(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    private static Spinner spinner(Context context) {
        Spinner spinner = new Spinner(context);
        SettingsViewStyler.spinner(spinner, context);
        return spinner;
    }

    private static boolean shouldHandleAfterInitialSelection(
            Spinner spinner,
            BooleanSupplier canHandleSelection) {
        if (Boolean.FALSE.equals(spinner.getTag())) {
            spinner.setTag(Boolean.TRUE);
            return false;
        }
        return canHandleSelection.getAsBoolean();
    }
}
