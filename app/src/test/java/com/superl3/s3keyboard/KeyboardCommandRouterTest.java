package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

public final class KeyboardCommandRouterTest {
    @Test
    public void routesCoreEditingCommands() {
        assertEquals(KeyboardCommandRoute.DELETE, KeyboardCommandRouter.route(KeyboardCommands.CMD_DELETE));
        assertEquals(KeyboardCommandRoute.SPACE, KeyboardCommandRouter.route(KeyboardCommands.CMD_SPACE));
        assertEquals(KeyboardCommandRoute.ENTER, KeyboardCommandRouter.route(KeyboardCommands.CMD_ENTER));
        assertEquals(KeyboardCommandRoute.NEWLINE, KeyboardCommandRouter.route(KeyboardCommands.CMD_NEWLINE));
        assertEquals(KeyboardCommandRoute.MOVE_LEFT, KeyboardCommandRouter.route(KeyboardCommands.CMD_MOVE_LEFT));
        assertEquals(KeyboardCommandRoute.MOVE_RIGHT, KeyboardCommandRouter.route(KeyboardCommands.CMD_MOVE_RIGHT));
    }

    @Test
    public void groupsReservedAndRemoteCommands() {
        assertEquals(
                KeyboardCommandRoute.RESERVED_PHRASE,
                KeyboardCommandRouter.route(KeyboardCommands.CMD_RESERVED_LEFT));
        assertEquals(
                KeyboardCommandRoute.REMOTE,
                KeyboardCommandRouter.route(KeyboardCommands.CMD_REMOTE_ALT_TAB));
        assertEquals(
                KeyboardCommandRoute.REMOTE,
                KeyboardCommandRouter.route(KeyboardCommands.CMD_REMOTE_F12));
    }

    @Test
    public void routesNullEmptyAndPlainTextSafely() {
        assertEquals(KeyboardCommandRoute.NOOP, KeyboardCommandRouter.route(null));
        assertEquals(KeyboardCommandRoute.NOOP, KeyboardCommandRouter.route(""));
        assertEquals(KeyboardCommandRoute.NOOP, KeyboardCommandRouter.route(KeyboardCommands.CMD_NOOP));
        assertEquals(KeyboardCommandRoute.TEXT, KeyboardCommandRouter.route("가"));
        assertEquals(KeyboardCommandRoute.TEXT, KeyboardCommandRouter.route("abc"));
        assertEquals(
                KeyboardCommandRoute.NOOP,
                KeyboardCommandRouter.route("__unregistered_internal_command__"));
    }

    @Test
    public void everyDeclaredCommandHasAnExplicitRuntimeRoute() throws IllegalAccessException {
        for (Field field : KeyboardCommands.class.getDeclaredFields()) {
            if (field.getType() != String.class
                    || !Modifier.isStatic(field.getModifiers())
                    || !field.getName().startsWith("CMD_")) {
                continue;
            }
            String command = (String) field.get(null);
            KeyboardCommandRoute route = KeyboardCommandRouter.route(command);
            if (KeyboardCommands.CMD_NOOP.equals(command)) {
                assertEquals(KeyboardCommandRoute.NOOP, route);
                continue;
            }
            assertNotEquals("unrouted command: " + field.getName(), KeyboardCommandRoute.NOOP, route);
            assertNotEquals("command leaked as text: " + field.getName(), KeyboardCommandRoute.TEXT, route);
        }
    }

    @Test
    public void localUtilityCommandsHaveReadableLabelResources() {
        assertEquals(
                R.string.command_quick_settings,
                KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_QUICK_SETTINGS));
        assertEquals(
                R.string.command_clipboard_panel,
                KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_CLIPBOARD_PANEL));
        assertEquals(
                R.string.command_voice_input,
                KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_VOICE_INPUT));
        assertEquals(
                R.string.command_undo,
                KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_UNDO));
        assertEquals(
                R.string.command_tools,
                KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_TOOLS));
    }

    @Test
    public void visibleCommandsHaveLabelResources() {
        String[] visibleCommands = {
                KeyboardCommands.CMD_DELETE,
                KeyboardCommands.CMD_SPACE,
                KeyboardCommands.CMD_ENTER,
                KeyboardCommands.CMD_NEWLINE,
                KeyboardCommands.CMD_MOVE_LEFT,
                KeyboardCommands.CMD_MOVE_RIGHT,
                KeyboardCommands.CMD_INPUT_PICKER,
                KeyboardCommands.CMD_SETTINGS,
                KeyboardCommands.CMD_HIDE,
                KeyboardCommands.CMD_TOGGLE_LANGUAGE,
                KeyboardCommands.CMD_RESERVED_PHRASES,
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                KeyboardCommands.CMD_OPEN_OPTIONS,
                KeyboardCommands.CMD_SHIFT_ONCE,
                KeyboardCommands.CMD_SHIFT_LOCK,
                KeyboardCommands.CMD_REMOTE_ESC,
                KeyboardCommands.CMD_REMOTE_TAB,
                KeyboardCommands.CMD_REMOTE_CTRL_LATCH,
                KeyboardCommands.CMD_REMOTE_WIN_LATCH,
                KeyboardCommands.CMD_REMOTE_ALT_LATCH,
                KeyboardCommands.CMD_REMOTE_IME_TOGGLE,
                KeyboardCommands.CMD_REMOTE_PAGE_UP,
                KeyboardCommands.CMD_REMOTE_PAGE_DOWN,
                KeyboardCommands.CMD_REMOTE_CTRL_ENTER,
                KeyboardCommands.CMD_REMOTE_F1,
                KeyboardCommands.CMD_REMOTE_F12
        };

        for (String command : visibleCommands) {
            assertEquals("missing label resource for " + command,
                    false,
                    KeyboardCommandLabels.labelResIdFor(command) == 0);
        }
    }

    @Test
    public void nonVisibleCommandsHaveNoLabelResource() {
        assertEquals(0, KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_NOOP));
        assertEquals(0, KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_RESERVED_LEFT));
        assertEquals(0, KeyboardCommandLabels.labelResIdFor(KeyboardCommands.CMD_HAND_LEFT));
        assertEquals(0, KeyboardCommandLabels.labelResIdFor("plain"));
    }
}
