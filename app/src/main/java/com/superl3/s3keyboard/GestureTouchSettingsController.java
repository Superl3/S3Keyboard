package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class GestureTouchSettingsController {
    private static final DingulVowelGestureProfile[] VOWEL_PROFILE_ORDER =
            DingulVowelGestureProfile.displayOrder();

    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Runnable controlsSyncer;
    private TextView hitSlopValue;
    private SeekBar hitSlopSeekBar;
    private TextView gestureThresholdValue;
    private SeekBar gestureThresholdSeekBar;
    private Spinner vowelProfileSpinner;
    private TextView spacebarDeadZoneValue;
    private SeekBar spacebarDeadZoneSeekBar;
    private TextView touchYOffsetValue;
    private SeekBar touchYOffsetSeekBar;
    private boolean syncing = true;

    GestureTouchSettingsController(
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
        hitSlopValue = SettingsRowBuilder.valueLabel(context);
        hitSlopSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                hitSlopValue,
                KeyboardSettings.MAX_HIT_SLOP_DP - KeyboardSettings.MIN_HIT_SLOP_DP,
                12,
                () -> !syncing,
                progress -> settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withHitSlop(KeyboardSettings.MIN_HIT_SLOP_DP + progress)));

        gestureThresholdValue = SettingsRowBuilder.valueLabel(context);
        gestureThresholdSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                gestureThresholdValue,
                KeyboardSettings.MAX_GESTURE_THRESHOLD_DP - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP,
                12,
                () -> !syncing,
                progress -> settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withGestureThreshold(KeyboardSettings.MIN_GESTURE_THRESHOLD_DP + progress)));

        vowelProfileSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_dingul_vowel_gesture_profile,
                SettingsRowBuilder.optionSpinner(
                        context,
                        VOWEL_PROFILE_ORDER,
                        () -> !syncing,
                        profile -> {
                            KeyboardPreferences.saveDingulVowelGestureProfile(
                                    context,
                                    profile);
                            controlsSyncer.run();
                        }),
                12);

        spacebarDeadZoneValue = SettingsRowBuilder.valueLabel(context);
        spacebarDeadZoneSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                spacebarDeadZoneValue,
                KeyboardPreferences.MAX_SPACEBAR_CURSOR_DEAD_ZONE_DP
                        - KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP,
                12,
                () -> !syncing,
                progress -> {
                    KeyboardPreferences.saveSpacebarCursorDeadZoneDp(
                            context,
                            KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP + progress);
                    controlsSyncer.run();
                });

        touchYOffsetValue = SettingsRowBuilder.valueLabel(context);
        touchYOffsetSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                root,
                touchYOffsetValue,
                KeyboardSettings.MAX_TOUCH_Y_OFFSET_DP - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP,
                12,
                () -> !syncing,
                progress -> settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withTouchYOffset(KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP + progress)));
    }

    void sync(KeyboardSettings settings) {
        if (gestureThresholdSeekBar == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        int deadZoneDp = KeyboardPreferences.loadSpacebarCursorDeadZoneDp(context);

        syncing = true;
        hitSlopSeekBar.setProgress(safe.hitSlopDp - KeyboardSettings.MIN_HIT_SLOP_DP);
        gestureThresholdSeekBar.setProgress(
                safe.gestureThresholdDp - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
        vowelProfileSpinner.setSelection(DingulVowelGestureProfile.indexOf(
                KeyboardPreferences.loadDingulVowelGestureProfile(context)));
        spacebarDeadZoneSeekBar.setProgress(
                deadZoneDp - KeyboardPreferences.MIN_SPACEBAR_CURSOR_DEAD_ZONE_DP);
        touchYOffsetSeekBar.setProgress(safe.touchYOffsetDp - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP);
        hitSlopValue.setText(SettingsValueFormatter.hitSlop(context, safe.hitSlopDp));
        gestureThresholdValue.setText(SettingsValueFormatter.gestureThreshold(
                context,
                safe.gestureThresholdDp));
        spacebarDeadZoneValue.setText(SettingsValueFormatter.spacebarCursorDeadZone(context, deadZoneDp));
        touchYOffsetValue.setText(SettingsValueFormatter.touchYOffset(context, safe.touchYOffsetDp));
        syncing = false;
    }

}
