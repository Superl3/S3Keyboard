package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class VoiceInputResultTest {
    @Test
    public void firstRecognizedTextSkipsEmptyCandidatesAndTrimsEdges() {
        assertEquals(
                "만나서 반가워요",
                VoiceInputResult.firstRecognizedText(Arrays.asList(
                        null,
                        "   ",
                        "  만나서 반가워요  ",
                        "다른 후보")));
    }

    @Test
    public void missingRecognitionCandidatesReturnEmptyText() {
        assertEquals("", VoiceInputResult.firstRecognizedText(null));
        assertEquals("", VoiceInputResult.firstRecognizedText(Collections.emptyList()));
        assertEquals("", VoiceInputResult.normalizeRecognizedText(null));
    }
}
