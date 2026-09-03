package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

public final class ThemeManagementModelTest {
    @Test
    public void filtersBySearchMaterialAndToneWithoutAddingThemes() {
        ThemeOption[] all = ThemeOption.buildOptions(null, false);
        assertEquals(42, all.length);

        ThemeOption[] result = ThemeManagementModel.filterAndOrder(
                all, "nord", KeyboardVisualEffects.MATERIAL_FROSTED,
                ThemeManagementModel.TONE_DARK, Collections.emptySet(),
                Collections.emptyList(), false, false);

        assertEquals(1, result.length);
        assertEquals("nord-frost-night", result[0].stableId());
    }
    @Test
    public void favoriteAndRecentOrderingIsStable() {
        ThemeOption[] all = ThemeOption.buildOptions(null, false);
        ThemeOption[] result = ThemeManagementModel.filterAndOrder(
                all, "", "", ThemeManagementModel.TONE_ALL,
                new HashSet<>(Collections.singletonList("gmk-olivia-light")),
                Arrays.asList("nord-night", "gmk-metropolis"), false, false);

        assertEquals("gmk-olivia-light", result[0].stableId());
        assertEquals("nord-night", result[1].stableId());
        assertEquals("gmk-metropolis", result[2].stableId());
    }

    @Test
    public void pairedThemeFallsBackToManualAndPreservesManualOverride() {
        assertEquals("manual", ThemeManagementModel.resolvePairedThemeId(
                false, true, "manual", "light", "dark"));
        assertEquals("dark", ThemeManagementModel.resolvePairedThemeId(
                true, true, "manual", "light", "dark"));
        assertEquals("light", ThemeManagementModel.resolvePairedThemeId(
                true, false, "manual", "light", "dark"));
        assertEquals("manual", ThemeManagementModel.resolvePairedThemeId(
                true, true, "manual", "light", ""));
    }

    @Test
    public void toneClassificationMatchesKnownLightAndDarkThemes() {
        ThemeOption[] all = ThemeOption.buildOptions(null, false);
        ThemeOption light = ThemeOption.at(all, ThemeOption.indexOfStableId(all, "gmk-olivia-light", -1));
        ThemeOption dark = ThemeOption.at(all, ThemeOption.indexOfStableId(all, "gmk-olivia-dark", -1));

        assertFalse(ThemeManagementModel.isDark(light));
        assertTrue(ThemeManagementModel.isDark(dark));
    }
}
