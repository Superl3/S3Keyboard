package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

final class OneFingerFlowGuideView extends View {
    private static final int[] LABELS = {
            R.string.one_finger_flow_press,
            R.string.one_finger_flow_commit,
            R.string.one_finger_flow_roam,
            R.string.one_finger_flow_select
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path loopPath = new Path();
    private final SettingsUiPalette palette;
    private final float density;
    private final float scaledDensity;

    OneFingerFlowGuideView(Context context) {
        super(context);
        palette = SettingsUiPalette.from(context);
        density = getResources().getDisplayMetrics().density;
        scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        setContentDescription(context.getString(R.string.one_finger_flow_accessibility));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                resolveSize(dp(280), widthMeasureSpec),
                resolveSize(dp(92), heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float sideInset = dp(24);
        float nodeY = dp(34);
        float radius = dp(12);
        float step = Math.max(
                1f,
                (getWidth() - sideInset * 2f) / (LABELS.length - 1f));

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1)));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(palette.border);
        for (int index = 0; index < LABELS.length - 1; index++) {
            float fromX = sideInset + step * index + radius;
            float toX = sideInset + step * (index + 1) - radius;
            canvas.drawLine(fromX, nodeY, toX, nodeY, paint);
            float arrow = dp(3);
            canvas.drawLine(toX, nodeY, toX - arrow, nodeY - arrow, paint);
            canvas.drawLine(toX, nodeY, toX - arrow, nodeY + arrow, paint);
        }
        drawRepeatLoop(canvas, sideInset, step, nodeY, radius);

        paint.setTextAlign(Paint.Align.CENTER);
        float labelTextSize = fittingLabelTextSize(step * 0.82f);
        for (int index = 0; index < LABELS.length; index++) {
            float centerX = sideInset + step * index;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(palette.surfaceRaised);
            canvas.drawCircle(centerX, nodeY, radius, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, dp(1)));
            paint.setColor(palette.selectedBorder);
            canvas.drawCircle(centerX, nodeY, radius, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(palette.specialForeground);
            paint.setTextSize(sp(12));
            paint.setFakeBoldText(true);
            Paint.FontMetrics numberMetrics = paint.getFontMetrics();
            float numberBaseline = nodeY - (numberMetrics.ascent + numberMetrics.descent) / 2f;
            canvas.drawText(String.valueOf(index + 1), centerX, numberBaseline, paint);

            paint.setColor(palette.textSecondary);
            paint.setTextSize(labelTextSize);
            paint.setFakeBoldText(false);
            Paint.FontMetrics labelMetrics = paint.getFontMetrics();
            float labelBaseline = dp(72) - (labelMetrics.ascent + labelMetrics.descent) / 2f;
            canvas.drawText(getContext().getString(LABELS[index]), centerX, labelBaseline, paint);
        }
    }

    private void drawRepeatLoop(
            Canvas canvas,
            float sideInset,
            float step,
            float nodeY,
            float radius) {
        float fromX = sideInset + step * (LABELS.length - 1);
        float toX = sideInset + step;
        float endpointY = nodeY - radius - dp(2);
        float curveY = dp(4);
        loopPath.reset();
        loopPath.moveTo(fromX, endpointY);
        loopPath.cubicTo(
                fromX - step * 0.15f,
                curveY,
                toX + step * 0.15f,
                curveY,
                toX,
                endpointY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dp(1)));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(palette.border);
        canvas.drawPath(loopPath, paint);
        float arrow = dp(3);
        canvas.drawLine(toX, endpointY, toX + arrow, endpointY - arrow, paint);
        canvas.drawLine(toX, endpointY, toX + arrow, endpointY + arrow, paint);
    }

    private int dp(int value) {
        return Math.round(value * density);
    }

    private float sp(int value) {
        return value * scaledDensity;
    }

    private float fittingLabelTextSize(float availableWidth) {
        float preferred = sp(11);
        paint.setTextSize(preferred);
        float widest = 1f;
        for (int label : LABELS) {
            widest = Math.max(widest, paint.measureText(getContext().getString(label)));
        }
        if (widest <= availableWidth) {
            return preferred;
        }
        return Math.max(sp(8), preferred * availableWidth / widest);
    }
}
