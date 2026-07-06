package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class RepeatSettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private TextView startDelayValue;
    private SeekBar startDelaySeekBar;
    private TextView intervalValue;
    private SeekBar intervalSeekBar;
    private boolean syncing;

    RepeatSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
    }

    void addTo(LinearLayout root) {
        startDelayValue = SettingsRowBuilder.valueLabel(context);
        startDelaySeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                startDelayValue,
                KeyboardSettings.MAX_REPEAT_START_DELAY_MS - KeyboardSettings.MIN_REPEAT_START_DELAY_MS,
                12,
                () -> !syncing,
                progress -> {
                    KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
                    settingsSaver.accept(settings.withRepeatTiming(
                            KeyboardSettings.MIN_REPEAT_START_DELAY_MS + progress,
                            settings.repeatIntervalMs));
                });

        intervalValue = SettingsRowBuilder.valueLabel(context);
        intervalSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                intervalValue,
                KeyboardSettings.MAX_REPEAT_INTERVAL_MS - KeyboardSettings.MIN_REPEAT_INTERVAL_MS,
                8,
                () -> !syncing,
                progress -> {
                    KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
                    settingsSaver.accept(settings.withRepeatTiming(
                            settings.repeatStartDelayMs,
                            KeyboardSettings.MIN_REPEAT_INTERVAL_MS + progress));
                });
    }

    void sync(KeyboardSettings settings) {
        if (startDelaySeekBar == null || intervalSeekBar == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        syncing = true;
        startDelaySeekBar.setProgress(
                safe.repeatStartDelayMs - KeyboardSettings.MIN_REPEAT_START_DELAY_MS);
        intervalSeekBar.setProgress(
                safe.repeatIntervalMs - KeyboardSettings.MIN_REPEAT_INTERVAL_MS);
        startDelayValue.setText(SettingsValueFormatter.repeatStartDelay(
                context,
                safe.repeatStartDelayMs));
        intervalValue.setText(SettingsValueFormatter.repeatInterval(context, safe.repeatIntervalMs));
        syncing = false;
    }

}
