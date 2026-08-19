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
        if (fullSpan) {
            if (style == STYLE_HORIZONTAL_BAR) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), thickPx);
            } else {
                setMeasuredDimension(thickPx, MeasureSpec.getSize(heightMeasureSpec));
            }
        } else {
            if (style == STYLE_VERTICAL_BAR) {
                setMeasuredDimension(thickPx, lenPx);
            } else {
                setMeasuredDimension(lenPx, thickPx);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        int baseColor = dragging
                ? (accentColor & 0x00FFFFFF) | 0xCC000000
                : (accentColor & 0x00FFFFFF) | 0x66000000;
        barPaint.setColor(baseColor);

        float corner = dp(BAR_THICKNESS_DP) / 2f;
        canvas.drawRoundRect(0, 0, w, h, corner, corner, barPaint);

        int gripColor = dragging ? 0xFFFFFFFF : 0x88FFFFFF;
        barPaint.setColor(gripColor);
        float gripLen = Math.min(w, h) * 0.4f;
        if (style == STYLE_VERTICAL_BAR) {
            float cx = w / 2f;
            float cy = h / 2f;
            canvas.drawRoundRect(cx - dp(1), cy - gripLen / 2, cx + dp(1), cy + gripLen / 2,
                    dp(1), dp(1), barPaint);
        } else {
            float cx = w / 2f;
            float cy = h / 2f;
            canvas.drawRoundRect(cx - gripLen / 2, cy - dp(1), cx + gripLen / 2, cy + dp(1),
                    dp(1), dp(1), barPaint);
        }

        if (dragging) {
            barPaint.setColor((accentColor & 0x00FFFFFF) | 0xCC000000);
            linePaint.setColor((accentColor & 0x00FFFFFF) | 0x88000000);
            if (horizontalDrag) {
                float cy = h / 2f;
                canvas.drawLine(0, cy, w, cy, linePaint);
            } else {
                float cx = w / 2f;
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
