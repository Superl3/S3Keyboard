package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public final class KeyboardThemePresetTest {
    @Test
    public void presetsCoverPlatformStyleFamilies() {
        assertNotNull(KeyboardThemePreset.find("ios-clean-light"));
        assertNotNull(KeyboardThemePreset.find("ios-clean-dark"));
        assertNotNull(KeyboardThemePreset.find("macos-frost-light"));
        assertNotNull(KeyboardThemePreset.find("macos-graphite-dark"));
        assertNotNull(KeyboardThemePreset.find("android-material-light"));
        assertNotNull(KeyboardThemePreset.find("android-material-dark"));
        assertNull(KeyboardThemePreset.find("mint-air"));
        assertNull(KeyboardThemePreset.find("lavender-focus"));
    }

    @Test
    public void presetsHaveUniqueIdsAndImportableJson() {
        Set<String> ids = new HashSet<>();

        for (KeyboardThemePreset preset : KeyboardThemePreset.PRESETS) {
            assertTrue(ids.add(preset.id));
            assertNotNull(preset.displayName);

            KeyboardSettings base = KeyboardSettings.defaults()
                    .withHeights(330, 270)
                    .withHangulSidePadding(4, 5)
                    .withEnglishSidePadding(6, 7)
                    .withLayoutSpacing(
                            8,
                            KeyboardSettings.DEFAULT_KEYBOARD_TOP_PADDING_DP,
                            9,
                            10);
            KeyboardSettings themed = preset.applyTo(base);

            assertEquals(themed.fontFamily, KeyboardSettings.normalizeFontFamily(themed.fontFamily));
            assertEquals(true, themed.showBeginnerTooltipPreview);
            assertTrue(themed.primaryFunctionKeyColor != themed.keyIdleColor);
            assertTrue(themed.accentKeyColor != themed.keyIdleColor);
            assertEquals(330, themed.hangulKeyboardHeightDp);
            assertEquals(270, themed.englishKeyboardHeightDp);
            assertEquals(4, themed.hangulLeftPaddingDp);
            assertEquals(5, themed.hangulRightPaddingDp);
            assertEquals(6, themed.englishLeftPaddingDp);
            assertEquals(7, themed.englishRightPaddingDp);
            assertEquals(8, themed.hangulMainSpecialGapDp);
            assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_TOP_PADDING_DP, themed.keyboardTopPaddingDp);
            assertEquals(9, themed.keyboardBottomPaddingDp);
            assertEquals(10, themed.bottomRowTopPaddingDp);
            assertTrue(themed.keyColorOverrides.containsKey("shiftindicator"));
        }
    }

    @Test
    public void paperMonoPresetUsesMonoFontAndFlatDepth() {
        KeyboardSettings themed = KeyboardThemePreset.find("paper-mono-flat")
                .applyTo(KeyboardSettings.defaults());

        assertEquals(KeyboardSettings.FONT_D2CODING, themed.fontFamily);
        assertEquals(false, themed.keyDepthEnabled);
    }

    @Test
    public void marigoldPresetsUseNeutralOutlineDepthAndLowerRoundness() {
        KeyboardSettings dark = KeyboardThemePreset.find("marigold-fiesta-dark")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings light = KeyboardThemePreset.find("marigold-fiesta-light")
                .applyTo(KeyboardSettings.defaults());

        assertEquals(4, dark.keyRoundnessDp);
        assertEquals(0xFF45484F, dark.borderColor);
        assertEquals(0xFF2F3339, dark.depthColor);
        assertEquals(4, light.keyRoundnessDp);
        assertEquals(0xFFBBB4AA, light.borderColor);
        assertEquals(0xFFD2CCC2, light.depthColor);
        assertEquals(0xFFFFFFFF, light.keyboardBackgroundColor);
        assertEquals(KeyboardSettings.FONT_NOTO_SANS_KR, dark.fontFamily);
        assertEquals(KeyboardSettings.FONT_NOTO_SANS_KR, light.fontFamily);
        assertEquals(false, dark.keyColorOverrides.containsKey(".."));
        assertEquals(false, light.keyColorOverrides.containsKey(".."));
    }

    @Test
    public void gmkDotsPresetsKeepReadableKeyLegends() {
        KeyboardSettings dark = KeyboardThemePreset.find("gmk-dots-dark")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings light = KeyboardThemePreset.find("gmk-dots-light")
                .applyTo(KeyboardSettings.defaults());

        assertEquals(LegendStylePreset.DEFAULT, dark.legendStylePreset);
        assertEquals(LegendStylePreset.DEFAULT, light.legendStylePreset);
        assertEquals(ModifierIconCatalog.PACK_DOTS_LINES, dark.modifierIconThemePackId);
        assertEquals(ModifierIconCatalog.PACK_DOTS_LINES, light.modifierIconThemePackId);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, dark.keyDisplayOverrides.get("alpha").value);
        assertEquals(ModifierIconCatalog.GLYPH_DOT, light.keyDisplayOverrides.get("alpha").value);
        assertNotEquals(
                dark.accentColor,
                KeyboardKeyVisualClassifier.textColorFor(
                        dark.withKeyboardMode(KeyboardMode.ENGLISH),
                        new GestureKey("q", "q", "Q", "!", null, null, "!", 2)));
        assertNotEquals(
                light.accentColor,
                KeyboardKeyVisualClassifier.textColorFor(
                light.withKeyboardMode(KeyboardMode.HANGUL),
                new GestureKey(
                                "ㅡㅐ",
                                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                                "ㅙ",
                                "ㅞ",
                                "ㅔ",
                                "ㅐ",
                                null)));
    }

    @Test
    public void sampleInspiredThemesSelectMatchingPacksAndMetropolisColors() {
        KeyboardSettings olivia = KeyboardThemePreset.find("gmk-olivia-light")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings oliviaDark = KeyboardThemePreset.find("gmk-olivia-dark")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings oblivion = KeyboardThemePreset.find("gmk-oblivion")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings metropolis = KeyboardThemePreset.find("gmk-metropolis")
                .applyTo(KeyboardSettings.defaults());

        assertEquals(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT, olivia.keyDisplayThemePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS, oblivion.keyDisplayThemePackId);
        assertEquals(0xFFF2EDE8, (int) olivia.keyColorOverrides.get("shift"));
        assertEquals(0xFF211F23, (int) olivia.keyColorOverrides.get("enter"));
        assertEquals(0xFFF4E7E2, (int) oliviaDark.keyColorOverrides.get("shift"));
        assertEquals(0xFF211F23, (int) oliviaDark.keyColorOverrides.get("enter"));
        assertEquals(ModifierIconCatalog.PACK_METROPOLIS_POINTS, metropolis.modifierIconThemePackId);
        assertEquals(0xFF090D12, metropolis.keyboardBackgroundColor);
        assertEquals(0xFF10151B, (int) metropolis.keyColorOverrides.get("background:alpha"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:1"));
        assertEquals(0xFF10151B, (int) metropolis.keyColorOverrides.get("background:tap:1"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("background:backspace"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("background:shift"));
        assertEquals(0xFF66E3C4, (int) metropolis.keyColorOverrides.get("background:enter"));
        assertEquals(true, metropolis.visualEffects.blurEnabled);
        assertEquals(true, metropolis.visualEffects.metallicEnabled);
        assertEquals(true, metropolis.visualEffects.angularPreviewBubble);
    }

    @Test
    public void colorfulDingulPresetsTintAllVowelLegends() {
        assertDingulVowelOverrides("marigold-fiesta-dark");
        assertDingulVowelOverrides("marigold-fiesta-light");
        assertDingulVowelOverrides("gmk-bento");
        assertDingulVowelOverrides("gmk-hammerhead");
        assertDingulVowelOverrides("gmk-8008");
        assertDingulVowelOverrides("gmk-modern-dolch");
    }

    @Test
    public void reportedGmkThemesKeepDingulSpecialLegendsReadable() {
        assertSpecialLegendsReadable("gmk-bento");
        assertSpecialLegendsReadable("gmk-hammerhead");
        assertSpecialLegendsReadable("gmk-8008");
        assertSpecialLegendsReadable("gmk-modern-dolch");
    }

    @Test
    public void marigoldExternalThemeFilesMatchBuiltInPresets() throws IOException {
        assertExternalThemeMatchesPreset("marigold-fiesta-dark");
        assertExternalThemeMatchesPreset("marigold-fiesta-light");
    }

    @Test
    public void gmkExternalThemeFilesMatchBuiltInPresetIdentity() throws IOException {
        String[] ids = {
                "gmk-bento",
                "gmk-metropolis",
                "gmk-oblivion",
                "gmk-oblivion-hagoromo",
                "gmk-8008",
                "gmk-hammerhead",
                "gmk-dracula",
                "gmk-modern-dolch",
                "gmk-olivia-light",
                "gmk-olivia-dark",
                "gmk-dots-light",
                "gmk-dots-dark"
        };
        for (String id : ids) {
            assertExternalThemeBasicMatchesPreset(id);
        }
    }

    private void assertExternalThemeMatchesPreset(String id) throws IOException {
        KeyboardThemePreset preset = KeyboardThemePreset.find(id);
        assertNotNull(preset);

        KeyboardSettings base = KeyboardSettings.defaults();
        KeyboardSettings builtIn = preset.applyTo(base);
        KeyboardSettings external = KeyboardThemeJson.importTheme(base, readThemeJson(id));

        assertEquals(builtIn.keyIdleColor, external.keyIdleColor);
        assertEquals(builtIn.functionKeyColor, external.functionKeyColor);
        assertEquals(builtIn.primaryFunctionKeyColor, external.primaryFunctionKeyColor);
        assertEquals(builtIn.accentKeyColor, external.accentKeyColor);
        assertEquals(builtIn.borderColor, external.borderColor);
        assertEquals(builtIn.depthColor, external.depthColor);
        assertEquals(builtIn.fontFamily, external.fontFamily);
        assertEquals(builtIn.primaryTextSizePercent, external.primaryTextSizePercent);
        assertEquals(builtIn.secondaryTextSizePercent, external.secondaryTextSizePercent);

        assertOverrideMatches(builtIn, external, "tap:ㅢ");
        assertOverrideMatches(builtIn, external, "__dingul_center_vowel__");
        assertOverrideMatches(builtIn, external, "__dingul_wide_vowel__");
        assertOverrideMatches(builtIn, external, "ㅢ");
        assertOverrideMatches(builtIn, external, "ㅣ.");
        assertOverrideMatches(builtIn, external, "ㅡㅐ");
        assertOverrideMatches(builtIn, external, "shiftIndicator");
    }

    private void assertExternalThemeBasicMatchesPreset(String id) throws IOException {
        KeyboardThemePreset preset = KeyboardThemePreset.find(id);
        assertNotNull(preset);

        KeyboardSettings base = KeyboardSettings.defaults();
        KeyboardSettings builtIn = preset.applyTo(base);
        KeyboardSettings external = KeyboardThemeJson.importTheme(base, readThemeJson(id));

        assertEquals(id, builtIn.keyIdleColor, external.keyIdleColor);
        assertEquals(id, builtIn.functionKeyColor, external.functionKeyColor);
        assertEquals(id, builtIn.primaryFunctionKeyColor, external.primaryFunctionKeyColor);
        assertEquals(id, builtIn.accentKeyColor, external.accentKeyColor);
        assertEquals(id, builtIn.keyPressedColor, external.keyPressedColor);
        assertEquals(id, builtIn.keyboardBackgroundColor, external.keyboardBackgroundColor);
        assertEquals(id, builtIn.borderColor, external.borderColor);
        assertEquals(id, builtIn.depthColor, external.depthColor);
        assertEquals(id, builtIn.modifierIconThemePackId, external.modifierIconThemePackId);
        assertEquals(id, builtIn.keyDisplayThemePackId, external.keyDisplayThemePackId);
        assertEquals(id, builtIn.visualEffects.blurEnabled, external.visualEffects.blurEnabled);
        assertEquals(id, builtIn.visualEffects.metallicEnabled, external.visualEffects.metallicEnabled);
        assertEquals(id, builtIn.visualEffects.angularPreviewBubble, external.visualEffects.angularPreviewBubble);
        assertEquals(id,
                builtIn.keyColorOverrides.get("shiftIndicator"),
                external.keyColorOverrides.get("shiftIndicator"));
    }

    private void assertOverrideMatches(
            KeyboardSettings builtIn,
            KeyboardSettings external,
            String key) {
        String normalized = KeyboardSettings.normalizeKeyOverrideName(key);
        assertTrue("Missing built-in override: " + key,
                builtIn.keyColorOverrides.containsKey(normalized));
        assertTrue("Missing external override: " + key,
                external.keyColorOverrides.containsKey(normalized));
        assertEquals("Override mismatch: " + key,
                builtIn.keyColorOverrides.get(normalized),
                external.keyColorOverrides.get(normalized));
    }

    private void assertDingulVowelOverrides(String presetId) {
        KeyboardThemePreset preset = KeyboardThemePreset.find(presetId);
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults());

        assertTrue(presetId + " missing tap:\u3162",
                settings.keyColorOverrides.containsKey("tap:\u3162"));
        assertTrue(presetId + " missing \u3162",
                settings.keyColorOverrides.containsKey("\u3162"));
        assertTrue(presetId + " missing center vowel command",
                settings.keyColorOverrides.containsKey("__dingul_center_vowel__"));
        assertTrue(presetId + " missing \u3163.",
                settings.keyColorOverrides.containsKey("\u3163."));
        assertTrue(presetId + " missing wide vowel command",
                settings.keyColorOverrides.containsKey("__dingul_wide_vowel__"));
        assertTrue(presetId + " missing \u3161\u3150",
                settings.keyColorOverrides.containsKey("\u3161\u3150"));
    }

    private void assertSpecialLegendsReadable(String presetId) {
        KeyboardThemePreset preset = KeyboardThemePreset.find(presetId);
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults());

        assertSpecialLegendReadable(
                presetId,
                settings,
                new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null));
        assertSpecialLegendReadable(
                presetId,
                settings,
                new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null));
    }

    private void assertSpecialLegendReadable(
            String presetId,
            KeyboardSettings settings,
            GestureKey key) {
        assertNotEquals(
                presetId + " special legend should contrast with its background",
                KeyboardKeyVisualClassifier.colorFor(settings, key),
                KeyboardKeyVisualClassifier.textColorFor(settings, key));
    }

    private String readThemeJson(String id) throws IOException {
        File file = new File("themes", id + ".json");
        if (!file.isFile()) {
            file = new File("../themes", id + ".json");
        }
        assertTrue("Missing external theme JSON: " + id, file.isFile());
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private static int brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }
}
