package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class DecorativeGlyphCatalogTest {
    @Test
    public void dotRadiusUsesKeyHeightAsSourceOfTruth() {
        float shortWide = DecorativeGlyphCatalog.dotRadiusForKeyHeight(50f, 2.1f);
        float shortNarrow = DecorativeGlyphCatalog.dotRadiusForKeyHeight(50f, 2.1f);
        float tall = DecorativeGlyphCatalog.dotRadiusForKeyHeight(80f, 2.1f);

        assertEquals(shortWide, shortNarrow, 0.0001f);
        assertTrue(tall > shortWide);
    }

    @Test
    public void twoDotCenterGapKeepsDotsSeparated() {
        float radius = 5f;

        assertTrue(DecorativeGlyphCatalog.twoDotCenterGap(radius, 0f) > radius * 2f);
    }

    @Test
    public void lineWeightDerivesFromDotRadius() {
        float radius = 5f;

        assertEquals(radius, DecorativeGlyphCatalog.lineWeightForDotRadius(radius)
                * DecorativeGlyphCatalog.DOT_RADIUS_TO_LINE_WEIGHT, 0.0001f);
    }

    @Test
    public void builtInPointGlyphIdsAreExplicitlyKnown() {
        assertTrue(DecorativeGlyphCatalog.isBuiltInPointGlyph(DecorativeGlyphCatalog.GLYPH_RING));
        assertTrue(DecorativeGlyphCatalog.isBuiltInPointGlyph(DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT));
        assertTrue(DecorativeGlyphCatalog.isBuiltInPointGlyph(DecorativeGlyphCatalog.GLYPH_SPACE_DOTS));
        assertFalse(DecorativeGlyphCatalog.isBuiltInPointGlyph("custom-pack-image"));
    }

    @Test
    public void builtInKeyboardGlyphIdsAreExplicitlyKnown() {
        assertTrue(DecorativeGlyphCatalog.isBuiltInKeyboardGlyph(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN));
        assertTrue(DecorativeGlyphCatalog.isBuiltInKeyboardGlyph(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_COMMAND));
        assertTrue(DecorativeGlyphCatalog.isBuiltInKeyboardGlyph(
                DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_RIGHT));
        assertFalse(DecorativeGlyphCatalog.isBuiltInKeyboardGlyph("keyboard_custom"));
    }

    @Test
    public void builtInGmkStyleGlyphIdsAreExplicitlyKnown() {
        assertTrue(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(
                DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_BAR));
        assertTrue(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(
                DecorativeGlyphCatalog.GLYPH_GMK_FLOWER));
        assertTrue(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(
                DecorativeGlyphCatalog.GLYPH_GMK_PIXEL_STEPS));
        assertTrue(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(
                DecorativeGlyphCatalog.GLYPH_GMK_CONSTELLATION));
        assertTrue(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(
                DecorativeGlyphCatalog.GLYPH_GMK_LAB_FLASK));
        assertFalse(DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph("gmk_exact_set_logo"));
    }

    @Test
    public void importedSourceGlyphsAreExplicitlyKnown() {
        String[] fontGlyphs = {
                DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_TAB_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_BACK_TAB,
                DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW,
                DecorativeGlyphCatalog.GLYPH_FONT_DELETE_LEFT,
                DecorativeGlyphCatalog.GLYPH_FONT_DELETE_RIGHT,
                DecorativeGlyphCatalog.GLYPH_FONT_COMMAND,
                DecorativeGlyphCatalog.GLYPH_FONT_OPTION,
                DecorativeGlyphCatalog.GLYPH_FONT_CONTROL,
                DecorativeGlyphCatalog.GLYPH_FONT_ESCAPE,
                DecorativeGlyphCatalog.GLYPH_FONT_HOME,
                DecorativeGlyphCatalog.GLYPH_FONT_END,
                DecorativeGlyphCatalog.GLYPH_FONT_PAGE_UP,
                DecorativeGlyphCatalog.GLYPH_FONT_PAGE_DOWN,
                DecorativeGlyphCatalog.GLYPH_FONT_POWER,
                DecorativeGlyphCatalog.GLYPH_FONT_EJECT,
                DecorativeGlyphCatalog.GLYPH_FONT_PLAY_PAUSE,
                DecorativeGlyphCatalog.GLYPH_FONT_RECORD,
                DecorativeGlyphCatalog.GLYPH_FONT_REWIND,
                DecorativeGlyphCatalog.GLYPH_FONT_FAST_FORWARD,
                DecorativeGlyphCatalog.GLYPH_FONT_TRIANGLE_UP,
                DecorativeGlyphCatalog.GLYPH_FONT_TRIANGLE_DOWN,
                DecorativeGlyphCatalog.GLYPH_FONT_STAR_OUTLINE,
                DecorativeGlyphCatalog.GLYPH_FONT_STAR_SOLID,
                DecorativeGlyphCatalog.GLYPH_FONT_KEYBOARD
        };
        String[] imageGlyphs = {
                DecorativeGlyphCatalog.GLYPH_IMG_TALL_CAPSULE,
                DecorativeGlyphCatalog.GLYPH_IMG_VERTICAL_RIBBON,
                DecorativeGlyphCatalog.GLYPH_IMG_SPLIT_PILL,
                DecorativeGlyphCatalog.GLYPH_IMG_KEYHOLE,
                DecorativeGlyphCatalog.GLYPH_IMG_BADGE_CUT,
                DecorativeGlyphCatalog.GLYPH_IMG_SIDE_NOTCH,
                DecorativeGlyphCatalog.GLYPH_IMG_STACKED_TILES,
                DecorativeGlyphCatalog.GLYPH_IMG_FOLDED_CORNER,
                DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB,
                DecorativeGlyphCatalog.GLYPH_IMG_TALL_BRACKET,
                DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS,
                DecorativeGlyphCatalog.GLYPH_IMG_LADDER,
                DecorativeGlyphCatalog.GLYPH_IMG_DUAL_POSTS,
                DecorativeGlyphCatalog.GLYPH_IMG_PIN_DROP,
                DecorativeGlyphCatalog.GLYPH_IMG_TICKET,
                DecorativeGlyphCatalog.GLYPH_IMG_LEAF_SLAB,
                DecorativeGlyphCatalog.GLYPH_IMG_BLOB_STAR,
                DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE,
                DecorativeGlyphCatalog.GLYPH_IMG_CORNER_FRAME,
                DecorativeGlyphCatalog.GLYPH_IMG_CAPSULE_DOTS,
                DecorativeGlyphCatalog.GLYPH_IMG_WAVE_TILE,
                DecorativeGlyphCatalog.GLYPH_IMG_DIAMOND_STACK,
                DecorativeGlyphCatalog.GLYPH_IMG_TALL_ORBIT,
                DecorativeGlyphCatalog.GLYPH_IMG_PUNCH_CARD,
                DecorativeGlyphCatalog.GLYPH_IMG_SOFT_CROSS
        };

        assertEquals(25, fontGlyphs.length);
        assertEquals(25, imageGlyphs.length);
        for (String glyph : fontGlyphs) {
            assertTrue(DecorativeGlyphCatalog.isBuiltInFontGlyph(glyph));
            assertEquals(DecorativeGlyphCatalog.SOURCE_FONT,
                    DecorativeGlyphCatalog.glyphSourceType(glyph));
            assertFalse(DecorativeGlyphCatalog.fontGlyphText(glyph).isEmpty());
        }
        for (String glyph : imageGlyphs) {
            assertTrue(DecorativeGlyphCatalog.isBuiltInImageMaskGlyph(glyph));
            assertEquals(DecorativeGlyphCatalog.SOURCE_IMAGE_MASK,
                    DecorativeGlyphCatalog.glyphSourceType(glyph));
            assertTrue(DecorativeGlyphCatalog.glyphAspectRatio(glyph) < 1.0f);
        }
    }
}
