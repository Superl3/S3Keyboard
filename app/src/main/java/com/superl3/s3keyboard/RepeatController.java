package com.superl3.s3keyboard;

import android.view.View;

import java.util.function.Consumer;

final class RepeatController implements Runnable {
    private final View host;
    private final Consumer<String> callback;
    private String activeValue;
    private int intervalMs;
    private boolean hasFired;

    RepeatController(View host, Consumer<String> callback) {
        this.host = host;
        this.callback = RuntimeDefaults.stringConsumer(callback);
    }

    @Override
    public void run() {
        if (activeValue == null) {
            return;
        }
        hasFired = true;
        callback.accept(activeValue);
        host.postDelayed(this, intervalMs);
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
            callback.accept(activeValue);
        }
        host.postDelayed(this, startDelayMs);
    }

    void stop() {
        activeValue = null;
        hasFired = false;
        host.removeCallbacks(this);
    }

    boolean isRepeating() {
        return activeValue != null;
    }

    boolean hasFired() {
        return hasFired;
    }
}
