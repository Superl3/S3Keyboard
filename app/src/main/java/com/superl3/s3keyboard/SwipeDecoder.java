package com.superl3.s3keyboard;

import java.util.List;

interface SwipeDecoder {
    List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates);
}
