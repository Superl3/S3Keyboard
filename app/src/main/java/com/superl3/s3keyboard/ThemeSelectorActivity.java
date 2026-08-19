package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class ThemeSelectorActivity extends Activity {
    private KeyboardSettings settings;
    private LinearLayout cards;
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedIndex;
    private TextView externalThemeSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        settings = KeyboardPreferences.load(this);
        setContentView(createContentView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        rebuildCards();
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(this);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(
                SettingsRowBuilder.dp(this, 20),
                SettingsRowBuilder.dp(this, 20),
                SettingsRowBuilder.dp(this, 20),
                SettingsRowBuilder.dp(this, 24));
        scrollView.addView(root);

        TextView title = SettingsRowBuilder.label(this, R.string.theme_selector_title);
        title.setTextColor(ui.textPrimary);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, SettingsRowBuilder.matchWrap());

        SettingsRowBuilder.iconButtonRow(
                this,
                root,
                R.string.theme_editor_open,
                R.drawable.ic_keyboard_settings,
                12,
                v -> startActivity(new Intent(this, ThemeEditorActivity.class)));

        SettingsRowBuilder.iconButtonRow(
                this,
                root,
                R.string.theme_reset_default,
                R.drawable.ic_keyboard_reset,
                8,
                v -> ThemeResetConfirmation.show(this, this::resetThemeToDefault));

        ExternalThemeStore.ensureThemeDirectory(this);
        LinearLayout externalSection = SettingsSubsection.add(
                this,
                root,
                R.string.external_theme_section,
                false).content;
        externalThemeSummary = SettingsRowBuilder.valueLabel(this);
        externalThemeSummary.setTextColor(ui.textSecondary);
        externalSection.addView(
                externalThemeSummary,
                SettingsRowBuilder.matchWrapWithTop(this, 4));

        LinearLayout externalRow = SettingsRowBuilder.horizontal(this);
        SettingsRowBuilder.weightedButton(
                this,
                externalRow,
                R.string.external_theme_folder_setting,
                3,
                3,
                v -> showExternalThemePathDialog());
        SettingsRowBuilder.weightedButton(
                this,
                externalRow,
                R.string.action_refresh,
                3,
                3,
                v -> rebuildCards());
        externalSection.addView(externalRow, SettingsRowBuilder.matchWrapWithTop(this, 8));

        cards = SettingsRowBuilder.vertical(this);
        root.addView(cards, SettingsRowBuilder.matchWrapWithTop(this, 14));
        rebuildCards();
        return scrollView;
    }

    private void rebuildCards() {
        if (cards == null) {
            return;
        }
        UserThemeStore.UserTheme[] externalThemes = ExternalThemeStore.load(this);
        themeOptions = ThemeOption.buildOptions(this, UserThemeStore.load(this), externalThemes, true);
        selectedIndex = ThemeOption.indexOfStableId(
                themeOptions,
                KeyboardPreferences.loadSelectedThemeId(this),
                -1);
        updateExternalThemeSummary(externalThemes.length);
        cards.removeAllViews();
        for (int i = 0; i < themeOptions.length; i++) {
            LinearLayout.LayoutParams params = SettingsRowBuilder.matchWrapWithTop(this, i == 0 ? 0 : 10);
            cards.addView(themeCard(i), params);
        }
    }

    private View themeCard(int index) {
        ThemeOption option = themeOptions[index];
        AccentPlacementPolicy accentPolicy = KeyboardPreferences.loadAccentPlacementPolicy(this);
        KeyboardSettings englishSettings = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH,
                accentPolicy);
        KeyboardSettings hangulSettings = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.HANGUL,
                accentPolicy);
        boolean selected = index == selectedIndex;
        SettingsUiPalette ui = SettingsUiPalette.from(this);

        LinearLayout card = SettingsRowBuilder.vertical(this);
        card.setPadding(
                SettingsRowBuilder.dp(this, 14),
                SettingsRowBuilder.dp(this, 14),
                SettingsRowBuilder.dp(this, 14),
                SettingsRowBuilder.dp(this, 16));
        card.setBackground(cardBackground(ui, selected));
        card.setElevation(SettingsRowBuilder.dp(this, selected ? 4 : 1));
        card.setOnClickListener(v -> applyTheme(index));

        LinearLayout header = SettingsRowBuilder.horizontal(this);
        TextView title = SettingsRowBuilder.label(this, option.label);
        title.setTextColor(ui.textPrimary);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        header.addView(title, SettingsRowBuilder.weightedWrap(this, 1f, 0, 0));
        if (selected) {
            header.addView(selectedBadge(ui), SettingsRowBuilder.wrapContentWithLeft(this, 8));
        }
        card.addView(header, SettingsRowBuilder.matchWrap());

        card.addView(
                previewKeyboard(englishSettings),
                SettingsRowBuilder.matchHeightWithTop(this, 88, 10));
        card.addView(
                previewKeyboard(hangulSettings),
                SettingsRowBuilder.matchHeightWithTop(this, 108, 4));
        return card;
    }

    private void updateExternalThemeSummary(int externalThemeCount) {
        if (externalThemeSummary == null) {
            return;
        }
        externalThemeSummary.setText(getString(
                R.string.external_theme_summary_format,
                externalThemeCount,
                ExternalThemeStore.loadDirectoryPath(this)));
    }

    private void showExternalThemePathDialog() {
        EditText input = SettingsRowBuilder.editText(this);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setText(ExternalThemeStore.loadDirectoryPath(this));
        int padding = SettingsRowBuilder.dp(this, 18);
        LinearLayout container = SettingsRowBuilder.vertical(this);
        container.setPadding(padding, padding, padding, 0);
        container.addView(input, SettingsRowBuilder.matchWrap());

        new AlertDialog.Builder(this)
                .setTitle(R.string.external_theme_folder_title)
                .setMessage(R.string.external_theme_folder_message)
                .setView(container)
                .setNegativeButton(R.string.action_cancel, null)
                .setNeutralButton(R.string.action_default_path, (dialog, which) -> {
                    ExternalThemeStore.saveDirectoryPath(this, ExternalThemeStore.defaultDirectoryPath(this));
                    rebuildCards();
                    Toast.makeText(this, R.string.external_theme_folder_reset, Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    ExternalThemeStore.saveDirectoryPath(this, input.getText().toString());
                    rebuildCards();
                    Toast.makeText(this, R.string.external_theme_folder_saved, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private TextView selectedBadge(SettingsUiPalette ui) {
        TextView badge = SettingsRowBuilder.label(this, R.string.selected_badge);
        badge.setTextColor(ui.selectedText);
        badge.setTextSize(12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(
                SettingsRowBuilder.dp(this, 10),
                SettingsRowBuilder.dp(this, 3),
                SettingsRowBuilder.dp(this, 10),
                SettingsRowBuilder.dp(this, 3));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.selectedFill);
        background.setCornerRadius(SettingsRowBuilder.dp(this, 999));
        background.setStroke(SettingsRowBuilder.dp(this, 1), ui.selectedBorder);
        badge.setBackground(background);
        return badge;
    }

    private void applyTheme(int index) {
        if (index < 0 || index >= themeOptions.length) {
            selectedIndex = 0;
            rebuildCards();
            return;
        }
        selectedIndex = index;
        settings = themeOptions[index].applyTo(settings);
        KeyboardPreferences.saveSelectedThemeId(this, themeOptions[index].stableId());
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        rebuildCards();
    }

    private void resetThemeToDefault() {
        selectedIndex = -1;
        settings = ThemeOption.resetToDefaultAppearance(settings);
        KeyboardPreferences.saveSelectedThemeId(this, "");
        KeyboardPreferences.saveSettings(this, settings);
        rebuildCards();
    }

    private HangulKeyboardView previewKeyboard(KeyboardSettings previewSettings) {
        return KeyboardPreviewFactory.nonInteractive(this, previewSettings);
    }

    private GradientDrawable cardBackground(SettingsUiPalette ui, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(SettingsRowBuilder.dp(this, 8));
        background.setStroke(
                SettingsRowBuilder.dp(this, selected ? 2 : 1),
                selected ? ui.selectedBorder : ui.border);
        return background;
    }

}
