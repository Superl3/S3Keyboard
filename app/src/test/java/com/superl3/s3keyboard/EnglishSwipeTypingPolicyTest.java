package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class EnglishSwipeTypingPolicyTest {
    @Test
    public void activeOnlyForEnglishQwertyTextSurfaces() {
        KeyboardSettings english = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        KeyboardLayoutProfiles qwerty = KeyboardLayoutProfiles.defaults();

        assertTrue(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.NORMAL, qwerty));
        assertTrue(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.SEARCH_EXTENDED, qwerty));
        assertTrue(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.MULTILINE_EXTENDED, qwerty));
        assertFalse(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.PASSWORD_SAFE, qwerty));
        assertFalse(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.URL_EXTENDED, qwerty));
        assertFalse(EnglishSwipeTypingPolicy.active(english, KeyboardSurface.EMAIL_EXTENDED, qwerty));
    }

    @Test
    public void remoteModeAndDingulLayoutDisableEnglishSwipe() {
        KeyboardSettings english = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);

        assertFalse(EnglishSwipeTypingPolicy.active(
                english.withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT),
                KeyboardSurface.NORMAL,
                KeyboardLayoutProfiles.defaults()));
        assertFalse(EnglishSwipeTypingPolicy.active(
                english,
                KeyboardSurface.NORMAL,
                KeyboardLayoutProfiles.defaults().withEnglishLayout(KeyboardLayoutProfile.DINGUL)));
        assertFalse(EnglishSwipeTypingPolicy.active(
                KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL),
                KeyboardSurface.NORMAL,
                KeyboardLayoutProfiles.defaults()));
    }
}
