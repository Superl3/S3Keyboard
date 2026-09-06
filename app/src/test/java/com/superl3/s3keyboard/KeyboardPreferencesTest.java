package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardPreferencesTest {
    @Test
    public void transparentOverlayInputDefaultsOffForOrdinaryImeUse() {
        assertFalse(KeyboardPreferences.DEFAULT_TRANSPARENT_OVERLAY_INPUT_ENABLED);
        assertTrue(TransparentOverlayStyle.fromPreference(null)
                == TransparentOverlayStyle.TRANSLUCENT_KEYS);
        assertTrue(TransparentOverlayStyle.fromPreference("extreme_floating")
                == TransparentOverlayStyle.EXTREME_FLOATING);
        assertFalse(KeyboardPreferences.DEFAULT_WATCH_RADIAL_INPUT_ENABLED);
    }

    @Test
    public void englishAssistancePreferencesUseIndependentStableKeys() {
        assertEquals("english_suggestions_enabled", KeyboardPreferences.ENGLISH_SUGGESTIONS_ENABLED);
        assertEquals(
                "english_auto_correction_enabled",
                KeyboardPreferences.ENGLISH_AUTO_CORRECTION_ENABLED);
    }

    @Test
    public void defaultRemoteAutoModePackagesIncludeCommonRemoteDesktopApps() {
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "tv.parsec.client"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.limelight"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.microsoft.rdc.android"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.microsoft.rdc.androidx"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.google.chromeremotedesktop"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.valvesoftware.steamlink"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.anydesk.anydeskandroid"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.teamviewer.teamviewer.market.mobile"));
        assertTrue(KeyboardPreferences.packageListContains(
                KeyboardPreferences.DEFAULT_REMOTE_AUTO_MODE_PACKAGES,
                "com.teamviewer.quicksupport.market"));
    }

    @Test
    public void packageListUsesExactTokensAcrossCommonSeparators() {
        String packageList = "tv.parsec.client, com.limelight\ncom.example.remote";

        assertTrue(KeyboardPreferences.packageListContains(packageList, "com.limelight"));
        assertTrue(KeyboardPreferences.packageListContains(packageList, "com.example.remote"));
        assertFalse(KeyboardPreferences.packageListContains(packageList, "com.limelight.beta"));
        assertFalse(KeyboardPreferences.packageListContains(packageList, "limelight"));
    }
}
