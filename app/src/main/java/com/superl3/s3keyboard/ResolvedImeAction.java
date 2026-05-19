package com.superl3.s3keyboard;

final class ResolvedImeAction {
    final String label;
    final int editorActionId;
    final boolean performEditorAction;

    ResolvedImeAction(String label, int editorActionId, boolean performEditorAction) {
        this.label = label;
        this.editorActionId = editorActionId;
        this.performEditorAction = performEditorAction;
    }
}
