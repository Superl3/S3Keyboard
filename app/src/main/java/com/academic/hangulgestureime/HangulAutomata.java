package com.academic.hangulgestureime;

import java.util.HashMap;
import java.util.Map;

final class HangulAutomata {
    private static final char[] INITIAL_CHARS = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final char[] VOWEL_CHARS = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    private static final char[] FINAL_CHARS = {
            '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
            'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final Map<Character, Integer> INITIAL_BY_CHAR = new HashMap<>();
    private static final Map<Character, Integer> VOWEL_BY_CHAR = new HashMap<>();
    private static final Map<Character, Integer> FINAL_BY_CHAR = new HashMap<>();
    private static final Map<String, Integer> DOUBLE_INITIAL_BY_SEQUENCE = new HashMap<>();
    private static final Map<String, Integer> COMPOUND_VOWEL_BY_SEQUENCE = new HashMap<>();
    private static final Map<Integer, Integer> VOWEL_BACKSPACE = new HashMap<>();
    private static final Map<String, Integer> COMPOUND_FINAL_BY_SEQUENCE = new HashMap<>();
    private static final Map<Integer, SplitFinal> SPLIT_FINAL = new HashMap<>();
    private static final Map<Integer, Integer> INITIAL_BACKSPACE = new HashMap<>();

    static {
        for (int i = 0; i < INITIAL_CHARS.length; i++) {
            INITIAL_BY_CHAR.put(INITIAL_CHARS[i], i);
        }
        for (int i = 0; i < VOWEL_CHARS.length; i++) {
            VOWEL_BY_CHAR.put(VOWEL_CHARS[i], i);
        }
        for (int i = 1; i < FINAL_CHARS.length; i++) {
            FINAL_BY_CHAR.put(FINAL_CHARS[i], i);
        }

        putDoubleInitial('ㄱ', 'ㄱ', 'ㄲ');
        putDoubleInitial('ㄷ', 'ㄷ', 'ㄸ');
        putDoubleInitial('ㅂ', 'ㅂ', 'ㅃ');
        putDoubleInitial('ㅅ', 'ㅅ', 'ㅆ');
        putDoubleInitial('ㅈ', 'ㅈ', 'ㅉ');

        putBackspaceInitial('ㄲ', 'ㄱ');
        putBackspaceInitial('ㄸ', 'ㄷ');
        putBackspaceInitial('ㅃ', 'ㅂ');
        putBackspaceInitial('ㅆ', 'ㅅ');
        putBackspaceInitial('ㅉ', 'ㅈ');

        putCompoundVowel('ㅏ', 'ㅣ', 'ㅐ');
        putCompoundVowel('ㅑ', 'ㅣ', 'ㅒ');
        putCompoundVowel('ㅓ', 'ㅣ', 'ㅔ');
        putCompoundVowel('ㅕ', 'ㅣ', 'ㅖ');
        putCompoundVowel('ㅗ', 'ㅏ', 'ㅘ');
        putCompoundVowel('ㅗ', 'ㅐ', 'ㅙ');
        putCompoundVowel('ㅗ', 'ㅣ', 'ㅚ');
        putCompoundVowel('ㅜ', 'ㅓ', 'ㅝ');
        putCompoundVowel('ㅜ', 'ㅔ', 'ㅞ');
        putCompoundVowel('ㅜ', 'ㅣ', 'ㅟ');
        putCompoundVowel('ㅡ', 'ㅣ', 'ㅢ');
        putCompoundVowel('ㅓ', 'ㅓ', 'ㅕ');
        putCompoundVowel('ㅏ', 'ㅏ', 'ㅑ');
        putCompoundVowel('ㅗ', 'ㅗ', 'ㅛ');
        putCompoundVowel('ㅜ', 'ㅜ', 'ㅠ');
        putCompoundVowel('ㅔ', 'ㅔ', 'ㅖ');
        putCompoundVowel('ㅐ', 'ㅐ', 'ㅒ');

        putCompoundFinal('ㄱ', 'ㄱ', 'ㄲ');
        putCompoundFinal('ㄱ', 'ㅅ', 'ㄳ');
        putCompoundFinal('ㄴ', 'ㅈ', 'ㄵ');
        putCompoundFinal('ㄴ', 'ㅎ', 'ㄶ');
        putCompoundFinal('ㄹ', 'ㄱ', 'ㄺ');
        putCompoundFinal('ㄹ', 'ㅁ', 'ㄻ');
        putCompoundFinal('ㄹ', 'ㅂ', 'ㄼ');
        putCompoundFinal('ㄹ', 'ㅅ', 'ㄽ');
        putCompoundFinal('ㄹ', 'ㅌ', 'ㄾ');
        putCompoundFinal('ㄹ', 'ㅍ', 'ㄿ');
        putCompoundFinal('ㄹ', 'ㅎ', 'ㅀ');
        putCompoundFinal('ㅂ', 'ㅅ', 'ㅄ');
        putCompoundFinal('ㅅ', 'ㅅ', 'ㅆ');

        putSplitFinal('ㄳ', 'ㄱ', 'ㅅ');
        putSplitFinal('ㄵ', 'ㄴ', 'ㅈ');
        putSplitFinal('ㄶ', 'ㄴ', 'ㅎ');
        putSplitFinal('ㄺ', 'ㄹ', 'ㄱ');
        putSplitFinal('ㄻ', 'ㄹ', 'ㅁ');
        putSplitFinal('ㄼ', 'ㄹ', 'ㅂ');
        putSplitFinal('ㄽ', 'ㄹ', 'ㅅ');
        putSplitFinal('ㄾ', 'ㄹ', 'ㅌ');
        putSplitFinal('ㄿ', 'ㄹ', 'ㅍ');
        putSplitFinal('ㅀ', 'ㄹ', 'ㅎ');
        putSplitFinal('ㅄ', 'ㅂ', 'ㅅ');
    }

    private int initial = -1;
    private int vowel = -1;
    private int finalConsonant = 0;

    String input(char ch) {
        if (INITIAL_BY_CHAR.containsKey(ch)) {
            return inputConsonant(ch);
        }
        if (VOWEL_BY_CHAR.containsKey(ch)) {
            return inputVowel(ch);
        }

        String committed = flush();
        return committed + ch;
    }

    boolean backspace() {
        if (finalConsonant > 0) {
            SplitFinal split = SPLIT_FINAL.get(finalConsonant);
            finalConsonant = split == null ? 0 : split.leadingFinal;
            return true;
        }
        if (vowel >= 0) {
            Integer previous = VOWEL_BACKSPACE.get(vowel);
            vowel = previous == null ? -1 : previous;
            return true;
        }
        if (initial >= 0) {
            Integer previous = INITIAL_BACKSPACE.get(initial);
            initial = previous == null ? -1 : previous;
            return true;
        }
        return false;
    }

    String flush() {
        String text = getComposingText();
        reset();
        return text;
    }

    void reset() {
        initial = -1;
        vowel = -1;
        finalConsonant = 0;
    }

    boolean isEmpty() {
        return initial < 0 && vowel < 0 && finalConsonant == 0;
    }

    char currentVowelWithoutFinal() {
        return vowel >= 0 && finalConsonant == 0 ? VOWEL_CHARS[vowel] : '\0';
    }

    static boolean isInitialConsonant(char ch) {
        return INITIAL_BY_CHAR.containsKey(ch);
    }

    static boolean isVowel(char ch) {
        return VOWEL_BY_CHAR.containsKey(ch);
    }

    static boolean canBeFinalConsonant(char ch) {
        return FINAL_BY_CHAR.containsKey(ch);
    }

    static String decomposeOpenSyllable(char ch) {
        if (ch < 0xAC00 || ch > 0xD7A3) {
            return null;
        }
        int offset = ch - 0xAC00;
        if (offset % 28 != 0) {
            return null;
        }
        int initialIndex = offset / (21 * 28);
        int vowelIndex = (offset / 28) % 21;
        return "" + INITIAL_CHARS[initialIndex] + VOWEL_CHARS[vowelIndex];
    }

    String getComposingText() {
        if (initial < 0 && vowel < 0) {
            return "";
        }
        if (initial >= 0 && vowel >= 0) {
            int code = 0xAC00 + ((initial * 21) + vowel) * 28 + finalConsonant;
            return String.valueOf((char) code);
        }
        if (initial >= 0) {
            return String.valueOf(INITIAL_CHARS[initial]);
        }
        return String.valueOf(VOWEL_CHARS[vowel]);
    }

    private String inputConsonant(char ch) {
        int nextInitial = INITIAL_BY_CHAR.get(ch);
        StringBuilder committed = new StringBuilder();

        if (initial < 0) {
            if (vowel >= 0) {
                committed.append(getComposingText());
                vowel = -1;
            }
            initial = nextInitial;
            return committed.toString();
        }

        if (vowel < 0) {
            String sequence = "" + INITIAL_CHARS[initial] + ch;
            Integer doubled = DOUBLE_INITIAL_BY_SEQUENCE.get(sequence);
            if (doubled != null) {
                initial = doubled;
                return "";
            }

            committed.append(getComposingText());
            initial = nextInitial;
            return committed.toString();
        }

        Integer nextFinal = FINAL_BY_CHAR.get(ch);
        if (nextFinal == null) {
            committed.append(getComposingText());
            initial = nextInitial;
            vowel = -1;
            finalConsonant = 0;
            return committed.toString();
        }

        if (finalConsonant == 0) {
            finalConsonant = nextFinal;
            return "";
        }

        String sequence = "" + FINAL_CHARS[finalConsonant] + ch;
        Integer compound = COMPOUND_FINAL_BY_SEQUENCE.get(sequence);
        if (compound != null) {
            finalConsonant = compound;
            return "";
        }

        committed.append(getComposingText());
        initial = nextInitial;
        vowel = -1;
        finalConsonant = 0;
        return committed.toString();
    }

    private String inputVowel(char ch) {
        int nextVowel = VOWEL_BY_CHAR.get(ch);
        StringBuilder committed = new StringBuilder();

        if (initial < 0 && vowel < 0) {
            vowel = nextVowel;
            return "";
        }

        if (initial < 0) {
            String sequence = "" + VOWEL_CHARS[vowel] + ch;
            Integer compound = COMPOUND_VOWEL_BY_SEQUENCE.get(sequence);
            if (compound != null) {
                vowel = compound;
                return "";
            }

            committed.append(getComposingText());
            vowel = nextVowel;
            return "";
        }

        if (vowel < 0) {
            vowel = nextVowel;
            return "";
        }

        if (finalConsonant == 0) {
            String sequence = "" + VOWEL_CHARS[vowel] + ch;
            Integer compound = COMPOUND_VOWEL_BY_SEQUENCE.get(sequence);
            if (compound != null) {
                vowel = compound;
                return "";
            }

            committed.append(getComposingText());
            initial = INITIAL_BY_CHAR.get('ㅇ');
            vowel = nextVowel;
            finalConsonant = 0;
            return committed.toString();
        }

        SplitFinal split = SPLIT_FINAL.get(finalConsonant);
        if (split != null) {
            finalConsonant = split.leadingFinal;
            committed.append(getComposingText());
            initial = split.trailingInitial;
        } else {
            int movedInitial = initialFromFinal(finalConsonant);
            finalConsonant = 0;
            committed.append(getComposingText());
            initial = movedInitial;
        }

        vowel = nextVowel;
        finalConsonant = 0;
        return committed.toString();
    }

    private static int initialFromFinal(int finalIndex) {
        Integer initialIndex = INITIAL_BY_CHAR.get(FINAL_CHARS[finalIndex]);
        if (initialIndex == null) {
            return INITIAL_BY_CHAR.get('ㅇ');
        }
        return initialIndex;
    }

    private static void putDoubleInitial(char first, char second, char combined) {
        DOUBLE_INITIAL_BY_SEQUENCE.put("" + first + second, INITIAL_BY_CHAR.get(combined));
    }

    private static void putBackspaceInitial(char combined, char previous) {
        INITIAL_BACKSPACE.put(INITIAL_BY_CHAR.get(combined), INITIAL_BY_CHAR.get(previous));
    }

    private static void putCompoundVowel(char first, char second, char combined) {
        int firstIndex = VOWEL_BY_CHAR.get(first);
        int combinedIndex = VOWEL_BY_CHAR.get(combined);
        COMPOUND_VOWEL_BY_SEQUENCE.put("" + first + second, combinedIndex);
        VOWEL_BACKSPACE.put(combinedIndex, firstIndex);
    }

    private static void putCompoundFinal(char first, char second, char combined) {
        COMPOUND_FINAL_BY_SEQUENCE.put("" + first + second, FINAL_BY_CHAR.get(combined));
    }

    private static void putSplitFinal(char combined, char leading, char trailing) {
        SPLIT_FINAL.put(
                FINAL_BY_CHAR.get(combined),
                new SplitFinal(FINAL_BY_CHAR.get(leading), INITIAL_BY_CHAR.get(trailing)));
    }

    private static final class SplitFinal {
        final int leadingFinal;
        final int trailingInitial;

        SplitFinal(int leadingFinal, int trailingInitial) {
            this.leadingFinal = leadingFinal;
            this.trailingInitial = trailingInitial;
        }
    }
}
