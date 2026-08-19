package com.superl3.s3keyboard;

import java.util.List;

final class VoiceInputResult {
    static final int RECOGNIZED = 1;
    static final int UNAVAILABLE = 2;
    static final int NO_MATCH = 3;
    static final int CANCELLED = 4;

    static final String EXTRA_RECEIVER = "com.superl3.s3keyboard.extra.VOICE_RECEIVER";
    static final String EXTRA_LANGUAGE_TAG = "com.superl3.s3keyboard.extra.VOICE_LANGUAGE";
    static final String EXTRA_TEXT = "com.superl3.s3keyboard.extra.VOICE_TEXT";

    private VoiceInputResult() {
    }

    static String firstRecognizedText(List<String> candidates) {
        if (candidates == null) {
            return "";
        }
        for (String candidate : candidates) {
            String normalized = normalizeRecognizedText(candidate);
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "";
    }

    static String normalizeRecognizedText(String text) {
        return text == null ? "" : text.trim();
    }
}
