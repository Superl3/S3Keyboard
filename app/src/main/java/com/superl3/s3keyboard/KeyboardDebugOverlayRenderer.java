package com.superl3.s3keyboard;

import static com.superl3.s3keyboard.KeyboardColorMath.withAlpha;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.util.List;

final class KeyboardDebugOverlayRenderer {
    private static final int HIT_RECT_COLOR = 0xFFFF5C5C;
    private static final int VISUAL_RECT_COLOR = 0xFF35D07F;
    private static final int ORIGIN_COLOR = 0xFF39C8FF;
    private static final int TOUCH_COLOR = 0xFFFFD84A;
    private static final int TOUCH_OUTLINE_COLOR = 0xFF2B2B2B;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    void draw(
            Canvas canvas,
            List<HangulKeyboardView.KeySlot> keySlots,
            KeyboardSettings settings,
            float density,
            float scaledDensity,
            float renderScale,
            float lastDownX,
            float lastDownY,
            String lastKeyId,
            GestureAction lastAction,
            GestureCandidateScore candidateScore,
            int edgeRailDirection) {
        float stroke = Math.max(1f, dp(1, density, renderScale));
        float radius = Math.max(0f, dp(settings.keyRoundnessDp, density, renderScale));
        String safeLastKeyId = RuntimeDefaults.stringOrDefault(lastKeyId, "");
        for (HangulKeyboardView.KeySlot keySlot : keySlots) {
            boolean recent = keySlot.debugId().equals(safeLastKeyId);
            drawRect(canvas, keySlot.hitBounds(), HIT_RECT_COLOR, stroke, radius, recent ? 118 : 68);
            drawRect(canvas, keySlot.visualBounds(), VISUAL_RECT_COLOR, stroke, radius, recent ? 190 : 112);
            drawOrigin(canvas, keySlot.gestureOriginX, keySlot.gestureOriginY, ORIGIN_COLOR, density, renderScale);
        }
        if (!Float.isNaN(lastDownX) && !Float.isNaN(lastDownY)) {
            drawLastTouch(canvas, lastDownX, lastDownY, stroke, density, renderScale);
        }
        drawLegend(
                canvas,
                density,
                scaledDensity,
                renderScale,
                safeLastKeyId,
                lastAction,
                candidateScore,
                edgeRailDirection);
    }

    private void drawRect(
            Canvas canvas,
            RectF bounds,
            int color,
            float stroke,
            float radius,
            int alpha) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(withAlpha(color, alpha));
        RectF rect = new RectF(bounds);
        rect.inset(stroke / 2f, stroke / 2f);
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawOrigin(
            Canvas canvas,
            float x,
            float y,
            int color,
            float density,
            float renderScale) {
        float arm = Math.max(dp(4, density, renderScale), 5f);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1, density, renderScale)));
        paint.setColor(withAlpha(color, 210));
        canvas.drawLine(x - arm, y, x + arm, y, paint);
        canvas.drawLine(x, y - arm, x, y + arm, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, Math.max(1.5f, dp(1, density, renderScale)), paint);
    }

    private void drawLastTouch(
            Canvas canvas,
            float x,
            float y,
            float stroke,
            float density,
            float renderScale) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(TOUCH_COLOR, 230));
        canvas.drawCircle(x, y, Math.max(dp(3, density, renderScale), 4f), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(withAlpha(TOUCH_OUTLINE_COLOR, 180));
        canvas.drawCircle(x, y, Math.max(dp(6, density, renderScale), 8f), paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawLegend(
            Canvas canvas,
            float density,
            float scaledDensity,
            float renderScale,
            String lastKeyId,
            GestureAction lastAction,
            GestureCandidateScore candidateScore,
            int edgeRailDirection) {
        String info = "debug key bounds  key="
                + (lastKeyId.isEmpty() ? "-" : lastKeyId)
                + "  action="
                + (lastAction == null ? GestureAction.TAP : lastAction).name()
                + "  rail="
                + edgeDirectionText(edgeRailDirection);
        if (candidateScore != null && candidateScore.isPresent()) {
            info += "  candidate="
                    + candidateScore.keyId
                    + "/"
                    + candidateScore.action.name()
                    + " score="
                    + String.format(java.util.Locale.US, "%.2f", candidateScore.score)
                    + (candidateScore.applied ? " applied" : " shadow");
        }
        Typeface previousTypeface = textPaint.getTypeface();
        Paint.Align previousAlign = textPaint.getTextAlign();
        float textSize = sp(10, scaledDensity, renderScale);
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        textPaint.setTextSize(textSize);
        textPaint.setColor(withAlpha(0xFFFFFFFF, 235));
        textPaint.setTextAlign(Paint.Align.LEFT);
        float pad = dp(6, density, renderScale);
        float top = dp(6, density, renderScale);
        float width = textPaint.measureText(info) + pad * 2f;
        float height = textSize + pad * 1.8f;
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(0xCC111111, 190));
        canvas.drawRoundRect(pad, top, pad + width, top + height,
                dp(5, density, renderScale), dp(5, density, renderScale), paint);
        canvas.drawText(info, pad * 2f, top + height - pad * 0.75f, textPaint);
        textPaint.setTypeface(previousTypeface);
        textPaint.setTextAlign(previousAlign);
    }

    private static int dp(float value, float density, float renderScale) {
        return Math.round(value * density * renderScale);
    }

    private static float sp(float value, float scaledDensity, float renderScale) {
        return value * scaledDensity * renderScale;
    }

    private static String edgeDirectionText(int edgeRailDirection) {
        if (edgeRailDirection < 0) {
            return "left-inward";
        }
        if (edgeRailDirection > 0) {
            return "right-inward";
        }
        return "-";
    }
}
