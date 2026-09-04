package com.superl3.s3keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.EnumSet;

public final class AccentPlacementActivity extends Activity {
    private static final AccentPlacementPolicy.SpaceRole[] SPACE_ROLE_ORDER =
            AccentPlacementPolicy.SpaceRole.displayOrder();
    private static final AccentPlacementPolicy.QuestionRole[] QUESTION_ROLE_ORDER =
            AccentPlacementPolicy.QuestionRole.displayOrder();
    private static final AdditionalNumberRowColorMode[] NUMBER_ROW_COLOR_MODE_ORDER =
            AdditionalNumberRowColorMode.displayOrder();
    private static final AccentPlacementTarget[] TARGET_ORDER =
            AccentPlacementTarget.displayOrder();

    private KeyboardSettings settings;
    private boolean syncing;
    private CheckBox themeDefaultCheckBox;
    private final EnumMap<AccentPlacementTarget, CheckBox> targetCheckBoxes =
            new EnumMap<>(AccentPlacementTarget.class);
    private Spinner spaceRoleSpinner;
    private Spinner questionRoleSpinner;
    private Spinner numberRowModeSpinner;
    private HangulKeyboardView qwertyPreview;
    private HangulKeyboardView dingulPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        settings = KeyboardPreferences.load(this);
        View content = createContentView();
        SettingsSystemBars.applyTopInset(content);
        setContentView(content);
        syncControls();
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(SettingsUiPalette.from(this).background);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setPadding(
                SettingsRowBuilder.dp(this, 16),
                SettingsRowBuilder.dp(this, 18),
                SettingsRowBuilder.dp(this, 16),
                SettingsRowBuilder.dp(this, 24));
        scrollView.addView(root);

        TextView title = SettingsRowBuilder.label(this, R.string.accent_placement_title);
        title.setTextSize(20);
        SettingsRowBuilder.addView(root, title);

        SettingsRowBuilder.labelRow(this, root, R.string.accent_placement_helper, 6);

        SettingsRowBuilder.sectionLabelRow(this, root, R.string.accent_placement_qwerty_section, 16);
        qwertyPreview = previewKeyboard(KeyboardMode.ENGLISH);
        root.addView(qwertyPreview, SettingsRowBuilder.matchHeightWithTop(this, 118, 6));

        SettingsRowBuilder.sectionLabelRow(this, root, R.string.accent_placement_dingul_section, 14);
        dingulPreview = previewKeyboard(KeyboardMode.HANGUL);
        root.addView(dingulPreview, SettingsRowBuilder.matchHeightWithTop(this, 144, 6));

        themeDefaultCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                getString(R.string.accent_placement_theme_default),
                18,
                () -> !syncing,
                isChecked -> savePolicy(isChecked
                        ? AccentPlacementPolicy.themeDefault()
                        : AccentPlacementPolicy.none()));

        for (AccentPlacementTarget target : TARGET_ORDER) {
            CheckBox checkBox = SettingsRowBuilder.checkBoxRow(
                    this,
                    root,
                    getString(target.labelResId),
                    targetCheckBoxes.size() == 0 ? 8 : 4,
                    () -> !syncing,
                    isChecked -> savePolicy(policyFromControls()));
            targetCheckBoxes.put(target, checkBox);
        }

        SettingsRowBuilder.sectionLabelRow(this, root, R.string.accent_placement_spacebar_section, 16);
        spaceRoleSpinner = spaceRoleSpinner();
        SettingsRowBuilder.addViewWithTop(this, root, spaceRoleSpinner, 6);

        SettingsRowBuilder.sectionLabelRow(this, root, R.string.accent_placement_question_section, 16);
        questionRoleSpinner = questionRoleSpinner();
        SettingsRowBuilder.addViewWithTop(this, root, questionRoleSpinner, 6);

        SettingsRowBuilder.sectionLabelRow(this, root, R.string.accent_placement_number_row_section, 16);
        numberRowModeSpinner = numberRowModeSpinner();
        SettingsRowBuilder.addViewWithTop(this, root, numberRowModeSpinner, 6);

        LinearLayout buttonRow = SettingsRowBuilder.horizontal(this);
        SettingsRowBuilder.weightedButton(
                this,
                buttonRow,
                R.string.accent_placement_none,
                3,
                3,
                v -> savePolicy(AccentPlacementPolicy.none()));
        SettingsRowBuilder.weightedButton(
                this,
                buttonRow,
                R.string.accent_placement_select_all,
                3,
                3,
                v -> savePolicy(AccentPlacementPolicy.of(
                        AccentPlacementTarget.allDisplayTargets(),
                        AccentPlacementPolicy.SpaceRole.ACCENT,
                        AccentPlacementPolicy.QuestionRole.ACCENT)));
        SettingsRowBuilder.addViewWithTop(this, root, buttonRow, 14);

        SettingsRowBuilder.buttonRow(this, root, R.string.action_close, 16, v -> finish());
        return scrollView;
    }

    private void syncControls() {
        syncing = true;
        AccentPlacementPolicy policy = KeyboardPreferences.loadAccentPlacementPolicy(this);
        boolean userAccentLocked = KeyboardPreferences.selectedThemeLocksUserAccentPlacement(this);
        boolean customPlacementEnabled = !policy.themeDefault && !userAccentLocked;
        themeDefaultCheckBox.setChecked(policy.themeDefault || userAccentLocked);
        themeDefaultCheckBox.setEnabled(!userAccentLocked);
        for (AccentPlacementTarget target : TARGET_ORDER) {
            CheckBox checkBox = targetCheckBoxes.get(target);
            if (checkBox != null) {
                checkBox.setChecked(customPlacementEnabled && policy.contains(target));
                checkBox.setEnabled(customPlacementEnabled);
            }
        }
        spaceRoleSpinner.setSelection(AccentPlacementPolicy.SpaceRole.indexOf(policy.spaceRole));
        questionRoleSpinner.setSelection(AccentPlacementPolicy.QuestionRole.indexOf(policy.questionRole));
        numberRowModeSpinner.setSelection(AdditionalNumberRowColorMode.indexOf(
                settings.additionalNumberRowColorMode));
        spaceRoleSpinner.setEnabled(customPlacementEnabled);
        questionRoleSpinner.setEnabled(customPlacementEnabled);
        numberRowModeSpinner.setEnabled(customPlacementEnabled);
        updatePreviews(policy);
        syncing = false;
    }

    private AccentPlacementPolicy policyFromControls() {
        EnumSet<AccentPlacementTarget> targets = EnumSet.noneOf(AccentPlacementTarget.class);
        for (AccentPlacementTarget target : TARGET_ORDER) {
            CheckBox checkBox = targetCheckBoxes.get(target);
            if (checkBox != null && checkBox.isChecked()) {
                targets.add(target);
            }
        }
        AccentPlacementPolicy.SpaceRole spaceRole =
                AccentPlacementPolicy.SpaceRole.at(spaceRoleSpinner.getSelectedItemPosition());
        AccentPlacementPolicy.QuestionRole questionRole =
                AccentPlacementPolicy.QuestionRole.at(questionRoleSpinner.getSelectedItemPosition());
        return AccentPlacementPolicy.of(targets, spaceRole, questionRole);
    }

    private void savePolicy(AccentPlacementPolicy policy) {
        KeyboardPreferences.saveAccentPlacementPolicy(this, policy);
        settings = KeyboardPreferences.load(this);
        syncControls();
    }

    private void updatePreviews(AccentPlacementPolicy policy) {
        if (qwertyPreview != null) {
            qwertyPreview.setSettings(policy.applyTo(settings.withKeyboardMode(KeyboardMode.ENGLISH)
                            .withEnglishNumberRow(true))
                    .withHintVisibility(false, false, false));
        }
        if (dingulPreview != null) {
            dingulPreview.setSettings(policy.applyTo(settings.withKeyboardMode(KeyboardMode.HANGUL)
                            .withHangulNumberRow(true))
                    .withHintVisibility(false, false, false));
        }
    }

    private HangulKeyboardView previewKeyboard(KeyboardMode mode) {
        KeyboardSettings previewSettings = settings.withKeyboardMode(mode)
                .withEnglishNumberRow(true)
                .withHangulNumberRow(true)
                .withHintVisibility(false, false, false);
        return KeyboardPreviewFactory.nonInteractive(this, previewSettings);
    }

    private Spinner spaceRoleSpinner() {
        return SettingsRowBuilder.optionSpinner(
                this,
                SPACE_ROLE_ORDER,
                () -> !syncing,
                role -> savePolicy(policyFromControls()));
    }

    private Spinner questionRoleSpinner() {
        return SettingsRowBuilder.optionSpinner(
                this,
                QUESTION_ROLE_ORDER,
                () -> !syncing,
                role -> savePolicy(policyFromControls()));
    }

    private Spinner numberRowModeSpinner() {
        return SettingsRowBuilder.optionSpinner(
                this,
                NUMBER_ROW_COLOR_MODE_ORDER,
                () -> !syncing,
                mode -> {
                    settings = settings.withAdditionalNumberRowColorMode(mode);
                    KeyboardPreferences.saveSettings(AccentPlacementActivity.this, settings);
                    updatePreviews(KeyboardPreferences.loadAccentPlacementPolicy(
                            AccentPlacementActivity.this));
                });
    }

}
