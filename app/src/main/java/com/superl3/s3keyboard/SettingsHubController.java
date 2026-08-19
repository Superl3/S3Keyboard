package com.superl3.s3keyboard;

import android.app.Activity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class SettingsHubController {
    private final Activity activity;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final GesturePracticeInputController gesturePracticeInputController;
    private TextView currentStateLabel;
    private TextView keyboardSetupStatus;
    private Button enableButton;
    private Button pickerButton;
    private ThemeHubSettingsController themeHubSettingsController;

    SettingsHubController(
            Activity activity,
            Supplier<KeyboardSettings> settings,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver) {
        this.activity = activity;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.gesturePracticeInputController = new GesturePracticeInputController(activity);
    }

    void addTo(
            LinearLayout root,
            DemoFieldProfile demoFieldProfile,
            boolean demoShowKeyboard) {
        SettingsRowBuilder.bodyLabelRow(activity, root, R.string.gesture_practice_body, 0);
        currentStateLabel = SettingsRowBuilder.secondaryLabel(activity, "");
        root.addView(currentStateLabel, SettingsRowBuilder.matchWrapWithTop(activity, 6));

        SettingsRowBuilder.labelRow(activity, root, R.string.settings_hub_keyboard_setup, 12);
        keyboardSetupStatus = SettingsRowBuilder.secondaryLabel(activity, "");
        root.addView(keyboardSetupStatus, SettingsRowBuilder.matchWrapWithTop(activity, 4));
        LinearLayout setupRow = SettingsRowBuilder.horizontal(activity);
        enableButton = SettingsRowBuilder.button(
                activity,
                R.string.settings_hub_enable_keyboard,
                view -> AndroidImeActions.openInputSettings(activity));
        SettingsViewStyler.buttonIcon(
                enableButton,
                activity,
                R.drawable.ic_keyboard_settings);
        enableButton.setContentDescription(activity.getString(R.string.open_input_settings));
        setupRow.addView(enableButton, SettingsRowBuilder.weightedWrap(activity, 1f, 0, 4));

        pickerButton = SettingsRowBuilder.button(
                activity,
                R.string.settings_hub_choose_keyboard,
                view -> AndroidImeActions.showInputPicker(activity));
        SettingsViewStyler.buttonIcon(
                pickerButton,
                activity,
                R.drawable.ic_keyboard_keyboard);
        pickerButton.setContentDescription(activity.getString(R.string.show_input_picker));
        setupRow.addView(pickerButton, SettingsRowBuilder.weightedWrap(activity, 1f, 4, 0));
        root.addView(setupRow, SettingsRowBuilder.matchWrapWithTop(activity, 6));

        EditText testInput = gesturePracticeInputController.createInput(
                demoFieldProfile,
                demoShowKeyboard);
        root.addView(testInput, SettingsRowBuilder.matchWrapWithTop(activity, 12));

        themeHubSettingsController = new ThemeHubSettingsController(
                activity,
                settings,
                currentThemeCustomMarker,
                settingsSaver);
        themeHubSettingsController.addTo(root);
        sync();
    }

    void sync() {
        if (currentStateLabel == null) {
            return;
        }
        KeyboardSettings current = RuntimeDefaults.keyboardSettingsFrom(settings);
        KeyboardLayoutProfiles profiles = KeyboardPreferences.loadLayoutProfiles(activity);
        currentStateLabel.setText(activity.getString(
                R.string.settings_hub_current_state_format,
                SettingsDisplayLabels.label(activity, profiles.hangulLayout),
                SettingsDisplayLabels.label(activity, profiles.englishLayout),
                SettingsDisplayLabels.label(activity, current.handednessMode),
                activity.getString(KeyboardPreferences.loadSingleTapCommitModeEnabled(activity)
                        ? R.string.state_on
                        : R.string.state_off),
                activity.getString(current.remoteModeEnabled ? R.string.state_on : R.string.state_off)));
        syncKeyboardSetupStatus();
        if (themeHubSettingsController != null) {
            themeHubSettingsController.sync();
        }
    }

    private void syncKeyboardSetupStatus() {
        if (keyboardSetupStatus == null || enableButton == null || pickerButton == null) {
            return;
        }
        AndroidImeStatus.State state = AndroidImeStatus.resolve(activity);
        int statusResId;
        switch (state) {
            case SELECTED:
                statusResId = R.string.settings_hub_keyboard_selected;
                break;
            case ENABLED:
                statusResId = R.string.settings_hub_keyboard_enabled;
                break;
            case DISABLED:
            default:
                statusResId = R.string.settings_hub_keyboard_disabled;
                break;
        }
        keyboardSetupStatus.setText(statusResId);
        enableButton.setText(state == AndroidImeStatus.State.DISABLED
                ? R.string.settings_hub_enable_keyboard
                : R.string.settings_hub_manage_keyboards);
        pickerButton.setEnabled(state != AndroidImeStatus.State.DISABLED);
        pickerButton.setAlpha(state == AndroidImeStatus.State.DISABLED ? 0.45f : 1f);
        SettingsViewStyler.button(
                enableButton,
                activity,
                state == AndroidImeStatus.State.DISABLED);
        SettingsViewStyler.button(
                pickerButton,
                activity,
                state == AndroidImeStatus.State.ENABLED);
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        gesturePracticeInputController.hideKeyboardWhenTouchingOutside(event);
    }
}
