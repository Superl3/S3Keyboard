package com.superl3.s3keyboard;

import java.util.Locale;

final class TextActionEngine {
    private TextActionEngine() {
    }

    static String correct(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        String result = source
                .replaceAll("[ \\t]{2,}", " ")
                .replaceAll("[ \\t]+([,.;:!?])", "$1");
        result = replaceWordIgnoreCase(result, "teh", "the");
        result = replaceWordIgnoreCase(result, "adn", "and");
        result = replaceWordIgnoreCase(result, "dont", "don't");
        return capitalizeFirstLatinLetter(result);
    }

    private static String replaceWordIgnoreCase(String text, String typo, String replacement) {
        return text.replaceAll("(?i)(?<![A-Za-z])" + typo + "(?![A-Za-z])", replacement);
    }

    private static String capitalizeFirstLatinLetter(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch >= 'a' && ch <= 'z') {
                return text.substring(0, i)
                        + String.valueOf(ch).toUpperCase(Locale.ROOT)
                        + text.substring(i + 1);
            }
            if (Character.isLetter(ch)) {
                break;
            }
        }
        return text;
    }
}
