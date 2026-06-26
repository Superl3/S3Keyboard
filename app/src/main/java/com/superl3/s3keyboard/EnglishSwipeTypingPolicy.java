package com.superl3.s3keyboard;

final class EnglishSwipeTypingPolicy {
    private EnglishSwipeTypingPolicy() {
    }

    static boolean active(
            KeyboardSettings settings,
            KeyboardSurface surface,
            KeyboardLayoutProfiles layoutProfiles) {
        if (settings == null
                || settings.keyboardMode != KeyboardMode.ENGLISH
                || settings.remoteModeEnabled
                || layoutProfiles == null
                || !layoutProfiles.activeIsQwerty(KeyboardMode.ENGLISH)) {
            return false;
        }
        return surface == KeyboardSurface.NORMAL
                || surface == KeyboardSurface.SEARCH_EXTENDED
                || surface == KeyboardSurface.MULTILINE_EXTENDED;
    }
}
