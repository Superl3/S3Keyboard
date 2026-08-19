package com.superl3.s3keyboard;

final class EditorTextBoundaryPolicy {
    private static final int ZERO_WIDTH_JOINER = 0x200D;
    private static final int HANGUL_NONE = 0;
    private static final int HANGUL_LEADING = 1;
    private static final int HANGUL_VOWEL = 2;
    private static final int HANGUL_TRAILING = 3;
    private static final int HANGUL_SYLLABLE_WITHOUT_TRAILING = 4;
    private static final int HANGUL_SYLLABLE_WITH_TRAILING = 5;

    private EditorTextBoundaryPolicy() {
    }

    static int trailingWordCodePointCount(CharSequence beforeCursor) {
        if (beforeCursor == null || beforeCursor.length() == 0) {
            return 0;
        }
        int end = beforeCursor.length();
        int start = end;
        while (start > 0) {
            int codePoint = Character.codePointBefore(beforeCursor, start);
            if (!Character.isWhitespace(codePoint)) {
                break;
            }
            start -= Character.charCount(codePoint);
        }
        while (start > 0) {
            int codePoint = Character.codePointBefore(beforeCursor, start);
            if (!isWordCodePoint(codePoint)) {
                break;
            }
            start -= Character.charCount(codePoint);
        }
        return Character.codePointCount(beforeCursor, start, end);
    }

    static int trailingCodePointUtf16UnitCount(CharSequence text, int codePointCount) {
        if (codePointCount <= 0) {
            return 0;
        }
        if (text == null || text.length() == 0) {
            return codePointCount;
        }
        int end = text.length();
        int start = end;
        int remaining = codePointCount;
        while (start > 0 && remaining > 0) {
            int codePoint = Character.codePointBefore(text, start);
            start -= Character.charCount(codePoint);
            remaining--;
        }
        return end - start;
    }

    static int trailingGraphemeUtf16UnitCount(CharSequence text) {
        if (text == null || text.length() == 0) {
            return 0;
        }
        int end = text.length();
        int finalCodePoint = Character.codePointBefore(text, end);
        if (finalCodePoint == '\n' && end > 1 && text.charAt(end - 2) == '\r') {
            return 2;
        }

        int start = trailingComponentStart(text, end);
        if (isRegionalIndicator(finalCodePoint)) {
            int runLength = 1;
            int scan = start;
            while (scan > 0) {
                int previous = Character.codePointBefore(text, scan);
                if (!isRegionalIndicator(previous)) {
                    break;
                }
                runLength++;
                scan -= Character.charCount(previous);
            }
            if ((runLength & 1) == 0 && start > 0) {
                int previous = Character.codePointBefore(text, start);
                start -= Character.charCount(previous);
            }
            return end - start;
        }

        while (start > 0 && Character.codePointBefore(text, start) == ZERO_WIDTH_JOINER) {
            start -= 1;
            start = trailingComponentStart(text, start);
        }
        return end - start;
    }

    private static boolean isWordCodePoint(int codePoint) {
        return Character.isLetterOrDigit(codePoint)
                || codePoint == '\''
                || codePoint == '_'
                || codePoint == '-';
    }

    private static int trailingComponentStart(CharSequence text, int endExclusive) {
        if (text == null || endExclusive <= 0) {
            return 0;
        }
        int start = Math.min(endExclusive, text.length());
        int current = Character.codePointBefore(text, start);
        start -= Character.charCount(current);
        while (start > 0 && isGraphemeExtension(current)) {
            current = Character.codePointBefore(text, start);
            start -= Character.charCount(current);
        }
        while (start > 0) {
            int previous = Character.codePointBefore(text, start);
            if (!hangulGraphemeContinues(previous, current)) {
                break;
            }
            start -= Character.charCount(previous);
            current = previous;
        }
        return start;
    }

    private static boolean isGraphemeExtension(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK
                || type == Character.ENCLOSING_MARK
                || (codePoint >= 0xFE00 && codePoint <= 0xFE0F)
                || (codePoint >= 0xE0100 && codePoint <= 0xE01EF)
                || (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF)
                || (codePoint >= 0xE0020 && codePoint <= 0xE007F);
    }

    private static boolean isRegionalIndicator(int codePoint) {
        return codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF;
    }

    private static boolean hangulGraphemeContinues(int previous, int current) {
        int previousType = hangulSyllableType(previous);
        int currentType = hangulSyllableType(current);
        switch (previousType) {
            case HANGUL_LEADING:
                return currentType == HANGUL_LEADING
                        || currentType == HANGUL_VOWEL
                        || currentType == HANGUL_SYLLABLE_WITHOUT_TRAILING
                        || currentType == HANGUL_SYLLABLE_WITH_TRAILING;
            case HANGUL_VOWEL:
            case HANGUL_SYLLABLE_WITHOUT_TRAILING:
                return currentType == HANGUL_VOWEL || currentType == HANGUL_TRAILING;
            case HANGUL_TRAILING:
            case HANGUL_SYLLABLE_WITH_TRAILING:
                return currentType == HANGUL_TRAILING;
            default:
                return false;
        }
    }

    private static int hangulSyllableType(int codePoint) {
        if ((codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0xA960 && codePoint <= 0xA97C)) {
            return HANGUL_LEADING;
        }
        if ((codePoint >= 0x1160 && codePoint <= 0x11A7)
                || (codePoint >= 0xD7B0 && codePoint <= 0xD7C6)) {
            return HANGUL_VOWEL;
        }
        if ((codePoint >= 0x11A8 && codePoint <= 0x11FF)
                || (codePoint >= 0xD7CB && codePoint <= 0xD7FB)) {
            return HANGUL_TRAILING;
        }
        if (codePoint >= 0xAC00 && codePoint <= 0xD7A3) {
            return (codePoint - 0xAC00) % 28 == 0
                    ? HANGUL_SYLLABLE_WITHOUT_TRAILING
                    : HANGUL_SYLLABLE_WITH_TRAILING;
        }
        return HANGUL_NONE;
    }
}
