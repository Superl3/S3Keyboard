package com.superl3.s3keyboard;

import android.annotation.TargetApi;
import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.hardware.HardwareBuffer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.List;

/**
 * Opt-in source provider for Glass. It captures only the active application window, never writes
 * pixels to disk, and publishes a single aggressively downscaled frame to the IME process.
 */
public final class GlassCaptureAccessibilityService extends AccessibilityService {
    private static final String TAG = "S3KeyboardGlass";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable captureRunnable = this::captureIfAllowed;
    private int sourceWindowId = -1;
    private long lastCaptureStartedMs = Long.MIN_VALUE / 2;
    private boolean captureInFlight;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        GlassBackdropSourceStore.setCaptureRequester(this::requestCapture);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "accessibility source connected");
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }
        CharSequence packageName = event.getPackageName();
        if (packageName == null) {
            return;
        }
        if (event.getWindowId() >= 0) {
            sourceWindowId = event.getWindowId();
        }
        if (GlassBackdropSourceStore.isConsumerActive()) {
            if (sourceWindowId < 0) {
                sourceWindowId = resolveActiveApplicationWindowId(sourceWindowId);
            }
            if (BuildConfig.DEBUG
                    && (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    || event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED)) {
                Log.d(TAG, "source window=" + sourceWindowId
                        + " event=" + event.getEventType());
            }
            requestCapture();
        }
    }

    @Override
    public void onInterrupt() {
        cancelAndClear();
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        cancelAndClear();
        GlassBackdropSourceStore.setCaptureRequester(null);
        return super.onUnbind(intent);
    }

    private void requestCapture() {
        mainHandler.removeCallbacks(captureRunnable);
        long elapsed = SystemClock.uptimeMillis() - lastCaptureStartedMs;
        long throttleDelay = Math.max(0L, GlassCapturePolicy.MIN_CAPTURE_INTERVAL_MS - elapsed);
        mainHandler.postDelayed(
                captureRunnable,
                Math.max(GlassCapturePolicy.EVENT_DEBOUNCE_MS, throttleDelay));
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void captureIfAllowed() {
        long now = SystemClock.uptimeMillis();
        sourceWindowId = resolveActiveApplicationWindowId(sourceWindowId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                || !GlassCapturePolicy.shouldCapture(
                        GlassBackdropSourceStore.isConsumerActive(),
                        sourceWindowId,
                        captureInFlight,
                        now,
                        lastCaptureStartedMs)) {
            return;
        }
        captureInFlight = true;
        lastCaptureStartedMs = now;
        takeScreenshotOfWindow(
                sourceWindowId,
                getMainExecutor(),
                new TakeScreenshotCallback() {
                    @Override
                    public void onSuccess(ScreenshotResult screenshot) {
                        captureInFlight = false;
                        publishDownscaled(screenshot);
                    }

                    @Override
                    public void onFailure(int errorCode) {
                        captureInFlight = false;
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "capture failed code=" + errorCode);
                        }
                    }
                });
    }

    private int resolveActiveApplicationWindowId(int fallback) {
        List<AccessibilityWindowInfo> windows = getWindows();
        int firstApplication = -1;
        for (AccessibilityWindowInfo window : windows) {
            if (window.getType() != AccessibilityWindowInfo.TYPE_APPLICATION) {
                continue;
            }
            if (firstApplication < 0) {
                firstApplication = window.getId();
            }
            if (window.isActive() || window.isFocused()) {
                return window.getId();
            }
        }
        return firstApplication >= 0 ? firstApplication : fallback;
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private void publishDownscaled(ScreenshotResult screenshot) {
        HardwareBuffer buffer = screenshot.getHardwareBuffer();
        ColorSpace colorSpace = screenshot.getColorSpace();
        Bitmap hardwareBitmap = null;
        try {
            hardwareBitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace);
            if (hardwareBitmap == null) {
                return;
            }
            int sourceWidth = hardwareBitmap.getWidth();
            int sourceHeight = hardwareBitmap.getHeight();
            float scale = GlassCapturePolicy.downscaleFor(sourceWidth, sourceHeight);
            if (scale <= 0f) {
                return;
            }
            int width = Math.max(1, Math.round(sourceWidth * scale));
            int height = Math.max(1, Math.round(sourceHeight * scale));
            Bitmap downscaled = Bitmap.createScaledBitmap(hardwareBitmap, width, height, true)
                    .copy(Bitmap.Config.ARGB_8888, false);
            if (isSecureBlackFrame(downscaled)) {
                downscaled.recycle();
                GlassBackdropSourceStore.clear();
                return;
            }
            GlassBackdropSourceStore.publish(downscaled, sourceWidth, sourceHeight);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "source " + sourceWidth + "x" + sourceHeight
                        + " cached " + width + "x" + height);
            }
        } finally {
            buffer.close();
        }
    }

    static boolean isSecureBlackFrame(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            return true;
        }
        int dark = 0;
        int samples = 0;
        for (int y = 1; y <= 5; y++) {
            int py = Math.min(bitmap.getHeight() - 1, y * bitmap.getHeight() / 6);
            for (int x = 1; x <= 5; x++) {
                int px = Math.min(bitmap.getWidth() - 1, x * bitmap.getWidth() / 6);
                int color = bitmap.getPixel(px, py);
                if (android.graphics.Color.red(color) <= 3
                        && android.graphics.Color.green(color) <= 3
                        && android.graphics.Color.blue(color) <= 3) {
                    dark++;
                }
                samples++;
            }
        }
        return dark == samples;
    }

    private void cancelAndClear() {
        mainHandler.removeCallbacks(captureRunnable);
        captureInFlight = false;
        sourceWindowId = -1;
        GlassBackdropSourceStore.clear();
    }
}
