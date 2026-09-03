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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public final class ThemeSelectorActivity extends Activity {
    private KeyboardSettings settings;
    private LinearLayout cards;
    private ThemeOption[] allThemeOptions = new ThemeOption[0];
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedIndex;
    private TextView externalThemeSummary;
    private TextView resultCount;
    private TextView pairSummary;
    private CheckBox pairEnabled;
    private String searchQuery = "";
    private String materialFilter = "";
    private String toneFilter = ThemeManagementModel.TONE_ALL;
    private int collectionFilter;
    private String compareAId = "";
    private String compareBId = "";

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

        addManagementControls(root);
        cards = SettingsRowBuilder.vertical(this);
        root.addView(cards, SettingsRowBuilder.matchWrapWithTop(this, 14));
        rebuildCards();
        return scrollView;
    }

    private void addManagementControls(LinearLayout root) {
        LinearLayout section = SettingsSubsection.add(
                this, root, R.string.theme_management_pair_title, false).content;
        EditText search = SettingsRowBuilder.editText(
                this, "", () -> true, value -> {
                    searchQuery = value;
                    rebuildCards();
                });
        search.setHint(R.string.theme_management_search_hint);
        section.addView(search, SettingsRowBuilder.matchWrapWithTop(this, 4));

        String[] materials = {
                getString(R.string.theme_management_material_all),
                "Solid", "Soft keycap", "Frosted", "Acrylic"
        };
        Spinner material = SettingsRowBuilder.spinnerAfterInitialSelection(
                this, materials, () -> true, position -> {
                    String[] values = {"", KeyboardVisualEffects.MATERIAL_SOLID,
                            KeyboardVisualEffects.MATERIAL_SOFT_KEYCAP,
                            KeyboardVisualEffects.MATERIAL_FROSTED,
                            KeyboardVisualEffects.MATERIAL_ACRYLIC};
                    materialFilter = values[Math.max(0, Math.min(position, values.length - 1))];
                    rebuildCards();
                });
        section.addView(material, SettingsRowBuilder.matchWrapWithTop(this, 6));

        addCollectionAndToneFilters(section);
        addPairAndCompareControls(section);
    }

    private void addCollectionAndToneFilters(LinearLayout section) {
        String[] tones = {
                getString(R.string.theme_management_tone_all),
                getString(R.string.theme_management_tone_light),
                getString(R.string.theme_management_tone_dark)
        };
        Spinner tone = SettingsRowBuilder.spinnerAfterInitialSelection(
                this, tones, () -> true, position -> {
                    String[] values = {ThemeManagementModel.TONE_ALL,
                            ThemeManagementModel.TONE_LIGHT, ThemeManagementModel.TONE_DARK};
                    toneFilter = values[Math.max(0, Math.min(position, values.length - 1))];
                    rebuildCards();
                });
        section.addView(tone, SettingsRowBuilder.matchWrapWithTop(this, 6));
        String[] collections = {
                getString(R.string.theme_management_filter_all),
                getString(R.string.theme_management_filter_favorites),
                getString(R.string.theme_management_filter_recent)
        };
        Spinner collection = SettingsRowBuilder.spinnerAfterInitialSelection(
                this, collections, () -> true, position -> {
                    collectionFilter = position;
                    rebuildCards();
                });
        section.addView(collection, SettingsRowBuilder.matchWrapWithTop(this, 6));
    }

    private void addPairAndCompareControls(LinearLayout section) {
        pairSummary = SettingsRowBuilder.valueLabel(this);
        section.addView(pairSummary, SettingsRowBuilder.matchWrapWithTop(this, 8));
        pairEnabled = SettingsRowBuilder.checkBox(
                this,
                R.string.theme_management_pair_enable,
                () -> true,
                enabled -> {
                    ThemeManagementStore.savePair(
                            this,
                            ThemeManagementStore.loadLightThemeId(this),
                            ThemeManagementStore.loadDarkThemeId(this),
                            enabled);
                    settings = KeyboardPreferences.load(this);
                    rebuildCards();
                });
        pairEnabled.setChecked(ThemeManagementStore.isPairEnabled(this));
        section.addView(pairEnabled, SettingsRowBuilder.matchWrapWithTop(this, 4));
        SettingsRowBuilder.buttonRow(
                this,
                section,
                R.string.theme_management_ab_compare,
                4,
                v -> showCompareDialog());
        resultCount = SettingsRowBuilder.valueLabel(this);
        section.addView(resultCount, SettingsRowBuilder.matchWrapWithTop(this, 4));
    }

    private void updateManagementSummary() {
        if (resultCount != null) {
            resultCount.setText(getString(R.string.theme_management_result_count, themeOptions.length));
        }
        if (pairEnabled != null && pairEnabled.isChecked() != ThemeManagementStore.isPairEnabled(this)) {
            pairEnabled.setChecked(ThemeManagementStore.isPairEnabled(this));
        }
        if (pairSummary != null) {
            pairSummary.setText(getString(R.string.theme_management_pair_light_format,
                    optionLabel(ThemeManagementStore.loadLightThemeId(this))) + "\n"
                    + getString(R.string.theme_management_pair_dark_format,
                    optionLabel(ThemeManagementStore.loadDarkThemeId(this))));
        }
    }

    private void rebuildCards() {
        if (cards == null) {
            return;
        }
        UserThemeStore.UserTheme[] externalThemes = ExternalThemeStore.load(this);
        allThemeOptions = ThemeOption.buildOptions(this, UserThemeStore.load(this), externalThemes, true);
        themeOptions = ThemeManagementModel.filterAndOrder(
                allThemeOptions,
                searchQuery,
                materialFilter,
                toneFilter,
                ThemeManagementStore.loadFavorites(this),
                ThemeManagementStore.loadRecents(this),
                collectionFilter == 1,
                collectionFilter == 2);
        String activeId = ThemeManagementStore.resolveSystemThemeId(
                this, KeyboardPreferences.loadSelectedThemeId(this));
        selectedIndex = ThemeOption.indexOfStableId(themeOptions, activeId, -1);
        updateExternalThemeSummary(externalThemes.length);
        updateManagementSummary();
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
        String stableId = option.stableId();
        if (!stableId.isEmpty()) {
            boolean favorite = ThemeManagementStore.isFavorite(this, stableId);
            Button favoriteButton = SettingsRowBuilder.button(
                    this,
                    getString(favorite ? R.string.theme_management_favorite_on
                            : R.string.theme_management_favorite_off),
                    v -> {
                        ThemeManagementStore.toggleFavorite(this, stableId);
                        rebuildCards();
                    });
            header.addView(favoriteButton, SettingsRowBuilder.wrapContentWithLeft(this, 6));
        }
        card.addView(header, SettingsRowBuilder.matchWrap());
        card.addView(themeMetadata(option), SettingsRowBuilder.matchWrapWithTop(this, 2));
        if (!stableId.isEmpty()) {
            card.addView(themeManagementButtons(option), SettingsRowBuilder.matchWrapWithTop(this, 4));
        }

        card.addView(
                previewKeyboard(englishSettings),
                SettingsRowBuilder.matchHeightWithTop(this, 88, 10));
        card.addView(
                previewKeyboard(hangulSettings),
                SettingsRowBuilder.matchHeightWithTop(this, 108, 4));
        return card;
    }

    private TextView themeMetadata(ThemeOption option) {
        KeyboardSettings appearance = option == null ? null : option.appearanceSettings();
        String material = appearance == null || appearance.visualEffects == null
                ? KeyboardVisualEffects.MATERIAL_SOFT_KEYCAP
                : appearance.visualEffects.materialStyle;
        String tone = getString(ThemeManagementModel.isDark(option)
                ? R.string.theme_management_tone_dark
                : R.string.theme_management_tone_light);
        TextView label = SettingsRowBuilder.valueLabel(this);
        label.setText(getString(R.string.theme_management_metadata_format, material, tone));
        return label;
    }

    private LinearLayout themeManagementButtons(ThemeOption option) {
        String id = option.stableId();
        LinearLayout row = SettingsRowBuilder.horizontal(this);
        row.addView(SettingsRowBuilder.button(this, R.string.theme_management_set_a, v -> compareAId = id),
                SettingsRowBuilder.weightedWrap(this, 1, 1));
        row.addView(SettingsRowBuilder.button(this, R.string.theme_management_set_b, v -> compareBId = id),
                SettingsRowBuilder.weightedWrap(this, 1, 1));
        row.addView(SettingsRowBuilder.button(this, R.string.theme_management_set_light, v -> setPairSlot(id, true)),
                SettingsRowBuilder.weightedWrap(this, 1, 1));
        row.addView(SettingsRowBuilder.button(this, R.string.theme_management_set_dark, v -> setPairSlot(id, false)),
                SettingsRowBuilder.weightedWrap(this, 1, 1));
        return row;
    }

    private void setPairSlot(String id, boolean light) {
        String lightId = light ? id : ThemeManagementStore.loadLightThemeId(this);
        String darkId = light ? ThemeManagementStore.loadDarkThemeId(this) : id;
        ThemeManagementStore.savePair(this, lightId, darkId, ThemeManagementStore.isPairEnabled(this));
        updateManagementSummary();
        Toast.makeText(this, R.string.theme_management_pair_saved, Toast.LENGTH_SHORT).show();
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

    private void showCompareDialog() {
        ThemeOption a = findOption(compareAId);
        ThemeOption b = findOption(compareBId);
        if (a == null || b == null) {
            Toast.makeText(this, R.string.theme_management_ab_missing, Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout previews = SettingsRowBuilder.vertical(this);
        int padding = SettingsRowBuilder.dp(this, 12);
        previews.setPadding(padding, padding, padding, 0);
        addComparePreview(previews, "A · " + a.label, a);
        addComparePreview(previews, "B · " + b.label, b);
        new AlertDialog.Builder(this)
                .setTitle(R.string.theme_management_ab_compare)
                .setView(previews)
                .setNegativeButton(R.string.action_cancel, null)
                .setNeutralButton(R.string.theme_management_apply_a, (dialog, which) -> applyThemeOption(a))
                .setPositiveButton(R.string.theme_management_apply_b, (dialog, which) -> applyThemeOption(b))
                .show();
    }

    private void addComparePreview(LinearLayout root, String title, ThemeOption option) {
        TextView label = SettingsRowBuilder.label(this, title);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(label, SettingsRowBuilder.matchWrapWithTop(this, 6));
        AccentPlacementPolicy policy = KeyboardPreferences.loadAccentPlacementPolicy(this);
        root.addView(previewKeyboard(ThemePreviewSettings.forOption(
                option, settings, KeyboardMode.ENGLISH, policy)),
                SettingsRowBuilder.matchHeightWithTop(this, 82, 4));
        root.addView(previewKeyboard(ThemePreviewSettings.forOption(
                option, settings, KeyboardMode.HANGUL, policy)),
                SettingsRowBuilder.matchHeightWithTop(this, 100, 3));
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
        applyThemeOption(themeOptions[index]);
    }

    private void applyThemeOption(ThemeOption option) {
        if (option == null) return;
        ThemeManagementStore.disablePairing(this);
        settings = option.applyTo(settings);
        KeyboardPreferences.saveSelectedThemeId(this, option.stableId());
        ThemeManagementStore.recordRecent(this, option.stableId());
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        rebuildCards();
    }

    private ThemeOption findOption(String id) {
        int index = ThemeOption.indexOfStableId(allThemeOptions, id, -1);
        return ThemeOption.at(allThemeOptions, index);
    }

    private String optionLabel(String id) {
        ThemeOption option = findOption(id);
        return option == null ? "—" : option.label;
    }

    private void resetThemeToDefault() {
        selectedIndex = -1;
        ThemeManagementStore.disablePairing(this);
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
