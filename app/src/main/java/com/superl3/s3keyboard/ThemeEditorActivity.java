package com.superl3.s3keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public final class ThemeEditorActivity extends Activity {
    private static final int MODE_HANGUL_ID = 101;
    private static final int MODE_ENGLISH_ID = 102;
    private static final int EDIT_GLOBAL_ID = 201;
    private static final int EDIT_KEY_TEXT_ID = 202;

    private KeyboardSettings settings;
    private boolean syncing;
    private HangulKeyboardView preview;
    private GestureKey selectedKey;
    private String selectedOverrideKey = "";

    private Spinner themePresetSpinner;
    private Spinner keyIdleColorSpinner;
    private Spinner functionKeyColorSpinner;
    private Spinner primaryFunctionKeyColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner fontFamilySpinner;
    private Spinner handednessSpinner;
    private Spinner additionalNumberRowColorModeSpinner;
    private Spinner selectedKeyColorSpinner;
    private Button deleteThemeButton;
    private Button resetSelectedKeyButton;
    private RadioGroup modeGroup;
    private RadioGroup editScopeGroup;
    private SeekBar hangulHeightSeekBar;
    private SeekBar englishHeightSeekBar;
    private SeekBar hangulLeftPaddingSeekBar;
    private SeekBar hangulRightPaddingSeekBar;
    private SeekBar englishLeftPaddingSeekBar;
    private SeekBar englishRightPaddingSeekBar;
    private SeekBar hangulSpecialColumnSeekBar;
    private SeekBar hangulMainSpecialGapSeekBar;
    private SeekBar keyboardTopPaddingSeekBar;
    private SeekBar keyboardBottomPaddingSeekBar;
    private SeekBar bottomRowTopPaddingSeekBar;
    private SeekBar roundnessSeekBar;
    private SeekBar keyBorderWidthSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar keyDepthSeekBar;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private CheckBox hangulNumberRowCheckBox;
    private CheckBox englishNumberRowCheckBox;
    private CheckBox keyDepthCheckBox;
    private CheckBox customDepthColorCheckBox;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private CheckBox hangulSlideHintsCheckBox;
    private CheckBox englishSlideHintsCheckBox;
    private CheckBox beginnerTooltipPreviewCheckBox;
    private TextView selectedKeyLabel;
    private TextView previewMeta;
    private TextView hangulHeightValue;
    private TextView englishHeightValue;
    private TextView hangulLeftPaddingValue;
    private TextView hangulRightPaddingValue;
    private TextView englishLeftPaddingValue;
    private TextView englishRightPaddingValue;
    private TextView hangulSpecialColumnValue;
    private TextView hangulMainSpecialGapValue;
    private TextView keyboardTopPaddingValue;
    private TextView keyboardBottomPaddingValue;
    private TextView bottomRowTopPaddingValue;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedThemePresetIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        settings = KeyboardPreferences.load(this);
        setContentView(createContentView());
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        syncControls();
    }

    private View createContentView() {
        int padding = dp(16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(ui.background);

        TextView title = label("Theme Editor");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, matchWrap());

        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.HORIZONTAL);
        modeGroup.addView(radio(MODE_HANGUL_ID, "Hangul"));
        modeGroup.addView(radio(MODE_ENGLISH_ID, "QWERTY"));
        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (syncing) {
                return;
            }
            selectedKey = null;
            selectedOverrideKey = "";
            updateSettings(settings.withKeyboardMode(checkedId == MODE_ENGLISH_ID
                    ? KeyboardMode.ENGLISH
                    : KeyboardMode.HANGUL));
        });
        root.addView(modeGroup, matchWrapWithTop(8));

        previewMeta = label("");
        root.addView(previewMeta, matchWrapWithTop(6));

        preview = new HangulKeyboardView(this);
        preview.setOnPreviewKeySelectionListener(key -> {
            selectedKey = key;
            selectedOverrideKey = overrideKeyFor(key);
            editScopeGroup.check(EDIT_KEY_TEXT_ID);
            syncSelectedKeyInspector();
        });
        root.addView(preview, matchWrapWithTop(8));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout editorRoot = new LinearLayout(this);
        editorRoot.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(editorRoot);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f));

        addThemeSaveControls(editorRoot);
        addSelectedKeyInspector(editorRoot);
        addColorControls(addExpandableSection(editorRoot, "Colors", true));
        addShapeControls(addExpandableSection(editorRoot, "Shape", false));
        addLayoutControls(addExpandableSection(editorRoot, "User Layout (not theme)", false));
        addTypographyControls(addExpandableSection(editorRoot, "Typography", false));
        return root;
    }

    private void addThemeSaveControls(LinearLayout root) {
        Button saveThemeButton = new Button(this);
        saveThemeButton.setText("Save current theme");
        styleSystemButton(saveThemeButton);
        saveThemeButton.setOnClickListener(v -> {
            UserThemeStore.UserTheme saved = UserThemeStore.saveCurrent(this, settings);
            selectedThemePresetIndex = indexOfUserTheme(saved.id);
            KeyboardPreferences.saveSelectedThemeId(this, saved.id);
            refreshThemePresetAdapter();
            syncControls();
        });
        root.addView(saveThemeButton, buttonParams());
    }

    private void addPresetControls(LinearLayout root) {
        themePresetSpinner = new Spinner(this);
        refreshThemePresetAdapter();
        themePresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing || position <= 0) {
                    return;
                }
                selectedThemePresetIndex = position;
                selectedKey = null;
                selectedOverrideKey = "";
                settings = themeOptions[position].applyTo(settings);
                KeyboardPreferences.saveSelectedThemeId(
                        ThemeEditorActivity.this,
                        themeOptions[position].stableId());
                KeyboardPreferences.saveSettings(ThemeEditorActivity.this, settings);
                syncControls();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(label("Theme preset"), matchWrapWithTop(14));
        root.addView(themePresetSpinner, matchWrap());

        Button saveThemeButton = new Button(this);
        saveThemeButton.setText("Save current theme");
        styleSystemButton(saveThemeButton);
        saveThemeButton.setOnClickListener(v -> {
            UserThemeStore.UserTheme saved = UserThemeStore.saveCurrent(this, settings);
            selectedThemePresetIndex = indexOfUserTheme(saved.id);
            KeyboardPreferences.saveSelectedThemeId(this, saved.id);
            refreshThemePresetAdapter();
            syncControls();
        });
        root.addView(saveThemeButton, buttonParams());

        deleteThemeButton = new Button(this);
        deleteThemeButton.setText("Delete selected custom theme");
        styleSystemButton(deleteThemeButton);
        deleteThemeButton.setOnClickListener(v -> {
            ThemeOption option = selectedThemeOption();
            if (option == null || option.userThemeId == null) {
                return;
            }
            UserThemeStore.delete(this, option.userThemeId);
            if (option.userThemeId.equals(KeyboardPreferences.loadSelectedThemeId(this))) {
                KeyboardPreferences.saveSelectedThemeId(this, "");
            }
            selectedThemePresetIndex = 0;
            refreshThemePresetAdapter();
            syncControls();
        });
        root.addView(deleteThemeButton, buttonParams());
    }

    private void addSelectedKeyInspector(LinearLayout root) {
        LinearLayout section = addExpandableSection(root, "Per-Key Legends", true);
        selectedKeyLabel = label("No key selected");
        section.addView(selectedKeyLabel, matchWrapWithTop(8));

        editScopeGroup = new RadioGroup(this);
        editScopeGroup.setOrientation(RadioGroup.HORIZONTAL);
        editScopeGroup.addView(radio(EDIT_GLOBAL_ID, "Global style"));
        editScopeGroup.addView(radio(EDIT_KEY_TEXT_ID, "This key legend color"));
        editScopeGroup.check(EDIT_GLOBAL_ID);
        editScopeGroup.setOnCheckedChangeListener((group, checkedId) -> syncSelectedKeyInspector());
        section.addView(editScopeGroup, matchWrapWithTop(8));

        selectedKeyColorSpinner = colorSpinner(color -> {
            if (selectedOverrideKey.isEmpty() || editScopeGroup.getCheckedRadioButtonId() != EDIT_KEY_TEXT_ID) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(selectedOverrideKey, color);
            selectedThemePresetIndex = 0;
            updateSettings(settings.withKeyColorOverrides(overrides));
        });
        section.addView(label("Selected key legend/icon color"), matchWrapWithTop(8));
        section.addView(selectedKeyColorSpinner, matchWrap());

        resetSelectedKeyButton = new Button(this);
        resetSelectedKeyButton.setText("Reset this key");
        styleSystemButton(resetSelectedKeyButton);
        resetSelectedKeyButton.setOnClickListener(v -> {
            if (selectedOverrideKey.isEmpty()) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey));
            selectedThemePresetIndex = 0;
            updateSettings(settings.withKeyColorOverrides(overrides));
        });
        section.addView(resetSelectedKeyButton, buttonParams());
    }

    private void addColorControls(LinearLayout root) {
        keyIdleColorSpinner = colorSpinner(color -> updateSettings(settings.withThemeColors(
                color,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor)));
        root.addView(label("Key idle"), matchWrapWithTop(8));
        root.addView(keyIdleColorSpinner, matchWrap());

        accentKeyColorSpinner = colorSpinner(color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,
                settings.primaryFunctionKeyColor,
                color,
                settings.borderColor,
                settings.customDepthColorEnabled,
                settings.depthColor)));
        root.addView(label("Accent key"), matchWrapWithTop(8));
        root.addView(accentKeyColorSpinner, matchWrap());

        keyPressedColorSpinner = colorSpinner(color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                color,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor)));
        root.addView(label("Pressed"), matchWrapWithTop(8));
        root.addView(keyPressedColorSpinner, matchWrap());

        keyboardBackgroundColorSpinner = colorSpinner(color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                color,
                settings.accentColor,
                settings.secondaryColor)));
        root.addView(label("Keyboard background"), matchWrapWithTop(8));
        root.addView(keyboardBackgroundColorSpinner, matchWrap());

        borderColorSpinner = colorSpinner(color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,
                settings.primaryFunctionKeyColor,
                settings.accentKeyColor,
                color,
                settings.customDepthColorEnabled,
                settings.depthColor)));
        root.addView(label("Outline"), matchWrapWithTop(8));
        root.addView(borderColorSpinner, matchWrap());

        customDepthColorCheckBox = checkBox("Use custom depth color", checked ->
                updateSettings(settings.withDepthColor(checked, settings.depthColor)));
        root.addView(customDepthColorCheckBox, matchWrapWithTop(12));

        depthColorSpinner = colorSpinner(color -> updateSettings(settings.withDepthColor(true, color)));
        root.addView(label("Depth color"), matchWrapWithTop(8));
        root.addView(depthColorSpinner, matchWrap());

        accentColorSpinner = colorSpinner(color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                color,
                settings.secondaryColor)));
        root.addView(label("Accent text"), matchWrapWithTop(8));
        root.addView(accentColorSpinner, matchWrap());

        secondaryColorSpinner = colorSpinner(color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                color)));
        root.addView(label("Secondary text"), matchWrapWithTop(8));
        root.addView(secondaryColorSpinner, matchWrap());
    }

    private void addShapeControls(LinearLayout root) {
        roundnessValue = label("");
        roundnessSeekBar = seekBar(KeyboardSettings.MAX_KEY_ROUNDNESS_DP, progress ->
                updateSettings(settings.withKeyRoundness(progress)));
        root.addView(roundnessValue, matchWrapWithTop(8));
        root.addView(roundnessSeekBar, matchWrap());

        keyBorderWidthValue = label("");
        keyBorderWidthSeekBar = seekBar(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP, progress ->
                updateSettings(settings.withKeyBorderWidth(progress)));
        root.addView(keyBorderWidthValue, matchWrapWithTop(8));
        root.addView(keyBorderWidthSeekBar, matchWrap());

        keyGapValue = label("");
        keyGapSeekBar = seekBar(KeyboardSettings.MAX_KEY_GAP_DP, progress ->
                updateSettings(settings.withKeyGap(progress)));
        root.addView(keyGapValue, matchWrapWithTop(8));
        root.addView(keyGapSeekBar, matchWrap());

        keyDepthCheckBox = checkBox("Depth effect", checked ->
                updateSettings(settings.withKeyDepth(checked, settings.keyDepthDp)));
        root.addView(keyDepthCheckBox, matchWrapWithTop(12));

        keyDepthValue = label("");
        keyDepthSeekBar = seekBar(KeyboardSettings.MAX_KEY_DEPTH_DP, progress ->
                updateSettings(settings.withKeyDepth(settings.keyDepthEnabled, progress)));
        root.addView(keyDepthValue, matchWrapWithTop(8));
        root.addView(keyDepthSeekBar, matchWrap());
    }

    private void addLayoutControls(LinearLayout root) {
        handednessSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(
                this,
                new String[] {
                        HandednessMode.BALANCED.displayName,
                        HandednessMode.LEFT.displayName,
                        HandednessMode.RIGHT.displayName
                });
        handednessSpinner.setAdapter(adapter);
        handednessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withHandednessPreset(HandednessMode.values()[position]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(label("Handedness"), matchWrapWithTop(8));
        root.addView(handednessSpinner, matchWrap());

        hangulHeightValue = label("");
        hangulHeightSeekBar = heightSeekBar(progress ->
                updateSettings(settings.withHangulHeight(KeyboardSettings.MIN_HEIGHT_DP + progress)));
        root.addView(hangulHeightValue, matchWrapWithTop(8));
        root.addView(hangulHeightSeekBar, matchWrap());

        englishHeightValue = label("");
        englishHeightSeekBar = heightSeekBar(progress ->
                updateSettings(settings.withEnglishHeight(KeyboardSettings.MIN_HEIGHT_DP + progress)));
        root.addView(englishHeightValue, matchWrapWithTop(8));
        root.addView(englishHeightSeekBar, matchWrap());

        root.addView(label("Dingul spacing"), matchWrapWithTop(12));
        hangulLeftPaddingValue = label("");
        hangulLeftPaddingSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP, progress ->
                updateSettings(settings.withHangulSidePadding(progress, settings.hangulRightPaddingDp)));
        root.addView(hangulLeftPaddingValue, matchWrapWithTop(8));
        root.addView(hangulLeftPaddingSeekBar, matchWrap());

        hangulRightPaddingValue = label("");
        hangulRightPaddingSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP, progress ->
                updateSettings(settings.withHangulSidePadding(settings.hangulLeftPaddingDp, progress)));
        root.addView(hangulRightPaddingValue, matchWrapWithTop(8));
        root.addView(hangulRightPaddingSeekBar, matchWrap());

        hangulSpecialColumnValue = label("");
        hangulSpecialColumnSeekBar = seekBar(
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT
                        - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                progress -> updateSettings(settings.withHangulSpecialColumnPercent(
                        KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT + progress)));
        root.addView(hangulSpecialColumnValue, matchWrapWithTop(8));
        root.addView(hangulSpecialColumnSeekBar, matchWrap());

        hangulMainSpecialGapValue = label("");
        hangulMainSpecialGapSeekBar = seekBar(KeyboardSettings.MAX_HANGUL_MAIN_SPECIAL_GAP_DP, progress ->
                updateSettings(settings.withLayoutSpacing(
                        progress,
                        settings.keyboardTopPaddingDp,
                        settings.keyboardBottomPaddingDp,
                        settings.bottomRowTopPaddingDp)));
        root.addView(hangulMainSpecialGapValue, matchWrapWithTop(8));
        root.addView(hangulMainSpecialGapSeekBar, matchWrap());

        root.addView(label("QWERTY spacing"), matchWrapWithTop(12));
        englishLeftPaddingValue = label("");
        englishLeftPaddingSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP, progress ->
                updateSettings(settings.withEnglishSidePadding(progress, settings.englishRightPaddingDp)));
        root.addView(englishLeftPaddingValue, matchWrapWithTop(8));
        root.addView(englishLeftPaddingSeekBar, matchWrap());

        englishRightPaddingValue = label("");
        englishRightPaddingSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP, progress ->
                updateSettings(settings.withEnglishSidePadding(settings.englishLeftPaddingDp, progress)));
        root.addView(englishRightPaddingValue, matchWrapWithTop(8));
        root.addView(englishRightPaddingSeekBar, matchWrap());

        root.addView(label("Vertical spacing"), matchWrapWithTop(12));
        keyboardTopPaddingValue = label("");
        keyboardTopPaddingSeekBar = seekBar(KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP, progress ->
                updateSettings(settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        progress,
                        settings.keyboardBottomPaddingDp,
                        settings.bottomRowTopPaddingDp)));
        root.addView(keyboardTopPaddingValue, matchWrapWithTop(8));
        root.addView(keyboardTopPaddingSeekBar, matchWrap());

        bottomRowTopPaddingValue = label("");
        bottomRowTopPaddingSeekBar = seekBar(KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP, progress ->
                updateSettings(settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        settings.keyboardTopPaddingDp,
                        settings.keyboardBottomPaddingDp,
                        progress)));
        root.addView(bottomRowTopPaddingValue, matchWrapWithTop(8));
        root.addView(bottomRowTopPaddingSeekBar, matchWrap());

        keyboardBottomPaddingValue = label("");
        keyboardBottomPaddingSeekBar = seekBar(KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP, progress ->
                updateSettings(settings.withLayoutSpacing(
                        settings.hangulMainSpecialGapDp,
                        settings.keyboardTopPaddingDp,
                        progress,
                        settings.bottomRowTopPaddingDp)));
        root.addView(keyboardBottomPaddingValue, matchWrapWithTop(8));
        root.addView(keyboardBottomPaddingSeekBar, matchWrap());

        hangulNumberRowCheckBox = checkBox("Hangul number row", checked ->
                updateSettings(settings.withHangulNumberRow(checked)));
        englishNumberRowCheckBox = checkBox("QWERTY number row", checked ->
                updateSettings(settings.withEnglishNumberRow(checked)));
        root.addView(hangulNumberRowCheckBox, matchWrapWithTop(12));
        root.addView(englishNumberRowCheckBox, matchWrapWithTop(4));

        additionalNumberRowColorModeSpinner = additionalNumberRowColorModeSpinner();
        root.addView(label("Additional number row color"), matchWrapWithTop(8));
        root.addView(additionalNumberRowColorModeSpinner, matchWrap());
    }

    private void addTypographyControls(LinearLayout root) {
        fontFamilySpinner = fontSpinner();
        root.addView(label("Font"), matchWrapWithTop(8));
        root.addView(fontFamilySpinner, matchWrap());

        primaryTextSizeValue = label("");
        primaryTextSizeSeekBar = textSizeSeekBar(progress -> updateSettings(settings.withTypography(
                settings.fontFamily,
                KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        root.addView(primaryTextSizeValue, matchWrapWithTop(8));
        root.addView(primaryTextSizeSeekBar, matchWrap());

        secondaryTextSizeValue = label("");
        secondaryTextSizeSeekBar = textSizeSeekBar(progress -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        root.addView(secondaryTextSizeValue, matchWrapWithTop(8));
        root.addView(secondaryTextSizeSeekBar, matchWrap());

        primaryTextBoldCheckBox = checkBox("Primary legend bold", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                checked,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        primaryTextItalicCheckBox = checkBox("Primary legend italic", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                checked,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        secondaryTextBoldCheckBox = checkBox("Secondary hint bold", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                checked,
                settings.secondaryTextItalic)));
        secondaryTextItalicCheckBox = checkBox("Secondary hint italic", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                checked)));
        root.addView(primaryTextBoldCheckBox, matchWrapWithTop(12));
        root.addView(primaryTextItalicCheckBox, matchWrapWithTop(4));
        root.addView(secondaryTextBoldCheckBox, matchWrapWithTop(8));
        root.addView(secondaryTextItalicCheckBox, matchWrapWithTop(4));
    }

    private void addHintControls(LinearLayout root) {
        hangulSlideHintsCheckBox = checkBox("Hangul slide hints", checked -> updateSettings(settings.withHintVisibility(
                checked,
                settings.showEnglishSlideHints,
                settings.showBeginnerTooltipPreview)));
        englishSlideHintsCheckBox = checkBox("QWERTY slide hints", checked -> updateSettings(settings.withHintVisibility(
                settings.showHangulSlideHints,
                checked,
                settings.showBeginnerTooltipPreview)));
        beginnerTooltipPreviewCheckBox = checkBox("Beginner tooltip preview", checked ->
                updateSettings(settings.withHintVisibility(
                        settings.showHangulSlideHints,
                        settings.showEnglishSlideHints,
                        checked)));
        root.addView(hangulSlideHintsCheckBox, matchWrapWithTop(8));
        root.addView(englishSlideHintsCheckBox, matchWrapWithTop(4));
        root.addView(beginnerTooltipPreviewCheckBox, matchWrapWithTop(4));
    }

    private void updateSettings(KeyboardSettings next) {
        selectedThemePresetIndex = 0;
        settings = next;
        KeyboardPreferences.saveSelectedThemeId(this, "");
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void syncControls() {
        if (preview == null) {
            return;
        }
        syncing = true;
        if (themeOptions.length == 0) {
            reloadThemeOptions();
        }
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            selectedThemePresetIndex = 0;
        }
        if (themePresetSpinner != null) {
            themePresetSpinner.setSelection(selectedThemePresetIndex);
        }
        modeGroup.check(settings.keyboardMode == KeyboardMode.ENGLISH ? MODE_ENGLISH_ID : MODE_HANGUL_ID);
        handednessSpinner.setSelection(settings.handednessMode.ordinal());
        setProgress(hangulHeightSeekBar, settings.hangulKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        setProgress(englishHeightSeekBar, settings.englishKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        setProgress(hangulLeftPaddingSeekBar, settings.hangulLeftPaddingDp);
        setProgress(hangulRightPaddingSeekBar, settings.hangulRightPaddingDp);
        setProgress(englishLeftPaddingSeekBar, settings.englishLeftPaddingDp);
        setProgress(englishRightPaddingSeekBar, settings.englishRightPaddingDp);
        setProgress(
                hangulSpecialColumnSeekBar,
                settings.hangulSpecialColumnPercent - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        setProgress(hangulMainSpecialGapSeekBar, settings.hangulMainSpecialGapDp);
        setProgress(keyboardTopPaddingSeekBar, settings.keyboardTopPaddingDp);
        setProgress(bottomRowTopPaddingSeekBar, settings.bottomRowTopPaddingDp);
        setProgress(keyboardBottomPaddingSeekBar, settings.keyboardBottomPaddingDp);
        setProgress(roundnessSeekBar, settings.keyRoundnessDp);
        setProgress(keyBorderWidthSeekBar, settings.keyBorderWidthDp);
        setProgress(keyGapSeekBar, settings.keyGapDp);
        setProgress(keyDepthSeekBar, settings.keyDepthDp);
        setProgress(primaryTextSizeSeekBar, settings.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        setProgress(secondaryTextSizeSeekBar, settings.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        setSelection(keyIdleColorSpinner, indexOfColor(settings.keyIdleColor));
        setSelection(accentKeyColorSpinner, indexOfColor(settings.accentKeyColor));
        setSelection(keyPressedColorSpinner, indexOfColor(settings.keyPressedColor));
        setSelection(keyboardBackgroundColorSpinner, indexOfColor(settings.keyboardBackgroundColor));
        setSelection(accentColorSpinner, indexOfColor(settings.accentColor));
        setSelection(secondaryColorSpinner, indexOfColor(settings.secondaryColor));
        setSelection(borderColorSpinner, indexOfColor(settings.borderColor));
        setSelection(depthColorSpinner, indexOfColor(settings.depthColor));
        setSelection(fontFamilySpinner, indexOfFont(settings.fontFamily));
        setSelection(additionalNumberRowColorModeSpinner, settings.additionalNumberRowColorMode.ordinal());
        setChecked(hangulNumberRowCheckBox, settings.showHangulNumberRow);
        setChecked(englishNumberRowCheckBox, settings.showEnglishNumberRow);
        setChecked(keyDepthCheckBox, settings.keyDepthEnabled);
        setChecked(customDepthColorCheckBox, settings.customDepthColorEnabled);
        setChecked(primaryTextBoldCheckBox, settings.primaryTextBold);
        setChecked(primaryTextItalicCheckBox, settings.primaryTextItalic);
        setChecked(secondaryTextBoldCheckBox, settings.secondaryTextBold);
        setChecked(secondaryTextItalicCheckBox, settings.secondaryTextItalic);
        if (hangulSlideHintsCheckBox != null) {
            setChecked(hangulSlideHintsCheckBox, settings.showHangulSlideHints);
        }
        if (englishSlideHintsCheckBox != null) {
            setChecked(englishSlideHintsCheckBox, settings.showEnglishSlideHints);
        }
        if (beginnerTooltipPreviewCheckBox != null) {
            setChecked(beginnerTooltipPreviewCheckBox, settings.showBeginnerTooltipPreview);
        }
        depthColorSpinner.setEnabled(settings.customDepthColorEnabled);
        keyDepthSeekBar.setEnabled(settings.keyDepthEnabled);
        if (deleteThemeButton != null) {
            deleteThemeButton.setEnabled(selectedThemeOption() != null && selectedThemeOption().userThemeId != null);
        }

        hangulHeightValue.setText("Dingul height: " + settings.hangulKeyboardHeightDp + "dp");
        englishHeightValue.setText("QWERTY height: " + settings.englishKeyboardHeightDp + "dp");
        hangulLeftPaddingValue.setText("Dingul left padding: " + settings.hangulLeftPaddingDp + "dp");
        hangulRightPaddingValue.setText("Dingul right padding: " + settings.hangulRightPaddingDp + "dp");
        englishLeftPaddingValue.setText("QWERTY left padding: " + settings.englishLeftPaddingDp + "dp");
        englishRightPaddingValue.setText("QWERTY right padding: " + settings.englishRightPaddingDp + "dp");
        hangulSpecialColumnValue.setText("Hangul special column: " + settings.hangulSpecialColumnPercent + "%");
        hangulMainSpecialGapValue.setText("Dingul main/special gap: "
                + settings.hangulMainSpecialGapDp + "dp");
        keyboardTopPaddingValue.setText("Keyboard top padding: " + settings.keyboardTopPaddingDp + "dp");
        bottomRowTopPaddingValue.setText("Bottom row top padding: " + settings.bottomRowTopPaddingDp + "dp");
        keyboardBottomPaddingValue.setText("Keyboard bottom padding: " + settings.keyboardBottomPaddingDp + "dp");
        roundnessValue.setText("Roundness: " + settings.keyRoundnessDp + "dp");
        keyBorderWidthValue.setText("Outline density: " + settings.keyBorderWidthDp + "dp");
        keyGapValue.setText("Visual key gap: " + settings.keyGapDp + "dp");
        keyDepthValue.setText("Depth height: " + settings.keyDepthDp + "dp"
                + (settings.keyDepthEnabled ? "" : " (flat)"));
        primaryTextSizeValue.setText("Primary legend size: " + settings.primaryTextSizePercent + "%");
        secondaryTextSizeValue.setText("Secondary hint size: " + settings.secondaryTextSizePercent + "%");
        previewMeta.setText((settings.keyboardMode == KeyboardMode.ENGLISH ? "QWERTY" : "Dingul")
                + " user layout preview / "
                + settings.measuredHeightDp()
                + "dp / tap a key to edit its legend color");
        updatePreviewHeight();
        preview.setSettings(settings);
        syncSelectedKeyInspector();
        preview.post(() -> syncing = false);
    }

    private void syncSelectedKeyInspector() {
        boolean keySelected = selectedKey != null && !selectedOverrideKey.isEmpty();
        boolean keyEdit = keySelected && editScopeGroup.getCheckedRadioButtonId() == EDIT_KEY_TEXT_ID;
        selectedKeyLabel.setText(keySelected
                ? "Selected: " + displayKeyName(selectedKey) + " / override key: " + selectedOverrideKey
                : "No key selected");
        selectedKeyColorSpinner.setEnabled(keyEdit);
        resetSelectedKeyButton.setEnabled(keyEdit && settings.keyColorOverrides.containsKey(
                KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey)));
        Integer override = keySelected
                ? settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey))
                : null;
        boolean wasSyncing = syncing;
        syncing = true;
        setSelection(selectedKeyColorSpinner, indexOfColor(override == null ? settings.accentColor : override));
        syncing = wasSyncing;
    }

    private void updatePreviewHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) preview.getLayoutParams();
        params.height = dp(settings.measuredHeightDp());
        preview.setLayoutParams(params);
    }

    private String overrideKeyFor(GestureKey key) {
        if (key == null) {
            return "";
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return "space";
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            return "backspace";
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            return "enter";
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            return "shift";
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            return "language";
        }
        if (key.tap != null && !key.tap.isEmpty() && !KeyboardCommands.isCommand(key.tap)) {
            return "tap:" + key.tap;
        }
        return key.label == null ? "" : "label:" + key.label;
    }

    private String displayKeyName(GestureKey key) {
        if (key == null) {
            return "";
        }
        if (key.label != null && !key.label.isEmpty()) {
            return key.label;
        }
        return key.tap == null ? "" : key.tap;
    }

    private LinearLayout addExpandableSection(LinearLayout root, String text, boolean expandedByDefault) {
        TextView title = label("");
        title.setTextSize(17);
        title.setGravity(Gravity.START);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setVisibility(expandedByDefault ? View.VISIBLE : View.GONE);
        setExpandableTitle(title, text, expandedByDefault);
        title.setOnClickListener(v -> {
            boolean expanded = content.getVisibility() != View.VISIBLE;
            content.setVisibility(expanded ? View.VISIBLE : View.GONE);
            setExpandableTitle(title, text, expanded);
        });
        root.addView(title, matchWrapWithTop(18));
        root.addView(content, matchWrap());
        return content;
    }

    private void setExpandableTitle(TextView title, String text, boolean expanded) {
        title.setText((expanded ? "- " : "+ ") + text);
    }

    private Spinner colorSpinner(final ColorChangeListener listener) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<ColorOption> adapter = new SettingsArrayAdapter<>(
                this,
                ColorOption.EDITOR_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    selectedThemePresetIndex = 0;
                    listener.onColorChanged(ColorOption.EDITOR_OPTIONS[position].color);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner additionalNumberRowColorModeSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<AdditionalNumberRowColorMode> adapter = new SettingsArrayAdapter<>(
                this,
                AdditionalNumberRowColorMode.values());
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withAdditionalNumberRowColorMode(
                            AdditionalNumberRowColorMode.values()[position]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner fontSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<FontOption> adapter = new SettingsArrayAdapter<>(
                this,
                FontOption.EDITOR_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withFontFamily(FontOption.EDITOR_OPTIONS[position].value));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private CheckBox checkBox(String label, BooleanChangeListener listener) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(label);
        SettingsViewStyler.compoundButton(checkBox, this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    selectedThemePresetIndex = 0;
                    listener.onChanged(isChecked);
                }
            }
        });
        return checkBox;
    }

    private SeekBar seekBar(int max, IntChangeListener listener) {
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    listener.onChanged(progress);
                }
            }
        });
        return seekBar;
    }

    private SeekBar heightSeekBar(IntChangeListener listener) {
        return seekBar(KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP, listener);
    }

    private SeekBar textSizeSeekBar(IntChangeListener listener) {
        return seekBar(KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT, listener);
    }

    private RadioButton radio(int id, String label) {
        RadioButton button = new RadioButton(this);
        button.setId(id);
        button.setText(label);
        SettingsViewStyler.compoundButton(button, this);
        return button;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(SettingsUiPalette.from(this).textPrimary);
        label.setTextSize(14);
        label.setGravity(Gravity.START);
        return label;
    }

    private void styleSystemButton(Button button) {
        SettingsViewStyler.button(button, this, false);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(10);
        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setProgress(SeekBar seekBar, int progress) {
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }

    private void setSelection(Spinner spinner, int position) {
        if (spinner != null) {
            spinner.setSelection(position);
        }
    }

    private void setChecked(CheckBox checkBox, boolean checked) {
        if (checkBox != null) {
            checkBox.setChecked(checked);
        }
    }

    private void refreshThemePresetAdapter() {
        reloadThemeOptions();
        if (themePresetSpinner == null) {
            return;
        }
        boolean wasSyncing = syncing;
        syncing = true;
        ArrayAdapter<ThemeOption> adapter = new SettingsArrayAdapter<>(
                this,
                themeOptions);
        themePresetSpinner.setAdapter(adapter);
        syncing = wasSyncing;
    }

    private void reloadThemeOptions() {
        themeOptions = ThemeOption.buildOptions(UserThemeStore.load(this), true);
    }

    private int indexOfUserTheme(String userThemeId) {
        reloadThemeOptions();
        for (int i = 0; i < themeOptions.length; i++) {
            if (userThemeId != null && userThemeId.equals(themeOptions[i].userThemeId)) {
                return i;
            }
        }
        return 0;
    }

    private ThemeOption selectedThemeOption() {
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            return null;
        }
        return themeOptions[selectedThemePresetIndex];
    }

    private int indexOfColor(Integer color) {
        if (color == null) {
            return 0;
        }
        int opaqueColor = 0xFF000000 | (color & 0x00FFFFFF);
        for (int i = 0; i < ColorOption.EDITOR_OPTIONS.length; i++) {
            if (ColorOption.EDITOR_OPTIONS[i].color == opaqueColor) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfFont(String fontFamily) {
        String normalized = KeyboardSettings.normalizeFontFamily(fontFamily);
        for (int i = 0; i < FontOption.EDITOR_OPTIONS.length; i++) {
            if (FontOption.EDITOR_OPTIONS[i].value.equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private interface ColorChangeListener {
        void onColorChanged(int color);
    }

    private interface BooleanChangeListener {
        void onChanged(boolean checked);
    }

    private interface IntChangeListener {
        void onChanged(int value);
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}
