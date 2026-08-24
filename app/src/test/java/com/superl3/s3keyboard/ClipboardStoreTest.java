package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void expiredAndFutureDatedEntriesAreRemoved() {
        long now = 1_000_000_000L;
        List<ClipboardStore.Entry> retained = ClipboardStore.pruneExpired(Arrays.asList(
                new ClipboardStore.Entry("fresh", now - 1000L),
                new ClipboardStore.Entry("expired", now - ClipboardStore.MAX_ENTRY_AGE_MS - 1L),
                new ClipboardStore.Entry("future", now + 1L)), now);

        assertEquals(1, retained.size());
        assertEquals("fresh", retained.get(0).text);
    }

    private static String repeat(char value, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
