package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class InputAssistanceSettingsControllerTest {
    @Test
    public void releaseModesHideDebugPreset() {
        InputAssistanceMode[] modes = InputAssistanceSettingsController.availableModes(false);

        assertEquals(3, modes.length);
        assertSame(InputAssistanceMode.CUSTOM, modes[0]);
        assertSame(InputAssistanceMode.CLEAN, modes[1]);
        assertSame(InputAssistanceMode.LEARNING, modes[2]);
    }

    @Test
    public void debuggableModesExposeDebugPreset() {
        InputAssistanceMode[] modes = InputAssistanceSettingsController.availableModes(true);

        assertEquals(InputAssistanceMode.values().length, modes.length);
        assertSame(InputAssistanceMode.DEBUG, modes[modes.length - 1]);
    }

    @Test
    public void modeIndexFallsBackToCustomWhenHiddenOrMissing() {
        InputAssistanceMode[] releaseModes = InputAssistanceSettingsController.availableModes(false);

        assertEquals(2, InputAssistanceSettingsController.indexOf(
                releaseModes,
                InputAssistanceMode.LEARNING));
        assertEquals(0, InputAssistanceSettingsController.indexOf(
                releaseModes,
                InputAssistanceMode.DEBUG));
        assertEquals(0, InputAssistanceSettingsController.indexOf(
                null,
                InputAssistanceMode.DEBUG));
    }

    @Test
    public void profileApplicationUpdatesOnlyHintVisibilitySettings() {
        KeyboardSettings base = KeyboardSettings.defaults()
                .withHapticFeedback(false)
                .withGestureThreshold(21);

        KeyboardSettings clean = InputAssistanceSettingsController.settingsForProfile(
                base,
                InputAssistanceMode.CLEAN.profile);
        KeyboardSettings learning = InputAssistanceSettingsController.settingsForProfile(
                base,
                InputAssistanceMode.LEARNING.profile);

        assertFalse(clean.showHangulSlideHints);
        assertFalse(clean.showEnglishSlideHints);
        assertFalse(clean.showBeginnerTooltipPreview);
        assertTrue(learning.showHangulSlideHints);
        assertTrue(learning.showEnglishSlideHints);
        assertTrue(learning.showBeginnerTooltipPreview);
        assertFalse(clean.hapticFeedbackEnabled);
        assertEquals(21, clean.gestureThresholdDp);
    }

    @Test
    public void modeApplicationReturnsRecommendedErgonomicsPreset() {
        KeyboardErgonomicsOptions custom = KeyboardErgonomicsPreset.AGGRESSIVE.options
                .withCompactFunctionRail(false);

        assertEquals(
                KeyboardErgonomicsPreset.LEGACY.options,
                InputAssistanceSettingsController.ergonomicsForMode(
                        custom,
                        InputAssistanceMode.CLEAN));
        assertEquals(
                KeyboardErgonomicsPreset.STABLE.options,
                InputAssistanceSettingsController.ergonomicsForMode(
                        custom,
                        InputAssistanceMode.LEARNING));
        assertEquals(
                KeyboardErgonomicsPreset.AGGRESSIVE.options,
                InputAssistanceSettingsController.ergonomicsForMode(
                        custom,
                        InputAssistanceMode.DEBUG));
        assertEquals(
                custom,
                InputAssistanceSettingsController.ergonomicsForMode(
                        custom,
                        InputAssistanceMode.CUSTOM));
    }
}
