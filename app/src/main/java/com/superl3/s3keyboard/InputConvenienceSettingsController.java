package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class InputConvenienceSettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Runnable controlsSyncer;
    private final Supplier<LocalDataControlsController> localDataControls;
    private CheckBox touchBiasAutoCorrectionCheckBox;
    private CheckBox palmRejectionCheckBox;
    private CheckBox clipboardHistoryCheckBox;
    private CheckBox singleTapCommitModeCheckBox;
    private TextView singleTapStartHoldValue;
    private SeekBar singleTapStartHoldSeekBar;
    private TextView singleTapCommitHoldValue;
    private SeekBar singleTapCommitHoldSeekBar;
    private CheckBox doubleSpacePeriodCheckBox;
    private boolean syncing;

    InputConvenienceSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver,
            Runnable controlsSyncer,
            Supplier<LocalDataControlsController> localDataControls) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
        this.localDataControls = RuntimeDefaults.localDataControlsSupplier(context, localDataControls);
    }

    void addTo(LinearLayout root) {
        touchBiasAutoCorrectionCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.touch_bias_auto_correction,
                8,
                () -> !syncing,
                this::saveTouchBiasAutoCorrection);
        palmRejectionCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.palm_rejection,
                8,
                () -> !syncing,
                this::savePalmRejection);
        clipboardHistoryCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.clipboard_history_setting,
                8,
                () -> !syncing,
                this::saveClipboardHistory);
        singleTapCommitModeCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.single_tap_commit_mode,
                8,
                () -> !syncing,
                this::saveSingleTapCommitMode);
        singleTapStartHoldValue = SettingsRowBuilder.valueLabel(context);
        singleTapStartHoldSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                singleTapStartHoldValue,
                KeyboardPreferences.MAX_SINGLE_TAP_HOLD_MS - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS,
                6,
                () -> !syncing,
                progress -> saveSingleTapStartHold(
                        KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS + progress));
        singleTapCommitHoldValue = SettingsRowBuilder.valueLabel(context);
        singleTapCommitHoldSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                singleTapCommitHoldValue,
                KeyboardPreferences.MAX_SINGLE_TAP_HOLD_MS - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS,
                6,
                () -> !syncing,
                progress -> saveSingleTapCommitHold(
                        KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS + progress));
        doubleSpacePeriodCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.english_double_space_period,
                8,
                () -> !syncing,
                this::saveDoubleSpacePeriod);
    }

    void sync(KeyboardSettings settings) {
        if (touchBiasAutoCorrectionCheckBox == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);

        syncing = true;
        touchBiasAutoCorrectionCheckBox.setChecked(
                KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(context));
        palmRejectionCheckBox.setChecked(KeyboardPreferences.loadPalmRejectionEnabled(context));
        clipboardHistoryCheckBox.setChecked(localDataControls.get().clipboardHistoryEnabled());
        singleTapCommitModeCheckBox.setChecked(KeyboardPreferences.loadSingleTapCommitModeEnabled(context));
        int startHoldMs = KeyboardPreferences.loadSingleTapStartHoldMs(context);
        int commitHoldMs = KeyboardPreferences.loadSingleTapCommitHoldMs(context);
        singleTapStartHoldSeekBar.setProgress(startHoldMs - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS);
        singleTapCommitHoldSeekBar.setProgress(commitHoldMs - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS);
        singleTapStartHoldValue.setText(SettingsValueFormatter.singleTapStartHold(context, startHoldMs));
        singleTapCommitHoldValue.setText(SettingsValueFormatter.singleTapCommitHold(context, commitHoldMs));
        doubleSpacePeriodCheckBox.setChecked(safe.englishDoubleSpacePeriodEnabled);
        syncing = false;
    }


    private void saveTouchBiasAutoCorrection(boolean enabled) {
        KeyboardPreferences.saveTouchBiasAutoCorrectionEnabled(context, enabled);
        controlsSyncer.run();
    }

    private void savePalmRejection(boolean enabled) {
        KeyboardPreferences.savePalmRejectionEnabled(context, enabled);
        controlsSyncer.run();
    }

    private void saveClipboardHistory(boolean enabled) {
        localDataControls.get().setClipboardHistoryEnabled(enabled);
        controlsSyncer.run();
    }

    private void saveSingleTapCommitMode(boolean enabled) {
        KeyboardPreferences.saveSingleTapCommitModeEnabled(context, enabled);
        controlsSyncer.run();
    }

    private void saveSingleTapStartHold(int valueMs) {
        KeyboardPreferences.saveSingleTapStartHoldMs(context, valueMs);
        controlsSyncer.run();
    }

    private void saveSingleTapCommitHold(int valueMs) {
        KeyboardPreferences.saveSingleTapCommitHoldMs(context, valueMs);
        controlsSyncer.run();
    }

    private void saveDoubleSpacePeriod(boolean enabled) {
        settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                .withEnglishDoubleSpacePeriod(enabled));
    }

}
