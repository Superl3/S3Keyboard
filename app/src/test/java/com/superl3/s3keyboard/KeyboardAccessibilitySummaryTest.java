package com.superl3.s3keyboard;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardAccessibilitySummaryTest {
    @Test
    public void fallbackDescribesDingulKeyboardWithPreviewState() {
        String summary = KeyboardAccessibilitySummary.describe(
                KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL),
                KeyboardSurface.NORMAL,
                20,
                false);

        assertTrue(summary.contains("New Dingul keyboard"));
        assertTrue(summary.contains("Hangul Dingul"));
        assertTrue(summary.contains("keys 20"));
        assertTrue(summary.contains("input preview on"));
    }

    @Test
    public void fallbackDescribesRemoteQwertyAndDebugOverlay() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT)
                .withHintVisibility(false, false, false);

        String summary = KeyboardAccessibilitySummary.describe(
                settings,
                KeyboardSurface.NORMAL,
                34,
                true);

        assertTrue(summary.contains("English QWERTY"));
        assertTrue(summary.contains("remote mode"));
        assertTrue(summary.contains("key bounds overlay on"));
    }

    @Test
    public void fallbackSurfaceOverridesNormalModeLabel() {
        String summary = KeyboardAccessibilitySummary.describe(
                KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL),
                KeyboardSurface.PASSWORD_SAFE,
                24,
                false);

        assertTrue(summary.contains("password input"));
    }

    @Test
    public void fallbackDescribesWebInputSurface() {
        String summary = KeyboardAccessibilitySummary.describe(
                KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL),
                KeyboardSurface.WEB_EXTENDED,
                30,
                false);

        assertTrue(summary.contains("web input"));
    }
}
