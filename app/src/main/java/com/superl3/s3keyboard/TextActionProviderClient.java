package com.superl3.s3keyboard;

final class TextActionProviderClient {
    interface Listener {
        void onComplete(TextActionProviderResult result);
    }

    final class Operation {
        private final Listener listener;
        private boolean done;
        private TextActionProvider.RequestHandle providerHandle;
        private TextActionTaskScheduler.CancelHandle timeoutHandle;

        private Operation(Listener listener) {
            this.listener = listener;
        }

        void cancel() {
            complete(TextActionProviderResult.failure(TextActionProviderError.CANCELLED), true);
        }

        boolean isDone() {
            synchronized (this) {
                return done;
            }
        }

        private void begin(TextActionProviderRequest request) {
            if (request == null) {
                complete(TextActionProviderResult.failure(TextActionProviderError.INVALID_REQUEST), false);
                return;
            }
            if (provider == null || !provider.isAvailable()) {
                complete(TextActionProviderResult.failure(TextActionProviderError.UNAVAILABLE), false);
                return;
            }
            timeoutHandle = scheduler.schedule(
                    () -> complete(TextActionProviderResult.failure(TextActionProviderError.TIMEOUT), true),
                    timeoutMs);
            try {
                TextActionProvider.RequestHandle handle = provider.request(
                        request,
                        result -> complete(validateResult(result), false));
                synchronized (this) {
                    providerHandle = handle;
                    if (done && providerHandle != null) {
                        providerHandle.cancel();
                    }
                }
                if (handle == null && !isDone()) {
                    complete(TextActionProviderResult.failure(TextActionProviderError.FAILED), false);
                }
            } catch (RuntimeException exception) {
                complete(TextActionProviderResult.failure(TextActionProviderError.FAILED), true);
            }
        }

        private void complete(TextActionProviderResult result, boolean cancelProvider) {
            TextActionTaskScheduler.CancelHandle timeout;
            TextActionProvider.RequestHandle requestHandle;
            synchronized (this) {
                if (done) {
                    return;
                }
                done = true;
                timeout = timeoutHandle;
                requestHandle = providerHandle;
            }
            if (timeout != null) {
                timeout.cancel();
            }
            if (cancelProvider && requestHandle != null) {
                requestHandle.cancel();
            }
            listener.onComplete(result == null
                    ? TextActionProviderResult.failure(TextActionProviderError.MALFORMED_RESULT)
                    : result);
        }
    }

    private final TextActionProvider provider;
    private final TextActionTaskScheduler scheduler;
    private final long timeoutMs;

    TextActionProviderClient(
            TextActionProvider provider,
            TextActionTaskScheduler scheduler,
            long timeoutMs) {
        this.provider = provider;
        this.scheduler = scheduler;
        this.timeoutMs = Math.max(1L, timeoutMs);
    }

    Operation start(TextActionProviderRequest request, Listener listener) {
        Operation operation = new Operation(listener);
        operation.begin(request);
        return operation;
    }

    private static TextActionProviderResult validateResult(TextActionProviderResult result) {
        if (result == null) {
            return TextActionProviderResult.failure(TextActionProviderError.MALFORMED_RESULT);
        }
        if (!result.succeeded()) {
            return result;
        }
        if (result.text == null) {
            return TextActionProviderResult.failure(TextActionProviderError.MALFORMED_RESULT);
        }
        if (result.text.trim().isEmpty()) {
            return TextActionProviderResult.failure(TextActionProviderError.EMPTY_RESULT);
        }
        return result;
    }
}
