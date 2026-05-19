package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyboardThemeJsonTest {
    @Test
    public void themeJsonRoundTripsV1VisualSettings() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withExtendedThemeColors(
                        0x00111111,
                        0x00222222,
                        0x00333333,
                        0x00444444,
                        0x00555555,
                        0x00666666,
                        0x00777777,
                        0x00888888,
                        0x00999999,
                        true,
                        0x00AAAAAA)
                .withKeyRoundness(7)
                .withKeyBorderWidth(3)
                .withKeyGap(6)
                .withKeyDepth(true, 4)
                .withTypography(
                        KeyboardSettings.FONT_D2CODING,
                        112,
                        88,
                        false,
                        true,
                        true,
                        false)
                .withHintVisibility(false, true, false)
                .withHeights(346, 282)
                .withHangulSidePadding(8, 10)
                .withEnglishSidePadding(3, 5)
                .withLayoutSpacing(12, 7, 9)
                .withKeyColorOverrides(sampleKeyOverrides());

        KeyboardSettings base = KeyboardSettings.defaults().withHintVisibility(true, false, true);
        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                base,
                KeyboardThemeJson.exportTheme(settings, "Soft Classic", "local", null));
        String exported = KeyboardThemeJson.exportTheme(settings, "Soft Classic", "local", null);

        assertEquals(0xFF111111, imported.keyIdleColor);
        assertEquals(0xFF222222, imported.keyPressedColor);
        assertEquals(0xFF333333, imported.keyboardBackgroundColor);
        assertEquals(0xFF444444, imported.accentColor);
        assertEquals(0xFF555555, imported.secondaryColor);
        assertEquals(0xFF666666, imported.functionKeyColor);
        assertEquals(0xFF777777, imported.primaryFunctionKeyColor);
        assertEquals(0xFF888888, imported.accentKeyColor);
        assertEquals(0xFF999999, imported.borderColor);
        assertEquals(true, imported.customDepthColorEnabled);
        assertEquals(0xFFAAAAAA, imported.depthColor);
        assertEquals(7, imported.keyRoundnessDp);
        assertEquals(3, imported.keyBorderWidthDp);
        assertEquals(6, imported.keyGapDp);
        assertEquals(true, imported.keyDepthEnabled);
        assertEquals(4, imported.keyDepthDp);
        assertEquals(KeyboardSettings.FONT_D2CODING, imported.fontFamily);
        assertEquals(112, imported.primaryTextSizePercent);
        assertEquals(88, imported.secondaryTextSizePercent);
        assertEquals(false, imported.primaryTextBold);
        assertEquals(true, imported.primaryTextItalic);
        assertEquals(true, imported.secondaryTextBold);
        assertEquals(false, imported.secondaryTextItalic);
        assertEquals(true, imported.showHangulSlideHints);
        assertEquals(false, imported.showEnglishSlideHints);
        assertEquals(true, imported.showBeginnerTooltipPreview);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_HEIGHT_DP, imported.hangulKeyboardHeightDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_HEIGHT_DP, imported.englishKeyboardHeightDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_LEFT_PADDING_DP, imported.hangulLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_RIGHT_PADDING_DP, imported.hangulRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_LEFT_PADDING_DP, imported.englishLeftPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_ENGLISH_RIGHT_PADDING_DP, imported.englishRightPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_HANGUL_MAIN_SPECIAL_GAP_DP, imported.hangulMainSpecialGapDp);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_BOTTOM_PADDING_DP, imported.keyboardBottomPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_BOTTOM_ROW_TOP_PADDING_DP, imported.bottomRowTopPaddingDp);
        assertFalse(exported.contains("\"layout\""));
        assertFalse(exported.contains("\"hints\""));
        assertEquals(0xFFE95420, imported.keyColorOverrides.get("tap:a").intValue());
        assertEquals(0xFF00A676, imported.keyColorOverrides.get("space").intValue());
    }

    @Test
    public void depthNullDisablesCustomDepthColorAndKeepsBorderColor() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{\"border\":\"#123456\",\"depth\":null}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults().withDepthColor(true, 0x00ABCDEF),
                json);

        assertEquals(0xFF123456, imported.borderColor);
        assertEquals(false, imported.customDepthColorEnabled);
    }

    @Test
    public void unsupportedFontInJsonFallsBackToDefault() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"typography\":{\"fontFamily\":\"missing_font\"}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(KeyboardSettings.FONT_DEFAULT, imported.fontFamily);
    }

    @Test
    public void missingKeyColorOverridesClearThemeSpecificOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:q", 0x00ABCDEF);

        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults().withKeyColorOverrides(overrides),
                "{\"schemaVersion\":1}");

        assertEquals(0, imported.keyColorOverrides.size());
    }

    @Test
    public void layoutFieldsInThemeJsonDoNotOverrideUserLayout() {
        KeyboardSettings base = KeyboardSettings.defaults()
                .withHeights(330, 270)
                .withHangulSidePadding(4, 5)
                .withEnglishSidePadding(6, 7)
                .withLayoutSpacing(8, 9, 10);
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"layout\":{"
                + "\"hangulHeightDp\":340,"
                + "\"englishHeightDp\":280,"
                + "\"hangulLeftPaddingDp\":11,"
                + "\"hangulRightPaddingDp\":12,"
                + "\"englishLeftPaddingDp\":13,"
                + "\"englishRightPaddingDp\":14,"
                + "\"hangulMainSpecialGapDp\":15,"
                + "\"keyboardBottomPaddingDp\":16,"
                + "\"bottomRowTopPaddingDp\":17"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(base, json);

        assertEquals(330, imported.hangulKeyboardHeightDp);
        assertEquals(270, imported.englishKeyboardHeightDp);
        assertEquals(4, imported.hangulLeftPaddingDp);
        assertEquals(5, imported.hangulRightPaddingDp);
        assertEquals(6, imported.englishLeftPaddingDp);
        assertEquals(7, imported.englishRightPaddingDp);
        assertEquals(8, imported.hangulMainSpecialGapDp);
        assertEquals(9, imported.keyboardBottomPaddingDp);
        assertEquals(10, imported.bottomRowTopPaddingDp);
    }

    @Test
    public void hintFieldsInThemeJsonDoNotOverrideUserConvenienceSettings() {
        KeyboardSettings base = KeyboardSettings.defaults().withHintVisibility(false, false, false);
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"hints\":{"
                + "\"showHangulSlideHints\":true,"
                + "\"showEnglishSlideHints\":true,"
                + "\"showBeginnerTooltipPreview\":true"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(base, json);

        assertEquals(false, imported.showHangulSlideHints);
        assertEquals(false, imported.showEnglishSlideHints);
        assertEquals(false, imported.showBeginnerTooltipPreview);
    }

    private static Map<String, Integer> sampleKeyOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:a", 0x00E95420);
        overrides.put("space", 0x0000A676);
        return overrides;
    }
}
