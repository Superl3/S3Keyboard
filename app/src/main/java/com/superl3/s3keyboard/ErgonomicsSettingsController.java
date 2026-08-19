package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class ErgonomicsSettingsController {
    private static final KeyboardErgonomicsPreset[] PRESET_ORDER =
            KeyboardErgonomicsPreset.displayOrder();
    private static final VisualConsistencyLevel[] VISUAL_CONSISTENCY_ORDER =
            VisualConsistencyLevel.displayOrder();

    private final Context context;
    private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions;
    private final Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver;
    private Spinner presetSpinner;
    private Spinner visualConsistencyLevelSpinner;
    private CheckBox mainKeyCenteringCheckBox;
    private CheckBox compactFunctionRailCheckBox;
    private CheckBox ergonomicHitboxCheckBox;
    private CheckBox ergonomicPositionAdjustCheckBox;
    private CheckBox leftAssistRailCheckBox;
    private CheckBox uniformGridGapCheckBox;
    private TextView presetStateValue;
    private boolean syncing = true;

    ErgonomicsSettingsController(
            Context context,
            Supplier<KeyboardErgonomicsOptions> ergonomicsOptions,
            Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver) {
        this.context = context;
        this.ergonomicsOptions = RuntimeDefaults.keyboardErgonomicsSupplier(ergonomicsOptions);
        this.ergonomicsOptionsSaver =
                RuntimeDefaults.keyboardErgonomicsConsumer(ergonomicsOptionsSaver);
    }

    void addTo(LinearLayout root) {
        SettingsRowBuilder.labelRow(context, root, R.string.settings_ergonomics_title, 16);
        presetSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_ergonomics_preset,
                SettingsRowBuilder.optionSpinner(
                        context,
                        PRESET_ORDER,
                        () -> !syncing,
                        preset -> ergonomicsOptionsSaver.accept(preset.options)),
                8);

        presetStateValue = SettingsRowBuilder.labelRow(context, root, "", 4);

        mainKeyCenteringCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_main_key_centering,
                8,
                (options, isChecked) -> options.withMainKeyCentering(isChecked));
        leftAssistRailCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_left_assist_rail,
                4,
                (options, isChecked) -> options.withLeftAssistRail(isChecked));
        SettingsRowBuilder.secondaryLabelRow(
                context,
                root,
                R.string.settings_left_assist_rail_summary,
                2);
        uniformGridGapCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_uniform_grid_gap,
                4,
                (options, isChecked) -> options.withUniformGridGap(isChecked));
        compactFunctionRailCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_compact_function_rail,
                4,
                (options, isChecked) -> options.withCompactFunctionRail(isChecked));
        ergonomicHitboxCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_ergonomic_hitbox,
                4,
                (options, isChecked) -> options.withErgonomicHitbox(isChecked));
        ergonomicPositionAdjustCheckBox = addErgonomicsCheckBox(
                root,
                R.string.settings_ergonomic_position_adjust,
                4,
                (options, isChecked) -> options.withErgonomicPositionAdjust(isChecked));

        visualConsistencyLevelSpinner = SettingsRowBuilder.labeledControl(
                context,
                root,
                R.string.settings_visual_consistency_level,
                SettingsRowBuilder.optionSpinner(
                        context,
                        VISUAL_CONSISTENCY_ORDER,
                        () -> !syncing,
                        level -> ergonomicsOptionsSaver.accept(
                                RuntimeDefaults.keyboardErgonomicsFrom(ergonomicsOptions)
                                        .withVisualConsistencyLevel(level))),
                8);
    }

    void sync() {
        if (presetSpinner == null) {
            return;
        }
        KeyboardErgonomicsOptions options =
                RuntimeDefaults.keyboardErgonomicsFrom(ergonomicsOptions);
        KeyboardErgonomicsPreset matchingPreset = KeyboardErgonomicsPreset.findMatching(options);

        syncing = true;
        if (matchingPreset != null) {
            presetSpinner.setSelection(KeyboardErgonomicsPreset.indexOf(matchingPreset));
        }
        String state = matchingPreset == null
                ? context.getString(R.string.settings_custom_state)
                : SettingsDisplayLabels.label(context, matchingPreset);
        presetStateValue.setText(SettingsValueFormatter.currentState(context, state));
        visualConsistencyLevelSpinner.setSelection(
                VisualConsistencyLevel.indexOf(options.visualConsistencyLevel));
        mainKeyCenteringCheckBox.setChecked(options.mainKeyCenteringEnabled);
        leftAssistRailCheckBox.setChecked(options.leftAssistRailEnabled);
        uniformGridGapCheckBox.setChecked(options.uniformGridGapEnabled);
        leftAssistRailCheckBox.setEnabled(options.mainKeyCenteringEnabled);
        uniformGridGapCheckBox.setEnabled(options.mainKeyCenteringEnabled);
        compactFunctionRailCheckBox.setChecked(options.compactFunctionRailEnabled);
        ergonomicHitboxCheckBox.setChecked(options.ergonomicHitboxEnabled);
        ergonomicPositionAdjustCheckBox.setChecked(options.ergonomicPositionAdjustEnabled);
        syncing = false;
    }

    private CheckBox addErgonomicsCheckBox(
            LinearLayout root,
            int labelResId,
            int topMarginDp,
            BiFunction<KeyboardErgonomicsOptions, Boolean, KeyboardErgonomicsOptions> change) {
        return SettingsRowBuilder.checkBoxRow(
                context,
                root,
                labelResId,
                topMarginDp,
                () -> !syncing,
                isChecked -> ergonomicsOptionsSaver.accept(change.apply(
                        RuntimeDefaults.keyboardErgonomicsFrom(ergonomicsOptions),
                        isChecked)));
    }
}
