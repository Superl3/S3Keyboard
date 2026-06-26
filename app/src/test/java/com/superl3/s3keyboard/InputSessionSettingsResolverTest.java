package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public final class InputSessionSettingsResolverTest {
    @Test
    public void remoteAutoPackageBuildsRuntimeRemoteProfileWithoutMutatingStoredToggle() {
        KeyboardSettings stored = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withRemoteOptions(false, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT)
                .withHangulNumberRow(false);
        EditorInfo info = textInfo("com.limelight");

        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                stored,
                true,
                AppInputProfileOverrides.EMPTY,
                "Send");

        assertEquals("com.limelight", session.packageName);
        assertEquals("remote_moonlight", session.appInputProfile.id);
        assertTrue(session.remoteModeAutoActivated);
        assertTrue(session.runtimeSettings.remoteModeEnabled);
        assertTrue(session.runtimeSettings.showNumberRow);
        assertFalse(stored.remoteModeEnabled);
        assertFalse(stored.showHangulNumberRow);
    }

    @Test
    public void storedRemoteModeUsesRemoteProfileButIsNotAutoActivated() {
        KeyboardSettings stored = KeyboardSettings.defaults()
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.CTRL_SPACE);
        EditorInfo info = textInfo("com.example.remoteclient");

        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                stored,
                false,
                AppInputProfileOverrides.EMPTY,
                "Send");

        assertEquals("remote_desktop", session.appInputProfile.id);
        assertFalse(session.remoteModeAutoActivated);
        assertTrue(session.runtimeSettings.remoteModeEnabled);
        assertEquals(RemoteImeShortcut.CTRL_SPACE, session.runtimeSettings.remoteImeShortcut);
    }

    @Test
    public void passwordFieldForcesAsciiNumberRowAndNoComposing() {
        KeyboardSettings stored = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL);
        EditorInfo info = textInfo("com.example.login");
        info.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                stored,
                false,
                AppInputProfileOverrides.EMPTY,
                "Done");

        assertEquals("password", session.appInputProfile.id);
        assertTrue(session.editorPolicy.preferAsciiLayout);
        assertTrue(session.editorPolicy.forceNumberRow);
        assertFalse(session.editorPolicy.allowComposingText);
        assertFalse(session.editorPolicy.allowTextConveniences);
        assertEquals(KeyboardMode.ENGLISH, session.runtimeSettings.keyboardMode);
        assertTrue(session.runtimeSettings.showNumberRow);
    }

    @Test
    public void userPackageOverridesAreAppliedAfterBuiltInProfile() {
        KeyboardSettings stored = KeyboardSettings.defaults();
        EditorInfo info = textInfo("com.example.editor");

        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                stored,
                false,
                new AppInputProfileOverrides(
                        "com.example.editor",
                        "com.example.editor",
                        "com.example.editor",
                        "com.example.editor"),
                "Send");

        assertEquals("default+user_app_profile_override", session.appInputProfile.source);
        assertTrue(session.editorPolicy.preferAsciiLayout);
        assertTrue(session.editorPolicy.forceNumberRow);
        assertFalse(session.editorPolicy.allowComposingText);
        assertFalse(session.editorPolicy.allowTextConveniences);
        assertTrue(session.runtimeSettings.showNumberRow);
    }

    @Test
    public void enterActionAndLabelAreCarriedIntoRuntimeSettings() {
        KeyboardSettings stored = KeyboardSettings.defaults();
        EditorInfo info = textInfo("com.example.search");
        info.imeOptions = EditorInfo.IME_ACTION_SEARCH;

        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                stored,
                false,
                AppInputProfileOverrides.EMPTY,
                "Search");

        assertEquals(EditorInfo.IME_ACTION_SEARCH, session.enterAction.editorActionId);
        assertTrue(session.enterAction.performEditorAction);
        assertEquals("Search", session.runtimeSettings.enterKeyLabel);
    }

    private static EditorInfo textInfo(String packageName) {
        EditorInfo info = new EditorInfo();
        info.packageName = packageName;
        info.inputType = InputType.TYPE_CLASS_TEXT;
        info.imeOptions = EditorInfo.IME_ACTION_SEND;
        return info;
    }
}
