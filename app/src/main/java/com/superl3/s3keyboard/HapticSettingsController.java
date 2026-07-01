package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

final class HapticSettingsController {
    interface Host {
        KeyboardSettings settings();

        void saveSettings(KeyboardSettings settings);

        void syncControls();
    }

    private final Context context;
    private final Host host;
    private CheckBox hapticCheckBox;
    private CheckBox differentiatedCheckBox;
    private TextView durationValue;
    private SeekBar durationSeekBar;
    private TextView gapValue;
    private SeekBar gapSeekBar;
    private boolean syncing;

    HapticSettingsController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void addTo(LinearLayout root) {
        hapticCheckBox = checkBox(R.string.settings_haptic_feedback);
        hapticCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    host.saveSettings(host.settings().withHapticFeedback(isChecked));
                }
            }
        });
        root.addView(hapticCheckBox, matchWrapWithTop(8));

        differentiatedCheckBox = checkBox(R.string.settings_differentiated_haptic);
        differentiatedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    KeyboardPreferences.saveDifferentiatedHapticEnabled(context, isChecked);
                    host.syncControls();
                }
            }
        });
        root.addView(differentiatedCheckBox, matchWrapWithTop(4));

        durationValue = SettingsRowBuilder.label(context, "");
        durationSeekBar = seekBar(
                KeyboardPreferences.MAX_HAPTIC_TICK_DURATION_MS
                        - KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS);
        durationSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onUserProgressChanged(int progress) {
                KeyboardPreferences.saveHapticTickDurationMs(
                        context,
                        KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS + progress);
                host.syncControls();
            }
        });
        root.addView(durationValue, matchWrapWithTop(12));
        root.addView(durationSeekBar, matchWrap());

        gapValue = SettingsRowBuilder.label(context, "");
        gapSeekBar = seekBar(
                KeyboardPreferences.MAX_HAPTIC_TICK_GAP_MS - KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS);
        gapSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onUserProgressChanged(int progress) {
                KeyboardPreferences.saveHapticTickGapMs(
                        context,
                        KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS + progress);
                host.syncControls();
            }
        });
        root.addView(gapValue, matchWrapWithTop(12));
        root.addView(gapSeekBar, matchWrap());
    }

    void sync(KeyboardSettings settings) {
        if (hapticCheckBox == null) {
            return;
        }
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
        int durationMs = KeyboardPreferences.loadHapticTickDurationMs(context);
        int gapMs = KeyboardPreferences.loadHapticTickGapMs(context);

        syncing = true;
        SettingsViewStyler.compoundButton(hapticCheckBox, context);
        SettingsViewStyler.compoundButton(differentiatedCheckBox, context);
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

    private CheckBox checkBox(int labelResId) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(labelResId);
        SettingsViewStyler.compoundButton(checkBox, context);
        return checkBox;
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
