package com.superl3.s3keyboard;

import android.view.inputmethod.EditorInfo;

final class InputSessionSettingsResolver {
    private InputSessionSettingsResolver() {
    }

    static InputSessionSettings resolve(
            EditorInfo info,
            KeyboardSettings storedSettings,
            boolean remotePackageMatched,
            AppInputProfileOverrides appProfileOverrides,
            String enterActionLabel) {
        ResolvedImeAction enterAction = ImeActionLabelResolver.resolve(info);
        EditorInputPolicy basePolicy = EditorInputPolicy.from(info);
        String packageName = info == null || info.packageName == null ? "" : info.packageName;
        KeyboardSettings safeSettings = storedSettings == null
                ? KeyboardSettings.defaults()
                : storedSettings;
        boolean remoteProfileRequested = safeSettings.remoteModeEnabled || remotePackageMatched;
        AppInputProfile profile = AppInputProfileResolver.resolve(
                packageName,
                basePolicy,
                remoteProfileRequested);
        AppInputProfileOverrides overrides = appProfileOverrides == null
                ? AppInputProfileOverrides.EMPTY
                : appProfileOverrides;
        profile = overrides.apply(packageName, profile);
        EditorInputPolicy effectivePolicy = profile.apply(basePolicy);
        KeyboardMode runtimeMode = effectivePolicy.initialKeyboardMode(safeSettings.keyboardMode);
        boolean autoRemoteMode = !safeSettings.remoteModeEnabled && profile.remoteMode;
        KeyboardSettings runtimeSettings = safeSettings
                .withKeyboardMode(runtimeMode)
                .withRemoteOptions(
                        safeSettings.remoteModeEnabled || autoRemoteMode,
                        safeSettings.remoteKeyPreset,
                        safeSettings.remoteImeShortcut)
                .withEnterKeyLabel(safeLabel(enterActionLabel))
                .withRuntimeNumberRowForced(effectivePolicy.forceNumberRow);
        return new InputSessionSettings(
                enterAction,
                effectivePolicy,
                profile,
                packageName,
                runtimeSettings,
                autoRemoteMode);
    }

    private static String safeLabel(String label) {
        return label == null ? "" : label;
    }
}
