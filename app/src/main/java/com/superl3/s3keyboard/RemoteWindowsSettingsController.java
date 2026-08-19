package com.superl3.s3keyboard;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class RemoteWindowsSettingsController {
    private static final RemoteKeyPreset[] REMOTE_KEY_PRESET_ORDER =
            RemoteKeyPreset.displayOrder();
    private static final RemoteImeShortcut[] REMOTE_IME_SHORTCUT_ORDER =
            RemoteImeShortcut.displayOrder();

    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Runnable controlsSyncer;
    private CheckBox remoteModeCheckBox;
    private CheckBox remoteAutoModeCheckBox;
    private CheckBox showCurrentAppProfileCheckBox;
    private TextView currentAppProfileSummaryValue;
    private EditText remoteAutoPackagesInput;
    private EditText appProfileAsciiPackagesInput;
    private EditText appProfileNumberRowPackagesInput;
    private EditText appProfileNoComposingPackagesInput;
    private EditText appProfileNoTextConveniencesPackagesInput;
    private Spinner remoteKeyPresetSpinner;
    private Spinner remoteImeShortcutSpinner;
    private boolean syncing = true;

    RemoteWindowsSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver,
            Runnable controlsSyncer) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
    }

    void addTo(LinearLayout root) {
        LinearLayout basicsSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_remote_basics_subsection,
                true).content;
        LinearLayout automaticSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_remote_automatic_subsection,
                false).content;
        LinearLayout appOverridesSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_remote_app_overrides_subsection,
                false).content;

        remoteModeCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                basicsSection,
                R.string.settings_remote_mode,
                8,
                () -> !syncing,
                this::saveRemoteMode);

        remoteKeyPresetSpinner = SettingsRowBuilder.labeledControl(
                context,
                basicsSection,
                R.string.settings_remote_key_preset,
                SettingsRowBuilder.optionSpinner(
                        context,
                        REMOTE_KEY_PRESET_ORDER,
                        () -> !syncing,
                        this::saveRemoteKeyPreset),
                12);

        remoteImeShortcutSpinner = SettingsRowBuilder.labeledControl(
                context,
                basicsSection,
                R.string.settings_remote_ime_shortcut,
                SettingsRowBuilder.optionSpinner(
                        context,
                        REMOTE_IME_SHORTCUT_ORDER,
                        () -> !syncing,
                        this::saveRemoteImeShortcut),
                12);

        SettingsRowBuilder.secondaryLabelRow(
                context,
                basicsSection,
                R.string.settings_remote_mode_help,
                12);

        remoteAutoModeCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                automaticSection,
                R.string.settings_remote_auto_mode,
                8,
                () -> !syncing,
                this::saveRemoteAutoMode);

        showCurrentAppProfileCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                automaticSection,
                R.string.settings_show_current_app_profile,
                8,
                () -> !syncing,
                this::saveShowCurrentAppProfile);

        currentAppProfileSummaryValue = SettingsRowBuilder.bodyLabelRow(
                context,
                automaticSection,
                "",
                6);

        remoteAutoPackagesInput = addPackageListPreference(
                automaticSection,
                R.string.settings_remote_auto_packages,
                KeyboardPreferences.loadRemoteAutoModePackages(context),
                packages -> KeyboardPreferences.saveRemoteAutoModePackages(context, packages));

        SettingsRowBuilder.secondaryLabelRow(
                context,
                automaticSection,
                R.string.settings_remote_auto_packages_help,
                6);

        SettingsRowBuilder.secondaryLabelRow(
                context,
                appOverridesSection,
                R.string.settings_app_profile_overrides_help,
                0);
        appProfileAsciiPackagesInput = addPackageListPreference(
                appOverridesSection,
                R.string.settings_app_profile_ascii_packages,
                KeyboardPreferences.loadAppProfileAsciiPackages(context),
                packages -> KeyboardPreferences.saveAppProfileAsciiPackages(context, packages));
        appProfileNumberRowPackagesInput = addPackageListPreference(
                appOverridesSection,
                R.string.settings_app_profile_number_row_packages,
                KeyboardPreferences.loadAppProfileNumberRowPackages(context),
                packages -> KeyboardPreferences.saveAppProfileNumberRowPackages(context, packages));
        appProfileNoComposingPackagesInput = addPackageListPreference(
                appOverridesSection,
                R.string.settings_app_profile_no_composing_packages,
                KeyboardPreferences.loadAppProfileNoComposingPackages(context),
                packages -> KeyboardPreferences.saveAppProfileNoComposingPackages(context, packages));
        appProfileNoTextConveniencesPackagesInput = addPackageListPreference(
                appOverridesSection,
                R.string.settings_app_profile_no_text_conveniences_packages,
                KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(context),
                packages -> KeyboardPreferences.saveAppProfileNoTextConveniencesPackages(context, packages));
    }

    void sync(KeyboardSettings settings) {
        if (remoteModeCheckBox == null) {
            return;
        }
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(settings);
        boolean remoteAutoModeEnabled = KeyboardPreferences.loadRemoteAutoModeEnabled(context);
        boolean showProfile = KeyboardPreferences.loadShowCurrentAppProfile(context);

        syncing = true;
        remoteModeCheckBox.setChecked(safe.remoteModeEnabled);
        remoteAutoModeCheckBox.setChecked(remoteAutoModeEnabled);
        showCurrentAppProfileCheckBox.setChecked(showProfile);
        currentAppProfileSummaryValue.setVisibility(showProfile ? View.VISIBLE : View.GONE);
        currentAppProfileSummaryValue.setText(currentAppProfileSummary());
        setPackageListTextIfNotFocused(
                remoteAutoPackagesInput,
                KeyboardPreferences.loadRemoteAutoModePackages(context));
        remoteAutoPackagesInput.setEnabled(remoteAutoModeEnabled);
        setPackageListTextIfNotFocused(
                appProfileAsciiPackagesInput,
                KeyboardPreferences.loadAppProfileAsciiPackages(context));
        setPackageListTextIfNotFocused(
                appProfileNumberRowPackagesInput,
                KeyboardPreferences.loadAppProfileNumberRowPackages(context));
        setPackageListTextIfNotFocused(
                appProfileNoComposingPackagesInput,
                KeyboardPreferences.loadAppProfileNoComposingPackages(context));
        setPackageListTextIfNotFocused(
                appProfileNoTextConveniencesPackagesInput,
                KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(context));
        remoteKeyPresetSpinner.setSelection(RemoteKeyPreset.indexOf(safe.remoteKeyPreset));
        remoteImeShortcutSpinner.setSelection(RemoteImeShortcut.indexOf(safe.remoteImeShortcut));
        remoteKeyPresetSpinner.setEnabled(safe.remoteModeEnabled || remoteAutoModeEnabled);
        remoteImeShortcutSpinner.setEnabled(safe.remoteModeEnabled || remoteAutoModeEnabled);
        syncing = false;
    }

    private String currentAppProfileSummary() {
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        return context.getString(
                R.string.current_app_profile_summary_format,
                context.getString(currentSettings.remoteModeEnabled ? R.string.state_on : R.string.state_off),
                context.getString(KeyboardPreferences.loadRemoteAutoModeEnabled(context)
                        ? R.string.state_on
                        : R.string.state_off),
                packageListOrDash(KeyboardPreferences.loadRemoteAutoModePackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileAsciiPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNumberRowPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNoComposingPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(context)));
    }

    private static String packageListOrDash(String value) {
        String trimmed = RuntimeDefaults.trimmedStringOrEmpty(value);
        if (trimmed.isEmpty()) {
            return "-";
        }
        return trimmed;
    }

    private void saveRemoteMode(boolean isChecked) {
        KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
        settingsSaver.accept(settings.withRemoteOptions(
                isChecked,
                settings.remoteKeyPreset,
                settings.remoteImeShortcut));
    }

    private void saveRemoteKeyPreset(RemoteKeyPreset preset) {
        KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
        settingsSaver.accept(settings.withRemoteOptions(
                settings.remoteModeEnabled,
                preset,
                settings.remoteImeShortcut));
    }

    private void saveRemoteImeShortcut(RemoteImeShortcut shortcut) {
        KeyboardSettings settings = RuntimeDefaults.keyboardSettingsFrom(this.settings);
        settingsSaver.accept(settings.withRemoteOptions(
                settings.remoteModeEnabled,
                settings.remoteKeyPreset,
                shortcut));
    }

    private void saveRemoteAutoMode(boolean isChecked) {
        KeyboardPreferences.saveRemoteAutoModeEnabled(context, isChecked);
        controlsSyncer.run();
    }

    private void saveShowCurrentAppProfile(boolean isChecked) {
        KeyboardPreferences.saveShowCurrentAppProfile(context, isChecked);
        controlsSyncer.run();
    }

    private EditText addPackageListPreference(
            LinearLayout root,
            int labelResId,
            String initialValue,
            Consumer<String> saver) {
        EditText input = packageListInput(initialValue, saver);
        return SettingsRowBuilder.labeledControl(context, root, labelResId, input, 8);
    }

    private EditText packageListInput(String initialValue, Consumer<String> saver) {
        Consumer<String> safeSaver = RuntimeDefaults.stringConsumer(saver);
        EditText input = SettingsRowBuilder.editText(
                context,
                initialValue,
                () -> !syncing,
                safeSaver);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return input;
    }

    private void setPackageListTextIfNotFocused(EditText input, String value) {
        if (input == null || input.hasFocus()) {
            return;
        }
        String safeValue = RuntimeDefaults.stringOrDefault(value, "");
        if (!safeValue.equals(input.getText().toString())) {
            input.setText(safeValue);
        }
    }

}
