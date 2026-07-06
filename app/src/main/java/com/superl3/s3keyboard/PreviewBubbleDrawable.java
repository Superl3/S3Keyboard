package com.superl3.s3keyboard;

import static com.superl3.s3keyboard.KeyboardColorMath.withAlpha;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

final class PreviewBubbleDrawable extends Drawable {
    private static final int SHADOW_LUMINANCE_THRESHOLD = 96;
    private static final int HIGHLIGHT_LUMINANCE_THRESHOLD = 110;
    private static final int COMMIT_LUMINANCE_THRESHOLD = 118;
    private static final int RIM_LUMINANCE_THRESHOLD = 126;
    private static final float[] TWO_STOP_POSITIONS = { 0f, 1f };
    private static final float[] CENTERED_HORIZONTAL_POSITIONS = { 0f, 0.5f, 1f };
    private static final float[] COMMIT_GLOW_POSITIONS = { 0f, 0.34f, 1f };
    private static final float[] INPUT_IMPACT_POSITIONS = { 0f, 0.58f, 1f };
    private static final float[] INPUT_CORE_POSITIONS = { 0f, 0.54f, 1f };
    private static final float[] COMMIT_HALO_POSITIONS = { 0f, 0.34f, 0.72f, 1f };
    private static final float[] TAIL_FILL_POSITIONS = { 0f, 0.66f, 0.80f, 0.91f, 0.966f, 0.992f, 1f };
    private static final float[] COMMIT_GLOW_DARK_AMOUNTS = { 0.18f, 0.12f, 0f };
    private static final float[] COMMIT_GLOW_LIGHT_AMOUNTS = { 0.14f, 0.10f, 0f };
    private static final float[] INPUT_IMPACT_DARK_AMOUNTS = { 0.06f, 0.16f, 0.08f };
    private static final float[] INPUT_IMPACT_LIGHT_AMOUNTS = { 0.05f, 0.12f, 0.06f };
    private static final float[] COMMIT_HALO_DARK_AMOUNTS = { 0.04f, 0.08f, 0.04f, 0f };
    private static final float[] COMMIT_HALO_LIGHT_AMOUNTS = { 0.03f, 0.06f, 0.03f, 0f };
    private static final float TAIL_TOP_HALF_RATIO = 0.46f;
    private static final float TAIL_MID_HALF_RATIO = 0.28f;
    private static final float TAIL_BOTTOM_HALF_RATIO = 0.14f;
    private static final float TAIL_CONCAVE_LIFT_RATIO = 0.15f;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint effectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tailStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path tailPath = new Path();
    private final Path strokePath = new Path();
    private final RectF body = new RectF();
    private final RectF scratchRect = new RectF();
    private final int backgroundColor;
    private final int backgroundLuminance;
    private final int borderColor;
    private final int cornerPx;
    private final int tailHeightPx;
    private final int borderWidthPx;
    private final float commitGlowAlpha;
    private final float inputImpactAlpha;
    private int alpha = 255;

    PreviewBubbleDrawable(
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            int cornerPx,
            int tailHeightPx,
            float commitGlowAlpha,
            float inputImpactAlpha) {
        this.backgroundColor = backgroundColor;
        this.backgroundLuminance = KeyboardColorMath.perceivedLuminance(backgroundColor);
        this.borderColor = borderColor;
        this.cornerPx = Math.max(0, cornerPx);
        this.tailHeightPx = Math.max(0, tailHeightPx);
        this.borderWidthPx = Math.max(0, borderWidthPx);
        this.commitGlowAlpha = clampUnit(commitGlowAlpha);
        this.inputImpactAlpha = clampUnit(inputImpactAlpha);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(backgroundColor);
        configureFillPaint(effectPaint);
        configureStrokePaint(strokePaint, this.borderWidthPx);
        strokePaint.setColor(borderColor);
        configureFillPaint(tailPaint);
        configureStrokePaint(tailStrokePaint, Math.max(1f, this.borderWidthPx * 0.75f));
        tailStrokePaint.setColor(borderColor);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float inset = borderWidthPx / 2f;
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset - tailHeightPx;
        float corner = Math.min(cornerPx, Math.min(right - left, bottom - top) / 2f);
        float centerX = (left + right) / 2f;
        float width = Math.max(1f, right - left);
        float tailTopHalf = width * TAIL_TOP_HALF_RATIO;

        body.set(left, top, right, bottom);
        drawBody(canvas, corner);
        drawBodyStroke(canvas, left, top, right, bottom, corner, centerX, tailTopHalf);
        drawTail(canvas, bottom, centerX, width);
    }

