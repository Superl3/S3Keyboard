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
}
