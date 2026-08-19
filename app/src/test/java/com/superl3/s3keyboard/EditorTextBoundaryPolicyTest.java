package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class EditorTextBoundaryPolicyTest {
    @Test
    public void wordDeleteCountsLettersAndTrailingWhitespaceInCodePoints() {
        assertEquals(5, EditorTextBoundaryPolicy.trailingWordCodePointCount("hello"));
        assertEquals(7, EditorTextBoundaryPolicy.trailingWordCodePointCount("hello  "));
        assertEquals(4, EditorTextBoundaryPolicy.trailingWordCodePointCount("안녕  "));
        assertEquals(7, EditorTextBoundaryPolicy.trailingWordCodePointCount("can't  "));
    }

    @Test
    public void wordDeleteTreatsSupplementaryLettersAsOneCodePoint() {
        String deseretLetter = new String(Character.toChars(0x10400));

        assertEquals(1, EditorTextBoundaryPolicy.trailingWordCodePointCount(deseretLetter));
        assertEquals(2, EditorTextBoundaryPolicy.trailingWordCodePointCount(deseretLetter + " "));
    }

    @Test
    public void punctuationAndEmojiStaySingleDeleteBoundaries() {
        assertEquals(0, EditorTextBoundaryPolicy.trailingWordCodePointCount("hello!"));
        assertEquals(0, EditorTextBoundaryPolicy.trailingWordCodePointCount("hello😀"));
        assertEquals(0, EditorTextBoundaryPolicy.trailingWordCodePointCount(null));
    }
}
