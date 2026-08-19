package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ThemeHubSettingsController {
    private final Activity activity;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private TextView currentThemeLabel;
    private String lastThemeId;

    ThemeHubSettingsController(
            Activity activity,
            Supplier<KeyboardSettings> settings,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver) {
        this.activity = activity;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
    }

    void addTo(LinearLayout root) {
        currentThemeLabel = SettingsRowBuilder.secondaryLabel(activity, "");
        root.addView(currentThemeLabel, SettingsRowBuilder.matchWrapWithTop(activity, 12));

        SettingsRowBuilder.iconButtonRow(
                activity,
                root,
                R.string.settings_theme_select,
                R.drawable.ic_keyboard_keyboard,
                12,
                v -> activity.startActivity(new Intent(activity, ThemeSelectorActivity.class)));

        SettingsRowBuilder.iconButtonRow(
                activity,
                root,
                R.string.settings_theme_edit,
                R.drawable.ic_keyboard_settings,
                12,
                v -> activity.startActivity(new Intent(activity, ThemeEditorActivity.class)));

        SettingsRowBuilder.iconButtonRow(
                activity,
                root,
                R.string.settings_reset_default_theme,
                R.drawable.ic_keyboard_reset,
                12,
                v -> ThemeResetConfirmation.show(activity, this::resetThemeAppearanceToDefault));
        sync();
    }

    void sync() {
        if (currentThemeLabel == null) {
            return;
        }
        String selectedThemeId = RuntimeDefaults.stringOrDefault(
                KeyboardPreferences.loadSelectedThemeId(activity),
                "");
        if (selectedThemeId.equals(lastThemeId)) {
            return;
        }
        lastThemeId = selectedThemeId;
        ThemeOption[] options = ThemeOption.buildOptions(
                activity,
                UserThemeStore.load(activity),
                ExternalThemeStore.load(activity),
                true);
        ThemeOption selected = ThemeOption.at(
                options,
                ThemeOption.indexOfStableId(
                        options,
                        selectedThemeId,
                        0));
        currentThemeLabel.setText(activity.getString(
                R.string.settings_current_theme_format,
                selected == null
                        ? activity.getString(R.string.theme_current_settings)
                        : selected.label));
    }

    private void resetThemeAppearanceToDefault() {
        currentThemeCustomMarker.run();
        settingsSaver.accept(ThemeOption.resetToDefaultAppearance(
                RuntimeDefaults.keyboardSettingsFrom(settings)));
    }
}
