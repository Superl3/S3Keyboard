package com.academic.hangulgestureime;

import android.content.Context;
import android.content.SharedPreferences;

final class KeyboardPreferences {
    static final String KEYBOARD_MODE_LAST = "keyboard_mode_last";
    static final String HANDEDNESS_MODE = "handedness_mode";
    static final String LEFT_MARGIN_DP = "left_margin_dp";
    static final String RIGHT_MARGIN_DP = "right_margin_dp";
    static final String HANGUL_LEFT_PADDING_DP = "hangul_left_padding_dp";
    static final String HANGUL_RIGHT_PADDING_DP = "hangul_right_padding_dp";
    static final String ENGLISH_LEFT_PADDING_DP = "english_left_padding_dp";
    static final String ENGLISH_RIGHT_PADDING_DP = "english_right_padding_dp";
    static final String HANGUL_MAIN_SPECIAL_GAP_DP = "hangul_main_special_gap_dp";
    static final String KEYBOARD_TOP_PADDING_DP = "keyboard_top_padding_dp";
    static final String KEYBOARD_BOTTOM_PADDING_DP = "keyboard_bottom_padding_dp";
    static final String BOTTOM_ROW_TOP_PADDING_DP = "bottom_row_top_padding_dp";
    static final String KEYBOARD_HEIGHT_DP = "keyboard_height_dp";
    static final String HANGUL_KEYBOARD_HEIGHT_DP = "hangul_keyboard_height_dp";
    static final String ENGLISH_KEYBOARD_HEIGHT_DP = "english_keyboard_height_dp";
    static final String SHOW_NUMBER_ROW = "show_number_row";
    static final String SHOW_HANGUL_NUMBER_ROW = "show_hangul_number_row";
    static final String SHOW_ENGLISH_NUMBER_ROW = "show_english_number_row";
    static final String ADDITIONAL_NUMBER_ROW_COLOR_MODE = "additional_number_row_color_mode";
    static final String HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled";
    static final String HIT_SLOP_DP = "hit_slop_dp";
    static final String GESTURE_THRESHOLD_DP = "gesture_threshold_dp";
    static final String TOUCH_Y_OFFSET_DP = "touch_y_offset_dp";
    static final String REPEAT_START_DELAY_MS = "repeat_start_delay_ms";
    static final String REPEAT_INTERVAL_MS = "repeat_interval_ms";
    static final String ENGLISH_DOUBLE_SPACE_PERIOD_ENABLED = "english_double_space_period_enabled";
    static final String KEY_IDLE_COLOR = "key_idle_color";
    static final String KEY_PRESSED_COLOR = "key_pressed_color";
    static final String KEYBOARD_BACKGROUND_COLOR = "keyboard_background_color";
    static final String ACCENT_COLOR = "accent_color";
    static final String SECONDARY_COLOR = "secondary_color";
    static final String FUNCTION_KEY_COLOR = "function_key_color";
    static final String PRIMARY_FUNCTION_KEY_COLOR = "primary_function_key_color";
    static final String ACCENT_KEY_COLOR = "accent_key_color";
    static final String BORDER_COLOR = "border_color";
    static final String KEY_BORDER_WIDTH_DP = "key_border_width_dp";
    static final String KEY_ROUNDNESS_DP = "key_roundness_dp";
    static final String KEY_GAP_DP = "key_gap_dp";
    static final String KEY_DEPTH_ENABLED = "key_depth_enabled";
    static final String KEY_DEPTH_DP = "key_depth_dp";
    static final String CUSTOM_DEPTH_COLOR_ENABLED = "custom_depth_color_enabled";
    static final String DEPTH_COLOR = "depth_color";
    static final String FONT_FAMILY = "font_family";
    static final String PRIMARY_TEXT_SIZE_PERCENT = "primary_text_size_percent";
    static final String SECONDARY_TEXT_SIZE_PERCENT = "secondary_text_size_percent";
    static final String PRIMARY_TEXT_BOLD = "primary_text_bold";
    static final String PRIMARY_TEXT_ITALIC = "primary_text_italic";
    static final String SECONDARY_TEXT_BOLD = "secondary_text_bold";
    static final String SECONDARY_TEXT_ITALIC = "secondary_text_italic";
    static final String SHOW_HANGUL_SLIDE_HINTS = "show_hangul_slide_hints";
    static final String SHOW_ENGLISH_SLIDE_HINTS = "show_english_slide_hints";
    static final String SHOW_BEGINNER_TOOLTIP_PREVIEW = "show_beginner_tooltip_preview";
    static final String SHOW_CONSONANT_PREVIEW = "show_consonant_preview";
    static final String SHOW_VOWEL_PREVIEW = "show_vowel_preview";
    static final String HAPTIC_TICK_DURATION_MS = "haptic_tick_duration_ms";
    static final String HAPTIC_TICK_GAP_MS = "haptic_tick_gap_ms";
    static final String HANGUL_SPECIAL_COLUMN_PERCENT = "hangul_special_column_percent";
    static final String HANGUL_MAIN_KEY_UNITS = "hangul_main_key_units";
    static final String KEY_COLOR_OVERRIDES = "key_color_overrides";
    static final String RESERVED_TAP_TEXT = "reserved_tap_text";
    static final String RESERVED_LEFT_TEXT = "reserved_left_text";
    static final String RESERVED_RIGHT_TEXT = "reserved_right_text";
    static final String RESERVED_UP_TEXT = "reserved_up_text";
    static final String DIFFERENTIATED_HAPTIC_ENABLED = "differentiated_haptic_enabled";
    static final String TOUCH_BIAS_AUTO_CORRECTION_ENABLED = "touch_bias_auto_correction_enabled";
    static final String CLIPBOARD_HISTORY_ENABLED = "clipboard_history_enabled";
    static final String FLOATING_MODE_ENABLED = "floating_mode_enabled";

