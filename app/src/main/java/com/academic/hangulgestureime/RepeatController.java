package com.academic.hangulgestureime;

import android.view.View;

final class RepeatController {
    interface Callback {
        void onRepeat(String value);
    }

    private final View host;
    private final Callback callback;
    private String activeValue;
    private int intervalMs;
    private boolean hasFired;

    private final Runnable repeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (activeValue == null) {
                return;
            }
            hasFired = true;
            callback.onRepeat(activeValue);
            host.postDelayed(this, intervalMs);
        }
    };

    RepeatController(View host, Callback callback) {
        this.host = host;
        this.callback = callback;
    }

    void start(String value, int startDelayMs, int intervalMs) {
        start(value, startDelayMs, intervalMs, true);
    }

    void start(String value, int startDelayMs, int intervalMs, boolean fireImmediately) {
        stop();
        activeValue = value;
        this.intervalMs = intervalMs;
        if (fireImmediately) {
            hasFired = true;
            callback.onRepeat(activeValue);
        }
        host.postDelayed(repeatRunnable, startDelayMs);
    }

    void stop() {
        activeValue = null;
        hasFired = false;
        host.removeCallbacks(repeatRunnable);
    }

    boolean isRepeating() {
        return activeValue != null;
    }

    boolean hasFired() {
        return hasFired;
    }
}
