package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyDisplayOverrideResolverTest {
    private static final GestureKey ENTER = new GestureKey(
            "enter", KeyboardCommands.CMD_ENTER, null, null, null, null, null);

    @Test
    public void noveltyOverrideResolvesBeforeOrdinaryDisplayOverride() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        overrides.put("enter", KeyDisplayOverride.text("GO"));
        overrides.put("novelty:enter", KeyDisplayOverride.icon("gmk_iso_enter_mark"));
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyDisplayOverrides(overrides);

        KeyDisplayOverride resolved = KeyDisplayOverrideResolver.resolve(settings, ENTER);
        assertEquals("gmk_iso_enter_mark", resolved.value);
        assertTrue(KeyDisplayOverrideResolver.hasNoveltyOverride(settings, ENTER));
    }

    @Test
    public void ordinaryDisplayOverrideIsNotMarkedAsNovelty() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        overrides.put("enter", KeyDisplayOverride.text("GO"));
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyDisplayOverrides(overrides);

        KeyDisplayOverride resolved = KeyDisplayOverrideResolver.resolve(settings, ENTER);
        assertEquals("GO", resolved.value);
        assertFalse(KeyDisplayOverrideResolver.hasNoveltyOverride(settings, ENTER));
    }
}
