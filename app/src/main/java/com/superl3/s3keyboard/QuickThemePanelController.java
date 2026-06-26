package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

final class QuickThemePanelController {
    interface Host {
        KeyboardMode currentKeyboardMode();

        String enterKeyLabel();

        boolean forceNumberRow();

        void applyRuntimeSettings(KeyboardSettings settings);

        void dismissQuickSettings();
    }

    private final Context context;
    private final Host host;

    QuickThemePanelController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void addTo(LinearLayout panel) {
        if (panel == null) {
            return;
        }
        ThemeOption[] options = ThemeOption.buildOptions(
                UserThemeStore.load(context),
                ExternalThemeStore.load(context),
                false);
        if (options.length == 0) {
            return;
        }

        SettingsUiPalette ui = SettingsUiPalette.from(context);
        TextView label = new TextView(context);
        label.setText(R.string.quick_theme_label);
        label.setTextColor(ui.textSecondary);
        label.setTextSize(13);
        panel.addView(label, topWrap(10));

        Spinner spinner = new Spinner(context);
        spinner.setAdapter(new SettingsArrayAdapter<>(context, options));
        spinner.setMinimumHeight(dp(44));
        spinner.setPadding(dp(8), 0, dp(8), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.controlFill);
        background.setCornerRadius(dp(8));
        background.setStroke(Math.max(1, dp(1)), ui.border);
        spinner.setBackground(background);
        int selectedIndex = ThemeOption.indexOfStableId(
                options,
                KeyboardPreferences.loadSelectedThemeId(context));
        spinner.setSelection(selectedIndex, false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean initialized;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!initialized) {
                    initialized = true;
                    return;
                }
                if (position >= 0 && position < options.length) {
                    apply(options[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        panel.addView(spinner, topWrap(4));
    }

    private void apply(ThemeOption option) {
        if (option == null || option.stableId().isEmpty()) {
            return;
        }
        try {
            KeyboardSettings storedSettings = KeyboardPreferences.load(context);
            KeyboardSettings savedSettings = option.applyTo(storedSettings);
            KeyboardPreferences.saveSelectedThemeId(context, option.stableId());
            savedSettings = KeyboardPreferences.applyAccentPlacementPolicy(context, savedSettings);
            KeyboardPreferences.saveSettings(context, savedSettings);
            KeyboardSettings runtimeSettings = savedSettings
                    .withKeyboardMode(host == null ? savedSettings.keyboardMode : host.currentKeyboardMode())
                    .withEnterKeyLabel(host == null ? savedSettings.enterKeyLabel : host.enterKeyLabel())
                    .withRuntimeNumberRowForced(host != null && host.forceNumberRow());
            if (host != null) {
                host.applyRuntimeSettings(runtimeSettings);
                host.dismissQuickSettings();
            }
        } catch (IllegalArgumentException exception) {
            Toast.makeText(context, R.string.quick_theme_apply_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout.LayoutParams topWrap(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