    private void drawBody(Canvas canvas, float corner) {
        drawCommitHalo(canvas, corner);
        drawBodyShadow(canvas, corner);
        canvas.drawRoundRect(body, corner, corner, fillPaint);
        drawPressedInset(canvas, corner);
        drawBodyHighlight(canvas, corner);
        drawCommitGlow(canvas, corner);
        drawInputCore(canvas, corner);
        drawInputImpact(canvas, corner);
        drawCommitSheen(canvas, corner);
        drawTopRim(canvas, corner);
        drawLowerLip(canvas, corner);
    }

    private void drawBodyStroke(
            Canvas canvas,
            float left,
            float top,
            float right,
            float bottom,
            float corner,
            float centerX,
            float tailTopHalf) {
        if (borderWidthPx <= 0) {
            return;
        }
        float tailJoinLeft = centerX - tailTopHalf;
        float tailJoinRight = centerX + tailTopHalf;
        strokePath.reset();
        strokePath.moveTo(left + corner, top);
        strokePath.lineTo(right - corner, top);
        strokePath.quadTo(right, top, right, top + corner);
        strokePath.lineTo(right, bottom - corner);
        strokePath.quadTo(right, bottom, right - corner, bottom);
        strokePath.lineTo(tailJoinRight, bottom);
        strokePath.moveTo(tailJoinLeft, bottom);
        strokePath.lineTo(left + corner, bottom);
        strokePath.quadTo(left, bottom, left, bottom - corner);
        strokePath.lineTo(left, top + corner);
        strokePath.quadTo(left, top, left + corner, top);
        strokePaint.setAlpha(Math.round(alpha * (0.88f + 0.11f * commitGlowAlpha
                + 0.08f * inputImpactAlpha)));
        canvas.drawPath(strokePath, strokePaint);
    }

    private void drawTail(
            Canvas canvas,
            float tailTopY,
            float centerX,
            float bodyWidth) {
        if (tailHeightPx <= 0) {
            return;
        }
        float tailTopHalf = bodyWidth * TAIL_TOP_HALF_RATIO;
        float tailMidHalf = bodyWidth * TAIL_MID_HALF_RATIO;
        float tailBottomHalf = Math.max(bodyWidth * TAIL_BOTTOM_HALF_RATIO, 1f);
        float tailBottomY = tailTopY + tailHeightPx;
        buildTailPath(tailTopY, tailBottomY, centerX, tailTopHalf, tailMidHalf, tailBottomHalf);
        drawTailShadow(canvas, tailHeightPx);
        drawTailFill(canvas, tailTopY, tailBottomY);
        drawTailContactGlow(canvas, tailBottomY, centerX, tailMidHalf);
        drawTailStroke(canvas);
    }

    private void buildTailPath(
            float tailTopY,
            float tailBottomY,
            float centerX,
            float tailTopHalf,
            float tailMidHalf,
            float tailBottomHalf) {
        float concaveLift = Math.max(1f, tailHeightPx * TAIL_CONCAVE_LIFT_RATIO);
        tailPath.reset();
        tailPath.moveTo(centerX - tailTopHalf, tailTopY - 0.5f);
        tailPath.lineTo(centerX + tailTopHalf, tailTopY - 0.5f);
        tailPath.cubicTo(
                centerX + tailTopHalf * 0.98f,
                tailTopY + tailHeightPx * 0.22f,
                centerX + tailMidHalf * 1.06f,
                tailTopY + tailHeightPx * 0.74f,
                centerX + tailBottomHalf,
                tailBottomY);
        tailPath.cubicTo(
                centerX + tailBottomHalf * 0.82f,
                tailBottomY - concaveLift,
                centerX - tailBottomHalf * 0.82f,
                tailBottomY - concaveLift,
                centerX - tailBottomHalf,
                tailBottomY);
        tailPath.cubicTo(
                centerX - tailMidHalf * 1.06f,
                tailTopY + tailHeightPx * 0.74f,
                centerX - tailTopHalf * 0.98f,
                tailTopY + tailHeightPx * 0.22f,
                centerX - tailTopHalf,
                tailTopY - 0.5f);
        tailPath.close();
    }

