package com.superl3.s3keyboard;

enum InputAssistanceMode implements SettingsLabelOption {
    CUSTOM(R.string.input_assistance_custom_mode, null),
    CLEAN(R.string.input_assistance_clean_mode, new Profile(
            false,
            false,
            false,
            false,
            false,
            false,
            KeyboardErgonomicsPreset.LEGACY)),
    LEARNING(R.string.input_assistance_learning_mode, new Profile(
            true,
            true,
            true,
            true,
            true,
            false,
            KeyboardErgonomicsPreset.STABLE)),
    DEBUG(R.string.input_assistance_debug_mode, new Profile(
            true,
            true,
            true,
            true,
            true,
            true,
            KeyboardErgonomicsPreset.AGGRESSIVE));

    private static final InputAssistanceMode[] DEBUGGABLE_DISPLAY_ORDER = values();
    private static final InputAssistanceMode[] RELEASE_DISPLAY_ORDER = {
            CUSTOM,
            CLEAN,
            LEARNING
    };

    final int labelResId;
    final Profile profile;

    InputAssistanceMode(int labelResId, Profile profile) {
        this.labelResId = labelResId;
        this.profile = profile;
    }

    @Override
    public int labelResId() {
        return labelResId;
    }

    boolean isPreset() {
        return profile != null;
    }

    static InputAssistanceMode[] displayOrder(boolean debuggableBuild) {
        return debuggableBuild ? DEBUGGABLE_DISPLAY_ORDER.clone() : RELEASE_DISPLAY_ORDER.clone();
    }

    static int indexOf(InputAssistanceMode[] modes, InputAssistanceMode mode) {
        if (modes == null || modes.length == 0) {
            return 0;
        }
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == mode) {
                return i;
            }
        }
        return 0;
    }

    static InputAssistanceMode match(
            boolean showHangulConsonantHints,
            boolean showHangulVowelHints,
            boolean showEnglishHints,
            boolean showSpacebarHints,
            boolean showPreview,
            boolean showDebugOverlay) {
        for (InputAssistanceMode mode : values()) {
            if (mode.profile != null
                    && mode.profile.matches(
                            showHangulConsonantHints,
                            showHangulVowelHints,
                            showEnglishHints,
                            showSpacebarHints,
                            showPreview,
                            showDebugOverlay)) {
                return mode;
            }
        }
        return CUSTOM;
    }

    @Override
    public String toString() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    static InputAssistanceMode valueOfPreference(String value) {
        if (value != null) {
            for (InputAssistanceMode mode : values()) {
                if (mode.toString().equals(value)) {
                    return mode;
                }
            }
        }
        return CUSTOM;
    }

    static final class Profile {
        final boolean showHangulConsonantHints;
        final boolean showHangulVowelHints;
        final boolean showEnglishHints;
        final boolean showSpacebarHints;
        final boolean showPreview;
        final boolean showDebugOverlay;
        final KeyboardErgonomicsPreset recommendedErgonomicsPreset;

        Profile(
                boolean showHangulConsonantHints,
                boolean showHangulVowelHints,
                boolean showEnglishHints,
                boolean showSpacebarHints,
                boolean showPreview,
                boolean showDebugOverlay,
                KeyboardErgonomicsPreset recommendedErgonomicsPreset) {
            this.showHangulConsonantHints = showHangulConsonantHints;
            this.showHangulVowelHints = showHangulVowelHints;
            this.showEnglishHints = showEnglishHints;
            this.showSpacebarHints = showSpacebarHints;
            this.showPreview = showPreview;
            this.showDebugOverlay = showDebugOverlay;
            this.recommendedErgonomicsPreset = recommendedErgonomicsPreset == null
                    ? KeyboardErgonomicsPreset.LEGACY
                    : recommendedErgonomicsPreset;
        }

        boolean showAnyHangulHints() {
            return showHangulConsonantHints || showHangulVowelHints;
        }

        private boolean matches(
                boolean showHangulConsonantHints,
                boolean showHangulVowelHints,
                boolean showEnglishHints,
                boolean showSpacebarHints,
                boolean showPreview,
                boolean showDebugOverlay) {
            return this.showHangulConsonantHints == showHangulConsonantHints
                    && this.showHangulVowelHints == showHangulVowelHints
                    && this.showEnglishHints == showEnglishHints
                    && this.showSpacebarHints == showSpacebarHints
                    && this.showPreview == showPreview
                    && this.showDebugOverlay == showDebugOverlay;
        }
    }
}
