package com.superl3.s3keyboard;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

final class GlassSourceSettingsController {
    private final Activity activity;
    private CheckBox enabledCheckBox;
    private TextView statusLabel;
    private boolean syncing;

    GlassSourceSettingsController(Activity activity) {
        this.activity = activity;
    }

    void addTo(LinearLayout root) {
        enabledCheckBox = SettingsRowBuilder.checkBoxRow(
                activity,
                root,
                R.string.settings_glass_source_enabled,
                12,
                () -> !syncing && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
                this::setEnabled);
        SettingsRowBuilder.secondaryLabelRow(
                activity,
                root,
                R.string.settings_glass_source_description,
                4);
        statusLabel = SettingsRowBuilder.secondaryLabel(activity, "");
        root.addView(statusLabel, SettingsRowBuilder.matchWrapWithTop(activity, 8));
        SettingsRowBuilder.buttonRow(
                activity,
                root,
                R.string.settings_open_accessibility,
                8,
                view -> openAccessibilitySettings());
        sync();
    }

    void sync() {
        if (enabledCheckBox == null) {
            return;
        }
        boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
        boolean requested = GlassBackdropPreferences.isSourceEnabled(activity);
        boolean connected = isServiceEnabled(activity);
        syncing = true;
        enabledCheckBox.setEnabled(supported);
        enabledCheckBox.setChecked(supported && requested);
        statusLabel.setText(activity.getString(
                !supported
                        ? R.string.settings_glass_source_unsupported
                        : connected
                                ? R.string.settings_glass_source_connected
                                : R.string.settings_glass_source_disconnected));
        syncing = false;
    }

    private void setEnabled(boolean enabled) {
        GlassBackdropPreferences.setSourceEnabled(activity, enabled);
        if (enabled && !isServiceEnabled(activity)) {
            openAccessibilitySettings();
        }
        sync();
    }

    private void openAccessibilitySettings() {
        activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    static boolean isServiceEnabled(Context context) {
        AccessibilityManager manager = context.getSystemService(AccessibilityManager.class);
        if (manager == null) {
            return false;
        }
        ComponentName expected = new ComponentName(context, GlassCaptureAccessibilityService.class);
        List<AccessibilityServiceInfo> services = manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : services) {
            if (info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null) {
                continue;
            }
            android.content.pm.ServiceInfo serviceInfo = info.getResolveInfo().serviceInfo;
            if (expected.getPackageName().equals(serviceInfo.packageName)
                    && expected.getClassName().equals(serviceInfo.name)) {
                return true;
            }
        }
        return false;
    }
}
