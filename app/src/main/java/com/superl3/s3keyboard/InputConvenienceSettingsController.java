package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;

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
    private CheckBox englishSuggestionsCheckBox;
    private CheckBox englishAutoCorrectionCheckBox;
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
        englishSuggestionsCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.english_suggestions,
                8,
                () -> !syncing,
                this::saveEnglishSuggestions);
        englishAutoCorrectionCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.english_auto_correction,
                8,
                () -> !syncing,
                this::saveEnglishAutoCorrection);
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
        englishSuggestionsCheckBox.setChecked(
                KeyboardPreferences.loadEnglishSuggestionsEnabled(context));
        englishAutoCorrectionCheckBox.setChecked(
                KeyboardPreferences.loadEnglishAutoCorrectionEnabled(context));
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

    private void saveEnglishSuggestions(boolean enabled) {
        KeyboardPreferences.saveEnglishSuggestionsEnabled(context, enabled);
        controlsSyncer.run();
    }

    private void saveEnglishAutoCorrection(boolean enabled) {
        KeyboardPreferences.saveEnglishAutoCorrectionEnabled(context, enabled);
        controlsSyncer.run();
    }

    private void saveDoubleSpacePeriod(boolean enabled) {
        settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                .withEnglishDoubleSpacePeriod(enabled));
    }

}
