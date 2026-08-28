package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

final class LayoutEditorHandleView extends View {
    private static final int BAR_THICKNESS_DP = 10;
    private static final int BAR_LENGTH_DP = 48;
    private static final int TOUCH_TARGET_DP = 48;
    private static final int HIT_SLOP_DP = 32;
    private static final int LABEL_TEXT_SP = 12;

    static final int STYLE_VERTICAL_BAR = 0;
    static final int STYLE_HORIZONTAL_BAR = 1;

    private final boolean horizontalDrag;
    private final int minValue;
    private final int maxValue;
    private final OnDragListener listener;
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int accentColor;
    private final float density;
    private final boolean invertDelta;
    private final String unit;
    private final int style;

    private int currentValue;
    private float startTouchRaw;
    private int startValue;
    private boolean dragging;
    private boolean fullSpan;
    private int lastDeltaDp;

    interface OnDragListener {
        void onDragStarted(int rawValue);
        void onDragMoved(int newValue, int rawDeltaDp);
        void onDragEnded(int finalValue, int rawDeltaDp);
    }

    LayoutEditorHandleView(Context context, int accentColor, int minValue, int maxValue,
                           int initialValue, boolean horizontalDrag, boolean invertDelta,
                           OnDragListener listener, String unit) {
        this(context, accentColor, minValue, maxValue, initialValue, horizontalDrag, invertDelta,
                listener, unit, horizontalDrag ? STYLE_VERTICAL_BAR : STYLE_HORIZONTAL_BAR);
    }

    LayoutEditorHandleView(Context context, int accentColor, int minValue, int maxValue,
                           int initialValue, boolean horizontalDrag, boolean invertDelta,
                           OnDragListener listener, String unit, int style) {
        super(context);
        this.accentColor = accentColor;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.currentValue = clamp(initialValue);
        this.horizontalDrag = horizontalDrag;
        this.invertDelta = invertDelta;
        this.listener = listener;
        this.density = context.getResources().getDisplayMetrics().density;
        this.unit = unit == null ? "" : unit;
        this.style = style;
        labelPaint.setTextSize(LABEL_TEXT_SP * density);
        labelPaint.setFakeBoldText(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(1));
        linePaint.setPathEffect(new android.graphics.DashPathEffect(new float[]{dp(4), dp(4)}, 0));
        setFocusable(false);
        setClickable(true);
        setElevation(dp(4));
    }

    void setFullSpan(boolean fullSpan) {
        this.fullSpan = fullSpan;
    }

    int getValue() {
        return currentValue;
    }

