package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ThemeHubSettingsController {
    private final Activity activity;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;

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
                v -> resetThemeAppearanceToDefault());
    }

    private void resetThemeAppearanceToDefault() {
        currentThemeCustomMarker.run();
        settingsSaver.accept(ThemeOption.resetToDefaultAppearance(
                RuntimeDefaults.keyboardSettingsFrom(settings)));
    }
}
