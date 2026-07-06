package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class LayoutSettingsController {
    private static final HandednessMode[] HANDEDNESS_ORDER = HandednessMode.displayOrder();
    private static final KeyboardLayoutProfile[] LAYOUT_PROFILE_ORDER = KeyboardLayoutProfile.displayOrder();

    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Consumer<KeyboardLayoutProfile> hangulLayoutProfileSaver;
    private final Consumer<KeyboardLayoutProfile> englishLayoutProfileSaver;
    private final Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver;
    private final Runnable controlsSyncer;
    private ErgonomicsSettingsController ergonomicsSettingsController;
    private Spinner handednessSpinner;
    private Spinner hangulLayoutProfileSpinner;
    private Spinner englishLayoutProfileSpinner;
    private SeekBar hangulHeightSeekBar;
    private SeekBar englishHeightSeekBar;
    private SeekBar hangulSpecialColumnSeekBar;
    private CheckBox hangulNumberRowCheckBox;
    private CheckBox englishNumberRowCheckBox;
    private TextView sharedPaddingValue;
    private TextView hangulHeightValue;
    private TextView englishHeightValue;
    private TextView hangulSpecialColumnValue;
    private TextView keyboardTopPaddingValue;
    private TextView keyboardBottomPaddingValue;
    private TextView numberRowBottomGapValue;
    private TextView hangulKeyGapValue;
    private TextView englishKeyGapValue;
    private EditText sharedPaddingInput;
    private EditText keyboardTopPaddingInput;
    private EditText keyboardBottomPaddingInput;
    private EditText numberRowBottomGapInput;
    private EditText hangulKeyGapInput;
    private EditText englishKeyGapInput;
    private boolean syncing = true;

    LayoutSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Supplier<KeyboardErgonomicsOptions> ergonomicsOptions,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver,
            Consumer<KeyboardLayoutProfile> hangulLayoutProfileSaver,
            Consumer<KeyboardLayoutProfile> englishLayoutProfileSaver,
            Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver,
            Runnable controlsSyncer) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.ergonomicsOptions = RuntimeDefaults.keyboardErgonomicsSupplier(ergonomicsOptions);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.hangulLayoutProfileSaver =
                RuntimeDefaults.keyboardLayoutProfileConsumer(hangulLayoutProfileSaver);
        this.englishLayoutProfileSaver =
                RuntimeDefaults.keyboardLayoutProfileConsumer(englishLayoutProfileSaver);
        this.ergonomicsOptionsSaver =
                RuntimeDefaults.keyboardErgonomicsConsumer(ergonomicsOptionsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
    }

    void addTo(LinearLayout root) {
        KeyboardSettings initialSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        handednessSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_handedness_label,
                SettingsRowBuilder.optionSpinner(
                        context,
                        HANDEDNESS_ORDER,
                        () -> !syncing,
                        mode -> settingsSaver.accept(
                                RuntimeDefaults.keyboardSettingsFrom(settings)
                                        .withHandednessPreset(mode))),
                12);

        hangulLayoutProfileSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_hangul_layout_label,
                layoutProfileSpinner(profile -> {
                    hangulLayoutProfileSaver.accept(profile);
                    controlsSyncer.run();
                }),
                16);

        englishLayoutProfileSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_english_layout_label,
                layoutProfileSpinner(profile -> {
                    englishLayoutProfileSaver.accept(profile);
                    controlsSyncer.run();
                }),
                8);

        ergonomicsSettingsController = new ErgonomicsSettingsController(
                context,
                () -> RuntimeDefaults.keyboardErgonomicsFrom(ergonomicsOptions),
                ergonomicsOptionsSaver);
        ergonomicsSettingsController.addTo(root);

        sharedPaddingValue = SettingsRowBuilder.valueLabelRow(context, root, 12);
        sharedPaddingInput = addNumericStepper(
                root,
                initialSettings.leftMarginDp,
                KeyboardSettings.MAX_MARGIN_DP,
                value -> settingsSaver.accept(
                        RuntimeDefaults.keyboardSettingsFrom(settings)
                                .withSharedMargin(value)));

        keyboardTopPaddingValue = SettingsRowBuilder.valueLabelRow(context, root, 12);
        keyboardTopPaddingInput = addNumericStepper(
                root,
                initialSettings.keyboardTopPaddingDp,
                KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP,
                value -> {
                    KeyboardSettings safe = RuntimeDefaults.keyboardSettingsFrom(settings);
                    settingsSaver.accept(safe.withLayoutSpacing(
                            safe.hangulMainSpecialGapDp,
                            value,
                            safe.keyboardBottomPaddingDp,
                            safe.bottomRowTopPaddingDp));
                });

        keyboardBottomPaddingValue = SettingsRowBuilder.valueLabelRow(context, root, 12);
        keyboardBottomPaddingInput = addNumericStepper(
                root,
                initialSettings.keyboardBottomPaddingDp,
                KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP,
                value -> {
                    KeyboardSettings safe = RuntimeDefaults.keyboardSettingsFrom(settings);
                    settingsSaver.accept(safe.withLayoutSpacing(
                            safe.hangulMainSpecialGapDp,
                            safe.keyboardTopPaddingDp,
                            value,
                            safe.bottomRowTopPaddingDp));
                });

        hangulNumberRowCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_hangul_number_row,
                16,
                () -> !syncing,
                isChecked -> settingsSaver.accept(
                        RuntimeDefaults.keyboardSettingsFrom(settings)
                                .withHangulNumberRow(isChecked)));

        englishNumberRowCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_english_number_row,
                8,
                () -> !syncing,
                isChecked -> settingsSaver.accept(
                        RuntimeDefaults.keyboardSettingsFrom(settings)
                                .withEnglishNumberRow(isChecked)));

        numberRowBottomGapValue = SettingsRowBuilder.valueLabelRow(context, root, 12);
        numberRowBottomGapInput = addNumericStepper(
                root,
                initialSettings.numberRowBottomGapDp,
                KeyboardSettings.MAX_NUMBER_ROW_BOTTOM_GAP_DP,
                value -> settingsSaver.accept(
                        RuntimeDefaults.keyboardSettingsFrom(settings)
                                .withNumberRowBottomGap(value)));

        hangulKeyGapValue = SettingsRowBuilder.valueLabelRow(context, root, 12);
        hangulKeyGapInput = addNumericStepper(
                root,
                initialSettings.hangulKeyGapDp,
                KeyboardSettings.MAX_KEY_GAP_DP,
                value -> {
                    currentThemeCustomMarker.run();
                    settingsSaver.accept(
                            RuntimeDefaults.keyboardSettingsFrom(settings)
                                    .withHangulKeyGap(value));
                });

        englishKeyGapValue = SettingsRowBuilder.valueLabelRow(context, root, 8);
        englishKeyGapInput = addNumericStepper(
                root,
                initialSettings.englishKeyGapDp,
                KeyboardSettings.MAX_KEY_GAP_DP,
                value -> {
                    currentThemeCustomMarker.run();
                    settingsSaver.accept(
                            RuntimeDefaults.keyboardSettingsFrom(settings)
                                    .withEnglishKeyGap(value));
                });

        hangulHeightValue = SettingsRowBuilder.valueLabel(context);
        hangulHeightSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                hangulHeightValue,
                KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP,
                16,
                () -> !syncing,
                progress -> {
                    currentThemeCustomMarker.run();
                    settingsSaver.accept(
                            RuntimeDefaults.keyboardSettingsFrom(settings)
                                    .withHangulHeight(KeyboardSettings.MIN_HEIGHT_DP + progress));
                });

        englishHeightValue = SettingsRowBuilder.valueLabel(context);
        englishHeightSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                englishHeightValue,
                KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP,
                8,
                () -> !syncing,
                progress -> {
                    currentThemeCustomMarker.run();
                    settingsSaver.accept(
                            RuntimeDefaults.keyboardSettingsFrom(settings)
                                    .withEnglishHeight(KeyboardSettings.MIN_HEIGHT_DP + progress));
                });

        hangulSpecialColumnValue = SettingsRowBuilder.valueLabel(context);
        hangulSpecialColumnSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                hangulSpecialColumnValue,
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT
                        - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                12,
                () -> !syncing,
                progress -> {
                    settingsSaver.accept(
                            RuntimeDefaults.keyboardSettingsFrom(settings)
                                    .withHangulSpecialColumnPercent(
                                            KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT + progress));
                });
    }

    void sync(KeyboardSettings settings, KeyboardLayoutProfiles layoutProfiles) {
        if (handednessSpinner == null) {
            return;
        }
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        KeyboardLayoutProfiles safeProfiles = RuntimeDefaults.keyboardLayoutProfiles(layoutProfiles);

        syncing = true;
        if (ergonomicsSettingsController != null) {
            ergonomicsSettingsController.sync();
        }
        handednessSpinner.setSelection(HandednessMode.indexOf(safeSettings.handednessMode));
        hangulLayoutProfileSpinner.setSelection(KeyboardLayoutProfile.indexOf(safeProfiles.hangulLayout));
        englishLayoutProfileSpinner.setSelection(KeyboardLayoutProfile.indexOf(safeProfiles.englishLayout));
        hangulHeightSeekBar.setProgress(safeSettings.hangulKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        englishHeightSeekBar.setProgress(safeSettings.englishKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        hangulSpecialColumnSeekBar.setProgress(
                safeSettings.hangulSpecialColumnPercent - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        setNumericText(sharedPaddingInput, safeSettings.leftMarginDp);
        setNumericText(keyboardTopPaddingInput, safeSettings.keyboardTopPaddingDp);
        setNumericText(keyboardBottomPaddingInput, safeSettings.keyboardBottomPaddingDp);
        setNumericText(numberRowBottomGapInput, safeSettings.numberRowBottomGapDp);
        setNumericText(hangulKeyGapInput, safeSettings.hangulKeyGapDp);
        setNumericText(englishKeyGapInput, safeSettings.englishKeyGapDp);
        hangulNumberRowCheckBox.setChecked(safeSettings.showHangulNumberRow);
        englishNumberRowCheckBox.setChecked(safeSettings.showEnglishNumberRow);
        sharedPaddingValue.setText(SettingsValueFormatter.sharedPadding(context, safeSettings.leftMarginDp));
        hangulHeightValue.setText(SettingsValueFormatter.hangulHeight(context, safeSettings));
        englishHeightValue.setText(SettingsValueFormatter.englishHeight(context, safeSettings));
        hangulSpecialColumnValue.setText(SettingsValueFormatter.hangulSpecialColumn(
                context,
                safeSettings.hangulSpecialColumnPercent));
        keyboardTopPaddingValue.setText(SettingsValueFormatter.keyboardTopPadding(
                context,
                safeSettings.keyboardTopPaddingDp));
        keyboardBottomPaddingValue.setText(SettingsValueFormatter.keyboardBottomPadding(
                context,
                safeSettings.keyboardBottomPaddingDp));
        numberRowBottomGapValue.setText(SettingsValueFormatter.numberRowGap(
                context,
                safeSettings.numberRowBottomGapDp));
        hangulKeyGapValue.setText(SettingsValueFormatter.hangulKeyGap(context, safeSettings.hangulKeyGapDp));
        englishKeyGapValue.setText(SettingsValueFormatter.englishKeyGap(context, safeSettings.englishKeyGapDp));
        handednessSpinner.post(() -> syncing = false);
    }

    private EditText addNumericStepper(
            LinearLayout root,
            int initialValue,
            int maxValue,
            IntConsumer listener) {
        IntConsumer safeListener = RuntimeDefaults.intConsumer(listener);
        NumericStepperRow row = new NumericStepperRow(
                context,
                initialValue,
                maxValue,
                value -> {
                    if (!syncing) {
                        safeListener.accept(value);
                    }
                });
        root.addView(row, SettingsRowBuilder.matchWrap());
        return row.input();
    }

    private void setNumericText(EditText input, int value) {
        if (input == null) {
            return;
        }
        String text = String.valueOf(value);
        if (!text.contentEquals(input.getText())) {
            input.setText(text);
        }
    }

    private Spinner layoutProfileSpinner(Consumer<KeyboardLayoutProfile> listener) {
        return SettingsRowBuilder.optionSpinner(
                context,
                LAYOUT_PROFILE_ORDER,
                () -> !syncing,
                listener);
    }

}
