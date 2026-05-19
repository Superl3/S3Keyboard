package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
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
}
