package com.superl3.s3keyboard;

final class TextActionProviderRequest {
    static final int MAX_TEXT_CHARS = 2048;

    static final class BuildResult {
        final TextActionProviderRequest request;
        final TextActionProviderError error;

        private BuildResult(TextActionProviderRequest request, TextActionProviderError error) {
            this.request = request;
            this.error = error;
        }

        static BuildResult success(TextActionProviderRequest request) {
            return new BuildResult(request, null);
        }

        static BuildResult failure(TextActionProviderError error) {
            return new BuildResult(null, error);
        }
    }

    final TextAction action;
    final String text;
    final String targetLanguage;

    private TextActionProviderRequest(TextAction action, String text, String targetLanguage) {
        this.action = action;
        this.text = text;
        this.targetLanguage = targetLanguage == null ? "" : targetLanguage;
    }

    static BuildResult build(
            TextAction action,
            TextActionRange range,
            EditorInputPolicy policy,
            boolean remoteModeEnabled,
            boolean providerEnabled,
            String targetLanguage) {
        if (!providerEnabled) {
            return BuildResult.failure(TextActionProviderError.DISABLED);
        }
        if (policy == null || !policy.allowsTextActions(remoteModeEnabled)) {
            return BuildResult.failure(TextActionProviderError.DENIED);
        }
        if (action == null || action == TextAction.RESTORE_ORIGINAL || range == null || range.text == null) {
            return BuildResult.failure(TextActionProviderError.INVALID_REQUEST);
        }
        if (range.text.isEmpty()) {
            return BuildResult.failure(TextActionProviderError.INVALID_REQUEST);
        }
        if (range.text.length() > MAX_TEXT_CHARS) {
            return BuildResult.failure(TextActionProviderError.TOO_LARGE);
        }
        return BuildResult.success(new TextActionProviderRequest(
                action,
                range.text,
                targetLanguage));
    }
}
