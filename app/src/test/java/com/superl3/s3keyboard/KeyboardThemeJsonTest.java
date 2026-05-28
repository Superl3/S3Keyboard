package com.superl3.s3keyboard;

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
                        0x00666666,                        0x00888888,
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
                .withLayoutSpacing(12, 11, 7, 9)
                .withKeyColorOverrides(sampleKeyOverrides());

        KeyboardSettings base = KeyboardSettings.defaults().withHintVisibility(true, false, true);
        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                base,
                KeyboardThemeJson.exportTheme(settings, "Soft Classic", "local", null));
        String exported = KeyboardThemeJson.exportTheme(settings, "Soft Classic", "local", null);

        assertEquals(true, exported.contains("\"alphaKey\""));
        assertEquals(true, exported.contains("\"modifierKey\""));
        assertEquals(false, exported.contains("\"keyIdle\""));
        assertEquals(false, exported.contains("\"functionKey\""));
        assertEquals(false, exported.contains("\"primaryFunctionKey\""));
        assertEquals(0xFF111111, imported.keyIdleColor);
        assertEquals(0xFF222222, imported.keyPressedColor);
        assertEquals(0xFF333333, imported.keyboardBackgroundColor);
        assertEquals(0xFF444444, imported.accentColor);
        assertEquals(0xFF555555, imported.secondaryColor);
        assertEquals(0xFF666666, imported.functionKeyColor);
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
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_TOP_PADDING_DP, imported.keyboardTopPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_BOTTOM_PADDING_DP, imported.keyboardBottomPaddingDp);
        assertEquals(KeyboardSettings.DEFAULT_BOTTOM_ROW_TOP_PADDING_DP, imported.bottomRowTopPaddingDp);
        assertFalse(exported.contains("\"layout\""));
        assertFalse(exported.contains("\"hints\""));
        assertFalse(exported.contains("\"keyboardTopPaddingDp\""));
        assertEquals(0xFFE95420, imported.keyColorOverrides.get("tap:a").intValue());
        assertEquals(0xFF00A676, imported.keyColorOverrides.get("space").intValue());
    }

    @Test
    public void panelBackgroundOverridesKeyboardBackgroundForRuntimePanel() {
        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults(),
                "{"
                        + "\"schemaVersion\":1,"
                        + "\"colors\":{"
                        + "\"keyboardBackground\":\"#111111\","
                        + "\"panelBackground\":\"#222222\""
                        + "}"
                        + "}");

        assertEquals(0xFF222222, imported.keyboardBackgroundColor);
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

        assertEquals(KeyboardSettings.DEFAULT_FONT_FAMILY, imported.fontFamily);
    }

    @Test
    public void missingKeyColorOverridesKeepLayeredOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:q", 0x00ABCDEF);

        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults().withKeyColorOverrides(overrides),
                "{\"schemaVersion\":1}");

        assertEquals(0xFFABCDEF, (int) imported.keyColorOverrides.get("tap:q"));
    }

    @Test
    public void layoutFieldsInThemeJsonDoNotOverrideUserLayout() {
        KeyboardSettings base = KeyboardSettings.defaults()
                .withHeights(330, 270)
                .withHangulSidePadding(4, 5)
                .withEnglishSidePadding(6, 7)
                .withLayoutSpacing(8, 9, 10, 11);
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
                + "\"keyboardTopPaddingDp\":18,"
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
        assertEquals(9, imported.keyboardTopPaddingDp);
        assertEquals(10, imported.keyboardBottomPaddingDp);
        assertEquals(11, imported.bottomRowTopPaddingDp);
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

    @Test
    public void legacyDotsLegendImportsAsKeyDisplayOverrideAndExportsNewShape() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"legendStyle\":{\"preset\":\"dots\"}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);
        String exported = KeyboardThemeJson.exportTheme(imported, "Dots", "local", null);

        assertEquals(LegendStylePreset.DEFAULT, imported.legendStylePreset);
        assertEquals(KeyDisplayOverride.TYPE_ICON, imported.keyDisplayOverrides.get("alpha").type);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, imported.keyDisplayOverrides.get("alpha").value);
        assertFalse(exported.contains("\"legendStyle\""));
    }

    @Test
    public void importsAndExportsIconsDisplayPacksAndEffects() {
        Map<String, KeyDisplayOverride> display = new HashMap<>();
        display.put("alpha", KeyDisplayOverride.icon(ModifierIconCatalog.GLYPH_DOT));
        display.put("tap:q", KeyDisplayOverride.text("Q"));
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withModifierIconThemePack(ModifierIconCatalog.PACK_ACCENT_COLOR)
                .withKeyDisplayThemePack(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT)
                .withVisualEffects(new KeyboardVisualEffects(
                        true,
                        12,
                        true,
                        24,
                        true,
                        true,
                        38,
                        0xFF778899,
                        0xFF010203,
                        KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_TOP_GLOW,
                        true,
                        0xFF112233,
                        0xFF445566))
                .withKeyDisplayOverrides(display);

        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults(),
                KeyboardThemeJson.exportTheme(settings, "Icons", "local", null));

        assertEquals(ModifierIconCatalog.PACK_ACCENT_COLOR, imported.modifierIconThemePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT, imported.keyDisplayThemePackId);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, imported.keyDisplayOverrides.get("alpha").value);
        assertEquals("Q", imported.keyDisplayOverrides.get("tap:q").value);
        assertEquals(true, imported.visualEffects.blurEnabled);
        assertEquals(12, imported.visualEffects.blurRadiusDp);
        assertEquals(true, imported.visualEffects.metallicEnabled);
        assertEquals(24, imported.visualEffects.metallicStrengthPercent);
        assertEquals(true, imported.visualEffects.keyFaceGradientEnabled);
        assertEquals(38, imported.visualEffects.keyFaceGradientStrengthPercent);
        assertEquals(0xFF778899, imported.visualEffects.keyFaceGradientStartColor);
        assertEquals(0xFF010203, imported.visualEffects.keyFaceGradientEndColor);
        assertEquals(
                KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_TOP_GLOW,
                imported.visualEffects.keyFaceGradientCurve);
        assertEquals(true, imported.visualEffects.panelGradientEnabled);
        assertEquals(0xFF112233, imported.visualEffects.panelGradientStartColor);
        assertEquals(0xFF445566, imported.visualEffects.panelGradientEndColor);
    }

    @Test
    public void themeLayersApplyInOrderBeforeLocalOverrides() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"layers\":[\"gmk-dots-dark\",\"gmk-metropolis\"],"
                + "\"colors\":{\"accent\":\"#123456\"},"
                + "\"keyBackgroundColorOverrides\":{\"alpha\":\"#010203\"}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFF123456, imported.accentColor);
        assertEquals(ModifierIconCatalog.PACK_METROPOLIS_POINTS, imported.modifierIconThemePackId);
        assertEquals(0xFF010203, (int) imported.keyColorOverrides.get("background:alpha"));
        assertEquals(ModifierIconCatalog.GLYPH_DOT, imported.keyDisplayOverrides.get("alpha").value);
    }

    @Test
    public void legacySimpleTextModifierIconPackImportsAsKeyDisplayPack() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"icons\":{\"modifierPackId\":\"olivia-script-text\"}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(ModifierIconCatalog.PACK_LINE_MONO, imported.modifierIconThemePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT, imported.keyDisplayThemePackId);
    }

    @Test
    public void importsAndExportsKeyBackgroundOverrides() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"keyBackgroundColorOverrides\":{"
                + "\"enter\":\"#FF6677\","
                + "\"tap:q\":\"#112233\""
                + "}"
                + "}";
        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFFFF6677, (int) imported.keyColorOverrides.get("background:enter"));
        assertEquals(0xFF112233, (int) imported.keyColorOverrides.get("background:tap:q"));

        String exported = KeyboardThemeJson.exportTheme(imported, "Backgrounds", "local", null);
        KeyboardSettings roundTrip = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), exported);

        assertEquals(0xFFFF6677, (int) roundTrip.keyColorOverrides.get("background:enter"));
        assertEquals(0xFF112233, (int) roundTrip.keyColorOverrides.get("background:tap:q"));
    }

    @Test
    public void importsDingulSemanticColorRoles() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"dingulColors\":{"
                + "\"alpha\":{\"foreground\":\"#111111\",\"background\":\"#222222\"},"
                + "\"mod\":{\"foreground\":\"#333333\",\"background\":\"#444444\"},"
                + "\"modInv\":{\"foreground\":\"#555555\",\"background\":\"#666666\"}"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("alpha"));
        assertEquals(0xFF222222, (int) imported.keyColorOverrides.get("background:alpha"));
        assertEquals(0xFF333333, (int) imported.keyColorOverrides.get("modifiers"));
        assertEquals(0xFF444444, (int) imported.keyColorOverrides.get("background:modifiers"));
        assertEquals(0xFF555555, (int) imported.keyColorOverrides.get("modinv"));
        assertEquals(0xFF666666, (int) imported.keyColorOverrides.get("background:modinv"));
    }

    @Test
    public void importsAccentPolicyAsSemanticKeyOverrides() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{\"accentKey\":\"#FF9900\"},"
                + "\"dingulColors\":{"
                + "\"modInv\":{\"foreground\":\"#111111\",\"background\":\"#FF9900\"}"
                + "},"
                + "\"accentPolicy\":{"
                + "\"qwerty\":[\"modCtrl\"],"
                + "\"dingul\":[\"modEnter\",\"modShift\"]"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:settings"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:options"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:enter"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:."));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:/"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("settings"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("."));
    }

    @Test
    public void importsAccentPolicyWithIndividualCommandsAndSpaceRole() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{"
                + "\"alphaKey\":\"#101010\","
                + "\"modifierKey\":\"#202020\","
                + "\"accent\":\"#111111\","
                + "\"secondary\":\"#222222\","
                + "\"accentKey\":\"#FF9900\""
                + "},"
                + "\"dingulColors\":{"
                + "\"alpha\":{\"foreground\":\"#AAAAAA\",\"background\":\"#BBBBBB\"},"
                + "\"mod\":{\"foreground\":\"#CCCCCC\",\"background\":\"#DDDDDD\"},"
                + "\"modInv\":{\"foreground\":\"#111111\",\"background\":\"#FF9900\"}"
                + "},"
                + "\"accentPolicy\":{"
                + "\"qwerty\":[\"qwertyShift\",\"backspace\",\"settingsEnter\",\"escPoint\"],"
                + "\"dingul\":[\"dingulDot\",\"escPoint\"],"
                + "\"spacebar\":\"mod\","
                + "\"question\":\"accent\""
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:shift"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:backspace"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:options"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:1"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:\u3131"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:."));
        assertEquals(false, imported.keyColorOverrides.containsKey("background:/"));
        assertEquals(0xFFDDDDDD, (int) imported.keyColorOverrides.get("background:space"));
        assertEquals(0xFFCCCCCC, (int) imported.keyColorOverrides.get("space"));
        assertEquals(0xFFFF9900, (int) imported.keyColorOverrides.get("background:?"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("?"));
    }

    @Test
    public void colorfulThemesLockUserAccentPlacement() {
        String colorful = "{"
                + "\"schemaVersion\":1,"
                + "\"metadata\":{\"features\":[\"colorfulForeground\"]},"
                + "\"colors\":{\"alphaKey\":\"#101010\",\"modifierKey\":\"#202020\"}"
                + "}";
        String colorfulModifierToken = "{"
                + "\"schemaVersion\":1,"
                + "\"metadata\":{\"features\":[\"colorfulModifier\"]},"
                + "\"colors\":{\"alphaKey\":\"#101010\",\"modifierKey\":\"#202020\"}"
                + "}";
        String colorfulModifierPack = "{"
                + "\"schemaVersion\":1,"
                + "\"icons\":{\"modifierPackId\":\"accent-color\"},"
                + "\"colors\":{\"alphaKey\":\"#101010\",\"modifierKey\":\"#202020\"}"
                + "}";
        String normal = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{\"alphaKey\":\"#101010\",\"modifierKey\":\"#202020\"}"
                + "}";

        assertEquals(true, KeyboardThemeJson.locksUserAccentPlacement(colorful));
        assertEquals(true, KeyboardThemeJson.locksUserAccentPlacement(colorfulModifierToken));
        assertEquals(true, KeyboardThemeJson.locksUserAccentPlacement(colorfulModifierPack));
        assertEquals(false, KeyboardThemeJson.locksUserAccentPlacement(normal));
    }

    @Test
    public void importsImplicitAccentPolicyWhenAccentKeyIsDistinct() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{"
                + "\"alphaKey\":\"#101010\","
                + "\"modifierKey\":\"#202020\","
                + "\"accentKey\":\"#F06030\""
                + "},"
                + "\"dingulColors\":{"
                + "\"modInv\":{\"foreground\":\"#111111\",\"background\":\"#F06030\"}"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:reserved"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:language"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:shift"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:backspace"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:settings"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:enter"));
        assertEquals(0xFFF06030, (int) imported.keyColorOverrides.get("background:."));
        assertEquals(false, imported.keyColorOverrides.containsKey("background:/"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("settings"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("language"));
        assertEquals(0xFF111111, (int) imported.keyColorOverrides.get("."));
    }

    @Test
    public void skipsImplicitAccentPolicyForTwoToneThemes() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{"
                + "\"alphaKey\":\"#101010\","
                + "\"modifierKey\":\"#202020\","
                + "\"accentKey\":\"#202020\""
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(false, imported.keyColorOverrides.containsKey("background:settings"));
        assertEquals(false, imported.keyColorOverrides.containsKey("background:."));
    }

    @Test
    public void skipsExplicitAccentPolicyForTwoToneThemes() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"colors\":{"
                + "\"alphaKey\":\"#101010\","
                + "\"modifierKey\":\"#202020\","
                + "\"accentKey\":\"#202020\""
                + "},"
                + "\"accentPolicy\":{"
                + "\"qwerty\":[\"modCtrl\"],"
                + "\"dingul\":[\"modEnter\",\"modShift\"]"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(false, imported.keyColorOverrides.containsKey("background:settings"));
        assertEquals(false, imported.keyColorOverrides.containsKey("background:."));
    }

    @Test
    public void numberRowColorModeImportsLegacyAliasesAndExportsSemanticValue() {
        KeyboardSettings imported = KeyboardThemeJson.importTheme(
                KeyboardSettings.defaults(),
                "{"
                        + "\"schemaVersion\":1,"
                        + "\"additionalNumberRow\":{\"colorMode\":\"center_dimmed\"}"
                        + "}");
        String exported = KeyboardThemeJson.exportTheme(imported, "Number Row", "local", null);
        KeyboardSettings roundTrip = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), exported);

        assertEquals(AdditionalNumberRowColorMode.HALF_MOD_4567, imported.additionalNumberRowColorMode);
        assertEquals(AdditionalNumberRowColorMode.HALF_MOD_4567, roundTrip.additionalNumberRowColorMode);
        assertEquals(true, exported.contains("\"colorMode\": \"half_mod_4567\""));
    }

    @Test
    public void importsExternalKeyDisplayPackAsOverrides() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"icons\":{\"keyDisplayPackId\":\"desk-pack\"},"
                + "\"iconPacks\":{"
                + "\"keyDisplay\":{"
                + "\"desk-pack\":{"
                + "\"overrides\":{"
                + "\"alpha\":{\"type\":\"icon\",\"value\":\"dot\"},"
                + "\"keys\":{\"enter\":{\"type\":\"text\",\"value\":\"go\"}}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(KeyDisplayOverridePackCatalog.PACK_NONE, imported.keyDisplayThemePackId);
        assertEquals(KeyDisplayOverride.TYPE_ICON, imported.keyDisplayOverrides.get("alpha").type);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, imported.keyDisplayOverrides.get("alpha").value);
        assertEquals("go", imported.keyDisplayOverrides.get("enter").value);
    }

    @Test
    public void importsExternalModifierPackThroughBuiltinRendererAndOverrides() {
        String json = "{"
                + "\"schemaVersion\":1,"
                + "\"icons\":{\"modifierPackId\":\"wide-dots\"},"
                + "\"iconPacks\":{"
                + "\"modifier\":{"
                + "\"wide-dots\":{"
                + "\"extends\":\"dots-lines\","
                + "\"keyDisplayOverrides\":{"
                + "\"modifiers\":{\"type\":\"icon\",\"value\":\"dot\"}"
                + "}"
                + "}"
                + "}"
                + "}"
                + "}";

        KeyboardSettings imported = KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);

        assertEquals(ModifierIconCatalog.PACK_DOTS_LINES, imported.modifierIconThemePackId);
        assertEquals(KeyDisplayOverride.TYPE_ICON, imported.keyDisplayOverrides.get("modifiers").type);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, imported.keyDisplayOverrides.get("modifiers").value);
    }

    private static Map<String, Integer> sampleKeyOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:a", 0x00E95420);
        overrides.put("space", 0x0000A676);
        return overrides;
    }
}
