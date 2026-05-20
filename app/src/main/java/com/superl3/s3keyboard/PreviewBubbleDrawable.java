package com.superl3.s3keyboard;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

final class PreviewBubbleDrawable extends Drawable {
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final int cornerPx;
    private final int tailHeightPx;
    private final int borderWidthPx;

    PreviewBubbleDrawable(
            int backgroundColor,
            int borderColor,
            int borderWidthPx,
            int cornerPx,
            int tailHeightPx) {
        this.cornerPx = Math.max(0, cornerPx);
        this.tailHeightPx = Math.max(0, tailHeightPx);
        this.borderWidthPx = Math.max(0, borderWidthPx);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(backgroundColor);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.MITER);
        strokePaint.setStrokeWidth(this.borderWidthPx);
        strokePaint.setColor(borderColor);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float inset = borderWidthPx / 2f;
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset - tailHeightPx;
        float corner = Math.min(cornerPx, Math.min(right - left, bottom - top) / 3f);
        float tailHalf = Math.min((right - left) * 0.14f, Math.max(corner * 1.3f, tailHeightPx * 0.9f));
        float centerX = (left + right) / 2f;

        path.reset();
        path.moveTo(left + corner, top);
        path.lineTo(right - corner, top);
        path.lineTo(right, top + corner);
        path.lineTo(right, bottom - corner);
        path.lineTo(right - corner, bottom);
        path.lineTo(centerX + tailHalf, bottom);
        path.lineTo(centerX, bottom + tailHeightPx);
        path.lineTo(centerX - tailHalf, bottom);
        path.lineTo(left + corner, bottom);
        path.lineTo(left, bottom - corner);
        path.lineTo(left, top + corner);
        path.close();

        canvas.drawPath(path, fillPaint);
        if (borderWidthPx > 0) {
            canvas.drawPath(path, strokePaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
