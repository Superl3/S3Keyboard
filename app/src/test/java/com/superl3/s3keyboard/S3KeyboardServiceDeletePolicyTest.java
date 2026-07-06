package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

public final class S3KeyboardServiceDeletePolicyTest {
    @Test
    public void composingTextFieldsDoNotUseCommitOnlyBackspace() {
        KeyboardSettings hangul = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL);

        assertFalse(S3KeyboardService.usesCommitOnlyHangul(
                hangul,
                EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT)));
        assertFalse(S3KeyboardService.usesCommitOnlyHangul(
                hangul,
                EditorInputPolicy.fromInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)));
    }

    @Test
    public void nonComposingHangulFieldsUseCommitOnlyBackspace() {
        KeyboardSettings hangul = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.HANGUL);

        assertTrue(S3KeyboardService.usesCommitOnlyHangul(
                hangul,
                EditorInputPolicy.fromInputType(InputType.TYPE_NULL)));
        assertTrue(S3KeyboardService.usesCommitOnlyHangul(
                hangul,
                EditorInputPolicy.fromInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)));
    }

    @Test
    public void englishModeNeverUsesHangulCommitOnlyBackspace() {
        KeyboardSettings english = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);

        assertFalse(S3KeyboardService.usesCommitOnlyHangul(
                english,
                EditorInputPolicy.fromInputType(InputType.TYPE_NULL)));
    }
}
