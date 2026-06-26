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

        assertEquals(R.string.ime_action_search, action.labelResId);
        assertTrue(action.performEditorAction);
        assertEquals(EditorInfo.IME_ACTION_SEARCH, action.editorActionId);
    }

    @Test
    public void doneActionUsesDoneLabel() {
        ResolvedImeAction action = ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_DONE,
                0);

        assertEquals(R.string.ime_action_done, action.labelResId);
        assertTrue(action.performEditorAction);
    }

    @Test
    public void nextGoAndSendActionsUseKoreanLabels() {
        assertEquals(R.string.ime_action_next, ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_NEXT,
                0).labelResId);
        assertEquals(R.string.ime_action_go, ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_GO,
                0).labelResId);
        assertEquals(R.string.ime_action_send, ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_SEND,
                0).labelResId);
    }

    @Test
    public void multilineWithoutEnterActionUsesNewline() {
        ResolvedImeAction action = ImeActionLabelResolver.resolve(
                EditorInfo.IME_ACTION_NONE,
                InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        assertEquals(R.string.ime_action_newline, action.labelResId);
        assertFalse(action.performEditorAction);
    }
}
