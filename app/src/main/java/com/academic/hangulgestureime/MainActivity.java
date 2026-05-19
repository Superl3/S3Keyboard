package com.academic.hangulgestureime;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
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
    private static final String EXTRA_PRIMARY_FUNCTION_KEY_COLOR = "primary_function_key_color";
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
    private SeekBar hapticDurationSeekBar;
    private SeekBar hapticGapSeekBar;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private Spinner themePresetSpinner;
    private Spinner keyIdleColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner functionKeyColorSpinner;
    private Spinner primaryFunctionKeyColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner fontFamilySpinner;
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
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private CheckBox hangulSlideHintsCheckBox;
    private CheckBox englishSlideHintsCheckBox;
    private CheckBox beginnerTooltipPreviewCheckBox;
    private CheckBox consonantPreviewCheckBox;
    private CheckBox vowelPreviewCheckBox;
    private TextView leftMarginValue;
    private TextView rightMarginValue;
    private TextView hangulHeightValue;
    private TextView englishHeightValue;
    private TextView hangulSpecialColumnValue;
    private TextView keyboardTopPaddingValue;
    private TextView keyboardBottomPaddingValue;
    private EditText leftMarginInput;
    private EditText rightMarginInput;
    private EditText keyboardTopPaddingInput;
    private EditText keyboardBottomPaddingInput;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private TextView gestureThresholdValue;
    private TextView hapticDurationValue;
    private TextView hapticGapValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private LinearLayout themePresetCards;
    private ThemePresetOption[] themeOptions = new ThemePresetOption[0];
    private int selectedThemePresetIndex;

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
                        colorExtra(
                                intent,
                                EXTRA_PRIMARY_FUNCTION_KEY_COLOR,
                                settings.primaryFunctionKeyColor),
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
                || intent.hasExtra(EXTRA_PRIMARY_FUNCTION_KEY_COLOR)
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
                || intent.hasExtra(EXTRA_SHOW_ENGLISH_NUMBER_ROW);
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
        int padding = dp(24);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, padding, padding, padding);
        scrollView.addView(root);

        TextView title = new TextView(this);
        title.setText(getString(R.string.app_name));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = matchWrap();
        titleParams.bottomMargin = dp(8);
        root.addView(title, titleParams);

        addSectionTitle(root, getString(R.string.gesture_practice_title));
        addBodyText(root, getString(R.string.gesture_practice_body), 8);

        EditText testInput = new EditText(this);
        testInput.setHint(R.string.gesture_practice_hint);
        testInput.setSingleLine(false);
        testInput.setMinLines(2);
        testInput.setFocusableInTouchMode(true);
        root.addView(testInput, matchWrapWithTop(18));
        maybeShowDemoKeyboard(testInput);

        addThemeQuickControls(root);
        addVisualControls(root);

        LinearLayout layoutSection = addExpandableSection(
                root,
                getString(R.string.layout_settings_title),
                false);
        addLayoutControls(layoutSection);

        LinearLayout inputSection = addExpandableSection(
                root,
                getString(R.string.input_feel_title),
                false);
        addInputFeelControls(inputSection);

        LinearLayout reservedSection = addExpandableSection(
                root,
                "예약어 설정",
                false);
        addReservedPhraseControls(reservedSection);

        LinearLayout androidSection = addExpandableSection(
                root,
                getString(R.string.android_ime_title),
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[] {
                        HandednessMode.BALANCED.displayName,
                        HandednessMode.LEFT.displayName,
                        HandednessMode.RIGHT.displayName
                });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        root.addView(label("한손 모드"), matchWrapWithTop(12));
        root.addView(handednessSpinner, matchWrap());

        leftMarginValue = label("");
        leftMarginSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP);
        leftMarginSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withMargins(progress, settings.rightMarginDp);
                    saveAndSync();
                }
            }
        });
        root.addView(leftMarginValue, matchWrapWithTop(12));
        root.addView(leftMarginSeekBar, matchWrap());
        leftMarginInput = numericInput(settings.leftMarginDp, KeyboardSettings.MAX_MARGIN_DP, value -> {
            settings = settings.withMargins(value, settings.rightMarginDp);
            saveAndSync();
        });
        root.addView(leftMarginInput, matchWrap());

        rightMarginValue = label("");
        rightMarginSeekBar = seekBar(KeyboardSettings.MAX_MARGIN_DP);
        rightMarginSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withMargins(settings.leftMarginDp, progress);
                    saveAndSync();
                }
            }
        });
        root.addView(rightMarginValue, matchWrapWithTop(12));
        root.addView(rightMarginSeekBar, matchWrap());
        rightMarginInput = numericInput(settings.rightMarginDp, KeyboardSettings.MAX_MARGIN_DP, value -> {
            settings = settings.withMargins(settings.leftMarginDp, value);
            saveAndSync();
        });
        root.addView(rightMarginInput, matchWrap());

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
        root.addView(hangulHeightValue, matchWrapWithTop(12));
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

        keyboardTopPaddingValue = label("");
        keyboardTopPaddingSeekBar = seekBar(KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP);
        keyboardTopPaddingSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withLayoutSpacing(
                            settings.hangulMainSpecialGapDp,
                            progress,
                            settings.keyboardBottomPaddingDp,
                            settings.bottomRowTopPaddingDp);
                    saveAndSync();
                }
            }
        });
        root.addView(keyboardTopPaddingValue, matchWrapWithTop(12));
        root.addView(keyboardTopPaddingSeekBar, matchWrap());
        keyboardTopPaddingInput = numericInput(
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
        root.addView(keyboardTopPaddingInput, matchWrap());

        keyboardBottomPaddingValue = label("");
        keyboardBottomPaddingSeekBar = seekBar(KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP);
        keyboardBottomPaddingSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    settings = settings.withLayoutSpacing(
                            settings.hangulMainSpecialGapDp,
                            settings.keyboardTopPaddingDp,
                            progress,
                            settings.bottomRowTopPaddingDp);
                    saveAndSync();
                }
            }
        });
        root.addView(keyboardBottomPaddingValue, matchWrapWithTop(12));
        root.addView(keyboardBottomPaddingSeekBar, matchWrap());
        keyboardBottomPaddingInput = numericInput(
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
        root.addView(keyboardBottomPaddingInput, matchWrap());

        hangulNumberRowCheckBox = new CheckBox(this);
        hangulNumberRowCheckBox.setText("한글 상단 숫자줄 표시");
        hangulNumberRowCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withHangulNumberRow(isChecked);
                saveAndSync();
            }
        });
        root.addView(hangulNumberRowCheckBox, matchWrapWithTop(16));

        englishNumberRowCheckBox = new CheckBox(this);
        englishNumberRowCheckBox.setText("영문 상단 숫자줄 표시");
        englishNumberRowCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withEnglishNumberRow(isChecked);
                saveAndSync();
            }
        });
        root.addView(englishNumberRowCheckBox, matchWrapWithTop(8));

        additionalNumberRowColorModeSpinner = additionalNumberRowColorModeSpinner();
        root.addView(label("Additional number row color"), matchWrapWithTop(8));
        root.addView(additionalNumberRowColorModeSpinner, matchWrap());
    }

    private void addThemeQuickControls(LinearLayout root) {
        addSectionTitle(root, "Theme");
        Button openThemeSelectorButton = new Button(this);
        openThemeSelectorButton.setText("Open Theme Selector");
        setButtonIcon(openThemeSelectorButton, R.drawable.ic_keyboard_keyboard);
        openThemeSelectorButton.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSelectorActivity.class)));
        root.addView(openThemeSelectorButton, buttonParams());

    }

    private void addVisualControls(LinearLayout root) {
        LinearLayout hiddenLegacyControls = new LinearLayout(this);
        hiddenLegacyControls.setOrientation(LinearLayout.VERTICAL);
        root = hiddenLegacyControls;

        Button saveThemeButton = new Button(this);
        saveThemeButton.setText("Save current theme");
        setButtonIcon(saveThemeButton, R.drawable.ic_keyboard_settings);
        saveThemeButton.setOnClickListener(v -> {
            UserThemeStore.UserTheme saved = UserThemeStore.saveCurrent(this, settings);
            selectedThemePresetIndex = indexOfUserTheme(saved.id);
            refreshThemePresetAdapter();
            syncControls();
        });
        root.addView(saveThemeButton, buttonParams());

        deleteThemeButton = new Button(this);
        deleteThemeButton.setText("Delete selected custom theme");
        setButtonIcon(deleteThemeButton, R.drawable.ic_keyboard_backspace);
        deleteThemeButton.setOnClickListener(v -> {
            ThemePresetOption option = selectedThemeOption();
            if (option == null || option.userThemeId == null) {
                return;
            }
            UserThemeStore.delete(this, option.userThemeId);
            selectedThemePresetIndex = 0;
            refreshThemePresetAdapter();
            syncControls();
        });
        root.addView(deleteThemeButton, buttonParams());

        keyIdleColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withThemeColors(
                    color,
                    settings.keyPressedColor,
                    settings.keyboardBackgroundColor,
                    settings.accentColor,
                    settings.secondaryColor);
            saveAndSync();
        });
        root.addView(label("문자/기호 키 색상"), matchWrapWithTop(12));
        root.addView(keyIdleColorSpinner, matchWrap());

        accentKeyColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withExtendedThemeColors(
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
                    settings.depthColor);
            saveAndSync();
        });
        root.addView(label("딩굴 특수열 accent 키 색상 (,; / @/)"), matchWrapWithTop(12));
        root.addView(accentKeyColorSpinner, matchWrap());

        keyPressedColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withThemeColors(
                    settings.keyIdleColor,
                    color,
                    settings.keyboardBackgroundColor,
                    settings.accentColor,
                    settings.secondaryColor);
            saveAndSync();
        });
        root.addView(label("눌림 색상"), matchWrapWithTop(12));
        root.addView(keyPressedColorSpinner, matchWrap());

        keyboardBackgroundColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withThemeColors(
                    settings.keyIdleColor,
                    settings.keyPressedColor,
                    color,
                    settings.accentColor,
                    settings.secondaryColor);
            saveAndSync();
        });
        root.addView(label("버튼 바깥 색상"), matchWrapWithTop(12));
        root.addView(keyboardBackgroundColorSpinner, matchWrap());

        accentColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withThemeColors(
                    settings.keyIdleColor,
                    settings.keyPressedColor,
                    settings.keyboardBackgroundColor,
                    color,
                    settings.secondaryColor);
            saveAndSync();
        });
        root.addView(label("Accent 색상"), matchWrapWithTop(12));
        root.addView(accentColorSpinner, matchWrap());

        secondaryColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withThemeColors(
                    settings.keyIdleColor,
                    settings.keyPressedColor,
                    settings.keyboardBackgroundColor,
                    settings.accentColor,
                    color);
            saveAndSync();
        });
        root.addView(label("Secondary 색상"), matchWrapWithTop(12));
        root.addView(secondaryColorSpinner, matchWrap());

        borderColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withExtendedThemeColors(
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
                    settings.depthColor);
            saveAndSync();
        });
        root.addView(label("테두리 색상"), matchWrapWithTop(12));
        root.addView(borderColorSpinner, matchWrap());

        customDepthColorCheckBox = new CheckBox(this);
        customDepthColorCheckBox.setText("Depth 색상 직접 지정");
        customDepthColorCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withDepthColor(isChecked, settings.depthColor);
                saveAndSync();
            }
        });
        root.addView(customDepthColorCheckBox, matchWrapWithTop(16));

        depthColorSpinner = colorSpinner(color -> {
            selectedThemePresetIndex = 0;
            settings = settings.withDepthColor(true, color);
            saveAndSync();
        });
        root.addView(label("Depth 색상"), matchWrapWithTop(8));
        root.addView(depthColorSpinner, matchWrap());

        fontFamilySpinner = fontSpinner();
        root.addView(label("키보드 폰트"), matchWrapWithTop(12));
        root.addView(fontFamilySpinner, matchWrap());

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
        primaryTextBoldCheckBox.setText("Primary legend bold");
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
        primaryTextItalicCheckBox.setText("Primary legend italic");
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
        secondaryTextBoldCheckBox.setText("Secondary hint bold");
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
        secondaryTextItalicCheckBox.setText("Secondary hint italic");
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

        roundnessValue = label("");
        roundnessSeekBar = seekBar(KeyboardSettings.MAX_KEY_ROUNDNESS_DP);
        roundnessSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withKeyRoundness(progress);
                    saveAndSync();
                }
            }
        });
        root.addView(roundnessValue, matchWrapWithTop(12));
        root.addView(roundnessSeekBar, matchWrap());

        keyBorderWidthValue = label("");
        keyBorderWidthSeekBar = seekBar(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP);
        keyBorderWidthSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withKeyBorderWidth(progress);
                    saveAndSync();
                }
            }
        });
        root.addView(keyBorderWidthValue, matchWrapWithTop(12));
        root.addView(keyBorderWidthSeekBar, matchWrap());

        keyGapValue = label("");
        keyGapSeekBar = seekBar(KeyboardSettings.MAX_KEY_GAP_DP);
        keyGapSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withKeyGap(progress);
                    saveAndSync();
                }
            }
        });
        root.addView(keyGapValue, matchWrapWithTop(12));
        root.addView(keyGapSeekBar, matchWrap());

        keyDepthCheckBox = new CheckBox(this);
        keyDepthCheckBox.setText("입체 depth 효과");
        keyDepthCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withKeyDepth(isChecked, settings.keyDepthDp);
                saveAndSync();
            }
        });
        root.addView(keyDepthCheckBox, matchWrapWithTop(16));

        keyDepthValue = label("");
        keyDepthSeekBar = seekBar(KeyboardSettings.MAX_KEY_DEPTH_DP);
        keyDepthSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withKeyDepth(settings.keyDepthEnabled, progress);
                    saveAndSync();
                }
            }
        });
        root.addView(keyDepthValue, matchWrapWithTop(8));
        root.addView(keyDepthSeekBar, matchWrap());
    }

    private void addInputFeelControls(LinearLayout root) {
        hapticCheckBox = new CheckBox(this);
        hapticCheckBox.setText("햅틱 피드백");
        hapticCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withHapticFeedback(isChecked);
                saveAndSync();
            }
        });
        root.addView(hapticCheckBox, matchWrapWithTop(8));

        differentiatedHapticCheckBox = new CheckBox(this);
        differentiatedHapticCheckBox.setText("Differentiated haptics");
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

        touchBiasAutoCorrectionCheckBox = new CheckBox(this);
        touchBiasAutoCorrectionCheckBox.setText("Learn touch/slide corrections");
        touchBiasAutoCorrectionCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveTouchBiasAutoCorrectionEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(touchBiasAutoCorrectionCheckBox, matchWrapWithTop(8));

        clipboardHistoryCheckBox = new CheckBox(this);
        clipboardHistoryCheckBox.setText("Clipboard history");
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
        doubleSpacePeriodCheckBox.setText("영어에서 스페이스 두 번으로 마침표");
        doubleSpacePeriodCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withEnglishDoubleSpacePeriod(isChecked);
                saveAndSync();
            }
        });
        root.addView(doubleSpacePeriodCheckBox, matchWrapWithTop(8));

        hangulSlideHintsCheckBox = new CheckBox(this);
        hangulSlideHintsCheckBox.setText("한글 슬라이드 힌트 표시");
        hangulSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                selectedThemePresetIndex = 0;
                settings = settings.withHintVisibility(
                        isChecked,
                        settings.showEnglishSlideHints,
                        settings.showBeginnerTooltipPreview);
                saveAndSync();
            }
        });
        root.addView(hangulSlideHintsCheckBox, matchWrapWithTop(12));

        englishSlideHintsCheckBox = new CheckBox(this);
        englishSlideHintsCheckBox.setText("영문 슬라이드 힌트 표시");
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

        beginnerTooltipPreviewCheckBox = new CheckBox(this);
        beginnerTooltipPreviewCheckBox.setText("초심자 입력 preview tooltip 표시");
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

        consonantPreviewCheckBox = new CheckBox(this);
        consonantPreviewCheckBox.setText("자음 입력 preview 표시");
        consonantPreviewCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowConsonantPreview(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(consonantPreviewCheckBox, matchWrapWithTop(8));

        vowelPreviewCheckBox = new CheckBox(this);
        vowelPreviewCheckBox.setText("모음 입력 preview 표시");
        vowelPreviewCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowVowelPreview(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(vowelPreviewCheckBox, matchWrapWithTop(4));

        Button resetTouchBiasButton = new Button(this);
        resetTouchBiasButton.setText("입력 보정 초기화");
        setButtonIcon(resetTouchBiasButton, R.drawable.ic_keyboard_reset);
        resetTouchBiasButton.setOnClickListener(v -> TouchBiasStore.reset(this));
        root.addView(resetTouchBiasButton, buttonParams());
    }

    private void addReservedPhraseControls(LinearLayout root) {
        addReservedPhraseField(root, "탭", GestureAction.TAP, 8);
        addReservedPhraseField(root, "왼쪽 슬라이드", GestureAction.LEFT, 8);
        addReservedPhraseField(root, "오른쪽 슬라이드", GestureAction.RIGHT, 8);
        addReservedPhraseField(root, "위 슬라이드", GestureAction.UP, 8);
    }

    private void addReservedPhraseField(
            LinearLayout root,
            String labelText,
            GestureAction action,
            int topMarginDp) {
        root.addView(label(labelText), matchWrapWithTop(topMarginDp));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("비우면 입력하지 않음");
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
        setButtonIcon(settingsButton, R.drawable.ic_keyboard_settings);
        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(settingsButton, buttonParams());

        Button pickerButton = new Button(this);
        pickerButton.setText(R.string.show_input_picker);
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
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void syncControls() {
        if (handednessSpinner == null) {
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
        rebuildThemePresetCards();
        handednessSpinner.setSelection(settings.handednessMode.ordinal());
        leftMarginSeekBar.setProgress(settings.leftMarginDp);
        rightMarginSeekBar.setProgress(settings.rightMarginDp);
        hangulHeightSeekBar.setProgress(settings.hangulKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        englishHeightSeekBar.setProgress(settings.englishKeyboardHeightDp - KeyboardSettings.MIN_HEIGHT_DP);
        hangulSpecialColumnSeekBar.setProgress(
                settings.hangulSpecialColumnPercent - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        keyboardTopPaddingSeekBar.setProgress(settings.keyboardTopPaddingDp);
        keyboardBottomPaddingSeekBar.setProgress(settings.keyboardBottomPaddingDp);
        setNumericText(leftMarginInput, settings.leftMarginDp);
        setNumericText(rightMarginInput, settings.rightMarginDp);
        setNumericText(keyboardTopPaddingInput, settings.keyboardTopPaddingDp);
        setNumericText(keyboardBottomPaddingInput, settings.keyboardBottomPaddingDp);
        roundnessSeekBar.setProgress(settings.keyRoundnessDp);
        keyBorderWidthSeekBar.setProgress(settings.keyBorderWidthDp);
        keyGapSeekBar.setProgress(settings.keyGapDp);
        keyDepthSeekBar.setProgress(settings.keyDepthDp);
        gestureThresholdSeekBar.setProgress(
                settings.gestureThresholdDp - KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);
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
        additionalNumberRowColorModeSpinner.setSelection(settings.additionalNumberRowColorMode.ordinal());
        primaryTextSizeSeekBar.setProgress(
                settings.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        secondaryTextSizeSeekBar.setProgress(
                settings.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        primaryTextBoldCheckBox.setChecked(settings.primaryTextBold);
        primaryTextItalicCheckBox.setChecked(settings.primaryTextItalic);
        secondaryTextBoldCheckBox.setChecked(settings.secondaryTextBold);
        secondaryTextItalicCheckBox.setChecked(settings.secondaryTextItalic);
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
        hangulSlideHintsCheckBox.setChecked(settings.showHangulSlideHints);
        englishSlideHintsCheckBox.setChecked(settings.showEnglishSlideHints);
        beginnerTooltipPreviewCheckBox.setChecked(settings.showBeginnerTooltipPreview);
        consonantPreviewCheckBox.setChecked(KeyboardPreferences.loadShowConsonantPreview(this));
        vowelPreviewCheckBox.setChecked(KeyboardPreferences.loadShowVowelPreview(this));
        consonantPreviewCheckBox.setEnabled(settings.showBeginnerTooltipPreview);
        vowelPreviewCheckBox.setEnabled(settings.showBeginnerTooltipPreview);
        keyDepthSeekBar.setEnabled(settings.keyDepthEnabled);
        depthColorSpinner.setEnabled(settings.customDepthColorEnabled);
        deleteThemeButton.setEnabled(selectedThemeOption() != null && selectedThemeOption().userThemeId != null);
        leftMarginValue.setText("왼쪽 패딩: " + settings.leftMarginDp + "dp");
        rightMarginValue.setText("오른쪽 패딩: " + settings.rightMarginDp + "dp");
        hangulHeightValue.setText("Dingul height: " + settings.hangulKeyboardHeightDp + "dp"
                + (settings.keyboardMode == KeyboardMode.HANGUL && settings.showNumberRow ? " + num row" : ""));
        englishHeightValue.setText("QWERTY height: " + settings.englishKeyboardHeightDp + "dp"
                + (settings.keyboardMode == KeyboardMode.ENGLISH && settings.showNumberRow ? " + num row" : ""));
        hangulSpecialColumnValue.setText("한글 오른쪽 특수열 폭: "
                + settings.hangulSpecialColumnPercent + "%");
        keyboardTopPaddingValue.setText("Keyboard top padding: " + settings.keyboardTopPaddingDp + "dp");
        keyboardBottomPaddingValue.setText("Keyboard bottom padding: " + settings.keyboardBottomPaddingDp + "dp");
        roundnessValue.setText("버튼 roundness: " + settings.keyRoundnessDp + "dp");
        keyBorderWidthValue.setText("Outline density: " + settings.keyBorderWidthDp + "dp");
        keyGapValue.setText("버튼 간 시각 간격: " + settings.keyGapDp + "dp");
        keyDepthValue.setText("입체 높이: " + settings.keyDepthDp + "dp"
                + (settings.keyDepthEnabled ? "" : " (flat)"));
        gestureThresholdValue.setText("슬라이드 시작 거리: " + settings.gestureThresholdDp
                + "dp (낮을수록 민감)");
        hapticDurationValue.setText("햅틱 길이: " + hapticDurationMs + "ms");
        hapticGapValue.setText("햅틱 사이 간격: " + hapticGapMs + "ms");
        primaryTextSizeValue.setText("Primary legend size: " + settings.primaryTextSizePercent + "%");
        secondaryTextSizeValue.setText("Secondary hint size: " + settings.secondaryTextSizePercent + "%");
        handednessSpinner.post(() -> syncing = false);
    }

    private void addSectionTitle(LinearLayout root, String text) {
        TextView title = label(text);
        title.setTextSize(17);
        title.setGravity(Gravity.START);
        root.addView(title, matchWrapWithTop(24));
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

        root.addView(title, matchWrapWithTop(24));
        root.addView(content, matchWrap());
        return content;
    }

    private void setExpandableTitle(TextView title, String text, boolean expanded) {
        title.setText((expanded ? "▾ " : "▸ ") + text);
    }

    private void addBodyText(LinearLayout root, String text, int topMarginDp) {
        TextView body = label(text);
        body.setLineSpacing(dp(2), 1.0f);
        body.setGravity(Gravity.START);
        root.addView(body, matchWrapWithTop(topMarginDp));
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(14);
        return label;
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
        ArrayAdapter<ColorOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                ColorOption.OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    listener.onColorChanged(ColorOption.OPTIONS[position].color);
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
        ArrayAdapter<AdditionalNumberRowColorMode> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                AdditionalNumberRowColorMode.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        ArrayAdapter<ThemePresetOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                themeOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themePresetSpinner.setAdapter(adapter);
        syncing = wasSyncing;
        rebuildThemePresetCards();
    }

    private void reloadThemeOptions() {
        themeOptions = ThemePresetOption.buildOptions(UserThemeStore.load(this));
    }

    private void applyThemeOption(int position) {
        if (position <= 0 || position >= themeOptions.length) {
            selectedThemePresetIndex = 0;
            syncControls();
            return;
        }
        selectedThemePresetIndex = position;
        settings = themeOptions[position].applyTo(settings);
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
        ThemePresetOption option = themeOptions[index];
        KeyboardSettings titleSettings = option.applyTo(settings);
        KeyboardSettings englishSettings = previewSettingsFor(option, KeyboardMode.ENGLISH);
        KeyboardSettings hangulSettings = previewSettingsFor(option, KeyboardMode.HANGUL);
        boolean selected = index == selectedThemePresetIndex;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(16));
        card.setBackground(themeCardBackground(titleSettings, selected));
        card.setOnClickListener(v -> applyThemeOption(index));

        TextView title = label(option.label);
        title.setTextColor(0xFF111827);
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
        label.setTextColor(0xFF64748B);
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

    private KeyboardSettings previewSettingsFor(ThemePresetOption option, KeyboardMode mode) {
        return option.applyTo(settings)
                .withKeyboardMode(mode)
                .withHintVisibility(
                        false,
                        false,
                        false)
                .withHangulNumberRow(false)
                .withEnglishNumberRow(false);
    }

    private GradientDrawable themeCardBackground(KeyboardSettings cardSettings, boolean selected) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xFFFFFFFF);
        background.setCornerRadius(dp(18));
        background.setStroke(
                dp(selected ? 2 : 1),
                selected ? cardSettings.accentColor : 0xFFE5E7EB);
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

    private ThemePresetOption selectedThemeOption() {
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            return null;
        }
        return themeOptions[selectedThemePresetIndex];
    }

    private Spinner fontSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<FontOption> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                FontOption.OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    selectedThemePresetIndex = 0;
                    settings = settings.withFontFamily(FontOption.OPTIONS[position].value);
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
        for (int i = 0; i < ColorOption.OPTIONS.length; i++) {
            if (ColorOption.OPTIONS[i].color == opaqueColor) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfFont(String fontFamily) {
        String normalized = KeyboardSettings.normalizeFontFamily(fontFamily);
        for (int i = 0; i < FontOption.OPTIONS.length; i++) {
            if (FontOption.OPTIONS[i].value.equals(normalized)) {
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

    private void setButtonIcon(Button button, int drawableResId) {
        button.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
        button.setCompoundDrawablePadding(dp(8));
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

    private static final class ThemePresetOption {
        final String label;
        final KeyboardThemePreset preset;
        final String userThemeId;
        final String userThemeJson;

        private ThemePresetOption(
                String label,
                KeyboardThemePreset preset,
                String userThemeId,
                String userThemeJson) {
            this.label = label;
            this.preset = preset;
            this.userThemeId = userThemeId;
            this.userThemeJson = userThemeJson;
        }

        private static ThemePresetOption[] buildOptions(UserThemeStore.UserTheme[] userThemes) {
            int userCount = userThemes == null ? 0 : userThemes.length;
            ThemePresetOption[] options = new ThemePresetOption[
                    KeyboardThemePreset.PRESETS.length + userCount + 1];
            options[0] = new ThemePresetOption("Current custom", null, null, null);
            for (int i = 0; i < KeyboardThemePreset.PRESETS.length; i++) {
                KeyboardThemePreset preset = KeyboardThemePreset.PRESETS[i];
                options[i + 1] = new ThemePresetOption(preset.displayName, preset, null, null);
            }
            for (int i = 0; i < userCount; i++) {
                UserThemeStore.UserTheme theme = userThemes[i];
                options[KeyboardThemePreset.PRESETS.length + 1 + i] =
                        new ThemePresetOption(theme.name, null, theme.id, theme.json);
            }
            return options;
        }

        private KeyboardSettings applyTo(KeyboardSettings settings) {
            if (preset != null) {
                return preset.applyTo(settings);
            }
            if (userThemeJson != null && !userThemeJson.isEmpty()) {
                return KeyboardThemeJson.importTheme(settings, userThemeJson);
            }
            return settings;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class FontOption {
        static final FontOption[] OPTIONS = {
                new FontOption("기본", KeyboardSettings.FONT_DEFAULT),
                new FontOption("Noto Sans KR", KeyboardSettings.FONT_NOTO_SANS_KR),
                new FontOption("Noto Serif KR", KeyboardSettings.FONT_NOTO_SERIF_KR),
                new FontOption("D2Coding", KeyboardSettings.FONT_D2CODING)
        };

        final String label;
        final String value;

        FontOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class ColorOption {
        static final ColorOption[] OPTIONS = {
                new ColorOption("기본 버튼", KeyboardSettings.DEFAULT_KEY_IDLE_COLOR),
                new ColorOption("버튼 바깥", KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR),
                new ColorOption("눌림 회색", KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR),
                new ColorOption("보조 회색", KeyboardSettings.DEFAULT_SECONDARY_COLOR),
                new ColorOption("검정", KeyboardSettings.DEFAULT_ACCENT_COLOR),
                new ColorOption("흰색", 0xFFFFFFFF),
                new ColorOption("파랑", 0xFF3F6EDB),
                new ColorOption("초록", 0xFF2E7D57),
                new ColorOption("청록", 0xFF00897B),
                new ColorOption("보라", 0xFF6D5BD0),
                new ColorOption("코랄", 0xFFE76F51),
                new ColorOption("노랑", 0xFFE9C46A)
        };

        final String label;
        final int color;

        ColorOption(String label, int color) {
            this.label = label;
            this.color = 0xFF000000 | (color & 0x00FFFFFF);
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
