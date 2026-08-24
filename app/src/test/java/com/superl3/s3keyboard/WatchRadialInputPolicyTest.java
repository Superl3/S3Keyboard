package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WatchRadialInputPolicyTest {
    @Test
    public void activeOnlyForHangulDingulNormalSurface() {
        KeyboardSettings defaults = KeyboardSettings.defaults();
        KeyboardLayoutProfiles profiles = KeyboardLayoutProfiles.defaults();

        assertTrue(WatchRadialInputPolicy.isActive(
                true,
                defaults.withKeyboardMode(KeyboardMode.HANGUL),
                profiles,
                KeyboardSurface.NORMAL));
        assertFalse(WatchRadialInputPolicy.isActive(
                false,
                defaults,
                profiles,
                KeyboardSurface.NORMAL));
        assertFalse(WatchRadialInputPolicy.isActive(
                true,
                defaults.withKeyboardMode(KeyboardMode.ENGLISH),
                profiles,
                KeyboardSurface.NORMAL));
        assertFalse(WatchRadialInputPolicy.isActive(
                true,
                defaults,
                profiles.withHangulLayout(KeyboardLayoutProfile.QWERTY),
                KeyboardSurface.NORMAL));
    }

    @Test
    public void remoteAndReplacementPadsAlwaysUseExistingKeyboardSurface() {
        KeyboardSettings remote = KeyboardSettings.defaults().withRemoteOptions(
                true,
                RemoteKeyPreset.PC_KEYBOARD,
                RemoteImeShortcut.ALT_SHIFT);
        KeyboardLayoutProfiles profiles = KeyboardLayoutProfiles.defaults();

        assertFalse(WatchRadialInputPolicy.isActive(
                true, remote, profiles, KeyboardSurface.NORMAL));
        assertFalse(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.NUMPAD));
        assertFalse(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.PINPAD));
        assertFalse(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.PASSWORD_SAFE));
        assertFalse(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.RAW));
        assertTrue(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.MULTILINE_EXTENDED));
        assertTrue(WatchRadialInputPolicy.isActive(
                true, KeyboardSettings.defaults(), profiles, KeyboardSurface.SEARCH_EXTENDED));
    }
}
