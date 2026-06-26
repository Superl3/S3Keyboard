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
        this.editorPolicy = editorPolicy == null ? EditorInputPolicy.DEFAULT : editorPolicy;
        this.appInputProfile = appInputProfile == null ? AppInputProfile.STANDARD : appInputProfile;
        this.packageName = packageName == null ? "" : packageName;
        this.runtimeSettings = runtimeSettings == null ? KeyboardSettings.defaults() : runtimeSettings;
        this.remoteModeAutoActivated = remoteModeAutoActivated;
    }
}
