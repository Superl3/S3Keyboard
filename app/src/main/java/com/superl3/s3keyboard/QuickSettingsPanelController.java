package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class QuickSettingsPanelController {
    private final Context context;
    private final RemoteCompatibilityPanelController remoteCompatibilityPanelController;
    private final QuickThemePanelController quickThemePanelController;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<String> remoteModeToggleLabel;
    private final Runnable remoteModeToggler;
    private final Supplier<String> numberRowToggleLabel;
    private final BooleanSupplier activeNumberRowVisible;
    private final Runnable activeNumberRowToggler;
    private final Consumer<HandednessMode> handednessApplier;
    private final Runnable themeClipboardImporter;
    private final Runnable inputIssueReportCopier;
    private final Runnable dismissQuickSettings;

    QuickSettingsPanelController(
            Context context,
            RemoteCompatibilityPanelController remoteCompatibilityPanelController,
            QuickThemePanelController quickThemePanelController,
            Supplier<KeyboardSettings> settings,
            Supplier<String> remoteModeToggleLabel,
            Runnable remoteModeToggler,
            Supplier<String> numberRowToggleLabel,
            BooleanSupplier activeNumberRowVisible,
            Runnable activeNumberRowToggler,
            Consumer<HandednessMode> handednessApplier,
            Runnable themeClipboardImporter,
            Runnable inputIssueReportCopier,
            Runnable dismissQuickSettings) {
        this.context = context;
        this.remoteCompatibilityPanelController = remoteCompatibilityPanelController;
        this.quickThemePanelController = quickThemePanelController;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.remoteModeToggleLabel = RuntimeDefaults.emptyStringSupplier(remoteModeToggleLabel);
        this.remoteModeToggler = RuntimeDefaults.runnable(remoteModeToggler);
        this.numberRowToggleLabel = RuntimeDefaults.emptyStringSupplier(numberRowToggleLabel);
        this.activeNumberRowVisible = RuntimeDefaults.booleanSupplier(activeNumberRowVisible);
        this.activeNumberRowToggler = RuntimeDefaults.runnable(activeNumberRowToggler);
        this.handednessApplier = RuntimeDefaults.handednessConsumer(handednessApplier);
        this.themeClipboardImporter = RuntimeDefaults.runnable(themeClipboardImporter);
        this.inputIssueReportCopier = RuntimeDefaults.runnable(inputIssueReportCopier);
        this.dismissQuickSettings = RuntimeDefaults.runnable(dismissQuickSettings);
    }

    View createPanel() {
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        LinearLayout panel = SettingsRowBuilder.vertical(context);
        panel.setPadding(
                QuickPanelUi.dp(context, 14),
                QuickPanelUi.dp(context, 12),
                QuickPanelUi.dp(context, 14),
                QuickPanelUi.dp(context, 14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(QuickPanelUi.dp(context, 12));
        background.setStroke(Math.max(1, QuickPanelUi.dp(context, 1)), ui.border);
        panel.setBackground(background);

        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.titleLabel(context, R.string.quick_settings_title),
                0);

        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.quickButton(
                        context,
                        remoteModeToggleLabel.get(),
                        currentSettings.remoteModeEnabled,
                        v -> remoteModeToggler.run()),
                8);
        if (currentSettings.remoteModeEnabled && remoteCompatibilityPanelController != null) {
            remoteCompatibilityPanelController.addTo(panel);
        }

        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.quickButton(
                        context,
                        numberRowToggleLabel.get(),
                        activeNumberRowVisible.getAsBoolean(),
                        v -> activeNumberRowToggler.run()),
                8);

        LinearLayout handRow = QuickPanelUi.row(context);
        handRow.addView(
                handednessButton(
                        context.getString(R.string.handedness_left),
                        HandednessMode.LEFT,
                        currentSettings),
                QuickPanelUi.weightedParams(context, 0, 4));
        handRow.addView(
                handednessButton(
                        context.getString(R.string.handedness_balanced),
                        HandednessMode.BALANCED,
                        currentSettings),
                QuickPanelUi.weightedParams(context, 0, 4));
        handRow.addView(
                handednessButton(
                        context.getString(R.string.handedness_right),
                        HandednessMode.RIGHT,
                        currentSettings),
                QuickPanelUi.weightedParams(context, 0, 0));
        QuickPanelUi.addWithTop(context, panel, handRow, 6);

        if (quickThemePanelController != null) {
            quickThemePanelController.addTo(panel);
        }

        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.quickButton(
                        context,
                        context.getString(R.string.import_theme_from_clipboard),
                        false,
                        v -> themeClipboardImporter.run()),
                6);
        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.quickButton(
                        context,
                        context.getString(R.string.copy_input_issue_report),
                        false,
                        v -> inputIssueReportCopier.run()),
                6);
        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.quickButton(
                        context,
                        context.getString(R.string.quick_settings_ok),
                        false,
                        v -> dismissQuickSettings.run()),
                8);
        return panel;
    }

    private View handednessButton(
            String text,
            HandednessMode mode,
            KeyboardSettings currentSettings) {
        return QuickPanelUi.quickButton(context, text, currentSettings.handednessMode == mode, v -> {
            handednessApplier.accept(mode);
            dismissQuickSettings.run();
        });
    }

}
