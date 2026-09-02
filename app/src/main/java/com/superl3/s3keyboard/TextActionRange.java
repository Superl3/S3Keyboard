package com.superl3.s3keyboard;

final class TextActionRange {
    final int start;
    final int end;
    final int selectionStart;
    final int selectionEnd;
    final String text;

    private TextActionRange(int start, int end, int selectionStart, int selectionEnd, String text) {
        this.start = start;
        this.end = end;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.text = text;
    }

    static TextActionRange resolve(String surrounding, int absoluteOffset, int selectionStart, int selectionEnd) {
        if (surrounding == null) {
            return null;
        }
        int localStart = Math.max(0, Math.min(surrounding.length(), selectionStart - absoluteOffset));
        int localEnd = Math.max(0, Math.min(surrounding.length(), selectionEnd - absoluteOffset));
        if (localStart > localEnd) {
            int swap = localStart;
            localStart = localEnd;
            localEnd = swap;
        }
        if (localStart != localEnd) {
            return new TextActionRange(selectionStart, selectionEnd, selectionStart, selectionEnd,
                    surrounding.substring(localStart, localEnd));
        }
        int sentenceStart = localStart;
        while (sentenceStart > 0 && !isBoundary(surrounding.charAt(sentenceStart - 1))) {
            sentenceStart--;
        }
        int sentenceEnd = localStart;
        while (sentenceEnd < surrounding.length() && !isBoundary(surrounding.charAt(sentenceEnd))) {
            sentenceEnd++;
        }
        while (sentenceStart < sentenceEnd && Character.isWhitespace(surrounding.charAt(sentenceStart))) {
            sentenceStart++;
        }
        while (sentenceEnd > sentenceStart && Character.isWhitespace(surrounding.charAt(sentenceEnd - 1))) {
            sentenceEnd--;
        }
        if (sentenceStart == sentenceEnd) {
            return null;
        }
        int absoluteStart = absoluteOffset + sentenceStart;
        int absoluteEnd = absoluteOffset + sentenceEnd;
        int cursor = absoluteOffset + localStart;
        return new TextActionRange(absoluteStart, absoluteEnd, cursor, cursor,
                surrounding.substring(sentenceStart, sentenceEnd));
    }

    private static boolean isBoundary(char ch) {
        return ch == '\n' || ch == '\r' || ch == '.' || ch == '!' || ch == '?' || ch == '。' || ch == '！' || ch == '？';
    }
}
