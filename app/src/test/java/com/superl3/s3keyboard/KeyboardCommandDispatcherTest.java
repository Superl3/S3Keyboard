package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class KeyboardCommandDispatcherTest {
    @Test
    public void dispatchesEditingAndTextCommands() {
        RecordingTarget target = new RecordingTarget();

        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_DELETE, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_SPACE, target);
        KeyboardCommandDispatcher.dispatch("가", target);

        assertEquals("delete", target.calls.get(0));
        assertEquals("space", target.calls.get(1));
        assertEquals("text:가", target.calls.get(2));
    }

    @Test
    public void dispatchesParameterizedCommandsWithOriginalValue() {
        RecordingTarget target = new RecordingTarget();

        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_RESERVED_UP, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_REMOTE_ALT_TAB, target);

        assertEquals("reserved:" + KeyboardCommands.CMD_RESERVED_UP, target.calls.get(0));
        assertEquals("remote:" + KeyboardCommands.CMD_REMOTE_ALT_TAB, target.calls.get(1));
    }

    @Test
    public void dispatchesHandednessAsTypedMode() {
        RecordingTarget target = new RecordingTarget();

        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_HAND_LEFT, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_HAND_RIGHT, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_HAND_BALANCED, target);

        assertEquals("hand:LEFT", target.calls.get(0));
        assertEquals("hand:RIGHT", target.calls.get(1));
        assertEquals("hand:BALANCED", target.calls.get(2));
    }

    @Test
    public void ignoresNoopAndMissingTarget() {
        RecordingTarget target = new RecordingTarget();

        KeyboardCommandDispatcher.dispatch(null, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_NOOP, target);
        KeyboardCommandDispatcher.dispatch(KeyboardCommands.CMD_SPACE, null);

        assertEquals(0, target.calls.size());
    }

    private static final class RecordingTarget extends KeyboardCommandDispatcher.Target {
        final List<String> calls = new ArrayList<>();

        @Override
        void delete() {
            calls.add("delete");
        }

        @Override
        void space() {
            calls.add("space");
        }

        @Override
        void enter() {
            calls.add("enter");
        }

        @Override
        void moveLeft() {
            calls.add("left");
        }

        @Override
        void moveRight() {
            calls.add("right");
        }

        @Override
        void toggleLanguage() {
            calls.add("toggleLanguage");
        }

        @Override
        void shiftOnce() {
            calls.add("shiftOnce");
        }

        @Override
        void shiftLock() {
            calls.add("shiftLock");
        }

        @Override
        void reservedPhrase(String command) {
            calls.add("reserved:" + command);
        }

        @Override
        void dingulCenterVowel() {
            calls.add("centerVowel");
        }

        @Override
        void dingulWideVowel() {
            calls.add("wideVowel");
        }

        @Override
        void openOptions() {
            calls.add("openOptions");
        }

        @Override
        void quickSettings() {
            calls.add("quickSettings");
        }

        @Override
        void clipboardPanel() {
            calls.add("clipboard");
        }

        @Override
        void voiceInput() {
            calls.add("voice");
        }

        @Override
        void undo() {
            calls.add("undo");
        }

        @Override
        void tools() {
            calls.add("tools");
        }

        @Override
        void setHandedness(HandednessMode mode) {
            calls.add("hand:" + mode.name());
        }

        @Override
        void inputPicker() {
            calls.add("picker");
        }

        @Override
        void settings() {
            calls.add("settings");
        }

        @Override
        void hide() {
            calls.add("hide");
        }

        @Override
        void remote(String command) {
            calls.add("remote:" + command);
        }

        @Override
        void text(String value) {
            calls.add("text:" + value);
        }
    }
}
