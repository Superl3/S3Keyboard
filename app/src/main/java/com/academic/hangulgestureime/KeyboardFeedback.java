package com.academic.hangulgestureime;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Queue;

final class KeyboardFeedback {
    private static final int MAX_QUEUE_SIZE = 6;
    private static final long MAX_EVENT_AGE_MS = 220L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Queue<HapticEvent> queue = new ArrayDeque<>();
    private final Vibrator vibrator;
    private boolean enabled;
    private int tickDurationMs = KeyboardPreferences.DEFAULT_HAPTIC_TICK_DURATION_MS;
    private int tickGapMs = KeyboardPreferences.DEFAULT_HAPTIC_TICK_GAP_MS;
    private boolean draining;

    KeyboardFeedback(View view) {
        vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            queue.clear();
            draining = false;
        }
    }

    void reloadPreferences(Context context) {
        tickDurationMs = KeyboardPreferences.loadHapticTickDurationMs(context);
        tickGapMs = KeyboardPreferences.loadHapticTickGapMs(context);
    }

    void tap() {
        enqueue(1.0f);
    }

    void tapHeavy() {
        enqueue(1.4f);
    }

    void tapClick() {
        enqueue(0.6f);
    }

    void tapConfirm() {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        int d = Math.max(1, Math.round(tickDurationMs * 1.2f));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, d, Math.max(1, d / 2), Math.round(d * 0.6f)},
                    -1));
        } else {
            vibrator.vibrate(d * 2L);
        }
    }

    void slideLock() {
        enqueue(0.75f);
    }

    void longPress() {
        enqueue(1.35f);
    }

    private void enqueue(float durationScale) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        if (queue.size() >= MAX_QUEUE_SIZE) {
            queue.poll();
        }
        int duration = Math.max(1, Math.round(tickDurationMs * durationScale));
        queue.offer(new HapticEvent(duration, System.currentTimeMillis()));
        if (!draining) {
            draining = true;
            drain();
        }
    }

    private void drain() {
        HapticEvent next = queue.poll();
        long now = System.currentTimeMillis();
        while (next != null && now - next.createdAtMs > MAX_EVENT_AGE_MS) {
            next = queue.poll();
        }
        if (next == null) {
            draining = false;
            return;
        }

        vibrate(next.durationMs);
        handler.postDelayed(this::drain, next.durationMs + tickGapMs);
    }

    private void vibrate(int durationMs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(durationMs);
        }
    }

    private static final class HapticEvent {
        final int durationMs;
        final long createdAtMs;

        HapticEvent(int durationMs, long createdAtMs) {
            this.durationMs = durationMs;
            this.createdAtMs = createdAtMs;
        }
    }
}
