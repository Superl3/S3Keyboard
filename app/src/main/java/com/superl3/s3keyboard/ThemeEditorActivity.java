package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

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

    private Spinner keyIdleColorSpinner;
    private Spinner functionKeyColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner keyFaceGradientStartColorSpinner;
    private Spinner keyFaceGradientEndColorSpinner;
    private Spinner keyFaceGradientCurveSpinner;
    private Spinner materialStyleSpinner;
    private Spinner panelGradientStartColorSpinner;
    private Spinner panelGradientEndColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner fontFamilySpinner;
    private Spinner modifierIconPackSpinner;
    private Spinner keyDisplayPackSpinner;
    private Spinner selectedKeyColorSpinner;
    private Spinner selectedKeyBackgroundColorSpinner;
    private Button addSelectedKeyOverrideButton;
    private Button resetSelectedKeyButton;
    private RadioGroup modeGroup;
    private RadioGroup editScopeGroup;
    private SeekBar roundnessSeekBar;
    private SeekBar keyBorderWidthSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar pseudoBlurRadiusSeekBar;
    private SeekBar glassTintSeekBar;
    private SeekBar glassHighlightSeekBar;
    private SeekBar keyDepthSeekBar;
    private SeekBar keyFaceGradientStrengthSeekBar;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private CheckBox keyDepthCheckBox;
    private CheckBox pseudoBlurCheckBox;
    private CheckBox customDepthColorCheckBox;
    private CheckBox keyFaceGradientCheckBox;
    private CheckBox panelGradientCheckBox;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private TextView selectedKeyLabel;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView pseudoBlurRadiusValue;
    private TextView glassTintValue;
    private TextView glassHighlightValue;
    private TextView keyDepthValue;
    private TextView keyFaceGradientStrengthValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private View keyIdleColorSwatch;
    private View functionKeyColorSwatch;
    private View accentKeyColorSwatch;
    private View keyPressedColorSwatch;
    private View keyboardBackgroundColorSwatch;
    private View keyFaceGradientStartColorSwatch;
    private View keyFaceGradientEndColorSwatch;
    private View panelGradientStartColorSwatch;
    private View panelGradientEndColorSwatch;
    private View borderColorSwatch;
    private View depthColorSwatch;
    private View accentColorSwatch;
    private View secondaryColorSwatch;
    private View selectedKeyColorSwatch;
    private View selectedKeyBackgroundColorSwatch;
    private final Map<View, TextView> swatchCodeLabels = new HashMap<>();

    private static final class ColorControl {
        final Spinner spinner;
        final View swatch;

        ColorControl(Spinner spinner, View swatch) {
            this.spinner = spinner;
            this.swatch = swatch;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        settings = KeyboardPreferences.load(this);
        View content = createContentView();
        SettingsSystemBars.applyTopInset(content);
        setContentView(content);
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        syncControls();
    }

    private View createContentView() {
        int padding = SettingsRowBuilder.dp(this, 16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(ui.background);

        TextView title = SettingsRowBuilder.label(this, getString(R.string.theme_editor_title));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        SettingsRowBuilder.addView(root, title);

        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.HORIZONTAL);
        modeGroup.addView(SettingsRowBuilder.radioButton(
                this,
                MODE_HANGUL_ID,
                R.string.theme_mode_dingul));
        modeGroup.addView(SettingsRowBuilder.radioButton(
                this,
                MODE_ENGLISH_ID,
                R.string.theme_mode_qwerty));
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
        SettingsRowBuilder.addViewWithTop(this, root, modeGroup, 8);

        preview = new HangulKeyboardView(this, true);
        preview.setOnPreviewKeySelectionListener(key -> {
            selectedKey = key;
            selectedOverrideKey = overrideKeyFor(key);
            editScopeGroup.check(EDIT_KEY_TEXT_ID);
            syncSelectedKeyInspector();
        });
        SettingsRowBuilder.addViewWithTop(this, root, preview, 8);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout editorRoot = SettingsRowBuilder.vertical(this);
        scrollView.addView(editorRoot);
        root.addView(scrollView, SettingsRowBuilder.matchWeightedFill());

        addThemeSaveControls(editorRoot);
        addThemePromptControls(editorRoot);
        LinearLayout globalSection = addExpandableSection(
                editorRoot,
                getString(R.string.theme_editor_global_section),
                true);
        addColorControls(SettingsSubsection.add(
                this,
                globalSection,
                R.string.theme_editor_colors_section,
                true).content);
        addBackgroundControls(SettingsSubsection.add(
                this,
                globalSection,
                R.string.theme_editor_background_section,
                true).content);
        addShapeControls(SettingsSubsection.add(
                this,
                globalSection,
                R.string.theme_editor_shape_section,
                false).content);
        addIconPackControls(SettingsSubsection.add(
                this,
                globalSection,
                R.string.theme_editor_icon_packs_section,
                true).content);
        addTypographyControls(SettingsSubsection.add(
                this,
                globalSection,
                R.string.theme_editor_typography_section,
                false).content);
        addSelectedKeyInspector(editorRoot);
        return root;
    }

    private void addThemeSaveControls(LinearLayout root) {
        SettingsRowBuilder.buttonRow(this, root, R.string.theme_save_current, 10, v -> {
            UserThemeStore.UserTheme saved = UserThemeStore.saveCurrent(this, settings);
            KeyboardPreferences.saveSelectedThemeId(this, saved.id);
            Toast.makeText(this, getString(R.string.theme_saved_format, saved.name), Toast.LENGTH_SHORT).show();
        });

        SettingsRowBuilder.buttonRow(this, root, R.string.theme_json_copy, 10, v -> copyThemeJsonToClipboard());

        SettingsRowBuilder.buttonRow(this, root, R.string.theme_json_import_title, 10,
                v -> showThemeJsonImportDialog());
    }

    private void addThemePromptControls(LinearLayout root) {
        LinearLayout section = addExpandableSection(root, getString(R.string.theme_ai_prompt_section), false);
        TextView description = SettingsRowBuilder.label(
                this,
                getString(R.string.theme_ai_prompt_description));
        SettingsRowBuilder.addViewWithTop(this, section, description, 6);

        SettingsRowBuilder.buttonRow(
                this,
                section,
                getString(R.string.theme_keyboard_image_prompt_copy),
                10,
                v -> copyPromptToClipboard(
                        getString(R.string.theme_keyboard_image_prompt_clip_label),
                        ThemePromptTemplates.keyboardImagePrompt(currentThemeJson()),
                        getString(R.string.theme_keyboard_image_prompt_copied)));

        SettingsRowBuilder.buttonRow(
                this,
                section,
                getString(R.string.theme_palette_image_prompt_copy),
                10,
                v -> copyPromptToClipboard(
                        getString(R.string.theme_palette_image_prompt_clip_label),
                        ThemePromptTemplates.paletteImagePrompt(currentThemeJson()),
                        getString(R.string.theme_palette_image_prompt_copied)));
    }

    private void addSelectedKeyInspector(LinearLayout root) {
        LinearLayout section = addExpandableSection(root, getString(R.string.theme_per_key_override_section), true);
        selectedKeyLabel = SettingsRowBuilder.label(this, getString(R.string.theme_no_key_selected));
        SettingsRowBuilder.addViewWithTop(this, section, selectedKeyLabel, 8);

        editScopeGroup = new RadioGroup(this);
        editScopeGroup.setOrientation(RadioGroup.HORIZONTAL);
        editScopeGroup.addView(SettingsRowBuilder.radioButton(
                this,
                EDIT_GLOBAL_ID,
                R.string.theme_global_style));
        editScopeGroup.addView(SettingsRowBuilder.radioButton(
                this,
                EDIT_KEY_TEXT_ID,
                R.string.theme_selected_key));
        editScopeGroup.check(EDIT_GLOBAL_ID);
        editScopeGroup.setOnCheckedChangeListener((group, checkedId) -> syncSelectedKeyInspector());
        SettingsRowBuilder.addViewWithTop(this, section, editScopeGroup, 8);

        IntConsumer selectedKeyTextListener = color -> {
            if (selectedOverrideKey.isEmpty() || editScopeGroup.getCheckedRadioButtonId() != EDIT_KEY_TEXT_ID) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(selectedOverrideKey, color);
            updateSettings(settings.withKeyColorOverrides(overrides));
        };
        selectedKeyColorSpinner = colorSpinner(selectedKeyTextListener);
        SettingsRowBuilder.addViewWithTop(
                this,
                section,
                SettingsRowBuilder.label(this, getString(R.string.theme_selected_key_text_color)),
                8);
        selectedKeyColorSwatch = addInlineSwatch(section, settings.accentColor);
        selectedKeyColorSwatch.setOnClickListener(v ->
                showColorEditDialog(
                        getString(R.string.theme_selected_key_text_color),
                        colorTag(selectedKeyColorSwatch),
                        selectedKeyTextListener));
        SettingsRowBuilder.addView(section, selectedKeyColorSpinner);

        IntConsumer selectedKeyBackgroundListener = color -> {
            if (selectedOverrideKey.isEmpty() || editScopeGroup.getCheckedRadioButtonId() != EDIT_KEY_TEXT_ID) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(backgroundOverrideKey(selectedOverrideKey), color);
            updateSettings(settings.withKeyColorOverrides(overrides));
        };
        selectedKeyBackgroundColorSpinner = colorSpinner(selectedKeyBackgroundListener);
        SettingsRowBuilder.addViewWithTop(
                this,
                section,
                SettingsRowBuilder.label(this, getString(R.string.theme_selected_key_background_color)),
                8);
        selectedKeyBackgroundColorSwatch = addInlineSwatch(section, settings.keyIdleColor);
        selectedKeyBackgroundColorSwatch.setOnClickListener(v ->
                showColorEditDialog(
                        getString(R.string.theme_selected_key_background_color),
                        colorTag(selectedKeyBackgroundColorSwatch),
                        selectedKeyBackgroundListener));
        SettingsRowBuilder.addView(section, selectedKeyBackgroundColorSpinner);

        addSelectedKeyOverrideButton = SettingsRowBuilder.buttonRow(
                this,
                section,
                R.string.theme_add_selected_key_override,
                10,
                v -> {
            if (selectedOverrideKey.isEmpty()) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(
                    selectedOverrideKey,
                    KeyboardKeyVisualClassifier.textColorFor(settings, selectedKey));
            overrides.put(
                    backgroundOverrideKey(selectedOverrideKey),
                    KeyboardKeyVisualClassifier.colorFor(settings, selectedKey));
            updateSettings(settings.withKeyColorOverrides(overrides));
        });

        resetSelectedKeyButton = SettingsRowBuilder.buttonRow(
                this,
                section,
                R.string.theme_reset_selected_key_override,
                10,
                v -> {
            if (selectedOverrideKey.isEmpty()) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey));
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName(backgroundOverrideKey(selectedOverrideKey)));
            updateSettings(settings.withKeyColorOverrides(overrides));
        });
    }

    private void addColorControls(LinearLayout root) {
        IntConsumer keyIdleListener = color -> updateSettings(settings.withThemeColors(
                color,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor));
        ColorControl keyIdleControl = addColorSetting(
                root,
                R.string.theme_color_alpha_title,
                R.string.theme_color_alpha_description,
                keyIdleListener);
        keyIdleColorSpinner = keyIdleControl.spinner;
        keyIdleColorSwatch = keyIdleControl.swatch;

        IntConsumer functionKeyListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                color,
                settings.accentKeyColor,
                settings.borderColor,
                settings.customDepthColorEnabled,
                settings.depthColor));
        ColorControl functionKeyControl = addColorSetting(
                root,
                R.string.theme_color_modifier_title,
                R.string.theme_color_modifier_description,
                functionKeyListener);
        functionKeyColorSpinner = functionKeyControl.spinner;
        functionKeyColorSwatch = functionKeyControl.swatch;
        SettingsRowBuilder.buttonRow(
                this,
                root,
                getString(R.string.theme_color_modifier_from_alpha),
                10,
                v -> functionKeyListener.accept(dimColor(settings.keyIdleColor, 0.90f)));
        IntConsumer accentKeyListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,
                color,
                settings.borderColor,
                settings.customDepthColorEnabled,
                settings.depthColor));
        ColorControl accentKeyControl = addColorSetting(
                root,
                R.string.theme_color_accent_title,
                R.string.theme_color_accent_description,
                accentKeyListener);
        accentKeyColorSpinner = accentKeyControl.spinner;
        accentKeyColorSwatch = accentKeyControl.swatch;
        SettingsRowBuilder.buttonRow(
                this,
                root,
                getString(R.string.theme_color_accent_from_modifier_inverse),
                10,
                v -> updateSettings(settings.withExtendedThemeColors(
                        settings.keyIdleColor,
                        settings.keyPressedColor,
                        settings.keyboardBackgroundColor,
                        settings.accentColor,
                        settings.functionKeyColor,
                        settings.functionKeyColor,
                        settings.secondaryColor,
                        settings.borderColor,
                        settings.customDepthColorEnabled,
                        settings.depthColor)));

        IntConsumer keyPressedListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                color,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor));
        ColorControl keyPressedControl = addColorSetting(
                root,
                R.string.theme_color_pressed_title,
                R.string.theme_color_pressed_description,
                keyPressedListener);
        keyPressedColorSpinner = keyPressedControl.spinner;
        keyPressedColorSwatch = keyPressedControl.swatch;

        IntConsumer keyboardBackgroundListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                color,
                settings.accentColor,
                settings.secondaryColor));
        ColorControl keyboardBackgroundControl = addColorSetting(
                root,
                R.string.theme_color_keyboard_background_title,
                R.string.theme_color_keyboard_background_description,
                keyboardBackgroundListener);
        keyboardBackgroundColorSpinner = keyboardBackgroundControl.spinner;
        keyboardBackgroundColorSwatch = keyboardBackgroundControl.swatch;

        panelGradientCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                R.string.theme_panel_gradient_enabled,
                12,
                () -> !syncing,
                checked ->
                        updateSettings(settings.withVisualEffects(settings.visualEffects.withPanelGradient(
                                checked,
                                settings.visualEffects.panelGradientStartColor,
                                settings.visualEffects.panelGradientEndColor))));

        IntConsumer panelGradientStartListener = color -> updateSettings(settings.withVisualEffects(
                settings.visualEffects.withPanelGradient(
                        true,
                        color,
                        settings.visualEffects.panelGradientEndColor)));
        ColorControl panelGradientStartControl = addColorSetting(
                root,
                R.string.theme_panel_gradient_start_title,
                R.string.theme_panel_gradient_start_description,
                panelGradientStartListener);
        panelGradientStartColorSpinner = panelGradientStartControl.spinner;
        panelGradientStartColorSwatch = panelGradientStartControl.swatch;

        IntConsumer panelGradientEndListener = color -> updateSettings(settings.withVisualEffects(
                settings.visualEffects.withPanelGradient(
                        true,
                        settings.visualEffects.panelGradientStartColor,
                        color)));
        ColorControl panelGradientEndControl = addColorSetting(
                root,
                R.string.theme_panel_gradient_end_title,
                R.string.theme_panel_gradient_end_description,
                panelGradientEndListener);
        panelGradientEndColorSpinner = panelGradientEndControl.spinner;
        panelGradientEndColorSwatch = panelGradientEndControl.swatch;

        IntConsumer borderListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,
                settings.accentKeyColor,
                color,
                settings.customDepthColorEnabled,
                settings.depthColor));
        ColorControl borderControl = addColorSetting(
                root,
                R.string.theme_color_border_title,
                R.string.theme_color_border_description,
                borderListener);
        borderColorSpinner = borderControl.spinner;
        borderColorSwatch = borderControl.swatch;

        customDepthColorCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                R.string.theme_custom_depth_color_enabled,
                12,
                () -> !syncing,
                checked ->
                        updateSettings(settings.withDepthColor(checked, settings.depthColor)));

        IntConsumer depthListener = color -> updateSettings(settings.withDepthColor(true, color));
        ColorControl depthControl = addColorSetting(
                root,
                R.string.theme_color_depth_title,
                R.string.theme_color_depth_description,
                depthListener);
        depthColorSpinner = depthControl.spinner;
        depthColorSwatch = depthControl.swatch;

        IntConsumer accentListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                color,
                settings.secondaryColor));
        ColorControl accentControl = addColorSetting(
                root,
                R.string.theme_color_primary_text_title,
                R.string.theme_color_primary_text_description,
                accentListener);
        accentColorSpinner = accentControl.spinner;
        accentColorSwatch = accentControl.swatch;

        IntConsumer secondaryListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                color));
        ColorControl secondaryControl = addColorSetting(
                root,
                R.string.theme_color_secondary_text_title,
                R.string.theme_color_secondary_text_description,
                secondaryListener);
        secondaryColorSpinner = secondaryControl.spinner;
        secondaryColorSwatch = secondaryControl.swatch;
    }

    private void addShapeControls(LinearLayout root) {
        roundnessValue = SettingsRowBuilder.valueLabel(this);
        roundnessSeekBar = SettingsRowBuilder.seekBarRow(this, root, roundnessValue,
                KeyboardSettings.MAX_KEY_ROUNDNESS_DP, 8, () -> !syncing, progress ->
                updateSettings(settings.withKeyRoundness(progress)));

        keyBorderWidthValue = SettingsRowBuilder.valueLabel(this);
        keyBorderWidthSeekBar = SettingsRowBuilder.seekBarRow(this, root, keyBorderWidthValue,
                KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP, 8, () -> !syncing, progress ->
                updateSettings(settings.withKeyBorderWidth(progress)));

        keyGapValue = SettingsRowBuilder.valueLabel(this);
        keyGapSeekBar = SettingsRowBuilder.seekBarRow(this, root, keyGapValue,
                KeyboardSettings.MAX_KEY_GAP_DP, 8, () -> !syncing, progress ->
                updateSettings(settings.withKeyGap(progress)));

        keyDepthCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                R.string.theme_key_depth_enabled,
                12,
                () -> !syncing,
                checked ->
                        updateSettings(settings.withKeyDepth(checked, settings.keyDepthDp)));

        keyDepthValue = SettingsRowBuilder.valueLabel(this);
        keyDepthSeekBar = SettingsRowBuilder.seekBarRow(this, root, keyDepthValue,
                KeyboardSettings.MAX_KEY_DEPTH_DP, 8, () -> !syncing, progress ->
                updateSettings(settings.withKeyDepth(settings.keyDepthEnabled, progress)));

        keyFaceGradientCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                R.string.theme_key_face_gradient,
                12,
                () -> !syncing,
                checked ->
                        updateSettings(settings.withVisualEffects(
                                settings.visualEffects.withKeyFaceGradient(
                                        checked,
                                        settings.visualEffects.keyFaceGradientStrengthPercent))));

        keyFaceGradientStrengthValue = SettingsRowBuilder.valueLabel(this);
        keyFaceGradientStrengthSeekBar = SettingsRowBuilder.seekBarRow(this, root, keyFaceGradientStrengthValue,
                100, 8, () -> !syncing, progress ->
                updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withKeyFaceGradient(
                                settings.visualEffects.keyFaceGradientEnabled,
                                progress))));

        IntConsumer keyFaceGradientStartListener = color -> updateSettings(settings.withVisualEffects(
                settings.visualEffects.withKeyFaceGradient(
                        true,
                        settings.visualEffects.keyFaceGradientStrengthPercent,
                        color,
                        settings.visualEffects.keyFaceGradientEndColor,
                        settings.visualEffects.keyFaceGradientCurve)));
        ColorControl keyFaceGradientStartControl = addColorSetting(
                root,
                "Key face gradient highlight",
                "Top blend color for the key face gradient.",
                keyFaceGradientStartListener);
        keyFaceGradientStartColorSpinner = keyFaceGradientStartControl.spinner;
        keyFaceGradientStartColorSwatch = keyFaceGradientStartControl.swatch;

        IntConsumer keyFaceGradientEndListener = color -> updateSettings(settings.withVisualEffects(
                settings.visualEffects.withKeyFaceGradient(
                        true,
                        settings.visualEffects.keyFaceGradientStrengthPercent,
                        settings.visualEffects.keyFaceGradientStartColor,
                        color,
                        settings.visualEffects.keyFaceGradientCurve)));
        ColorControl keyFaceGradientEndControl = addColorSetting(
                root,
                "Key face gradient shade",
                "Bottom blend color for the key face gradient.",
                keyFaceGradientEndListener);
        keyFaceGradientEndColorSpinner = keyFaceGradientEndControl.spinner;
        keyFaceGradientEndColorSwatch = keyFaceGradientEndControl.swatch;

        keyFaceGradientCurveSpinner = createKeyFaceGradientCurveSpinner();
        SettingsRowBuilder.labeledControl(
                this,
                root,
                R.string.theme_key_face_gradient_curve,
                keyFaceGradientCurveSpinner,
                8);
    }

    private void addBackgroundControls(LinearLayout root) {
        materialStyleSpinner = createMaterialStyleSpinner();
        SettingsRowBuilder.labeledControl(
                this,
                root,
                R.string.theme_material_style,
                materialStyleSpinner,
                8);
        SettingsRowBuilder.secondaryLabelRow(
                this,
                root,
                R.string.theme_material_style_description,
                4);

        SettingsRowBuilder.labelRow(this, root, R.string.theme_glass_tint, 8);
        glassTintValue = SettingsRowBuilder.valueLabel(this);
        glassTintSeekBar = SettingsRowBuilder.seekBarRow(
                this,
                root,
                glassTintValue,
                53,
                0,
                () -> !syncing && settings.visualEffects.usesGlassSurface(),
                progress -> updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withGlass(
                                true,
                                progress + 45,
                                settings.visualEffects.glassHighlightPercent,
                                settings.visualEffects.glassBorderAlphaPercent))));

        SettingsRowBuilder.labelRow(this, root, R.string.theme_glass_highlight, 8);
        glassHighlightValue = SettingsRowBuilder.valueLabel(this);
        glassHighlightSeekBar = SettingsRowBuilder.seekBarRow(
                this,
                root,
                glassHighlightValue,
                60,
                0,
                () -> !syncing && settings.visualEffects.usesGlassSurface(),
                progress -> updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withGlass(
                                true,
                                settings.visualEffects.glassTintAlphaPercent,
                                progress,
                                settings.visualEffects.glassBorderAlphaPercent))));

        pseudoBlurCheckBox = SettingsRowBuilder.checkBoxRow(
                this,
                root,
                R.string.theme_pseudo_blur_enabled,
                12,
                () -> !syncing,
                checked -> updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withBlur(
                                checked,
                                settings.visualEffects.blurRadiusDp))));
        SettingsRowBuilder.secondaryLabelRow(
                this,
                root,
                R.string.theme_pseudo_blur_enabled_description,
                4);

        SettingsRowBuilder.labelRow(
                this,
                root,
                R.string.theme_pseudo_blur_radius,
                8);
        pseudoBlurRadiusValue = SettingsRowBuilder.valueLabel(this);
        pseudoBlurRadiusSeekBar = SettingsRowBuilder.seekBarRow(
                this,
                root,
                pseudoBlurRadiusValue,
                32,
                8,
                () -> !syncing,
                progress -> updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withBlur(
                                settings.visualEffects.blurEnabled,
                                progress))));
    }

    private void addTypographyControls(LinearLayout root) {
        fontFamilySpinner = fontSpinner();
        SettingsRowBuilder.labeledControl(
                this,
                root,
                R.string.theme_font_label,
                fontFamilySpinner,
                8);

        primaryTextSizeValue = SettingsRowBuilder.valueLabel(this);
        primaryTextSizeSeekBar = SettingsRowBuilder.seekBarRow(this, root, primaryTextSizeValue,
                KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT,
                8,
                () -> !syncing,
                progress -> updateSettings(settings.withTypography(
                        settings.fontFamily,
                        KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic)));

        secondaryTextSizeValue = SettingsRowBuilder.valueLabel(this);
        secondaryTextSizeSeekBar = SettingsRowBuilder.seekBarRow(this, root, secondaryTextSizeValue,
                KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT,
                8,
                () -> !syncing,
                progress -> updateSettings(settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic)));

        primaryTextBoldCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_primary_text_bold,
                12,
                checked -> settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        checked,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic));
        primaryTextItalicCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_primary_text_italic,
                4,
                checked -> settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        checked,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic));
        secondaryTextBoldCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_secondary_text_bold,
                8,
                checked -> settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        checked,
                        settings.secondaryTextItalic));
        secondaryTextItalicCheckBox = addTypographyCheckBox(
                root,
                R.string.theme_secondary_text_italic,
                4,
                checked -> settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        checked));
    }

    private CheckBox addTypographyCheckBox(
            LinearLayout root,
            int labelResId,
            int topMarginDp,
            Function<Boolean, KeyboardSettings> change) {
        return SettingsRowBuilder.checkBoxRow(
                this,
                root,
                labelResId,
                topMarginDp,
                () -> !syncing,
                checked -> updateSettings(change.apply(checked)));
    }

    private void addIconPackControls(LinearLayout root) {
        modifierIconPackSpinner = modifierIconPackSpinner();
        SettingsRowBuilder.labeledControl(
                this,
                root,
                R.string.theme_modifier_icons,
                modifierIconPackSpinner,
                8);

        keyDisplayPackSpinner = keyDisplayPackSpinner();
        SettingsRowBuilder.labeledControl(
                this,
                root,
                R.string.theme_key_display_override_pack,
                keyDisplayPackSpinner,
                8);
    }

    private void updateSettings(KeyboardSettings next) {
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
        modeGroup.check(settings.keyboardMode == KeyboardMode.ENGLISH ? MODE_ENGLISH_ID : MODE_HANGUL_ID);
        SettingsRowBuilder.setProgressIfPresent(roundnessSeekBar, settings.keyRoundnessDp);
        SettingsRowBuilder.setProgressIfPresent(keyBorderWidthSeekBar, settings.keyBorderWidthDp);
        SettingsRowBuilder.setProgressIfPresent(keyGapSeekBar, settings.keyGapDp);
        SettingsRowBuilder.setProgressIfPresent(
                pseudoBlurRadiusSeekBar,
                settings.visualEffects.blurRadiusDp);
        SettingsRowBuilder.setProgressIfPresent(
                glassTintSeekBar,
                settings.visualEffects.glassTintAlphaPercent - 45);
        SettingsRowBuilder.setProgressIfPresent(
                glassHighlightSeekBar,
                settings.visualEffects.glassHighlightPercent);
        SettingsRowBuilder.setProgressIfPresent(keyDepthSeekBar, settings.keyDepthDp);
        SettingsRowBuilder.setProgressIfPresent(
                keyFaceGradientStrengthSeekBar,
                settings.visualEffects.keyFaceGradientStrengthPercent);
        SettingsRowBuilder.setProgressIfPresent(
                primaryTextSizeSeekBar,
                settings.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        SettingsRowBuilder.setProgressIfPresent(
                secondaryTextSizeSeekBar,
                settings.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        SettingsRowBuilder.setSelectionIfValid(
                keyIdleColorSpinner,
                ColorOption.editorIndexOf(settings.keyIdleColor));
        SettingsRowBuilder.setSelectionIfValid(
                functionKeyColorSpinner,
                ColorOption.editorIndexOf(settings.functionKeyColor));
        SettingsRowBuilder.setSelectionIfValid(
                accentKeyColorSpinner,
                ColorOption.editorIndexOf(settings.accentKeyColor));
        SettingsRowBuilder.setSelectionIfValid(
                keyPressedColorSpinner,
                ColorOption.editorIndexOf(settings.keyPressedColor));
        SettingsRowBuilder.setSelectionIfValid(
                keyboardBackgroundColorSpinner,
                ColorOption.editorIndexOf(settings.keyboardBackgroundColor));
        SettingsRowBuilder.setSelectionIfValid(
                keyFaceGradientStartColorSpinner,
                ColorOption.editorIndexOf(settings.visualEffects.keyFaceGradientStartColor));
        SettingsRowBuilder.setSelectionIfValid(
                keyFaceGradientEndColorSpinner,
                ColorOption.editorIndexOf(settings.visualEffects.keyFaceGradientEndColor));
        SettingsRowBuilder.setSelectionIfValid(
                keyFaceGradientCurveSpinner,
                KeyboardVisualEffects.keyFaceGradientCurveIndexOf(settings.visualEffects.keyFaceGradientCurve));
        SettingsRowBuilder.setSelectionIfValid(
                materialStyleSpinner,
                KeyboardVisualEffects.materialStyleIndexOf(settings.visualEffects.materialStyle));
        SettingsRowBuilder.setSelectionIfValid(
                panelGradientStartColorSpinner,
                ColorOption.editorIndexOf(settings.visualEffects.panelGradientStartColor));
        SettingsRowBuilder.setSelectionIfValid(
                panelGradientEndColorSpinner,
                ColorOption.editorIndexOf(settings.visualEffects.panelGradientEndColor));
        SettingsRowBuilder.setSelectionIfValid(
                accentColorSpinner,
                ColorOption.editorIndexOf(settings.accentColor));
        SettingsRowBuilder.setSelectionIfValid(
                secondaryColorSpinner,
                ColorOption.editorIndexOf(settings.secondaryColor));
        SettingsRowBuilder.setSelectionIfValid(
                borderColorSpinner,
                ColorOption.editorIndexOf(settings.borderColor));
        SettingsRowBuilder.setSelectionIfValid(
                depthColorSpinner,
                ColorOption.editorIndexOf(settings.depthColor));
        SettingsRowBuilder.setSelectionIfValid(fontFamilySpinner, FontOption.editorIndexOf(settings.fontFamily));
        SettingsRowBuilder.setSelectionIfValid(
                modifierIconPackSpinner,
                ModifierIconCatalog.selectablePackIndexOf(settings.modifierIconThemePackId, false));
        SettingsRowBuilder.setSelectionIfValid(
                keyDisplayPackSpinner,
                KeyDisplayOverridePackCatalog.selectablePackIndexOf(settings.keyDisplayThemePackId, false));
        SettingsRowBuilder.setCheckedIfPresent(keyDepthCheckBox, settings.keyDepthEnabled);
        SettingsRowBuilder.setCheckedIfPresent(
                pseudoBlurCheckBox,
                settings.visualEffects.blurEnabled);
        SettingsRowBuilder.setCheckedIfPresent(customDepthColorCheckBox, settings.customDepthColorEnabled);
        SettingsRowBuilder.setCheckedIfPresent(
                keyFaceGradientCheckBox,
                settings.visualEffects.keyFaceGradientEnabled);
        SettingsRowBuilder.setCheckedIfPresent(
                panelGradientCheckBox,
                settings.visualEffects.panelGradientEnabled);
        SettingsRowBuilder.setCheckedIfPresent(primaryTextBoldCheckBox, settings.primaryTextBold);
        SettingsRowBuilder.setCheckedIfPresent(primaryTextItalicCheckBox, settings.primaryTextItalic);
        SettingsRowBuilder.setCheckedIfPresent(secondaryTextBoldCheckBox, settings.secondaryTextBold);
        SettingsRowBuilder.setCheckedIfPresent(secondaryTextItalicCheckBox, settings.secondaryTextItalic);
        SettingsRowBuilder.setEnabledIfPresent(depthColorSpinner, settings.customDepthColorEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                pseudoBlurRadiusSeekBar,
                settings.visualEffects.blurEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                glassTintSeekBar,
                settings.visualEffects.usesGlassSurface());
        SettingsRowBuilder.setEnabledIfPresent(
                glassHighlightSeekBar,
                settings.visualEffects.usesGlassSurface());
        SettingsRowBuilder.setEnabledIfPresent(
                panelGradientStartColorSpinner,
                settings.visualEffects.panelGradientEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                panelGradientEndColorSpinner,
                settings.visualEffects.panelGradientEnabled);
        SettingsRowBuilder.setEnabledIfPresent(keyDepthSeekBar, settings.keyDepthEnabled);
        SettingsRowBuilder.setEnabledIfPresent(keyFaceGradientCheckBox, settings.keyDepthEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                keyFaceGradientStrengthSeekBar,
                settings.keyDepthEnabled && settings.visualEffects.keyFaceGradientEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                keyFaceGradientStartColorSpinner,
                settings.keyDepthEnabled && settings.visualEffects.keyFaceGradientEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                keyFaceGradientEndColorSpinner,
                settings.keyDepthEnabled && settings.visualEffects.keyFaceGradientEnabled);
        SettingsRowBuilder.setEnabledIfPresent(
                keyFaceGradientCurveSpinner,
                settings.keyDepthEnabled && settings.visualEffects.keyFaceGradientEnabled);
        setSwatch(keyIdleColorSwatch, settings.keyIdleColor);
        setSwatch(functionKeyColorSwatch, settings.functionKeyColor);
        setSwatch(accentKeyColorSwatch, settings.accentKeyColor);
        setSwatch(keyPressedColorSwatch, settings.keyPressedColor);
        setSwatch(keyboardBackgroundColorSwatch, settings.keyboardBackgroundColor);
        setSwatch(keyFaceGradientStartColorSwatch, settings.visualEffects.keyFaceGradientStartColor);
        setSwatch(keyFaceGradientEndColorSwatch, settings.visualEffects.keyFaceGradientEndColor);
        setSwatch(panelGradientStartColorSwatch, settings.visualEffects.panelGradientStartColor);
        setSwatch(panelGradientEndColorSwatch, settings.visualEffects.panelGradientEndColor);
        setSwatch(borderColorSwatch, settings.borderColor);
        setSwatch(depthColorSwatch, settings.customDepthColorEnabled ? settings.depthColor : settings.borderColor);
        setSwatch(accentColorSwatch, settings.accentColor);
        setSwatch(secondaryColorSwatch, settings.secondaryColor);

        SettingsRowBuilder.setTextIfPresent(
                roundnessValue,
                getString(R.string.theme_roundness_format, settings.keyRoundnessDp));
        SettingsRowBuilder.setTextIfPresent(
                keyBorderWidthValue,
                getString(R.string.theme_border_width_format, settings.keyBorderWidthDp));
        SettingsRowBuilder.setTextIfPresent(
                keyGapValue,
                getString(R.string.theme_key_gap_format, settings.keyGapDp));
        SettingsRowBuilder.setTextIfPresent(
                glassTintValue,
                settings.visualEffects.glassTintAlphaPercent + "%");
        SettingsRowBuilder.setTextIfPresent(
                glassHighlightValue,
                settings.visualEffects.glassHighlightPercent + "%");
        SettingsRowBuilder.setTextIfPresent(keyDepthValue, getString(
                R.string.theme_depth_format,
                settings.keyDepthDp,
                settings.keyDepthEnabled ? "" : getString(R.string.settings_flat_suffix)));
        SettingsRowBuilder.setTextIfPresent(
                keyFaceGradientStrengthValue,
                getString(
                        R.string.theme_surface_gradient_strength_format,
                        settings.visualEffects.keyFaceGradientStrengthPercent));
        SettingsRowBuilder.setTextIfPresent(primaryTextSizeValue, getString(
                R.string.theme_primary_text_size_format,
                settings.primaryTextSizePercent));
        SettingsRowBuilder.setTextIfPresent(secondaryTextSizeValue, getString(
                R.string.theme_secondary_text_size_format,
                settings.secondaryTextSizePercent));
        updatePreviewHeight();
        preview.setSettings(settings);
        syncSelectedKeyInspector();
        preview.post(() -> syncing = false);
    }

    private void syncSelectedKeyInspector() {
        boolean keySelected = selectedKey != null && !selectedOverrideKey.isEmpty();
        boolean keyEdit = keySelected && editScopeGroup.getCheckedRadioButtonId() == EDIT_KEY_TEXT_ID;
        KeyVisualRole role = keySelected
                ? KeyboardKeyVisualClassifier.roleFor(settings, selectedKey)
                : KeyVisualRole.ALPHA;
        selectedKeyLabel.setText(keySelected
                ? getString(
                        R.string.theme_selected_key_summary_format,
                        displayKeyName(selectedKey),
                        visualRoleLabel(role),
                        selectedOverrideKey)
                : getString(R.string.theme_no_key_selected));
        selectedKeyColorSpinner.setEnabled(keyEdit);
        selectedKeyBackgroundColorSpinner.setEnabled(keyEdit);
        addSelectedKeyOverrideButton.setEnabled(keyEdit);
        boolean hasTextOverride = keySelected && settings.keyColorOverrides.containsKey(
                KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey));
        boolean hasBackgroundOverride = keySelected && settings.keyColorOverrides.containsKey(
                KeyboardSettings.normalizeKeyOverrideName(backgroundOverrideKey(selectedOverrideKey)));
        resetSelectedKeyButton.setEnabled(keyEdit && (hasTextOverride || hasBackgroundOverride));
        Integer override = keySelected
                ? settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey))
                : null;
        Integer backgroundOverride = keySelected
                ? settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(
                        backgroundOverrideKey(selectedOverrideKey)))
                : null;
        boolean wasSyncing = syncing;
        syncing = true;
        SettingsRowBuilder.setSelectionIfValid(
                selectedKeyColorSpinner,
                ColorOption.editorIndexOf(override == null ? settings.accentColor : override));
        setSwatch(selectedKeyColorSwatch, override == null ? settings.accentColor : override);
        SettingsRowBuilder.setSelectionIfValid(
                selectedKeyBackgroundColorSpinner,
                ColorOption.editorIndexOf(backgroundOverride == null
                        ? KeyboardKeyVisualClassifier.colorFor(settings, selectedKey)
                        : backgroundOverride));
        setSwatch(
                selectedKeyBackgroundColorSwatch,
                backgroundOverride == null
                        ? KeyboardKeyVisualClassifier.colorFor(settings, selectedKey)
                        : backgroundOverride);
        syncing = wasSyncing;
    }

    private String backgroundOverrideKey(String key) {
        return "background:" + key;
    }

    private void updatePreviewHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) preview.getLayoutParams();
        params.height = SettingsRowBuilder.dp(this, settings.measuredHeightDp());
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

    private String visualRoleLabel(KeyVisualRole role) {
        if (role == null) {
            return "alpha";
        }
        switch (role) {
            case ACCENT:
                return "accent";
            case MODIFIER:
                return "modifier";
            case ALPHA:
            default:
                return "alpha";
        }
    }

    private LinearLayout addExpandableSection(LinearLayout root, String text, boolean expandedByDefault) {
        SettingsSectionCard section = SettingsSectionCard.create(this, text, expandedByDefault);
        SettingsRowBuilder.addViewWithTop(this, root, section.container, 18);
        return section.content;
    }

    private ColorControl addColorSetting(
            LinearLayout root,
            int titleResId,
            int descriptionResId,
            IntConsumer listener) {
        return addColorSetting(
                root,
                getString(titleResId),
                getString(descriptionResId),
                listener);
    }

    private ColorControl addColorSetting(
            LinearLayout root,
            String title,
            String description,
            IntConsumer listener) {
        Spinner spinner = colorSpinner(listener);
        View swatch = addColorSpinnerControl(root, title, description, spinner, listener);
        return new ColorControl(spinner, swatch);
    }

    private View addColorSpinnerControl(
            LinearLayout root,
            String title,
            String description,
            Spinner spinner,
            IntConsumer listener) {
        View swatch = addColorControl(root, title, description, spinner, listener);
        SettingsRowBuilder.addView(root, spinner);
        return swatch;
    }

    private View addColorControl(
            LinearLayout root,
            String title,
            String description,
            Spinner spinner,
            IntConsumer listener) {
        LinearLayout row = SettingsRowBuilder.horizontal(this);
        TextView label = SettingsRowBuilder.label(this, title);
        row.addView(label, SettingsRowBuilder.weightedWrap(this, 0, 0));

        View swatch = colorSwatch();
        row.addView(swatch, SettingsRowBuilder.fixedSizeWithLeft(this, 42, 28, 8));

        TextView code = SettingsRowBuilder.valueLabel(this);
        code.setTextSize(12);
        row.addView(code, SettingsRowBuilder.fixedWidthWrapWithLeft(this, 86, 8));
        swatchCodeLabels.put(swatch, code);

        TextView info = infoButton();
        info.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton(R.string.action_ok, null)
                .show());
        row.addView(info, SettingsRowBuilder.fixedSizeWithLeft(this, 30, 30, 8));
        swatch.setOnClickListener(v -> showColorEditDialog(title, colorTag(swatch), listener));
        code.setOnClickListener(v -> showColorEditDialog(title, colorTag(swatch), listener));
        SettingsRowBuilder.addViewWithTop(this, root, row, 8);
        if (spinner != null) {
            spinner.setContentDescription(title);
        }
        return swatch;
    }

    private TextView infoButton() {
        TextView info = SettingsRowBuilder.label(this, "?");
        info.setGravity(Gravity.CENTER);
        info.setTextSize(13);
        info.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        info.setTextColor(ui.textSecondary);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(ui.controlFill);
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(this, 1)), ui.border);
        info.setBackground(background);
        return info;
    }

    private View addInlineSwatch(LinearLayout root, int color) {
        View swatch = colorSwatch();
        setSwatch(swatch, color);
        root.addView(swatch, SettingsRowBuilder.matchHeightWithVerticalMargins(this, 18, 4));
        return swatch;
    }

    private View colorSwatch() {
        View swatch = new View(this);
        swatch.setMinimumHeight(SettingsRowBuilder.dp(this, 18));
        return swatch;
    }

    private void setSwatch(View swatch, int color) {
        if (swatch == null) {
            return;
        }
        int opaqueColor = 0xFF000000 | (color & 0x00FFFFFF);
        GradientDrawable background = new GradientDrawable();
        background.setColor(opaqueColor);
        background.setCornerRadius(SettingsRowBuilder.dp(this, 6));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(this, 1)), SettingsUiPalette.from(this).border);
        swatch.setBackground(background);
        swatch.setTag(opaqueColor);
        TextView code = swatchCodeLabels.get(swatch);
        if (code != null) {
            code.setText(colorHex(opaqueColor));
        }
    }

    private int colorTag(View swatch) {
        Object tag = swatch == null ? null : swatch.getTag();
        return tag instanceof Integer ? (Integer) tag : 0xFF000000;
    }

    private void showColorEditDialog(String title, int currentColor, IntConsumer listener) {
        LinearLayout layout = SettingsRowBuilder.vertical(this);
        layout.setPadding(
                SettingsRowBuilder.dp(this, 12),
                SettingsRowBuilder.dp(this, 8),
                SettingsRowBuilder.dp(this, 12),
                SettingsRowBuilder.dp(this, 4));

        TextView description = SettingsRowBuilder.label(
                this,
                getString(R.string.theme_color_edit_description));
        SettingsRowBuilder.addView(layout, description);

        EditText editor = SettingsRowBuilder.editText(this);
        editor.setSingleLine(true);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setText(colorHex(currentColor).substring(1));
        editor.setSelectAllOnFocus(true);
        SettingsRowBuilder.addViewWithTop(this, layout, editor, 8);

        LinearLayout presetGrid = SettingsRowBuilder.vertical(this);
        SettingsRowBuilder.addViewWithTop(this, layout, presetGrid, 10);
        addPresetColorRows(presetGrid, listener);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_apply, (dialog, which) -> {
                    Integer parsed = parseHexColor(editor.getText().toString());
                    if (parsed != null) {
                        listener.accept(parsed);
                    }
                })
                .show();
    }

    private void addPresetColorRows(LinearLayout root, IntConsumer listener) {
        LinearLayout row = null;
        for (int i = 0; i < ColorOption.EDITOR_OPTIONS.length; i++) {
            if (i % 3 == 0) {
                row = SettingsRowBuilder.horizontal(this);
                SettingsRowBuilder.addViewWithTop(this, root, row, i == 0 ? 0 : 6);
            }
            ColorOption option = ColorOption.EDITOR_OPTIONS[i];
            Button button = SettingsRowBuilder.button(
                    this,
                    SettingsDisplayLabels.label(this, option),
                    false,
                    v -> listener.accept(option.color));
            button.setTextSize(11);
            button.setTextColor(KeyboardColorMath.contrastTextColor(option.color, 150));
            GradientDrawable background = new GradientDrawable();
            background.setColor(option.color);
            background.setCornerRadius(SettingsRowBuilder.dp(this, 7));
            background.setStroke(Math.max(1, SettingsRowBuilder.dp(this, 1)), SettingsUiPalette.from(this).border);
            button.setBackground(background);
            row.addView(button, SettingsRowBuilder.weightedHeight(this, 40, i % 3 == 0 ? 0 : 6));
        }
    }

    private Integer parseHexColor(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6 && normalized.length() != 8) {
            return null;
        }
        try {
            long parsed = Long.parseLong(normalized, 16);
            if (normalized.length() == 6) {
                parsed |= 0xFF000000L;
            }
            return (int) parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String colorHex(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    private int dimColor(int color, float factor) {
        int red = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
        int green = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
        int blue = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private void copyThemeJsonToClipboard() {
        String json = currentThemeJson();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.theme_json_clip_label), json));
        }
        Toast.makeText(this, R.string.theme_json_copied, Toast.LENGTH_SHORT).show();
    }

    private String currentThemeJson() {
        return KeyboardThemeJson.exportTheme(settings, "Current Theme", "local", null);
    }

    private void copyPromptToClipboard(String label, String prompt, String toast) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, prompt));
        }
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    private void showThemeJsonImportDialog() {
        EditText editor = SettingsRowBuilder.editText(this);
        editor.setMinLines(8);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        String clipboardText = currentClipboardText();
        if (!clipboardText.isEmpty()) {
            editor.setText(clipboardText);
            editor.setSelection(editor.length());
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.theme_json_import_title)
                .setView(editor)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_import, (dialog, which) -> importThemeJson(editor.getText().toString()))
                .show();
    }

    private String currentClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null
                || clipboard.getPrimaryClip().getItemCount() == 0) {
            return "";
        }
        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
        return RuntimeDefaults.stringOrEmpty(text);
    }

    private void importThemeJson(String json) {
        try {
            settings = KeyboardThemeJson.importTheme(settings, json);
            selectedKey = null;
            selectedOverrideKey = "";
            KeyboardPreferences.saveSelectedThemeId(this, "");
            KeyboardPreferences.saveSettings(this, settings);
            syncControls();
            Toast.makeText(this, R.string.theme_json_imported, Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException exception) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.theme_json_import_failed)
                    .setMessage(exception.getMessage())
                    .setPositiveButton(R.string.action_ok, null)
                    .show();
        }
    }

    private Spinner colorSpinner(IntConsumer listener) {
        return SettingsRowBuilder.optionSpinner(
                this,
                ColorOption.EDITOR_OPTIONS,
                () -> !syncing,
                option -> listener.accept(option.color));
    }

    private Spinner fontSpinner() {
        return SettingsRowBuilder.optionSpinner(
                this,
                FontOption.EDITOR_OPTIONS,
                () -> !syncing,
                option -> {
                    if (!Objects.equals(settings.fontFamily, option.value)) {
                        updateSettings(settings.withFontFamily(option.value));
                    }
                });
    }

    private Spinner modifierIconPackSpinner() {
        return themePackSpinner(
                ModifierIconCatalog.selectablePackLabels(false, ""),
                position -> ModifierIconCatalog.selectablePackIdAt(position, false),
                () -> settings.modifierIconThemePackId,
                packId -> settings.withModifierIconThemePack(packId));
    }

    private Spinner keyDisplayPackSpinner() {
        return themePackSpinner(
                KeyDisplayOverridePackCatalog.selectablePackLabels(false, ""),
                position -> KeyDisplayOverridePackCatalog.selectablePackIdAt(position, false),
                () -> settings.keyDisplayThemePackId,
                packId -> settings.withKeyDisplayThemePack(packId));
    }

    private Spinner themePackSpinner(
            String[] labels,
            IntFunction<String> packIdAt,
            Supplier<String> currentPackId,
            Function<String, KeyboardSettings> change) {
        return SettingsRowBuilder.spinnerAfterInitialSelection(this, labels, () -> !syncing, position -> {
            String packId = packIdAt.apply(position);
            if (!Objects.equals(currentPackId.get(), packId)) {
                updateSettings(change.apply(packId));
            }
        });
    }

    private Spinner createKeyFaceGradientCurveSpinner() {
        return SettingsRowBuilder.spinnerAfterInitialSelection(
                this,
                KeyboardVisualEffects.keyFaceGradientCurveLabels(),
                () -> !syncing,
                position -> {
                    String curve = KeyboardVisualEffects.keyFaceGradientCurveAt(position);
                    if (!Objects.equals(settings.visualEffects.keyFaceGradientCurve, curve)) {
                        updateSettings(settings.withVisualEffects(
                                settings.visualEffects.withKeyFaceGradient(
                                        settings.visualEffects.keyFaceGradientEnabled,
                                        settings.visualEffects.keyFaceGradientStrengthPercent,
                                        settings.visualEffects.keyFaceGradientStartColor,
                                        settings.visualEffects.keyFaceGradientEndColor,
                                        curve)));
                    }
                });
    }

    private Spinner createMaterialStyleSpinner() {
        return SettingsRowBuilder.spinnerAfterInitialSelection(
                this,
                KeyboardVisualEffects.materialStyleLabels(),
                () -> !syncing,
                position -> {
                    String style = KeyboardVisualEffects.materialStyleAt(position);
                    if (!Objects.equals(settings.visualEffects.materialStyle, style)) {
                        updateSettings(settings.withVisualEffects(
                                settings.visualEffects.withMaterialPreset(style)));
                    }
                });
    }

}
