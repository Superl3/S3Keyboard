package com.superl3.s3keyboard;

import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/** Applies platform background blur only inside the IME window bounds. */
final class ImeWindowBlurController {
    private static final String TAG = "S3KeyboardBlur";

    private ImeWindowBlurController() {
    }

    static boolean apply(
            Window window,
            KeyboardVisualEffects effects,
            boolean allowPanelBlur,
            float density) {
        if (window == null) {
            return false;
        }
        int requestedRadiusDp = effects == null || effects.blurRadiusDp <= 0
                ? 10
                : effects.blurRadiusDp;
        boolean requested = allowPanelBlur
                && effects != null
                && (effects.blurEnabled || effects.glassEnabled)
                && requestedRadiusDp > 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
        boolean enabled = requested;
        boolean platformSupported = false;
        if (enabled) {
            WindowManager windowManager = window.getDecorView().getContext()
                    .getSystemService(WindowManager.class);
            platformSupported = windowManager != null && windowManager.isCrossWindowBlurEnabled();
            enabled = platformSupported;
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (enabled) {
            int radiusPx = radiusPx(requestedRadiusDp, density);
            // FLAG_BLUR_BEHIND affects the entire screen behind the IME. The keyboard
            // material needs the source only within its own back panel instead.
            attributes.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            attributes.setBlurBehindRadius(0);
            window.setAttributes(attributes);
            window.setBackgroundBlurRadius(radiusPx);
            logState(requested, platformSupported, true, radiusPx);
            return radiusPx > 0;
        }

        attributes.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            attributes.setBlurBehindRadius(0);
            window.setBackgroundBlurRadius(0);
        }
        window.setAttributes(attributes);
        logState(requested, platformSupported, false, 0);
        return false;
    }

    private static void logState(
            boolean requested,
            boolean platformSupported,
            boolean applied,
            int radiusPx) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "requested=" + requested
                    + " platformSupported=" + platformSupported
                    + " applied=" + applied
                    + " radiusPx=" + radiusPx);
        }
    }

    static int radiusPx(int radiusDp, float density) {
        if (radiusDp <= 0 || density <= 0f) {
            return 0;
        }
        return Math.max(1, Math.round(radiusDp * density));
    }
}
