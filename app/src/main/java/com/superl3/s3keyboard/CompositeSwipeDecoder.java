package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.List;

final class CompositeSwipeDecoder implements SwipeDecoder {
    private final SwipeDecoder primary;
    private final SwipeDecoder fallback;

    CompositeSwipeDecoder(SwipeDecoder primary, SwipeDecoder fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
        List<SwipeCandidate> primaryCandidates = decodeSafely(primary, trace, maxCandidates);
        if (!primaryCandidates.isEmpty()) {
            return primaryCandidates;
        }
        return decodeSafely(fallback, trace, maxCandidates);
    }

    private static List<SwipeCandidate> decodeSafely(
            SwipeDecoder decoder,
            SwipeTrace trace,
            int maxCandidates) {
        if (decoder == null) {
            return Collections.emptyList();
        }
        try {
            List<SwipeCandidate> candidates = decoder.decode(trace, maxCandidates);
            return candidates == null ? Collections.emptyList() : candidates;
        } catch (RuntimeException | LinkageError ignored) {
            return Collections.emptyList();
        }
    }
}
