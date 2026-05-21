package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public final class ThemeOptionTest {
    @Test
    public void buildOptionsCanIncludeCurrentCustomSlot() {
        UserThemeStore.UserTheme[] customThemes = {
                new UserThemeStore.UserTheme("custom-1", "My Theme", "{}")
        };

        ThemeOption[] options = ThemeOption.buildOptions(customThemes, true);

        assertEquals("Current custom", options[0].label);
        assertEquals("", options[0].stableId());
        assertEquals(KeyboardThemePreset.PRESETS[0].displayName, options[1].label);
        assertEquals("My Theme", options[options.length - 1].label);
        assertEquals("custom-1", options[options.length - 1].stableId());
    }

    @Test
    public void buildOptionsCanOmitCurrentCustomSlot() {
        ThemeOption[] options = ThemeOption.buildOptions(null, false);

        assertEquals(KeyboardThemePreset.PRESETS.length, options.length);
        assertNotNull(options[0].preset);
        assertEquals(KeyboardThemePreset.PRESETS[0].id, options[0].stableId());
    }

    @Test
    public void themeSelectionDoesNotCarryPreviousThemeAppearance() {
        ThemeOption metropolis = option("gmk-metropolis");
        ThemeOption clean = option("ios-clean-light");
        KeyboardSettings contaminated = metropolis.applyTo(KeyboardSettings.defaults())
                .withHeights(333, 222)
                .withTypography(
                        KeyboardSettings.FONT_D2CODING,
                        116,
                        91,
                        true,
                        false,
                        true,
                        false)
                .withModifierIconOverridePack(ModifierIconCatalog.PACK_DOTS_LINES)
                .withKeyDisplayOverridePack(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT);

        KeyboardSettings next = clean.applyTo(contaminated);

        assertEquals(333, next.hangulKeyboardHeightDp);
        assertEquals(222, next.englishKeyboardHeightDp);
        assertEquals(0xFFFBFBFD, next.keyIdleColor);
        assertEquals(0xFFFBFBFD, (int) next.keyColorOverrides.get("background:alpha"));
        assertFalse(next.keyColorOverrides.containsKey("tap:1"));
        assertEquals(ModifierIconCatalog.PACK_LINE_MONO, next.modifierIconThemePackId);
        assertEquals(ModifierIconCatalog.PACK_THEME_DEFAULT, next.modifierIconOverridePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_NONE, next.keyDisplayThemePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_THEME_DEFAULT, next.keyDisplayOverridePackId);
        assertFalse(next.visualEffects.blurEnabled);
        assertEquals(KeyboardSettings.FONT_D2CODING, next.fontFamily);
        assertEquals(116, next.primaryTextSizePercent);
        assertEquals(true, next.primaryTextBold);
    }

    @Test
    public void themeSelectionCanFollowThemeTypographyWhenEnabled() {
        ThemeOption metropolis = option("gmk-metropolis");
        KeyboardSettings custom = KeyboardSettings.defaults()
                .withTypography(
                        KeyboardSettings.FONT_NOTO_SANS_KR,
                        120,
                        110,
                        true,
                        false,
                        true,
                        false)
                .withFollowThemeTypography(true);

        KeyboardSettings themed = metropolis.applyTo(custom);

        assertEquals(KeyboardSettings.FONT_D2CODING, themed.fontFamily);
        assertEquals(78, themed.primaryTextSizePercent);
        assertEquals(false, themed.primaryTextBold);
        assertEquals(true, themed.followThemeTypography);
    }

    @Test
    public void resetToDefaultRestoresDefaultAppearanceOnly() {
        KeyboardSettings themed = option("gmk-metropolis")
                .applyTo(KeyboardSettings.defaults().withHeights(333, 222));

        KeyboardSettings reset = ThemeOption.resetToDefaultAppearance(themed);

        assertEquals(333, reset.hangulKeyboardHeightDp);
        assertEquals(222, reset.englishKeyboardHeightDp);
        assertEquals(KeyboardSettings.DEFAULT_KEY_IDLE_COLOR, reset.keyIdleColor);
        assertEquals(KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR, reset.keyboardBackgroundColor);
        assertEquals(ModifierIconCatalog.PACK_LINE_MONO, reset.modifierIconThemePackId);
        assertEquals(KeyDisplayOverridePackCatalog.PACK_NONE, reset.keyDisplayThemePackId);
        assertFalse(reset.visualEffects.blurEnabled);
        assertFalse(reset.visualEffects.metallicEnabled);
    }

    private ThemeOption option(String id) {
        ThemeOption[] options = ThemeOption.buildOptions(null, false);
        for (ThemeOption option : options) {
            if (id.equals(option.stableId())) {
                return option;
            }
        }
        throw new AssertionError("Missing theme option: " + id);
    }
}
