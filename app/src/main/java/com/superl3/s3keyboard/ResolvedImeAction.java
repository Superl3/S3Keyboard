package com.superl3.s3keyboard;

import android.content.Context;

final class ResolvedImeAction {
    final int labelResId;
    final int editorActionId;
    final boolean performEditorAction;

    ResolvedImeAction(int labelResId, int editorActionId, boolean performEditorAction) {
        this.labelResId = labelResId;
        this.editorActionId = editorActionId;
        this.performEditorAction = performEditorAction;
    }

    String label(Context context) {
        return context == null ? "" : context.getString(labelResId);
    }
}
