package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.function.BooleanSupplier;

final class AndroidImeSettingsController {
    private final Activity activity;
    private final BooleanSupplier debuggableBuild;
    private final Runnable onChanged;
    private DebugOverlaySettingsController debugOverlaySettingsController;

    AndroidImeSettingsController(
            Activity activity,
            BooleanSupplier debuggableBuild,
            Runnable onChanged) {
        this.activity = activity;
        this.debuggableBuild = RuntimeDefaults.booleanSupplier(debuggableBuild);
        this.onChanged = RuntimeDefaults.runnable(onChanged);
    }

    void addTo(LinearLayout root) {
        SettingsRowBuilder.iconButtonRow(
                activity,
                root,
                R.string.open_input_settings,
                R.drawable.ic_keyboard_settings,
                12,
                v -> activity.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

        SettingsRowBuilder.iconButtonRow(
                activity,
                root,
                R.string.show_input_picker,
                R.drawable.ic_keyboard_keyboard,
                12,
                v -> {
                    InputMethodManager imm = activity.getSystemService(InputMethodManager.class);
                    if (imm != null) {
                        imm.showInputMethodPicker();
                    }
                });

        if (debuggableBuild.getAsBoolean()) {
            debugOverlaySettingsController =
                    new DebugOverlaySettingsController(activity, onChanged);
            debugOverlaySettingsController.addTo(root);
        }
    }

    void sync() {
        if (debugOverlaySettingsController != null) {
            debugOverlaySettingsController.sync();
        }
    }
}
