package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class EnglishQwertyCorrectionEngine {
    static final EnglishQwertyCorrectionEngine DEFAULT =
            new EnglishQwertyCorrectionEngine(EnglishWordDictionary.COMMON_WORDS);

    private static final int MAX_WORD_LENGTH = 32;
    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator
            .comparingInt((Candidate candidate) -> candidate.exactCorrection ? 1 : 0)
            .reversed()
            .thenComparing(Comparator
                    .comparingDouble((Candidate candidate) -> candidate.score)
                    .reversed())
            .thenComparingInt(candidate -> candidate.text.length());

    private final String[] words;
    private final Map<String, String> exactCorrections;
    private final Map<Character, Set<Character>> adjacentKeys;

    EnglishQwertyCorrectionEngine(String[] words) {
        this.words = words == null ? new String[0] : words.clone();
        this.exactCorrections = exactCorrections();
        this.adjacentKeys = qwertyAdjacency();
    }

    List<Candidate> suggest(String currentWord, int maxCandidates) {
        String normalized = normalizeWord(currentWord);
        if (normalized == null || normalized.length() < 2 || maxCandidates <= 0) {
            return Collections.emptyList();
        }
        List<Candidate> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        String exact = exactCorrections.get(normalized);
        if (exact != null && seen.add(exact)) {
            candidates.add(new Candidate(applyCase(currentWord, exact), 0.98f, true));
        }
        for (String word : words) {
            String candidate = normalizeWord(word);
            if (candidate == null || candidate.equals(normalized) || !seen.add(candidate)) {
                continue;
            }
            float score = score(normalized, candidate);
            if (score >= 0.70f) {
                candidates.add(new Candidate(applyCase(currentWord, candidate), score, false));
            }
        }
        candidates.sort(CANDIDATE_ORDER);
        if (candidates.size() <= maxCandidates) {
            return candidates;
        }
        return new ArrayList<>(candidates.subList(0, maxCandidates));
    }

    String autoCorrection(String currentWord) {
        String normalized = normalizeWord(currentWord);
        if (normalized == null) {
            return null;
        }
        String correction = exactCorrections.get(normalized);
        return correction == null ? null : applyCase(currentWord, correction);
    }

    private float score(String source, String candidate) {
        if (source.length() > MAX_WORD_LENGTH || candidate.length() > MAX_WORD_LENGTH) {
            return 0f;
        }
        if (isAdjacentSubstitution(source, candidate)) {
            return 0.92f;
        }
        if (isTransposition(source, candidate)) {
            return 0.90f;
        }
        int distance = editDistance(source, candidate);
        if (distance == 1) {
            return source.charAt(0) == candidate.charAt(0) ? 0.84f : 0.76f;
        }
        if (source.length() >= 5 && distance == 2 && sameEdge(source, candidate)) {
            return 0.72f;
        }
        return 0f;
    }

    private boolean isAdjacentSubstitution(String source, String candidate) {
        if (source.length() != candidate.length()) {
            return false;
        }
        int adjacentSubstitutions = 0;
        for (int i = 0; i < source.length(); i++) {
            char a = source.charAt(i);
            char b = candidate.charAt(i);
            if (a == b) {
                continue;
            }
            Set<Character> adjacent = adjacentKeys.get(a);
            if (adjacent == null || !adjacent.contains(b)) {
                return false;
            }
            adjacentSubstitutions++;
        }
        return adjacentSubstitutions > 0 && adjacentSubstitutions <= 2;
    }

    private static boolean isTransposition(String source, String candidate) {
        if (source.length() != candidate.length()) {
            return false;
        }
        int first = -1;
        int second = -1;
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == candidate.charAt(i)) {
                continue;
            }
            if (first < 0) {
                first = i;
            } else if (second < 0) {
                second = i;
            } else {
                return false;
            }
        }
        return first >= 0
                && second == first + 1
                && source.charAt(first) == candidate.charAt(second)
                && source.charAt(second) == candidate.charAt(first);
    }

    private static boolean sameEdge(String source, String candidate) {
        return !source.isEmpty()
                && !candidate.isEmpty()
                && source.charAt(0) == candidate.charAt(0)
                && source.charAt(source.length() - 1) == candidate.charAt(candidate.length() - 1);
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

    static String normalizeWord(String word) {
        if (word == null || word.isEmpty() || word.length() > MAX_WORD_LENGTH) {
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

    static String applyCase(String source, String candidate) {
        if (candidate == null || candidate.isEmpty()) {
            return candidate;
        }
        if (source == null || source.isEmpty()) {
            return candidate;
        }
        boolean allCaps = true;
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch >= 'a' && ch <= 'z') {
                allCaps = false;
                break;
            }
        }
        if (allCaps) {
            return candidate.toUpperCase(Locale.US);
        }
        char first = source.charAt(0);
        if (first >= 'A' && first <= 'Z') {
            return candidate.substring(0, 1).toUpperCase(Locale.US) + candidate.substring(1);
        }
        return candidate;
    }

    private static Map<String, String> exactCorrections() {
        Map<String, String> corrections = new LinkedHashMap<>();
        corrections.put("teh", "the");
        corrections.put("hte", "the");
        corrections.put("adn", "and");
        corrections.put("nad", "and");
        corrections.put("taht", "that");
        corrections.put("thta", "that");
        corrections.put("wiht", "with");
        corrections.put("wih", "with");
        corrections.put("youre", "you're");
        corrections.put("ypu", "you");
        corrections.put("yuo", "you");
        corrections.put("gppd", "good");
        corrections.put("goof", "good");
        corrections.put("hellp", "hello");
        corrections.put("helo", "hello");
        corrections.put("keybaord", "keyboard");
        corrections.put("keybpard", "keyboard");
        corrections.put("qwertu", "qwerty");
        corrections.put("recieve", "receive");
        corrections.put("seperate", "separate");
        return corrections;
    }

    private static Map<Character, Set<Character>> qwertyAdjacency() {
        String[] rows = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
        Map<Character, Set<Character>> map = new HashMap<>();
        for (int row = 0; row < rows.length; row++) {
            String letters = rows[row];
            for (int col = 0; col < letters.length(); col++) {
                char key = letters.charAt(col);
                Set<Character> adjacent = map.computeIfAbsent(key, ignored -> new HashSet<>());
                for (int dr = -1; dr <= 1; dr++) {
                    int neighborRow = row + dr;
                    if (neighborRow < 0 || neighborRow >= rows.length) {
                        continue;
                    }
                    String neighborLetters = rows[neighborRow];
                    for (int dc = -1; dc <= 1; dc++) {
                        int neighborCol = col + dc;
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        if (neighborCol >= 0 && neighborCol < neighborLetters.length()) {
                            adjacent.add(neighborLetters.charAt(neighborCol));
                        }
                    }
                }
            }
        }
        return map;
    }

    static final class Candidate {
        final String text;
        final float score;
        final boolean exactCorrection;

        Candidate(String text, float score, boolean exactCorrection) {
            this.text = text == null ? "" : text;
            this.score = score;
            this.exactCorrection = exactCorrection;
        }
    }
}