    void setValue(int value) {
        currentValue = clamp(value);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int thickPx = dp(BAR_THICKNESS_DP);
        int lenPx = dp(BAR_LENGTH_DP);
        int targetPx = dp(TOUCH_TARGET_DP);
        if (fullSpan) {
            if (style == STYLE_HORIZONTAL_BAR) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), targetPx);
            } else {
                setMeasuredDimension(targetPx, MeasureSpec.getSize(heightMeasureSpec));
            }
        } else {
            if (style == STYLE_VERTICAL_BAR) {
                setMeasuredDimension(targetPx, Math.max(targetPx, lenPx));
            } else {
                setMeasuredDimension(Math.max(targetPx, lenPx), targetPx);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        int outlineColor = (accentColor & 0x00FFFFFF) | (dragging ? 0xF0000000 : 0xC0000000);
        int fillColor = (accentColor & 0x00FFFFFF) | (dragging ? 0xF0000000 : 0xD0000000);
        float thickness = dp(BAR_THICKNESS_DP);
        float visualLength = dp(BAR_LENGTH_DP);
        float cx = w / 2f;
        float cy = h / 2f;

        if (style == STYLE_HORIZONTAL_BAR && fullSpan) {
            linePaint.setColor((accentColor & 0x00FFFFFF) | 0x88000000);
            linePaint.setStrokeWidth(dp(2));
            canvas.drawLine(0, cy, w, cy, linePaint);
            visualLength = Math.min(visualLength, Math.max(thickness, w - dp(8)));
        } else {
            visualLength = Math.min(visualLength,
                    style == STYLE_VERTICAL_BAR ? Math.max(thickness, h - dp(8))
                            : Math.max(thickness, w - dp(8)));
        }

        barPaint.setColor(outlineColor);
        if (style == STYLE_VERTICAL_BAR) {
            canvas.drawRoundRect(
                    cx - thickness / 2f - dp(1),
                    cy - visualLength / 2f - dp(1),
                    cx + thickness / 2f + dp(1),
                    cy + visualLength / 2f + dp(1),
                    thickness / 2f,
                    thickness / 2f,
                    barPaint);
        } else {
            canvas.drawRoundRect(
                    cx - visualLength / 2f - dp(1),
                    cy - thickness / 2f - dp(1),
                    cx + visualLength / 2f + dp(1),
                    cy + thickness / 2f + dp(1),
                    thickness / 2f,
                    thickness / 2f,
                    barPaint);
        }
        barPaint.setColor(fillColor);
        if (style == STYLE_VERTICAL_BAR) {
            canvas.drawRoundRect(
                    cx - thickness / 2f,
                    cy - visualLength / 2f,
                    cx + thickness / 2f,
                    cy + visualLength / 2f,
                    thickness / 2f,
                    thickness / 2f,
                    barPaint);
        } else {
            canvas.drawRoundRect(
                    cx - visualLength / 2f,
                    cy - thickness / 2f,
                    cx + visualLength / 2f,
                    cy + thickness / 2f,
                    thickness / 2f,
                    thickness / 2f,
                    barPaint);
        }

        int gripColor = dragging ? 0xFFFFFFFF : 0xDDFFFFFF;
        barPaint.setColor(gripColor);
        float gripLen = Math.min(visualLength * 0.42f, dp(28));
        if (style == STYLE_VERTICAL_BAR) {
            canvas.drawRoundRect(cx - dp(1), cy - gripLen / 2, cx + dp(1), cy + gripLen / 2,
                    dp(1), dp(1), barPaint);
        } else {
            canvas.drawRoundRect(cx - gripLen / 2, cy - dp(1), cx + gripLen / 2, cy + dp(1),
                    dp(1), dp(1), barPaint);
        }

        if (dragging) {
            barPaint.setColor((accentColor & 0x00FFFFFF) | 0xCC000000);
            linePaint.setColor((accentColor & 0x00FFFFFF) | 0x88000000);
            if (horizontalDrag) {
                canvas.drawLine(0, cy, w, cy, linePaint);
            } else {
                canvas.drawLine(cx, 0, cx, h, linePaint);
            }

            String label = currentValue + unit;
            float labelW = labelPaint.measureText(label);
            float labelH = labelPaint.getTextSize();
            float padX = dp(6);
            float padY = dp(3);
            RectF labelRect = new RectF();
            if (horizontalDrag) {
                labelRect.left = w / 2f - labelW / 2f - padX;
                labelRect.top = -labelH - padY * 2;
                labelRect.right = w / 2f + labelW / 2f + padX;
                labelRect.bottom = -padY;
            } else {
                labelRect.left = w + padX;
                labelRect.top = h / 2f - labelH / 2f - padY;
                labelRect.right = w + padX + labelW + padX * 2;
                labelRect.bottom = h / 2f + labelH / 2f + padY;
            }
            labelPaint.setColor((accentColor & 0x00FFFFFF) | 0xEE000000);
            canvas.drawRoundRect(labelRect, dp(3), dp(3), labelPaint);
            labelPaint.setColor(0xFFFFFFFF);
            canvas.drawText(label,
                    labelRect.left + padX,
                    labelRect.bottom - padY - (labelH - labelPaint.getTextSize()) / 2f,
                    labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawCoord = horizontalDrag ? event.getRawX() : event.getRawY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!isInsideHitRect(event.getX(), event.getY())) {
                    return false;
                }
                startTouchRaw = rawCoord;
                startValue = currentValue;
                lastDeltaDp = 0;
                dragging = true;
                listener.onDragStarted(currentValue);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!dragging) {
                    return false;
                }
                float deltaPx = rawCoord - startTouchRaw;
                int deltaDp = Math.round(deltaPx / density);
                lastDeltaDp = deltaDp;
                int signedDelta = invertDelta ? -deltaDp : deltaDp;
                int newValue = clamp(startValue + signedDelta);
                if (newValue != currentValue) {
                    currentValue = newValue;
                    listener.onDragMoved(currentValue, deltaDp);
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    listener.onDragEnded(currentValue, lastDeltaDp);
                    invalidate();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean isInsideHitRect(float x, float y) {
        float slop = dp(HIT_SLOP_DP);
        float left = -slop;
        float top = -slop;
        float right = getWidth() + slop;
        float bottom = getHeight() + slop;
        if (horizontalDrag) {
            left -= slop;
            right += slop;
        } else {
            top -= slop;
            bottom += slop;
        }
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    private int clamp(int value) {
        if (value < minValue) {
            return minValue;
        }
        if (value > maxValue) {
            return maxValue;
        }
        return value;
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
