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
import android.widget.Button;
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
    private KeyboardMode previewMode = KeyboardMode.HANGUL;
    private Button dingulPreviewButton;
    private Button qwertyPreviewButton;
    private TextView externalThemeSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                v -> resetThemeToDefault());

        ExternalThemeStore.ensureThemeDirectory(this);
        externalThemeSummary = SettingsRowBuilder.valueLabel(this);
        externalThemeSummary.setTextColor(ui.textSecondary);
        root.addView(externalThemeSummary, SettingsRowBuilder.matchWrapWithTop(this, 10));

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
        root.addView(externalRow, SettingsRowBuilder.matchWrapWithTop(this, 8));

        LinearLayout previewModeRow = SettingsRowBuilder.horizontal(this);
        dingulPreviewButton = previewModeButton(
                getString(R.string.theme_preview_mode_dingul),
                KeyboardMode.HANGUL);
        qwertyPreviewButton = previewModeButton(
                getString(R.string.theme_preview_mode_qwerty),
                KeyboardMode.ENGLISH);
        previewModeRow.addView(dingulPreviewButton, SettingsRowBuilder.weightedWrap(this, 3, 3));
        previewModeRow.addView(qwertyPreviewButton, SettingsRowBuilder.weightedWrap(this, 3, 3));
        root.addView(previewModeRow, SettingsRowBuilder.matchWrapWithTop(this, 10));

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
        themeOptions = ThemeOption.buildOptions(UserThemeStore.load(this), externalThemes, false);
        selectedIndex = ThemeOption.indexOfStableId(
                themeOptions,
                KeyboardPreferences.loadSelectedThemeId(this),
                -1);
        updatePreviewModeButtons();
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

        boolean englishPreview = previewMode == KeyboardMode.ENGLISH;
        card.addView(previewKeyboard(englishPreview ? englishSettings : hangulSettings),
                SettingsRowBuilder.matchHeightWithTop(this, englishPreview ? 88 : 108, 10));
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

    private Button previewModeButton(String label, KeyboardMode mode) {
        Button button = SettingsRowBuilder.button(
                this,
                label,
                previewMode == mode,
                v -> {
                    previewMode = mode;
                    updatePreviewModeButtons();
                    rebuildCards();
                });
        styleSystemButton(button, previewMode == mode);
        return button;
    }

    private void updatePreviewModeButtons() {
        if (dingulPreviewButton != null) {
            styleSystemButton(dingulPreviewButton, previewMode == KeyboardMode.HANGUL);
        }
        if (qwertyPreviewButton != null) {
            styleSystemButton(qwertyPreviewButton, previewMode == KeyboardMode.ENGLISH);
        }
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
        background.setCornerRadius(SettingsRowBuilder.dp(this, 18));
        background.setStroke(
                SettingsRowBuilder.dp(this, selected ? 4 : 1),
                selected ? ui.selectedBorder : ui.border);
        return background;
    }

    private void styleSystemButton(Button button, boolean selected) {
        SettingsViewStyler.button(button, this, selected);
        button.setPadding(
                SettingsRowBuilder.dp(this, 18),
                0,
                SettingsRowBuilder.dp(this, 18),
                0);
    }

}
