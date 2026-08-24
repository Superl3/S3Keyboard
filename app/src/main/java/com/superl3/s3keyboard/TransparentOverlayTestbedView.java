package com.superl3.s3keyboard;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

final class TransparentOverlayTestbedView {
    private static final String LOG_TAG = "OverlayImeTestbed";
    private TransparentOverlayTestbedView() {
    }

    static View create(Activity activity, boolean showKeyboard) {
        int pagePadding = SettingsRowBuilder.dp(activity, 20);
        FrameLayout root = new FrameLayout(activity);
        root.setBackgroundColor(Color.rgb(236, 241, 244));
        root.addView(new TestBackdropView(activity), new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout content = SettingsRowBuilder.vertical(activity);
        content.setPadding(pagePadding, pagePadding, pagePadding, pagePadding);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(content, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        TextView title = label(activity, R.string.overlay_testbed_title, 24, Color.rgb(24, 45, 53));
        content.addView(title, SettingsRowBuilder.matchWrap());

        TextView topAnchor = label(
                activity,
                R.string.overlay_testbed_top_anchor,
                14,
                Color.rgb(48, 93, 102));
        topAnchor.setContentDescription("overlay_test_top_anchor");
        content.addView(topAnchor, SettingsRowBuilder.matchWrapWithTop(activity, 16));

        EditText input = SettingsRowBuilder.editText(activity);
        input.setText(R.string.overlay_testbed_initial_text);
        input.setSelection(input.length());
        input.setMinHeight(SettingsRowBuilder.dp(activity, 64));
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setContentDescription("overlay_test_textbox");
        content.addView(input, SettingsRowBuilder.matchWrapWithTop(activity, 22));

        TextView fixedPosition = label(
                activity,
                R.string.overlay_testbed_fixed_position,
                16,
                Color.rgb(33, 78, 87));
        fixedPosition.setGravity(Gravity.CENTER);
        fixedPosition.setBackground(markerBackground(
                Color.rgb(208, 229, 231),
                Color.rgb(57, 112, 121),
                SettingsRowBuilder.dp(activity, 8)));
        fixedPosition.setPadding(pagePadding, pagePadding, pagePadding, pagePadding);
        fixedPosition.setContentDescription("overlay_test_fixed_marker");
        content.addView(fixedPosition, SettingsRowBuilder.matchWrapWithTop(activity, 28));

        View spacer = new View(activity);
        content.addView(spacer, SettingsRowBuilder.matchWeightedFill());

        TextView behindKeyboard = label(
                activity,
                R.string.overlay_testbed_behind_keyboard,
                18,
                Color.rgb(91, 58, 18));
        behindKeyboard.setGravity(Gravity.CENTER);
        behindKeyboard.setBackground(markerBackground(
                Color.rgb(255, 229, 172),
                Color.rgb(172, 110, 24),
                SettingsRowBuilder.dp(activity, 6)));
        behindKeyboard.setPadding(pagePadding, pagePadding, pagePadding, pagePadding);
        behindKeyboard.setContentDescription("overlay_test_behind_keyboard");
        content.addView(behindKeyboard, SettingsRowBuilder.matchWrapWithTop(activity, 12));

        TextView viewport = label(activity, R.string.overlay_testbed_viewport_pending, 12, Color.DKGRAY);
        viewport.setContentDescription("overlay_test_viewport");
        content.addView(viewport, SettingsRowBuilder.matchWrapWithTop(activity, 8));
        root.addOnLayoutChangeListener((view, left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom) -> {
            viewport.setText(activity.getString(
                    R.string.overlay_testbed_viewport_format,
                    right - left,
                    bottom - top));
            root.post(() -> logGeometry(root, input));
        });
        root.postDelayed(() -> logGeometry(root, input), 750);
        root.postDelayed(() -> logGeometry(root, input), 3_000);
        root.postDelayed(() -> logGeometry(root, input), 8_000);

        input.setFocusableInTouchMode(true);
        if (showKeyboard) {
            input.requestFocusFromTouch();
            input.postDelayed(() -> {
                InputMethodManager imm = activity.getSystemService(InputMethodManager.class);
                if (imm != null) {
                    imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 450);
        }
        return root;
    }

    private static void logGeometry(View root, View input) {
        int[] location = new int[2];
        input.getLocationOnScreen(location);
        Log.i(LOG_TAG, "viewport=" + root.getWidth() + "x" + root.getHeight()
                + " textbox=" + location[0] + "," + location[1]
                + "," + input.getWidth() + "x" + input.getHeight());
    }

    private static TextView label(Activity activity, int textResId, int textSizeSp, int color) {
        TextView view = new TextView(activity);
        view.setText(textResId);
        view.setTextSize(textSizeSp);
        view.setTextColor(color);
        return view;
    }

    private static GradientDrawable markerBackground(int fillColor, int strokeColor, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setStroke(2, strokeColor);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static final class TestBackdropView extends View {
        private static final int[] BAND_COLORS = {
                0xFFE7F3F2,
                0xFFFFEDC7,
                0xFFF2E8F5,
                0xFFDDEBF4
        };
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int bandHeight;

        TestBackdropView(Activity activity) {
            super(activity);
            bandHeight = SettingsRowBuilder.dp(activity, 92);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            for (int top = 0, index = 0; top < getHeight(); top += bandHeight, index++) {
                paint.setColor(BAND_COLORS[index % BAND_COLORS.length]);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0, top, getWidth(), Math.min(getHeight(), top + bandHeight), paint);
            }
            paint.setColor(0x35507078);
            paint.setStrokeWidth(SettingsRowBuilder.dp(getContext(), 1));
            paint.setStyle(Paint.Style.STROKE);
            int diagonalStep = Math.max(1, SettingsRowBuilder.dp(getContext(), 64));
            for (int start = -getHeight(); start < getWidth(); start += diagonalStep) {
                canvas.drawLine(start, getHeight(), start + getHeight(), 0, paint);
            }
        }
    }
}
