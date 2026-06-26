package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class HeuristicEnglishSwipeDecoder implements SwipeDecoder {
    static final HeuristicEnglishSwipeDecoder DEFAULT =
            new HeuristicEnglishSwipeDecoder(EnglishSwipeDictionary.COMMON_WORDS);

    private final String[] words;

    HeuristicEnglishSwipeDecoder(String[] words) {
        this.words = words == null ? new String[0] : words.clone();
    }

    @Override
    public List<SwipeCandidate> decode(SwipeTrace trace, int maxCandidates) {
        if (trace == null || trace.distinctKeyCount() < 2 || maxCandidates <= 0) {
            return Collections.emptyList();
        }
        String sequence = trace.collapsedKeySequence();
        if (sequence.length() < 2) {
            return Collections.emptyList();
        }

        List<SwipeCandidate> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String word : words) {
            String normalized = normalizeWord(word);
            if (normalized == null || !seen.add(normalized)) {
                continue;
            }
            float score = score(sequence, normalized);
            if (score >= 0.48f) {
                candidates.add(new SwipeCandidate(normalized, score));
            }
        }
        if (sequence.length() >= 2 && seen.add(sequence)) {
            candidates.add(new SwipeCandidate(sequence, 0.50f));
        }
        candidates.sort(new Comparator<SwipeCandidate>() {
            @Override
            public int compare(SwipeCandidate first, SwipeCandidate second) {
                int scoreOrder = Float.compare(second.score, first.score);
                if (scoreOrder != 0) {
                    return scoreOrder;
                }
                return Integer.compare(first.word.length(), second.word.length());
            }
        });
        if (candidates.size() <= maxCandidates) {
            return candidates;
        }
        return new ArrayList<>(candidates.subList(0, maxCandidates));
    }

    private static String normalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }
        String normalized = word.toLowerCase(Locale.US);
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch < 'a' || ch > 'z') {
                return null;
            }
        }
        return normalized;
    }

    private static float score(String sequence, String word) {
        String collapsedWord = collapseRepeats(word);
        float firstLast = 0f;
        if (word.charAt(0) == sequence.charAt(0)) {
            firstLast += 0.22f;
        }
        if (word.charAt(word.length() - 1) == sequence.charAt(sequence.length() - 1)) {
            firstLast += 0.22f;
        }
        float order = orderedCoverage(sequence, collapsedWord) * 0.32f;
        int distance = editDistance(sequence, collapsedWord);
        float edit = Math.max(0f, 1f - (distance / (float) Math.max(sequence.length(), collapsedWord.length())))
                * 0.18f;
        float length = Math.max(0f, 1f - Math.abs(sequence.length() - collapsedWord.length())
                / (float) Math.max(sequence.length(), collapsedWord.length()))
                * 0.06f;
        return firstLast + order + edit + length;
    }

    private static String collapseRepeats(String value) {
        StringBuilder builder = new StringBuilder();
        char previous = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == previous) {
                continue;
            }
            builder.append(ch);
            previous = ch;
        }
        return builder.toString();
    }

    private static float orderedCoverage(String sequence, String word) {
        int matched = 0;
        int wordIndex = 0;
        for (int i = 0; i < sequence.length() && wordIndex < word.length(); i++) {
            char target = sequence.charAt(i);
            while (wordIndex < word.length() && word.charAt(wordIndex) != target) {
                wordIndex++;
            }
            if (wordIndex < word.length()) {
                matched++;
                wordIndex++;
            }
        }
        return matched / (float) Math.max(sequence.length(), word.length());
    }

    private static int editDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }
}
