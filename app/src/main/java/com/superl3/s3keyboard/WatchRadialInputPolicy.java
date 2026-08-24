package com.superl3.s3keyboard;

final class WatchRadialInputPolicy {
    private WatchRadialInputPolicy() {
    }

    static boolean isActive(
            boolean enabled,
            KeyboardSettings settings,
            KeyboardLayoutProfiles layoutProfiles,
            KeyboardSurface surface) {
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        KeyboardLayoutProfiles safeProfiles = RuntimeDefaults.keyboardLayoutProfiles(layoutProfiles);
        return enabled
                && safeSettings.keyboardMode == KeyboardMode.HANGUL
                && !safeSettings.remoteModeEnabled
                && safeProfiles.activeIsDingul(KeyboardMode.HANGUL)
                && supportsSurface(RuntimeDefaults.keyboardSurface(surface));
    }

    private static boolean supportsSurface(KeyboardSurface surface) {
        switch (surface) {
            case NUMPAD:
            case PHONEPAD:
            case DATEPAD:
            case PINPAD:
            case PASSWORD_SAFE:
            case RAW:
                return false;
            case NORMAL:
            case URL_EXTENDED:
            case EMAIL_EXTENDED:
            case WEB_EXTENDED:
            case SEARCH_EXTENDED:
            case MULTILINE_EXTENDED:
            default:
                return true;
        }
    }
}
