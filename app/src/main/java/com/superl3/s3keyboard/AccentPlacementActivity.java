package com.superl3.s3keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.EnumSet;

public final class AccentPlacementActivity extends Activity {
    private KeyboardSettings settings;
    private boolean syncing;
    private CheckBox themeDefaultCheckBox;
    private CheckBox settingsEnterCheckBox;
    private CheckBox metaCheckBox;
    private CheckBox qwertyShiftCheckBox;
    private CheckBox backspaceCheckBox;
    private CheckBox dingulDotCheckBox;
    private CheckBox dingulSlashCheckBox;
    private CheckBox escPointCheckBox;
    private Spinner spaceRoleSpinner;
    private Spinner questionRoleSpinner;
    private Spinner numberRowModeSpinner;
    private HangulKeyboardView qwertyPreview;
    private HangulKeyboardView dingulPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        settings = KeyboardPreferences.load(this);
        setContentView(createContentView());
        syncControls();
    }

    private View createContentView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(SettingsUiPalette.from(this).background);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(24));
        scrollView.addView(root);

        TextView title = label("\uC2DC\uAC01 \uC5ED\uD560 \uD3B8\uC9D1");
        title.setTextSize(20);
        root.addView(title, matchWrap());

        TextView helper = label("\uD14C\uB9C8\uC758 alpha / modifier / accent \uC5ED\uD560\uC744 \uD0A4 \uC704\uCE58\uBCC4\uB85C \uC801\uC6A9\uD569\uB2C8\uB2E4.");
        root.addView(helper, topParams(6));

        root.addView(sectionLabel("QWERTY"), topParams(16));
        qwertyPreview = previewKeyboard(KeyboardMode.ENGLISH);
        root.addView(qwertyPreview, previewParams(118));

        root.addView(sectionLabel("\uB529\uAD74"), topParams(14));
        dingulPreview = previewKeyboard(KeyboardMode.HANGUL);
        root.addView(dingulPreview, previewParams(144));

        themeDefaultCheckBox = checkBox("\uD14C\uB9C8 \uAE30\uBCF8 \uBC30\uCE58 \uC0AC\uC6A9");
        themeDefaultCheckBox.setOnClickListener(v -> {
            if (!syncing) {
                AccentPlacementPolicy current = KeyboardPreferences.loadAccentPlacementPolicy(this);
                savePolicy(current.themeDefault ? AccentPlacementPolicy.none() : AccentPlacementPolicy.themeDefault());
            }
        });
        root.addView(themeDefaultCheckBox, topParams(18));

        settingsEnterCheckBox = targetCheckBox(AccentPlacementTarget.SETTINGS_ENTER);
        metaCheckBox = targetCheckBox(AccentPlacementTarget.META);
        qwertyShiftCheckBox = targetCheckBox(AccentPlacementTarget.QWERTY_SHIFT);
        backspaceCheckBox = targetCheckBox(AccentPlacementTarget.BACKSPACE);
        dingulDotCheckBox = targetCheckBox(AccentPlacementTarget.DINGUL_DOT);
        dingulSlashCheckBox = targetCheckBox(AccentPlacementTarget.DINGUL_SLASH);
        escPointCheckBox = targetCheckBox(AccentPlacementTarget.ESC_POINT);
        root.addView(settingsEnterCheckBox, topParams(8));
        root.addView(metaCheckBox, topParams(4));
        root.addView(qwertyShiftCheckBox, topParams(4));
        root.addView(backspaceCheckBox, topParams(4));
        root.addView(dingulDotCheckBox, topParams(4));
        root.addView(dingulSlashCheckBox, topParams(4));
        root.addView(escPointCheckBox, topParams(4));

        root.addView(sectionLabel("Spacebar"), topParams(16));
        spaceRoleSpinner = spaceRoleSpinner();
        root.addView(spaceRoleSpinner, topParams(6));

        root.addView(sectionLabel("? key"), topParams(16));
        questionRoleSpinner = questionRoleSpinner();
        root.addView(questionRoleSpinner, topParams(6));

        root.addView(sectionLabel("Number row preview"), topParams(16));
        numberRowModeSpinner = numberRowModeSpinner();
        root.addView(numberRowModeSpinner, topParams(6));

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        Button noneButton = actionButton("Accent \uC5C6\uC74C");
        noneButton.setOnClickListener(v -> savePolicy(AccentPlacementPolicy.none()));
        Button allButton = actionButton("\uBAA8\uB450 \uC120\uD0DD");
        allButton.setOnClickListener(v -> savePolicy(AccentPlacementPolicy.of(
                EnumSet.allOf(AccentPlacementTarget.class),
                AccentPlacementPolicy.SpaceRole.ACCENT,
                AccentPlacementPolicy.QuestionRole.ACCENT)));
        buttonRow.addView(noneButton, weightedButtonParams());
        buttonRow.addView(allButton, weightedButtonParams());
        root.addView(buttonRow, topParams(14));

        Button closeButton = actionButton("\uB2EB\uAE30");
        closeButton.setOnClickListener(v -> finish());
        root.addView(closeButton, topParams(16));
        return scrollView;
    }

    private CheckBox targetCheckBox(AccentPlacementTarget target) {
        CheckBox checkBox = checkBox(target.label);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!syncing) {
                savePolicy(policyFromControls());
            }
        });
        return checkBox;
    }

    private void syncControls() {
        syncing = true;
        AccentPlacementPolicy policy = KeyboardPreferences.loadAccentPlacementPolicy(this);
        boolean userAccentLocked = KeyboardPreferences.selectedThemeLocksUserAccentPlacement(this);
        themeDefaultCheckBox.setChecked(policy.themeDefault || userAccentLocked);
        themeDefaultCheckBox.setEnabled(!userAccentLocked);
        settingsEnterCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.SETTINGS_ENTER));
        metaCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.META));
        qwertyShiftCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.QWERTY_SHIFT));
        backspaceCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.BACKSPACE));
        dingulDotCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.DINGUL_DOT));
        dingulSlashCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.DINGUL_SLASH));
        escPointCheckBox.setChecked(!policy.themeDefault && !userAccentLocked
                && policy.contains(AccentPlacementTarget.ESC_POINT));
        spaceRoleSpinner.setSelection(policy.spaceRole.ordinal());
        questionRoleSpinner.setSelection(policy.questionRole.ordinal());
        numberRowModeSpinner.setSelection(settings.additionalNumberRowColorMode.ordinal());
        boolean customPlacementEnabled = !policy.themeDefault && !userAccentLocked;
        settingsEnterCheckBox.setEnabled(customPlacementEnabled);
        metaCheckBox.setEnabled(customPlacementEnabled);
        qwertyShiftCheckBox.setEnabled(customPlacementEnabled);
        backspaceCheckBox.setEnabled(customPlacementEnabled);
        dingulDotCheckBox.setEnabled(customPlacementEnabled);
        dingulSlashCheckBox.setEnabled(customPlacementEnabled);
        escPointCheckBox.setEnabled(customPlacementEnabled);
        spaceRoleSpinner.setEnabled(customPlacementEnabled);
        questionRoleSpinner.setEnabled(customPlacementEnabled);
        updatePreviews(policy);
        syncing = false;
    }

    private AccentPlacementPolicy policyFromControls() {
        EnumSet<AccentPlacementTarget> targets = EnumSet.noneOf(AccentPlacementTarget.class);
        if (settingsEnterCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.SETTINGS_ENTER);
        }
        if (metaCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.META);
        }
        if (qwertyShiftCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.QWERTY_SHIFT);
        }
        if (backspaceCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.BACKSPACE);
        }
        if (dingulDotCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.DINGUL_DOT);
        }
        if (dingulSlashCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.DINGUL_SLASH);
        }
        if (escPointCheckBox.isChecked()) {
            targets.add(AccentPlacementTarget.ESC_POINT);
        }
        AccentPlacementPolicy.SpaceRole spaceRole =
                AccentPlacementPolicy.SpaceRole.values()[spaceRoleSpinner.getSelectedItemPosition()];
        AccentPlacementPolicy.QuestionRole questionRole =
                AccentPlacementPolicy.QuestionRole.values()[questionRoleSpinner.getSelectedItemPosition()];
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
        HangulKeyboardView preview = new HangulKeyboardView(this);
        preview.setCompactPreviewRendering(true);
        KeyboardSettings previewSettings = settings.withKeyboardMode(mode)
                .withEnglishNumberRow(true)
                .withHangulNumberRow(true)
                .withHintVisibility(false, false, false);
        preview.setSettings(previewSettings);
        preview.setOnTouchListener((v, event) -> true);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }

    private Spinner spaceRoleSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<AccentPlacementPolicy.SpaceRole> adapter = new SettingsArrayAdapter<>(
                this,
                AccentPlacementPolicy.SpaceRole.values());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    savePolicy(policyFromControls());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner questionRoleSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<AccentPlacementPolicy.QuestionRole> adapter = new SettingsArrayAdapter<>(
                this,
                AccentPlacementPolicy.QuestionRole.values());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    savePolicy(policyFromControls());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner numberRowModeSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<AdditionalNumberRowColorMode> adapter = new SettingsArrayAdapter<>(
                this,
                AdditionalNumberRowColorMode.values());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    settings = settings.withAdditionalNumberRowColorMode(
                            AdditionalNumberRowColorMode.values()[position]);
                    KeyboardPreferences.saveSettings(AccentPlacementActivity.this, settings);
                    updatePreviews(KeyboardPreferences.loadAccentPlacementPolicy(
                            AccentPlacementActivity.this));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(SettingsUiPalette.from(this).textPrimary);
        label.setTextSize(14);
        return label;
    }

    private TextView sectionLabel(String text) {
        TextView label = label(text);
        label.setTextSize(16);
        label.setGravity(Gravity.START);
        return label;
    }

    private CheckBox checkBox(String text) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        SettingsViewStyler.compoundButton(checkBox, this);
        return checkBox;
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        SettingsViewStyler.button(button, this, false);
        return button;
    }

    private LinearLayout.LayoutParams previewParams(int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp));
        params.topMargin = dp(6);
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

    private LinearLayout.LayoutParams topParams(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
