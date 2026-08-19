package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class RuntimeDefaultsTest {
    @Test
    public void runtimeImeStatePreservesSessionOverridesWithoutMutatingPersistedSettings() {
        KeyboardSettings persisted = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withRemoteOptions(false, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT)
                .withEnglishNumberRow(false)
                .withEnterKeyLabel("Persisted");
        KeyboardSettings runtime = persisted
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.CTRL_SPACE)
                .withRuntimeNumberRowForced(true);

        KeyboardSettings merged = RuntimeDefaults.withRuntimeImeState(
                persisted,
                runtime,
                "Send",
                true);

        assertEquals(KeyboardMode.ENGLISH, merged.keyboardMode);
        assertTrue(merged.remoteModeEnabled);
        assertEquals(RemoteImeShortcut.CTRL_SPACE, merged.remoteImeShortcut);
        assertTrue(merged.forceNumberRow);
        assertTrue(merged.showNumberRow);
        assertEquals("Send", merged.enterKeyLabel);

        assertEquals(KeyboardMode.HANGUL, persisted.keyboardMode);
        assertFalse(persisted.remoteModeEnabled);
        assertFalse(persisted.forceNumberRow);
        assertFalse(persisted.showEnglishNumberRow);
        assertEquals("Persisted", persisted.enterKeyLabel);
    }
}
