package com.superl3.s3keyboard;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** Process-local, memory-only handoff between the accessibility capture and IME renderer. */
final class GlassBackdropSourceStore {
    private static final String TAG = "S3KeyboardGlass";
    interface Listener {
        void onGlassBackdropChanged();
    }

    interface CaptureRequester {
        void requestCapture();
    }

    static final class Frame {
        final Bitmap bitmap;
        final int displayWidth;
        final int displayHeight;
        final long generation;

        Frame(Bitmap bitmap, int displayWidth, int displayHeight, long generation) {
            this.bitmap = bitmap;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            this.generation = generation;
        }
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final List<Listener> LISTENERS = new ArrayList<>();
    private static Frame frame;
    private static CaptureRequester captureRequester;
    private static boolean consumerActive;
    private static long generation;

    private GlassBackdropSourceStore() {
    }

    static void addListener(Listener listener) {
        runOnMain(() -> {
            if (listener != null && !LISTENERS.contains(listener)) {
                LISTENERS.add(listener);
            }
        });
    }

    static void removeListener(Listener listener) {
        runOnMain(() -> LISTENERS.remove(listener));
    }

    static Frame currentFrame() {
        return frame;
    }

    static boolean isConsumerActive() {
        return consumerActive;
    }

    static void setConsumerActive(boolean active) {
        runOnMain(() -> {
            if (consumerActive == active) {
                return;
            }
            consumerActive = active;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "consumer active=" + active);
            }
            if (!active) {
                clearOnMain();
            } else if (captureRequester != null) {
                captureRequester.requestCapture();
            }
        });
    }

    static void setCaptureRequester(CaptureRequester requester) {
        runOnMain(() -> {
            captureRequester = requester;
            if (consumerActive && requester != null) {
                requester.requestCapture();
            }
        });
    }

    static void publish(Bitmap bitmap, int displayWidth, int displayHeight) {
        if (bitmap == null) {
            return;
        }
        runOnMain(() -> {
            if (!consumerActive) {
                bitmap.recycle();
                return;
            }
            Bitmap previous = frame == null ? null : frame.bitmap;
            frame = new Frame(bitmap, displayWidth, displayHeight, ++generation);
            notifyListeners();
            if (previous != null && previous != bitmap && !previous.isRecycled()) {
                previous.recycle();
            }
        });
    }

    static void clear() {
        runOnMain(GlassBackdropSourceStore::clearOnMain);
    }

    private static void clearOnMain() {
        Bitmap previous = frame == null ? null : frame.bitmap;
        frame = null;
        generation++;
        notifyListeners();
        if (previous != null && !previous.isRecycled()) {
            previous.recycle();
        }
    }

    private static void notifyListeners() {
        for (Listener listener : new ArrayList<>(LISTENERS)) {
            listener.onGlassBackdropChanged();
        }
    }

    private static void runOnMain(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            MAIN.post(action);
        }
    }
}
