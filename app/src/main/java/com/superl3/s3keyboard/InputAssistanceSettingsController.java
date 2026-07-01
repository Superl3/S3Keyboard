package com.superl3.s3keyboard;

import android.content.Context;

final class InputAssistanceSettingsController {
    private static final InputAssistanceMode[] DEBUGGABLE_MODES = InputAssistanceMode.values();
    private static final InputAssistanceMode[] RELEASE_MODES = {
            InputAssistanceMode.CUSTOM,
            InputAssistanceMode.CLEAN,
            InputAssistanceMode.LEARNING
    };

    private InputAssistanceSettingsController() {
    }

    static InputAssistanceMode[] availableModes(boolean debuggableBuild) {
        return debuggableBuild ? DEBUGGABLE_MODES.clone() : RELEASE_MODES.clone();
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

    static InputAssistanceMode currentMode(
            Context context,
            KeyboardSettings settings,
            boolean debuggableBuild) {
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
        return InputAssistanceMode.match(
                KeyboardPreferences.loadShowHangulConsonantSlideHints(context),
                KeyboardPreferences.loadShowHangulVowelSlideHints(context),
                safe.showEnglishSlideHints,
                KeyboardPreferences.loadShowSpacebarSlideHints(context),
                safe.showBeginnerTooltipPreview,
                debuggableBuild && KeyboardPreferences.loadDebugKeyBoundsOverlayEnabled(context));
    }

    static KeyboardSettings applyPreset(
            Context context,
            KeyboardSettings settings,
            InputAssistanceMode mode,
            boolean debuggableBuild) {
        if (mode == null || !mode.isPreset()) {
            return settings == null ? KeyboardSettings.defaults() : settings;
        }
        KeyboardPreferences.saveInputAssistanceMode(context, mode);
        InputAssistanceMode.Profile profile = mode.profile;
        saveHangulConsonantHints(context, profile.showHangulConsonantHints);
        saveHangulVowelHints(context, profile.showHangulVowelHints);
        saveSpacebarHints(context, profile.showSpacebarHints);
        if (debuggableBuild) {
            saveDebugOverlay(context, profile.showDebugOverlay);
        }
        return settingsForProfile(settings, profile);
    }

    static KeyboardErgonomicsOptions ergonomicsForMode(
            KeyboardErgonomicsOptions current,
            InputAssistanceMode mode) {
        KeyboardErgonomicsOptions safe = current == null
                ? KeyboardErgonomicsOptions.DEFAULT
                : current;
        if (mode == null || !mode.isPreset()) {
            return safe;
        }
        return mode.profile.recommendedErgonomicsPreset.options;
    }

    static void saveHangulConsonantHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowHangulConsonantSlideHints(context, enabled);
    }

    static void saveHangulVowelHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowHangulVowelSlideHints(context, enabled);
    }

    static void saveSpacebarHints(Context context, boolean enabled) {
        KeyboardPreferences.saveShowSpacebarSlideHints(context, enabled);
    }

    static void saveDebugOverlay(Context context, boolean enabled) {
        KeyboardPreferences.saveDebugKeyBoundsOverlayEnabled(context, enabled);
    }

    static KeyboardSettings settingsForProfile(
            KeyboardSettings settings,
            InputAssistanceMode.Profile profile) {
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
        if (profile == null) {
            return safe;
        }
        return safe.withHintVisibility(
                profile.showAnyHangulHints(),
                profile.showEnglishHints,
                profile.showPreview);
    }
}
