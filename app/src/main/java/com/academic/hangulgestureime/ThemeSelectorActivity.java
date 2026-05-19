package com.academic.hangulgestureime;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(24));
        scrollView.addView(root);

        TextView title = label("Theme Selector");
        title.setTextColor(ui.textPrimary);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, matchWrap());

        Button editorButton = new Button(this);
        editorButton.setText("Open Theme Editor");
        styleSystemButton(editorButton, false);
        editorButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_settings, 0, 0, 0);
        editorButton.setCompoundDrawablePadding(dp(8));
        editorButton.setOnClickListener(v -> startActivity(new Intent(this, ThemeEditorActivity.class)));
        root.addView(editorButton, topParams(12));

        LinearLayout previewModeRow = new LinearLayout(this);
        previewModeRow.setOrientation(LinearLayout.HORIZONTAL);
        Button dingulPreviewButton = previewModeButton("Dingul", KeyboardMode.HANGUL);
        Button qwertyPreviewButton = previewModeButton("QWERTY", KeyboardMode.ENGLISH);
        previewModeRow.addView(dingulPreviewButton, weightedButtonParams());
        previewModeRow.addView(qwertyPreviewButton, weightedButtonParams());
        root.addView(previewModeRow, topParams(10));

        cards = new LinearLayout(this);
        cards.setOrientation(LinearLayout.VERTICAL);
        root.addView(cards, topParams(14));
        rebuildCards();
        return scrollView;
    }

    private void rebuildCards() {
        if (cards == null) {
            return;
        }
        themeOptions = ThemeOption.buildOptions(UserThemeStore.load(this));
        cards.removeAllViews();
        for (int i = 0; i < themeOptions.length; i++) {
            LinearLayout.LayoutParams params = matchWrap();
            params.topMargin = dp(i == 0 ? 0 : 10);
            cards.addView(themeCard(i), params);
        }
    }

    private View themeCard(int index) {
        ThemeOption option = themeOptions[index];
        KeyboardSettings englishSettings = previewSettingsFor(option, KeyboardMode.ENGLISH);
        KeyboardSettings hangulSettings = previewSettingsFor(option, KeyboardMode.HANGUL);
        boolean selected = index == selectedIndex;
        SettingsUiPalette ui = SettingsUiPalette.from(this);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(16));
        card.setBackground(cardBackground(ui, selected));
        card.setElevation(selected ? dp(4) : dp(1));
        card.setOnClickListener(v -> applyTheme(index));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label(option.label);
        title.setTextColor(ui.textPrimary);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(title, new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        if (selected) {
            header.addView(selectedBadge(ui));
        }
        card.addView(header, matchWrap());

        boolean englishPreview = previewMode == KeyboardMode.ENGLISH;
        card.addView(previewKeyboard(englishPreview ? englishSettings : hangulSettings),
                previewParams(englishPreview ? 88 : 108));
        return card;
    }

    private TextView selectedBadge(SettingsUiPalette ui) {
        TextView badge = label("Selected");
        badge.setTextColor(ui.selectedText);
        badge.setTextSize(12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(3), dp(10), dp(3));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.selectedFill);
        background.setCornerRadius(dp(999));
        background.setStroke(dp(1), ui.selectedBorder);
        badge.setBackground(background);
        return badge;
    }

    private Button previewModeButton(String label, KeyboardMode mode) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        styleSystemButton(button, previewMode == mode);
        button.setOnClickListener(v -> {
            previewMode = mode;
            rebuildCards();
        });
        return button;
    }

    private void applyTheme(int index) {
        if (index < 0 || index >= themeOptions.length) {
            selectedIndex = 0;
            rebuildCards();
            return;
        }
        selectedIndex = index;
        settings = themeOptions[index].applyTo(settings);
        KeyboardPreferences.saveSettings(this, settings);
        Toast.makeText(this, "Theme applied: " + themeOptions[index].label, Toast.LENGTH_SHORT).show();
        rebuildCards();
    }

    private KeyboardSettings previewSettingsFor(ThemeOption option, KeyboardMode mode) {
        return option.applyTo(settings)
                .withKeyboardMode(mode)
                .withHintVisibility(false, false, false)
                .withHangulNumberRow(false)
                .withEnglishNumberRow(false);
    }

    private HangulKeyboardView previewKeyboard(KeyboardSettings previewSettings) {
        HangulKeyboardView preview = new HangulKeyboardView(this);
        preview.setCompactPreviewRendering(true);
        preview.setSettings(previewSettings);
        preview.setClickable(true);
        preview.setFocusable(false);
        preview.setOnTouchListener((v, event) -> true);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }

    private GradientDrawable cardBackground(SettingsUiPalette ui, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(dp(18));
        background.setStroke(dp(selected ? 4 : 1), selected ? ui.selectedBorder : ui.border);
        return background;
    }

    private void styleSystemButton(Button button, boolean selected) {
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        button.setTextColor(selected ? ui.selectedText : ui.controlText);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? ui.selectedFill : ui.controlFill);
        background.setCornerRadius(dp(8));
        background.setStroke(dp(selected ? 2 : 1), selected ? ui.selectedBorder : ui.border);
        button.setBackground(background);
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(SettingsUiPalette.from(this).textPrimary);
        label.setTextSize(14);
        return label;
    }

    private LinearLayout.LayoutParams previewParams(int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp));
        params.topMargin = dp(10);
        return params;
    }

    private LinearLayout.LayoutParams topParams(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private LinearLayout.LayoutParams weightedButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        params.leftMargin = dp(3);
        params.rightMargin = dp(3);
        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class ThemeOption {
        final String label;
        final KeyboardThemePreset preset;
        final String userThemeJson;

        ThemeOption(String label, KeyboardThemePreset preset, String userThemeJson) {
            this.label = label;
            this.preset = preset;
            this.userThemeJson = userThemeJson;
        }

        static ThemeOption[] buildOptions(UserThemeStore.UserTheme[] userThemes) {
            int userCount = userThemes == null ? 0 : userThemes.length;
            ThemeOption[] options = new ThemeOption[KeyboardThemePreset.PRESETS.length + userCount];
            for (int i = 0; i < KeyboardThemePreset.PRESETS.length; i++) {
                KeyboardThemePreset preset = KeyboardThemePreset.PRESETS[i];
                options[i] = new ThemeOption(preset.displayName, preset, null);
            }
            for (int i = 0; i < userCount; i++) {
                UserThemeStore.UserTheme theme = userThemes[i];
                options[KeyboardThemePreset.PRESETS.length + i] =
                        new ThemeOption(theme.name, null, theme.json);
            }
            return options;
        }

        KeyboardSettings applyTo(KeyboardSettings settings) {
            if (preset != null) {
                return preset.applyTo(settings);
            }
            if (userThemeJson != null && !userThemeJson.isEmpty()) {
                return KeyboardThemeJson.importTheme(settings, userThemeJson);
            }
            return settings;
        }
    }
}
