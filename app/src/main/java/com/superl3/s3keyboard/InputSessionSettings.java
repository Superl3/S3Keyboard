package com.superl3.s3keyboard;

final class InputSessionSettings {
    final ResolvedImeAction enterAction;
    final EditorInputPolicy editorPolicy;
    final AppInputProfile appInputProfile;
    final String packageName;
    final KeyboardSettings runtimeSettings;
    final boolean remoteModeAutoActivated;

    InputSessionSettings(
            ResolvedImeAction enterAction,
            EditorInputPolicy editorPolicy,
            AppInputProfile appInputProfile,
            String packageName,
            KeyboardSettings runtimeSettings,
            boolean remoteModeAutoActivated) {
        this.enterAction = enterAction == null ? ImeActionLabelResolver.defaultAction() : enterAction;
        this.editorPolicy = RuntimeDefaults.editorInputPolicy(editorPolicy);
        this.appInputProfile = RuntimeDefaults.appInputProfile(appInputProfile);
        this.packageName = RuntimeDefaults.stringOrDefault(packageName, "");
        this.runtimeSettings = RuntimeDefaults.keyboardSettings(runtimeSettings);
        this.remoteModeAutoActivated = remoteModeAutoActivated;
    }
}
