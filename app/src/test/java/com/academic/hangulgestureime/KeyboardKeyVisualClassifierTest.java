package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyboardKeyVisualClassifierTest {
    @Test
    public void spacebarUsesNormalKeyRole() {
        GestureKey space = new GestureKey(
                "Space",
                KeyboardCommands.CMD_SPACE,
                null,
                null,
                KeyboardCommands.CMD_MOVE_LEFT,
                KeyboardCommands.CMD_MOVE_RIGHT,
                null,
                10,
                KeyIcon.SPACE);

        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(KeyboardSettings.defaults(), space));
    }

    @Test
    public void onlyBackspaceEnterAndShiftUsePrimaryFunctionRole() {
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Delete", KeyboardCommands.CMD_DELETE, 3)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH),
                        GestureKey.command(
                                "Shift",
                                KeyboardCommands.CMD_SHIFT_ONCE,
                                KeyboardCommands.CMD_SHIFT_LOCK,
                                3,
                                KeyIcon.SHIFT)));
    }

    @Test
    public void optionReservedAndLanguageUseFunctionRole() {
        assertEquals(
                KeyVisualRole.FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                KeyVisualRole.FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Reserved", KeyboardCommands.CMD_RESERVED_PHRASES, 2)));
        assertEquals(
                KeyVisualRole.FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Language", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2)));
    }

    @Test
    public void hangulSemicolonAndSlashSpecialKeysUseAccentRole() {
        KeyboardSettings hangul = KeyboardSettings.defaults();

        assertEquals(
                KeyVisualRole.ACCENT,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.ACCENT,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(". .", ".", null, null, null, null, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul.withKeyboardMode(KeyboardMode.ENGLISH),
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
    }

    @Test
    public void keyColorOverrideTintsTextWithoutChangingRoleOrBackground() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:a", 0x00123456);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withKeyColorOverrides(overrides);
        GestureKey key = new GestureKey("a", "a", "A", "-", null, null, "-", 2);

        assertEquals(KeyVisualRole.NORMAL, KeyboardKeyVisualClassifier.roleFor(settings, key));
        assertEquals(settings.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(settings, key));
        assertEquals(0xFF123456, KeyboardKeyVisualClassifier.textColorFor(settings, key));
    }
}
