package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class AppearanceSettingsController {
    private static final AdditionalNumberRowColorMode[] NUMBER_ROW_COLOR_MODE_ORDER =
            AdditionalNumberRowColorMode.displayOrder();

    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private Spinner keyIdleColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner additionalNumberRowColorModeSpinner;
    private CheckBox customDepthColorCheckBox;
    private CheckBox keyDepthCheckBox;
    private SeekBar roundnessSeekBar;
    private SeekBar keyBorderWidthSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar keyDepthSeekBar;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private boolean syncing = true;

    AppearanceSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
    }

    void initializeHiddenControls() {
        keyIdleColorSpinner = themeColorSpinner(ThemeColorSlot.KEY_IDLE);
        keyPressedColorSpinner = themeColorSpinner(ThemeColorSlot.KEY_PRESSED);
        keyboardBackgroundColorSpinner = themeColorSpinner(ThemeColorSlot.KEYBOARD_BACKGROUND);
        accentColorSpinner = themeColorSpinner(ThemeColorSlot.ACCENT);
        secondaryColorSpinner = themeColorSpinner(ThemeColorSlot.SECONDARY);
        accentKeyColorSpinner = extendedThemeColorSpinner(ExtendedThemeColorSlot.ACCENT_KEY);
        borderColorSpinner = extendedThemeColorSpinner(ExtendedThemeColorSlot.BORDER);
        customDepthColorCheckBox = SettingsRowBuilder.checkBox(
                context,
                R.string.settings_custom_depth_color,
                () -> !syncing,
                isChecked -> {
                    KeyboardSettings safe = RuntimeDefaults.keyboardSettingsFrom(settings);
                    saveCustomAppearance(safe.withDepthColor(isChecked, safe.depthColor));
                });
        depthColorSpinner = colorSpinner(color -> saveCustomAppearance(
                RuntimeDefaults.keyboardSettingsFrom(settings).withDepthColor(true, color)));
        roundnessValue = SettingsRowBuilder.valueLabel(context);
        roundnessSeekBar = SettingsRowBuilder.seekBar(context, KeyboardSettings.MAX_KEY_ROUNDNESS_DP);
        keyBorderWidthValue = SettingsRowBuilder.valueLabel(context);
        keyBorderWidthSeekBar = SettingsRowBuilder.seekBar(context, KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP);
        keyGapValue = SettingsRowBuilder.valueLabel(context);
        keyGapSeekBar = SettingsRowBuilder.seekBar(context, KeyboardSettings.MAX_KEY_GAP_DP);
        keyDepthCheckBox = SettingsRowBuilder.checkBox(
                context,
                R.string.settings_key_depth_effect,
                () -> !syncing,
                isChecked -> {
                    KeyboardSettings safe = RuntimeDefaults.keyboardSettingsFrom(settings);
                    saveCustomAppearance(safe.withKeyDepth(isChecked, safe.keyDepthDp));
        });
        keyDepthValue = SettingsRowBuilder.valueLabel(context);
        keyDepthSeekBar = SettingsRowBuilder.seekBar(context, KeyboardSettings.MAX_KEY_DEPTH_DP);
        additionalNumberRowColorModeSpinner = numberRowColorModeSpinner();
    }

    void sync(KeyboardSettings settings) {
        if (keyIdleColorSpinner == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        syncing = true;
        roundnessSeekBar.setProgress(safe.keyRoundnessDp);
        keyBorderWidthSeekBar.setProgress(safe.keyBorderWidthDp);
        keyGapSeekBar.setProgress(safe.keyGapDp);
        keyDepthSeekBar.setProgress(safe.keyDepthDp);
        keyIdleColorSpinner.setSelection(ColorOption.basicIndexOf(safe.keyIdleColor));
        keyPressedColorSpinner.setSelection(ColorOption.basicIndexOf(safe.keyPressedColor));
        keyboardBackgroundColorSpinner.setSelection(ColorOption.basicIndexOf(safe.keyboardBackgroundColor));
        accentColorSpinner.setSelection(ColorOption.basicIndexOf(safe.accentColor));
        secondaryColorSpinner.setSelection(ColorOption.basicIndexOf(safe.secondaryColor));
        accentKeyColorSpinner.setSelection(ColorOption.basicIndexOf(safe.accentKeyColor));
        borderColorSpinner.setSelection(ColorOption.basicIndexOf(safe.borderColor));
        depthColorSpinner.setSelection(ColorOption.basicIndexOf(safe.depthColor));
        additionalNumberRowColorModeSpinner.setSelection(
                AdditionalNumberRowColorMode.indexOf(safe.additionalNumberRowColorMode));
        keyDepthCheckBox.setChecked(safe.keyDepthEnabled);
        customDepthColorCheckBox.setChecked(safe.customDepthColorEnabled);
        keyDepthSeekBar.setEnabled(safe.keyDepthEnabled);
        depthColorSpinner.setEnabled(safe.customDepthColorEnabled);
        roundnessValue.setText(SettingsValueFormatter.roundness(context, safe.keyRoundnessDp));
        keyBorderWidthValue.setText(SettingsValueFormatter.borderWidth(context, safe.keyBorderWidthDp));
        keyGapValue.setText(SettingsValueFormatter.visualGap(context, safe.keyGapDp));
        keyDepthValue.setText(SettingsValueFormatter.depthHeight(context, safe));
        syncing = false;
    }

    private Spinner colorSpinner(IntConsumer listener) {
        return SettingsRowBuilder.optionSpinner(
                context,
                ColorOption.BASIC_OPTIONS,
                () -> !syncing,
                option -> {
                    currentThemeCustomMarker.run();
                    listener.accept(option.color);
                });
    }

    private Spinner themeColorSpinner(ThemeColorSlot slot) {
        return colorSpinner(color -> settingsSaver.accept(withThemeColor(
                RuntimeDefaults.keyboardSettingsFrom(settings),
                slot,
                color)));
    }

    private Spinner extendedThemeColorSpinner(ExtendedThemeColorSlot slot) {
        return colorSpinner(color -> settingsSaver.accept(withExtendedThemeColor(
                RuntimeDefaults.keyboardSettingsFrom(settings),
                slot,
                color)));
    }

    private KeyboardSettings withThemeColor(
            KeyboardSettings settings,
            ThemeColorSlot slot,
            int color) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        switch (slot) {
            case KEY_IDLE:
                return safe.withThemeColors(
                        color,
                        safe.keyPressedColor,
                        safe.keyboardBackgroundColor,
                        safe.accentColor,
                        safe.secondaryColor);
            case KEY_PRESSED:
                return safe.withThemeColors(
                        safe.keyIdleColor,
                        color,
                        safe.keyboardBackgroundColor,
                        safe.accentColor,
                        safe.secondaryColor);
            case KEYBOARD_BACKGROUND:
                return safe.withThemeColors(
                        safe.keyIdleColor,
                        safe.keyPressedColor,
                        color,
                        safe.accentColor,
                        safe.secondaryColor);
            case ACCENT:
                return safe.withThemeColors(
                        safe.keyIdleColor,
                        safe.keyPressedColor,
                        safe.keyboardBackgroundColor,
                        color,
                        safe.secondaryColor);
            case SECONDARY:
                return safe.withThemeColors(
                        safe.keyIdleColor,
                        safe.keyPressedColor,
                        safe.keyboardBackgroundColor,
                        safe.accentColor,
                        color);
            default:
                return safe;
        }
    }

    private Spinner numberRowColorModeSpinner() {
        return SettingsRowBuilder.optionSpinner(
                context,
                NUMBER_ROW_COLOR_MODE_ORDER,
                () -> !syncing,
                mode -> saveCustomAppearance(RuntimeDefaults.keyboardSettingsFrom(settings)
                        .withAdditionalNumberRowColorMode(mode)));
    }

    private KeyboardSettings withExtendedThemeColor(
            KeyboardSettings settings,
            ExtendedThemeColorSlot slot,
            int color) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        switch (slot) {
            case ACCENT_KEY:
                return safe.withExtendedThemeColors(
                        safe.keyIdleColor,
                        safe.keyPressedColor,
                        safe.keyboardBackgroundColor,
                        safe.accentColor,
                        safe.secondaryColor,
                        safe.functionKeyColor,
                        color,
                        safe.borderColor,
                        safe.customDepthColorEnabled,
                        safe.depthColor);
            case BORDER:
                return safe.withExtendedThemeColors(
                        safe.keyIdleColor,
                        safe.keyPressedColor,
                        safe.keyboardBackgroundColor,
                        safe.accentColor,
                        safe.secondaryColor,
                        safe.functionKeyColor,
                        safe.accentKeyColor,
                        color,
                        safe.customDepthColorEnabled,
                        safe.depthColor);
            default:
                return safe;
        }
    }

    private void saveCustomAppearance(KeyboardSettings settings) {
        currentThemeCustomMarker.run();
        settingsSaver.accept(settings);
    }

    private enum ThemeColorSlot {
        KEY_IDLE,
        KEY_PRESSED,
        KEYBOARD_BACKGROUND,
        ACCENT,
        SECONDARY
    }

    private enum ExtendedThemeColorSlot {
        ACCENT_KEY,
        BORDER
    }

}
