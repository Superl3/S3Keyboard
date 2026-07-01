package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

final class DebugOverlaySettingsController {
    private final Context context;
    private final Runnable onChanged;
    private CheckBox boundsOverlayCheckBox;
    private CheckBox resolverScoresCheckBox;
    private boolean syncing;

    DebugOverlaySettingsController(Context context, Runnable onChanged) {
        this.context = context;
        this.onChanged = onChanged;
    }

    void addTo(LinearLayout root) {
        boundsOverlayCheckBox = checkBox(R.string.settings_debug_key_bounds_overlay);
        boundsOverlayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    InputAssistanceSettingsController.saveDebugOverlay(context, isChecked);
                    notifyChanged();
                }
            }
        });
        root.addView(boundsOverlayCheckBox, matchWrapWithTop(12));

        resolverScoresCheckBox = checkBox(R.string.settings_debug_resolver_scores);
        resolverScoresCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    KeyboardPreferences.saveDebugShowResolverScores(context, isChecked);
                    notifyChanged();
                }
            }
        });
        root.addView(resolverScoresCheckBox, matchWrapWithTop(8));
    }

    void sync() {
        if (boundsOverlayCheckBox == null || resolverScoresCheckBox == null) {
            return;
        }
        syncing = true;
        SettingsViewStyler.compoundButton(boundsOverlayCheckBox, context);
        SettingsViewStyler.compoundButton(resolverScoresCheckBox, context);
        boolean overlayEnabled = KeyboardPreferences.loadDebugKeyBoundsOverlayEnabled(context);
        boundsOverlayCheckBox.setChecked(overlayEnabled);
        resolverScoresCheckBox.setChecked(KeyboardPreferences.loadDebugShowResolverScores(context));
        resolverScoresCheckBox.setEnabled(overlayEnabled);
        syncing = false;
    }

    private CheckBox checkBox(int labelResId) {
        CheckBox checkBox = new CheckBox(context);
        checkBox.setText(labelResId);
        SettingsViewStyler.compoundButton(checkBox, context);
        return checkBox;
    }

    private void notifyChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = Math.round(topMarginDp * context.getResources().getDisplayMetrics().density);
        return params;
    }
}
