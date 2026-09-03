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
        AppInputProfileOverrides overrides = RuntimeDefaults.appInputProfileOverrides(appProfileOverrides);
        AppInputProfileOverride appOverride = overrides.forPackage(packageName);
        boolean hardRestricted = basePolicy.password || basePolicy.numberLike || basePolicy.rawKeyInput;
        boolean remoteProfileRequested = !hardRestricted && (appOverride.remoteMode != null
                ? appOverride.remoteMode
                : safeSettings.remoteModeEnabled || remotePackageMatched);
        AppInputProfile profile = AppInputProfileResolver.resolve(
                packageName, basePolicy, remoteProfileRequested);
        profile = overrides.apply(packageName, profile);
        EditorInputPolicy effectivePolicy = profile.apply(basePolicy);
        if (hardRestricted) {
            effectivePolicy = basePolicy;
        }

        KeyboardMode runtimeMode = appOverride.keyboardMode != null && !hardRestricted
                ? appOverride.keyboardMode
                : effectivePolicy.initialKeyboardMode(safeSettings.keyboardMode);
        boolean runtimeRemoteMode = !hardRestricted && profile.remoteMode;
        boolean autoRemoteMode = !safeSettings.remoteModeEnabled && runtimeRemoteMode;
        KeyboardSettings runtimeSettings = safeSettings
                .withKeyboardMode(runtimeMode)
                .withRemoteOptions(
                        runtimeRemoteMode,
                        safeSettings.remoteKeyPreset,
                        safeSettings.remoteImeShortcut)
                .withEnterKeyLabel(RuntimeDefaults.stringOrDefault(enterActionLabel, ""))
                .withRuntimeNumberRowForced(effectivePolicy.forceNumberRow);
        if (appOverride.numberRowVisible != null && !hardRestricted && !runtimeRemoteMode) {
            runtimeSettings = runtimeSettings
                    .withHangulNumberRow(appOverride.numberRowVisible)
                    .withEnglishNumberRow(appOverride.numberRowVisible)
                    .withRuntimeNumberRowForced(false);
        }
        return new InputSessionSettings(
                enterAction,
                effectivePolicy,
                profile,
                packageName,
                runtimeSettings,
                autoRemoteMode);
    }
}
