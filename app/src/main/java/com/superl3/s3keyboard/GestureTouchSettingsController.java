package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

final class GestureTouchSettingsController {
    interface Host {
        KeyboardSettings settings();

        void saveSettings(KeyboardSettings settings);

        void syncControls();
    }

    private final Context context;
    private final Host host;
    private TextView gestureThresholdValue;
    private SeekBar gestureThresholdSeekBar;
    private Spinner vowelProfileSpinner;
    private TextView spacebarDeadZoneValue;
    private SeekBar spacebarDeadZoneSeekBar;
    private TextView touchYOffsetValue;
    private SeekBar touchYOffsetSeekBar;
    private boolean syncing = true;

    GestureTouchSettingsController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void addTo(LinearLayout root) {
        gestureThresholdValue = SettingsRowBuilder.label(context, "");
        gestureThresholdSeekBar = seekBar(
                KeyboardSettings.MAX_GESTURE_THRESHOLD_DP - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
        gestureThresholdSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            void onUserProgressChanged(int progress) {
                host.saveSettings(host.settings().withGestureThreshold(
                        KeyboardSettings.MIN_GESTURE_THRESHOLD_DP + progress));
            }
        });
        root.addView(gestureThresholdValue, matchWrapWithTop(12));
        root.addView(gestureThresholdSeekBar, matchWrap());

        root.addView(
                SettingsRowBuilder.label(context, context.getString(R.string.settings_dingul_vowel_gesture_profile)),
                matchWrapWithTop(12));
        vowelProfileSpinner = new Spinner(context);
        vowelProfileSpinner.setAdapter(new SettingsArrayAdapter<>(
                context,
                SettingsDisplayLabels.labels(context, DingulVowelGestureProfile.values())));
        vowelProfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) {
                    return;
                }
                DingulVowelGestureProfile[] profiles = DingulVowelGestureProfile.values();
                if (position >= 0 && position < profiles.length) {
                    KeyboardPreferences.saveDingulVowelGestureProfile(context, profiles[position]);
                    host.syncControls();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(vowelProfileSpinner, matchWrap());

        spacebarDeadZoneValue = SettingsRowBuilder.label(context, "");
        spacebarDeadZoneSeekBar = seekBar(
                KeyboardPreferences.MAX_SPACEBAR_CURSOR_DEAD_ZONE_DP
                        - KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP);
        spacebarDeadZoneSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            void onUserProgressChanged(int progress) {
                KeyboardPreferences.saveSpacebarCursorDeadZoneDp(
                        context,
                        KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP + progress);
                host.syncControls();
            }
        });
        root.addView(spacebarDeadZoneValue, matchWrapWithTop(12));
        root.addView(spacebarDeadZoneSeekBar, matchWrap());

        touchYOffsetValue = SettingsRowBuilder.label(context, "");
        touchYOffsetSeekBar = seekBar(
                KeyboardSettings.MAX_TOUCH_Y_OFFSET_DP - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP);
        touchYOffsetSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            void onUserProgressChanged(int progress) {
                host.saveSettings(host.settings().withTouchYOffset(
                        KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP + progress));
            }
        });
        root.addView(touchYOffsetValue, matchWrapWithTop(12));
        root.addView(touchYOffsetSeekBar, matchWrap());
    }

    void sync(KeyboardSettings settings) {
        if (gestureThresholdSeekBar == null) {
            return;
        }
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
        int deadZoneDp = KeyboardPreferences.loadSpacebarCursorDeadZoneDp(context);

        syncing = true;
        gestureThresholdSeekBar.setProgress(
                safe.gestureThresholdDp - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
        vowelProfileSpinner.setSelection(KeyboardPreferences.loadDingulVowelGestureProfile(context).ordinal());
        spacebarDeadZoneSeekBar.setProgress(
                deadZoneDp - KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP);
        touchYOffsetSeekBar.setProgress(safe.touchYOffsetDp - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP);
        gestureThresholdValue.setText(SettingsValueFormatter.gestureThreshold(
                context,
                safe.gestureThresholdDp));
        spacebarDeadZoneValue.setText(SettingsValueFormatter.spacebarCursorDeadZone(context, deadZoneDp));
        touchYOffsetValue.setText(SettingsValueFormatter.touchYOffset(context, safe.touchYOffsetDp));
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
