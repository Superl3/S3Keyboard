package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

public final class TextToolsPolicyTest {
    @Test
    public void normalTextAllowsPanelAndInsertion() {
        EditorInputPolicy policy = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);
        assertTrue(TextToolsPolicy.allows(policy, false));
        assertTrue(TextToolsPolicy.allowsInsertion(policy, false, "hello"));
    }

    @Test
    public void sensitiveAndStructuredFieldsSuppressTextTools() {
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD), false));
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_NUMBER), false));
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI), false));
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS), false));
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT), false));
    }

    @Test
    public void remoteRawAndEmptyInsertionAreSuppressed() {
        EditorInputPolicy normal = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);
        assertFalse(TextToolsPolicy.allows(normal, true));
        assertFalse(TextToolsPolicy.allows(EditorInputPolicy.fromInputType(InputType.TYPE_NULL), false));
        assertFalse(TextToolsPolicy.allowsInsertion(normal, false, ""));
        assertFalse(TextToolsPolicy.allowsInsertion(normal, false, null));
    }
}
