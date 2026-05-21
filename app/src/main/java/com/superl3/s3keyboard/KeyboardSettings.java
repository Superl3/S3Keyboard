package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class KeyboardSettings {
    static final int DEFAULT_ENGLISH_HEIGHT_DP = 235;
    static final int DEFAULT_HANGUL_HEIGHT_DP = 260;
    static final int DEFAULT_HEIGHT_DP = DEFAULT_ENGLISH_HEIGHT_DP;
    static final int DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP = 46;
    static final int NUMBER_ROW_HEIGHT_DP = DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP;
    static final int MIN_HEIGHT_DP = 150;
    static final int MAX_HEIGHT_DP = 430;
    static final int MAX_MARGIN_DP = 64;
    static final int DEFAULT_HANGUL_LEFT_PADDING_DP = 6;
    static final int DEFAULT_HANGUL_RIGHT_PADDING_DP = 6;
    static final int DEFAULT_ENGLISH_LEFT_PADDING_DP = 6;
    static final int DEFAULT_ENGLISH_RIGHT_PADDING_DP = 6;
    static final int DEFAULT_HANGUL_MAIN_SPECIAL_GAP_DP = 8;
    static final int DEFAULT_KEYBOARD_TOP_PADDING_DP = 6;
    static final int DEFAULT_KEYBOARD_BOTTOM_PADDING_DP = 4;
    static final int DEFAULT_BOTTOM_ROW_TOP_PADDING_DP = 0;
    static final int DEFAULT_NUMBER_ROW_BOTTOM_GAP_DP = 8;
    static final int MAX_HANGUL_MAIN_SPECIAL_GAP_DP = 24;
    static final int MAX_KEYBOARD_TOP_PADDING_DP = 24;
    static final int MAX_KEYBOARD_BOTTOM_PADDING_DP = 24;
    static final int MAX_BOTTOM_ROW_TOP_PADDING_DP = 24;
    static final int MAX_NUMBER_ROW_BOTTOM_GAP_DP = 24;
    static final int DEFAULT_HIT_SLOP_DP = 8;
    static final int MIN_GESTURE_THRESHOLD_DP = 12;
    static final int MAX_GESTURE_THRESHOLD_DP = 64;
    static final int DEFAULT_GESTURE_THRESHOLD_DP = 22;
    static final int MIN_TOUCH_Y_OFFSET_DP = -24;
    static final int MAX_TOUCH_Y_OFFSET_DP = 24;
    static final int DEFAULT_TOUCH_Y_OFFSET_DP = 4;
    static final int DEFAULT_REPEAT_START_DELAY_MS = 420;
    static final int DEFAULT_REPEAT_INTERVAL_MS = 55;
    static final int MIN_REPEAT_START_DELAY_MS = 180;
    static final int MAX_REPEAT_START_DELAY_MS = 900;
    static final int MIN_REPEAT_INTERVAL_MS = 30;
    static final int MAX_REPEAT_INTERVAL_MS = 180;
    static final int DEFAULT_KEY_IDLE_COLOR = 0xFFF8F8F8;
    static final int DEFAULT_KEY_PRESSED_COLOR = 0xFFB2B2B2;
    static final int DEFAULT_KEYBOARD_BACKGROUND_COLOR = 0xFFEBEBEB;
    static final int DEFAULT_ACCENT_COLOR = 0xFF232323;
    static final int DEFAULT_SECONDARY_COLOR = 0xFF696969;
    static final int DEFAULT_FUNCTION_KEY_COLOR = 0xFFE7EAF0;
    static final int DEFAULT_ACCENT_KEY_COLOR = 0xFFDDE3EC;
    static final int DEFAULT_BORDER_COLOR = DEFAULT_SECONDARY_COLOR;
    static final int DEFAULT_KEY_BORDER_WIDTH_DP = 1;
    static final int DEFAULT_KEY_ROUNDNESS_DP = 0;
    static final int DEFAULT_KEY_GAP_DP = 5;
    static final boolean DEFAULT_KEY_DEPTH_ENABLED = true;
    static final int DEFAULT_KEY_DEPTH_DP = 3;
    static final int DEFAULT_DEPTH_COLOR = DEFAULT_BORDER_COLOR;
    static final boolean DEFAULT_CUSTOM_DEPTH_COLOR_ENABLED = false;
    static final String FONT_DEFAULT = "default";
    static final String FONT_NOTO_SANS_KR = "noto_sans_kr";
    static final String FONT_NOTO_SERIF_KR = "noto_serif_kr";
    static final String FONT_D2CODING = "d2coding";
    static final String DEFAULT_FONT_FAMILY = FONT_NOTO_SANS_KR;
    static final int DEFAULT_PRIMARY_TEXT_SIZE_PERCENT = 78;
    static final int DEFAULT_SECONDARY_TEXT_SIZE_PERCENT = 80;
    static final boolean DEFAULT_PRIMARY_TEXT_BOLD = true;
    static final boolean DEFAULT_PRIMARY_TEXT_ITALIC = false;
    static final boolean DEFAULT_SECONDARY_TEXT_BOLD = true;
    static final boolean DEFAULT_SECONDARY_TEXT_ITALIC = false;
    static final boolean DEFAULT_FOLLOW_THEME_TYPOGRAPHY = false;
    static final int MIN_TEXT_SIZE_PERCENT = 70;
    static final int MAX_TEXT_SIZE_PERCENT = 150;
    static final boolean DEFAULT_SHOW_HANGUL_SLIDE_HINTS = true;
    static final boolean DEFAULT_SHOW_ENGLISH_SLIDE_HINTS = true;
    static final boolean DEFAULT_SHOW_BEGINNER_TOOLTIP_PREVIEW = true;
    static final LegendStylePreset DEFAULT_LEGEND_STYLE_PRESET = LegendStylePreset.DEFAULT;
    static final boolean DEFAULT_POINT_KEYCAP_STYLE_ENABLED = true;
    static final String DEFAULT_MODIFIER_ICON_PACK_ID = ModifierIconCatalog.PACK_LINE_MONO;
    static final String DEFAULT_KEY_DISPLAY_PACK_ID = KeyDisplayOverridePackCatalog.PACK_NONE;
    static final boolean DEFAULT_REMOTE_MODE_ENABLED = false;
    static final RemoteKeyPreset DEFAULT_REMOTE_KEY_PRESET = RemoteKeyPreset.PC_KEYBOARD;
    static final RemoteImeShortcut DEFAULT_REMOTE_IME_SHORTCUT = RemoteImeShortcut.ALT_SHIFT;
    static final boolean DEFAULT_SHOW_HANGUL_NUMBER_ROW = false;
    static final boolean DEFAULT_SHOW_ENGLISH_NUMBER_ROW = true;
    static final AdditionalNumberRowColorMode DEFAULT_ADDITIONAL_NUMBER_ROW_COLOR_MODE =
            AdditionalNumberRowColorMode.FULL_MOD;
    static final int DEFAULT_HANGUL_SPECIAL_COLUMN_PERCENT = 17;
    static final int MIN_HANGUL_SPECIAL_COLUMN_PERCENT = 10;
    static final int MAX_HANGUL_SPECIAL_COLUMN_PERCENT = 30;
    static final int MAX_KEY_BORDER_WIDTH_DP = 8;
    static final int MAX_KEY_ROUNDNESS_DP = 24;
    static final int MAX_KEY_GAP_DP = 18;
    static final int MAX_KEY_DEPTH_DP = 8;

    final KeyboardMode keyboardMode;
    final HandednessMode handednessMode;
    final int leftMarginDp;
    final int rightMarginDp;
    final int hangulLeftPaddingDp;
    final int hangulRightPaddingDp;
    final int englishLeftPaddingDp;
    final int englishRightPaddingDp;
    final int keyboardHeightDp;
    final int hangulKeyboardHeightDp;
    final int englishKeyboardHeightDp;
    final boolean showHangulNumberRow;
    final boolean showEnglishNumberRow;
    final boolean forceNumberRow;
    final boolean showNumberRow;
    final AdditionalNumberRowColorMode additionalNumberRowColorMode;
    final boolean hapticFeedbackEnabled;
    final int hitSlopDp;
    final int gestureThresholdDp;
    final int touchYOffsetDp;
    final int repeatStartDelayMs;
    final int repeatIntervalMs;
    final boolean englishDoubleSpacePeriodEnabled;
    final String enterKeyLabel;
    final int keyIdleColor;
    final int keyPressedColor;
    final int keyboardBackgroundColor;
    final int accentColor;
    final int secondaryColor;
    final int functionKeyColor;
    final int accentKeyColor;
    final int borderColor;
    final int keyBorderWidthDp;
    final int keyRoundnessDp;
    final int keyGapDp;
    final boolean keyDepthEnabled;
    final int keyDepthDp;
    final boolean customDepthColorEnabled;
    final int depthColor;
    final String fontFamily;
    final int primaryTextSizePercent;
    final int secondaryTextSizePercent;
    final boolean primaryTextBold;
    final boolean primaryTextItalic;
    final boolean secondaryTextBold;
    final boolean secondaryTextItalic;
    final boolean followThemeTypography;
    final boolean showHangulSlideHints;
    final boolean showEnglishSlideHints;
    final boolean showBeginnerTooltipPreview;
    final int hangulSpecialColumnPercent;
    final int hangulMainSpecialGapDp;
    final int keyboardTopPaddingDp;
    final int keyboardBottomPaddingDp;
    final int bottomRowTopPaddingDp;
    final int numberRowBottomGapDp;
    final Map<String, Integer> keyColorOverrides;
    final LegendStylePreset legendStylePreset;
    final boolean pointKeycapStyleEnabled;
    final String modifierIconThemePackId;
    final String modifierIconOverridePackId;
    final String keyDisplayThemePackId;
    final String keyDisplayOverridePackId;
    final Map<String, KeyDisplayOverride> keyDisplayOverrides;
    final KeyboardVisualEffects visualEffects;
    final boolean remoteModeEnabled;
    final RemoteKeyPreset remoteKeyPreset;
    final RemoteImeShortcut remoteImeShortcut;

    KeyboardSettings(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int keyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            int keyBorderWidthDp,
            int keyRoundnessDp,
            int keyGapDp,
            boolean keyDepthEnabled,
            int keyDepthDp,
            boolean customDepthColorEnabled,
            int depthColor,
            String fontFamily,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview,
            int hangulSpecialColumnPercent) {
        this(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                DEFAULT_PRIMARY_TEXT_SIZE_PERCENT,
                DEFAULT_SECONDARY_TEXT_SIZE_PERCENT,
                DEFAULT_PRIMARY_TEXT_BOLD,
                DEFAULT_PRIMARY_TEXT_ITALIC,
                DEFAULT_SECONDARY_TEXT_BOLD,
                DEFAULT_SECONDARY_TEXT_ITALIC,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                DEFAULT_ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                Collections.emptyMap());
    }

    KeyboardSettings(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int hangulKeyboardHeightDp,
            int englishKeyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            int keyBorderWidthDp,
            int keyRoundnessDp,
            int keyGapDp,
            boolean keyDepthEnabled,
            int keyDepthDp,
            boolean customDepthColorEnabled,
            int depthColor,
            String fontFamily,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview,
            int hangulSpecialColumnPercent) {
        this(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                DEFAULT_PRIMARY_TEXT_SIZE_PERCENT,
                DEFAULT_SECONDARY_TEXT_SIZE_PERCENT,
                DEFAULT_PRIMARY_TEXT_BOLD,
                DEFAULT_PRIMARY_TEXT_ITALIC,
                DEFAULT_SECONDARY_TEXT_BOLD,
                DEFAULT_SECONDARY_TEXT_ITALIC,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                DEFAULT_ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                Collections.emptyMap());
    }

    private KeyboardSettings(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int hangulKeyboardHeightDp,
            int englishKeyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            int keyBorderWidthDp,
            int keyRoundnessDp,
            int keyGapDp,
            boolean keyDepthEnabled,
            int keyDepthDp,
            boolean customDepthColorEnabled,
            int depthColor,
            String fontFamily,
            int primaryTextSizePercent,
            int secondaryTextSizePercent,
            boolean primaryTextBold,
            boolean primaryTextItalic,
            boolean secondaryTextBold,
            boolean secondaryTextItalic,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview,
            int hangulSpecialColumnPercent,
            AdditionalNumberRowColorMode additionalNumberRowColorMode,
            Map<String, Integer> keyColorOverrides) {
        this(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                leftMarginDp,
                rightMarginDp,
                leftMarginDp,
                rightMarginDp,
                DEFAULT_HANGUL_MAIN_SPECIAL_GAP_DP,
                DEFAULT_KEYBOARD_TOP_PADDING_DP,
                DEFAULT_KEYBOARD_BOTTOM_PADDING_DP,
                DEFAULT_BOTTOM_ROW_TOP_PADDING_DP);
    }

    private KeyboardSettings(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int hangulKeyboardHeightDp,
            int englishKeyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            int keyBorderWidthDp,
            int keyRoundnessDp,
            int keyGapDp,
            boolean keyDepthEnabled,
            int keyDepthDp,
            boolean customDepthColorEnabled,
            int depthColor,
            String fontFamily,
            int primaryTextSizePercent,
            int secondaryTextSizePercent,
            boolean primaryTextBold,
            boolean primaryTextItalic,
            boolean secondaryTextBold,
            boolean secondaryTextItalic,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview,
            int hangulSpecialColumnPercent,
            AdditionalNumberRowColorMode additionalNumberRowColorMode,
            Map<String, Integer> keyColorOverrides,
            int hangulLeftPaddingDp,
            int hangulRightPaddingDp,
            int englishLeftPaddingDp,
            int englishRightPaddingDp,
            int hangulMainSpecialGapDp,
            int keyboardTopPaddingDp,
            int keyboardBottomPaddingDp,
            int bottomRowTopPaddingDp) {
        this(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                DEFAULT_NUMBER_ROW_BOTTOM_GAP_DP,
                DEFAULT_LEGEND_STYLE_PRESET,
                DEFAULT_POINT_KEYCAP_STYLE_ENABLED,
                DEFAULT_MODIFIER_ICON_PACK_ID,
                "",
                DEFAULT_KEY_DISPLAY_PACK_ID,
                "",
                Collections.emptyMap(),
                KeyboardVisualEffects.DEFAULT,
                DEFAULT_FOLLOW_THEME_TYPOGRAPHY,
                DEFAULT_REMOTE_MODE_ENABLED,
                DEFAULT_REMOTE_KEY_PRESET,
                DEFAULT_REMOTE_IME_SHORTCUT);
    }

    private KeyboardSettings(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int hangulKeyboardHeightDp,
            int englishKeyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            int keyBorderWidthDp,
            int keyRoundnessDp,
            int keyGapDp,
            boolean keyDepthEnabled,
            int keyDepthDp,
            boolean customDepthColorEnabled,
            int depthColor,
            String fontFamily,
            int primaryTextSizePercent,
            int secondaryTextSizePercent,
            boolean primaryTextBold,
            boolean primaryTextItalic,
            boolean secondaryTextBold,
            boolean secondaryTextItalic,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview,
            int hangulSpecialColumnPercent,
            AdditionalNumberRowColorMode additionalNumberRowColorMode,
            Map<String, Integer> keyColorOverrides,
            int hangulLeftPaddingDp,
            int hangulRightPaddingDp,
            int englishLeftPaddingDp,
            int englishRightPaddingDp,
            int hangulMainSpecialGapDp,
            int keyboardTopPaddingDp,
            int keyboardBottomPaddingDp,
            int bottomRowTopPaddingDp,
            int numberRowBottomGapDp,
            LegendStylePreset legendStylePreset,
            boolean pointKeycapStyleEnabled,
            String modifierIconThemePackId,
            String modifierIconOverridePackId,
            String keyDisplayThemePackId,
            String keyDisplayOverridePackId,
            Map<String, KeyDisplayOverride> keyDisplayOverrides,
            KeyboardVisualEffects visualEffects,
            boolean followThemeTypography,
            boolean remoteModeEnabled,
            RemoteKeyPreset remoteKeyPreset,
            RemoteImeShortcut remoteImeShortcut) {
        this.keyboardMode = keyboardMode == null ? KeyboardMode.HANGUL : keyboardMode;
        this.handednessMode = handednessMode == null ? HandednessMode.BALANCED : handednessMode;
        this.leftMarginDp = clamp(leftMarginDp, 0, MAX_MARGIN_DP);
        this.rightMarginDp = clamp(rightMarginDp, 0, MAX_MARGIN_DP);
        this.hangulLeftPaddingDp = clamp(hangulLeftPaddingDp, 0, MAX_MARGIN_DP);
        this.hangulRightPaddingDp = clamp(hangulRightPaddingDp, 0, MAX_MARGIN_DP);
        this.englishLeftPaddingDp = clamp(englishLeftPaddingDp, 0, MAX_MARGIN_DP);
        this.englishRightPaddingDp = clamp(englishRightPaddingDp, 0, MAX_MARGIN_DP);
        this.hangulKeyboardHeightDp = clamp(hangulKeyboardHeightDp, MIN_HEIGHT_DP, MAX_HEIGHT_DP);
        this.englishKeyboardHeightDp = clamp(englishKeyboardHeightDp, MIN_HEIGHT_DP, MAX_HEIGHT_DP);
        this.keyboardHeightDp = this.keyboardMode == KeyboardMode.ENGLISH
                ? this.englishKeyboardHeightDp
                : this.hangulKeyboardHeightDp;
        this.showHangulNumberRow = showHangulNumberRow;
        this.showEnglishNumberRow = showEnglishNumberRow;
        this.forceNumberRow = forceNumberRow;
        this.showNumberRow = forceNumberRow
                || remoteModeEnabled
                || (this.keyboardMode == KeyboardMode.ENGLISH ? showEnglishNumberRow : showHangulNumberRow);
        this.additionalNumberRowColorMode = additionalNumberRowColorMode == null
                ? DEFAULT_ADDITIONAL_NUMBER_ROW_COLOR_MODE
                : additionalNumberRowColorMode;
        this.hapticFeedbackEnabled = hapticFeedbackEnabled;
        this.hitSlopDp = clamp(hitSlopDp, 0, 32);
        this.gestureThresholdDp = clamp(
                gestureThresholdDp,
                MIN_GESTURE_THRESHOLD_DP,
                MAX_GESTURE_THRESHOLD_DP);
        this.touchYOffsetDp = clamp(touchYOffsetDp, MIN_TOUCH_Y_OFFSET_DP, MAX_TOUCH_Y_OFFSET_DP);
        this.repeatStartDelayMs = clamp(
                repeatStartDelayMs,
                MIN_REPEAT_START_DELAY_MS,
                MAX_REPEAT_START_DELAY_MS);
        this.repeatIntervalMs = clamp(
                repeatIntervalMs,
                MIN_REPEAT_INTERVAL_MS,
                MAX_REPEAT_INTERVAL_MS);
        this.englishDoubleSpacePeriodEnabled = englishDoubleSpacePeriodEnabled;
        this.enterKeyLabel = enterKeyLabel == null || enterKeyLabel.isEmpty() ? "\uC804\uC1A1" : enterKeyLabel;
        this.keyIdleColor = opaque(keyIdleColor);
        this.keyPressedColor = opaque(keyPressedColor);
        this.keyboardBackgroundColor = opaque(keyboardBackgroundColor);
        this.accentColor = opaque(accentColor);
        this.secondaryColor = opaque(secondaryColor);
        this.functionKeyColor = opaque(functionKeyColor);
        this.accentKeyColor = opaque(accentKeyColor);
        this.borderColor = opaque(borderColor);
        this.keyBorderWidthDp = clamp(keyBorderWidthDp, 0, MAX_KEY_BORDER_WIDTH_DP);
        this.keyRoundnessDp = clamp(keyRoundnessDp, 0, MAX_KEY_ROUNDNESS_DP);
        this.keyGapDp = clamp(keyGapDp, 0, MAX_KEY_GAP_DP);
        this.keyDepthEnabled = keyDepthEnabled;
        this.keyDepthDp = clamp(keyDepthDp, 0, MAX_KEY_DEPTH_DP);
        this.customDepthColorEnabled = customDepthColorEnabled;
        this.depthColor = opaque(depthColor);
        this.fontFamily = normalizeFontFamily(fontFamily);
        this.primaryTextSizePercent = clamp(
                primaryTextSizePercent,
                MIN_TEXT_SIZE_PERCENT,
                MAX_TEXT_SIZE_PERCENT);
        this.secondaryTextSizePercent = clamp(
                secondaryTextSizePercent,
                MIN_TEXT_SIZE_PERCENT,
                MAX_TEXT_SIZE_PERCENT);
        this.primaryTextBold = primaryTextBold;
        this.primaryTextItalic = primaryTextItalic;
        this.secondaryTextBold = secondaryTextBold;
        this.secondaryTextItalic = secondaryTextItalic;
        this.followThemeTypography = followThemeTypography;
        this.showHangulSlideHints = showHangulSlideHints;
        this.showEnglishSlideHints = showEnglishSlideHints;
        this.showBeginnerTooltipPreview = showBeginnerTooltipPreview;
        this.hangulSpecialColumnPercent = clamp(
                hangulSpecialColumnPercent,
                MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                MAX_HANGUL_SPECIAL_COLUMN_PERCENT);
        this.hangulMainSpecialGapDp = clamp(
                hangulMainSpecialGapDp,
                0,
                MAX_HANGUL_MAIN_SPECIAL_GAP_DP);
        this.keyboardTopPaddingDp = clamp(
                keyboardTopPaddingDp,
                0,
                MAX_KEYBOARD_TOP_PADDING_DP);
        this.keyboardBottomPaddingDp = clamp(
                keyboardBottomPaddingDp,
                0,
                MAX_KEYBOARD_BOTTOM_PADDING_DP);
        this.bottomRowTopPaddingDp = clamp(
                bottomRowTopPaddingDp,
                0,
                MAX_BOTTOM_ROW_TOP_PADDING_DP);
        this.numberRowBottomGapDp = clamp(
                numberRowBottomGapDp,
                0,
                MAX_NUMBER_ROW_BOTTOM_GAP_DP);
        this.keyColorOverrides = normalizeKeyColorOverrides(keyColorOverrides);
        this.legendStylePreset = legendStylePreset == null ? DEFAULT_LEGEND_STYLE_PRESET : legendStylePreset;
        this.pointKeycapStyleEnabled = pointKeycapStyleEnabled;
        this.modifierIconThemePackId = ModifierIconCatalog.normalizePackId(modifierIconThemePackId);
        this.modifierIconOverridePackId = normalizeModifierIconOverridePackId(modifierIconOverridePackId);
        this.keyDisplayThemePackId = KeyDisplayOverridePackCatalog.normalizePackId(keyDisplayThemePackId);
        this.keyDisplayOverridePackId =
                KeyDisplayOverridePackCatalog.normalizeOverridePackId(keyDisplayOverridePackId);
        this.keyDisplayOverrides = normalizeKeyDisplayOverrides(keyDisplayOverrides);
        this.visualEffects = visualEffects == null ? KeyboardVisualEffects.DEFAULT : visualEffects;
        this.remoteModeEnabled = remoteModeEnabled;
        this.remoteKeyPreset = remoteKeyPreset == null ? DEFAULT_REMOTE_KEY_PRESET : remoteKeyPreset;
        this.remoteImeShortcut = remoteImeShortcut == null ? DEFAULT_REMOTE_IME_SHORTCUT : remoteImeShortcut;
    }

    static KeyboardSettings defaults() {
        return new KeyboardSettings(
                KeyboardMode.HANGUL,
                HandednessMode.BALANCED,
                DEFAULT_HANGUL_LEFT_PADDING_DP,
                DEFAULT_HANGUL_RIGHT_PADDING_DP,
                DEFAULT_HANGUL_HEIGHT_DP,
                DEFAULT_ENGLISH_HEIGHT_DP,
                DEFAULT_SHOW_HANGUL_NUMBER_ROW,
                DEFAULT_SHOW_ENGLISH_NUMBER_ROW,
                false,
                true,
                DEFAULT_HIT_SLOP_DP,
                DEFAULT_GESTURE_THRESHOLD_DP,
                DEFAULT_TOUCH_Y_OFFSET_DP,
                DEFAULT_REPEAT_START_DELAY_MS,
                DEFAULT_REPEAT_INTERVAL_MS,
                true,
                "\uC804\uC1A1",
                DEFAULT_KEY_IDLE_COLOR,
                DEFAULT_KEY_PRESSED_COLOR,
                DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                DEFAULT_ACCENT_COLOR,
                DEFAULT_SECONDARY_COLOR,
                DEFAULT_FUNCTION_KEY_COLOR,
                DEFAULT_ACCENT_KEY_COLOR,
                DEFAULT_BORDER_COLOR,
                DEFAULT_KEY_BORDER_WIDTH_DP,
                DEFAULT_KEY_ROUNDNESS_DP,
                DEFAULT_KEY_GAP_DP,
                DEFAULT_KEY_DEPTH_ENABLED,
                DEFAULT_KEY_DEPTH_DP,
                DEFAULT_CUSTOM_DEPTH_COLOR_ENABLED,
                DEFAULT_DEPTH_COLOR,
                DEFAULT_FONT_FAMILY,
                DEFAULT_PRIMARY_TEXT_SIZE_PERCENT,
                DEFAULT_SECONDARY_TEXT_SIZE_PERCENT,
                DEFAULT_PRIMARY_TEXT_BOLD,
                DEFAULT_PRIMARY_TEXT_ITALIC,
                DEFAULT_SECONDARY_TEXT_BOLD,
                DEFAULT_SECONDARY_TEXT_ITALIC,
                DEFAULT_SHOW_HANGUL_SLIDE_HINTS,
                DEFAULT_SHOW_ENGLISH_SLIDE_HINTS,
                DEFAULT_SHOW_BEGINNER_TOOLTIP_PREVIEW,
                DEFAULT_HANGUL_SPECIAL_COLUMN_PERCENT,
                DEFAULT_ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                Collections.emptyMap());
    }

    private KeyboardSettings copy(
            KeyboardMode keyboardMode,
            HandednessMode handednessMode,
            int leftMarginDp,
            int rightMarginDp,
            int keyboardHeightDp,
            boolean showHangulNumberRow,
            boolean showEnglishNumberRow,
            boolean forceNumberRow,
            boolean hapticFeedbackEnabled,
            int hitSlopDp,
            int gestureThresholdDp,
            int touchYOffsetDp,
            int repeatStartDelayMs,
            int repeatIntervalMs,
            boolean englishDoubleSpacePeriodEnabled,
            String enterKeyLabel,
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int keyRoundnessDp,
            int keyBorderWidthDp,
            int keyGapDp,
            int hangulSpecialColumnPercent) {
        int nextHangulKeyboardHeightDp = keyboardMode == KeyboardMode.HANGUL
                ? keyboardHeightDp
                : hangulKeyboardHeightDp;
        int nextEnglishKeyboardHeightDp = keyboardMode == KeyboardMode.ENGLISH
                ? keyboardHeightDp
                : englishKeyboardHeightDp;
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                nextHangulKeyboardHeightDp,
                nextEnglishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withKeyboardMode(KeyboardMode mode) {
        return copy(
                mode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                activeHeightForMode(mode),
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withRemoteOptions(
            boolean remoteModeEnabled,
            RemoteKeyPreset remoteKeyPreset,
            RemoteImeShortcut remoteImeShortcut) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withMargins(int leftMarginDp, int rightMarginDp) {
        if (handednessMode == HandednessMode.LEFT) {
            return withAllSidePadding(leftMarginDp, presetRightMargin(HandednessMode.LEFT));
        }
        if (handednessMode == HandednessMode.RIGHT) {
            return withAllSidePadding(presetLeftMargin(HandednessMode.RIGHT), rightMarginDp);
        }
        int sharedMarginDp = Math.max(leftMarginDp, rightMarginDp);
        return withAllSidePadding(sharedMarginDp, sharedMarginDp);
    }

    KeyboardSettings withSharedMargin(int marginDp) {
        return withAllSidePadding(marginDp, marginDp);
    }

    KeyboardSettings withAllSidePadding(int leftPaddingDp, int rightPaddingDp) {
        return withLayoutSpacing(
                leftPaddingDp,
                rightPaddingDp,
                leftPaddingDp,
                rightPaddingDp,
                leftPaddingDp,
                rightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp);
    }

    KeyboardSettings withHangulSidePadding(int leftPaddingDp, int rightPaddingDp) {
        return withLayoutSpacing(
                leftMarginDp,
                rightMarginDp,
                leftPaddingDp,
                rightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp);
    }

    KeyboardSettings withEnglishSidePadding(int leftPaddingDp, int rightPaddingDp) {
        return withLayoutSpacing(
                leftMarginDp,
                rightMarginDp,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                leftPaddingDp,
                rightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp);
    }

    KeyboardSettings withLayoutSpacing(
            int hangulMainSpecialGapDp,
            int keyboardBottomPaddingDp,
            int bottomRowTopPaddingDp) {
        return withLayoutSpacing(
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp);
    }

    KeyboardSettings withLayoutSpacing(
            int hangulMainSpecialGapDp,
            int keyboardTopPaddingDp,
            int keyboardBottomPaddingDp,
            int bottomRowTopPaddingDp) {
        return withLayoutSpacing(
                leftMarginDp,
                rightMarginDp,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp);
    }

    private KeyboardSettings withLayoutSpacing(
            int leftMarginDp,
            int rightMarginDp,
            int hangulLeftPaddingDp,
            int hangulRightPaddingDp,
            int englishLeftPaddingDp,
            int englishRightPaddingDp,
            int hangulMainSpecialGapDp,
            int keyboardTopPaddingDp,
            int keyboardBottomPaddingDp,
            int bottomRowTopPaddingDp) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withNumberRowBottomGap(int numberRowBottomGapDp) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withHeight(int keyboardHeightDp) {
        return withHeights(keyboardHeightDp, keyboardHeightDp);
    }

    KeyboardSettings withHangulHeight(int hangulKeyboardHeightDp) {
        return withHeights(hangulKeyboardHeightDp, englishKeyboardHeightDp);
    }

    KeyboardSettings withEnglishHeight(int englishKeyboardHeightDp) {
        return withHeights(hangulKeyboardHeightDp, englishKeyboardHeightDp);
    }

    KeyboardSettings withHeights(int hangulKeyboardHeightDp, int englishKeyboardHeightDp) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withActiveHeight(int keyboardHeightDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withNumberRow(boolean showNumberRow) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showNumberRow,
                showNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withHangulNumberRow(boolean showHangulNumberRow) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withEnglishNumberRow(boolean showEnglishNumberRow) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withNumberRows(boolean showHangulNumberRow, boolean showEnglishNumberRow) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode mode) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                mode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withRuntimeNumberRowForced(boolean forceNumberRow) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withHandednessPreset(HandednessMode mode) {
        HandednessMode normalizedMode = mode == null ? HandednessMode.BALANCED : mode;
        int presetLeftPaddingDp = presetLeftMargin(normalizedMode);
        int presetRightPaddingDp = presetRightMargin(normalizedMode);
        return copy(
                keyboardMode,
                normalizedMode,
                presetLeftPaddingDp,
                presetRightPaddingDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent)
                .withAllSidePadding(presetLeftPaddingDp, presetRightPaddingDp);
    }

    KeyboardSettings withHapticFeedback(boolean enabled) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                enabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withGestureThreshold(int thresholdDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                thresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withTouchYOffset(int offsetDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                offsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withRepeatTiming(int repeatStartDelayMs, int repeatIntervalMs) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withEnglishDoubleSpacePeriod(boolean enabled) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                enabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withEnterKeyLabel(String label) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                label,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withThemeColors(
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withModifierKeyColor(int modifierKeyColor) {
        return withExtendedThemeColors(
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                modifierKeyColor,
                accentKeyColor,
                borderColor,
                customDepthColorEnabled,
                depthColor);
    }

    KeyboardSettings withExtendedThemeColors(
            int keyIdleColor,
            int keyPressedColor,
            int keyboardBackgroundColor,
            int accentColor,
            int secondaryColor,
            int functionKeyColor,
            int accentKeyColor,
            int borderColor,
            boolean customDepthColorEnabled,
            int depthColor) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withKeyRoundness(int keyRoundnessDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withKeyBorderWidth(int keyBorderWidthDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withKeyGap(int keyGapDp) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withKeyDepth(boolean keyDepthEnabled, int keyDepthDp) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withDepthColor(boolean customDepthColorEnabled, int depthColor) {
        return withExtendedThemeColors(
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                customDepthColorEnabled,
                depthColor);
    }

    KeyboardSettings withFontFamily(String fontFamily) {
        return withTypography(
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic);
    }

    KeyboardSettings withTypography(
            String fontFamily,
            int primaryTextSizePercent,
            int secondaryTextSizePercent,
            boolean primaryTextBold,
            boolean primaryTextItalic,
            boolean secondaryTextBold,
            boolean secondaryTextItalic) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withHintVisibility(
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            boolean showBeginnerTooltipPreview) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withKeyColorOverrides(Map<String, Integer> keyColorOverrides) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withLegendStyle(LegendStylePreset legendStylePreset) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withFollowThemeTypography(boolean followThemeTypography) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withPointKeycapStyle(boolean enabled) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                enabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withModifierIconThemePack(String packId) {
        return withModifierIconPacks(packId, modifierIconOverridePackId);
    }

    KeyboardSettings withModifierIconOverridePack(String packId) {
        return withModifierIconPacks(modifierIconThemePackId, packId);
    }

    KeyboardSettings withModifierIconPacks(String themePackId, String overridePackId) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                themePackId,
                overridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withKeyDisplayThemePack(String packId) {
        return withKeyDisplayPacks(packId, keyDisplayOverridePackId);
    }

    KeyboardSettings withKeyDisplayOverridePack(String packId) {
        return withKeyDisplayPacks(keyDisplayThemePackId, packId);
    }

    KeyboardSettings withKeyDisplayPacks(String themePackId, String overridePackId) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                themePackId,
                overridePackId,
                keyDisplayOverrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withKeyDisplayOverrides(Map<String, KeyDisplayOverride> overrides) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                overrides,
                visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withVisualEffects(KeyboardVisualEffects effects) {
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                functionKeyColor,
                accentKeyColor,
                borderColor,
                keyBorderWidthDp,
                keyRoundnessDp,
                keyGapDp,
                keyDepthEnabled,
                keyDepthDp,
                customDepthColorEnabled,
                depthColor,
                fontFamily,
                primaryTextSizePercent,
                secondaryTextSizePercent,
                primaryTextBold,
                primaryTextItalic,
                secondaryTextBold,
                secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                additionalNumberRowColorMode,
                keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                legendStylePreset,
                pointKeycapStyleEnabled,
                modifierIconThemePackId,
                modifierIconOverridePackId,
                keyDisplayThemePackId,
                keyDisplayOverridePackId,
                keyDisplayOverrides,
                effects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withAppearanceFrom(KeyboardSettings appearance) {
        KeyboardSettings theme = appearance == null ? defaults() : appearance;
        KeyboardSettings typography = followThemeTypography ? theme : this;
        return new KeyboardSettings(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                hangulKeyboardHeightDp,
                englishKeyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                theme.keyIdleColor,
                theme.keyPressedColor,
                theme.keyboardBackgroundColor,
                theme.accentColor,
                theme.secondaryColor,
                theme.functionKeyColor,
                theme.accentKeyColor,
                theme.borderColor,
                theme.keyBorderWidthDp,
                theme.keyRoundnessDp,
                theme.keyGapDp,
                theme.keyDepthEnabled,
                theme.keyDepthDp,
                theme.customDepthColorEnabled,
                theme.depthColor,
                typography.fontFamily,
                typography.primaryTextSizePercent,
                typography.secondaryTextSizePercent,
                typography.primaryTextBold,
                typography.primaryTextItalic,
                typography.secondaryTextBold,
                typography.secondaryTextItalic,
                showHangulSlideHints,
                showEnglishSlideHints,
                showBeginnerTooltipPreview,
                hangulSpecialColumnPercent,
                theme.additionalNumberRowColorMode,
                theme.keyColorOverrides,
                hangulLeftPaddingDp,
                hangulRightPaddingDp,
                englishLeftPaddingDp,
                englishRightPaddingDp,
                hangulMainSpecialGapDp,
                keyboardTopPaddingDp,
                keyboardBottomPaddingDp,
                bottomRowTopPaddingDp,
                numberRowBottomGapDp,
                theme.legendStylePreset,
                theme.pointKeycapStyleEnabled,
                theme.modifierIconThemePackId,
                theme.modifierIconOverridePackId,
                theme.keyDisplayThemePackId,
                theme.keyDisplayOverridePackId,
                theme.keyDisplayOverrides,
                theme.visualEffects,
                followThemeTypography,
                remoteModeEnabled,
                remoteKeyPreset,
                remoteImeShortcut);
    }

    KeyboardSettings withHangulSpecialColumnPercent(int hangulSpecialColumnPercent) {
        return copy(
                keyboardMode,
                handednessMode,
                leftMarginDp,
                rightMarginDp,
                keyboardHeightDp,
                showHangulNumberRow,
                showEnglishNumberRow,
                forceNumberRow,
                hapticFeedbackEnabled,
                hitSlopDp,
                gestureThresholdDp,
                touchYOffsetDp,
                repeatStartDelayMs,
                repeatIntervalMs,
                englishDoubleSpacePeriodEnabled,
                enterKeyLabel,
                keyIdleColor,
                keyPressedColor,
                keyboardBackgroundColor,
                accentColor,
                secondaryColor,
                keyRoundnessDp,
                keyBorderWidthDp,
                keyGapDp,
                hangulSpecialColumnPercent);
    }

    KeyboardSettings withHangulMainKeyUnits(int mainRegionRatio) {
        return withHangulSpecialColumnPercent(specialPercentForMainRegionRatio(mainRegionRatio));
    }

    int activeLeftPaddingDp() {
        return keyboardMode == KeyboardMode.ENGLISH ? englishLeftPaddingDp : hangulLeftPaddingDp;
    }

    int activeRightPaddingDp() {
        return keyboardMode == KeyboardMode.ENGLISH ? englishRightPaddingDp : hangulRightPaddingDp;
    }

    int measuredHeightDp() {
        return keyboardHeightDp + (showNumberRow ? NUMBER_ROW_HEIGHT_DP + numberRowBottomGapDp : 0);
    }

    private int activeHeightForMode(KeyboardMode mode) {
        KeyboardMode normalizedMode = mode == null ? KeyboardMode.HANGUL : mode;
        return normalizedMode == KeyboardMode.ENGLISH
                ? englishKeyboardHeightDp
                : hangulKeyboardHeightDp;
    }

    static int specialPercentForMainRegionRatio(int mainRegionRatio) {
        int clampedRatio = clamp(mainRegionRatio, 4, 5);
        return Math.round(100f / (clampedRatio + 1));
    }

    static String normalizeFontFamily(String fontFamily) {
        if (fontFamily == null || fontFamily.isEmpty()) {
            return DEFAULT_FONT_FAMILY;
        }
        switch (fontFamily) {
            case FONT_DEFAULT:
            case FONT_NOTO_SANS_KR:
            case FONT_NOTO_SERIF_KR:
            case FONT_D2CODING:
                return fontFamily;
            default:
                return DEFAULT_FONT_FAMILY;
        }
    }

    static String normalizeKeyOverrideName(String key) {
        return key == null ? "" : key.trim().toLowerCase().replaceAll("\\s+", "");
    }

    private static Map<String, Integer> normalizeKeyColorOverrides(Map<String, Integer> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> normalized = new HashMap<>();
        for (Map.Entry<String, Integer> entry : overrides.entrySet()) {
            String key = normalizeKeyOverrideName(entry.getKey());
            Integer value = entry.getValue();
            if (!key.isEmpty() && value != null) {
                normalized.put(key, opaque(value));
            }
        }
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static Map<String, KeyDisplayOverride> normalizeKeyDisplayOverrides(
            Map<String, KeyDisplayOverride> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, KeyDisplayOverride> normalized = new HashMap<>();
        for (Map.Entry<String, KeyDisplayOverride> entry : overrides.entrySet()) {
            String key = normalizeKeyOverrideName(entry.getKey());
            KeyDisplayOverride value = entry.getValue();
            if (!key.isEmpty() && value != null && !value.value.isEmpty()) {
                normalized.put(key, value);
            }
        }
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static String normalizeModifierIconOverridePackId(String packId) {
        if (packId == null || packId.isEmpty() || ModifierIconCatalog.PACK_THEME_DEFAULT.equals(packId)) {
            return "";
        }
        return ModifierIconCatalog.normalizePackId(packId);
    }

    private static int presetLeftMargin(HandednessMode mode) {
        return mode == HandednessMode.RIGHT ? 56 : DEFAULT_HANGUL_LEFT_PADDING_DP;
    }

    private static int presetRightMargin(HandednessMode mode) {
        return mode == HandednessMode.LEFT ? 56 : DEFAULT_HANGUL_RIGHT_PADDING_DP;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int opaque(int color) {
        return 0xFF000000 | (color & 0x00FFFFFF);
    }
}
