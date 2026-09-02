package com.superl3.s3keyboard;

final class LocalTestTextActionProvider implements TextActionProvider {
    private static final long NORMAL_DELAY_MS = 180L;
    private static final long SLOW_DELAY_MS = 5000L;

    private final TextActionTaskScheduler scheduler;

    LocalTestTextActionProvider(TextActionTaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public String id() {
        return AiTextActionSettings.LOCAL_TEST_PROVIDER_ID;
    }

    @Override
    public boolean isAvailable() {
        return scheduler != null;
    }

    @Override
    public RequestHandle request(TextActionProviderRequest request, Callback callback) {
        if (request == null || callback == null || scheduler == null) {
            return null;
        }
        if (request.text.contains("[[timeout]]")) {
            return () -> { };
        }
        long delayMs = request.text.contains("[[slow]]") ? SLOW_DELAY_MS : NORMAL_DELAY_MS;
        TextActionTaskScheduler.CancelHandle scheduled = scheduler.schedule(() -> {
            if (request.text.contains("[[fail]]")) {
                callback.onResult(TextActionProviderResult.failure(TextActionProviderError.FAILED));
                return;
            }
            if (request.text.contains("[[empty]]")) {
                callback.onResult(TextActionProviderResult.success(" "));
                return;
            }
            if (request.text.contains("[[malformed]]")) {
                callback.onResult(null);
                return;
            }
            callback.onResult(TextActionProviderResult.success(transform(request)));
        }, delayMs);
        return scheduled::cancel;
    }

    private static String transform(TextActionProviderRequest request) {
        String corrected = TextActionEngine.correct(request.text);
        switch (request.action) {
            case CORRECT:
                return corrected;
            case POLISH:
                return "Polished · " + corrected;
            case SHORTER:
                return "Short · " + collapseWhitespace(request.text);
            case POLITE:
                return "Please · " + corrected;
            case TRANSLATE:
                return "[" + request.targetLanguage + "] " + request.text;
            default:
                return corrected;
        }
    }

    private static String collapseWhitespace(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }
}
