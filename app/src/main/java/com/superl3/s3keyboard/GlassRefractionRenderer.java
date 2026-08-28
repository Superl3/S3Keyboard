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
            uniform float cornerRadius;
            uniform float strength;
            uniform float blurRadius;
            uniform float materialGloss;
            uniform float surfaceTexture;
            uniform float4 materialTint;
            uniform float sourceCenterMix;
            uniform float sourceEdgeMix;

            half4 blurredSample(float2 p) {
                float radius = max(0.0, blurRadius);
                float diagonal = radius * 0.70710678;
                half4 color = source.eval(p) * 0.24;
                color += source.eval(p + float2(radius, 0.0)) * 0.11;
                color += source.eval(p - float2(radius, 0.0)) * 0.11;
                color += source.eval(p + float2(0.0, radius)) * 0.11;
                color += source.eval(p - float2(0.0, radius)) * 0.11;
                color += source.eval(p + float2(diagonal, diagonal)) * 0.08;
                color += source.eval(p + float2(diagonal, -diagonal)) * 0.08;
                color += source.eval(p + float2(-diagonal, diagonal)) * 0.08;
                color += source.eval(p - float2(diagonal, diagonal)) * 0.08;
                return color;
            }

            half4 main(float2 p) {
                float2 halfSize = max(focusSize * 0.5, float2(1.0));
                float2 local = p - focusCenter;
                float radius = clamp(cornerRadius, 0.0, min(halfSize.x, halfSize.y) - 1.0);
                float2 q = abs(local) - halfSize + float2(radius);
                float signedDistance = length(max(q, float2(0.0)))
                        + min(max(q.x, q.y), 0.0) - radius;
                float edgeWidth = max(5.0, min(halfSize.x, halfSize.y) * 0.23);
                float edge = smoothstep(-edgeWidth, 0.0, signedDistance);

                float2 normalized = clamp(local / halfSize, float2(-1.0), float2(1.0));
                float2 axis = abs(normalized);
                float2 shaped = float2(
                        axis.x * axis.x * axis.x * axis.x,
                        axis.y * axis.y * axis.y * axis.y);
                float2 direction = sign(local) * (shaped + axis * 0.035);
                float directionLength = max(length(direction), 0.001);
                direction /= directionLength;
                float2 tangent = float2(-direction.y, direction.x);

                float rippleA = sin(p.x * 0.052 + p.y * 0.027);
                float rippleB = sin(p.x * 0.019 - p.y * 0.071 + 1.7);
                float ripple = rippleA * 0.62 + rippleB * 0.38;
                // Keep the center calm while making the lens thickness readable at the rim.
                // Excess blur used to erase this displacement before the tint pass was drawn.
                float bend = strength * (0.05 + 1.68 * edge * edge);
                float textureBend = surfaceTexture * strength * ripple * (0.012 + edge * 0.065);
                float2 offset = direction * bend + tangent * textureBend;

                half4 color = blurredSample(p + offset);
                float sourceMix = mix(sourceCenterMix, sourceEdgeMix, edge * edge);
                color.rgb = mix(half3(materialTint.rgb), color.rgb, half(sourceMix));
                color.a = 1.0;
                float x01 = normalized.x * 0.5 + 0.5;
                float y01 = normalized.y * 0.5 + 0.5;
                float topGloss = 1.0 - smoothstep(0.05, 0.24, y01);
                float diagonalGloss = 1.0 - smoothstep(0.0, 0.12, abs(x01 * 0.46 + y01 - 0.30));
                float facingLight = clamp(0.52 - direction.y * 0.40 - direction.x * 0.12, 0.0, 1.0);
                float rimGloss = edge * edge * facingLight;
                float gloss = materialGloss * (0.045 * topGloss + 0.028 * diagonalGloss + 0.080 * rimGloss);
                color.rgb = mix(color.rgb, half3(1.0), half(gloss));

                float flatDark = smoothstep(0.42, 0.70, y01) * (1.0 - edge * 0.72);
                color.rgb = mix(color.rgb, half3(0.0), half(materialGloss * flatDark * 0.070));
                float microSheen = ripple * surfaceTexture * (1.0 - edge * 0.55) * 0.006;
                color.rgb += half3(microSheen);
                return color;
            }
            """;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix sourceMatrix = new Matrix();
    private final RectF sourceBounds = new RectF();
    private final RectF localDisplayBounds = new RectF();
    private final int[] viewLocation = new int[2];
    private RuntimeShader runtimeShader;
    private BitmapShader bitmapShader;
    private long sourceGeneration = Long.MIN_VALUE;
    private int lastScreenX = Integer.MIN_VALUE;
    private int lastScreenY = Integer.MIN_VALUE;

    boolean draw(Canvas canvas, View view, float requestedBlurRadiusPx) {
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
                0f,
                Math.min(11f, Math.max(4f, view.getHeight() * 0.012f)),
                Math.min(24f, Math.max(0f, requestedBlurRadiusPx * 0.42f)),
                0.22f,
                0.16f,
                0xFF000000,
                1f,
                1f);
        canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), paint);
        return true;
    }

    boolean drawKey(
            Canvas canvas,
            View view,
            RectF bounds,
            float cornerRadius,
            float pressProgress,
            float requestedBlurRadiusPx,
            float materialGloss,
            float surfaceTexture,
            int materialTint,
            float sourceCenterMix,
            float sourceEdgeMix) {
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
        // onDraw() always prepares the same view in the panel pass first. Avoid querying the
        // window location again for every key unless this is the first live-source draw.
        if (sourceGeneration != frame.generation || bitmapShader == null) {
            view.getLocationOnScreen(viewLocation);
            if (!prepare(frame, viewLocation[0], viewLocation[1])) {
                return false;
            }
        }
        float keyStrength = Math.min(
                18f,
                Math.max(7f, Math.min(bounds.width(), bounds.height()) * 0.12f));
        keyStrength *= 1f - 0.16f * clamp01(pressProgress);
        setFocus(
                bounds.centerX(),
                bounds.centerY(),
                bounds.width(),
                bounds.height(),
                cornerRadius,
                keyStrength,
                Math.min(10f, Math.max(0f, requestedBlurRadiusPx * 0.16f)),
                clamp01(materialGloss),
                clamp01(surfaceTexture),
                materialTint,
                clamp01(sourceCenterMix),
                clamp01(sourceEdgeMix));
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
                sourceBounds.set(0f, 0f, frame.bitmap.getWidth(), frame.bitmap.getHeight());
                localDisplayBounds.set(
                        -screenX,
                        -screenY,
                        frame.displayWidth - screenX,
                        frame.displayHeight - screenY);
                sourceMatrix.setRectToRect(
                        sourceBounds,
                        localDisplayBounds,
                        Matrix.ScaleToFit.FILL);
                bitmapShader.setLocalMatrix(sourceMatrix);
                lastScreenX = screenX;
                lastScreenY = screenY;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "renderer source matrix screen=" + screenX + "," + screenY
                            + " display=" + frame.displayWidth + "x" + frame.displayHeight);
                }
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
    private void setFocus(
            float centerX,
            float centerY,
            float width,
            float height,
            float cornerRadius,
            float strength,
            float blurRadius,
            float materialGloss,
            float surfaceTexture,
            int materialTint,
            float sourceCenterMix,
            float sourceEdgeMix) {
        runtimeShader.setFloatUniform("focusCenter", centerX, centerY);
        runtimeShader.setFloatUniform("focusSize", Math.max(1f, width), Math.max(1f, height));
        runtimeShader.setFloatUniform("cornerRadius", Math.max(0f, cornerRadius));
        runtimeShader.setFloatUniform("strength", strength);
        runtimeShader.setFloatUniform("blurRadius", Math.max(0f, blurRadius));
        runtimeShader.setFloatUniform("materialGloss", clamp01(materialGloss));
        runtimeShader.setFloatUniform("surfaceTexture", clamp01(surfaceTexture));
        runtimeShader.setFloatUniform(
                "materialTint",
                android.graphics.Color.red(materialTint) / 255f,
                android.graphics.Color.green(materialTint) / 255f,
                android.graphics.Color.blue(materialTint) / 255f,
                1f);
        runtimeShader.setFloatUniform("sourceCenterMix", clamp01(sourceCenterMix));
        runtimeShader.setFloatUniform("sourceEdgeMix", clamp01(sourceEdgeMix));
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
