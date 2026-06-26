package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class InputAssistanceModeTest {
    @Test
    public void cleanModeHidesHintsPreviewAndDebugOverlay() {
        InputAssistanceMode.Profile profile = InputAssistanceMode.CLEAN.profile;

        assertFalse(profile.showHangulConsonantHints);
        assertFalse(profile.showHangulVowelHints);
        assertFalse(profile.showEnglishHints);
        assertFalse(profile.showSpacebarHints);
        assertFalse(profile.showPreview);
        assertFalse(profile.showDebugOverlay);
        assertFalse(profile.showAnyHangulHints());
        assertEquals(KeyboardErgonomicsPreset.LEGACY, profile.recommendedErgonomicsPreset);
    }

    @Test
    public void learningModeMatchesCurrentBeginnerVisibleDefaults() {
        assertEquals(
                KeyboardErgonomicsPreset.STABLE,
                InputAssistanceMode.LEARNING.profile.recommendedErgonomicsPreset);
        assertEquals(
                InputAssistanceMode.LEARNING,
                InputAssistanceMode.match(true, true, true, true, true, false));
    }

    @Test
    public void debugModeIncludesOverlay() {
        InputAssistanceMode.Profile profile = InputAssistanceMode.DEBUG.profile;

        assertTrue(profile.showPreview);
        assertTrue(profile.showDebugOverlay);
        assertEquals(KeyboardErgonomicsPreset.AGGRESSIVE, profile.recommendedErgonomicsPreset);
        assertEquals(
                InputAssistanceMode.DEBUG,
                InputAssistanceMode.match(true, true, true, true, true, true));
    }

    @Test
    public void partialManualChangesBecomeCustom() {
        assertEquals(
                InputAssistanceMode.CUSTOM,
                InputAssistanceMode.match(true, false, true, true, true, false));
        assertEquals(
                InputAssistanceMode.CUSTOM,
                InputAssistanceMode.match(false, false, false, true, false, false));
    }

    @Test
    public void displayNamesComeFromAndroidStringResources() {
        assertEquals(R.string.input_assistance_custom_mode, InputAssistanceMode.CUSTOM.labelResId);
        assertEquals(R.string.input_assistance_clean_mode, InputAssistanceMode.CLEAN.labelResId);
        assertEquals(R.string.input_assistance_learning_mode, InputAssistanceMode.LEARNING.labelResId);
        assertEquals(R.string.input_assistance_debug_mode, InputAssistanceMode.DEBUG.labelResId);
        assertEquals("custom", InputAssistanceMode.CUSTOM.toString());
    }
}
