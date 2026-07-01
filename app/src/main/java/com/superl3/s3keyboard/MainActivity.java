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

import java.util.Objects;

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
    private static final String EXTRA_DEMO_FIELD_PROFILE = "demo_field_profile";
    private static final String EXTRA_THEME_PRESET_ID = "theme_preset_id";

    private KeyboardSettings settings;
    private KeyboardLayoutProfiles layoutProfiles;
    private KeyboardErgonomicsOptions ergonomicsOptions = KeyboardErgonomicsOptions.DEFAULT;
    private LocalDataControlsController localDataControlsController;
    private DebugOverlaySettingsController debugOverlaySettingsController;
    private HapticSettingsController hapticSettingsController;
    private RepeatSettingsController repeatSettingsController;
    private GestureTouchSettingsController gestureTouchSettingsController;
    private boolean syncing;
    private boolean demoShowKeyboard;
    private Spinner handednessSpinner;
    private Spinner hangulLayoutProfileSpinner;
    private Spinner englishLayoutProfileSpinner;
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
    private Spinner remoteKeyPresetSpinner;
    private Spinner remoteImeShortcutSpinner;
    private Spinner inputAssistanceModeSpinner;
    private InputAssistanceMode[] inputAssistanceModes =
            InputAssistanceSettingsController.availableModes(false);
    private Spinner motionEffectLevelSpinner;
    private Spinner ergonomicsPresetSpinner;
    private Spinner visualConsistencyLevelSpinner;
    private Button deleteThemeButton;
    private CheckBox hangulNumberRowCheckBox;
    private CheckBox englishNumberRowCheckBox;
    private CheckBox touchBiasAutoCorrectionCheckBox;
    private CheckBox palmRejectionCheckBox;
    private CheckBox clipboardHistoryCheckBox;
    private TextView localDataSummaryValue;
    private CheckBox doubleSpacePeriodCheckBox;
    private CheckBox keyDepthCheckBox;
    private CheckBox customDepthColorCheckBox;
    private CheckBox followThemeTypographyCheckBox;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private CheckBox pointKeycapStyleCheckBox;
    private CheckBox remoteModeCheckBox;
    private CheckBox remoteAutoModeCheckBox;
    private CheckBox showCurrentAppProfileCheckBox;
    private CheckBox hangulConsonantSlideHintsCheckBox;
    private CheckBox hangulVowelSlideHintsCheckBox;
    private CheckBox englishSlideHintsCheckBox;
    private CheckBox spacebarSlideHintsCheckBox;
    private CheckBox beginnerTooltipPreviewCheckBox;
    private CheckBox mainKeyCenteringCheckBox;
    private CheckBox compactFunctionRailCheckBox;
    private CheckBox ergonomicHitboxCheckBox;
    private CheckBox ergonomicPositionAdjustCheckBox;
    private CheckBox leftAssistRailCheckBox;
    private CheckBox uniformGridGapCheckBox;
    private TextView leftMarginValue;
    private TextView rightMarginValue;
    private TextView hangulHeightValue;
    private TextView englishHeightValue;
    private TextView hangulSpecialColumnValue;
    private TextView keyboardTopPaddingValue;
    private TextView keyboardBottomPaddingValue;
    private TextView numberRowBottomGapValue;
    private TextView hangulKeyGapValue;
    private TextView englishKeyGapValue;
    private EditText leftMarginInput;
    private EditText rightMarginInput;
    private EditText keyboardTopPaddingInput;
    private EditText keyboardBottomPaddingInput;
    private EditText numberRowBottomGapInput;
    private EditText hangulKeyGapInput;
    private EditText englishKeyGapInput;
    private EditText remoteAutoPackagesInput;
    private EditText appProfileAsciiPackagesInput;
    private EditText appProfileNumberRowPackagesInput;
    private EditText appProfileNoComposingPackagesInput;
    private EditText appProfileNoTextConveniencesPackagesInput;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private TextView dingulInputDiagnosticsValue;
    private TextView currentAppProfileSummaryValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private TextView ergonomicsPresetStateValue;
    private LinearLayout themePresetCards;
    private ThemeOption[] themeOptions = new ThemeOption[0];
    private int selectedThemePresetIndex;
    private EditText gestureTestInput;
    private int demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
    private int demoPracticeImeOptions = EditorInfo.IME_ACTION_NONE;
    private boolean demoPracticeSingleLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        settings = KeyboardPreferences.load(this);
        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        ergonomicsOptions = KeyboardPreferences.loadErgonomicsOptions(this);
        localDataControlsController = new LocalDataControlsController(this);
        KeyboardPreferences.saveFloatingModeEnabled(this, false);
        applyIntentOverrides(getIntent());
        restoreSelectedThemePresetIndex();
        syncing = true;
        setContentView(createContentView());
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        ergonomicsOptions = KeyboardPreferences.loadErgonomicsOptions(this);
        KeyboardPreferences.saveFloatingModeEnabled(this, false);
        applyIntentOverrides(getIntent());
        restoreSelectedThemePresetIndex();
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
        if (debugDemoIntent && intent.hasExtra(EXTRA_DEMO_FIELD_PROFILE)) {
            applyDemoFieldProfile(intent.getStringExtra(EXTRA_DEMO_FIELD_PROFILE));
        }
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
                || intent.hasExtra(EXTRA_DEMO_FIELD_PROFILE)
                || intent.hasExtra(EXTRA_THEME_PRESET_ID);
    }

    private void applyDemoFieldProfile(String profile) {
        demoPracticeSingleLine = false;
        demoPracticeImeOptions = EditorInfo.IME_ACTION_NONE;
        demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        if (profile == null) {
            return;
        }
        switch (profile) {
            case "password":
                demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD;
                demoPracticeSingleLine = true;
                break;
            case "number":
                demoPracticeInputType = InputType.TYPE_CLASS_NUMBER;
                demoPracticeSingleLine = true;
                break;
            case "phone":
                demoPracticeInputType = InputType.TYPE_CLASS_PHONE;
                demoPracticeSingleLine = true;
                break;
            case "datetime":
                demoPracticeInputType = InputType.TYPE_CLASS_DATETIME;
                demoPracticeSingleLine = true;
                break;
            case "url":
                demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
                demoPracticeSingleLine = true;
                break;
            case "email":
                demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                demoPracticeSingleLine = true;
                break;
            case "web_edit":
                demoPracticeInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT;
                break;
            case "search":
                demoPracticeInputType = InputType.TYPE_CLASS_TEXT;
                demoPracticeImeOptions = EditorInfo.IME_ACTION_SEARCH;
                demoPracticeSingleLine = true;
                break;
            case "multiline":
            case "standard":
            default:
                break;
        }
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

        LinearLayout hubSection = addExpandableSection(root, getString(R.string.settings_hub_title), true);
        addBodyText(hubSection, getString(R.string.gesture_practice_body), 0);
        addBodyText(hubSection, BuildInfoProvider.summary(this), 6);

        EditText testInput = new EditText(this);
        gestureTestInput = testInput;
        testInput.setHint(R.string.gesture_practice_hint);
        testInput.setSingleLine(false);
        testInput.setMinLines(2);
        testInput.setInputType(demoPracticeInputType);
        testInput.setImeOptions(demoPracticeImeOptions);
        testInput.setSingleLine(demoPracticeSingleLine);
        if (!demoPracticeSingleLine) {
            testInput.setMinLines(2);
        }
        testInput.setFocusableInTouchMode(true);
        SettingsViewStyler.editText(testInput, this);
        hubSection.addView(testInput, matchWrapWithTop(12));
        maybeShowDemoKeyboard(testInput);

        addThemeQuickControls(hubSection);
        // Keep existing preference wiring alive while appearance editing moves to ThemeEditor.
        addVisualControls(root);

        LinearLayout layoutSection = addExpandableSection(
                root,
                getString(R.string.settings_layout_section),
                true);
        addLayoutControls(layoutSection);

        LinearLayout displaySection = addExpandableSection(
                root,
                getString(R.string.settings_display_section),
                true);
        addVisibleVisualControls(displaySection);

        LinearLayout inputSection = addExpandableSection(
                root,
                getString(R.string.settings_input_feel_section),
                false);
        addInputFeelControls(inputSection);

        LinearLayout reservedSection = addExpandableSection(
                root,
                getString(R.string.settings_reserved_phrase_section),
                false);
        addReservedPhraseControls(reservedSection);

        LinearLayout remoteSection = addExpandableSection(
                root,
                getString(R.string.settings_remote_windows_section),
                false);
        addRemoteControls(remoteSection);

        LinearLayout androidSection = addExpandableSection(
                root,
                getString(R.string.settings_android_ime_section),
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
                SettingsDisplayLabels.labels(this, new HandednessMode[] {
                        HandednessMode.BALANCED,
                        HandednessMode.LEFT,
                        HandednessMode.RIGHT
                }));
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
        root.addView(label(getString(R.string.settings_handedness_label)), matchWrapWithTop(12));
        root.addView(handednessSpinner, matchWrap());

        hangulLayoutProfileSpinner = layoutProfileSpinner(profile -> {
            layoutProfiles = layoutProfiles.withHangulLayout(profile);
            KeyboardPreferences.saveHangulLayoutProfile(this, profile);
            syncControls();
        });
        root.addView(label(getString(R.string.settings_hangul_layout_label)), matchWrapWithTop(16));
        root.addView(hangulLayoutProfileSpinner, matchWrap());

        englishLayoutProfileSpinner = layoutProfileSpinner(profile -> {
            layoutProfiles = layoutProfiles.withEnglishLayout(profile);
            KeyboardPreferences.saveEnglishLayoutProfile(this, profile);
            syncControls();
        });
        root.addView(label(getString(R.string.settings_english_layout_label)), matchWrapWithTop(8));
        root.addView(englishLayoutProfileSpinner, matchWrap());

        addErgonomicControls(root);

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
        hangulNumberRowCheckBox.setText(R.string.settings_hangul_number_row);
        hangulNumberRowCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withHangulNumberRow(isChecked);
                saveAndSync();
            }
        });
        root.addView(hangulNumberRowCheckBox, matchWrapWithTop(16));

        englishNumberRowCheckBox = new CheckBox(this);
        englishNumberRowCheckBox.setText(R.string.settings_english_number_row);
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

        hangulKeyGapValue = label("");
        root.addView(hangulKeyGapValue, matchWrapWithTop(12));
        hangulKeyGapInput = addNumericStepper(root,
                settings.hangulKeyGapDp,
                KeyboardSettings.MAX_KEY_GAP_DP,
                value -> {
                    markCurrentThemeCustom();
                    settings = settings.withHangulKeyGap(value);
                    saveAndSync();
                });

        englishKeyGapValue = label("");
        root.addView(englishKeyGapValue, matchWrapWithTop(8));
        englishKeyGapInput = addNumericStepper(root,
                settings.englishKeyGapDp,
                KeyboardSettings.MAX_KEY_GAP_DP,
                value -> {
                    markCurrentThemeCustom();
                    settings = settings.withEnglishKeyGap(value);
                    saveAndSync();
                });

        hangulHeightValue = label("");
        hangulHeightSeekBar = seekBar(KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP);
        hangulHeightSeekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    markCurrentThemeCustom();
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
                    markCurrentThemeCustom();
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

    private void addErgonomicControls(LinearLayout root) {
        root.addView(label(getString(R.string.settings_ergonomics_title)), matchWrapWithTop(16));
        root.addView(label(getString(R.string.settings_ergonomics_preset)), matchWrapWithTop(8));
        ergonomicsPresetSpinner = new Spinner(this);
        ergonomicsPresetSpinner.setAdapter(new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, KeyboardErgonomicsPreset.values())));
        ergonomicsPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    ergonomicsOptions = KeyboardErgonomicsPreset.values()[position].options;
                    saveErgonomicsAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(ergonomicsPresetSpinner, matchWrap());
        ergonomicsPresetStateValue = label("");
        root.addView(ergonomicsPresetStateValue, matchWrapWithTop(4));

        mainKeyCenteringCheckBox = new CheckBox(this);
        mainKeyCenteringCheckBox.setText(R.string.settings_main_key_centering);
        mainKeyCenteringCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withMainKeyCentering(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(mainKeyCenteringCheckBox, matchWrapWithTop(8));

        leftAssistRailCheckBox = new CheckBox(this);
        leftAssistRailCheckBox.setText(R.string.settings_left_assist_rail);
        leftAssistRailCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withLeftAssistRail(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(leftAssistRailCheckBox, matchWrapWithTop(4));

        uniformGridGapCheckBox = new CheckBox(this);
        uniformGridGapCheckBox.setText(R.string.settings_uniform_grid_gap);
        uniformGridGapCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withUniformGridGap(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(uniformGridGapCheckBox, matchWrapWithTop(4));

        compactFunctionRailCheckBox = new CheckBox(this);
        compactFunctionRailCheckBox.setText(R.string.settings_compact_function_rail);
        compactFunctionRailCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withCompactFunctionRail(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(compactFunctionRailCheckBox, matchWrapWithTop(4));

        ergonomicHitboxCheckBox = new CheckBox(this);
        ergonomicHitboxCheckBox.setText(R.string.settings_ergonomic_hitbox);
        ergonomicHitboxCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withErgonomicHitbox(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(ergonomicHitboxCheckBox, matchWrapWithTop(4));

        ergonomicPositionAdjustCheckBox = new CheckBox(this);
        ergonomicPositionAdjustCheckBox.setText(R.string.settings_ergonomic_position_adjust);
        ergonomicPositionAdjustCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                ergonomicsOptions = ergonomicsOptions.withErgonomicPositionAdjust(isChecked);
                saveErgonomicsAndSync();
            }
        });
        root.addView(ergonomicPositionAdjustCheckBox, matchWrapWithTop(4));

        visualConsistencyLevelSpinner = new Spinner(this);
        ArrayAdapter<String> visualAdapter = new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, VisualConsistencyLevel.values()));
        visualConsistencyLevelSpinner.setAdapter(visualAdapter);
        visualConsistencyLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    ergonomicsOptions = ergonomicsOptions.withVisualConsistencyLevel(
                            VisualConsistencyLevel.values()[position]);
                    saveErgonomicsAndSync();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(label(getString(R.string.settings_visual_consistency_level)), matchWrapWithTop(8));
        root.addView(visualConsistencyLevelSpinner, matchWrap());
    }

    private void addThemeQuickControls(LinearLayout root) {
        Button openThemeSelectorButton = new Button(this);
        openThemeSelectorButton.setText(R.string.settings_theme_select);
        styleSystemButton(openThemeSelectorButton);
        setButtonIcon(openThemeSelectorButton, R.drawable.ic_keyboard_keyboard);
        openThemeSelectorButton.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSelectorActivity.class)));
        root.addView(openThemeSelectorButton, buttonParams());

        Button openThemeEditorButton = new Button(this);
        openThemeEditorButton.setText(R.string.settings_theme_edit);
        styleSystemButton(openThemeEditorButton);
        setButtonIcon(openThemeEditorButton, R.drawable.ic_keyboard_settings);
        openThemeEditorButton.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeEditorActivity.class)));
        root.addView(openThemeEditorButton, buttonParams());

        Button resetThemeButton = new Button(this);
        resetThemeButton.setText(R.string.settings_reset_default_theme);
        styleSystemButton(resetThemeButton);
        setButtonIcon(resetThemeButton, R.drawable.ic_keyboard_reset);
        resetThemeButton.setOnClickListener(v -> resetThemeAppearanceToDefault());
        root.addView(resetThemeButton, buttonParams());
    }

    private void resetThemeAppearanceToDefault() {
        markCurrentThemeCustom();
        settings = ThemeOption.resetToDefaultAppearance(settings);
        saveAndSync();
    }

    private void applyInputAssistanceMode(InputAssistanceMode mode) {
        settings = InputAssistanceSettingsController.applyPreset(
                this,
                settings,
                mode,
                isDebuggableBuild());
        ergonomicsOptions = InputAssistanceSettingsController.ergonomicsForMode(
                ergonomicsOptions,
                mode);
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        saveAndSync();
    }

    private void addVisibleVisualControls(LinearLayout root) {
        root.addView(label(getString(R.string.settings_icon_style)), matchWrapWithTop(8));
        modifierIconPackSpinner = modifierIconPackSpinner(true);
        root.addView(modifierIconPackSpinner, matchWrap());

        root.addView(label(getString(R.string.settings_display_style)), matchWrapWithTop(12));
        keyDisplayPackSpinner = keyDisplayPackSpinner(true);
        root.addView(keyDisplayPackSpinner, matchWrap());

        Button accentPlacementButton = new Button(this);
        accentPlacementButton.setText(R.string.settings_visual_role_edit);
        styleSystemButton(accentPlacementButton);
        accentPlacementButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AccentPlacementActivity.class)));
        root.addView(accentPlacementButton, buttonParams());

        fontFamilySpinner = fontSpinner();
        root.addView(label(getString(R.string.theme_font_label)), matchWrapWithTop(12));
        root.addView(fontFamilySpinner, matchWrap());

        followThemeTypographyCheckBox = new CheckBox(this);
        followThemeTypographyCheckBox.setText(R.string.settings_follow_theme_typography);
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
                    markCurrentThemeCustom();
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
                    markCurrentThemeCustom();
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
        primaryTextBoldCheckBox.setText(R.string.theme_primary_text_bold);
        primaryTextBoldCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
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
        primaryTextItalicCheckBox.setText(R.string.theme_primary_text_italic);
        primaryTextItalicCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
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
        secondaryTextBoldCheckBox.setText(R.string.theme_secondary_text_bold);
        secondaryTextBoldCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
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
        secondaryTextItalicCheckBox.setText(R.string.theme_secondary_text_italic);
        secondaryTextItalicCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
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
        pointKeycapStyleCheckBox.setText(R.string.settings_point_keycap_style);
        pointKeycapStyleCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
                settings = settings.withPointKeycapStyle(isChecked);
                saveAndSync();
            }
        });
        root.addView(pointKeycapStyleCheckBox, matchWrapWithTop(8));

        root.addView(label(getString(R.string.settings_input_assistance_mode)), matchWrapWithTop(12));
        inputAssistanceModeSpinner = new Spinner(this);
        inputAssistanceModes = InputAssistanceSettingsController.availableModes(isDebuggableBuild());
        inputAssistanceModeSpinner.setAdapter(new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, inputAssistanceModes)));
        inputAssistanceModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) {
                    return;
                }
                if (position < 0 || position >= inputAssistanceModes.length) {
                    return;
                }
                InputAssistanceMode mode = inputAssistanceModes[position];
                if (mode.isPreset()) {
                    applyInputAssistanceMode(mode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(inputAssistanceModeSpinner, matchWrap());

        hangulConsonantSlideHintsCheckBox = new CheckBox(this);
        hangulConsonantSlideHintsCheckBox.setText(R.string.settings_hangul_consonant_slide_hints);
        hangulConsonantSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                InputAssistanceSettingsController.saveHangulConsonantHints(
                        MainActivity.this,
                        isChecked);
                syncControls();
            }
        });
        root.addView(hangulConsonantSlideHintsCheckBox, matchWrapWithTop(12));

        hangulVowelSlideHintsCheckBox = new CheckBox(this);
        hangulVowelSlideHintsCheckBox.setText(R.string.settings_hangul_vowel_slide_hints);
        hangulVowelSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                InputAssistanceSettingsController.saveHangulVowelHints(
                        MainActivity.this,
                        isChecked);
                syncControls();
            }
        });
        root.addView(hangulVowelSlideHintsCheckBox, matchWrapWithTop(8));

        englishSlideHintsCheckBox = new CheckBox(this);
        englishSlideHintsCheckBox.setText(R.string.settings_english_slide_hints);
        englishSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
                settings = settings.withHintVisibility(
                        settings.showHangulSlideHints,
                        isChecked,
                        settings.showBeginnerTooltipPreview);
                saveAndSync();
            }
        });
        root.addView(englishSlideHintsCheckBox, matchWrapWithTop(8));

        spacebarSlideHintsCheckBox = new CheckBox(this);
        spacebarSlideHintsCheckBox.setText(R.string.settings_spacebar_slide_hints);
        spacebarSlideHintsCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                InputAssistanceSettingsController.saveSpacebarHints(
                        MainActivity.this,
                        isChecked);
                syncControls();
            }
        });
        root.addView(spacebarSlideHintsCheckBox, matchWrapWithTop(8));

        beginnerTooltipPreviewCheckBox = new CheckBox(this);
        beginnerTooltipPreviewCheckBox.setText(R.string.settings_beginner_preview);
        beginnerTooltipPreviewCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
                settings = settings.withHintVisibility(
                        settings.showHangulSlideHints,
                        settings.showEnglishSlideHints,
                        isChecked);
                saveAndSync();
            }
        });
        root.addView(beginnerTooltipPreviewCheckBox, matchWrapWithTop(8));

        motionEffectLevelSpinner = motionEffectLevelSpinner();
        root.addView(label(getString(R.string.settings_motion_effect_level)), matchWrapWithTop(12));
        root.addView(motionEffectLevelSpinner, matchWrap());
    }

    private void addVisualControls(LinearLayout unusedRoot) {
        deleteThemeButton = new Button(this);
        deleteThemeButton.setText(R.string.settings_delete_selected_user_theme);
        deleteThemeButton.setOnClickListener(v -> {
            ThemeOption option = selectedThemeOption();
            if (option == null || !option.isDeletableUserTheme()) {
                return;
            }
            UserThemeStore.delete(this, option.userThemeId);
            if (option.userThemeId.equals(KeyboardPreferences.loadSelectedThemeId(this))) {
                KeyboardPreferences.saveSelectedThemeId(this, "");
            }
            markCurrentThemeCustom();
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
        customDepthColorCheckBox.setText(R.string.settings_custom_depth_color);
        customDepthColorCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
                settings = settings.withDepthColor(isChecked, settings.depthColor);
                saveAndSync();
            }
        });
        depthColorSpinner = colorSpinner(color -> {
            settings = settings.withDepthColor(true, color);
        });
        roundnessValue = label("");
        roundnessSeekBar = seekBar(KeyboardSettings.MAX_KEY_ROUNDNESS_DP);
        keyBorderWidthValue = label("");
        keyBorderWidthSeekBar = seekBar(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP);
        keyGapValue = label("");
        keyGapSeekBar = seekBar(KeyboardSettings.MAX_KEY_GAP_DP);
        keyDepthCheckBox = new CheckBox(this);
        keyDepthCheckBox.setText(R.string.settings_key_depth_effect);
        keyDepthCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                markCurrentThemeCustom();
                settings = settings.withKeyDepth(isChecked, settings.keyDepthDp);
                saveAndSync();
            }
        });
        keyDepthValue = label("");
        keyDepthSeekBar = seekBar(KeyboardSettings.MAX_KEY_DEPTH_DP);
    }

    private void addInputFeelControls(LinearLayout root) {
        hapticSettingsController = new HapticSettingsController(this, new HapticSettingsController.Host() {
            @Override
            public KeyboardSettings settings() {
                return settings;
            }

            @Override
            public void saveSettings(KeyboardSettings nextSettings) {
                settings = nextSettings;
                saveAndSync();
            }

            @Override
            public void syncControls() {
                MainActivity.this.syncControls();
            }
        });
        hapticSettingsController.addTo(root);

        gestureTouchSettingsController = new GestureTouchSettingsController(
                this,
                new GestureTouchSettingsController.Host() {
                    @Override
                    public KeyboardSettings settings() {
                        return settings;
                    }

                    @Override
                    public void saveSettings(KeyboardSettings nextSettings) {
                        settings = nextSettings;
                        saveAndSync();
                    }

                    @Override
                    public void syncControls() {
                        MainActivity.this.syncControls();
                    }
                });
        gestureTouchSettingsController.addTo(root);

        repeatSettingsController = new RepeatSettingsController(this, new RepeatSettingsController.Host() {
            @Override
            public KeyboardSettings settings() {
                return settings;
            }

            @Override
            public void saveSettings(KeyboardSettings nextSettings) {
                settings = nextSettings;
                saveAndSync();
            }
        });
        repeatSettingsController.addTo(root);

        touchBiasAutoCorrectionCheckBox = new CheckBox(this);
        touchBiasAutoCorrectionCheckBox.setText(R.string.touch_bias_auto_correction);
        touchBiasAutoCorrectionCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveTouchBiasAutoCorrectionEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(touchBiasAutoCorrectionCheckBox, matchWrapWithTop(8));

        palmRejectionCheckBox = new CheckBox(this);
        palmRejectionCheckBox.setText(R.string.palm_rejection);
        palmRejectionCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.savePalmRejectionEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(palmRejectionCheckBox, matchWrapWithTop(8));

        clipboardHistoryCheckBox = new CheckBox(this);
        clipboardHistoryCheckBox.setText(R.string.clipboard_history_setting);
        clipboardHistoryCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                localDataControlsController.setClipboardHistoryEnabled(isChecked);
                syncControls();
            }
        });
        root.addView(clipboardHistoryCheckBox, matchWrapWithTop(8));

        doubleSpacePeriodCheckBox = new CheckBox(this);
        doubleSpacePeriodCheckBox.setText(R.string.english_double_space_period);
        doubleSpacePeriodCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withEnglishDoubleSpacePeriod(isChecked);
                saveAndSync();
            }
        });
        root.addView(doubleSpacePeriodCheckBox, matchWrapWithTop(8));

        addBodyText(
                root,
                getString(R.string.local_data_disclosure),
                12);
        localDataSummaryValue = label("");
        localDataSummaryValue.setLineSpacing(dp(2), 1.0f);
        root.addView(localDataSummaryValue, matchWrapWithTop(6));

        dingulInputDiagnosticsValue = label("");
        dingulInputDiagnosticsValue.setLineSpacing(dp(2), 1.0f);
        root.addView(dingulInputDiagnosticsValue, matchWrapWithTop(6));

        root.addView(label(getString(R.string.practice_mode_section)), matchWrapWithTop(12));
        root.addView(PracticeModeController.createPanel(this), matchWrapWithTop(6));

        Button clearAllLocalDataButton = new Button(this);
        clearAllLocalDataButton.setText(R.string.clear_all_local_data);
        styleSystemButton(clearAllLocalDataButton);
        setButtonIcon(clearAllLocalDataButton, R.drawable.ic_keyboard_reset);
        clearAllLocalDataButton.setOnClickListener(v -> {
            localDataControlsController.clearAllLocalData();
            syncControls();
        });
        root.addView(clearAllLocalDataButton, buttonParams());

        Button resetTouchBiasButton = new Button(this);
        resetTouchBiasButton.setText(R.string.clear_touch_correction_and_input_logs);
        styleSystemButton(resetTouchBiasButton);
        setButtonIcon(resetTouchBiasButton, R.drawable.ic_keyboard_reset);
        resetTouchBiasButton.setOnClickListener(v -> {
            localDataControlsController.resetTouchCorrectionAndInputLogs();
            syncControls();
        });
        root.addView(resetTouchBiasButton, buttonParams());

        Button clearInputLogsButton = new Button(this);
        clearInputLogsButton.setText(R.string.clear_input_logs_only);
        styleSystemButton(clearInputLogsButton);
        setButtonIcon(clearInputLogsButton, R.drawable.ic_keyboard_reset);
        clearInputLogsButton.setOnClickListener(v -> {
            localDataControlsController.clearInputLogsOnly();
            syncControls();
        });
        root.addView(clearInputLogsButton, buttonParams());

        Button clearTouchBiasOnlyButton = new Button(this);
        clearTouchBiasOnlyButton.setText(R.string.clear_touch_bias_only);
        styleSystemButton(clearTouchBiasOnlyButton);
        setButtonIcon(clearTouchBiasOnlyButton, R.drawable.ic_keyboard_reset);
        clearTouchBiasOnlyButton.setOnClickListener(v -> {
            localDataControlsController.clearTouchBiasOnly();
            syncControls();
        });
        root.addView(clearTouchBiasOnlyButton, buttonParams());

        Button clearClipboardButton = new Button(this);
        clearClipboardButton.setText(R.string.clear_clipboard_history);
        styleSystemButton(clearClipboardButton);
        setButtonIcon(clearClipboardButton, R.drawable.ic_keyboard_reset);
        clearClipboardButton.setOnClickListener(v -> {
            localDataControlsController.clearClipboardHistory();
            syncControls();
        });
        root.addView(clearClipboardButton, buttonParams());

        Button clearRemoteLogButton = new Button(this);
        clearRemoteLogButton.setText(R.string.clear_remote_test_log);
        styleSystemButton(clearRemoteLogButton);
        setButtonIcon(clearRemoteLogButton, R.drawable.ic_keyboard_reset);
        clearRemoteLogButton.setOnClickListener(v -> {
            localDataControlsController.clearRemoteCompatibilityLog();
            syncControls();
        });
        root.addView(clearRemoteLogButton, buttonParams());
    }
    private void addReservedPhraseControls(LinearLayout root) {
        addReservedPhraseField(root, getString(R.string.reserved_phrase_tap), GestureAction.TAP, 8);
        addReservedPhraseField(root, getString(R.string.reserved_phrase_left_slide), GestureAction.LEFT, 8);
        addReservedPhraseField(root, getString(R.string.reserved_phrase_right_slide), GestureAction.RIGHT, 8);
        addReservedPhraseField(root, getString(R.string.reserved_phrase_up_slide), GestureAction.UP, 8);
    }

    private void addReservedPhraseField(
            LinearLayout root,
            String labelText,
            GestureAction action,
            int topMarginDp) {
        root.addView(label(labelText), matchWrapWithTop(topMarginDp));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(R.string.reserved_phrase_empty_hint);
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

    private void addRemoteControls(LinearLayout root) {
        remoteModeCheckBox = new CheckBox(this);
        remoteModeCheckBox.setText(R.string.settings_remote_mode);
        remoteModeCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                settings = settings.withRemoteOptions(
                        isChecked,
                        settings.remoteKeyPreset,
                        settings.remoteImeShortcut);
                saveAndSync();
            }
        });
        root.addView(remoteModeCheckBox, matchWrapWithTop(8));

        remoteAutoModeCheckBox = new CheckBox(this);
        remoteAutoModeCheckBox.setText(R.string.settings_remote_auto_mode);
        remoteAutoModeCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveRemoteAutoModeEnabled(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(remoteAutoModeCheckBox, matchWrapWithTop(8));

        showCurrentAppProfileCheckBox = new CheckBox(this);
        showCurrentAppProfileCheckBox.setText(R.string.settings_show_current_app_profile);
        showCurrentAppProfileCheckBox.setOnCheckedChangeListener(new BooleanSettingListener() {
            @Override
            protected void onUserChanged(boolean isChecked) {
                KeyboardPreferences.saveShowCurrentAppProfile(MainActivity.this, isChecked);
                syncControls();
            }
        });
        root.addView(showCurrentAppProfileCheckBox, matchWrapWithTop(8));

        currentAppProfileSummaryValue = label("");
        currentAppProfileSummaryValue.setLineSpacing(dp(2), 1.0f);
        root.addView(currentAppProfileSummaryValue, matchWrapWithTop(6));

        root.addView(label(getString(R.string.settings_remote_auto_packages)), matchWrapWithTop(8));
        remoteAutoPackagesInput = new EditText(this);
        remoteAutoPackagesInput.setSingleLine(false);
        remoteAutoPackagesInput.setMinLines(2);
        remoteAutoPackagesInput.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        remoteAutoPackagesInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        SettingsViewStyler.editText(remoteAutoPackagesInput, this);
        remoteAutoPackagesInput.setText(KeyboardPreferences.loadRemoteAutoModePackages(this));
        remoteAutoPackagesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncing) {
                    KeyboardPreferences.saveRemoteAutoModePackages(
                            MainActivity.this,
                            s == null ? "" : s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        root.addView(remoteAutoPackagesInput, matchWrap());

        addBodyText(
                root,
                getString(R.string.settings_remote_auto_packages_help),
                6);

        addBodyText(root, getString(R.string.settings_app_profile_overrides_help), 12);
        appProfileAsciiPackagesInput = addPackageListPreference(
                root,
                R.string.settings_app_profile_ascii_packages,
                KeyboardPreferences.loadAppProfileAsciiPackages(this),
                packages -> KeyboardPreferences.saveAppProfileAsciiPackages(MainActivity.this, packages));
        appProfileNumberRowPackagesInput = addPackageListPreference(
                root,
                R.string.settings_app_profile_number_row_packages,
                KeyboardPreferences.loadAppProfileNumberRowPackages(this),
                packages -> KeyboardPreferences.saveAppProfileNumberRowPackages(MainActivity.this, packages));
        appProfileNoComposingPackagesInput = addPackageListPreference(
                root,
                R.string.settings_app_profile_no_composing_packages,
                KeyboardPreferences.loadAppProfileNoComposingPackages(this),
                packages -> KeyboardPreferences.saveAppProfileNoComposingPackages(MainActivity.this, packages));
        appProfileNoTextConveniencesPackagesInput = addPackageListPreference(
                root,
                R.string.settings_app_profile_no_text_conveniences_packages,
                KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(this),
                packages -> KeyboardPreferences.saveAppProfileNoTextConveniencesPackages(
                        MainActivity.this,
                        packages));

        root.addView(label(getString(R.string.settings_remote_key_preset)), matchWrapWithTop(12));
        remoteKeyPresetSpinner = new Spinner(this);
        remoteKeyPresetSpinner.setAdapter(new SettingsArrayAdapter<>(
                this,
                remoteKeyPresetLabels()));
        remoteKeyPresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) {
                    return;
                }
                settings = settings.withRemoteOptions(
                        settings.remoteModeEnabled,
                        RemoteKeyPreset.values()[position],
                        settings.remoteImeShortcut);
                saveAndSync();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(remoteKeyPresetSpinner, matchWrap());

        root.addView(label(getString(R.string.settings_remote_ime_shortcut)), matchWrapWithTop(12));
        remoteImeShortcutSpinner = new Spinner(this);
        remoteImeShortcutSpinner.setAdapter(new SettingsArrayAdapter<>(
                this,
                remoteImeShortcutLabels()));
        remoteImeShortcutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (syncing) {
                    return;
                }
                settings = settings.withRemoteOptions(
                        settings.remoteModeEnabled,
                        settings.remoteKeyPreset,
                        RemoteImeShortcut.values()[position]);
                saveAndSync();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        root.addView(remoteImeShortcutSpinner, matchWrap());

        addBodyText(
                root,
                getString(R.string.settings_remote_mode_help),
                12);
    }

    private EditText addPackageListPreference(
            LinearLayout root,
            int labelResId,
            String initialValue,
            PackageListSaver saver) {
        root.addView(label(getString(labelResId)), matchWrapWithTop(8));
        EditText input = new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        SettingsViewStyler.editText(input, this);
        input.setText(initialValue == null ? "" : initialValue);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!syncing && saver != null) {
                    saver.save(s == null ? "" : s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        root.addView(input, matchWrap());
        return input;
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

        if (isDebuggableBuild()) {
            debugOverlaySettingsController = new DebugOverlaySettingsController(this, this::syncControls);
            debugOverlaySettingsController.addTo(root);
        }
    }

    private String[] remoteKeyPresetLabels() {
        return SettingsDisplayLabels.labels(this, RemoteKeyPreset.values());
    }

    private String[] remoteImeShortcutLabels() {
        return SettingsDisplayLabels.labels(this, RemoteImeShortcut.values());
    }

    private void saveAndSync() {
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void saveErgonomicsAndSync() {
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        syncControls();
    }

    private InputAssistanceMode currentInputAssistanceMode() {
        return InputAssistanceSettingsController.currentMode(
                this,
                settings,
                isDebuggableBuild());
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
        styleCheckBox(remoteModeCheckBox);
        styleCheckBox(remoteAutoModeCheckBox);
        styleCheckBox(showCurrentAppProfileCheckBox);
        styleCheckBox(pointKeycapStyleCheckBox);
        styleCheckBox(hangulNumberRowCheckBox);
        styleCheckBox(englishNumberRowCheckBox);
        styleCheckBox(touchBiasAutoCorrectionCheckBox);
        styleCheckBox(palmRejectionCheckBox);
        styleCheckBox(clipboardHistoryCheckBox);
        styleCheckBox(doubleSpacePeriodCheckBox);
        styleCheckBox(keyDepthCheckBox);
        styleCheckBox(hangulConsonantSlideHintsCheckBox);
        styleCheckBox(hangulVowelSlideHintsCheckBox);
        styleCheckBox(englishSlideHintsCheckBox);
        styleCheckBox(spacebarSlideHintsCheckBox);
        styleCheckBox(beginnerTooltipPreviewCheckBox);
        styleCheckBox(mainKeyCenteringCheckBox);
        styleCheckBox(leftAssistRailCheckBox);
        styleCheckBox(uniformGridGapCheckBox);
        styleCheckBox(compactFunctionRailCheckBox);
        styleCheckBox(ergonomicHitboxCheckBox);
        styleCheckBox(ergonomicPositionAdjustCheckBox);
        if (debugOverlaySettingsController != null) {
            debugOverlaySettingsController.sync();
        }
        if (hapticSettingsController != null) {
            hapticSettingsController.sync(settings);
        }
        if (repeatSettingsController != null) {
            repeatSettingsController.sync(settings);
        }
        if (gestureTouchSettingsController != null) {
            gestureTouchSettingsController.sync(settings);
        }
        if (themeOptions.length == 0) {
            reloadThemeOptions();
        }
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            markCurrentThemeCustom();
        }
        if (themePresetSpinner != null) {
            themePresetSpinner.setSelection(selectedThemePresetIndex);
        }
        rebuildThemePresetCards();
        handednessSpinner.setSelection(settings.handednessMode.ordinal());
        if (hangulLayoutProfileSpinner != null) {
            hangulLayoutProfileSpinner.setSelection(layoutProfiles.hangulLayout.ordinal());
        }
        if (englishLayoutProfileSpinner != null) {
            englishLayoutProfileSpinner.setSelection(layoutProfiles.englishLayout.ordinal());
        }
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
        setNumericText(hangulKeyGapInput, settings.hangulKeyGapDp);
        setNumericText(englishKeyGapInput, settings.englishKeyGapDp);
        roundnessSeekBar.setProgress(settings.keyRoundnessDp);
        keyBorderWidthSeekBar.setProgress(settings.keyBorderWidthDp);
        keyGapSeekBar.setProgress(settings.keyGapDp);
        keyDepthSeekBar.setProgress(settings.keyDepthDp);
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
        if (motionEffectLevelSpinner != null) {
            motionEffectLevelSpinner.setSelection(KeyboardPreferences.loadMotionEffectLevel(this).ordinal());
        }
        if (visualConsistencyLevelSpinner != null) {
            visualConsistencyLevelSpinner.setSelection(ergonomicsOptions.visualConsistencyLevel.ordinal());
        }
        KeyboardErgonomicsPreset matchingErgonomicsPreset =
                KeyboardErgonomicsPreset.findMatching(ergonomicsOptions);
        if (ergonomicsPresetSpinner != null && matchingErgonomicsPreset != null) {
            ergonomicsPresetSpinner.setSelection(matchingErgonomicsPreset.ordinal());
        }
        if (ergonomicsPresetStateValue != null) {
            String state = matchingErgonomicsPreset == null
                    ? getString(R.string.settings_custom_state)
                    : SettingsDisplayLabels.label(this, matchingErgonomicsPreset);
            ergonomicsPresetStateValue.setText(SettingsValueFormatter.currentState(this, state));
        }
        remoteKeyPresetSpinner.setSelection(settings.remoteKeyPreset.ordinal());
        remoteImeShortcutSpinner.setSelection(settings.remoteImeShortcut.ordinal());
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
        if (inputAssistanceModeSpinner != null) {
            inputAssistanceModeSpinner.setSelection(
                    InputAssistanceSettingsController.indexOf(
                            inputAssistanceModes,
                            currentInputAssistanceMode()));
        }
        hangulNumberRowCheckBox.setChecked(settings.showHangulNumberRow);
        englishNumberRowCheckBox.setChecked(settings.showEnglishNumberRow);
        touchBiasAutoCorrectionCheckBox.setChecked(
                KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(this));
        palmRejectionCheckBox.setChecked(KeyboardPreferences.loadPalmRejectionEnabled(this));
        clipboardHistoryCheckBox.setChecked(localDataControlsController.clipboardHistoryEnabled());
        if (localDataSummaryValue != null) {
            localDataSummaryValue.setText(localDataControlsController.summaryText());
        }
        if (dingulInputDiagnosticsValue != null) {
            dingulInputDiagnosticsValue.setText(DingulInputDiagnostics.load(this).summaryText(this));
        }
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
        mainKeyCenteringCheckBox.setChecked(ergonomicsOptions.mainKeyCenteringEnabled);
        leftAssistRailCheckBox.setChecked(ergonomicsOptions.leftAssistRailEnabled);
        uniformGridGapCheckBox.setChecked(ergonomicsOptions.uniformGridGapEnabled);
        leftAssistRailCheckBox.setEnabled(ergonomicsOptions.mainKeyCenteringEnabled);
        uniformGridGapCheckBox.setEnabled(ergonomicsOptions.mainKeyCenteringEnabled);
        compactFunctionRailCheckBox.setChecked(ergonomicsOptions.compactFunctionRailEnabled);
        ergonomicHitboxCheckBox.setChecked(ergonomicsOptions.ergonomicHitboxEnabled);
        ergonomicPositionAdjustCheckBox.setChecked(ergonomicsOptions.ergonomicPositionAdjustEnabled);
        remoteModeCheckBox.setChecked(settings.remoteModeEnabled);
        boolean remoteAutoModeEnabled = KeyboardPreferences.loadRemoteAutoModeEnabled(this);
        remoteAutoModeCheckBox.setChecked(remoteAutoModeEnabled);
        if (showCurrentAppProfileCheckBox != null) {
            boolean showProfile = KeyboardPreferences.loadShowCurrentAppProfile(this);
            showCurrentAppProfileCheckBox.setChecked(showProfile);
            if (currentAppProfileSummaryValue != null) {
                currentAppProfileSummaryValue.setVisibility(showProfile ? View.VISIBLE : View.GONE);
                currentAppProfileSummaryValue.setText(CurrentAppProfilePanelController.summary(this));
            }
        }
        if (remoteAutoPackagesInput != null) {
            setPackageListTextIfNotFocused(
                    remoteAutoPackagesInput,
                    KeyboardPreferences.loadRemoteAutoModePackages(this));
            remoteAutoPackagesInput.setEnabled(remoteAutoModeEnabled);
        }
        setPackageListTextIfNotFocused(
                appProfileAsciiPackagesInput,
                KeyboardPreferences.loadAppProfileAsciiPackages(this));
        setPackageListTextIfNotFocused(
                appProfileNumberRowPackagesInput,
                KeyboardPreferences.loadAppProfileNumberRowPackages(this));
        setPackageListTextIfNotFocused(
                appProfileNoComposingPackagesInput,
                KeyboardPreferences.loadAppProfileNoComposingPackages(this));
        setPackageListTextIfNotFocused(
                appProfileNoTextConveniencesPackagesInput,
                KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(this));
        remoteKeyPresetSpinner.setEnabled(settings.remoteModeEnabled || remoteAutoModeEnabled);
        remoteImeShortcutSpinner.setEnabled(settings.remoteModeEnabled || remoteAutoModeEnabled);
        keyDepthSeekBar.setEnabled(settings.keyDepthEnabled);
        depthColorSpinner.setEnabled(settings.customDepthColorEnabled);
        deleteThemeButton.setEnabled(selectedThemeOption() != null && selectedThemeOption().isDeletableUserTheme());
        leftMarginValue.setText(SettingsValueFormatter.sharedPadding(this, settings.leftMarginDp));
        rightMarginValue.setText(SettingsValueFormatter.sharedPadding(this, settings.rightMarginDp));
        hangulHeightValue.setText(SettingsValueFormatter.hangulHeight(this, settings));
        englishHeightValue.setText(SettingsValueFormatter.englishHeight(this, settings));
        hangulSpecialColumnValue.setText(SettingsValueFormatter.hangulSpecialColumn(
                this,
                settings.hangulSpecialColumnPercent));
        keyboardTopPaddingValue.setText(SettingsValueFormatter.keyboardTopPadding(
                this,
                settings.keyboardTopPaddingDp));
        keyboardBottomPaddingValue.setText(SettingsValueFormatter.keyboardBottomPadding(
                this,
                settings.keyboardBottomPaddingDp));
        numberRowBottomGapValue.setText(SettingsValueFormatter.numberRowGap(
                this,
                settings.numberRowBottomGapDp));
        hangulKeyGapValue.setText(SettingsValueFormatter.hangulKeyGap(this, settings.hangulKeyGapDp));
        englishKeyGapValue.setText(SettingsValueFormatter.englishKeyGap(this, settings.englishKeyGapDp));
        roundnessValue.setText(SettingsValueFormatter.roundness(this, settings.keyRoundnessDp));
        keyBorderWidthValue.setText(SettingsValueFormatter.borderWidth(this, settings.keyBorderWidthDp));
        keyGapValue.setText(SettingsValueFormatter.visualGap(this, settings.keyGapDp));
        keyDepthValue.setText(SettingsValueFormatter.depthHeight(this, settings));
        primaryTextSizeValue.setText(SettingsValueFormatter.primaryTextSize(
                this,
                settings.primaryTextSizePercent));
        secondaryTextSizeValue.setText(SettingsValueFormatter.secondaryTextSize(
                this,
                settings.secondaryTextSizePercent));
        handednessSpinner.post(() -> syncing = false);
    }

    private boolean shouldHandleSpinnerSelection(Spinner spinner) {
        if (Boolean.FALSE.equals(spinner.getTag())) {
            spinner.setTag(Boolean.TRUE);
            return false;
        }
        return !syncing;
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
        if (checkBox == null) {
            return;
        }
        SettingsViewStyler.compoundButton(checkBox, this);
    }

    private boolean isDebuggableBuild() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
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

    private Spinner layoutProfileSpinner(final LayoutProfileChangeListener listener) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, KeyboardLayoutProfile.values()));
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    listener.onProfileChanged(KeyboardLayoutProfile.values()[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner colorSpinner(final ColorChangeListener listener) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, ColorOption.BASIC_OPTIONS));
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    markCurrentThemeCustom();
                    listener.onColorChanged(ColorOption.BASIC_OPTIONS[position].color);
                    saveAndSync();
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
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, AdditionalNumberRowColorMode.values()));
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    markCurrentThemeCustom();
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
        spinner.setTag(Boolean.FALSE);
        reloadThemeOptions();
        themePresetSpinner = spinner;
        refreshThemePresetAdapter();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!shouldHandleSpinnerSelection(spinner)) {
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
        themeOptions = ThemeOption.buildOptions(UserThemeStore.load(this), ExternalThemeStore.load(this), true);
    }

    private void restoreSelectedThemePresetIndex() {
        reloadThemeOptions();
        selectedThemePresetIndex = ThemeOption.indexOfStableId(
                themeOptions,
                KeyboardPreferences.loadSelectedThemeId(this));
    }

    private void markCurrentThemeCustom() {
        selectedThemePresetIndex = 0;
        KeyboardPreferences.saveSelectedThemeId(this, "");
    }

    private void applyThemeOption(int position) {
        if (position <= 0 || position >= themeOptions.length) {
            if (selectedThemePresetIndex == 0
                    && KeyboardPreferences.loadSelectedThemeId(this).isEmpty()) {
                return;
            }
            markCurrentThemeCustom();
            syncControls();
            return;
        }
        String themeId = themeOptions[position].stableId();
        if (position == selectedThemePresetIndex
                && Objects.equals(KeyboardPreferences.loadSelectedThemeId(this), themeId)) {
            return;
        }
        selectedThemePresetIndex = position;
        settings = themeOptions[position].applyTo(settings);
        KeyboardPreferences.saveSelectedThemeId(this, themeId);
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

        card.addView(themePreviewLabel(getString(R.string.theme_preview_qwerty_label)), matchWrapWithTop(10));
        card.addView(themePreviewKeyboard(englishSettings), previewParams(88));
        card.addView(themePreviewLabel(getString(R.string.theme_preview_dingul_label)), matchWrapWithTop(12));
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
        HangulKeyboardView preview = KeyboardPreviewFactory.nonInteractive(this, previewSettings);
        preview.setAlpha(1f);
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
        return ThemeOption.indexOfStableId(themeOptions, userThemeId);
    }

    private ThemeOption selectedThemeOption() {
        if (selectedThemePresetIndex < 0 || selectedThemePresetIndex >= themeOptions.length) {
            return null;
        }
        return themeOptions[selectedThemePresetIndex];
    }

    private Spinner fontSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(
                this,
                SettingsDisplayLabels.labels(this, FontOption.BASIC_OPTIONS));
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    markCurrentThemeCustom();
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

    private Spinner motionEffectLevelSpinner() {
        Spinner spinner = new Spinner(this);
        MotionEffectLevel[] values = MotionEffectLevel.values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = SettingsDisplayLabels.label(this, values[i]);
        }
        spinner.setAdapter(new SettingsArrayAdapter<>(this, labels));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    KeyboardPreferences.saveMotionEffectLevel(MainActivity.this, values[position]);
                    syncControls();
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
            labels[i] = ids[i].isEmpty()
                    ? getString(R.string.settings_theme_default)
                    : ModifierIconCatalog.displayName(ids[i]);
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
                    ? getString(R.string.settings_theme_default)
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

    private void setPackageListTextIfNotFocused(EditText input, String value) {
        if (input == null || input.hasFocus()) {
            return;
        }
        String safeValue = value == null ? "" : value;
        if (!safeValue.equals(input.getText().toString())) {
            input.setText(safeValue);
        }
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

    private interface LayoutProfileChangeListener {
        void onProfileChanged(KeyboardLayoutProfile profile);
    }

    private interface IntSettingListener {
        void onValue(int value);
    }

    private interface PackageListSaver {
        void save(String packages);
    }

}