    private void drawTailFill(Canvas canvas, float tailTopY, float tailBottomY) {
        int tailMidAlpha = Math.round(alpha * (0.98f + 0.02f * commitGlowAlpha));
        int tailFadeAlpha = Math.round(alpha * (0.86f + 0.08f * commitGlowAlpha));
        setVerticalGradient(
                tailPaint,
                tailTopY,
                tailBottomY,
                new int[] {
                        withAlpha(backgroundColor, alpha),
                        withAlpha(backgroundColor, alpha),
                        withAlpha(backgroundColor, alpha),
                        withAlpha(backgroundColor, tailMidAlpha),
                        withAlpha(backgroundColor, tailFadeAlpha),
                        withAlpha(backgroundColor, Math.round(alpha * (0.42f + 0.08f * commitGlowAlpha))),
                        withAlpha(backgroundColor, 0)
                },
                TAIL_FILL_POSITIONS);
        canvas.drawPath(tailPath, tailPaint);
        tailPaint.setShader(null);
    }

    private void drawTailContactGlow(Canvas canvas, float tailBottomY, float centerX, float tailMidHalf) {
        float impact = Math.max(commitGlowAlpha, inputImpactAlpha * 0.54f);
        if (impact <= 0f) {
            return;
        }
        float width = Math.max(1f, tailMidHalf * (2.60f + impact * 1.24f));
        float height = Math.max(1f, tailHeightPx * (0.15f + impact * 0.065f));
        scratchRect.set(
                centerX - width,
                tailBottomY - height * 1.28f,
                centerX + width,
                tailBottomY + height * 0.45f);
        int contactColor = lightAdaptiveColor(COMMIT_LUMINANCE_THRESHOLD);
        int centerAlpha = adaptiveAlpha(COMMIT_LUMINANCE_THRESHOLD, 0.42f, 0.34f, impact);
        setCenteredHorizontalGradient(
                effectPaint,
                scratchRect.left,
                scratchRect.right,
                scratchRect.centerY(),
                contactColor,
                0,
                centerAlpha);
        canvas.drawOval(scratchRect, effectPaint);
        effectPaint.setShader(null);
    }

