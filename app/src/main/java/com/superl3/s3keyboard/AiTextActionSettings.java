package com.superl3.s3keyboard;

final class AiTextActionSettings {
    static final String LOCAL_TEST_PROVIDER_ID = "local-test";
    static final int DEFAULT_TIMEOUT_MS = 5000;
    static final int MIN_TIMEOUT_MS = 1000;
    static final int MAX_TIMEOUT_MS = 15000;
    static final AiTextActionSettings DEFAULT = new AiTextActionSettings(
            false,
            LOCAL_TEST_PROVIDER_ID,
            DEFAULT_TIMEOUT_MS,
            "ko");

    final boolean enabled;
    final String providerId;
    final int timeoutMs;
    final String translateTargetLanguage;

    AiTextActionSettings(
            boolean enabled,
            String providerId,
            int timeoutMs,
            String translateTargetLanguage) {
        this.enabled = enabled;
        this.providerId = normalizeProviderId(providerId);
        this.timeoutMs = Math.max(MIN_TIMEOUT_MS, Math.min(MAX_TIMEOUT_MS, timeoutMs));
        this.translateTargetLanguage = normalizeLanguage(translateTargetLanguage);
    }

    AiTextActionSettings withEnabled(boolean nextEnabled) {
        return new AiTextActionSettings(
                nextEnabled,
                providerId,
                timeoutMs,
                translateTargetLanguage);
    }

    AiTextActionSettings withTimeoutMs(int nextTimeoutMs) {
        return new AiTextActionSettings(
                enabled,
                providerId,
                nextTimeoutMs,
                translateTargetLanguage);
    }

    AiTextActionSettings withTranslateTarget(String language) {
        return new AiTextActionSettings(enabled, providerId, timeoutMs, language);
    }

    private static String normalizeProviderId(String value) {
        return value == null || value.trim().isEmpty()
                ? LOCAL_TEST_PROVIDER_ID
                : value.trim();
    }

    private static String normalizeLanguage(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        return "en".equals(normalized) ? "en" : "ko";
    }
}
