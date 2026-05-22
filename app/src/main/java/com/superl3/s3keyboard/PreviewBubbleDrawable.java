package com.superl3.s3keyboard;

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
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tailStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path tailPath = new Path();
    private final Path strokePath = new Path();
    private final RectF body = new RectF();
    private final int backgroundColor;
    private final int borderColor;
    private final int cornerPx;
    private final int tailHeightPx;
    private final int borderWidthPx;
    private int alpha = 255;

    PreviewBubbleDrawable(
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            int cornerPx,
            int tailHeightPx) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.cornerPx = Math.max(0, cornerPx);
        this.tailHeightPx = Math.max(0, tailHeightPx);
        this.borderWidthPx = Math.max(0, borderWidthPx);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(backgroundColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeWidth(this.borderWidthPx);
        strokePaint.setColor(borderColor);
        tailPaint.setStyle(Paint.Style.FILL);
        tailPaint.setDither(true);
        tailStrokePaint.setStyle(Paint.Style.STROKE);
        tailStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        tailStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        tailStrokePaint.setStrokeWidth(Math.max(1f, this.borderWidthPx * 0.75f));
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
        float tailTopHalf = width * 0.43f;
        float tailMidHalf = width * 0.28f;
        float tailBottomHalf = Math.max(width * 0.16f, 1f);

        body.set(left, top, right, bottom);
        canvas.drawRoundRect(body, corner, corner, fillPaint);
        if (borderWidthPx > 0) {
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
            canvas.drawPath(strokePath, strokePaint);
        }

        if (tailHeightPx > 0) {
            float tailBottomY = bottom + tailHeightPx;
            float concaveLift = Math.max(1f, tailHeightPx * 0.20f);
            tailPath.reset();
            tailPath.moveTo(centerX - tailTopHalf, bottom - 0.5f);
            tailPath.lineTo(centerX + tailTopHalf, bottom - 0.5f);
            tailPath.cubicTo(
                    centerX + tailTopHalf * 0.96f,
                    bottom + tailHeightPx * 0.32f,
                    centerX + tailMidHalf,
                    bottom + tailHeightPx * 0.70f,
                    centerX + tailBottomHalf,
                    tailBottomY);
            tailPath.cubicTo(
                    centerX + tailBottomHalf * 0.58f,
                    tailBottomY - concaveLift,
                    centerX - tailBottomHalf * 0.58f,
                    tailBottomY - concaveLift,
                    centerX - tailBottomHalf,
                    tailBottomY);
            tailPath.cubicTo(
                    centerX - tailMidHalf,
                    bottom + tailHeightPx * 0.70f,
                    centerX - tailTopHalf * 0.96f,
                    bottom + tailHeightPx * 0.32f,
                    centerX - tailTopHalf,
                    bottom - 0.5f);
            tailPath.close();
            tailPaint.setShader(new LinearGradient(
                    0,
                    bottom,
                    0,
                    tailBottomY,
                    new int[] {
                            withAlpha(backgroundColor, alpha),
                            withAlpha(backgroundColor, alpha),
                            withAlpha(backgroundColor, Math.round(alpha * 0.36f)),
                            withAlpha(backgroundColor, 0)
                    },
                    new float[] { 0f, 0.44f, 0.78f, 1f },
                    Shader.TileMode.CLAMP));
            canvas.drawPath(tailPath, tailPaint);
            tailPaint.setShader(null);
            if (borderWidthPx > 0) {
                tailStrokePaint.setAlpha(Math.round(alpha * 0.26f));
                canvas.drawPath(tailPath, tailStrokePaint);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        fillPaint.setAlpha(this.alpha);
        strokePaint.setAlpha(this.alpha);
        tailStrokePaint.setAlpha(Math.round(this.alpha * 0.26f));
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
        tailStrokePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }
}
