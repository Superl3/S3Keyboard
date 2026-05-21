package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardSettingsTest {
    @Test
    public void defaultsExposeVisualCustomizationValues() {
        KeyboardSettings settings = KeyboardSettings.defaults();

        assertEquals(KeyboardSettings.DEFAULT_KEY_IDLE_COLOR, settings.keyIdleColor);
        assertEquals(KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR, settings.keyPressedColor);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR, settings.keyboardBackgroundColor);
        assertEquals(KeyboardSettings.DEFAULT_ACCENT_COLOR, settings.accentColor);
        assertEquals(KeyboardSettings.DEFAULT_SECONDARY_COLOR, settings.secondaryColor);
        assertEquals(KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR, settings.functionKeyColor);
        assertEquals(KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR, settings.functionKeyColor);
        assertEquals(KeyboardSettings.DEFAULT_ACCENT_KEY_COLOR, settings.accentKeyColor);
        assertEquals(KeyboardSettings.DEFAULT_BORDER_COLOR, settings.borderColor);
        assertEquals(KeyboardSettings.DEFAULT_KEY_BORDER_WIDTH_DP, settings.keyBorderWidthDp);
        assertEquals(KeyboardSettings.DEFAULT_KEY_ROUNDNESS_DP, settings.keyRoundnessDp);
        assertEquals(KeyboardSettings.DEFAULT_KEY_GAP_DP, settings.keyGapDp);
        assertEquals(KeyboardSettings.DEFAULT_KEY_DEPTH_ENABLED, settings.keyDepthEnabled);
        assertEquals(KeyboardSettings.DEFAULT_KEY_DEPTH_DP, settings.keyDepthDp);
        assertEquals(KeyboardSettings.DEFAULT_CUSTOM_DEPTH_COLOR_ENABLED, settings.customDepthColorEnabled);
        assertEquals(KeyboardSettings.DEFAULT_DEPTH_COLOR, settings.depthColor);
        assertEquals(KeyboardSettings.DEFAULT_FONT_FAMILY, settings.fontFamily);
        assertEquals(KeyboardSettings.DEFAULT_PRIMARY_TEXT_SIZE_PERCENT, settings.primaryTextSizePercent);
        assertEquals(KeyboardSettings.DEFAULT_SECONDARY_TEXT_SIZE_PERCENT, settings.secondaryTextSizePercent);
        assertEquals(KeyboardSettings.DEFAULT_PRIMARY_TEXT_BOLD, settings.primaryTextBold);
        assertEquals(KeyboardSettings.DEFAULT_PRIMARY_TEXT_ITALIC, settings.primaryTextItalic);
        assertEquals(KeyboardSettings.DEFAULT_SECONDARY_TEXT_BOLD, settings.secondaryTextBold);
        assertEquals(KeyboardSettings.DEFAULT_SECONDARY_TEXT_ITALIC, settings.secondaryTextItalic);
        assertEquals(KeyboardSettings.DEFAULT_FOLLOW_THEME_TYPOGRAPHY, settings.followThemeTypography);
        assertEquals(KeyboardSettings.DEFAULT_SHOW_HANGUL_SLIDE_HINTS, settings.showHangulSlideHints);
        assertEquals(KeyboardSettings.DEFAULT_SHOW_ENGLISH_SLIDE_HINTS, settings.showEnglishSlideHints);
        assertEquals(LegendStylePreset.DEFAULT, settings.legendStylePreset);
        assertEquals(
                KeyboardSettings.DEFAULT_SHOW_BEGINNER_TOOLTIP_PREVIEW,
                settings.showBeginnerTooltipPreview);
        assertEquals(KeyboardSettings.DEFAULT_SHOW_HANGUL_NUMBER_ROW, settings.showHangulNumberRow);
        assertEquals(KeyboardSettings.DEFAULT_SHOW_ENGLISH_NUMBER_ROW, settings.showEnglishNumberRow);
        assertFalse(settings.remoteModeEnabled);
        assertEquals(RemoteKeyPreset.PC_KEYBOARD, settings.remoteKeyPreset);
        assertEquals(RemoteImeShortcut.ALT_SHIFT, settings.remoteImeShortcut);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_HEIGHT_DP, settings.hangulKeyboardHeightDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_HEIGHT_DP, settings.englishKeyboardHeightDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_HEIGHT_DP, settings.keyboardHeightDp);
        assertTrue(settings.hangulKeyboardHeightDp < 390 * 0.70f);
        assertEquals(235, settings.englishKeyboardHeightDp);
        assertEquals(
                KeyboardSettings.DEFAULT_HANGUL_SPECIAL_COLUMN_PERCENT,
                settings.hangulSpecialColumnPercent);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, settings.hangulLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, settings.hangulRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_LEFT_PADDING_DP, settings.englishLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_RIGHT_PADDING_DP, settings.englishRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_MAIN_SPECIAL_GAP_DP, settings.hangulMainSpecialGapDp);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_TOP_PADDING_DP, settings.keyboardTopPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_BOTTOM_PADDING_DP, settings.keyboardBottomPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_BOTTOM_ROW_TOP_PADDING_DP, settings.bottomRowTopPaddingDp);
        assertEquals(4, settings.keyboardBottomPaddingDp);
        assertEquals(0, settings.bottomRowTopPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_GESTURE_THRESHOLD_DP, settings.gestureThresholdDp);
        assertEquals(4, settings.touchYOffsetDp);
        assertEquals(430, KeyboardSettings.MAX_HEIGHT_DP);
    }

    @Test
    public void touchYOffsetCanBeAdjustedAndClamped() {
        assertEquals(KeyboardSettings.MAX_TOUCH_Y_OFFSET_DP,
                KeyboardSettings.defaults().withTouchYOffset(99).touchYOffsetDp);
        assertEquals(KeyboardSettings.MIN_TOUCH_Y_OFFSET_DP,
                KeyboardSettings.defaults().withTouchYOffset(-99).touchYOffsetDp);
        assertEquals(-6, KeyboardSettings.defaults().withTouchYOffset(-6).touchYOffsetDp);
    }

    @Test
    public void remoteOptionsPersistAcrossOtherSettingCopies() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.WIN_SPACE)
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withKeyGap(9)
                .withEnterKeyLabel("Enter");

        assertTrue(settings.remoteModeEnabled);
        assertEquals(RemoteKeyPreset.PC_KEYBOARD, settings.remoteKeyPreset);
        assertEquals(RemoteImeShortcut.WIN_SPACE, settings.remoteImeShortcut);
    }

    @Test
    public void remoteModeForcesNumberRowWithoutChangingStoredNumberRowToggles() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulNumberRow(false)
                .withEnglishNumberRow(false)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);

        assertTrue(settings.showNumberRow);
        assertFalse(settings.showHangulNumberRow);
        assertFalse(settings.showEnglishNumberRow);

        KeyboardSettings restored = settings.withRemoteOptions(
                false,
                RemoteKeyPreset.PC_KEYBOARD,
                RemoteImeShortcut.ALT_SHIFT);

        assertFalse(restored.showNumberRow);
        assertFalse(restored.showHangulNumberRow);
        assertFalse(restored.showEnglishNumberRow);
    }

    @Test
    public void themeColorsCanChangeWithoutChangingInputSettings() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withThemeColors(0x00111111, 0x00222222, 0x00333333, 0x00444444, 0x00555555);

        assertEquals(KeyboardMode.ENGLISH, settings.keyboardMode);
        assertEquals(0xFF111111, settings.keyIdleColor);
        assertEquals(0xFF222222, settings.keyPressedColor);
        assertEquals(0xFF333333, settings.keyboardBackgroundColor);
        assertEquals(0xFF444444, settings.accentColor);
        assertEquals(0xFF555555, settings.secondaryColor);
        assertEquals(KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR, settings.functionKeyColor);
        assertEquals(KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR, settings.functionKeyColor);
    }

    @Test
    public void modifierKeyColorCanChangeWithoutChangingInputSettings() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withModifierKeyColor(0x00666666);

        assertEquals(KeyboardMode.ENGLISH, settings.keyboardMode);
        assertEquals(0xFF666666, settings.functionKeyColor);
        assertEquals(KeyboardSettings.DEFAULT_KEY_IDLE_COLOR, settings.keyIdleColor);
    }

    @Test
    public void extendedThemeValuesCanChangeWithoutChangingInputSettings() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withExtendedThemeColors(
                        0x00111111,
                        0x00222222,
                        0x00333333,
                        0x00444444,
                        0x00555555,
                        0x00666666,                        0x00888888,
                        0x00999999,
                        true,
                        0x00AAAAAA)
                .withTypography(
                        KeyboardSettings.FONT_D2CODING,
                        116,
                        91,
                        false,
                        true,
                        true,
                        true)
                .withHintVisibility(false, true, false);

        assertEquals(KeyboardMode.ENGLISH, settings.keyboardMode);
        assertEquals(0xFF888888, settings.accentKeyColor);
        assertEquals(0xFF999999, settings.borderColor);
        assertEquals(true, settings.customDepthColorEnabled);
        assertEquals(0xFFAAAAAA, settings.depthColor);
        assertEquals(KeyboardSettings.FONT_D2CODING, settings.fontFamily);
        assertEquals(116, settings.primaryTextSizePercent);
        assertEquals(91, settings.secondaryTextSizePercent);
        assertEquals(false, settings.primaryTextBold);
        assertEquals(true, settings.primaryTextItalic);
        assertEquals(true, settings.secondaryTextBold);
        assertEquals(true, settings.secondaryTextItalic);
        assertEquals(false, settings.showHangulSlideHints);
        assertEquals(true, settings.showEnglishSlideHints);
        assertEquals(false, settings.showBeginnerTooltipPreview);
    }

    @Test
    public void unsupportedFontFallsBackToDefault() {
        assertEquals(
                KeyboardSettings.DEFAULT_FONT_FAMILY,
                KeyboardSettings.defaults().withFontFamily("unknown").fontFamily);
    }

    @Test
    public void roundnessGapAndDepthAreClamped() {
        KeyboardSettings tooLarge = KeyboardSettings.defaults()
                .withKeyRoundness(999)
                .withKeyBorderWidth(999)
                .withKeyGap(999)
                .withKeyDepth(true, 999);
        KeyboardSettings tooSmall = KeyboardSettings.defaults()
                .withKeyRoundness(-1)
                .withKeyBorderWidth(-1)
                .withKeyGap(-1)
                .withKeyDepth(false, -1);

        assertEquals(KeyboardSettings.MAX_KEY_ROUNDNESS_DP, tooLarge.keyRoundnessDp);
        assertEquals(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP, tooLarge.keyBorderWidthDp);
        assertEquals(KeyboardSettings.MAX_KEY_GAP_DP, tooLarge.keyGapDp);
        assertEquals(KeyboardSettings.MAX_KEY_DEPTH_DP, tooLarge.keyDepthDp);
        assertEquals(true, tooLarge.keyDepthEnabled);
        assertEquals(0, tooSmall.keyRoundnessDp);
        assertEquals(0, tooSmall.keyBorderWidthDp);
        assertEquals(0, tooSmall.keyGapDp);
        assertEquals(0, tooSmall.keyDepthDp);
        assertEquals(false, tooSmall.keyDepthEnabled);
    }

    @Test
    public void hangulSpecialColumnPercentIsClampedToCustomizationRange() {
        assertEquals(
                KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                KeyboardSettings.defaults().withHangulSpecialColumnPercent(0).hangulSpecialColumnPercent);
        assertEquals(
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT,
                KeyboardSettings.defaults().withHangulSpecialColumnPercent(99).hangulSpecialColumnPercent);
        assertEquals(17, KeyboardSettings.defaults().withHangulSpecialColumnPercent(17).hangulSpecialColumnPercent);
        assertEquals(20, KeyboardSettings.defaults().withHangulSpecialColumnPercent(20).hangulSpecialColumnPercent);
    }

    @Test
    public void legacyMainRegionRatioMapsToSpecialColumnPercent() {
        assertEquals(20, KeyboardSettings.defaults().withHangulMainKeyUnits(4).hangulSpecialColumnPercent);
        assertEquals(17, KeyboardSettings.defaults().withHangulMainKeyUnits(5).hangulSpecialColumnPercent);
    }

    @Test
    public void layoutSpacingValuesAreIndependentAndClamped() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulSidePadding(11, 12)
                .withEnglishSidePadding(21, 22)
                .withLayoutSpacing(999, 999, 999, 999);
        KeyboardSettings tooSmall = KeyboardSettings.defaults()
                .withHangulSidePadding(-1, -2)
                .withEnglishSidePadding(-3, -4)
                .withLayoutSpacing(-5, -6, -7, -8);

        assertEquals(11, settings.hangulLeftPaddingDp);
        assertEquals(12, settings.hangulRightPaddingDp);
        assertEquals(21, settings.englishLeftPaddingDp);
        assertEquals(22, settings.englishRightPaddingDp);
        assertEquals(KeyboardSettings.MAX_HANGUL_MAIN_SPECIAL_GAP_DP, settings.hangulMainSpecialGapDp);
        assertEquals(KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP, settings.keyboardTopPaddingDp);
        assertEquals(KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP, settings.keyboardBottomPaddingDp);
        assertEquals(KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP, settings.bottomRowTopPaddingDp);
        assertEquals(
                KeyboardSettings.MAX_NUMBER_ROW_BOTTOM_GAP_DP,
                settings.withNumberRowBottomGap(999).numberRowBottomGapDp);
        assertEquals(0, tooSmall.hangulLeftPaddingDp);
        assertEquals(0, tooSmall.hangulRightPaddingDp);
        assertEquals(0, tooSmall.englishLeftPaddingDp);
        assertEquals(0, tooSmall.englishRightPaddingDp);
        assertEquals(0, tooSmall.hangulMainSpecialGapDp);
        assertEquals(0, tooSmall.keyboardTopPaddingDp);
        assertEquals(0, tooSmall.keyboardBottomPaddingDp);
        assertEquals(0, tooSmall.bottomRowTopPaddingDp);
        assertEquals(0, tooSmall.withNumberRowBottomGap(-9).numberRowBottomGapDp);
    }

    @Test
    public void legacyMarginSetterUpdatesBothLayoutSidePaddings() {
        KeyboardSettings settings = KeyboardSettings.defaults().withMargins(13, 17);

        assertEquals(17, settings.leftMarginDp);
        assertEquals(17, settings.rightMarginDp);
        assertEquals(17, settings.hangulLeftPaddingDp);
        assertEquals(17, settings.hangulRightPaddingDp);
        assertEquals(17, settings.englishLeftPaddingDp);
        assertEquals(17, settings.englishRightPaddingDp);
    }

    @Test
    public void sharedMarginCanBeReducedToZero() {
        KeyboardSettings settings = KeyboardSettings.defaults().withSharedMargin(12).withSharedMargin(0);

        assertEquals(0, settings.leftMarginDp);
        assertEquals(0, settings.rightMarginDp);
        assertEquals(0, settings.hangulLeftPaddingDp);
        assertEquals(0, settings.hangulRightPaddingDp);
        assertEquals(0, settings.englishLeftPaddingDp);
        assertEquals(0, settings.englishRightPaddingDp);
    }

    @Test
    public void oneHandedPresetUsesPresetSidePadding() {
        KeyboardSettings base = KeyboardSettings.defaults().withSharedMargin(12);
        KeyboardSettings left = base.withHandednessPreset(HandednessMode.LEFT);
        KeyboardSettings right = base.withHandednessPreset(HandednessMode.RIGHT);
        KeyboardSettings balanced = right.withHandednessPreset(HandednessMode.BALANCED);

        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, left.leftMarginDp);
        assertEquals(56, left.rightMarginDp);
        assertEquals(56, right.leftMarginDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, right.rightMarginDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, balanced.leftMarginDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, balanced.rightMarginDp);
    }

    @Test
    public void oneHandedPresetAppliesToBothLayouts() {
        KeyboardSettings base = KeyboardSettings.defaults()
                .withSharedMargin(12)
                .withHangulSidePadding(7, 9)
                .withEnglishSidePadding(11, 13);
        KeyboardSettings left = base.withHandednessPreset(HandednessMode.LEFT);
        KeyboardSettings right = base.withHandednessPreset(HandednessMode.RIGHT);
        KeyboardSettings balanced = right.withHandednessPreset(HandednessMode.BALANCED);

        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, left.hangulLeftPaddingDp);
        assertEquals(56, left.hangulRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, left.englishLeftPaddingDp);
        assertEquals(56, left.englishRightPaddingDp);
        assertEquals(56, right.hangulLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, right.hangulRightPaddingDp);
        assertEquals(56, right.englishLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, right.englishRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, balanced.hangulLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, balanced.hangulRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, balanced.englishLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, balanced.englishRightPaddingDp);
    }

    @Test
    public void hangulAndEnglishHeightsAreIndependent() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulHeight(340)
                .withEnglishHeight(280);

        assertEquals(340, settings.hangulKeyboardHeightDp);
        assertEquals(280, settings.englishKeyboardHeightDp);
        assertEquals(340, settings.keyboardHeightDp);
        assertEquals(280, settings.withKeyboardMode(KeyboardMode.ENGLISH).keyboardHeightDp);
        assertEquals(340, settings.withKeyboardMode(KeyboardMode.ENGLISH)
                .withKeyboardMode(KeyboardMode.HANGUL)
                .keyboardHeightDp);
    }

    @Test
    public void keyboardHeightsClampToExpandedEditorRange() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulHeight(999)
                .withEnglishHeight(0);

        assertEquals(KeyboardSettings.MAX_HEIGHT_DP, settings.hangulKeyboardHeightDp);
        assertEquals(KeyboardSettings.MIN_HEIGHT_DP, settings.englishKeyboardHeightDp);
    }
}
