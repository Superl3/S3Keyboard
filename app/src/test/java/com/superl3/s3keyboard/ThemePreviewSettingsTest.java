package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.util.EnumSet;

import org.junit.Test;

public final class ThemePreviewSettingsTest {
    @Test
    public void previewSettingsDisableHintsAndKeepQwertyNumberRow() {
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
        assertEquals(true, preview.showEnglishNumberRow);
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

    @Test
    public void previewSettingsApplyUserAccentPlacementWhenNotThemeDefault() {
        ThemeOption option = option("gmk-modern-dolch");
        KeyboardSettings settings = KeyboardSettings.defaults();

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH,
                AccentPlacementPolicy.of(EnumSet.of(AccentPlacementTarget.META)));

        assertEquals(
                preview.accentKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        preview,
                        GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
        assertNotEquals(
                preview.accentKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        preview,
                        GestureKey.command("", KeyboardCommands.CMD_ENTER)));
    }

    @Test
    public void previewSettingsIgnoreUserAccentPlacementForLockedColorfulThemes() {
        ThemeOption option = option("gmk-dots-dark");
        KeyboardSettings settings = KeyboardSettings.defaults();

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH,
                AccentPlacementPolicy.of(EnumSet.of(AccentPlacementTarget.META)));

        assertNotEquals(
                preview.accentKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        preview,
                        GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
    }

    @Test
    public void previewSettingsUseThemeAccentPlacementByDefault() {
        ThemeOption option = option("gmk-modern-dolch");

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                KeyboardSettings.defaults(),
                KeyboardMode.ENGLISH,
                AccentPlacementPolicy.themeDefault());

        assertEquals(
                preview.accentKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        preview,
                        GestureKey.command("", KeyboardCommands.CMD_ENTER)));
        assertEquals(
                preview.accentKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        preview,
                        GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
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
