package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class QuickSettingsPanelController {
    interface Host {
        KeyboardSettings settings();

        String remoteModeToggleLabel();

        void toggleRemoteMode();

        String numberRowToggleLabel();

        boolean activeNumberRowVisible();

        void toggleActiveNumberRow();

        void setHandedness(HandednessMode mode);

        void importThemeFromClipboard();

        void copyInputIssueReport();

        void dismissQuickSettings();
    }

    private final Context context;
    private final RemoteCompatibilityPanelController remoteCompatibilityPanelController;
    private final QuickThemePanelController quickThemePanelController;
    private final Host host;

    QuickSettingsPanelController(
            Context context,
            RemoteCompatibilityPanelController remoteCompatibilityPanelController,
            QuickThemePanelController quickThemePanelController,
            Host host) {
        this.context = context;
        this.remoteCompatibilityPanelController = remoteCompatibilityPanelController;
        this.quickThemePanelController = quickThemePanelController;
        this.host = host;
    }

    View createPanel() {
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(dp(12));
        background.setStroke(Math.max(1, dp(1)), ui.border);
        panel.setBackground(background);

        TextView title = new TextView(context);
        title.setText(R.string.quick_settings_title);
        title.setTextColor(ui.textPrimary);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(title, matchWrap());

        panel.addView(
                quickButton(
                        host.remoteModeToggleLabel(),
                        settings().remoteModeEnabled,
                        v -> host.toggleRemoteMode()),
                topWrap(8));
        if (settings().remoteModeEnabled && remoteCompatibilityPanelController != null) {
            remoteCompatibilityPanelController.addTo(panel);
        }

        panel.addView(
                quickButton(
                        host.numberRowToggleLabel(),
                        host.activeNumberRowVisible(),
                        v -> host.toggleActiveNumberRow()),
                topWrap(8));

        LinearLayout handRow = new LinearLayout(context);
        handRow.setOrientation(LinearLayout.HORIZONTAL);
        handRow.addView(
                handednessButton(context.getString(R.string.handedness_left), HandednessMode.LEFT),
                weightedParams(0, 4));
        handRow.addView(
                handednessButton(context.getString(R.string.handedness_balanced), HandednessMode.BALANCED),
                weightedParams(0, 4));
        handRow.addView(
                handednessButton(context.getString(R.string.handedness_right), HandednessMode.RIGHT),
                weightedParams(0, 0));
        panel.addView(handRow, topWrap(6));

        if (quickThemePanelController != null) {
            quickThemePanelController.addTo(panel);
        }

        panel.addView(quickButton(
                context.getString(R.string.import_theme_from_clipboard),
                false,
                v -> host.importThemeFromClipboard()), topWrap(6));
        panel.addView(quickButton(
                context.getString(R.string.copy_input_issue_report),
                false,
                v -> host.copyInputIssueReport()), topWrap(6));
        panel.addView(quickButton(
                context.getString(R.string.quick_settings_ok),
                false,
                v -> host.dismissQuickSettings()), topWrap(8));
        return panel;
    }

    private KeyboardSettings settings() {
        KeyboardSettings settings = host == null ? null : host.settings();
        return settings == null ? KeyboardSettings.defaults() : settings;
    }

    private Button quickButton(String text, boolean selected, View.OnClickListener listener) {
        Button button = new Button(context);
        button.setText(text);
        button.setAllCaps(false);
        styleQuickButton(button, selected);
        button.setOnClickListener(listener);
        return button;
    }

    private Button handednessButton(String text, HandednessMode mode) {
        Button button = quickButton(text, settings().handednessMode == mode, v -> {
            host.setHandedness(mode);
            host.dismissQuickSettings();
        });
        button.setAllCaps(false);
        return button;
    }

    private void styleQuickButton(Button button, boolean selected) {
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        button.setAllCaps(false);
        button.setTextColor(selected ? ui.selectedText : ui.controlText);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(44));
        button.setPadding(dp(24), 0, dp(24), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? ui.selectedFill : ui.controlFill);
        background.setCornerRadius(dp(8));
        background.setStroke(Math.max(1, dp(selected ? 2 : 1)), selected ? ui.selectedBorder : ui.border);
        button.setBackground(background);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams topWrap(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private LinearLayout.LayoutParams weightedParams(int leftMarginDp, int rightMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        params.leftMargin = dp(leftMarginDp);
        params.rightMargin = dp(rightMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
