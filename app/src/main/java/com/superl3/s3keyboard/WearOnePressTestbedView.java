package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

final class WearOnePressTestbedView extends View {
    private static final int PAGE_CONSONANTS = 0;
    private static final int PAGE_VOWELS = 1;
    private static final int STAGE_SELECT_KEY = 0;
    private static final int STAGE_SELECT_ACTION = 1;

    private static final String[] CONSONANT_KEYS = {
            "\u3131", "\u3134", "\u3139", "\u3141",
            "\u3145", "\u3147", "\u3148", "\u314e"
    };
    private static final String[] VOWEL_AND_COMMAND_KEYS = {
            "\u3162", "\u3163\u00b7", "\u3161\u3150", "\u00b7\u00b7",
            "\u232b", "\u2423", "?", "\u21b5"
    };
    private static final String[][] CONSONANT_ACTIONS = {
            { "\u3131", "\u3132", "#", "\u314b", "\u314b" },
            { "\u3134", "\u3138", "\u3137", "\u314c", "\u314c" },
            { "\u3139", "^", "~", "=", "-" },
            { "\u3141", "\u3143", "\u3142", "\u314d", "\u314d" },
            { "\u3145", "\u3146", "2", "1", "3" },
            { "\u3147", "\u2665", "5", "4", "6" },
            { "\u3148", "\u3149", "~", "\u314a", "\u314a" },
            { "\u314e", "0", "8", "7", "9" }
    };
    private static final String[][] VOWEL_ACTIONS = {
            { "\u3162", "\u315a", "\u315f", "\u315d", "\u3158" },
            { "\u3163", "\u3157", "\u315c", "\u3153", "\u314f" },
            { "\u3161", "\u3159", "\u315e", "\u3154", "\u3150" },
            { " ", "\u315b", "\u3160", "\u3155", "\u3151" },
            { "\u232b", "\u232b", "\u232b", "\u232b", "\u232b" },
            { " ", "\u00b7", "\u00b7", "\u2190", "\u2192" },
            { "?", "!", "*", "+", "-" },
            { "\u21b5", "\u21b5", "\u21b5", "\u21b5", "\u21b5" }
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path gesturePath = new Path();
    private int page = PAGE_CONSONANTS;
    private int stage = STAGE_SELECT_KEY;
    private int selectedIndex = -1;
    private String committedPreview = "";
    private float centerX;
    private float centerY;
    private float watchRadius;
    private float downX;
    private float downY;

    WearOnePressTestbedView(Context context) {
        super(context);
        setContentDescription("wear_one_press_surface");
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFE7EEF0);
        centerX = getWidth() / 2f;
        centerY = getHeight() * 0.52f;
        watchRadius = Math.min(getWidth() * 0.455f, getHeight() * 0.31f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF111619);
        canvas.drawCircle(centerX, centerY, watchRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setColor(0xFF3D4A4F);
        canvas.drawCircle(centerX, centerY, watchRadius - dp(2), paint);

        drawTitle(canvas);
        drawRing(canvas);
        drawCenterControl(canvas);
        drawInstruction(canvas);
    }

    private void drawTitle(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(sp(18));
        paint.setColor(Color.WHITE);
        String title = stage == STAGE_SELECT_KEY
                ? "1  \ud0a4 \uc120\ud0dd"
                : "2  \ub3d9\uc791 \uc120\ud0dd";
        canvas.drawText(title, centerX, centerY - watchRadius * 0.86f, paint);

        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        paint.setTextSize(sp(12));
        paint.setColor(0xFF9EB4BC);
        String pageLabel = page == PAGE_CONSONANTS ? "\uc790\uc74c \ud398\uc774\uc9c0" : "\ubaa8\uc74c\u00b7\uae30\ub2a5 \ud398\uc774\uc9c0";
        canvas.drawText(pageLabel, centerX, centerY - watchRadius * 0.76f, paint);

        if (!committedPreview.isEmpty()) {
            paint.setTextSize(sp(15));
            paint.setColor(0xFF75D8CB);
            canvas.drawText(committedPreview, centerX, centerY - watchRadius * 0.65f, paint);
        }
    }

    private void drawRing(Canvas canvas) {
        String[] keys = page == PAGE_CONSONANTS ? CONSONANT_KEYS : VOWEL_AND_COMMAND_KEYS;
        float ringRadius = watchRadius * 0.57f;
        float itemRadius = watchRadius * 0.105f;
        for (int index = 0; index < keys.length; index++) {
            float angle = angleFor(index);
            float x = centerX + (float) Math.cos(angle) * ringRadius;
            float y = centerY + (float) Math.sin(angle) * ringRadius;
            boolean selected = stage == STAGE_SELECT_ACTION && selectedIndex == index;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selected ? 0xFF75D8CB : 0x522E3B40);
            canvas.drawCircle(x, y, itemRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(selected ? 2 : 1));
            paint.setColor(selected ? 0xFFE8FFFC : 0x806F858D);
            canvas.drawCircle(x, y, itemRadius, paint);
            drawText(canvas, keys[index], x, y, selected ? 20 : 17,
                    selected ? 0xFF101719 : 0xFFE7F0F2, true);
        }
        if (stage == STAGE_SELECT_ACTION && selectedIndex >= 0) {
            drawReturnPath(canvas, ringRadius);
        }
    }

