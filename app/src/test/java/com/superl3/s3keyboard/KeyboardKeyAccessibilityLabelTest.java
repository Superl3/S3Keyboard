package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardKeyAccessibilityLabelTest {
    @Test
    public void textKeyIncludesTapAndSlideOutputs() {
        GestureKey key = new GestureKey("a", "a", "A", "!", null, null, null);

        String description = KeyboardKeyAccessibilityLabel.describe(key);

        assertTrue(description.contains("a"));
        assertTrue(description.contains("up A"));
        assertTrue(description.contains("down !"));
    }

    @Test
    public void fallbackSlideValuesAreNotRepeated() {
        GestureKey key = new GestureKey("space", KeyboardCommands.CMD_SPACE, null, null, null, null, null);

        String description = KeyboardKeyAccessibilityLabel.describe(key);

        assertFalse(description.contains("up"));
        assertFalse(description.contains("down"));
        assertFalse(description.contains("left"));
        assertFalse(description.contains("right"));
    }

    @Test
    public void noopAndEmptyOutputsAreSkipped() {
        GestureKey key = new GestureKey("x", "x", KeyboardCommands.CMD_NOOP, "", null, null, null);

        String description = KeyboardKeyAccessibilityLabel.describe(key);

        assertFalse(description.contains("noop"));
        assertFalse(description.contains("up"));
    }

    @Test
    public void leftAssistRailKeysUseCommandAsLabelSource() {
        GestureKey key = LeftAssistRailItem.keyForRow(0);

        assertEquals(KeyboardCommands.CMD_CLIPBOARD_PANEL, key.label);
        assertEquals(KeyboardCommands.CMD_CLIPBOARD_PANEL, key.tap);
        assertFalse("Assist rail must not carry English display labels.", "Clip".equals(key.label));
    }

    @Test
    public void customAccessibilityActionNamesTheGestureAndExactMappedValue() {
        assertEquals(
                "up A",
                KeyboardKeyAccessibilityLabel.actionDescription(
                        null,
                        GestureAction.UP,
                        "A"));
        assertEquals(
                "long press !",
                KeyboardKeyAccessibilityLabel.actionDescription(
                        null,
                        GestureAction.LONG_PRESS,
                        "!"));
        assertEquals(
                null,
                KeyboardKeyAccessibilityLabel.actionDescription(
                        null,
                        GestureAction.LEFT,
                        null));
    }
}
