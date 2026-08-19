package com.superl3.s3keyboard;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

final class OneFingerInputSettingsController {
    private static final OneFingerInputSpeedPreset[] SPEED_PRESETS =
            OneFingerInputSpeedPreset.displayOrder();

    private final Activity context;
    private final Runnable controlsSyncer;
    private final OneFingerPracticeController practiceController;
    private CheckBox enabledCheckBox;
    private TextView stateSummary;
    private LinearLayout enabledControls;
    private Spinner speedPresetSpinner;
    private Button advancedButton;
    private LinearLayout advancedControls;
    private TextView actionHoldValue;
    private SeekBar actionHoldSeekBar;
    private TextView targetDwellValue;
    private SeekBar targetDwellSeekBar;
    private boolean advancedVisible;
    private boolean syncing;

    OneFingerInputSettingsController(Activity context, Runnable controlsSyncer) {
        this.context = context;
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
        this.practiceController = new OneFingerPracticeController(context);
    }

    void addTo(LinearLayout root) {
        OneFingerFlowGuideView flowGuide = new OneFingerFlowGuideView(context);
        root.addView(flowGuide, SettingsRowBuilder.matchWrapWithTop(context, 4));
        SettingsRowBuilder.bodyLabelRow(context, root, R.string.one_finger_input_guide, 0);

        enabledCheckBox = SettingsRowBuilder.checkBoxRow(
                context,
                root,
                R.string.single_tap_commit_mode,
                12,
                () -> !syncing,
                this::saveEnabled);
        stateSummary = SettingsRowBuilder.secondaryLabel(context, "");
        root.addView(stateSummary, SettingsRowBuilder.matchWrapWithTop(context, 4));

        enabledControls = SettingsRowBuilder.vertical(context);
        root.addView(enabledControls, SettingsRowBuilder.matchWrap());

        speedPresetSpinner = SettingsRowBuilder.labeledControl(
                context,
                enabledControls,
                R.string.one_finger_input_speed,
                SettingsRowBuilder.optionSpinner(
                        context,
                        SPEED_PRESETS,
                        () -> !syncing,
                        this::applySpeedPreset),
                10);

        practiceController.addTo(enabledControls);

        advancedButton = SettingsRowBuilder.buttonRow(
                context,
                enabledControls,
                R.string.one_finger_advanced_open,
                8,
                view -> toggleAdvanced());

        advancedControls = SettingsRowBuilder.vertical(context);
        enabledControls.addView(advancedControls, SettingsRowBuilder.matchWrap());

        actionHoldValue = SettingsRowBuilder.valueLabel(context);
        actionHoldSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                advancedControls,
                actionHoldValue,
                KeyboardPreferences.MAX_SINGLE_TAP_HOLD_MS
                        - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS,
                8,
                () -> !syncing,
                progress -> saveActionHold(
                        KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS + progress));

        targetDwellValue = SettingsRowBuilder.valueLabel(context);
        targetDwellSeekBar = SettingsRowBuilder.seekBarRow(
                context,
                advancedControls,
                targetDwellValue,
                KeyboardPreferences.MAX_SINGLE_TAP_HOLD_MS
                        - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS,
                8,
                () -> !syncing,
                progress -> saveTargetDwell(
                        KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS + progress));

        LinearLayout help = SettingsSubsection.add(
                context,
                root,
                R.string.one_finger_help_subsection,
                false).content;
        SettingsRowBuilder.secondaryLabelRow(context, help, R.string.one_finger_input_scope, 4);
        SettingsRowBuilder.secondaryLabelRow(context, help, R.string.one_finger_input_sequence, 8);
        SettingsRowBuilder.secondaryLabelRow(context, help, R.string.one_finger_input_feedback, 6);
        sync();
    }

    void sync() {
        if (enabledCheckBox == null) {
            return;
        }
        int actionHoldMs = KeyboardPreferences.loadSingleTapStartHoldMs(context);
        int targetDwellMs = KeyboardPreferences.loadSingleTapCommitHoldMs(context);
        OneFingerInputSpeedPreset preset =
                OneFingerInputSpeedPreset.findMatching(actionHoldMs, targetDwellMs);
        boolean enabled = KeyboardPreferences.loadSingleTapCommitModeEnabled(context);
        if (preset == OneFingerInputSpeedPreset.CUSTOM) {
            advancedVisible = true;
        }

        syncing = true;
        enabledCheckBox.setChecked(enabled);
        speedPresetSpinner.setSelection(OneFingerInputSpeedPreset.indexOf(preset), false);
        actionHoldSeekBar.setProgress(
                actionHoldMs - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS);
        targetDwellSeekBar.setProgress(
                targetDwellMs - KeyboardPreferences.MIN_SINGLE_TAP_HOLD_MS);
        actionHoldValue.setText(SettingsValueFormatter.singleTapStartHold(context, actionHoldMs));
        targetDwellValue.setText(SettingsValueFormatter.singleTapCommitHold(context, targetDwellMs));
        syncing = false;
        stateSummary.setText(enabled
                ? context.getString(
                        R.string.one_finger_current_state_format,
                        context.getString(preset.labelResId()),
                        targetDwellMs,
                        actionHoldMs)
                : context.getString(R.string.one_finger_current_state_off));
        updateVisibility(enabled);
        practiceController.onEnabledChanged(enabled);
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        practiceController.hideKeyboardWhenTouchingOutside(event);
    }

    private void saveEnabled(boolean enabled) {
        KeyboardPreferences.saveSingleTapCommitModeEnabled(context, enabled);
        updateVisibility(enabled);
        practiceController.onEnabledChanged(enabled);
        controlsSyncer.run();
    }

    private void applySpeedPreset(OneFingerInputSpeedPreset preset) {
        if (preset == null) {
            return;
        }
        if (!preset.isConcrete()) {
            advancedVisible = true;
            updateVisibility(KeyboardPreferences.loadSingleTapCommitModeEnabled(context));
            return;
        }
        advancedVisible = false;
        KeyboardPreferences.saveSingleTapStartHoldMs(context, preset.actionHoldMs);
        KeyboardPreferences.saveSingleTapCommitHoldMs(context, preset.targetDwellMs);
        controlsSyncer.run();
    }

    private void saveActionHold(int valueMs) {
        KeyboardPreferences.saveSingleTapStartHoldMs(context, valueMs);
        controlsSyncer.run();
    }

    private void saveTargetDwell(int valueMs) {
        KeyboardPreferences.saveSingleTapCommitHoldMs(context, valueMs);
        controlsSyncer.run();
    }

    private void toggleAdvanced() {
        advancedVisible = !advancedVisible;
        updateVisibility(KeyboardPreferences.loadSingleTapCommitModeEnabled(context));
    }

    private void updateVisibility(boolean enabled) {
        if (enabledControls == null) {
            return;
        }
        enabledControls.setVisibility(enabled ? View.VISIBLE : View.GONE);
        advancedControls.setVisibility(enabled && advancedVisible ? View.VISIBLE : View.GONE);
        advancedButton.setText(advancedVisible
                ? R.string.one_finger_advanced_close
                : R.string.one_finger_advanced_open);
    }
}
