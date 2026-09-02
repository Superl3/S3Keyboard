package com.superl3.s3keyboard;

final class TextActionTransaction {
    interface Editor {
        boolean setSelection(int start, int end);
        boolean commitText(String text);
    }

    final String editorId;
    final int start;
    final String originalText;
    final String replacementText;
    final int originalSelectionStart;
    final int originalSelectionEnd;

    private TextActionTransaction(
            String editorId,
            int start,
            String originalText,
            String replacementText,
            int originalSelectionStart,
            int originalSelectionEnd) {
        this.editorId = editorId == null ? "" : editorId;
        this.start = start;
        this.originalText = originalText;
        this.replacementText = replacementText;
        this.originalSelectionStart = originalSelectionStart;
        this.originalSelectionEnd = originalSelectionEnd;
    }

    static TextActionTransaction apply(
            Editor editor,
            String editorId,
            TextActionRange range,
            String replacementText) {
        if (editor == null || range == null || replacementText == null || replacementText.equals(range.text)) {
            return null;
        }
        if (!editor.setSelection(range.start, range.end) || !editor.commitText(replacementText)) {
            return null;
        }
        return new TextActionTransaction(
                editorId,
                range.start,
                range.text,
                replacementText,
                range.selectionStart,
                range.selectionEnd);
    }

    boolean restore(Editor editor, String activeEditorId) {
        if (editor == null || !editorId.equals(activeEditorId == null ? "" : activeEditorId)) {
            return false;
        }
        int replacementEnd = start + replacementText.length();
        if (!editor.setSelection(start, replacementEnd) || !editor.commitText(originalText)) {
            return false;
        }
        return editor.setSelection(originalSelectionStart, originalSelectionEnd);
    }
}
