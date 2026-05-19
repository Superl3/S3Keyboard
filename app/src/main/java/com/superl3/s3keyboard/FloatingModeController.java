package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;

/**
 * Controls floating keyboard mode where the keyboard can be dragged freely on screen.
 *
 * <p>When enabled, a drag handle bar appears above the keyboard.
 * Dragging this handle repositions the entire keyboard within the screen bounds.
 * Double-tapping the handle resets to the default bottom-docked position.
 *
 * <p>Position is persisted across sessions when floating mode is active.
 */
final class FloatingModeController {
    private static final String PREF_NAME = "keyboard_preferences";
    private static final String KEY_ENABLED = KeyboardPreferences.FLOATING_MODE_ENABLED;
    private static final String KEY_OFFSET_X = "floating_offset_x";
    private static final String KEY_OFFSET_Y = "floating_offset_y";
    private static final long DOUBLE_TAP_TIMEOUT_MS = 300;

    private final SharedPreferences preferences;
    private boolean enabled;
    private int offsetX;
    private int offsetY;

    // Drag state
    private float dragStartX;
    private float dragStartY;
    private int dragStartOffsetX;
    private int dragStartOffsetY;
    private boolean dragging;

    // Double-tap detection
    private long lastTapTime;

    interface OnPositionChangedListener {
        void onPositionChanged(int offsetX, int offsetY);
        void onFloatingModeChanged(boolean enabled);
    }

    private OnPositionChangedListener listener;

    FloatingModeController(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        enabled = preferences.getBoolean(KEY_ENABLED, false);
        offsetX = preferences.getInt(KEY_OFFSET_X, 0);
        offsetY = preferences.getInt(KEY_OFFSET_Y, 0);
    }

    boolean isEnabled() {
        enabled = preferences.getBoolean(KEY_ENABLED, false);
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (!enabled) {
            resetPosition();
        }
        if (listener != null) {
            listener.onFloatingModeChanged(enabled);
        }
    }

    int getOffsetX() {
        return offsetX;
    }

    int getOffsetY() {
        return offsetY;
    }

    void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Handle touch events on the drag handle.
     * Returns true if the event was consumed.
     */
    boolean onHandleTouch(View handle, MotionEvent event) {
        if (!enabled) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragStartX = event.getRawX();
                dragStartY = event.getRawY();
                dragStartOffsetX = offsetX;
                dragStartOffsetY = offsetY;
                dragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - dragStartX;
                float dy = event.getRawY() - dragStartY;
                if (!dragging && Math.hypot(dx, dy) > 10) {
                    dragging = true;
                }
                if (dragging) {
                    offsetX = dragStartOffsetX + Math.round(dx);
                    offsetY = dragStartOffsetY + Math.round(dy);
                    if (listener != null) {
                        listener.onPositionChanged(offsetX, offsetY);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (dragging) {
                    savePosition();
                } else {
                    long now = System.currentTimeMillis();
                    if (now - lastTapTime < DOUBLE_TAP_TIMEOUT_MS) {
                        resetPosition();
                        lastTapTime = 0;
                    } else {
                        lastTapTime = now;
                    }
                }
                dragging = false;
                return true;

            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                offsetX = dragStartOffsetX;
                offsetY = dragStartOffsetY;
                if (listener != null) {
                    listener.onPositionChanged(offsetX, offsetY);
                }
                return true;
        }
        return false;
    }

    private void resetPosition() {
        offsetX = 0;
        offsetY = 0;
        savePosition();
        if (listener != null) {
            listener.onPositionChanged(offsetX, offsetY);
        }
    }

    private void savePosition() {
        preferences.edit()
                .putInt(KEY_OFFSET_X, offsetX)
                .putInt(KEY_OFFSET_Y, offsetY)
                .apply();
    }
}
