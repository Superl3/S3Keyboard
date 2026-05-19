package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals(0xFFB9B7B0, light.borderColor);
        assertEquals(0xFFCCC9C2, light.depthColor);
        assertEquals(0xFFF3F2EF, light.keyboardBackgroundColor);
        assertEquals(KeyboardSettings.FONT_NOTO_SANS_KR, dark.fontFamily);
        assertEquals(KeyboardSettings.FONT_NOTO_SANS_KR, light.fontFamily);
        assertEquals(true, dark.keyColorOverrides.containsKey(".."));
        assertEquals(true, light.keyColorOverrides.containsKey(".."));
    }

    private static int brightness(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }
}
