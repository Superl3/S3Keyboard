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
        String packageName = AppPackageCatalog.normalizePackageName(info == null ? null : info.packageName);
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(storedSettings);
        boolean remoteProfileRequested = safeSettings.remoteModeEnabled || remotePackageMatched;
        AppInputProfile profile = AppInputProfileResolver.resolve(
                packageName,
                basePolicy,
                remoteProfileRequested);
        AppInputProfileOverrides overrides =
                RuntimeDefaults.appInputProfileOverrides(appProfileOverrides);
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
                .withEnterKeyLabel(RuntimeDefaults.stringOrDefault(enterActionLabel, ""))
                .withRuntimeNumberRowForced(effectivePolicy.forceNumberRow);
        return new InputSessionSettings(
                enterAction,
                effectivePolicy,
                profile,
                packageName,
                runtimeSettings,
                autoRemoteMode);
    }
}
