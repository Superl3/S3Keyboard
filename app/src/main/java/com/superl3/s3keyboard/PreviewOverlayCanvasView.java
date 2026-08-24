package com.superl3.s3keyboard;

import static com.superl3.s3keyboard.KeyboardColorMath.withAlpha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** Draws every preview in one display list instead of maintaining a TextView per bubble. */
final class PreviewOverlayCanvasView extends View {
    private static final float SHADOW_OFFSET_DP = 1.5f;
    private static final float SHADOW_ALPHA = 0.18f;
    private static final float HIGHLIGHT_ALPHA = 0.10f;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics textMetrics = new Paint.FontMetrics();
    private final RectF body = new RectF();
    private final RectF effectBounds = new RectF();
    private final Path tailPath = new Path();
    private final List<RenderState> renderStates = new ArrayList<>(4);

    private int visibleCount;
    private int topPadPx;

    PreviewOverlayCanvasView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        strokePaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    void setSettings(KeyboardSettings settings) {
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        textPaint.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                getContext(),
                safeSettings.fontFamily,
                safeSettings.primaryTextBold,
                safeSettings.primaryTextItalic));
        invalidate();
    }

    void setSpecs(List<PreviewOverlaySpec> specs, int topPadPx) {
        this.topPadPx = topPadPx;
        visibleCount = specs.size();
        ensureCapacity(visibleCount);
        for (int i = 0; i < visibleCount; i++) {
            renderStates.get(i).copyFrom(specs.get(i));
        }
        invalidate();
    }

    void clearSpecs() {
        if (visibleCount == 0) {
            return;
        }
        visibleCount = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < visibleCount; i++) {
            drawPreview(canvas, renderStates.get(i));
        }
    }

    private void drawPreview(Canvas canvas, RenderState state) {
        float left = state.x;
        float top = state.y + topPadPx;
        float right = left + state.width;
        float bottom = top + state.height;
        float pivotX = (left + right) * 0.5f;
        float pivotY = bottom;
        float corner = Math.min(
                state.cornerRadiusPx,
                Math.min(state.width, state.height) * 0.5f);
        int saveCount = canvas.save();
        canvas.scale(state.scaleX, state.scaleY, pivotX, pivotY);

        body.set(left, top, right, bottom);
        int alpha = Math.round(255f * state.alpha);
        drawShadow(canvas, corner, alpha);
        drawBody(canvas, state, corner, alpha);
        if (state.angularBubble) {
            drawTail(canvas, state, alpha);
        }
        drawText(canvas, state, alpha);
        canvas.restoreToCount(saveCount);
    }

    private void drawShadow(Canvas canvas, float corner, int alpha) {
        float offset = dp(SHADOW_OFFSET_DP);
        effectBounds.set(body);
        effectBounds.offset(0f, offset);
        effectPaint.setStyle(Paint.Style.FILL);
        effectPaint.setColor(withAlpha(Color.BLACK, Math.round(alpha * SHADOW_ALPHA)));
        canvas.drawRoundRect(effectBounds, corner, corner, effectPaint);
    }

    private void drawBody(Canvas canvas, RenderState state, float corner, int alpha) {
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(withAlpha(state.backgroundColor, alpha));
        canvas.drawRoundRect(body, corner, corner, fillPaint);

        float highlightHeight = Math.max(dp(1f), body.height() * 0.08f);
        effectBounds.set(
                body.left + corner * 0.45f,
                body.top + dp(1f),
                body.right - corner * 0.45f,
                body.top + highlightHeight);
        effectPaint.setStyle(Paint.Style.FILL);
        effectPaint.setColor(withAlpha(Color.WHITE, Math.round(alpha * HIGHLIGHT_ALPHA)));
        canvas.drawRoundRect(effectBounds, highlightHeight, highlightHeight, effectPaint);

        if (state.borderWidthPx <= 0) {
            return;
        }
        float inset = state.borderWidthPx * 0.5f;
        effectBounds.set(body);
        effectBounds.inset(inset, inset);
        strokePaint.setStrokeWidth(state.borderWidthPx);
        strokePaint.setColor(withAlpha(state.borderColor, alpha));
        canvas.drawRoundRect(
                effectBounds,
                Math.max(0f, corner - inset),
                Math.max(0f, corner - inset),
                strokePaint);
    }

    private void drawTail(Canvas canvas, RenderState state, int alpha) {
        float halfWidth = body.width() * 0.17f;
        float tailHeight = Math.min(body.height() * 0.28f, dp(18f));
        float centerX = body.centerX();
        tailPath.reset();
        tailPath.moveTo(centerX - halfWidth, body.bottom - dp(1f));
        tailPath.lineTo(centerX, body.bottom + tailHeight);
        tailPath.lineTo(centerX + halfWidth, body.bottom - dp(1f));
        tailPath.close();
        fillPaint.setColor(withAlpha(state.backgroundColor, alpha));
        canvas.drawPath(tailPath, fillPaint);
    }

    private void drawText(Canvas canvas, RenderState state, int alpha) {
        textPaint.setColor(withAlpha(state.textColor, alpha));
        textPaint.setTextSize(state.textSizePx * state.textScale);
        textPaint.getFontMetrics(textMetrics);
        float baseline = body.centerY() - (textMetrics.ascent + textMetrics.descent) * 0.5f;
        canvas.drawText(state.label, body.centerX(), baseline, textPaint);
    }

    private void ensureCapacity(int count) {
        while (renderStates.size() < count) {
            renderStates.add(new RenderState());
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private static final class RenderState {
        String label = "";
        int x;
        int y;
        int width;
        int height;
        float textSizePx;
        int textColor;
        int backgroundColor;
        int borderColor;
        int borderWidthPx;
        int cornerRadiusPx;
        boolean angularBubble;
        float alpha;
        float scaleX;
        float scaleY;
        float textScale;

        void copyFrom(PreviewOverlaySpec spec) {
            label = spec.label;
            x = spec.x;
            y = spec.y;
            width = spec.width;
            height = spec.height;
            textSizePx = spec.textSizePx;
            textColor = spec.textColor;
            backgroundColor = spec.backgroundColor;
            borderColor = spec.borderColor;
            borderWidthPx = spec.borderWidthPx;
            cornerRadiusPx = spec.cornerRadiusPx;
            angularBubble = spec.angularBubble;
            alpha = spec.alpha;
            scaleX = spec.scaleX;
            scaleY = spec.scaleY;
            textScale = spec.textScale;
        }
    }
}
