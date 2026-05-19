package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
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
        assertEquals(true, dark.keyColorOverrides.containsKey(".."));
        assertEquals(true, light.keyColorOverrides.containsKey(".."));
    }

    @Test
    public void gmkDotsPresetsKeepReadableKeyLegends() {
        KeyboardSettings dark = KeyboardThemePreset.find("gmk-dots-dark")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings light = KeyboardThemePreset.find("gmk-dots-light")
                .applyTo(KeyboardSettings.defaults());

        assertEquals(LegendStylePreset.DEFAULT, dark.legendStylePreset);
        assertEquals(LegendStylePreset.DEFAULT, light.legendStylePreset);
    }

    @Test
    public void colorfulDingulPresetsTintAllVowelLegends() {
        KeyboardSettings dark = KeyboardThemePreset.find("marigold-fiesta-dark")
                .applyTo(KeyboardSettings.defaults());
        KeyboardSettings light = KeyboardThemePreset.find("marigold-fiesta-light")
                .applyTo(KeyboardSettings.defaults());

        assertTrue(dark.keyColorOverrides.containsKey("tap:ㅢ"));
        assertTrue(dark.keyColorOverrides.containsKey("__dingul_center_vowel__"));
        assertTrue(dark.keyColorOverrides.containsKey("__dingul_wide_vowel__"));
        assertTrue(dark.keyColorOverrides.containsKey("ㅣ."));
        assertTrue(dark.keyColorOverrides.containsKey("ㅡㅐ"));
        assertTrue(light.keyColorOverrides.containsKey("tap:ㅢ"));
        assertTrue(light.keyColorOverrides.containsKey("__dingul_center_vowel__"));
        assertTrue(light.keyColorOverrides.containsKey("__dingul_wide_vowel__"));
        assertTrue(light.keyColorOverrides.containsKey("ㅣ."));
        assertTrue(light.keyColorOverrides.containsKey("ㅡㅐ"));
    }

    @Test
    public void marigoldExternalThemeFilesMatchBuiltInPresets() throws IOException {
        assertExternalThemeMatchesPreset("marigold-fiesta-dark");
        assertExternalThemeMatchesPreset("marigold-fiesta-light");
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
