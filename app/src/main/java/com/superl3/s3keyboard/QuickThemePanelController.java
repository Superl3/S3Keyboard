package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class QuickThemePanelController {
    private final Context context;
    private final Supplier<KeyboardSettings> currentSettings;
    private final Supplier<String> enterKeyLabel;
    private final BooleanSupplier forceNumberRow;
    private final Consumer<KeyboardSettings> runtimeSettingsApplier;
    private final Runnable dismissQuickSettings;

    QuickThemePanelController(
            Context context,
            Supplier<KeyboardSettings> currentSettings,
            Supplier<String> enterKeyLabel,
            BooleanSupplier forceNumberRow,
            Consumer<KeyboardSettings> runtimeSettingsApplier,
            Runnable dismissQuickSettings) {
        this.context = context;
        this.currentSettings = RuntimeDefaults.keyboardSettingsSupplier(currentSettings);
        this.enterKeyLabel = RuntimeDefaults.nullStringSupplier(enterKeyLabel);
        this.forceNumberRow = RuntimeDefaults.booleanSupplier(forceNumberRow);
        this.runtimeSettingsApplier = RuntimeDefaults.keyboardSettingsConsumer(runtimeSettingsApplier);
        this.dismissQuickSettings = RuntimeDefaults.runnable(dismissQuickSettings);
    }

    void addTo(LinearLayout panel) {
        if (panel == null) {
            return;
        }
        ThemeOption[] options = ThemeOption.buildOptions(
                context,
                UserThemeStore.load(context),
                ExternalThemeStore.load(context),
                true);
        if (options.length == 0) {
            return;
        }

        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.sectionLabel(context, R.string.quick_theme_label),
                10);

        Spinner spinner = SettingsRowBuilder.spinner(context, options);
        int selectedIndex = ThemeOption.indexOfStableId(
                options,
                KeyboardPreferences.loadSelectedThemeId(context));
        spinner.setSelection(selectedIndex, false);
        spinner.setOnItemSelectedListener(UserInputListeners.itemSelectedAfterInitialSelection(position -> {
            apply(ThemeOption.at(options, position));
        }));
        QuickPanelUi.addWithTop(context, panel, spinner, 4);
    }

    private void apply(ThemeOption option) {
        if (option == null || option.stableId().isEmpty()) {
            return;
        }
        try {
            KeyboardSettings runtimeSettings = RuntimeDefaults.keyboardSettingsFrom(currentSettings);
            KeyboardSettings storedSettings = KeyboardPreferences.load(context);
            KeyboardSettings savedSettings = option.applyTo(storedSettings);
            KeyboardPreferences.saveSelectedThemeId(context, option.stableId());
            savedSettings = KeyboardPreferences.applyAccentPlacementPolicy(context, savedSettings);
            KeyboardPreferences.saveSettings(context, savedSettings);
            runtimeSettings = RuntimeDefaults.withRuntimeImeState(
                    savedSettings,
                    runtimeSettings,
                    enterKeyLabel.get(),
                    forceNumberRow.getAsBoolean());
            runtimeSettingsApplier.accept(runtimeSettings);
            dismissQuickSettings.run();
        } catch (IllegalArgumentException exception) {
            Toast.makeText(context, R.string.quick_theme_apply_failed, Toast.LENGTH_SHORT).show();
        }
    }

}
