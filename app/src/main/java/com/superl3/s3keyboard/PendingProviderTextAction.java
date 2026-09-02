package com.superl3.s3keyboard;

final class PendingProviderTextAction {
    final TextAction action;
    final TextActionRange range;
    final String editorId;
    final TextActionProviderRequest request;

    PendingProviderTextAction(
            TextAction action,
            TextActionRange range,
            String editorId,
            TextActionProviderRequest request) {
        this.action = action;
        this.range = range;
        this.editorId = editorId == null ? "" : editorId;
        this.request = request;
    }

    boolean targets(String activeEditorId, TextActionRange activeRange) {
        if (!editorId.equals(activeEditorId == null ? "" : activeEditorId) || activeRange == null) {
            return false;
        }
        return range.start == activeRange.start
                && range.end == activeRange.end
                && range.text.equals(activeRange.text);
    }
}
