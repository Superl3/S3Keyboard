package com.superl3.s3keyboard;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class SwipeTraceTest {
    @Test
    public void normalizesPointsAgainstProvidedBounds() {
        SwipeTrace.Builder builder = new SwipeTrace.Builder(10f, 20f, 110f, 220f);
        builder.add(10f, 20f, 1000L, key("a"));
        builder.add(60f, 120f, 1016L, key("b"));
        builder.add(120f, 240f, 1048L, key("c"));

        SwipeTrace trace = builder.build();

        assertArrayEquals(new float[] {0f, 0.5f, 1f}, trace.normalizedXArray(), 0.0001f);
        assertArrayEquals(new float[] {0f, 0.5f, 1f}, trace.normalizedYArray(), 0.0001f);
        assertArrayEquals(new float[] {0f, 16f, 48f}, trace.relativeTimeMsArray(), 0.0001f);
    }

    @Test
    public void keepsCollapsedKeySequenceForHeuristicFallback() {
        SwipeTrace.Builder builder = new SwipeTrace.Builder(0f, 0f, 100f, 100f);
        builder.add(0f, 0f, 0L, key("h"));
        builder.add(10f, 0f, 10L, key("h"));
        builder.add(20f, 0f, 20L, key("i"));

        SwipeTrace trace = builder.build();

        assertEquals("hi", trace.collapsedKeySequence());
    }

    private static GestureKey key(String label) {
        return new GestureKey(label, label, label.toUpperCase(), null, null, null, null);
    }
}
