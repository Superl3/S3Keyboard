package com.superl3.s3keyboard;

final class KeyboardLayoutProfiles {
    static final KeyboardLayoutProfile DEFAULT_HANGUL = KeyboardLayoutProfile.DINGUL;
    static final KeyboardLayoutProfile DEFAULT_ENGLISH = KeyboardLayoutProfile.QWERTY;

    final KeyboardLayoutProfile hangulLayout;
    final KeyboardLayoutProfile englishLayout;

    KeyboardLayoutProfiles(KeyboardLayoutProfile hangulLayout, KeyboardLayoutProfile englishLayout) {
        this.hangulLayout = hangulLayout == null ? DEFAULT_HANGUL : hangulLayout;
        this.englishLayout = englishLayout == null ? DEFAULT_ENGLISH : englishLayout;
    }

    static KeyboardLayoutProfiles defaults() {
        return new KeyboardLayoutProfiles(DEFAULT_HANGUL, DEFAULT_ENGLISH);
    }

    KeyboardLayoutProfiles withHangulLayout(KeyboardLayoutProfile profile) {
        return new KeyboardLayoutProfiles(profile, englishLayout);
    }

    KeyboardLayoutProfiles withEnglishLayout(KeyboardLayoutProfile profile) {
        return new KeyboardLayoutProfiles(hangulLayout, profile);
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
