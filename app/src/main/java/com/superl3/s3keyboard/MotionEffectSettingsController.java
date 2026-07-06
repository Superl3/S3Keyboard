package com.superl3.s3keyboard;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Spinner;

final class MotionEffectSettingsController {
    private static final MotionEffectLevel[] MOTION_EFFECT_ORDER =
            MotionEffectLevel.displayOrder();

    private final Activity activity;
    private final Runnable onChanged;
    private Spinner motionEffectLevelSpinner;
    private boolean syncing = true;

    MotionEffectSettingsController(Activity activity, Runnable onChanged) {
        this.activity = activity;
        this.onChanged = RuntimeDefaults.runnable(onChanged);
    }

    void addTo(LinearLayout root) {
        motionEffectLevelSpinner = SettingsRowBuilder.labeledControl(
                activity,
                root,
                R.string.settings_motion_effect_level,
                motionEffectLevelSpinner(),
                12);
    }

    void sync() {
        if (motionEffectLevelSpinner == null) {
            return;
        }
        syncing = true;
        motionEffectLevelSpinner.setSelection(
                MotionEffectLevel.indexOf(KeyboardPreferences.loadMotionEffectLevel(activity)));
        syncing = false;
    }

    private Spinner motionEffectLevelSpinner() {
        return SettingsRowBuilder.optionSpinner(
                activity,
                MOTION_EFFECT_ORDER,
                () -> !syncing,
                level -> {
                    KeyboardPreferences.saveMotionEffectLevel(
                            activity,
                            level);
                    onChanged.run();
                });
    }
}
