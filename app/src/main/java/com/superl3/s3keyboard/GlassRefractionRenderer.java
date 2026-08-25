package com.superl3.s3keyboard;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.Build;
import android.util.Log;
import android.view.View;

/** GPU-only, allocation-free-after-warmup refraction over the cached accessibility source. */
final class GlassRefractionRenderer {
    private static final String TAG = "S3KeyboardGlass";
    private static final String SHADER = """
            uniform shader source;
            uniform float2 focusCenter;
            uniform float2 focusSize;
            uniform float strength;
            half4 main(float2 p) {
                float2 safeFocusSize = max(focusSize, float2(1.0));
                float2 center = (p - focusCenter) / safeFocusSize;
                float edge = smoothstep(0.16, 0.72, length(center));
                float wave = sin((p.x * 0.018) + (p.y * 0.011)) * strength * 0.12;
                float2 offset = center * edge * strength + float2(wave, -wave * 0.55);
                half4 base = source.eval(p + offset);
                half red = source.eval(p + offset + float2(0.9, 0.0)).r;
                half blue = source.eval(p + offset - float2(0.9, 0.0)).b;
                return half4(red, base.g, blue, base.a);
            }
            """;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix sourceMatrix = new Matrix();
    private final int[] viewLocation = new int[2];
    private RuntimeShader runtimeShader;
    private BitmapShader bitmapShader;
    private long sourceGeneration = Long.MIN_VALUE;
    private int lastScreenX = Integer.MIN_VALUE;
    private int lastScreenY = Integer.MIN_VALUE;

    boolean draw(Canvas canvas, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false;
        }
        GlassBackdropSourceStore.Frame frame = GlassBackdropSourceStore.currentFrame();
        if (frame == null || frame.bitmap == null || frame.bitmap.isRecycled()) {
            return false;
        }
        view.getLocationOnScreen(viewLocation);
        if (!prepare(frame, viewLocation[0], viewLocation[1])) {
            return false;
        }
        setFocus(
                view.getWidth() * 0.5f,
                view.getHeight() * 0.5f,
                view.getWidth(),
                view.getHeight(),
                Math.min(10f, Math.max(3f, view.getHeight() * 0.018f)));
        canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), paint);
        return true;
    }

    boolean drawKey(Canvas canvas, View view, RectF bounds, float cornerRadius, float pressProgress) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || bounds == null
                || bounds.width() <= 0f
                || bounds.height() <= 0f) {
            return false;
        }
        GlassBackdropSourceStore.Frame frame = GlassBackdropSourceStore.currentFrame();
        if (frame == null || frame.bitmap == null || frame.bitmap.isRecycled()) {
            return false;
        }
        view.getLocationOnScreen(viewLocation);
        if (!prepare(frame, viewLocation[0], viewLocation[1])) {
            return false;
        }
        float keyStrength = Math.min(
                12f,
                Math.max(3.2f, Math.min(bounds.width(), bounds.height()) * 0.11f));
        keyStrength *= 1f - 0.18f * clamp01(pressProgress);
        setFocus(
                bounds.centerX(),
                bounds.centerY(),
                bounds.width(),
                bounds.height(),
                keyStrength);
        if (cornerRadius <= 0f) {
            canvas.drawRect(bounds, paint);
        } else {
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint);
        }
        return true;
    }

    void clear() {
        bitmapShader = null;
        runtimeShader = null;
        paint.setShader(null);
        sourceGeneration = Long.MIN_VALUE;
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private boolean prepare(
            GlassBackdropSourceStore.Frame frame,
            int screenX,
            int screenY) {
        try {
            if (runtimeShader == null) {
                runtimeShader = new RuntimeShader(SHADER);
                paint.setShader(runtimeShader);
            }
            if (sourceGeneration != frame.generation) {
                bitmapShader = new BitmapShader(
                        frame.bitmap,
                        Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP);
                runtimeShader.setInputShader("source", bitmapShader);
                sourceGeneration = frame.generation;
                lastScreenX = Integer.MIN_VALUE;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "renderer source generation=" + frame.generation);
                }
            }
            if (screenX != lastScreenX || screenY != lastScreenY) {
                sourceMatrix.reset();
                sourceMatrix.setScale(
                        frame.displayWidth / (float) frame.bitmap.getWidth(),
                        frame.displayHeight / (float) frame.bitmap.getHeight());
                sourceMatrix.postTranslate(-screenX, -screenY);
                bitmapShader.setLocalMatrix(sourceMatrix);
                lastScreenX = screenX;
                lastScreenY = screenY;
            }
            return true;
        } catch (RuntimeException unsupportedShader) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "refraction shader unavailable", unsupportedShader);
            }
            clear();
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private void setFocus(float centerX, float centerY, float width, float height, float strength) {
        runtimeShader.setFloatUniform("focusCenter", centerX, centerY);
        runtimeShader.setFloatUniform("focusSize", Math.max(1f, width), Math.max(1f, height));
        runtimeShader.setFloatUniform("strength", strength);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
