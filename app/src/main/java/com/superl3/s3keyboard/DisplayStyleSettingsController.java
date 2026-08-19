package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

final class DisplayStyleSettingsController {
    private final Activity activity;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private Spinner modifierIconPackSpinner;
    private Spinner keyDisplayPackSpinner;
    private CheckBox pointKeycapStyleCheckBox;
    private boolean syncing = true;

    DisplayStyleSettingsController(
            Activity activity,
            Supplier<KeyboardSettings> settings,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver) {
        this.activity = activity;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
    }

    void addPackControlsTo(LinearLayout root) {
        modifierIconPackSpinner = SettingsRowBuilder.labeledControl(
                activity,
                root,
                R.string.settings_icon_style,
                modifierIconPackSpinner(),
                8);

        keyDisplayPackSpinner = SettingsRowBuilder.labeledControl(
                activity,
                root,
                R.string.settings_display_style,
                keyDisplayPackSpinner(),
                12);

        SettingsRowBuilder.buttonRow(
                activity,
                root,
                R.string.settings_visual_role_edit,
                12,
                v -> activity.startActivity(new Intent(activity, AccentPlacementActivity.class)));
    }

    void addPointKeycapControlTo(LinearLayout root) {
        pointKeycapStyleCheckBox = SettingsRowBuilder.checkBoxRow(
                activity,
                root,
                R.string.settings_point_keycap_style,
                8,
                () -> !syncing,
                this::savePointKeycapStyle);
    }

    void sync(KeyboardSettings settings) {
        if (modifierIconPackSpinner == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        syncing = true;
        if (pointKeycapStyleCheckBox != null) {
            pointKeycapStyleCheckBox.setChecked(safe.pointKeycapStyleEnabled);
        }
        modifierIconPackSpinner.setSelection(
                ModifierIconCatalog.selectablePackIndexOf(safe.modifierIconOverridePackId, true));
        keyDisplayPackSpinner.setSelection(
                KeyDisplayOverridePackCatalog.selectablePackIndexOf(safe.keyDisplayOverridePackId, true));
        syncing = false;
    }

    private Spinner modifierIconPackSpinner() {
        String[] labels = ModifierIconCatalog.selectablePackLabels(
                true,
                activity.getString(R.string.settings_theme_default));
        return packSpinner(
                labels,
                position -> RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withModifierIconOverridePack(ModifierIconCatalog.selectablePackIdAt(
                                position,
                                true)));
    }

    private Spinner keyDisplayPackSpinner() {
        String[] labels = KeyDisplayOverridePackCatalog.selectablePackLabels(
                true,
                activity.getString(R.string.settings_theme_default));
        return packSpinner(
                labels,
                position -> RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withKeyDisplayOverridePack(KeyDisplayOverridePackCatalog.selectablePackIdAt(
                                position,
                                true)));
    }

    private Spinner packSpinner(String[] labels, IntFunction<KeyboardSettings> change) {
        return SettingsRowBuilder.spinner(
                activity,
                labels,
                () -> !syncing,
                position -> {
                    currentThemeCustomMarker.run();
                    settingsSaver.accept(change.apply(position));
                });
    }

    private void savePointKeycapStyle(boolean isChecked) {
        currentThemeCustomMarker.run();
        settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                .withPointKeycapStyle(isChecked));
    }

}
