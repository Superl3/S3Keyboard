package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

final class RepeatSettingsController {
    interface Host {
        KeyboardSettings settings();

        void saveSettings(KeyboardSettings settings);
    }

    private final Context context;
    private final Host host;
    private TextView startDelayValue;
    private SeekBar startDelaySeekBar;
    private TextView intervalValue;
    private SeekBar intervalSeekBar;
    private boolean syncing;

    RepeatSettingsController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void addTo(LinearLayout root) {
        startDelayValue = SettingsRowBuilder.label(context, "");
        startDelaySeekBar = seekBar(
                KeyboardSettings.MAX_REPEAT_START_DELAY_MS - KeyboardSettings.MIN_REPEAT_START_DELAY_MS);
        startDelaySeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            void onUserProgressChanged(int progress) {
                KeyboardSettings settings = host.settings();
                host.saveSettings(settings.withRepeatTiming(
                        KeyboardSettings.MIN_REPEAT_START_DELAY_MS + progress,
                        settings.repeatIntervalMs));
            }
        });
        root.addView(startDelayValue, matchWrapWithTop(12));
        root.addView(startDelaySeekBar, matchWrap());

        intervalValue = SettingsRowBuilder.label(context, "");
        intervalSeekBar = seekBar(
                KeyboardSettings.MAX_REPEAT_INTERVAL_MS - KeyboardSettings.MIN_REPEAT_INTERVAL_MS);
        intervalSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            void onUserProgressChanged(int progress) {
                KeyboardSettings settings = host.settings();
                host.saveSettings(settings.withRepeatTiming(
                        settings.repeatStartDelayMs,
                        KeyboardSettings.MIN_REPEAT_INTERVAL_MS + progress));
            }
        });
        root.addView(intervalValue, matchWrapWithTop(8));
        root.addView(intervalSeekBar, matchWrap());
    }

    void sync(KeyboardSettings settings) {
        if (startDelaySeekBar == null || intervalSeekBar == null) {
            return;
        }
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
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

    private SeekBar seekBar(int max) {
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(max);
        return seekBar;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = Math.round(topMarginDp * context.getResources().getDisplayMetrics().density);
        return params;
    }

    private abstract class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public final void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && !syncing) {
                onUserProgressChanged(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        abstract void onUserProgressChanged(int progress);
    }
}
