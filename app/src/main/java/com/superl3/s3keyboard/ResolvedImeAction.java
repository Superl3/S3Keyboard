package com.superl3.s3keyboard;

import android.content.Context;

final class ResolvedImeAction {
    final int labelResId;
    final int editorActionId;
    final boolean performEditorAction;
    final boolean commitNewline;

    ResolvedImeAction(int labelResId, int editorActionId, boolean performEditorAction) {
        this(labelResId, editorActionId, performEditorAction, !performEditorAction);
    }

    ResolvedImeAction(
            int labelResId,
            int editorActionId,
            boolean performEditorAction,
            boolean commitNewline) {
        this.labelResId = labelResId;
        this.editorActionId = editorActionId;
        this.performEditorAction = performEditorAction;
        this.commitNewline = commitNewline;
    }

    String label(Context context) {
        return context == null ? "" : context.getString(labelResId);
    }
}
