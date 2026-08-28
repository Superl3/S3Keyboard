package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

final class LayoutEditorOverlay extends View {
    private static final int MARGIN_COLOR = 0x282196F3;
    private static final int TOP_PADDING_COLOR = 0x284CAF50;
    private static final int BOTTOM_PADDING_COLOR = 0x28FF9800;
    private static final int SPECIAL_GAP_COLOR = 0x289C27B0;
    private static final int BOTTOM_ROW_GAP_COLOR = 0x28F44336;

    private final Paint zonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint originPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    private int leftMarginDp;
    private int rightMarginDp;
    private int topPaddingDp;
    private int bottomPaddingDp;
    private int keyboardHeightDp;
    private int specialColumnPct;
    private int mainSpecialGapDp;
    private int bottomRowTopPaddingDp;
    private int keyGapDp;
    private boolean hangulMode;
    private boolean showZones = true;
    private LayoutGeometrySnapshot geometry = LayoutGeometrySnapshot.empty();

    LayoutEditorOverlay(Context context) {
        super(context);
        this.density = context.getResources().getDisplayMetrics().density;
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(1));
        linePaint.setColor(0x44000000);
        hitPaint.setStyle(Paint.Style.STROKE);
        hitPaint.setStrokeWidth(dp(1));
        hitPaint.setColor(0x884F6BED);
        hitPaint.setPathEffect(new android.graphics.DashPathEffect(
                new float[]{dp(3), dp(3)}, 0));
        originPaint.setStyle(Paint.Style.FILL);
        originPaint.setColor(0xCC4F6BED);
        setFocusable(false);
        // The preview is a visual reference only. Consume its touch stream here so pressing a
        // key never creates a live keyboard gesture underneath the editor handles.
        setClickable(true);
        setWillNotDraw(false);
    }

    void setGeometry(LayoutGeometrySnapshot geometry) {
        this.geometry = geometry == null ? LayoutGeometrySnapshot.empty() : geometry;
        invalidate();
    }

    void update(int leftMarginDp, int rightMarginDp, int topPaddingDp, int bottomPaddingDp,
                int keyboardHeightDp, int specialColumnPct, int mainSpecialGapDp,
                int bottomRowTopPaddingDp, int keyGapDp, boolean hangulMode) {
        this.leftMarginDp = leftMarginDp;
        this.rightMarginDp = rightMarginDp;
        this.topPaddingDp = topPaddingDp;
        this.bottomPaddingDp = bottomPaddingDp;
        this.keyboardHeightDp = keyboardHeightDp;
        this.specialColumnPct = specialColumnPct;
        this.mainSpecialGapDp = mainSpecialGapDp;
        this.bottomRowTopPaddingDp = bottomRowTopPaddingDp;
        this.keyGapDp = keyGapDp;
        this.hangulMode = hangulMode;
        invalidate();
    }

    void setShowZones(boolean show) {
        this.showZones = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!showZones) {
            return;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        int kbHeight = dp(keyboardHeightDp);
        int kbTop = h - kbHeight;
        int kbBottom = h;
        if (kbTop < 0) {
            kbTop = 0;
        }
        int leftMargin = dp(leftMarginDp);
        int rightMargin = dp(rightMarginDp);
        int topPad = dp(topPaddingDp);
        int bottomPad = dp(bottomPaddingDp);
        int bottomRowGap = dp(bottomRowTopPaddingDp);

        zonePaint.setColor(MARGIN_COLOR);
        if (leftMargin > 0) {
            canvas.drawRect(0, kbTop, leftMargin, kbBottom, zonePaint);
        }
        if (rightMargin > 0) {
            canvas.drawRect(w - rightMargin, kbTop, w, kbBottom, zonePaint);
        }

        if (topPad > 0) {
            zonePaint.setColor(TOP_PADDING_COLOR);
            canvas.drawRect(leftMargin, kbTop, w - rightMargin, kbTop + topPad, zonePaint);
        }

        if (bottomPad > 0) {
            zonePaint.setColor(BOTTOM_PADDING_COLOR);
            canvas.drawRect(leftMargin, kbBottom - bottomPad, w - rightMargin, kbBottom, zonePaint);
        }

        if (bottomRowGap > 0) {
            int bottomRowH = dp(KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP);
            zonePaint.setColor(BOTTOM_ROW_GAP_COLOR);
            canvas.drawRect(leftMargin, kbBottom - bottomPad - bottomRowH - bottomRowGap,
                    w - rightMargin, kbBottom - bottomPad - bottomRowH, zonePaint);
        }

        if (hangulMode && specialColumnPct > 0) {
            int availWidth = w - leftMargin - rightMargin;
            int mainWidth = availWidth * (100 - specialColumnPct) / 100;
            int gap = dp(mainSpecialGapDp);
            if (gap > 0) {
                zonePaint.setColor(SPECIAL_GAP_COLOR);
                canvas.drawRect(leftMargin + mainWidth, kbTop + topPad,
                        leftMargin + mainWidth + gap, kbBottom - bottomPad, zonePaint);
            }
        }

        linePaint.setColor(0x33000000);
        canvas.drawRect(leftMargin, kbTop, w - rightMargin, kbBottom, linePaint);

        // These are the real calculator bounds, not an approximation based on padding.
        for (LayoutGeometrySnapshot.Slot slot : geometry.slots) {
            canvas.drawRect(slot.hitRect, hitPaint);
            canvas.drawCircle(slot.gestureOriginX, slot.gestureOriginY, dp(2), originPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
