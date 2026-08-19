package com.superl3.s3keyboard;

final class KeyboardLayoutProfiles {
    static final KeyboardLayoutProfile DEFAULT_HANGUL = KeyboardLayoutProfile.DINGUL;
    static final KeyboardLayoutProfile DEFAULT_ENGLISH = KeyboardLayoutProfile.QWERTY;
    static final boolean DEFAULT_DINGUL_DOT_ENTER_KEY_ENABLED = true;

    final KeyboardLayoutProfile hangulLayout;
    final KeyboardLayoutProfile englishLayout;
    final boolean dingulDotEnterKeyEnabled;

    KeyboardLayoutProfiles(KeyboardLayoutProfile hangulLayout, KeyboardLayoutProfile englishLayout) {
        this(hangulLayout, englishLayout, DEFAULT_DINGUL_DOT_ENTER_KEY_ENABLED);
    }

    KeyboardLayoutProfiles(
            KeyboardLayoutProfile hangulLayout,
            KeyboardLayoutProfile englishLayout,
            boolean dingulDotEnterKeyEnabled) {
        this.hangulLayout = hangulLayout == null ? DEFAULT_HANGUL : hangulLayout;
        this.englishLayout = englishLayout == null ? DEFAULT_ENGLISH : englishLayout;
        this.dingulDotEnterKeyEnabled = dingulDotEnterKeyEnabled;
    }

    static KeyboardLayoutProfiles defaults() {
        return new KeyboardLayoutProfiles(DEFAULT_HANGUL, DEFAULT_ENGLISH, DEFAULT_DINGUL_DOT_ENTER_KEY_ENABLED);
    }

    KeyboardLayoutProfiles withHangulLayout(KeyboardLayoutProfile profile) {
        return new KeyboardLayoutProfiles(profile, englishLayout, dingulDotEnterKeyEnabled);
    }

    KeyboardLayoutProfiles withEnglishLayout(KeyboardLayoutProfile profile) {
        return new KeyboardLayoutProfiles(hangulLayout, profile, dingulDotEnterKeyEnabled);
    }

    KeyboardLayoutProfiles withDingulDotEnterKeyEnabled(boolean enabled) {
        return new KeyboardLayoutProfiles(hangulLayout, englishLayout, enabled);
    }

    KeyboardLayoutProfiles effectiveForOneFingerInput(boolean enabled) {
        return enabled && dingulDotEnterKeyEnabled
                ? withDingulDotEnterKeyEnabled(false)
                : this;
    }

    KeyboardLayoutProfile activeFor(KeyboardMode mode) {
        return mode == KeyboardMode.ENGLISH ? englishLayout : hangulLayout;
    }

    boolean activeIsDingul(KeyboardMode mode) {
        return activeFor(mode) == KeyboardLayoutProfile.DINGUL;
    }

    boolean activeIsQwerty(KeyboardMode mode) {
        return activeFor(mode) == KeyboardLayoutProfile.QWERTY;
    }
}
