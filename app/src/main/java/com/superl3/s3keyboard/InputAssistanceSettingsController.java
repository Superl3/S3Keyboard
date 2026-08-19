package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class InputAssistanceSettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions;
    private final BooleanSupplier debuggableBuild;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions> settingsAndErgonomicsSaver;
    private final Runnable controlsSyncer;
    private InputAssistanceMode[] modes;
    private Spinner modeSpinner;
    private CheckBox hangulConsonantSlideHintsCheckBox;
    private CheckBox hangulVowelSlideHintsCheckBox;
    private CheckBox englishSlideHintsCheckBox;
    private CheckBox spacebarSlideHintsCheckBox;
    private CheckBox beginnerTooltipPreviewCheckBox;
    private boolean syncing = true;

    InputAssistanceSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Supplier<KeyboardErgonomicsOptions> ergonomicsOptions,
            BooleanSupplier debuggableBuild,
            Consumer<KeyboardSettings> settingsSaver,
            BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions> settingsAndErgonomicsSaver,
            Runnable controlsSyncer) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.ergonomicsOptions = RuntimeDefaults.keyboardErgonomicsSupplier(ergonomicsOptions);
        this.debuggableBuild = RuntimeDefaults.booleanSupplier(debuggableBuild);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.settingsAndErgonomicsSaver =
                RuntimeDefaults.keyboardSettingsAndErgonomicsConsumer(settingsAndErgonomicsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
        modes = InputAssistanceMode.displayOrder(isDebuggableBuild());
    }

    void addTo(LinearLayout root) {
        modes = InputAssistanceMode.displayOrder(isDebuggableBuild());
        modeSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_input_assistance_mode,
                SettingsRowBuilder.optionSpinner(
                        context,
                        modes,
                        () -> !syncing,
                        mode -> {
                            if (mode.isPreset()) {
                                applyMode(mode);
                            }
                        }),
                12);

        hangulConsonantSlideHintsCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_hangul_consonant_slide_hints,
                12,
                () -> !syncing,
                this::saveHangulConsonantSlideHints);
        hangulVowelSlideHintsCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_hangul_vowel_slide_hints,
                8,
                () -> !syncing,
                this::saveHangulVowelSlideHints);
        englishSlideHintsCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_english_slide_hints,
                8,
                () -> !syncing,
                this::saveEnglishSlideHints);
        spacebarSlideHintsCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_spacebar_slide_hints,
                8,
                () -> !syncing,
                this::saveSpacebarSlideHints);
        beginnerTooltipPreviewCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.settings_beginner_preview,
                8,
                () -> !syncing,
                this::saveBeginnerTooltipPreview);
    }

    void sync(KeyboardSettings settings) {
        if (modeSpinner == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);

        syncing = true;
        modeSpinner.setSelection(InputAssistanceMode.indexOf(
                modes,
                currentMode(context, safe, isDebuggableBuild())));
        hangulConsonantSlideHintsCheckBox.setChecked(
                KeyboardPreferences.loadShowHangulConsonantSlideHints(context));
        hangulVowelSlideHintsCheckBox.setChecked(
                KeyboardPreferences.loadShowHangulVowelSlideHints(context));
        englishSlideHintsCheckBox.setChecked(safe.showEnglishSlideHints);
        spacebarSlideHintsCheckBox.setChecked(KeyboardPreferences.loadShowSpacebarSlideHints(context));
        beginnerTooltipPreviewCheckBox.setChecked(safe.showBeginnerTooltipPreview);
        syncing = false;
    }

    static InputAssistanceMode currentMode(
            Context context,
            KeyboardSettings settings,
            boolean debuggableBuild) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        return InputAssistanceMode.match(
                KeyboardPreferences.loadShowHangulConsonantSlideHints(context),
                KeyboardPreferences.loadShowHangulVowelSlideHints(context),
                safe.showEnglishSlideHints,
                KeyboardPreferences.loadShowSpacebarSlideHints(context),
                safe.showBeginnerTooltipPreview,
                debuggableBuild && KeyboardPreferences.loadDebugKeyBoundsOverlayEnabled(context));
    }

    static KeyboardSettings applyPreset(
            Context context,
            KeyboardSettings settings,
            InputAssistanceMode mode,
            boolean debuggableBuild) {
        if (mode == null || !mode.isPreset()) {
            return RuntimeDefaults.keyboardSettings(settings);
        }
        KeyboardPreferences.saveInputAssistanceMode(context, mode);
        InputAssistanceMode.Profile profile = mode.profile;
        saveHangulConsonantHints(context, profile.showHangulConsonantHints);
        saveHangulVowelHints(context, profile.showHangulVowelHints);
        saveSpacebarHints(context, profile.showSpacebarHints);
        if (debuggableBuild) {
            saveDebugOverlay(context, profile.showDebugOverlay);
        }
        return settingsForProfile(settings, profile);
    }

    static KeyboardErgonomicsOptions ergonomicsForMode(
            KeyboardErgonomicsOptions current,
            InputAssistanceMode mode) {
        KeyboardErgonomicsOptions safe = RuntimeDefaults.keyboardErgonomics(current);
        if (mode == null || !mode.isPreset()) {
            return safe;
        }
        return mode.profile.recommendedErgonomicsPreset.options;
    }

    static void saveHangulConsonantHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowHangulConsonantSlideHints(context, enabled);
    }

    static void saveHangulVowelHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowHangulVowelSlideHints(context, enabled);
    }

    static void saveSpacebarHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowSpacebarSlideHints(context, enabled);
    }

    static void saveDebugOverlay(Context context, boolean enabled) {
        KeyboardPreferences.saveDebugKeyBoundsOverlayEnabled(context, enabled);
    }

    static KeyboardSettings settingsForProfile(
            KeyboardSettings settings,
            InputAssistanceMode.Profile profile) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        if (profile == null) {
            return safe;
        }
        return safe.withHintVisibility(
                profile.showAnyHangulHints(),
                profile.showEnglishHints,
                profile.showPreview);
    }

    private boolean isDebuggableBuild() {
        return debuggableBuild.getAsBoolean();
    }

    private void applyMode(InputAssistanceMode mode) {
        KeyboardSettings nextSettings = applyPreset(
                context,
                RuntimeDefaults.keyboardSettingsFrom(settings),
                mode,
                isDebuggableBuild());
        KeyboardErgonomicsOptions nextErgonomics = ergonomicsForMode(
                RuntimeDefaults.keyboardErgonomicsFrom(ergonomicsOptions),
                mode);
        settingsAndErgonomicsSaver.accept(nextSettings, nextErgonomics);
    }

    private void saveHangulConsonantSlideHints(boolean enabled) {
        saveHangulConsonantHints(context, enabled);
        controlsSyncer.run();
    }

    private void saveHangulVowelSlideHints(boolean enabled) {
        saveHangulVowelHints(context, enabled);
        controlsSyncer.run();
    }

    private void saveEnglishSlideHints(boolean enabled) {
        KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
        settingsSaver.accept(settings.withHintVisibility(
                settings.showHangulSlideHints,
                enabled,
                settings.showBeginnerTooltipPreview));
    }

    private void saveSpacebarSlideHints(boolean enabled) {
        saveSpacebarHints(context, enabled);
        controlsSyncer.run();
    }

    private void saveBeginnerTooltipPreview(boolean enabled) {
        KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
        settingsSaver.accept(settings.withHintVisibility(
                settings.showHangulSlideHints,
                settings.showEnglishSlideHints,
                enabled));
    }

}
