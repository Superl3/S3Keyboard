package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class ClipboardStoreTest {
    @Test
    public void rejectsEmptyAndOversizedHistoryEntries() {
        assertNull(ClipboardStore.storableEntry(null));
        assertNull(ClipboardStore.storableEntry(""));
        assertNull(ClipboardStore.storableEntry(repeat('a', ClipboardStore.MAX_ENTRY_LENGTH + 1)));
    }

    @Test
    public void preservesBoundedTextAndRemovesTheStorageDelimiter() {
        assertEquals("hello", ClipboardStore.storableEntry("hello"));
        assertEquals("hello world", ClipboardStore.storableEntry("hello\u001Fworld"));
        String maximum = repeat('x', ClipboardStore.MAX_ENTRY_LENGTH);
        assertEquals(maximum, ClipboardStore.storableEntry(maximum));
    }

    private static String repeat(char value, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
