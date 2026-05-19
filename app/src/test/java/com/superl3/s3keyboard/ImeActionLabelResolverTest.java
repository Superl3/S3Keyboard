package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public final class ImeActionLabelResolverTest {
    @Test
    public void searchActionUsesSearchLabelAndEditorAction() {
        ResolvedImeAction action = ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_SEARCH,
                0);

        assertEquals("검색", action.label);
        assertTrue(action.performEditorAction);
        assertEquals(EditorInfo.IME_ACTION_SEARCH, action.editorActionId);
    }

    @Test
    public void doneActionUsesDoneLabel() {
        ResolvedImeAction action = ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_DONE,
                0);

        assertEquals("완료", action.label);
        assertTrue(action.performEditorAction);
    }

    @Test
    public void multilineWithoutEnterActionUsesNewline() {
        ResolvedImeAction action = ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_NONE,
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        assertEquals("줄바꿈", action.label);
        assertFalse(action.performEditorAction);
    }
}
