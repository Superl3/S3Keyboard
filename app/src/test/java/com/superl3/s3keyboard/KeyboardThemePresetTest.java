package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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
        assertNotNull(KeyboardThemePreset.find("lavender-focus"));
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
            assertTrue(themed.functionKeyColor != themed.keyIdleColor);
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
        assertEquals(0xFFE4DDD1, light.functionKeyColor);
        assertEquals(0xFFE4DDD1, light.functionKeyColor);
        assertEquals(0xFF1A1C20, dark.functionKeyColor);
        assertEquals(0xFF1A1C20, dark.accentKeyColor);
        assertEquals(0xFFE4DDD1, light.accentKeyColor);
        assertEquals(0xFFB8A9BF, (int) dark.keyColorOverrides.get("modinv"));
        assertEquals(0xFF1A1C20, (int) dark.keyColorOverrides.get("background:modinv"));
        assertEquals(0xFFB8A9BF, (int) dark.keyColorOverrides.get("."));
        assertEquals(0xFFB8A9BF, (int) dark.keyColorOverrides.get("/"));
        assertEquals(0xFF6C5542, (int) light.keyColorOverrides.get("modinv"));
        assertEquals(0xFFE4DDD1, (int) light.keyColorOverrides.get("background:modinv"));
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
        assertTrue(brightness(dark.functionKeyColor) < brightness(dark.keyIdleColor));
        assertEquals(AdditionalNumberRowColorMode.FULL_ALPHA, dark.additionalNumberRowColorMode);
        assertEquals(AdditionalNumberRowColorMode.FULL_ALPHA, light.additionalNumberRowColorMode);
        assertTrue(dark.keyColorOverrides.containsKey("tap:1"));
        assertTrue(light.keyColorOverrides.containsKey("tap:1"));
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
    public void colorfulForegroundThemesAlsoColorNumberRowLegends() {
        assertThemeHasNumberLegendColor("gmk-dots-dark");
        assertThemeHasNumberLegendColor("gmk-dots-light");
        assertThemeHasNumberLegendColor("marigold-fiesta-dark");
        assertThemeHasNumberLegendColor("marigold-fiesta-light");
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
        assertEquals(0xFFE8A5AE, olivia.accentKeyColor);
        assertEquals(0xFFD9A3AA, oliviaDark.accentKeyColor);
        assertEquals(AdditionalNumberRowColorMode.FULL_ALPHA, olivia.additionalNumberRowColorMode);
        assertEquals(AdditionalNumberRowColorMode.FULL_ALPHA, oliviaDark.additionalNumberRowColorMode);
        assertEquals(0xFFE8A5AE, (int) olivia.keyColorOverrides.get("shift"));
        assertEquals(0xFFE8A5AE, (int) olivia.keyColorOverrides.get("backspace"));
        assertEquals("diff", KeyDisplayOverridePackCatalog
                .overridesForPack(KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                .get(".").value);
        assertEquals("log", KeyDisplayOverridePackCatalog
                .overridesForPack(KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                .get("/").value);
        assertEquals(0xFFEBCB8B, (int) oblivion.keyColorOverrides.get("options"));
        assertEquals(0xFFA3BE8C, (int) oblivion.keyColorOverrides.get("enter"));
        assertEquals(0xFF88C0D0, (int) oblivion.keyColorOverrides.get("."));
        assertEquals(0xFFB48EAD, (int) oblivion.keyColorOverrides.get("/"));
        assertEquals(false, oblivion.keyColorOverrides.containsKey("background:enter"));
        assertEquals(
                oblivion.functionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        oblivion,
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(ModifierIconCatalog.PACK_METROPOLIS_POINTS, metropolis.modifierIconThemePackId);
        assertEquals(0xFF090D12, metropolis.keyboardBackgroundColor);
        assertEquals(0xFF10151B, (int) metropolis.keyColorOverrides.get("background:alpha"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:1"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:2"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:3"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:8"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:9"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("tap:0"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("tap:4"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("tap:5"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("tap:6"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("tap:7"));
        assertEquals(0xFF10151B, (int) metropolis.keyColorOverrides.get("background:tap:1"));
        assertEquals(0xFFFFB000, (int) metropolis.keyColorOverrides.get("background:backspace"));
        assertEquals(0xFFFF4B3E, (int) metropolis.keyColorOverrides.get("background:shift"));
        assertEquals(0xFF66E3C4, (int) metropolis.keyColorOverrides.get("background:enter"));
        assertEquals(true, metropolis.visualEffects.blurEnabled);
        assertEquals(true, metropolis.visualEffects.metallicEnabled);
        assertEquals(true, metropolis.visualEffects.angularPreviewBubble);
    }

    @Test
    public void semanticDingulPresetsUseRoleColorOverrides() {
        assertDingulVowelOverrides("marigold-fiesta-dark");
        assertDingulVowelOverrides("marigold-fiesta-light");
        assertDingulRoleOverrides("gmk-bento");
        assertDingulRoleOverrides("gmk-hammerhead");
        assertDingulRoleOverrides("gmk-8008");
        assertDingulRoleOverrides("gmk-modern-dolch");
    }

    @Test
    public void reportedGmkThemesKeepDingulSpecialLegendsReadable() {
        assertSpecialLegendsReadable("gmk-bento");
        assertSpecialLegendsReadable("gmk-hammerhead");
        assertSpecialLegendsReadable("gmk-8008");
        assertSpecialLegendsReadable("gmk-modern-dolch");
    }

    @Test
    public void threeToneThemesUseCurrentDefaultAccentPlacement() {
        assertDefaultAccentPlacement("gmk-bento");
        assertDefaultAccentPlacement("gmk-hammerhead");
        assertDefaultAccentPlacement("gmk-8008");
    }

    @Test
    public void modernDolchUsesEscAndEnterAccentPlacement() {
        KeyboardThemePreset preset = KeyboardThemePreset.find("gmk-modern-dolch");
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults().withEnglishNumberRow(false));
        int redBackground = settings.keyColorOverrides.get("background:escpoint");
        int greenBackground = settings.keyColorOverrides.get("background:enter");
        int alphaLegend = settings.keyColorOverrides.get("alpha");
        assertEquals(
                "modern dolch dot should use visual enter accent",
                greenBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, new GestureKey(
                        ".",
                        ".",
                        "\"",
                        "`",
                        ",",
                        KeyboardCommands.CMD_NOOP,
                        null)));
        assertEquals(
                "modern dolch dot legend should use alpha legend color",
                alphaLegend,
                KeyboardKeyVisualClassifier.textColorFor(settings, new GestureKey(
                        ".",
                        ".",
                        "\"",
                        "`",
                        ",",
                        KeyboardCommands.CMD_NOOP,
                        null)));

        KeyboardSettings english = settings.withKeyboardMode(KeyboardMode.ENGLISH);
        assertEquals(
                "modern dolch q should use esc point accent when number row is hidden",
                redBackground,
                KeyboardKeyVisualClassifier.colorFor(english, new GestureKey(
                        "q",
                        "q",
                        "Q",
                        null,
                        null,
                        null,
                        "!")));
        assertEquals(
                "modern dolch q legend should use alpha legend color",
                alphaLegend,
                KeyboardKeyVisualClassifier.textColorFor(english, new GestureKey(
                        "q",
                        "q",
                        "Q",
                        null,
                        null,
                        null,
                        "!")));
        assertEquals(
                "modern dolch enter should use accent",
                greenBackground,
                KeyboardKeyVisualClassifier.colorFor(english, GestureKey.command("", KeyboardCommands.CMD_ENTER)));
        assertEquals(
                "modern dolch enter legend should use alpha legend color",
                alphaLegend,
                KeyboardKeyVisualClassifier.textColorFor(english, GestureKey.command("", KeyboardCommands.CMD_ENTER)));
        assertNotEquals(
                "modern dolch language should stay modifier",
                greenBackground,
                KeyboardKeyVisualClassifier.colorFor(english, GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
        assertNotEquals(
                "modern dolch shift should stay modifier",
                greenBackground,
                KeyboardKeyVisualClassifier.colorFor(english, GestureKey.command("", KeyboardCommands.CMD_SHIFT_ONCE)));
        assertNotEquals(
                "modern dolch backspace should stay modifier",
                greenBackground,
                KeyboardKeyVisualClassifier.colorFor(english, GestureKey.command("", KeyboardCommands.CMD_DELETE)));

        KeyboardSettings englishWithNumberRow = preset.applyTo(KeyboardSettings.defaults())
                .withKeyboardMode(KeyboardMode.ENGLISH);
        assertEquals(
                "modern dolch number-row 1 should use esc accent when number row is visible",
                redBackground,
                KeyboardKeyVisualClassifier.colorFor(englishWithNumberRow, new GestureKey(
                        "1",
                        "1",
                        null,
                        null,
                        null,
                        null,
                        null)));
    }

    @Test
    public void marigoldExternalThemeFilesMatchBuiltInPresets() throws IOException {
        assertExternalThemeMatchesPreset("marigold-fiesta-dark");
        assertExternalThemeMatchesPreset("marigold-fiesta-light");
    }

    @Test
    public void externalThemeFilesMatchBuiltInPresetIdentity() throws IOException {
        File themeDir = themeDirectory();
        File[] files = themeDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        for (File file : files) {
            String name = file.getName();
            assertExternalThemeBasicMatchesPreset(name.substring(0, name.length() - ".json".length()));
        }
    }

    @Test
    public void everyThemeJsonFileHasBuiltInPreset() {
        File themeDir = themeDirectory();
        File[] files = themeDir.listFiles((dir, name) -> name.endsWith(".json"));
        assertNotNull(files);

        for (File file : files) {
            String name = file.getName();
            String id = name.substring(0, name.length() - ".json".length());
            assertNotNull("Missing built-in preset for " + name, KeyboardThemePreset.find(id));
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
        assertEquals(builtIn.functionKeyColor, external.functionKeyColor);
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
        assertEquals(id, builtIn.functionKeyColor, external.functionKeyColor);
        assertEquals(id, builtIn.accentKeyColor, external.accentKeyColor);
        assertEquals(id, builtIn.keyPressedColor, external.keyPressedColor);
        assertEquals(id, builtIn.keyboardBackgroundColor, external.keyboardBackgroundColor);
        assertEquals(id, builtIn.borderColor, external.borderColor);
        assertEquals(id, builtIn.depthColor, external.depthColor);
        assertEquals(id, builtIn.modifierIconThemePackId, external.modifierIconThemePackId);
        assertEquals(id, builtIn.keyDisplayThemePackId, external.keyDisplayThemePackId);
        assertEquals(id, builtIn.additionalNumberRowColorMode, external.additionalNumberRowColorMode);
        assertEquals(id, builtIn.visualEffects.blurEnabled, external.visualEffects.blurEnabled);
        assertEquals(id, builtIn.visualEffects.metallicEnabled, external.visualEffects.metallicEnabled);
        assertEquals(id, builtIn.visualEffects.angularPreviewBubble, external.visualEffects.angularPreviewBubble);
        assertEquals(id,
                builtIn.visualEffects.keyFaceGradientEnabled,
                external.visualEffects.keyFaceGradientEnabled);
        assertEquals(id,
                builtIn.visualEffects.keyFaceGradientStrengthPercent,
                external.visualEffects.keyFaceGradientStrengthPercent);
        assertEquals(id,
                builtIn.visualEffects.keyFaceGradientStartColor,
                external.visualEffects.keyFaceGradientStartColor);
        assertEquals(id,
                builtIn.visualEffects.keyFaceGradientEndColor,
                external.visualEffects.keyFaceGradientEndColor);
        assertEquals(id,
                builtIn.visualEffects.keyFaceGradientCurve,
                external.visualEffects.keyFaceGradientCurve);
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

    private void assertDefaultAccentPlacement(String presetId) {
        KeyboardThemePreset preset = KeyboardThemePreset.find(presetId);
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults());

        int accentBackground = KeyboardKeyVisualClassifier.colorFor(
                settings,
                GestureKey.command("", KeyboardCommands.CMD_ENTER));
        assertEquals(
                presetId + " language should use qwerty meta accent",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
        assertEquals(
                presetId + " shift should use qwerty accent",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, GestureKey.command("", KeyboardCommands.CMD_SHIFT_ONCE)));
        assertEquals(
                presetId + " backspace should use qwerty accent",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, GestureKey.command("", KeyboardCommands.CMD_DELETE)));
        assertEquals(
                presetId + " settings should use dingul ctrl accent",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, GestureKey.command("", KeyboardCommands.CMD_SETTINGS)));
        assertEquals(
                presetId + " dot should use dingul visual enter accent",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, new GestureKey(
                        ".",
                        ".",
                        "\"",
                        "`",
                        ",",
                        KeyboardCommands.CMD_NOOP,
                        null)));
        assertNotEquals(
                presetId + " slash should stay modifier by default",
                accentBackground,
                KeyboardKeyVisualClassifier.colorFor(settings, new GestureKey(
                        "/",
                        "/",
                        ":",
                        ";",
                        "@",
                        KeyboardCommands.CMD_NOOP,
                        null)));
    }

    private String readThemeJson(String id) throws IOException {
        File file = new File(themeDirectory(), id + ".json");
        assertTrue("Missing external theme JSON: " + id, file.isFile());
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private void assertDingulRoleOverrides(String presetId) {
        KeyboardThemePreset preset = KeyboardThemePreset.find(presetId);
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults());

        assertTrue(presetId + " missing alpha foreground",
                settings.keyColorOverrides.containsKey("alpha"));
        assertTrue(presetId + " missing alpha background",
                settings.keyColorOverrides.containsKey("background:alpha"));
        assertTrue(presetId + " missing mod foreground",
                settings.keyColorOverrides.containsKey("modifiers"));
        assertTrue(presetId + " missing mod background",
                settings.keyColorOverrides.containsKey("background:modifiers"));
        assertTrue(presetId + " missing mod inv foreground",
                settings.keyColorOverrides.containsKey("modinv"));
        assertTrue(presetId + " missing mod inv background",
                settings.keyColorOverrides.containsKey("background:modinv"));
    }

    private File themeDirectory() {
        File dir = new File("themes");
        if (!dir.isDirectory()) {
            dir = new File("../themes");
        }
        assertTrue("Missing themes directory", dir.isDirectory());
        return dir;
    }

    private void assertThemeHasNumberLegendColor(String presetId) {
        KeyboardThemePreset preset = KeyboardThemePreset.find(presetId);
        assertNotNull(preset);
        KeyboardSettings settings = preset.applyTo(KeyboardSettings.defaults());
        for (int digit = 0; digit <= 9; digit++) {
            assertTrue(presetId + " missing tap:" + digit,
                    settings.keyColorOverrides.containsKey("tap:" + digit));
        }
    }

    private static int brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }
}