    private static final String PREF_NAME = "keyboard_preferences";
    private static final String DEFAULT_RESERVED_TAP_TEXT = "ㅋㅋㅋ";
    private static final int LEGACY_DEFAULT_HANGUL_HEIGHT_DP = 390;
    private static final int LEGACY_DEFAULT_ENGLISH_HEIGHT_DP = 286;
    private static final int LEGACY_DEFAULT_KEYBOARD_BOTTOM_PADDING_DP = 4;
    private static final int LEGACY_DEFAULT_BOTTOM_ROW_TOP_PADDING_DP = 6;
    private static final int LEGACY_DEFAULT_PRIMARY_TEXT_SIZE_PERCENT = 92;
    private static final int LEGACY_DEFAULT_SECONDARY_TEXT_SIZE_PERCENT = 90;
    private static final int LEGACY_DEFAULT_GESTURE_THRESHOLD_DP = 28;
    static final int DEFAULT_HAPTIC_TICK_DURATION_MS = 14;
    static final int MIN_HAPTIC_TICK_DURATION_MS = 4;
    static final int MAX_HAPTIC_TICK_DURATION_MS = 40;
    static final int DEFAULT_HAPTIC_TICK_GAP_MS = 18;
    static final int MIN_HAPTIC_TICK_GAP_MS = 4;
    static final int MAX_HAPTIC_TICK_GAP_MS = 60;

    private KeyboardPreferences() {
    }

