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

public final class ThemeSelectorActivity extends Activity {
    private KeyboardSettings settings;
    private LinearLayout cards;
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(24));
        scrollView.addView(root);

        TextView title = label("Theme Selector");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, matchWrap());

        Button editorButton = new Button(this);
        editorButton.setText("Open Theme Editor");
        editorButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_settings, 0, 0, 0);
        editorButton.setCompoundDrawablePadding(dp(8));
        editorButton.setOnClickListener(v -> startActivity(new Intent(this, ThemeEditorActivity.class)));
        root.addView(editorButton, topParams(12));

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
        KeyboardSettings titleSettings = option.applyTo(settings);
        KeyboardSettings englishSettings = previewSettingsFor(option, KeyboardMode.ENGLISH);
        KeyboardSettings hangulSettings = previewSettingsFor(option, KeyboardMode.HANGUL);
        boolean selected = index == selectedIndex;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(16));
        card.setBackground(cardBackground(titleSettings, selected));
        card.setOnClickListener(v -> applyTheme(index));

        TextView title = label(option.label);
        title.setTextColor(0xFF111827);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title, matchWrap());

        card.addView(previewLabel("QWERTY preview"), topParams(10));
        card.addView(previewKeyboard(englishSettings), previewParams(88));
        card.addView(previewLabel("Dingul preview"), topParams(12));
        card.addView(previewKeyboard(hangulSettings), previewParams(108));
        return card;
    }

    private void applyTheme(int index) {
        if (index <= 0 || index >= themeOptions.length) {
            selectedIndex = 0;
            rebuildCards();
            return;
        }
        selectedIndex = index;
        settings = themeOptions[index].applyTo(settings);
        KeyboardPreferences.saveSettings(this, settings);
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
        preview.setEnabled(false);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }

    private TextView previewLabel(String text) {
        TextView label = label(text);
        label.setTextColor(0xFF64748B);
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        return label;
    }

    private GradientDrawable cardBackground(KeyboardSettings cardSettings, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFFFFFFFF);
        background.setCornerRadius(dp(18));
        background.setStroke(dp(selected ? 2 : 1), selected ? cardSettings.accentColor : 0xFFE5E7EB);
        return background;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(14);
        return label;
    }

    private LinearLayout.LayoutParams previewParams(int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp));
        params.topMargin = dp(6);
        return params;
    }

    private LinearLayout.LayoutParams topParams(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
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
            ThemeOption[] options = new ThemeOption[KeyboardThemePreset.PRESETS.length + userCount + 1];
            options[0] = new ThemeOption("Current custom", null, null);
            for (int i = 0; i < KeyboardThemePreset.PRESETS.length; i++) {
                KeyboardThemePreset preset = KeyboardThemePreset.PRESETS[i];
                options[i + 1] = new ThemeOption(preset.displayName, preset, null);
            }
            for (int i = 0; i < userCount; i++) {
                UserThemeStore.UserTheme theme = userThemes[i];
                options[KeyboardThemePreset.PRESETS.length + 1 + i] =
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
