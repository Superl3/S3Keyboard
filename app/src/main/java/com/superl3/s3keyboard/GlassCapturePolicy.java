package com.superl3.s3keyboard;

final class GlassCapturePolicy {
    static final long MIN_CAPTURE_INTERVAL_MS = 360L;
    static final long EVENT_DEBOUNCE_MS = 160L;
    static final int MAX_SOURCE_PIXELS = 360_000;

    private GlassCapturePolicy() {
    }

    static boolean shouldCapture(
            boolean consumerActive,
            int sourceWindowId,
            boolean captureInFlight,
            long nowMs,
            long lastCaptureStartedMs) {
        return consumerActive
                && sourceWindowId >= 0
                && !captureInFlight
                && nowMs - lastCaptureStartedMs >= MIN_CAPTURE_INTERVAL_MS;
    }

    static float downscaleFor(int width, int height) {
        if (width <= 0 || height <= 0) {
            return 0f;
        }
        long pixels = (long) width * height;
        if (pixels <= MAX_SOURCE_PIXELS) {
            return 1f;
        }
        return (float) Math.sqrt((double) MAX_SOURCE_PIXELS / pixels);
    }
}
