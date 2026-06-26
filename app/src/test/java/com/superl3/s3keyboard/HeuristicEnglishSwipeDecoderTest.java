package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class HeuristicEnglishSwipeDecoderTest {
    @Test
    public void decodesCommonCollapsedQwertyPathToWord() {
        SwipeTrace trace = trace("k", "e", "y", "b", "o", "a", "r", "d");

        List<SwipeCandidate> candidates = HeuristicEnglishSwipeDecoder.DEFAULT.decode(trace, 4);

        assertTrue(!candidates.isEmpty());
        assertEquals("keyboard", candidates.get(0).word);
    }

    @Test
    public void handlesRepeatedLettersCollapsedOutOfSwipePath() {
        SwipeTrace trace = trace("h", "e", "l", "o");

        List<SwipeCandidate> candidates = HeuristicEnglishSwipeDecoder.DEFAULT.decode(trace, 4);

        assertTrue(!candidates.isEmpty());
        assertEquals("hello", candidates.get(0).word);
    }

    @Test
    public void singleKeyDragDoesNotProduceSwipeCandidate() {
        SwipeTrace trace = trace("a");

        assertTrue(HeuristicEnglishSwipeDecoder.DEFAULT.decode(trace, 4).isEmpty());
    }

    @Test
    public void keepsRawSequenceFallbackForUnknownButIntentionalPath() {
        SwipeTrace trace = trace("x", "q");

        List<SwipeCandidate> candidates = HeuristicEnglishSwipeDecoder.DEFAULT.decode(trace, 4);

        assertTrue(!candidates.isEmpty());
        assertEquals("xq", candidates.get(0).word);
    }

    private static SwipeTrace trace(String... labels) {
        SwipeTrace.Builder builder = new SwipeTrace.Builder();
        for (int i = 0; i < labels.length; i++) {
            builder.add(i * 10f, 0f, i * 10L,
                    new GestureKey(labels[i], labels[i], labels[i].toUpperCase(), null, null, null, null));
        }
        return builder.build();
    }
}
