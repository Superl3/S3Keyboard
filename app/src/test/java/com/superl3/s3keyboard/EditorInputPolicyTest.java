package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public final class EditorInputPolicyTest {
    @Test
    public void passwordFieldsForceNumberRowAndDisableComposingConveniences() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);

        assertTrue(policy.password);
        assertTrue(policy.preferAsciiLayout);
        assertTrue(policy.forceNumberRow);
        assertFalse(policy.allowComposingText);
        assertFalse(policy.allowTextConveniences);
        assertEquals(KeyboardMode.ENGLISH, policy.initialKeyboardMode(KeyboardMode.HANGUL));
        assertFalse(policy.locksLanguageToggle());
    }

    @Test
    public void numberFieldsUseNumpadPolicy() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER);

        assertTrue(policy.numberLike);
        assertTrue(policy.forceNumberRow);
        assertFalse(policy.allowComposingText);
        assertFalse(policy.allowTextConveniences);
        assertEquals(KeyboardMode.ENGLISH, policy.initialKeyboardMode(KeyboardMode.HANGUL));
        assertTrue(policy.locksLanguageToggle());
    }

    @Test
    public void phoneAndDatetimeFieldsUseNumberLikePolicy() {
        EditorInputPolicy phone = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_PHONE);
        EditorInputPolicy datetime = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_DATETIME);

        assertTrue(phone.numberLike);
        assertTrue(phone.forceNumberRow);
        assertFalse(phone.allowComposingText);
        assertTrue(datetime.numberLike);
        assertTrue(datetime.forceNumberRow);
        assertFalse(datetime.allowComposingText);
    }

    @Test
    public void uriFieldsKeepComposingButDisableTextConvenienceRewrites() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        assertTrue(policy.uriLike);
        assertTrue(policy.preferAsciiLayout);
        assertFalse(policy.forceNumberRow);
        assertTrue(policy.allowComposingText);
        assertFalse(policy.allowTextConveniences);
        assertEquals(KeyboardMode.ENGLISH, policy.initialKeyboardMode(KeyboardMode.HANGUL));
        assertFalse(policy.locksLanguageToggle());
    }

    @Test
    public void emailFieldsKeepComposingButPreferAsciiAndDisableRewrites() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        assertTrue(policy.emailLike);
        assertTrue(policy.preferAsciiLayout);
        assertFalse(policy.forceNumberRow);
        assertTrue(policy.allowComposingText);
        assertFalse(policy.allowTextConveniences);
    }

    @Test
    public void typeNullUsesRawKeyFallbackAndDisablesComposing() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(InputType.TYPE_NULL);

        assertTrue(policy.rawKeyInput);
        assertTrue(policy.preferAsciiLayout);
        assertFalse(policy.allowComposingText);
        assertFalse(policy.allowTextConveniences);
    }

    @Test
    public void multilineAndSearchActionAreExplicitPolicyFlags() {
        EditorInputPolicy multiline = EditorInputPolicy.fromEditorInfo(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                0);
        EditorInputPolicy search = EditorInputPolicy.fromEditorInfo(
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_SEARCH);

        assertTrue(multiline.multiline);
        assertFalse(multiline.searchAction);
        assertTrue(search.searchAction);
        assertFalse(search.multiline);
    }
}
