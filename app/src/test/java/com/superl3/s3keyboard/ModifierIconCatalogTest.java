package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class ModifierIconCatalogTest {
    @Test
    public void monoPackUsesThemeForegroundColor() {
        assertEquals(
                0xFF123456,
                ModifierIconCatalog.colorForPack(ModifierIconCatalog.PACK_LINE_MONO, 0xFF123456));
    }

    @Test
    public void coloredPackIgnoresThemeForegroundColor() {
        int themed = ModifierIconCatalog.colorForPack(ModifierIconCatalog.PACK_ACCENT_COLOR, 0xFF123456);

        assertNotEquals(0xFF123456, themed);
        assertEquals(themed, ModifierIconCatalog.colorForPack(ModifierIconCatalog.PACK_ACCENT_COLOR, 0xFFABCDEF));
    }

    @Test
    public void userOverridePackWinsOverThemePack() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withModifierIconThemePack(ModifierIconCatalog.PACK_ACCENT_COLOR)
                .withModifierIconOverridePack(ModifierIconCatalog.PACK_LINE_MONO);

        assertEquals(ModifierIconCatalog.PACK_LINE_MONO, ModifierIconCatalog.effectivePackId(settings));
    }

    @Test
    public void themedSamplePacksAreSelectableAndNormalized() {
        assertEquals(
                ModifierIconCatalog.PACK_DOTS_LINES,
                ModifierIconCatalog.normalizePackId(ModifierIconCatalog.PACK_DOTS_LINES));
        assertEquals(
                ModifierIconCatalog.PACK_METROPOLIS_POINTS,
                ModifierIconCatalog.normalizePackId(ModifierIconCatalog.PACK_METROPOLIS_POINTS));
    }

    @Test
    public void simpleTextPackIsAKeyDisplayOverridePack() {
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT,
                KeyDisplayOverridePackCatalog.normalizePackId(
                        KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT));
        assertEquals(
                "hihihi",
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT)
                        .get("label:.")
                        .value);
        assertNull(KeyDisplayOverridePackCatalog.overridesForPack(
                KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT)
                .get("enter"));
        assertNull(KeyDisplayOverridePackCatalog.overridesForPack(
                KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT)
                .get("backspace"));
    }

    @Test
    public void gitCommandPackIsAKeyDisplayOverridePack() {
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS,
                KeyDisplayOverridePackCatalog.normalizePackId(
                        KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS));
        assertEquals(
                "exec",
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                        .get("enter")
                        .value);
        assertEquals(
                "fetch",
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                        .get("language")
                        .value);
        assertEquals(
                "pull",
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                        .get("space")
                        .value);
        assertEquals(
                "rebase",
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS)
                        .get("shift")
                .value);
    }

    @Test
    public void pointDisplayPacksExposeSemanticGlyphs() {
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_GEO_POINTS,
                KeyDisplayOverridePackCatalog.normalizePackId(
                        KeyDisplayOverridePackCatalog.PACK_GEO_POINTS));
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_CHEVRON_UP,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GEO_POINTS)
                        .get("shift")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_SPACE_DOTS,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_SOFT_SYMBOLS)
                        .get("space")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_SLASH_DOT,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_PUNCTUATION_POINTS)
                        .get("label:/")
                        .value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_FULL_DECORATIVE)
                        .get("alpha")
                        .value);
    }

    @Test
    public void keyboardDisplayPacksExposeKeyboardGlyphs() {
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_KEYBOARD_SYMBOLS,
                KeyDisplayOverridePackCatalog.normalizePackId(
                        KeyDisplayOverridePackCatalog.PACK_KEYBOARD_SYMBOLS));
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_KEYBOARD_SYMBOLS)
                        .get("enter")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_COMMAND,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_KEYBOARD_SYMBOLS)
                        .get("settings")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_LEFT,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_KEYBOARD_NAVIGATION)
                        .get("options")
                        .value);
    }

    @Test
    public void gmkStyleDisplayPacksExposeGenericNoveltyGlyphs() {
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_POINTS,
                KeyDisplayOverridePackCatalog.normalizePackId(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_POINTS));
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_POINTS)
                        .get("space")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_FLOWER,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_NOVELTIES)
                        .get("options")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_PIXEL_STEPS,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_MACROS)
                        .get("reserved")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_CONSTELLATION,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_CELESTIAL)
                        .get("shift")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_FLOWER_ALT,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_NATURE)
                        .get("enter")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_GMK_SPLIT_BAR,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_GMK_STYLE_SPACEBARS)
                        .get("space")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_FONT_SYMBOLS)
                        .get("enter")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_IMAGE_MASK_MARKS)
                        .get("enter")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_TALL_MOD_GLYPHS)
                        .get("enter")
                        .value);
        assertEquals(
                DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW,
                KeyDisplayOverridePackCatalog.overridesForPack(
                        KeyDisplayOverridePackCatalog.PACK_MIXED_SOURCE_NOVELTIES)
                        .get("enter")
                        .value);
    }

    @Test
    public void importedSourcePacksKeepStrongModifiersSemantic() {
        assertStrongModifierGlyphs(
                KeyDisplayOverridePackCatalog.PACK_FONT_SYMBOLS,
                DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_DELETE_LEFT,
                DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_KEYBOARD);
        assertStrongModifierGlyphs(
                KeyDisplayOverridePackCatalog.PACK_IMAGE_MASK_MARKS,
                DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE,
                DecorativeGlyphCatalog.GLYPH_IMG_SIDE_NOTCH,
                DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB,
                DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS);
        assertStrongModifierGlyphs(
                KeyDisplayOverridePackCatalog.PACK_TALL_MOD_GLYPHS,
                DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE,
                DecorativeGlyphCatalog.GLYPH_IMG_TALL_BRACKET,
                DecorativeGlyphCatalog.GLYPH_IMG_VERTICAL_RIBBON,
                DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS);
        assertStrongModifierGlyphs(
                KeyDisplayOverridePackCatalog.PACK_MIXED_SOURCE_NOVELTIES,
                DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_DELETE_LEFT,
                DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW,
                DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS);
    }

    @Test
    public void pointKeycapDoesNotReplaceMainLegendWithNestedEscIcon() {
        KeyboardSettings english = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false)
                .withPointKeycapStyle(true);

        GestureKey q = new GestureKey("q", "q", null, null, null, null, null);
        KeyDisplayOverride override = KeyDisplayOverrideResolver.resolve(english, q);

        assertEquals(null, override);
    }

    @Test
    public void metropolisPackUsesIntrinsicModifierIconColors() {
        assertEquals(true, ModifierIconCatalog.isColoredPack(ModifierIconCatalog.PACK_METROPOLIS_POINTS));
        assertEquals(0xFFFFB000, ModifierIconCatalog.metropolisColorFor(KeyIcon.OPTIONS));
        assertEquals(0xFFFFB000, ModifierIconCatalog.metropolisColorFor(KeyIcon.BACKSPACE));
        assertEquals(0xFFFF4B3E, ModifierIconCatalog.metropolisColorFor(KeyIcon.SHIFT));
        assertEquals(0xFFFF4B3E, ModifierIconCatalog.metropolisColorFor(KeyIcon.RESERVED));
        assertEquals(0xFF66E3C4, ModifierIconCatalog.metropolisColorFor(KeyIcon.ENTER));
        assertEquals(0xFF66E3C4, ModifierIconCatalog.metropolisColorFor(KeyIcon.SETTINGS));
        assertEquals(0xFF70D7E8, ModifierIconCatalog.metropolisColorFor(KeyIcon.SPACE));
        assertNotEquals(
                ModifierIconCatalog.metropolisColorFor(KeyIcon.SHIFT),
                ModifierIconCatalog.metropolisColorFor(KeyIcon.BACKSPACE));
    }

    private static void assertStrongModifierGlyphs(
            String packId,
            String enter,
            String backspace,
            String shift,
            String space) {
        assertEquals(
                enter,
                KeyDisplayOverridePackCatalog.overridesForPack(packId).get("enter").value);
        assertEquals(
                backspace,
                KeyDisplayOverridePackCatalog.overridesForPack(packId).get("backspace").value);
        assertEquals(
                shift,
                KeyDisplayOverridePackCatalog.overridesForPack(packId).get("shift").value);
        assertEquals(
                space,
                KeyDisplayOverridePackCatalog.overridesForPack(packId).get("space").value);
    }
}
