package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final String EXTRA_HANGUL_MAIN_REGION_RATIO = "hangul_main_region_ratio";
    private static final String EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT = "hangul_special_column_percent";
    private static final String EXTRA_HANGUL_MAIN_KEY_UNITS = "hangul_main_key_units";
    private static final String EXTRA_DEMO_SHOW_KEYBOARD = "demo_show_keyboard";
    private static final String EXTRA_KEY_IDLE_COLOR = "key_idle_color";
    private static final String EXTRA_KEY_PRESSED_COLOR = "key_pressed_color";
    private static final String EXTRA_KEYBOARD_BACKGROUND_COLOR = "keyboard_background_color";
    private static final String EXTRA_ACCENT_COLOR = "accent_color";
    private static final String EXTRA_SECONDARY_COLOR = "secondary_color";
    private static final String EXTRA_FUNCTION_KEY_COLOR = "function_key_color";
    private static final String EXTRA_ACCENT_KEY_COLOR = "accent_key_color";
    private static final String EXTRA_BORDER_COLOR = "border_color";
    private static final String EXTRA_KEY_ROUNDNESS_DP = "key_roundness_dp";
    private static final String EXTRA_KEY_GAP_DP = "key_gap_dp";
    private static final String EXTRA_KEY_DEPTH_ENABLED = "key_depth_enabled";
    private static final String EXTRA_KEY_DEPTH_DP = "key_depth_dp";
    private static final String EXTRA_CUSTOM_DEPTH_COLOR_ENABLED = "custom_depth_color_enabled";
    private static final String EXTRA_DEPTH_COLOR = "depth_color";
    private static final String EXTRA_FONT_FAMILY = "font_family";
    private static final String EXTRA_SHOW_HANGUL_SLIDE_HINTS = "show_hangul_slide_hints";
    private static final String EXTRA_SHOW_ENGLISH_SLIDE_HINTS = "show_english_slide_hints";
    private static final String EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW = "show_beginner_tooltip_preview";
    private static final String EXTRA_SHOW_NUMBER_ROW = "show_number_row";
    private static final String EXTRA_SHOW_HANGUL_NUMBER_ROW = "show_hangul_number_row";
    private static final String EXTRA_SHOW_ENGLISH_NUMBER_ROW = "show_english_number_row";
    private static final String EXTRA_DEMO_SETTINGS = "demo_settings";
    private static final String EXTRA_THEME_PRESET_ID = "theme_preset_id";

    private KeyboardSettings settings;
    private boolean syncing;
    private boolean demoShowKeyboard;
    private Spinner handednessSpinner;
    private SeekBar leftMarginSeekBar;
    private SeekBar rightMarginSeekBar;
    private SeekBar hangulHeightSeekBar;
    private SeekBar englishHeightSeekBar;
    private SeekBar hangulSpecialColumnSeekBar;
    private SeekBar keyboardTopPaddingSeekBar;
    private SeekBar keyboardBottomPaddingSeekBar;
    private SeekBar roundnessSeekBar;
    private SeekBar keyBorderWidthSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar keyDepthSeekBar;
    private SeekBar gestureThresholdSeekBar;
    private SeekBar touchYOffsetSeekBar;
    private SeekBar hapticDurationSeekBar;
    private SeekBar hapticGapSeekBar;
    private SeekBar repeatStartDelaySeekBar;
    private SeekBar repeatIntervalSeekBar;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private Spinner themePresetSpinner;
    private Spinner keyIdleColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner functionKeyColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner fontFamilySpinner;
    private Spinner modifierIconPackSpinner;
    private Spinner keyDisplayPackSpinner;
    private Spinner additionalNumberRowColorModeSpinner;
    private Button deleteThemeButton;
    private CheckBox hangulNumberRowCheckBox;
    private CheckBox englishNumberRowCheckBox;
    private CheckBox hapticCheckBox;
    private CheckBox differentiatedHapticCheckBox;
    private CheckBox touchBiasAutoCorrectionCheckBox;
    private CheckBox clipboardHistoryCheckBox;
    private CheckBox doubleSpacePeriodCheckBox;
    private CheckBox keyDepthCheckBox;
    private CheckBox customDepthColorCheckBox;
    private CheckBox followThemeTypographyCheckBox;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private CheckBox pointKeycapStyleCheckBox;
    private CheckBox hangulConsonantSlideHintsCheckBox;
    private CheckBox hangulVowelSlideHintsCheckBox;
    private CheckBox englishSlideHintsCheckBox;
    private CheckBox spacebarSlideHintsCheckBox;
    private CheckBox beginnerTooltipPreviewCheckBox;
    private TextView leftMarginValue;
    private TextView rightMarginValue;
    private TextView hangulHeightValue;
    private TextView englishHeightValue;
    private TextView hangulSpecialColumnValue;
    private TextView keyboardTopPaddingValue;
    private TextView keyboardBottomPaddingValue;
    private TextView numberRowBottomGapValue;
    private EditText leftMarginInput;
    private EditText rightMarginInput;
    private EditText keyboardTopPaddingInput;
    private EditText keyboardBottomPaddingInput;
    private EditText numberRowBottomGapInput;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private TextView gestureThresholdValue;
    private TextView touchYOffsetValue;
    private TextView repeatStartDelayValue;
    private TextView repeatIntervalValue;
    private TextView hapticDurationValue;
    private TextView hapticGapValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private LinearLayout themePresetCards;
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedThemePresetIndex;
    private EditText gestureTestInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        settings = KeyboardPreferences.load(this);
        KeyboardPreferences.saveFloatingModeEnabled(this, false);
        applyIntentOverrides(getIntent());
        syncing = true;
        setContentView(createContentView());
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        KeyboardPreferences.saveFloatingModeEnabled(this, false);
        applyIntentOverrides(getIntent());
        syncControls();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && gestureTestInput != null
                && gestureTestInput.hasFocus()
                && isTouchOutsideView(event, gestureTestInput)) {
            gestureTestInput.clearFocus();
            InputMethodManager imm = getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(gestureTestInput.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean isTouchOutsideView(MotionEvent event, View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return !rect.contains(Math.round(event.getRawX()), Math.round(event.getRawY()));
    }

    private void applyIntentOverrides(Intent intent) {
        if (intent == null) {
            return;
        }

        boolean debugDemoIntent = isDebugDemoIntent(intent);
        demoShowKeyboard = debugDemoIntent
                && intent.getBooleanExtra(EXTRA_DEMO_SHOW_KEYBOARD, demoShowKeyboard);
        if (!debugDemoIntent || !hasDemoSettingOverride(intent)) {
            return;
        }

        String themePresetId = intent.getStringExtra(EXTRA_THEME_PRESET_ID);
        KeyboardThemePreset themePreset = KeyboardThemePreset.find(themePresetId);
        if (themePreset != null) {
            settings = settings.withAppearanceFrom(themePreset.applyTo(KeyboardSettings.defaults()));
            KeyboardPreferences.saveSelectedThemeId(this, themePreset.id);
        }

        if (intent.hasExtra(EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT)) {
            settings = settings.withHangulSpecialColumnPercent(intent.getIntExtra(
                    EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT,
                    settings.hangulSpecialColumnPercent));
        } else if (intent.hasExtra(EXTRA_HANGUL_MAIN_REGION_RATIO)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_KEY_UNITS)) {
            settings = settings.withHangulMainKeyUnits(intent.getIntExtra(
                    EXTRA_HANGUL_MAIN_REGION_RATIO,
                    intent.getIntExtra(
                            EXTRA_HANGUL_MAIN_KEY_UNITS,
                            5)));
        }
        boolean customDepthColorEnabled = intent.hasExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED)
                ? intent.getBooleanExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED, settings.customDepthColorEnabled)
                : (settings.customDepthColorEnabled || intent.hasExtra(EXTRA_DEPTH_COLOR));
        settings = settings
                .withExtendedThemeColors(
                        colorExtra(intent, EXTRA_KEY_IDLE_COLOR, settings.keyIdleColor),
                        colorExtra(intent, EXTRA_KEY_PRESSED_COLOR, settings.keyPressedColor),
                        colorExtra(intent, EXTRA_KEYBOARD_BACKGROUND_COLOR, settings.keyboardBackgroundColor),
                        colorExtra(intent, EXTRA_ACCENT_COLOR, settings.accentColor),
                        colorExtra(intent, EXTRA_SECONDARY_COLOR, settings.secondaryColor),
                        colorExtra(intent, EXTRA_FUNCTION_KEY_COLOR, settings.functionKeyColor),
                        colorExtra(intent, EXTRA_ACCENT_KEY_COLOR, settings.accentKeyColor),
                        colorExtra(intent, EXTRA_BORDER_COLOR, settings.borderColor),
                        customDepthColorEnabled,
                        colorExtra(intent, EXTRA_DEPTH_COLOR, settings.depthColor))
                .withFontFamily(stringExtra(intent, EXTRA_FONT_FAMILY, settings.fontFamily))
                .withHintVisibility(
                        intent.getBooleanExtra(
                                EXTRA_SHOW_HANGUL_SLIDE_HINTS,
                                settings.showHangulSlideHints),
                        intent.getBooleanExtra(
                                EXTRA_SHOW_ENGLISH_SLIDE_HINTS,
                                settings.showEnglishSlideHints),
                        intent.getBooleanExtra(
                                EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW,
                                settings.showBeginnerTooltipPreview))
                .withKeyRoundness(intent.getIntExtra(EXTRA_KEY_ROUNDNESS_DP, settings.keyRoundnessDp))
                .withKeyGap(intent.getIntExtra(EXTRA_KEY_GAP_DP, settings.keyGapDp))
                .withKeyDepth(
                        intent.getBooleanExtra(EXTRA_KEY_DEPTH_ENABLED, settings.keyDepthEnabled),
                        intent.getIntExtra(EXTRA_KEY_DEPTH_DP, settings.keyDepthDp));
        if (intent.hasExtra(EXTRA_SHOW_NUMBER_ROW)) {
            settings = settings.withNumberRow(intent.getBooleanExtra(EXTRA_SHOW_NUMBER_ROW, settings.showNumberRow));
        }
        settings = settings
                .withHangulNumberRow(intent.getBooleanExtra(
                        EXTRA_SHOW_HANGUL_NUMBER_ROW,
                        settings.showHangulNumberRow))
                .withEnglishNumberRow(intent.getBooleanExtra(
                        EXTRA_SHOW_ENGLISH_NUMBER_ROW,
                        settings.showEnglishNumberRow));
        KeyboardPreferences.saveSettings(this, settings);
    }

    private boolean isDebugDemoIntent(Intent intent) {
        return isDebuggable() && intent.getBooleanExtra(EXTRA_DEMO_SETTINGS, false);
    }

    private boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private boolean hasDemoSettingOverride(Intent intent) {
        return intent.hasExtra(EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_REGION_RATIO)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_KEY_UNITS)
                || intent.hasExtra(EXTRA_KEY_IDLE_COLOR)
                || intent.hasExtra(EXTRA_KEY_PRESSED_COLOR)
                || intent.hasExtra(EXTRA_KEYBOARD_BACKGROUND_COLOR)
                || intent.hasExtra(EXTRA_ACCENT_COLOR)
                || intent.hasExtra(EXTRA_SECONDARY_COLOR)
                || intent.hasExtra(EXTRA_FUNCTION_KEY_COLOR)
                || intent.hasExtra(EXTRA_ACCENT_KEY_COLOR)
                || intent.hasExtra(EXTRA_BORDER_COLOR)
                || intent.hasExtra(EXTRA_KEY_ROUNDNESS_DP)
                || intent.hasExtra(EXTRA_KEY_GAP_DP)
                || intent.hasExtra(EXTRA_KEY_DEPTH_ENABLED)
                || intent.hasExtra(EXTRA_KEY_DEPTH_DP)
                || intent.hasExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED)
                || intent.hasExtra(EXTRA_DEPTH_COLOR)
                || intent.hasExtra(EXTRA_FONT_FAMILY)
                || intent.hasExtra(EXTRA_SHOW_HANGUL_SLIDE_HINTS)
                || intent.hasExtra(EXTRA_SHOW_ENGLISH_SLIDE_HINTS)
                || intent.hasExtra(EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW)
                || intent.hasExtra(EXTRA_SHOW_NUMBER_ROW)
                || intent.hasExtra(EXTRA_SHOW_HANGUL_NUMBER_ROW)
                || intent.hasExtra(EXTRA_SHOW_ENGLISH_NUMBER_ROW)
                || intent.hasExtra(EXTRA_THEME_PRESET_ID);
    }

    private String stringExtra(Intent intent, String name, String fallback) {
        String value = intent.getStringExtra(name);
        return value == null ? fallback : value;
    }

    private int colorExtra(Intent intent, String name, int fallback) {
        if (!intent.hasExtra(name)) {
            return fallback;
        }
        try {
            String value = intent.getStringExtra(name);
            if (value != null && !value.startsWith("#")
                    && (value.length() == 6 || value.length() == 8)) {
                value = "#" + value;
            }
            return Color.parseColor(value);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private ScrollView createContentView() {
        int padding = dp(16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, padding, padding, padding);
        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextColor(ui.textPrimary);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.bottomMargin = dp(12);
        root.addView(title, titleParams);

        LinearLayout hubSection = addExpandableSection(root, "\uC124\uC815 \uD5C8\uBE0C", true);
        addBodyText(hubSection, getString(R.string.gesture_practice_body), 0);

        EditText testInput = new EditText(this);
        gestureTestInput = testInput;
        testInput.setHint(R.string.gesture_practice_hint);
        testInput.setSingleLine(false);
        testInput.setMinLines(2);
        testInput.setFocusableInTouchMode(true);
        SettingsViewStyler.editText(testInput, this);
        hubSection.addView(testInput, matchWrapWithTop(12));
        maybeShowDemoKeyboard(testInput);

        addThemeQuickControls(hubSection);
        // Keep existing preference wiring alive while appearance editing moves to ThemeEditor.
        addVisualControls(root);

        LinearLayout layoutSection = addExpandableSection(
                root,
                "\uB808\uC774\uC544\uC6C3",
                true);
        addLayoutControls(layoutSection);

        LinearLayout displaySection = addExpandableSection(
                root,
                "\uD45C\uC2DC",
                true);
        addVisibleVisualControls(displaySection);

        LinearLayout inputSection = addExpandableSection(
                root,
                "\uC785\uB825\uAC10",
                false);
        addInputFeelControls(inputSection);

        LinearLayout reservedSection = addExpandableSection(
                root,
                "\uC608\uC57D\uC5B4",
                false);
        addReservedPhraseControls(reservedSection);

        LinearLayout androidSection = addExpandableSection(
                root,
                "Android / IME",
                false);
        addAndroidImeControls(androidSection);

        return scrollView;
    }

    private void maybeShowDemoKeyboard(EditText testInput) {
        if (!demoShowKeyboard) {
            return;
        }
        testInput.requestFocusFromTouch();
        testInput.postDelayed(() -> showSoftInput(testInput, InputMethodManager.SHOW_IMPLICIT), 350);
        testInput.postDelayed(() -> showSoftInput(testInput, InputMethodManager.SHOW_FORCED), 900);
    }

    private void showSoftInput(EditText testInput, int flag) {
        testInput.requestFocusFromTouch();
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.showSoftInput(testInput, flag);
        }
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
                if (syncing) {
                    return;
                }
                settings = settings.withHandednessPreset(HandednessMode.values()[position]);
                saveAndSync();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(label("\uD55C\uC190 \uBAA8\uB4DC"), matchWrapWithTop(12));
        root.addView(handednessSpinner, matchWrap());

        leftMarginValue = label("");
        root.addView(leftMarginValue, matchWrapWithTop(12));
        leftMarginInput = addNumericStepper(root, settings.leftMarginDp, KeyboardSettings.MAX_MARGIN_DP, value -> {
            settings = settings.withSharedMargin(value);
            saveAndSync();
        });

        rightMarginValue = label("");

        keyboardTopPaddingValue = label("");
        root.addView(keyboardTopPaddingValue, matchWrapWithTop(12));
        keyboardTopPaddingInput = addNumericStepper(root,
                settings.keyboardTopPaddingDp,
                KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP,
                value -> {
                    settings = settings.withLayoutSpacing(
                            settings.hangulMainSpecialGapDp,
                            value,
                            settings.keyboardBottomPaddingDp,
                            settings.bottomRowTopPaddingDp);
                    saveAndSync();
                });

        keyboardBottomPaddingValue = label("");
        root.addView(keyboardBottomPaddingValue, matchWrapWithTop(12));
        keyboardBottomPaddingInput = addNumericStepper(root,
                settings.keyboardBottomPaddingDp,
                KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP,
                value -> {
                    settings = settings.withLayoutSpacing(
                            settings.hangulMainSpecialGapDp,
                            settings.keyboardTopPaddingDp,
                            value,
                            settings.bottomRowTopPaddingDp);
                    saveAndSync();
                });

        hangulNumberRowCheckBox = new CheckBox(this);
        hangulNumberRowCheckBox.setText("\uD55C\uAE00 \uC22B\uC790\uC904 \uD45C\uC2DC");
        hangulNumberRowCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withHangulNumberRow(isChecked);
                saveAndSync();
            }
        });
        root.addView(hangulNumberRowCheckBox, matchWrapWithTop(16));

        englishNumberRowCheckBox = new CheckBox(this);
        englishNumberRowCheckBox.setText("\uC601\uBB38 \uC22B\uC790\uC904 \uD45C\uC2DC");
        englishNumberRowCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withEnglishNumberRow(isChecked);
                saveAndSync();
            }
        });
        root.addView(englishNumberRowCheckBox, matchWrapWithTop(8));

        numberRowBottomGapValue = label("");
        root.addView(numberRowBottomGapValue, matchWrapWithTop(12));
        numberRowBottomGapInput = addNumericStepper(root,
                settings.numberRowBottomGapDp,
                KeyboardSettings.MAX_NUMBER_ROW_BOTTOM_GAP_DP,
                value -> {
                    settings = settings.withNumberRowBottomGap(value);
                    saveAndSync();
                });

        hangulHeightValue = label("");
        hangulHeightSeekBar = seekBar(KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP);
        hangulHeightSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withHangulHeight(KeyboardSettings.MIN_HEIGHT_DP + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(hangulHeightValue, matchWrapWithTop(16));
        root.addView(hangulHeightSeekBar, matchWrap());

        englishHeightValue = label("");
        englishHeightSeekBar = seekBar(KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP);
        englishHeightSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withEnglishHeight(KeyboardSettings.MIN_HEIGHT_DP + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(englishHeightValue, matchWrapWithTop(8));
        root.addView(englishHeightSeekBar, matchWrap());

        hangulSpecialColumnValue = label("");
        hangulSpecialColumnSeekBar = seekBar(
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT
                        - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        hangulSpecialColumnSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withHangulSpecialColumnPercent(
                            KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(hangulSpecialColumnValue, matchWrapWithTop(12));
        root.addView(hangulSpecialColumnSeekBar, matchWrap());

    }

    private void addThemeQuickControls(LinearLayout root) {
        Button openThemeSelectorButton = new Button(this);
        openThemeSelectorButton.setText("\uD14C\uB9C8 \uC120\uD0DD");
        styleSystemButton(openThemeSelectorButton);
        setButtonIcon(openThemeSelectorButton, R.drawable.ic_keyboard_keyboard);
        openThemeSelectorButton.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSelectorActivity.class)));
        root.addView(openThemeSelectorButton, buttonParams());

        Button openThemeEditorButton = new Button(this);
        openThemeEditorButton.setText("\uD14C\uB9C8 \uD3B8\uC9D1");
        styleSystemButton(openThemeEditorButton);
        setButtonIcon(openThemeEditorButton, R.drawable.ic_keyboard_settings);
        openThemeEditorButton.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeEditorActivity.class)));
        root.addView(openThemeEditorButton, buttonParams());

        Button resetThemeButton = new Button(this);
        resetThemeButton.setText("\uAE30\uBCF8 \uD14C\uB9C8 \uBCF5\uC6D0");
        styleSystemButton(resetThemeButton);
        setButtonIcon(resetThemeButton, R.drawable.ic_keyboard_reset);
        resetThemeButton.setOnClickListener(v -> resetThemeAppearanceToDefault());
        root.addView(resetThemeButton, buttonParams());
    }

    private void resetThemeAppearanceToDefault() {
        selectedThemePresetIndex = 0;
        settings = ThemeOption.resetToDefaultAppearance(settings);
        KeyboardPreferences.saveSelectedThemeId(this, "");
        saveAndSync();
    }

    private void addVisibleVisualControls(LinearLayout root) {
        root.addView(label("\uC544\uC774\uCF58 \uC2A4\uD0C0\uC77C"), matchWrapWithTop(8));
        modifierIconPackSpinner = modifierIconPackSpinner(true);
        root.addView(modifierIconPackSpinner, matchWrap());

        root.addView(label("\uD45C\uC2DC \uC2A4\uD0C0\uC77C"), matchWrapWithTop(12));
        keyDisplayPackSpinner = keyDisplayPackSpinner(true);
        root.addView(keyDisplayPackSpinner, matchWrap());

        Button accentPlacementButton = new Button(this);
        accentPlacementButton.setText("\uC2DC\uAC01 \uC5ED\uD560 \uD3B8\uC9D1");
        styleSystemButton(accentPlacementButton);
        accentPlacementButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AccentPlacementActivity.class)));
        root.addView(accentPlacementButton, buttonParams());

        fontFamilySpinner = fontSpinner();
        root.addView(label("\uD3F0\uD2B8"), matchWrapWithTop(12));
        root.addView(fontFamilySpinner, matchWrap());

        followThemeTypographyCheckBox = new CheckBox(this);
        followThemeTypographyCheckBox.setText("\uD14C\uB9C8 \uAE00\uAF34/\uAD75\uAE30/\uD06C\uAE30 \uB530\uB974\uAE30");
        followThemeTypographyCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withFollowThemeTypography(isChecked);
                saveAndSync();
            }
        });
        root.addView(followThemeTypographyCheckBox, matchWrapWithTop(8));

        primaryTextSizeValue = label("");
        primaryTextSizeSeekBar = seekBar(
                KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        primaryTextSizeSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withTypography(
                            settings.fontFamily,
                            KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                            settings.secondaryTextSizePercent,
                            settings.primaryTextBold,
                            settings.primaryTextItalic,
                            settings.secondaryTextBold,
                            settings.secondaryTextItalic);
                    saveAndSync();
                }
            }
        });
        root.addView(primaryTextSizeValue, matchWrapWithTop(12));
        root.addView(primaryTextSizeSeekBar, matchWrap());

        secondaryTextSizeValue = label("");
        secondaryTextSizeSeekBar = seekBar(
                KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        secondaryTextSizeSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withTypography(
                            settings.fontFamily,
                            settings.primaryTextSizePercent,
                            KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                            settings.primaryTextBold,
                            settings.primaryTextItalic,
                            settings.secondaryTextBold,
                            settings.secondaryTextItalic);
                    saveAndSync();
                }
            }
        });
        root.addView(secondaryTextSizeValue, matchWrapWithTop(8));
        root.addView(secondaryTextSizeSeekBar, matchWrap());

        primaryTextBoldCheckBox = new CheckBox(this);
        primaryTextBoldCheckBox.setText("\uBA54\uC778 \uAE00\uC790 \uAD75\uAC8C");
        primaryTextBoldCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        isChecked,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic);
                saveAndSync();
            }
        });
        root.addView(primaryTextBoldCheckBox, matchWrapWithTop(8));

        primaryTextItalicCheckBox = new CheckBox(this);
        primaryTextItalicCheckBox.setText("\uBA54\uC778 \uAE00\uC790 \uAE30\uC6B8\uC784");
        primaryTextItalicCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        isChecked,
                        settings.secondaryTextBold,
                        settings.secondaryTextItalic);
                saveAndSync();
            }
        });
        root.addView(primaryTextItalicCheckBox, matchWrapWithTop(4));

        secondaryTextBoldCheckBox = new CheckBox(this);
        secondaryTextBoldCheckBox.setText("\uBCF4\uC870 \uAE00\uC790 \uAD75\uAC8C");
        secondaryTextBoldCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        isChecked,
                        settings.secondaryTextItalic);
                saveAndSync();
            }
        });
        root.addView(secondaryTextBoldCheckBox, matchWrapWithTop(8));

        secondaryTextItalicCheckBox = new CheckBox(this);
        secondaryTextItalicCheckBox.setText("\uBCF4\uC870 \uAE00\uC790 \uAE30\uC6B8\uC784");
        secondaryTextItalicCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withTypography(
                        settings.fontFamily,
                        settings.primaryTextSizePercent,
                        settings.secondaryTextSizePercent,
                        settings.primaryTextBold,
                        settings.primaryTextItalic,
                        settings.secondaryTextBold,
                        isChecked);
                saveAndSync();
            }
        });
        root.addView(secondaryTextItalicCheckBox, matchWrapWithTop(4));

        pointKeycapStyleCheckBox = new CheckBox(this);
        pointKeycapStyleCheckBox.setText("\uD3EC\uC778\uD2B8 \uD0A4\uCEA1 \uC2A4\uD0C0\uC77C \uC0AC\uC6A9");
        pointKeycapStyleCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withPointKeycapStyle(isChecked);
                saveAndSync();
            }
        });
        root.addView(pointKeycapStyleCheckBox, matchWrapWithTop(8));

        hangulConsonantSlideHintsCheckBox = new CheckBox(this);
        hangulConsonantSlideHintsCheckBox.setText("\uD55C\uAE00 \uC790\uC74C \uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8");
        hangulConsonantSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowHangulConsonantSlideHints(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(hangulConsonantSlideHintsCheckBox, matchWrapWithTop(12));

        hangulVowelSlideHintsCheckBox = new CheckBox(this);
        hangulVowelSlideHintsCheckBox.setText("\uD55C\uAE00 \uBAA8\uC74C \uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8");
        hangulVowelSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowHangulVowelSlideHints(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(hangulVowelSlideHintsCheckBox, matchWrapWithTop(8));

        englishSlideHintsCheckBox = new CheckBox(this);
        englishSlideHintsCheckBox.setText("\uC601\uBB38 \uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8");
        englishSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withHintVisibility(
                        settings.showHangulSlideHints,
                        isChecked,
                        settings.showBeginnerTooltipPreview);
                saveAndSync();
            }
        });
        root.addView(englishSlideHintsCheckBox, matchWrapWithTop(8));

        spacebarSlideHintsCheckBox = new CheckBox(this);
        spacebarSlideHintsCheckBox.setText("\uC2A4\uD398\uC774\uC2A4\uBC14 \uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8");
        spacebarSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowSpacebarSlideHints(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(spacebarSlideHintsCheckBox, matchWrapWithTop(8));

        beginnerTooltipPreviewCheckBox = new CheckBox(this);
        beginnerTooltipPreviewCheckBox.setText("\uC785\uB825 preview \uD45C\uC2DC");
        beginnerTooltipPreviewCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withHintVisibility(
                        settings.showHangulSlideHints,
                        settings.showEnglishSlideHints,
                        isChecked);
                saveAndSync();
            }
        });
        root.addView(beginnerTooltipPreviewCheckBox, matchWrapWithTop(8));
    }

    private void addVisualControls(LinearLayout unusedRoot) {
        deleteThemeButton = new Button(this);
        deleteThemeButton.setText("\uC120\uD0DD\uD55C \uC0AC\uC6A9\uC790 \uD14C\uB9C8 \uC0AD\uC81C");
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
        keyIdleColorSpinner = colorSpinner(color -> settings = settings.withThemeColors(
                color, settings.keyPressedColor, settings.keyboardBackgroundColor,
                settings.accentColor, settings.secondaryColor));
        keyPressedColorSpinner = colorSpinner(color -> settings = settings.withThemeColors(
                settings.keyIdleColor, color, settings.keyboardBackgroundColor,
                settings.accentColor, settings.secondaryColor));
        keyboardBackgroundColorSpinner = colorSpinner(color -> settings = settings.withThemeColors(
                settings.keyIdleColor, settings.keyPressedColor, color,
                settings.accentColor, settings.secondaryColor));
        accentColorSpinner = colorSpinner(color -> settings = settings.withThemeColors(
                settings.keyIdleColor, settings.keyPressedColor, settings.keyboardBackgroundColor,
                color, settings.secondaryColor));
        secondaryColorSpinner = colorSpinner(color -> settings = settings.withThemeColors(
                settings.keyIdleColor, settings.keyPressedColor, settings.keyboardBackgroundColor,
                settings.accentColor, color));
        accentKeyColorSpinner = colorSpinner(color -> settings = settings.withExtendedThemeColors(
                settings.keyIdleColor, settings.keyPressedColor, settings.keyboardBackgroundColor,
                settings.accentColor, settings.secondaryColor, settings.functionKeyColor,
                color, settings.borderColor, settings.customDepthColorEnabled, settings.depthColor));
        borderColorSpinner = colorSpinner(color -> settings = settings.withExtendedThemeColors(
                settings.keyIdleColor, settings.keyPressedColor, settings.keyboardBackgroundColor,
                settings.accentColor, settings.secondaryColor, settings.functionKeyColor,
                settings.accentKeyColor, color, settings.customDepthColorEnabled, settings.depthColor));
        customDepthColorCheckBox = new CheckBox(this);
        customDepthColorCheckBox.setText("\uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1 \uC9C1\uC811 \uC9C0\uC815");
        customDepthColorCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withDepthColor(isChecked, settings.depthColor);
                saveAndSync();
            }
        });
        depthColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withDepthColor(true, color);
            saveAndSync();
        });
        roundnessValue = label("");
        roundnessSeekBar = seekBar(KeyboardSettings.MAX_KEY_ROUNDNESS_DP);
        keyBorderWidthValue = label("");
        keyBorderWidthSeekBar = seekBar(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP);
        keyGapValue = label("");
        keyGapSeekBar = seekBar(KeyboardSettings.MAX_KEY_GAP_DP);
        keyDepthCheckBox = new CheckBox(this);
        keyDepthCheckBox.setText("\uC785\uCCB4 depth \uD6A8\uACFC");
        keyDepthCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withKeyDepth(isChecked, settings.keyDepthDp);
                saveAndSync();
            }
        });
        keyDepthValue = label("");
        keyDepthSeekBar = seekBar(KeyboardSettings.MAX_KEY_DEPTH_DP);
    }

    private void addInputFeelControls(LinearLayout root) {
        hapticCheckBox = new CheckBox(this);
        hapticCheckBox.setText("\uD589\uD2F1 \uD53C\uB4DC\uBC31");
        hapticCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withHapticFeedback(isChecked);
                saveAndSync();
            }
        });
        root.addView(hapticCheckBox, matchWrapWithTop(8));

        differentiatedHapticCheckBox = new CheckBox(this);
        differentiatedHapticCheckBox.setText("\uC785\uB825 \uC885\uB958\uBCC4 \uD589\uD2F1 \uAD6C\uBD84");
        differentiatedHapticCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveDifferentiatedHapticEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(differentiatedHapticCheckBox, matchWrapWithTop(4));

        hapticDurationValue = label("");
        hapticDurationSeekBar = seekBar(
                KeyboardPreferences.MAX_HAPTIC_TICK_DURATION_MS
                        - KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS);
        hapticDurationSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    KeyboardPreferences.saveHapticTickDurationMs(
                            MainActivity.this,
                            KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS + progress);
                    syncControls();
                }
            }
        });
        root.addView(hapticDurationValue, matchWrapWithTop(12));
        root.addView(hapticDurationSeekBar, matchWrap());

        hapticGapValue = label("");
        hapticGapSeekBar = seekBar(
                KeyboardPreferences.MAX_HAPTIC_TICK_GAP_MS - KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS);
        hapticGapSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    KeyboardPreferences.saveHapticTickGapMs(
                            MainActivity.this,
                            KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS + progress);
                    syncControls();
                }
            }
        });
        root.addView(hapticGapValue, matchWrapWithTop(12));
        root.addView(hapticGapSeekBar, matchWrap());

        gestureThresholdValue = label("");
        gestureThresholdSeekBar = seekBar(
                KeyboardSettings.MAX_GESTURE_THRESHOLD_DP - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
        gestureThresholdSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withGestureThreshold(
                            KeyboardSettings.MIN_GESTURE_THRESHOLD_DP + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(gestureThresholdValue, matchWrapWithTop(12));
        root.addView(gestureThresholdSeekBar, matchWrap());

        touchYOffsetValue = label("");
        touchYOffsetSeekBar = seekBar(
                KeyboardSettings.MAX_TOUCH_Y_OFFSET_DP - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP);
        touchYOffsetSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withTouchYOffset(
                            KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(touchYOffsetValue, matchWrapWithTop(12));
        root.addView(touchYOffsetSeekBar, matchWrap());

        repeatStartDelayValue = label("");
        repeatStartDelaySeekBar = seekBar(
                KeyboardSettings.MAX_REPEAT_START_DELAY_MS - KeyboardSettings.MIN_REPEAT_START_DELAY_MS);
        repeatStartDelaySeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withRepeatTiming(
                            KeyboardSettings.MIN_REPEAT_START_DELAY_MS + progress,
                            settings.repeatIntervalMs);
                    saveAndSync();
                }
            }
        });
        root.addView(repeatStartDelayValue, matchWrapWithTop(12));
        root.addView(repeatStartDelaySeekBar, matchWrap());

        repeatIntervalValue = label("");
        repeatIntervalSeekBar = seekBar(
                KeyboardSettings.MAX_REPEAT_INTERVAL_MS - KeyboardSettings.MIN_REPEAT_INTERVAL_MS);
        repeatIntervalSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withRepeatTiming(
                            settings.repeatStartDelayMs,
                            KeyboardSettings.MIN_REPEAT_INTERVAL_MS + progress);
                    saveAndSync();
                }
            }
        });
        root.addView(repeatIntervalValue, matchWrapWithTop(8));
        root.addView(repeatIntervalSeekBar, matchWrap());

        touchBiasAutoCorrectionCheckBox = new CheckBox(this);
        touchBiasAutoCorrectionCheckBox.setText("\uD130\uCE58/\uC2AC\uB77C\uC774\uB4DC \uBCF4\uC815 \uD559\uC2B5");
        touchBiasAutoCorrectionCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveTouchBiasAutoCorrectionEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(touchBiasAutoCorrectionCheckBox, matchWrapWithTop(8));

        clipboardHistoryCheckBox = new CheckBox(this);
        clipboardHistoryCheckBox.setText("\uD074\uB9BD\uBCF4\uB4DC \uAE30\uB85D");
        clipboardHistoryCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveClipboardHistoryEnabled(MainActivity.this, isChecked);
                if (!isChecked) {
                    new ClipboardStore(MainActivity.this).clear();
                }
                syncControls();
            }
        });
        root.addView(clipboardHistoryCheckBox, matchWrapWithTop(8));

        doubleSpacePeriodCheckBox = new CheckBox(this);
        doubleSpacePeriodCheckBox.setText("\uC601\uC5B4\uC5D0\uC11C \uC2A4\uD398\uC774\uC2A4 2\uBC88\uC73C\uB85C \uB9C8\uCE68\uD45C");
        doubleSpacePeriodCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withEnglishDoubleSpacePeriod(isChecked);
                saveAndSync();
            }
        });
        root.addView(doubleSpacePeriodCheckBox, matchWrapWithTop(8));

        Button resetTouchBiasButton = new Button(this);
        resetTouchBiasButton.setText("\uC785\uB825 \uBCF4\uC815 \uCD08\uAE30\uD654");
        styleSystemButton(resetTouchBiasButton);
        setButtonIcon(resetTouchBiasButton, R.drawable.ic_keyboard_reset);
        resetTouchBiasButton.setOnClickListener(v -> TouchBiasStore.reset(this));
        root.addView(resetTouchBiasButton, buttonParams());
    }
    private void addReservedPhraseControls(LinearLayout root) {
        addReservedPhraseField(root, "\uD0ED", GestureAction.TAP, 8);
        addReservedPhraseField(root, "\uC67C\uCABD \uC2AC\uB77C\uC774\uB4DC", GestureAction.LEFT, 8);
        addReservedPhraseField(root, "\uC624\uB978\uCABD \uC2AC\uB77C\uC774\uB4DC", GestureAction.RIGHT, 8);
        addReservedPhraseField(root, "\uC704 \uC2AC\uB77C\uC774\uB4DC", GestureAction.UP, 8);
    }

    private void addReservedPhraseField(
            LinearLayout root,
            String labelText,
            GestureAction action,
            int topMarginDp) {
        root.addView(label(labelText), matchWrapWithTop(topMarginDp));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("\uBE44\uC6B0\uBA74 \uC785\uB825\uD558\uC9C0 \uC54A\uC74C");
        SettingsViewStyler.editText(input, this);
        input.setText(KeyboardPreferences.loadReservedPhrase(this, action));
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                KeyboardPreferences.saveReservedPhrase(
                        MainActivity.this,
                        action,
                        s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        root.addView(input, matchWrap());
    }

    private void addAndroidImeControls(LinearLayout root) {
        Button settingsButton = new Button(this);
        settingsButton.setText(R.string.open_input_settings);
        styleSystemButton(settingsButton);
        setButtonIcon(settingsButton, R.drawable.ic_keyboard_settings);
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(settingsButton, buttonParams());

        Button pickerButton = new Button(this);
        pickerButton.setText(R.string.show_input_picker);
        styleSystemButton(pickerButton);
        setButtonIcon(pickerButton, R.drawable.ic_keyboard_keyboard);
        pickerButton.setOnClickListener(v -> {
            InputMethodManager imm = getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });
        root.addView(pickerButton, buttonParams());
    }

    private void saveAndSync() {
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void syncControls() {
        if (handednessSpinner == null) {
            return;
        }

        syncing = true;
        styleCheckBox(customDepthColorCheckBox);
        styleCheckBox(followThemeTypographyCheckBox);
        styleCheckBox(primaryTextBoldCheckBox);
        styleCheckBox(primaryTextItalicCheckBox);
        styleCheckBox(secondaryTextBoldCheckBox);
        styleCheckBox(secondaryTextItalicCheckBox);
        styleCheckBox(pointKeycapStyleCheckBox);
        styleCheckBox(hangulNumberRowCheckBox);
        styleCheckBox(englishNumberRowCheckBox);
        styleCheckBox(hapticCheckBox);
        styleCheckBox(differentiatedHapticCheckBox);
        styleCheckBox(touchBiasAutoCorrectionCheckBox);
        styleCheckBox(clipboardHistoryCheckBox);
        styleCheckBox(doubleSpacePeriodCheckBox);
        styleCheckBox(keyDepthCheckBox);
        styleCheckBox(hangulConsonantSlideHintsCheckBox);
        styleCheckBox(hangulVowelSlideHintsCheckBox);
        styleCheckBox(englishSlideHintsCheckBox);
        styleCheckBox(spacebarSlideHintsCheckBox);
        styleCheckBox(beginnerTooltipPreviewCheckBox);
        if (themeOptions.length == 0) {
            reloadThemeOptions();
        }
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            selectedThemePresetIndex = 0;
        }
        if (themePresetSpinner != null) {
            themePresetSpinner.setSelection(selectedThemePresetIndex);
        }
        rebuildThemePresetCards();
        handednessSpinner.setSelection(settings.handednessMode.ordinal());
        if (leftMarginSeekBar != null) {
            leftMarginSeekBar.setProgress(settings.leftMarginDp);
        }
        if (rightMarginSeekBar != null) {
            rightMarginSeekBar.setProgress(settings.rightMarginDp);
        }
        hangulHeightSeekBar.setProgress(settings.hangulKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        englishHeightSeekBar.setProgress(settings.englishKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        hangulSpecialColumnSeekBar.setProgress(
                settings.hangulSpecialColumnPercent - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        if (keyboardTopPaddingSeekBar != null) {
            keyboardTopPaddingSeekBar.setProgress(settings.keyboardTopPaddingDp);
        }
        if (keyboardBottomPaddingSeekBar != null) {
            keyboardBottomPaddingSeekBar.setProgress(settings.keyboardBottomPaddingDp);
        }
        setNumericText(leftMarginInput, settings.leftMarginDp);
        setNumericText(rightMarginInput, settings.rightMarginDp);
        setNumericText(keyboardTopPaddingInput, settings.keyboardTopPaddingDp);
        setNumericText(keyboardBottomPaddingInput, settings.keyboardBottomPaddingDp);
        setNumericText(numberRowBottomGapInput, settings.numberRowBottomGapDp);
        roundnessSeekBar.setProgress(settings.keyRoundnessDp);
        keyBorderWidthSeekBar.setProgress(settings.keyBorderWidthDp);
        keyGapSeekBar.setProgress(settings.keyGapDp);
        keyDepthSeekBar.setProgress(settings.keyDepthDp);
        gestureThresholdSeekBar.setProgress(
                settings.gestureThresholdDp - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
        touchYOffsetSeekBar.setProgress(
                settings.touchYOffsetDp - KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP);
        repeatStartDelaySeekBar.setProgress(
                settings.repeatStartDelayMs - KeyboardSettings.MIN_REPEAT_START_DELAY_MS);
        repeatIntervalSeekBar.setProgress(
                settings.repeatIntervalMs - KeyboardSettings.MIN_REPEAT_INTERVAL_MS);
        int hapticDurationMs = KeyboardPreferences.loadHapticTickDurationMs(this);
        int hapticGapMs = KeyboardPreferences.loadHapticTickGapMs(this);
        hapticDurationSeekBar.setProgress(hapticDurationMs - KeyboardPreferences.MIN_HAPTIC_TICK_DURATION_MS);
        hapticGapSeekBar.setProgress(hapticGapMs - KeyboardPreferences.MIN_HAPTIC_TICK_GAP_MS);
        keyIdleColorSpinner.setSelection(indexOfColor(settings.keyIdleColor));
        keyPressedColorSpinner.setSelection(indexOfColor(settings.keyPressedColor));
        keyboardBackgroundColorSpinner.setSelection(indexOfColor(settings.keyboardBackgroundColor));
        accentColorSpinner.setSelection(indexOfColor(settings.accentColor));
        secondaryColorSpinner.setSelection(indexOfColor(settings.secondaryColor));
        accentKeyColorSpinner.setSelection(indexOfColor(settings.accentKeyColor));
        borderColorSpinner.setSelection(indexOfColor(settings.borderColor));
        depthColorSpinner.setSelection(indexOfColor(settings.depthColor));
        fontFamilySpinner.setSelection(indexOfFont(settings.fontFamily));
        followThemeTypographyCheckBox.setChecked(settings.followThemeTypography);
        modifierIconPackSpinner.setSelection(indexOfModifierIconPack(settings.modifierIconOverridePackId, true));
        keyDisplayPackSpinner.setSelection(indexOfKeyDisplayPack(settings.keyDisplayOverridePackId, true));
        if (additionalNumberRowColorModeSpinner != null) {
            additionalNumberRowColorModeSpinner.setSelection(settings.additionalNumberRowColorMode.ordinal());
        }
        primaryTextSizeSeekBar.setProgress(
                settings.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        secondaryTextSizeSeekBar.setProgress(
                settings.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        primaryTextBoldCheckBox.setChecked(settings.primaryTextBold);
        primaryTextItalicCheckBox.setChecked(settings.primaryTextItalic);
        secondaryTextBoldCheckBox.setChecked(settings.secondaryTextBold);
        secondaryTextItalicCheckBox.setChecked(settings.secondaryTextItalic);
        boolean typographyControlsEnabled = !settings.followThemeTypography;
        fontFamilySpinner.setEnabled(typographyControlsEnabled);
        primaryTextSizeSeekBar.setEnabled(typographyControlsEnabled);
        secondaryTextSizeSeekBar.setEnabled(typographyControlsEnabled);
        primaryTextBoldCheckBox.setEnabled(typographyControlsEnabled);
        primaryTextItalicCheckBox.setEnabled(typographyControlsEnabled);
        secondaryTextBoldCheckBox.setEnabled(typographyControlsEnabled);
        secondaryTextItalicCheckBox.setEnabled(typographyControlsEnabled);
        pointKeycapStyleCheckBox.setChecked(settings.pointKeycapStyleEnabled);
        hangulNumberRowCheckBox.setChecked(settings.showHangulNumberRow);
        englishNumberRowCheckBox.setChecked(settings.showEnglishNumberRow);
        hapticCheckBox.setChecked(settings.hapticFeedbackEnabled);
        differentiatedHapticCheckBox.setChecked(KeyboardPreferences.loadDifferentiatedHapticEnabled(this));
        touchBiasAutoCorrectionCheckBox.setChecked(
                KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(this));
        clipboardHistoryCheckBox.setChecked(KeyboardPreferences.loadClipboardHistoryEnabled(this));
        hapticDurationSeekBar.setEnabled(settings.hapticFeedbackEnabled);
        hapticGapSeekBar.setEnabled(settings.hapticFeedbackEnabled);
        differentiatedHapticCheckBox.setEnabled(settings.hapticFeedbackEnabled);
        doubleSpacePeriodCheckBox.setChecked(settings.englishDoubleSpacePeriodEnabled);
        keyDepthCheckBox.setChecked(settings.keyDepthEnabled);
        customDepthColorCheckBox.setChecked(settings.customDepthColorEnabled);
        hangulConsonantSlideHintsCheckBox.setChecked(
                KeyboardPreferences.loadShowHangulConsonantSlideHints(this));
        hangulVowelSlideHintsCheckBox.setChecked(
                KeyboardPreferences.loadShowHangulVowelSlideHints(this));
        englishSlideHintsCheckBox.setChecked(settings.showEnglishSlideHints);
        spacebarSlideHintsCheckBox.setChecked(KeyboardPreferences.loadShowSpacebarSlideHints(this));
        beginnerTooltipPreviewCheckBox.setChecked(settings.showBeginnerTooltipPreview);
        keyDepthSeekBar.setEnabled(settings.keyDepthEnabled);
        depthColorSpinner.setEnabled(settings.customDepthColorEnabled);
        deleteThemeButton.setEnabled(selectedThemeOption() != null && selectedThemeOption().userThemeId != null);
        leftMarginValue.setText("\uC88C\uC6B0 \uD328\uB529: " + settings.leftMarginDp + "dp");
        rightMarginValue.setText("\uC88C\uC6B0 \uD328\uB529: " + settings.rightMarginDp + "dp");
        hangulHeightValue.setText("\uB529\uAD74 \uB192\uC774: " + settings.hangulKeyboardHeightDp + "dp"
                + (settings.keyboardMode == KeyboardMode.HANGUL && settings.showNumberRow ? " + num row" : ""));
        englishHeightValue.setText("\uCFFC\uD2F0 \uB192\uC774: " + settings.englishKeyboardHeightDp + "dp"
                + (settings.keyboardMode == KeyboardMode.ENGLISH && settings.showNumberRow ? " + num row" : ""));
        hangulSpecialColumnValue.setText("\uB529\uAD74 \uC6B0\uCE21 \uD2B9\uC218\uC5F4 \uBE44\uC728: "
                + settings.hangulSpecialColumnPercent + "%");
        keyboardTopPaddingValue.setText("\uD0A4\uBCF4\uB4DC \uC0C1\uB2E8 \uD328\uB529: " + settings.keyboardTopPaddingDp + "dp");
        keyboardBottomPaddingValue.setText("\uD0A4\uBCF4\uB4DC \uD558\uB2E8 \uD328\uB529: " + settings.keyboardBottomPaddingDp + "dp");
        numberRowBottomGapValue.setText("\uC22B\uC790\uC904-\uC790\uD310 \uAC04\uACA9: " + settings.numberRowBottomGapDp + "dp");
        roundnessValue.setText("\uBC84\uD2BC roundness: " + settings.keyRoundnessDp + "dp");
        keyBorderWidthValue.setText("\uD14C\uB450\uB9AC \uAD75\uAE30: " + settings.keyBorderWidthDp + "dp");
        keyGapValue.setText("\uBC84\uD2BC \uAC04 \uC2DC\uAC01 \uAC04\uACA9: " + settings.keyGapDp + "dp");
        keyDepthValue.setText("\uC785\uCCB4 \uB192\uC774: " + settings.keyDepthDp + "dp"
                + (settings.keyDepthEnabled ? "" : " (flat)"));
        gestureThresholdValue.setText("\uC2AC\uB77C\uC774\uB4DC \uC2DC\uC791 \uAC70\uB9AC: " + settings.gestureThresholdDp
                + "dp (\uB0AE\uC744\uC218\uB85D \uBBFC\uAC10)");
        hapticDurationValue.setText("\uD589\uD2F1 \uAE38\uC774: " + hapticDurationMs + "ms");
        hapticGapValue.setText("\uD589\uD2F1 \uC0AC\uC774 \uAC04\uACA9: " + hapticGapMs + "ms");
        primaryTextSizeValue.setText("\uBA54\uC778 \uAE00\uC790 \uD06C\uAE30: " + settings.primaryTextSizePercent + "%");
        secondaryTextSizeValue.setText("\uBCF4\uC870 \uD78C\uD2B8 \uD06C\uAE30: " + settings.secondaryTextSizePercent + "%");
        touchYOffsetValue.setText("\uD130\uCE58 Y \uBCF4\uC815: " + settings.touchYOffsetDp
                + "dp (\uC591\uC218=\uC544\uB798\uCABD \uD0A4\uB85C \uBCF4\uC815)");
        repeatStartDelayValue.setText("\uBC18\uBCF5 \uC2DC\uC791 \uC9C0\uC5F0: "
                + settings.repeatStartDelayMs + "ms");
        repeatIntervalValue.setText("\uBC18\uBCF5 \uAC04\uACA9: " + settings.repeatIntervalMs + "ms");
        handednessSpinner.post(() -> syncing = false);
    }

    private void addSectionTitle(LinearLayout root, String text) {
        TextView title = label(text);
        title.setTextSize(17);
        title.setGravity(Gravity.START);
        root.addView(title, matchWrapWithTop(24));
    }

    private LinearLayout addExpandableSection(LinearLayout root, String text, boolean expandedByDefault) {
        SettingsSectionCard card = SettingsSectionCard.create(this, text, expandedByDefault);
        root.addView(card.container, matchWrapWithTop(12));
        return card.content;
    }
    private void addBodyText(LinearLayout root, String text, int topMarginDp) {
        TextView body = label(text);
        body.setLineSpacing(dp(2), 1.0f);
        body.setGravity(Gravity.START);
        root.addView(body, matchWrapWithTop(topMarginDp));
    }

    private TextView label(String text) {
        return SettingsRowBuilder.label(this, text);
    }

    private void styleSystemButton(Button button) {
        SettingsViewStyler.button(button, this, false);
    }

    private void styleCheckBox(CheckBox checkBox) {
        SettingsViewStyler.compoundButton(checkBox, this);
    }

    private SeekBar seekBar(int max) {
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        return seekBar;
    }

    private EditText numericInput(int initialValue, int maxValue, IntSettingListener listener) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        SettingsViewStyler.editText(input, this);
        input.setText(String.valueOf(initialValue));
        input.setSelectAllOnFocus(true);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyNumericInput(input, maxValue, listener);
                input.clearFocus();
                return true;
            }
            return false;
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                applyNumericInput(input, maxValue, listener);
            }
        });
        return input;
    }

    private EditText addNumericStepper(
            LinearLayout root,
            int initialValue,
            int maxValue,
            IntSettingListener listener) {
        NumericStepperRow row = new NumericStepperRow(
                this,
                initialValue,
                maxValue,
                value -> {
                    if (!syncing && listener != null) {
                        listener.onValue(value);
                    }
                });
        root.addView(row, matchWrap());
        return row.input();
    }
    private Button stepperButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        styleSystemButton(button);
        return button;
    }

    private void stepNumericInput(EditText input, int maxValue, int delta, IntSettingListener listener) {
        if (syncing || input == null || listener == null) {
            return;
        }
        int value;
        try {
            value = Integer.parseInt(input.getText().toString().trim());
        } catch (NumberFormatException ex) {
            value = 0;
        }
        value = Math.max(0, Math.min(maxValue, value + delta));
        input.setText(String.valueOf(value));
        listener.onValue(value);
    }

    private void applyNumericInput(EditText input, int maxValue, IntSettingListener listener) {
        if (syncing || input == null || listener == null) {
            return;
        }
        int value;
        try {
            value = Integer.parseInt(input.getText().toString().trim());
        } catch (NumberFormatException ex) {
            syncControls();
            return;
        }
        value = Math.max(0, Math.min(maxValue, value));
        listener.onValue(value);
    }

    private void setNumericText(EditText input, int value) {
        if (input == null) {
            return;
        }
        String text = String.valueOf(value);
        if (!text.contentEquals(input.getText())) {
            input.setText(text);
        }
    }

    private Spinner colorSpinner(final ColorChangeListener listener) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<ColorOption> adapter = new SettingsArrayAdapter<>(
                this,
                ColorOption.BASIC_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    listener.onColorChanged(ColorOption.BASIC_OPTIONS[position].color);
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
                    selectedThemePresetIndex = 0;
                    settings = settings.withAdditionalNumberRowColorMode(
                            AdditionalNumberRowColorMode.values()[position]);
                    saveAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner themePresetSpinner() {
        Spinner spinner = new Spinner(this);
        reloadThemeOptions();
        themePresetSpinner = spinner;
        refreshThemePresetAdapter();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) {
                    return;
                }
                applyThemeOption(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
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
        rebuildThemePresetCards();
    }

    private void reloadThemeOptions() {
        themeOptions = ThemeOption.buildOptions(UserThemeStore.load(this), true);
    }

    private void applyThemeOption(int position) {
        if (position <= 0 || position >= themeOptions.length) {
            selectedThemePresetIndex = 0;
            KeyboardPreferences.saveSelectedThemeId(this, "");
            syncControls();
            return;
        }
        selectedThemePresetIndex = position;
        settings = themeOptions[position].applyTo(settings);
        KeyboardPreferences.saveSelectedThemeId(this, themeOptions[position].stableId());
        saveAndSync();
    }

    private void rebuildThemePresetCards() {
        if (themePresetCards == null) {
            return;
        }
        reloadThemeOptions();
        themePresetCards.removeAllViews();
        for (int i = 0; i < themeOptions.length; i++) {
            View card = themePresetCard(i);
            LinearLayout.LayoutParams params = matchWrap();
            params.topMargin = dp(i == 0 ? 0 : 8);
            themePresetCards.addView(card, params);
        }
    }

    private View themePresetCard(int index) {
        ThemeOption option = themeOptions[index];
        AccentPlacementPolicy accentPolicy = KeyboardPreferences.loadAccentPlacementPolicy(this);
        KeyboardSettings englishSettings = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH,
                accentPolicy);
        KeyboardSettings hangulSettings = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.HANGUL,
                accentPolicy);
        boolean selected = index == selectedThemePresetIndex;
        SettingsUiPalette ui = SettingsUiPalette.from(this);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(16));
        card.setBackground(themeCardBackground(ui, selected));
        card.setOnClickListener(v -> applyThemeOption(index));

        TextView title = label(option.label);
        title.setTextColor(ui.textPrimary);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(title, matchWrap());

        card.addView(themePreviewLabel("QWERTY preview"), matchWrapWithTop(10));
        card.addView(themePreviewKeyboard(englishSettings), previewParams(88));
        card.addView(themePreviewLabel("Dingul preview"), matchWrapWithTop(12));
        card.addView(themePreviewKeyboard(hangulSettings), previewParams(108));
        return card;
    }

    private TextView themePreviewLabel(String text) {
        TextView label = label(text);
        label.setTextColor(SettingsUiPalette.from(this).textSecondary);
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        return label;
    }

    private HangulKeyboardView themePreviewKeyboard(KeyboardSettings previewSettings) {
        HangulKeyboardView preview = new HangulKeyboardView(this);
        preview.setCompactPreviewRendering(true);
        preview.setSettings(previewSettings);
        preview.setClickable(true);
        preview.setFocusable(false);
        preview.setOnTouchListener((v, event) -> true);
        preview.setAlpha(1f);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }

    private LinearLayout.LayoutParams previewParams(int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp));
        params.topMargin = dp(6);
        return params;
    }

    private GradientDrawable themeCardBackground(SettingsUiPalette ui, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(dp(18));
        background.setStroke(
                dp(selected ? 2 : 1),
                selected ? ui.selectedBorder : ui.border);
        return background;
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

    private Spinner fontSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<FontOption> adapter = new SettingsArrayAdapter<>(
                this,
                FontOption.BASIC_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withFontFamily(FontOption.BASIC_OPTIONS[position].value);
                    saveAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner modifierIconPackSpinner(boolean includeThemeDefault) {
        Spinner spinner = new Spinner(this);
        String[] ids = ModifierIconCatalog.selectablePackIds(includeThemeDefault);
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            labels[i] = ids[i].isEmpty() ? "\uD14C\uB9C8 \uAE30\uBCF8\uAC12" : ModifierIconCatalog.displayName(ids[i]);
        }
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(this, labels);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    settings = settings.withModifierIconOverridePack(ids[position]);
                    saveAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner keyDisplayPackSpinner(boolean includeThemeDefault) {
        Spinner spinner = new Spinner(this);
        String[] ids = KeyDisplayOverridePackCatalog.selectablePackIds(includeThemeDefault);
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            labels[i] = ids[i].isEmpty()
                    ? "\uD14C\uB9C8 \uAE30\uBCF8\uAC12"
                    : KeyDisplayOverridePackCatalog.displayName(ids[i]);
        }
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(this, labels);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    settings = settings.withKeyDisplayOverridePack(ids[position]);
                    saveAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private int indexOfColor(int color) {
        int opaqueColor = 0xFF000000 | (color & 0x00FFFFFF);
        for (int i = 0; i < ColorOption.BASIC_OPTIONS.length; i++) {
            if (ColorOption.BASIC_OPTIONS[i].color == opaqueColor) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfFont(String fontFamily) {
        String normalized = KeyboardSettings.normalizeFontFamily(fontFamily);
        for (int i = 0; i < FontOption.BASIC_OPTIONS.length; i++) {
            if (FontOption.BASIC_OPTIONS[i].value.equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfModifierIconPack(String packId, boolean includeThemeDefault) {
        String[] ids = ModifierIconCatalog.selectablePackIds(includeThemeDefault);
        String normalized = packId == null || packId.isEmpty()
                ? ModifierIconCatalog.PACK_THEME_DEFAULT
                : ModifierIconCatalog.normalizePackId(packId);
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfKeyDisplayPack(String packId, boolean includeThemeDefault) {
        String normalized = packId == null || packId.isEmpty()
                ? KeyDisplayOverridePackCatalog.PACK_THEME_DEFAULT
                : KeyDisplayOverridePackCatalog.normalizePackId(packId);
        String[] ids = KeyDisplayOverridePackCatalog.selectablePackIds(includeThemeDefault);
        for (int i = 0; i < ids.length; i++) {
            String candidate = ids[i].isEmpty()
                    ? KeyDisplayOverridePackCatalog.PACK_THEME_DEFAULT
                    : KeyDisplayOverridePackCatalog.normalizePackId(ids[i]);
            if (candidate.equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(12);
        return params;
    }

    private LinearLayout.LayoutParams stepperButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp(52),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = dp(3);
        params.rightMargin = dp(3);
        return params;
    }

    private LinearLayout.LayoutParams stepperInputParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        params.leftMargin = dp(3);
        params.rightMargin = dp(3);
        return params;
    }

    private void setButtonIcon(Button button, int drawableResId) {
        button.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
        button.setCompoundDrawablePadding(dp(8));
        int tint = SettingsUiPalette.from(this).controlText;
        for (Drawable drawable : button.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setTint(tint);
            }
        }
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

    private abstract class BooleanSettingListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public final void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!syncing) {
                onUserChanged(isChecked);
            }
        }

        protected abstract void onUserChanged(boolean isChecked);
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private interface ColorChangeListener {
        void onColorChanged(int color);
    }

    private interface IntSettingListener {
        void onValue(int value);
    }

}
