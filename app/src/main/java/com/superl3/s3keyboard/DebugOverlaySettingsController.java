package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;

final class DebugOverlaySettingsController {
    private final Context context;
    private final Runnable onChanged;
    private CheckBox boundsOverlayCheckBox;
    private CheckBox resolverScoresCheckBox;
    private boolean syncing;

    DebugOverlaySettingsController(Context context, Runnable onChanged) {
        this.context = context;
        this.onChanged = RuntimeDefaults.runnable(onChanged);
    }

    void addTo(LinearLayout root) {
        boundsOverlayCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_debug_key_bounds_overlay,
                12,
                () -> !syncing,
                this::saveDebugOverlay);
        resolverScoresCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_debug_resolver_scores,
                8,
                () -> !syncing,
                this::saveResolverScores);
    }

    void sync() {
        if (boundsOverlayCheckBox == null || resolverScoresCheckBox == null) {
            return;
        }
        syncing = true;
        boolean overlayEnabled = KeyboardPreferences.loadDebugKeyBoundsOverlayEnabled(context);
        boundsOverlayCheckBox.setChecked(overlayEnabled);
        resolverScoresCheckBox.setChecked(KeyboardPreferences.loadDebugShowResolverScores(context));
        resolverScoresCheckBox.setEnabled(overlayEnabled);
        syncing = false;
    }

    private void saveDebugOverlay(boolean enabled) {
        InputAssistanceSettingsController.saveDebugOverlay(context, enabled);
        onChanged.run();
    }

    private void saveResolverScores(boolean enabled) {
        KeyboardPreferences.saveDebugShowResolverScores(context, enabled);
        onChanged.run();
    }
}