    private void drawTailStroke(Canvas canvas) {
        if (borderWidthPx <= 0) {
            return;
        }
        tailStrokePaint.setAlpha(Math.round(alpha * (0.18f + 0.08f * commitGlowAlpha
                + 0.06f * inputImpactAlpha)));
        canvas.drawPath(tailPath, tailStrokePaint);
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = clampByte(alpha);
        fillPaint.setAlpha(this.alpha);
        strokePaint.setAlpha(this.alpha);
        tailStrokePaint.setAlpha(Math.round(this.alpha * 0.26f));
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        applyColorFilter(
                colorFilter,
                fillPaint,
                effectPaint,
                strokePaint,
                tailPaint,
                tailStrokePaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private void drawBodyHighlight(Canvas canvas, float corner) {
        drawAdaptiveTwoStopVerticalRoundRect(
                canvas,
                body,
                corner,
                body.top,
                body.top + body.height() * 0.52f,
                HIGHLIGHT_LUMINANCE_THRESHOLD,
                0.38f,
                0.28f,
                0f,
                0f,
                1f + commitGlowAlpha * 0.50f);
    }

    private void drawCommitGlow(Canvas canvas, float corner) {
        if (commitGlowAlpha <= 0f) {
            return;
        }
        drawAdaptiveVerticalGradientRoundRect(
                canvas,
                body,
                corner,
                body.top,
                body.bottom,
                COMMIT_LUMINANCE_THRESHOLD,
                COMMIT_GLOW_DARK_AMOUNTS,
                COMMIT_GLOW_LIGHT_AMOUNTS,
                commitGlowAlpha,
                COMMIT_GLOW_POSITIONS);
    }

    private void drawInputImpact(Canvas canvas, float corner) {
        if (inputImpactAlpha <= 0f) {
            return;
        }
        float insetX = Math.max(1f, body.width() * 0.075f);
        scratchRect.set(
                body.left + insetX,
                body.top + body.height() * 0.52f,
                body.right - insetX,
                body.bottom - Math.max(1f, borderWidthPx * 0.55f));
        drawAdaptiveVerticalGradientRoundRect(
                canvas,
                scratchRect,
                Math.max(1f, corner * 0.48f),
                scratchRect.top,
                scratchRect.bottom,
                COMMIT_LUMINANCE_THRESHOLD,
                INPUT_IMPACT_DARK_AMOUNTS,
                INPUT_IMPACT_LIGHT_AMOUNTS,
                inputImpactAlpha,
                INPUT_IMPACT_POSITIONS);
    }

    private void drawPressedInset(Canvas canvas, float corner) {
        if (inputImpactAlpha <= 0f) {
            return;
        }
        float top = body.top + body.height() * 0.38f;
        scratchRect.set(body.left, top, body.right, body.bottom);
        int shadeColor = lightAdaptiveColor(SHADOW_LUMINANCE_THRESHOLD);
        int centerAlpha = adaptiveAlpha(SHADOW_LUMINANCE_THRESHOLD, 0.38f, 0.28f, inputImpactAlpha);
        int endAlpha = adaptiveAlpha(SHADOW_LUMINANCE_THRESHOLD, 0.26f, 0.18f, inputImpactAlpha);
        drawVerticalGradientRoundRect(
                canvas,
                effectPaint,
                scratchRect,
                Math.max(1f, corner * 0.62f),
                scratchRect.top,
                scratchRect.bottom,
                new int[] {
                        withAlpha(shadeColor, 0),
                        withAlpha(shadeColor, centerAlpha),
                        withAlpha(shadeColor, endAlpha)
                },
                INPUT_CORE_POSITIONS);
    }

    private void drawCommitSheen(Canvas canvas, float corner) {
        if (commitGlowAlpha <= 0f) {
            return;
        }
        float width = body.width() * (0.46f + 0.34f * commitGlowAlpha);
        float height = Math.max(1f, body.height() * 0.070f + borderWidthPx * 0.35f);
        scratchRect.set(
                body.centerX() - width,
                body.top + body.height() * 0.22f,
                body.centerX() + width,
                body.top + body.height() * 0.22f + height);
        int sheenColor = lightAdaptiveColor(COMMIT_LUMINANCE_THRESHOLD);
        int centerAlpha = adaptiveAlpha(COMMIT_LUMINANCE_THRESHOLD, 0.58f, 0.44f, commitGlowAlpha);
        int edgeAlpha = adaptiveAlpha(COMMIT_LUMINANCE_THRESHOLD, 0.12f, 0.09f, commitGlowAlpha);
        setCenteredHorizontalGradient(
                effectPaint,
                scratchRect.left,
                scratchRect.right,
                scratchRect.centerY(),
                sheenColor,
                edgeAlpha,
                centerAlpha);
        float sheenCorner = Math.max(1f, Math.min(corner * 0.32f, height));
        canvas.drawRoundRect(scratchRect, sheenCorner, sheenCorner, effectPaint);
        effectPaint.setShader(null);
    }

    private void drawInputCore(Canvas canvas, float corner) {
        float intensity = Math.max(commitGlowAlpha, inputImpactAlpha * 0.78f);
        if (intensity <= 0f) {
            return;
        }
        float insetX = Math.max(1f, body.width() * 0.10f);
        float insetY = Math.max(1f, body.height() * 0.16f);
        scratchRect.set(body);
        scratchRect.inset(insetX, insetY);
        int coreColor = lightAdaptiveColor(COMMIT_LUMINANCE_THRESHOLD);
        int centerAlpha = adaptiveAlpha(COMMIT_LUMINANCE_THRESHOLD, 0.32f, 0.24f, intensity);
        int edgeAlpha = adaptiveAlpha(COMMIT_LUMINANCE_THRESHOLD, 0.03f, 0.02f, commitGlowAlpha);
        drawVerticalGradientRoundRect(
                canvas,
                effectPaint,
                scratchRect,
                Math.max(1f, corner * 0.52f),
                scratchRect.top,
                scratchRect.bottom,
                new int[] {
                        withAlpha(coreColor, edgeAlpha),
                        withAlpha(coreColor, centerAlpha),
                        withAlpha(coreColor, edgeAlpha)
                },
                INPUT_CORE_POSITIONS);
    }

    private void drawCommitHalo(Canvas canvas, float corner) {
        if (commitGlowAlpha <= 0f) {
            return;
        }
        float expandX = Math.max(1f, body.width() * 0.034f + borderWidthPx);
        float expandY = Math.max(1f, body.height() * 0.026f + borderWidthPx);
        scratchRect.set(body);
        scratchRect.inset(-expandX, -expandY);
        drawAdaptiveVerticalGradientRoundRect(
                canvas,
                scratchRect,
                corner + expandY,
                scratchRect.top,
                scratchRect.bottom,
                COMMIT_LUMINANCE_THRESHOLD,
                COMMIT_HALO_DARK_AMOUNTS,
                COMMIT_HALO_LIGHT_AMOUNTS,
                commitGlowAlpha,
                COMMIT_HALO_POSITIONS);
    }

    private void drawTopRim(Canvas canvas, float corner) {
        float rimHeight = Math.max(1f, body.height() * 0.085f);
        float inset = Math.max(1f, borderWidthPx + body.width() * 0.055f);
        scratchRect.set(
                body.left + inset,
                body.top + Math.max(1f, borderWidthPx),
                body.right - inset,
                body.top + rimHeight + Math.max(1f, borderWidthPx));
        int rimColor = lightAdaptiveColor(RIM_LUMINANCE_THRESHOLD);
        int rimAlpha = adaptiveAlpha(
                RIM_LUMINANCE_THRESHOLD,
                0.68f,
                0.44f,
                1f + commitGlowAlpha * 0.34f);
        float rimCorner = Math.max(1f, corner * 0.55f);
        drawTwoStopVerticalRoundRect(
                canvas,
                scratchRect,
                rimCorner,
                scratchRect.top,
                scratchRect.bottom,
                rimColor,
                rimAlpha,
                0);
    }

    private void drawBodyShadow(Canvas canvas, float corner) {
        float shadowOffset = Math.max(
                1f,
                borderWidthPx + body.height() * (0.045f + 0.020f * inputImpactAlpha));
        scratchRect.set(body);
        scratchRect.offset(0, shadowOffset);
        drawAdaptiveTwoStopVerticalRoundRect(
                canvas,
                scratchRect,
                corner,
                scratchRect.top,
                scratchRect.bottom,
                SHADOW_LUMINANCE_THRESHOLD,
                0.38f,
                0.48f + 0.22f * inputImpactAlpha,
                0f,
                0f,
                1f + commitGlowAlpha * 0.18f);
    }

    private void drawTailShadow(Canvas canvas, int tailHeightPx) {
        int shadowColor = lightAdaptiveColor(SHADOW_LUMINANCE_THRESHOLD);
        int startAlpha = adaptiveAlpha(
                SHADOW_LUMINANCE_THRESHOLD,
                0.16f,
                0.20f,
                1f + commitGlowAlpha * 0.16f);
        Rect bounds = getBounds();
        float shadowStartY = bounds.bottom - tailHeightPx;
        setTwoStopVerticalGradient(
                effectPaint,
                shadowStartY,
                bounds.bottom,
                shadowColor,
                startAlpha,
                0);
        canvas.save();
        canvas.translate(0, Math.max(1f, borderWidthPx * 0.75f));
        canvas.drawPath(tailPath, effectPaint);
        canvas.restore();
        effectPaint.setShader(null);
    }

    private void drawLowerLip(Canvas canvas, float corner) {
        drawAdaptiveTwoStopVerticalRoundRect(
                canvas,
                body,
                corner,
                body.top + body.height() * 0.50f,
                body.bottom,
                HIGHLIGHT_LUMINANCE_THRESHOLD,
                0f,
                0f,
                0.38f,
                0.40f,
                1f + commitGlowAlpha * 0.30f);
    }

    private void drawTwoStopVerticalRoundRect(
            Canvas canvas,
            RectF rect,
            float corner,
            float gradientTop,
            float gradientBottom,
            int color,
            int startAlpha,
            int endAlpha) {
        setTwoStopVerticalGradient(effectPaint, gradientTop, gradientBottom, color, startAlpha, endAlpha);
        canvas.drawRoundRect(rect, corner, corner, effectPaint);
        effectPaint.setShader(null);
    }

    private void drawAdaptiveTwoStopVerticalRoundRect(
            Canvas canvas,
            RectF rect,
            float corner,
            float gradientTop,
            float gradientBottom,
            int threshold,
            float darkStartAmount,
            float lightStartAmount,
            float darkEndAmount,
            float lightEndAmount,
            float multiplier) {
        drawTwoStopVerticalRoundRect(
                canvas,
                rect,
                corner,
                gradientTop,
                gradientBottom,
                lightAdaptiveColor(threshold),
                adaptiveAlpha(threshold, darkStartAmount, lightStartAmount, multiplier),
                adaptiveAlpha(threshold, darkEndAmount, lightEndAmount, multiplier));
    }

    private void drawVerticalGradientRoundRect(
            Canvas canvas,
            Paint paint,
            RectF rect,
            float corner,
            float gradientTop,
            float gradientBottom,
            int[] colors,
            float[] positions) {
        setVerticalGradient(paint, gradientTop, gradientBottom, colors, positions);
        canvas.drawRoundRect(rect, corner, corner, paint);
        paint.setShader(null);
    }

    private void drawAdaptiveVerticalGradientRoundRect(
            Canvas canvas,
            RectF rect,
            float corner,
            float gradientTop,
            float gradientBottom,
            int threshold,
            float[] darkAmounts,
            float[] lightAmounts,
            float multiplier,
            float[] positions) {
        int color = lightAdaptiveColor(threshold);
        int count = darkAmounts.length;
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = withAlpha(
                    color,
                    adaptiveAlpha(threshold, darkAmounts[i], lightAmounts[i], multiplier));
        }
        drawVerticalGradientRoundRect(
                canvas,
                effectPaint,
                rect,
                corner,
                gradientTop,
                gradientBottom,
                colors,
                positions);
    }

    private void setVerticalGradient(
            Paint paint,
            float top,
            float bottom,
            int[] colors,
            float[] positions) {
        paint.setShader(new LinearGradient(
                0,
                top,
                0,
                bottom,
                colors,
                positions,
                Shader.TileMode.CLAMP));
    }

    private void setTwoStopVerticalGradient(
            Paint paint,
            float top,
            float bottom,
            int color,
            int startAlpha,
            int endAlpha) {
        setVerticalGradient(
                paint,
                top,
                bottom,
                new int[] {
                        withAlpha(color, startAlpha),
                        withAlpha(color, endAlpha)
                },
                TWO_STOP_POSITIONS);
    }

    private void setHorizontalGradient(
            Paint paint,
            float left,
            float right,
            float centerY,
            int[] colors,
            float[] positions) {
        paint.setShader(new LinearGradient(
                left,
                centerY,
                right,
                centerY,
                colors,
                positions,
                Shader.TileMode.CLAMP));
    }

    private void setCenteredHorizontalGradient(
            Paint paint,
            float left,
            float right,
            float centerY,
            int color,
            int edgeAlpha,
            int centerAlpha) {
        setHorizontalGradient(
                paint,
                left,
                right,
                centerY,
                new int[] {
                        withAlpha(color, edgeAlpha),
                        withAlpha(color, centerAlpha),
                        withAlpha(color, edgeAlpha)
                },
                CENTERED_HORIZONTAL_POSITIONS);
    }

    private int adaptiveAlpha(int threshold, float darkAmount, float lightAmount, float multiplier) {
        float amount = backgroundLuminance < threshold ? darkAmount : lightAmount;
        return Math.round(alpha * amount * multiplier);
    }

    private int lightAdaptiveColor(int threshold) {
        return backgroundLuminance < threshold ? 0xFFFFFFFF : 0xFF000000;
    }

    private static void configureFillPaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setDither(true);
    }

    private static void configureStrokePaint(Paint paint, float strokeWidth) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
    }

    private static void applyColorFilter(ColorFilter colorFilter, Paint... paints) {
        for (Paint paint : paints) {
            paint.setColorFilter(colorFilter);
        }
    }

    private static float clampUnit(float value) {
        if (value < 0f) {
            return 0f;
        }
        if (value > 1f) {
            return 1f;
        }
        return value;
    }

    private static int clampByte(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }
}
