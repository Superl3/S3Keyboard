package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class TextToolsStoreTest {
    @Test
    public void savedClipboardItemsDeduplicateAndMoveToPinnedFront() {
        List<TextToolsStore.Item> items = new ArrayList<>();
        items.add(new TextToolsStore.Item("older", "Older", "alpha", false, 10L));
        items.add(new TextToolsStore.Item("other", "Other", "beta", true, 20L));

        List<TextToolsStore.Item> next = TextToolsStore.withSavedClipboardItem(items, "alpha", 30L);

        assertEquals(2, next.size());
        assertEquals("older", next.get(0).id);
        assertEquals("alpha", next.get(0).text);
        assertTrue(next.get(0).pinned);
        assertEquals(30L, next.get(0).updatedAtMs);
        assertEquals("other", next.get(1).id);
    }

    @Test
    public void pinnedItemsSortBeforeUnpinnedThenByRecentUpdate() {
        List<TextToolsStore.Item> items = new ArrayList<>();
        items.add(new TextToolsStore.Item("a", "A", "a", false, 100L));
        items.add(new TextToolsStore.Item("b", "B", "b", true, 50L));
        items.add(new TextToolsStore.Item("c", "C", "c", true, 75L));

        List<TextToolsStore.Item> ordered = TextToolsStore.ordered(items);

        assertEquals("c", ordered.get(0).id);
        assertEquals("b", ordered.get(1).id);
        assertEquals("a", ordered.get(2).id);
    }

    @Test
    public void versionedRoundTripPreservesPinNameAndText() {
        List<TextToolsStore.Item> items = new ArrayList<>();
        items.add(new TextToolsStore.Item("id", "Greeting", "hello", true, 123L));

        String encoded = TextToolsStore.encode(items);
        List<TextToolsStore.Item> decoded = TextToolsStore.decode(encoded);

        assertTrue(encoded.contains("\"version\":" + TextToolsStore.SCHEMA_VERSION));
        assertEquals(1, decoded.size());
        assertEquals("id", decoded.get(0).id);
        assertEquals("Greeting", decoded.get(0).name);
        assertEquals("hello", decoded.get(0).text);
        assertTrue(decoded.get(0).pinned);
    }

    @Test
    public void unpinKeepsSavedItemAndRenameKeepsContent() {
        List<TextToolsStore.Item> items = new ArrayList<>();
        items.add(new TextToolsStore.Item("id", "Old", "payload", true, 10L));

        List<TextToolsStore.Item> unpinned =
                TextToolsStore.withPinnedState(items, "id", false, 20L);
        List<TextToolsStore.Item> renamed =
                TextToolsStore.withRenamedItem(unpinned, "id", "New name", 30L);

        assertEquals(1, renamed.size());
        assertFalse(renamed.get(0).pinned);
        assertEquals("New name", renamed.get(0).name);
        assertEquals("payload", renamed.get(0).text);
        assertEquals(30L, renamed.get(0).updatedAtMs);
    }

    @Test
    public void unknownSchemaIsIgnoredSafely() {
        assertTrue(TextToolsStore.decode("{\"version\":999,\"items\":[]}").isEmpty());
    }
}
