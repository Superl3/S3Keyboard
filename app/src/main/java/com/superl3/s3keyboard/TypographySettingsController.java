package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class TypographySettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private Spinner fontFamilySpinner;
    private CheckBox followThemeTypographyCheckBox;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private boolean syncing = true;

    TypographySettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
    }

    void addTo(LinearLayout root) {
        fontFamilySpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.theme_font_label,
                fontSpinner(),
                12);

        followThemeTypographyCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_follow_theme_typography,
                8,
                () -> !syncing,
                this::saveFollowThemeTypography);

        primaryTextSizeValue = SettingsRowBuilder.valueLabel(context);
        primaryTextSizeSeekBar = addTypographySizeSeekBar(
                root,
                primaryTextSizeValue,
                12,
                (safe, progress) -> safe.withTypography(
                        safe.fontFamily,
                        KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                        safe.secondaryTextSizePercent,
                        safe.primaryTextBold,
                        safe.primaryTextItalic,
                        safe.secondaryTextBold,
                        safe.secondaryTextItalic));

        secondaryTextSizeValue = SettingsRowBuilder.valueLabel(context);
        secondaryTextSizeSeekBar = addTypographySizeSeekBar(
                root,
                secondaryTextSizeValue,
                8,
                (safe, progress) -> safe.withTypography(
                        safe.fontFamily,
                        safe.primaryTextSizePercent,
                        KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                        safe.primaryTextBold,
                        safe.primaryTextItalic,
                        safe.secondaryTextBold,
                        safe.secondaryTextItalic));

        primaryTextBoldCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_primary_text_bold,
                8,
                (safe, isChecked) -> safe.withTypography(
                        safe.fontFamily,
                        safe.primaryTextSizePercent,
                        safe.secondaryTextSizePercent,
                        isChecked,
                        safe.primaryTextItalic,
                        safe.secondaryTextBold,
                        safe.secondaryTextItalic));
        primaryTextItalicCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_primary_text_italic,
                4,
                (safe, isChecked) -> safe.withTypography(
                        safe.fontFamily,
                        safe.primaryTextSizePercent,
                        safe.secondaryTextSizePercent,
                        safe.primaryTextBold,
                        isChecked,
                        safe.secondaryTextBold,
                        safe.secondaryTextItalic));
        secondaryTextBoldCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_secondary_text_bold,
                8,
                (safe, isChecked) -> safe.withTypography(
                        safe.fontFamily,
                        safe.primaryTextSizePercent,
                        safe.secondaryTextSizePercent,
                        safe.primaryTextBold,
                        safe.primaryTextItalic,
                        isChecked,
                        safe.secondaryTextItalic));
        secondaryTextItalicCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_secondary_text_italic,
                4,
                (safe, isChecked) -> safe.withTypography(
                        safe.fontFamily,
                        safe.primaryTextSizePercent,
                        safe.secondaryTextSizePercent,
                        safe.primaryTextBold,
                        safe.primaryTextItalic,
                        safe.secondaryTextBold,
                        isChecked));
    }

    void sync(KeyboardSettings settings) {
        if (fontFamilySpinner == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        syncing = true;
        fontFamilySpinner.setSelection(FontOption.basicIndexOf(safe.fontFamily));
        followThemeTypographyCheckBox.setChecked(safe.followThemeTypography);
        primaryTextSizeSeekBar.setProgress(
                safe.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        secondaryTextSizeSeekBar.setProgress(
                safe.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        primaryTextBoldCheckBox.setChecked(safe.primaryTextBold);
        primaryTextItalicCheckBox.setChecked(safe.primaryTextItalic);
        secondaryTextBoldCheckBox.setChecked(safe.secondaryTextBold);
        secondaryTextItalicCheckBox.setChecked(safe.secondaryTextItalic);
        boolean controlsEnabled = !safe.followThemeTypography;
        fontFamilySpinner.setEnabled(controlsEnabled);
        primaryTextSizeSeekBar.setEnabled(controlsEnabled);
        secondaryTextSizeSeekBar.setEnabled(controlsEnabled);
        primaryTextBoldCheckBox.setEnabled(controlsEnabled);
        primaryTextItalicCheckBox.setEnabled(controlsEnabled);
        secondaryTextBoldCheckBox.setEnabled(controlsEnabled);
        secondaryTextItalicCheckBox.setEnabled(controlsEnabled);
        primaryTextSizeValue.setText(SettingsValueFormatter.primaryTextSize(
                context,
                safe.primaryTextSizePercent));
        secondaryTextSizeValue.setText(SettingsValueFormatter.secondaryTextSize(
                context,
                safe.secondaryTextSizePercent));
        syncing = false;
    }

    private Spinner fontSpinner() {
        return SettingsRowBuilder.optionSpinner(
                context,
                FontOption.BASIC_OPTIONS,
                () -> !syncing,
                option -> saveCustomTypography(
                        RuntimeDefaults.keyboardSettingsFrom(settings)
                                .withFontFamily(option.value)));
    }

    private SeekBar addTypographySizeSeekBar(
            LinearLayout root,
            TextView valueLabel,
            int topMarginDp,
            BiFunction<KeyboardSettings, Integer, KeyboardSettings> change) {
        return SettingsRowBuilder.seekBarRow(
                context,
                root,
                valueLabel,
                KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT,
                topMarginDp,
                () -> !syncing,
                progress -> saveCustomTypography(
                        change.apply(RuntimeDefaults.keyboardSettingsFrom(settings), progress)));
    }

    private CheckBox addTypographyCheckBox(
            LinearLayout root,
            int labelResId,
            int topMarginDp,
            BiFunction<KeyboardSettings, Boolean, KeyboardSettings> change) {
        return SettingsRowBuilder.checkBoxRow(
                context,
                root,
                labelResId,
                topMarginDp,
                () -> !syncing,
                isChecked -> saveCustomTypography(
                        change.apply(RuntimeDefaults.keyboardSettingsFrom(settings), isChecked)));
    }

    private void saveFollowThemeTypography(boolean isChecked) {
        settingsSaver.accept(RuntimeDefaults.keyboardSettingsFrom(settings)
                .withFollowThemeTypography(isChecked));
    }

    private void saveCustomTypography(KeyboardSettings settings) {
        settingsSaver.accept(settings);
    }

}
