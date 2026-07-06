package com.superl3.s3keyboard;

import android.view.View;

import java.util.function.Consumer;

final class RepeatController implements Runnable {
    interface Scheduler {
        void postDelayed(Runnable runnable, int delayMs);

        void removeCallbacks(Runnable runnable);
    }

    private final Scheduler scheduler;
    private final Consumer<String> callback;
    private final Object defaultOwner = new Object();
    private Object activeOwner;
    private String activeValue;
    private int intervalMs;
    private boolean hasFired;

    RepeatController(View host, Consumer<String> callback) {
        this.scheduler = new ViewScheduler(host);
        this.callback = RuntimeDefaults.stringConsumer(callback);
    }

    RepeatController(Scheduler scheduler, Consumer<String> callback) {
        this.scheduler = scheduler;
        this.callback = RuntimeDefaults.stringConsumer(callback);
    }

    @Override
    public void run() {
        String value = activeValue;
        Object owner = activeOwner;
        if (value == null) {
            return;
        }
        hasFired = true;
        callback.accept(value);
        if (activeValue != null && activeOwner == owner) {
            scheduler.postDelayed(this, intervalMs);
        }
    }

    void start(String value, int startDelayMs, int intervalMs) {
        start(defaultOwner, value, startDelayMs, intervalMs, true);
    }

    void start(String value, int startDelayMs, int intervalMs, boolean fireImmediately) {
        start(defaultOwner, value, startDelayMs, intervalMs, fireImmediately);
    }

    void start(Object owner, String value, int startDelayMs, int intervalMs, boolean fireImmediately) {
        Object normalizedOwner = normalizeOwner(owner);
        stop();
        activeOwner = normalizedOwner;
        activeValue = value;
        this.intervalMs = intervalMs;
        if (fireImmediately) {
            hasFired = true;
            callback.accept(value);
            if (activeValue == null || activeOwner != normalizedOwner) {
                return;
            }
        }
        scheduler.postDelayed(this, startDelayMs);
    }

    void stop() {
        activeOwner = null;
        activeValue = null;
        hasFired = false;
        scheduler.removeCallbacks(this);
    }

    void stop(Object owner) {
        Object normalizedOwner = normalizeOwner(owner);
        if (activeOwner == normalizedOwner) {
            stop();
        }
    }

    boolean isRepeating() {
        return activeValue != null;
    }

    boolean hasFired() {
        return hasFired;
    }

    boolean hasFired(Object owner) {
        Object normalizedOwner = normalizeOwner(owner);
        return activeOwner == normalizedOwner && hasFired;
    }

    private Object normalizeOwner(Object owner) {
        return owner == null ? defaultOwner : owner;
    }

    private static final class ViewScheduler implements Scheduler {
        private final View host;

        ViewScheduler(View host) {
            this.host = host;
        }

        @Override
        public void postDelayed(Runnable runnable, int delayMs) {
            host.postDelayed(runnable, delayMs);
        }

        @Override
        public void removeCallbacks(Runnable runnable) {
            host.removeCallbacks(runnable);
        }
    }
}