    static KeyboardSettings load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        KeyboardSettings defaults = KeyboardSettings.defaults();
        int legacyHeightDp = prefs.getInt(KEYBOARD_HEIGHT_DP, defaults.keyboardHeightDp);
        int hangulHeightDp = prefs.getInt(
                HANGUL_KEYBOARD_HEIGHT_DP,
                prefs.contains(KEYBOARD_HEIGHT_DP) ? legacyHeightDp : defaults.hangulKeyboardHeightDp);
        int englishHeightDp = prefs.getInt(
                ENGLISH_KEYBOARD_HEIGHT_DP,
                prefs.contains(KEYBOARD_HEIGHT_DP) ? legacyHeightDp : defaults.englishKeyboardHeightDp);
        hangulHeightDp = migrateLegacyDefault(
                prefs,
                HANGUL_KEYBOARD_HEIGHT_DP,
                hangulHeightDp,
                LEGACY_DEFAULT_HANGUL_HEIGHT_DP,
                defaults.hangulKeyboardHeightDp);
        englishHeightDp = migrateLegacyDefault(
                prefs,
                ENGLISH_KEYBOARD_HEIGHT_DP,
                englishHeightDp,
                LEGACY_DEFAULT_ENGLISH_HEIGHT_DP,
                defaults.englishKeyboardHeightDp);
        int legacyLeftPaddingDp = prefs.getInt(LEFT_MARGIN_DP, defaults.leftMarginDp);
        int legacyRightPaddingDp = prefs.getInt(RIGHT_MARGIN_DP, defaults.rightMarginDp);
        int hangulLeftPaddingDp = layoutPadding(
                prefs,
                HANGUL_LEFT_PADDING_DP,
                LEFT_MARGIN_DP,
                legacyLeftPaddingDp,
                defaults.hangulLeftPaddingDp);
        int hangulRightPaddingDp = layoutPadding(
                prefs,
                HANGUL_RIGHT_PADDING_DP,
                RIGHT_MARGIN_DP,
                legacyRightPaddingDp,
                defaults.hangulRightPaddingDp);
        int englishLeftPaddingDp = layoutPadding(
                prefs,
                ENGLISH_LEFT_PADDING_DP,
                LEFT_MARGIN_DP,
                legacyLeftPaddingDp,
                defaults.englishLeftPaddingDp);
        int englishRightPaddingDp = layoutPadding(
                prefs,
                ENGLISH_RIGHT_PADDING_DP,
                RIGHT_MARGIN_DP,
                legacyRightPaddingDp,
                defaults.englishRightPaddingDp);
        int hangulMainSpecialGapDp = prefs.getInt(
                HANGUL_MAIN_SPECIAL_GAP_DP,
                defaults.hangulMainSpecialGapDp);
        int keyboardTopPaddingDp = prefs.getInt(
                KEYBOARD_TOP_PADDING_DP,
                defaults.keyboardTopPaddingDp);
        int keyboardBottomPaddingDp = prefs.getInt(
                KEYBOARD_BOTTOM_PADDING_DP,
                defaults.keyboardBottomPaddingDp);
        int bottomRowTopPaddingDp = prefs.getInt(
                BOTTOM_ROW_TOP_PADDING_DP,
                defaults.bottomRowTopPaddingDp);
        keyboardBottomPaddingDp = migrateLegacyDefault(
                prefs,
                KEYBOARD_BOTTOM_PADDING_DP,
                keyboardBottomPaddingDp,
                0,
                defaults.keyboardBottomPaddingDp);
        bottomRowTopPaddingDp = migrateLegacyDefault(
                prefs,
                BOTTOM_ROW_TOP_PADDING_DP,
                bottomRowTopPaddingDp,
                LEGACY_DEFAULT_BOTTOM_ROW_TOP_PADDING_DP,
                defaults.bottomRowTopPaddingDp);
        int primaryTextSizePercent = migrateLegacyDefault(
                prefs,
                PRIMARY_TEXT_SIZE_PERCENT,
                prefs.getInt(PRIMARY_TEXT_SIZE_PERCENT, defaults.primaryTextSizePercent),
                LEGACY_DEFAULT_PRIMARY_TEXT_SIZE_PERCENT,
                defaults.primaryTextSizePercent);
        int secondaryTextSizePercent = migrateLegacyDefault(
                prefs,
                SECONDARY_TEXT_SIZE_PERCENT,
                prefs.getInt(SECONDARY_TEXT_SIZE_PERCENT, defaults.secondaryTextSizePercent),
                LEGACY_DEFAULT_SECONDARY_TEXT_SIZE_PERCENT,
                defaults.secondaryTextSizePercent);
        int gestureThresholdDp = migrateLegacyDefault(
                prefs,
                GESTURE_THRESHOLD_DP,
                prefs.getInt(GESTURE_THRESHOLD_DP, defaults.gestureThresholdDp),
                LEGACY_DEFAULT_GESTURE_THRESHOLD_DP,
                defaults.gestureThresholdDp);
        KeyboardSettings loaded = new KeyboardSettings(
                KeyboardMode.fromPreference(prefs.getString(
                        KEYBOARD_MODE_LAST,
                        defaults.keyboardMode.preferenceValue)),
                HandednessMode.fromPreference(prefs.getString(
                        HANDEDNESS_MODE,
                        defaults.handednessMode.preferenceValue)),
                legacyLeftPaddingDp,
                legacyRightPaddingDp,
                hangulHeightDp,
                englishHeightDp,
                showHangulNumberRow(prefs, defaults),
                showEnglishNumberRow(prefs, defaults),
                false,
                prefs.getBoolean(HAPTIC_FEEDBACK_ENABLED, defaults.hapticFeedbackEnabled),
                prefs.getInt(HIT_SLOP_DP, defaults.hitSlopDp),
                gestureThresholdDp,
                prefs.getInt(TOUCH_Y_OFFSET_DP, defaults.touchYOffsetDp),
                prefs.getInt(REPEAT_START_DELAY_MS, defaults.repeatStartDelayMs),
                prefs.getInt(REPEAT_INTERVAL_MS, defaults.repeatIntervalMs),
                prefs.getBoolean(
                        ENGLISH_DOUBLE_SPACE_PERIOD_ENABLED,
                        defaults.englishDoubleSpacePeriodEnabled),
                defaults.enterKeyLabel,
                prefs.getInt(KEY_IDLE_COLOR, defaults.keyIdleColor),
                prefs.getInt(KEY_PRESSED_COLOR, defaults.keyPressedColor),
                prefs.getInt(KEYBOARD_BACKGROUND_COLOR, defaults.keyboardBackgroundColor),
                prefs.getInt(ACCENT_COLOR, defaults.accentColor),
                prefs.getInt(SECONDARY_COLOR, defaults.secondaryColor),
                prefs.getInt(FUNCTION_KEY_COLOR, defaults.functionKeyColor),
                prefs.getInt(PRIMARY_FUNCTION_KEY_COLOR, defaults.primaryFunctionKeyColor),
                prefs.getInt(ACCENT_KEY_COLOR, defaults.accentKeyColor),
                prefs.getInt(BORDER_COLOR, defaults.borderColor),
                prefs.getInt(KEY_BORDER_WIDTH_DP, defaults.keyBorderWidthDp),
                prefs.getInt(KEY_ROUNDNESS_DP, defaults.keyRoundnessDp),
                prefs.getInt(KEY_GAP_DP, defaults.keyGapDp),
                prefs.getBoolean(KEY_DEPTH_ENABLED, defaults.keyDepthEnabled),
                prefs.getInt(KEY_DEPTH_DP, defaults.keyDepthDp),
                prefs.getBoolean(CUSTOM_DEPTH_COLOR_ENABLED, defaults.customDepthColorEnabled),
                prefs.getInt(DEPTH_COLOR, defaults.depthColor),
                prefs.getString(FONT_FAMILY, defaults.fontFamily),
                prefs.getBoolean(SHOW_HANGUL_SLIDE_HINTS, defaults.showHangulSlideHints),
                prefs.getBoolean(SHOW_ENGLISH_SLIDE_HINTS, defaults.showEnglishSlideHints),
                prefs.getBoolean(SHOW_BEGINNER_TOOLTIP_PREVIEW, defaults.showBeginnerTooltipPreview),
                hangulSpecialColumnPercent(prefs, defaults));
        return loaded
                .withHangulSidePadding(hangulLeftPaddingDp, hangulRightPaddingDp)
                .withEnglishSidePadding(englishLeftPaddingDp, englishRightPaddingDp)
                .withLayoutSpacing(
                        hangulMainSpecialGapDp,
                        keyboardTopPaddingDp,
                        keyboardBottomPaddingDp,
                        bottomRowTopPaddingDp)
                .withTypography(
                        prefs.getString(FONT_FAMILY, defaults.fontFamily),
                        primaryTextSizePercent,
                        secondaryTextSizePercent,
                        prefs.getBoolean(PRIMARY_TEXT_BOLD, defaults.primaryTextBold),
                        prefs.getBoolean(PRIMARY_TEXT_ITALIC, defaults.primaryTextItalic),
                        prefs.getBoolean(SECONDARY_TEXT_BOLD, defaults.secondaryTextBold),
                        prefs.getBoolean(SECONDARY_TEXT_ITALIC, defaults.secondaryTextItalic))
                .withKeyColorOverrides(
                        KeyboardThemeJson.decodeKeyColorOverrides(prefs.getString(KEY_COLOR_OVERRIDES, "")))
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.fromPreference(prefs.getString(
                        ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                        defaults.additionalNumberRowColorMode.preferenceValue)));
    }

    static void saveSettings(Context context, KeyboardSettings settings) {
        prefs(context).edit()
                .putString(KEYBOARD_MODE_LAST, settings.keyboardMode.preferenceValue)
                .putString(HANDEDNESS_MODE, settings.handednessMode.preferenceValue)
                .putInt(LEFT_MARGIN_DP, settings.leftMarginDp)
                .putInt(RIGHT_MARGIN_DP, settings.rightMarginDp)
                .putInt(HANGUL_LEFT_PADDING_DP, settings.hangulLeftPaddingDp)
                .putInt(HANGUL_RIGHT_PADDING_DP, settings.hangulRightPaddingDp)
                .putInt(ENGLISH_LEFT_PADDING_DP, settings.englishLeftPaddingDp)
                .putInt(ENGLISH_RIGHT_PADDING_DP, settings.englishRightPaddingDp)
                .putInt(HANGUL_MAIN_SPECIAL_GAP_DP, settings.hangulMainSpecialGapDp)
                .putInt(KEYBOARD_TOP_PADDING_DP, settings.keyboardTopPaddingDp)
                .putInt(KEYBOARD_BOTTOM_PADDING_DP, settings.keyboardBottomPaddingDp)
                .putInt(BOTTOM_ROW_TOP_PADDING_DP, settings.bottomRowTopPaddingDp)
                .putInt(KEYBOARD_HEIGHT_DP, settings.keyboardHeightDp)
                .putInt(HANGUL_KEYBOARD_HEIGHT_DP, settings.hangulKeyboardHeightDp)
                .putInt(ENGLISH_KEYBOARD_HEIGHT_DP, settings.englishKeyboardHeightDp)
                .putBoolean(SHOW_NUMBER_ROW, settings.showNumberRow)
                .putBoolean(SHOW_HANGUL_NUMBER_ROW, settings.showHangulNumberRow)
                .putBoolean(SHOW_ENGLISH_NUMBER_ROW, settings.showEnglishNumberRow)
                .putString(
                        ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                        settings.additionalNumberRowColorMode.preferenceValue)
                .putBoolean(HAPTIC_FEEDBACK_ENABLED, settings.hapticFeedbackEnabled)
                .putInt(HIT_SLOP_DP, settings.hitSlopDp)
                .putInt(GESTURE_THRESHOLD_DP, settings.gestureThresholdDp)
                .putInt(TOUCH_Y_OFFSET_DP, settings.touchYOffsetDp)
                .putInt(REPEAT_START_DELAY_MS, settings.repeatStartDelayMs)
                .putInt(REPEAT_INTERVAL_MS, settings.repeatIntervalMs)
                .putBoolean(
                        ENGLISH_DOUBLE_SPACE_PERIOD_ENABLED,
                        settings.englishDoubleSpacePeriodEnabled)
                .putInt(KEY_IDLE_COLOR, settings.keyIdleColor)
                .putInt(KEY_PRESSED_COLOR, settings.keyPressedColor)
                .putInt(KEYBOARD_BACKGROUND_COLOR, settings.keyboardBackgroundColor)
                .putInt(ACCENT_COLOR, settings.accentColor)
                .putInt(SECONDARY_COLOR, settings.secondaryColor)
                .putInt(FUNCTION_KEY_COLOR, settings.functionKeyColor)
                .putInt(PRIMARY_FUNCTION_KEY_COLOR, settings.primaryFunctionKeyColor)
                .putInt(ACCENT_KEY_COLOR, settings.accentKeyColor)
                .putInt(BORDER_COLOR, settings.borderColor)
                .putInt(KEY_BORDER_WIDTH_DP, settings.keyBorderWidthDp)
                .putInt(KEY_ROUNDNESS_DP, settings.keyRoundnessDp)
                .putInt(KEY_GAP_DP, settings.keyGapDp)
                .putBoolean(KEY_DEPTH_ENABLED, settings.keyDepthEnabled)
                .putInt(KEY_DEPTH_DP, settings.keyDepthDp)
                .putBoolean(CUSTOM_DEPTH_COLOR_ENABLED, settings.customDepthColorEnabled)
                .putInt(DEPTH_COLOR, settings.depthColor)
                .putString(FONT_FAMILY, settings.fontFamily)
                .putInt(PRIMARY_TEXT_SIZE_PERCENT, settings.primaryTextSizePercent)
                .putInt(SECONDARY_TEXT_SIZE_PERCENT, settings.secondaryTextSizePercent)
                .putBoolean(PRIMARY_TEXT_BOLD, settings.primaryTextBold)
                .putBoolean(PRIMARY_TEXT_ITALIC, settings.primaryTextItalic)
                .putBoolean(SECONDARY_TEXT_BOLD, settings.secondaryTextBold)
                .putBoolean(SECONDARY_TEXT_ITALIC, settings.secondaryTextItalic)
                .putBoolean(SHOW_HANGUL_SLIDE_HINTS, settings.showHangulSlideHints)
                .putBoolean(SHOW_ENGLISH_SLIDE_HINTS, settings.showEnglishSlideHints)
                .putBoolean(SHOW_BEGINNER_TOOLTIP_PREVIEW, settings.showBeginnerTooltipPreview)
                .putInt(HANGUL_SPECIAL_COLUMN_PERCENT, settings.hangulSpecialColumnPercent)
                .putString(
                        KEY_COLOR_OVERRIDES,
                        KeyboardThemeJson.encodeKeyColorOverrides(settings.keyColorOverrides))
                .apply();
    }

    static void saveKeyboardMode(Context context, KeyboardMode mode) {
        prefs(context).edit()
                .putString(KEYBOARD_MODE_LAST, mode.preferenceValue)
                .apply();
    }

    static int loadHapticTickDurationMs(Context context) {
        return clamp(
                prefs(context).getInt(HAPTIC_TICK_DURATION_MS, DEFAULT_HAPTIC_TICK_DURATION_MS),
                MIN_HAPTIC_TICK_DURATION_MS,
                MAX_HAPTIC_TICK_DURATION_MS);
    }

    static int loadHapticTickGapMs(Context context) {
        return clamp(
                prefs(context).getInt(HAPTIC_TICK_GAP_MS, DEFAULT_HAPTIC_TICK_GAP_MS),
                MIN_HAPTIC_TICK_GAP_MS,
                MAX_HAPTIC_TICK_GAP_MS);
    }

    static void saveHapticTickDurationMs(Context context, int durationMs) {
        prefs(context).edit()
                .putInt(
                        HAPTIC_TICK_DURATION_MS,
                        clamp(durationMs, MIN_HAPTIC_TICK_DURATION_MS, MAX_HAPTIC_TICK_DURATION_MS))
                .apply();
    }

    static boolean loadDifferentiatedHapticEnabled(Context context) {
        return prefs(context).getBoolean(DIFFERENTIATED_HAPTIC_ENABLED, true);
    }

    static void saveDifferentiatedHapticEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(DIFFERENTIATED_HAPTIC_ENABLED, enabled).apply();
    }

    static boolean loadTouchBiasAutoCorrectionEnabled(Context context) {
        return prefs(context).getBoolean(TOUCH_BIAS_AUTO_CORRECTION_ENABLED, true);
    }

    static void saveTouchBiasAutoCorrectionEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(TOUCH_BIAS_AUTO_CORRECTION_ENABLED, enabled).apply();
    }

    static boolean loadClipboardHistoryEnabled(Context context) {
        return prefs(context).getBoolean(CLIPBOARD_HISTORY_ENABLED, false);
    }

    static void saveClipboardHistoryEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(CLIPBOARD_HISTORY_ENABLED, enabled).apply();
    }

    static boolean loadFloatingModeEnabled(Context context) {
        return prefs(context).getBoolean(FLOATING_MODE_ENABLED, false);
    }

    static void saveFloatingModeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(FLOATING_MODE_ENABLED, enabled).apply();
    }

    static void saveHapticTickGapMs(Context context, int gapMs) {
        prefs(context).edit()
                .putInt(HAPTIC_TICK_GAP_MS, clamp(gapMs, MIN_HAPTIC_TICK_GAP_MS, MAX_HAPTIC_TICK_GAP_MS))
                .apply();
    }

    static boolean loadShowConsonantPreview(Context context) {
        return prefs(context).getBoolean(SHOW_CONSONANT_PREVIEW, true);
    }

    static boolean loadShowVowelPreview(Context context) {
        return prefs(context).getBoolean(SHOW_VOWEL_PREVIEW, true);
    }

    static void saveShowConsonantPreview(Context context, boolean enabled) {
        prefs(context).edit()
                .putBoolean(SHOW_CONSONANT_PREVIEW, enabled)
                .apply();
    }

    static void saveShowVowelPreview(Context context, boolean enabled) {
        prefs(context).edit()
                .putBoolean(SHOW_VOWEL_PREVIEW, enabled)
                .apply();
    }

    static void saveHandednessPreset(Context context, KeyboardSettings settings) {
        prefs(context).edit()
                .putString(HANDEDNESS_MODE, settings.handednessMode.preferenceValue)
                .putInt(LEFT_MARGIN_DP, settings.leftMarginDp)
                .putInt(RIGHT_MARGIN_DP, settings.rightMarginDp)
                .putInt(HANGUL_LEFT_PADDING_DP, settings.hangulLeftPaddingDp)
                .putInt(HANGUL_RIGHT_PADDING_DP, settings.hangulRightPaddingDp)
                .putInt(ENGLISH_LEFT_PADDING_DP, settings.englishLeftPaddingDp)
                .putInt(ENGLISH_RIGHT_PADDING_DP, settings.englishRightPaddingDp)
                .apply();
    }

    static String loadReservedPhrase(Context context, GestureAction action) {
        return prefs(context).getString(reservedPhraseKey(action), defaultReservedPhrase(action));
    }

    static void saveReservedPhrase(Context context, GestureAction action, String value) {
        prefs(context).edit()
                .putString(reservedPhraseKey(action), value == null ? "" : value)
                .apply();
    }

    static String loadReservedPhraseForCommand(Context context, String command) {
        return loadReservedPhrase(context, reservedActionForCommand(command));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static GestureAction reservedActionForCommand(String command) {
        if (KeyboardCommands.CMD_RESERVED_LEFT.equals(command)) {
            return GestureAction.LEFT;
        }
        if (KeyboardCommands.CMD_RESERVED_RIGHT.equals(command)) {
            return GestureAction.RIGHT;
        }
        if (KeyboardCommands.CMD_RESERVED_UP.equals(command)) {
            return GestureAction.UP;
        }
        return GestureAction.TAP;
    }

    private static String reservedPhraseKey(GestureAction action) {
        if (action == GestureAction.LEFT) {
            return RESERVED_LEFT_TEXT;
        }
        if (action == GestureAction.RIGHT) {
            return RESERVED_RIGHT_TEXT;
        }
        if (action == GestureAction.UP) {
            return RESERVED_UP_TEXT;
        }
        return RESERVED_TAP_TEXT;
    }

    private static String defaultReservedPhrase(GestureAction action) {
        return action == GestureAction.TAP ? DEFAULT_RESERVED_TAP_TEXT : "";
    }

    private static int migrateLegacyDefault(
            SharedPreferences prefs,
            String key,
            int value,
            int legacyDefault,
            int newDefault) {
        boolean explicitLegacyValue = prefs.contains(key) && value == legacyDefault;
        boolean explicitLegacyMarigoldTextValue = PRIMARY_TEXT_SIZE_PERCENT.equals(key)
                && prefs.contains(key)
                && value == 94;
        boolean legacySingleHeightValue = !prefs.contains(key)
                && prefs.contains(KEYBOARD_HEIGHT_DP)
                && isHeightKey(key)
                && value == LEGACY_DEFAULT_ENGLISH_HEIGHT_DP;
        return explicitLegacyValue || explicitLegacyMarigoldTextValue || legacySingleHeightValue
                ? newDefault
                : value;
    }

    private static int layoutPadding(
            SharedPreferences prefs,
            String key,
            String legacyKey,
            int legacyValue,
            int defaultValue) {
        if (prefs.contains(key)) {
            int value = prefs.getInt(key, defaultValue);
            return value == 0 ? defaultValue : value;
        }
        if (prefs.contains(legacyKey)) {
            return legacyValue == 0 ? defaultValue : legacyValue;
        }
        return defaultValue;
    }

    private static boolean isHeightKey(String key) {
        return HANGUL_KEYBOARD_HEIGHT_DP.equals(key) || ENGLISH_KEYBOARD_HEIGHT_DP.equals(key);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int hangulSpecialColumnPercent(SharedPreferences prefs, KeyboardSettings defaults) {
        if (prefs.contains(HANGUL_SPECIAL_COLUMN_PERCENT)) {
            return prefs.getInt(HANGUL_SPECIAL_COLUMN_PERCENT, defaults.hangulSpecialColumnPercent);
        }
        if (prefs.contains(HANGUL_MAIN_KEY_UNITS)) {
            return KeyboardSettings.specialPercentForMainRegionRatio(prefs.getInt(
                    HANGUL_MAIN_KEY_UNITS,
                    5));
        }
        return defaults.hangulSpecialColumnPercent;
    }

    private static boolean showHangulNumberRow(SharedPreferences prefs, KeyboardSettings defaults) {
        if (prefs.contains(SHOW_HANGUL_NUMBER_ROW)) {
            return prefs.getBoolean(SHOW_HANGUL_NUMBER_ROW, defaults.showHangulNumberRow);
        }
        if (prefs.contains(SHOW_NUMBER_ROW)) {
            return prefs.getBoolean(SHOW_NUMBER_ROW, defaults.showHangulNumberRow);
        }
        return defaults.showHangulNumberRow;
    }

    private static boolean showEnglishNumberRow(SharedPreferences prefs, KeyboardSettings defaults) {
        if (prefs.contains(SHOW_ENGLISH_NUMBER_ROW)) {
            return prefs.getBoolean(SHOW_ENGLISH_NUMBER_ROW, defaults.showEnglishNumberRow);
        }
        if (prefs.contains(SHOW_NUMBER_ROW)) {
            return prefs.getBoolean(SHOW_NUMBER_ROW, defaults.showEnglishNumberRow);
        }
        return defaults.showEnglishNumberRow;
    }
}