    private void drawCenterControl(Canvas canvas) {
        float centerRadius = watchRadius * 0.23f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(stage == STAGE_SELECT_ACTION ? 0xE62A3438 : 0xA9212A2E);
        canvas.drawCircle(centerX, centerY, centerRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(0xFF75D8CB);
        canvas.drawCircle(centerX, centerY, centerRadius, paint);

        if (stage == STAGE_SELECT_KEY) {
            drawText(canvas, page == PAGE_CONSONANTS ? "\ubaa8\uc74c" : "\uc790\uc74c",
                    centerX, centerY, 15, 0xFFBFE9E3, true);
            return;
        }

        String[] actions = actionsForSelected();
        drawText(canvas, actions[0], centerX, centerY, 24, Color.WHITE, true);
        float actionRadius = watchRadius * 0.34f;
        drawText(canvas, actions[1], centerX, centerY - actionRadius, 15, 0xFF75D8CB, true);
        drawText(canvas, actions[2], centerX, centerY + actionRadius, 15, 0xFF75D8CB, true);
        drawText(canvas, actions[3], centerX - actionRadius, centerY, 15, 0xFF75D8CB, true);
        drawText(canvas, actions[4], centerX + actionRadius, centerY, 15, 0xFF75D8CB, true);
    }

    private void drawReturnPath(Canvas canvas, float ringRadius) {
        float angle = angleFor(selectedIndex);
        float startX = centerX + (float) Math.cos(angle) * ringRadius * 0.78f;
        float startY = centerY + (float) Math.sin(angle) * ringRadius * 0.78f;
        gesturePath.reset();
        gesturePath.moveTo(startX, startY);
        gesturePath.quadTo(
                (startX + centerX) / 2f + watchRadius * 0.06f,
                (startY + centerY) / 2f - watchRadius * 0.04f,
                centerX,
                centerY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xB875D8CB);
        canvas.drawPath(gesturePath, paint);
    }

    private void drawInstruction(Canvas canvas) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        paint.setTextSize(sp(13));
        paint.setColor(0xFF34464C);
        String line1 = stage == STAGE_SELECT_KEY
                ? "\ubc14\uae65 8\ubc29\ud5a5\uc5d0\uc11c \ud0a4\ub97c \uace0\ub974\uc138\uc694"
                : "\uc911\uc559\uc73c\ub85c \ub3cc\uc544\uc628 \ub4a4 \ubc29\ud5a5\uc73c\ub85c \ubc00\uc5b4 \uc785\ub825";
        canvas.drawText(line1, centerX, centerY + watchRadius + dp(38), paint);
        paint.setTextSize(sp(11));
        paint.setColor(0xFF64777E);
        canvas.drawText("\uc911\uc559 \ud0ed: \uc790\uc74c/\ubaa8\uc74c \ud398\uc774\uc9c0 \uc804\ud658", centerX,
                centerY + watchRadius + dp(61), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) {
            return true;
        }
        if (stage == STAGE_SELECT_KEY) {
            handleKeySelection(event.getX(), event.getY());
        } else {
            handleActionSelection(event.getX(), event.getY());
        }
        performClick();
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void handleKeySelection(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.hypot(dx, dy);
        if (distance < watchRadius * 0.30f) {
            page = page == PAGE_CONSONANTS ? PAGE_VOWELS : PAGE_CONSONANTS;
            return;
        }
        selectedIndex = nearestRingIndex(dx, dy);
        stage = STAGE_SELECT_ACTION;
    }

    private void handleActionSelection(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float distance = (float) Math.hypot(dx, dy);
        int actionIndex;
        if (distance < watchRadius * 0.16f) {
            actionIndex = 0;
        } else if (Math.abs(dx) > Math.abs(dy)) {
            actionIndex = dx < 0 ? 3 : 4;
        } else {
            actionIndex = dy < 0 ? 1 : 2;
        }
        String value = actionsForSelected()[actionIndex];
        committedPreview = "\uc785\ub825  " + value;
        page = page == PAGE_CONSONANTS ? PAGE_VOWELS : PAGE_CONSONANTS;
        stage = STAGE_SELECT_KEY;
        selectedIndex = -1;
    }

    private String[] actionsForSelected() {
        int safeIndex = Math.max(0, Math.min(7, selectedIndex));
        return page == PAGE_CONSONANTS
                ? CONSONANT_ACTIONS[safeIndex]
                : VOWEL_ACTIONS[safeIndex];
    }

    private int nearestRingIndex(float dx, float dy) {
        double angle = Math.atan2(dy, dx);
        double normalized = angle + Math.PI / 2.0;
        if (normalized < 0) {
            normalized += Math.PI * 2.0;
        }
        return ((int) Math.round(normalized / (Math.PI / 4.0))) % 8;
    }

    private float angleFor(int index) {
        return (float) (-Math.PI / 2.0 + index * Math.PI / 4.0);
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float y,
            float textSizeSp,
            int color,
            boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                "sans-serif",
                bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        paint.setTextSize(sp(textSizeSp));
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(text, x, y - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
