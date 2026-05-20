package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public final class ThemePreviewSettingsTest {
    @Test
    public void previewSettingsDisableHintsAndNumberRows() {
        ThemeOption option = ThemeOption.buildOptions(null, false)[0];
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHintVisibility(true, true, true)
                .withHangulNumberRow(true)
                .withEnglishNumberRow(true);

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH);

        assertEquals(KeyboardMode.ENGLISH, preview.keyboardMode);
        assertFalse(preview.showHangulSlideHints);
        assertFalse(preview.showEnglishSlideHints);
        assertFalse(preview.showBeginnerTooltipPreview);
        assertFalse(preview.showHangulNumberRow);
        assertFalse(preview.showEnglishNumberRow);
    }

    @Test
    public void previewSettingsUseThemeIconPacks() {
        ThemeOption option = option("gmk-metropolis");
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withModifierIconOverridePack(ModifierIconCatalog.PACK_DOTS_LINES)
                .withKeyDisplayOverridePack(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT);

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH);

        assertEquals(ModifierIconCatalog.PACK_METROPOLIS_POINTS, preview.modifierIconThemePackId);
        assertEquals(ModifierIconCatalog.PACK_THEME_DEFAULT, preview.modifierIconOverridePackId);
        assertEquals(ModifierIconCatalog.PACK_METROPOLIS_POINTS, ModifierIconCatalog.effectivePackId(preview));
        assertEquals(KeyDisplayOverridePackCatalog.PACK_THEME_DEFAULT, preview.keyDisplayOverridePackId);
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
