package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class CompositeSwipeDecoderTest {
    @Test
    public void usesPrimaryCandidatesWhenAvailable() {
        SwipeDecoder decoder = new CompositeSwipeDecoder(
                fixed("model", 0.9f),
                fixed("fallback", 0.5f));

        List<SwipeCandidate> candidates = decoder.decode(trace(), 4);

        assertEquals("model", candidates.get(0).word);
    }

    @Test
    public void fallsBackWhenPrimaryIsUnavailable() {
        SwipeDecoder decoder = new CompositeSwipeDecoder(
                empty(),
                fixed("fallback", 0.5f));

        List<SwipeCandidate> candidates = decoder.decode(trace(), 4);

        assertEquals("fallback", candidates.get(0).word);
    }

    @Test
    public void fallsBackWhenPrimaryThrows() {
        SwipeDecoder decoder = new CompositeSwipeDecoder(
                throwing(),
                fixed("fallback", 0.5f));

        List<SwipeCandidate> candidates = decoder.decode(trace(), 4);

        assertEquals("fallback", candidates.get(0).word);
    }

    private static SwipeDecoder fixed(String word, float score) {
        return new SwipeDecoder() {
            @Override
            public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
                return Collections.singletonList(new SwipeCandidate(word, score));
            }
        };
    }

    private static SwipeDecoder empty() {
        return new SwipeDecoder() {
            @Override
            public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
                return Collections.emptyList();
            }
        };
    }

    private static SwipeDecoder throwing() {
        return new SwipeDecoder() {
            @Override
            public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
                throw new IllegalStateException("boom");
            }
        };
    }

    private static SwipeTrace trace() {
        SwipeTrace.Builder builder = new SwipeTrace.Builder(0f, 0f, 100f, 100f);
        builder.add(0f, 0f, 0L, key("a"));
        builder.add(100f, 100f, 16L, key("b"));
        return builder.build();
    }

    private static GestureKey key(String label) {
        return new GestureKey(label, label, label.toUpperCase(), null, null, null, null);
    }
}
