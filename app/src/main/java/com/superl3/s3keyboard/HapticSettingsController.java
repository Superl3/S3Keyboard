package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class HapticSettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Runnable controlsSyncer;
    private CheckBox hapticCheckBox;
    private CheckBox differentiatedCheckBox;
    private TextView durationValue;
    private SeekBar durationSeekBar;
    private TextView gapValue;
    private SeekBar gapSeekBar;
    private boolean syncing;

    HapticSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver,
            Runnable controlsSyncer) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
    }

    void addTo(LinearLayout root) {
        hapticCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_haptic_feedback,
                8,
                () -> !syncing,
                this::saveHapticFeedback);

        differentiatedCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_differentiated_haptic,
                4,
                () -> !syncing,
                this::saveDifferentiatedHaptic);

        durationValue = SettingsRowBuilder.valueLabel(context);
        durationSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                durationValue,
                KeyboardPreferences.MAX_HAPTIC_TICK_DURATION_MS
                        - KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS,
                12,
                () -> !syncing,
                progress -> {
                    KeyboardPreferences.saveHapticTickDurationMs(
                            context,
                            KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS + progress);
                    controlsSyncer.run();
                });

        gapValue = SettingsRowBuilder.valueLabel(context);
        gapSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                gapValue,
                KeyboardPreferences.MAX_HAPTIC_TICK_GAP_MS - KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS,
                12,
                () -> !syncing,
                progress -> {
                    KeyboardPreferences.saveHapticTickGapMs(
                            context,
                            KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS + progress);
                    controlsSyncer.run();
                });
    }

    void sync(KeyboardSettings settings) {
        if (hapticCheckBox == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        int durationMs = KeyboardPreferences.loadHapticTickDurationMs(context);
        int gapMs = KeyboardPreferences.loadHapticTickGapMs(context);

        syncing = true;
        hapticCheckBox.setChecked(safe.hapticFeedbackEnabled);
        differentiatedCheckBox.setChecked(KeyboardPreferences.loadDifferentiatedHapticEnabled(context));
        durationSeekBar.setProgress(durationMs - KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS);
        gapSeekBar.setProgress(gapMs - KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS);
        durationSeekBar.setEnabled(safe.hapticFeedbackEnabled);
        gapSeekBar.setEnabled(safe.hapticFeedbackEnabled);
        differentiatedCheckBox.setEnabled(safe.hapticFeedbackEnabled);
        durationValue.setText(SettingsValueFormatter.hapticDuration(context, durationMs));
        gapValue.setText(SettingsValueFormatter.hapticGap(context, gapMs));
        syncing = false;
    }

    private void saveHapticFeedback(boolean isChecked) {
        settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings).withHapticFeedback(isChecked));
    }

    private void saveDifferentiatedHaptic(boolean isChecked) {
        KeyboardPreferences.saveDifferentiatedHapticEnabled(context, isChecked);
        controlsSyncer.run();
    }

}
