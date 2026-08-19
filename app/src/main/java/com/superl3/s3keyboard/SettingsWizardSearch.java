package com.superl3.s3keyboard;

import java.util.Locale;

final class SettingsWizardSearch {
    private SettingsWizardSearch() {
    }

    static String normalize(CharSequence value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");
    }

    static boolean matches(String searchableText, String query) {
        if (query == null || query.toString().trim().isEmpty()) {
            return true;
        }
        String normalizedText = normalize(searchableText);
        boolean hasTerm = false;
        for (String rawTerm : query.toString().split("[\\s,;/·:_\\-]+")) {
            String term = normalize(rawTerm);
            if (term.isEmpty()) {
                continue;
            }
            hasTerm = true;
            if (!normalizedText.contains(term)) {
                return false;
            }
        }
        return hasTerm;
    }
}
