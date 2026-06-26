package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

final class PreviewOverlayController {
    private static final int TOP_RESERVE_DP = 112;
    private static final int ANGULAR_TAIL_DP = 22;

    private final Context context;
    private final FrameLayout overlayContainer;
    private final PopupWindow popup;
    private final List<TextView> overlayPool = new ArrayList<>();

    private KeyboardSettings settings = KeyboardSettings.defaults();
    private int topPadPx;

    PreviewOverlayController(Context context) {
        this.context = context;
        overlayContainer = new FrameLayout(context);
        overlayContainer.setClipChildren(false);
        overlayContainer.setClipToPadding(false);

        popup = new PopupWindow(overlayContainer, 1, 1);
        popup.setTouchable(false);
        popup.setFocusable(false);
        popup.setClippingEnabled(false);
        popup.setBackgroundDrawable(null);
        ensureOverlay(0);
    }

    void setSettings(KeyboardSettings settings) {
        this.settings = settings == null ? KeyboardSettings.defaults() : settings;
    }

    void show(View anchor, PreviewOverlaySpec spec) {
        if (spec == null) {
            return;
        }
        List<PreviewOverlaySpec> specs = new ArrayList<>();
        specs.add(spec);
        show(anchor, specs);
    }

    void show(View anchor, List<PreviewOverlaySpec> specs) {
        if (anchor == null || specs == null || specs.isEmpty()) {
            dismiss();
            return;
        }

        int[] windowLocation = new int[2];
        anchor.getLocationInWindow(windowLocation);
        int maxBottom = anchor.getHeight();
        int requiredTopPad = dp(TOP_RESERVE_DP);
        for (PreviewOverlaySpec spec : specs) {
            requiredTopPad = Math.max(requiredTopPad, Math.max(0, -spec.y) + dp(4));
            maxBottom = Math.max(maxBottom, spec.y + spec.height);
        }
        int topPad = popup.isShowing() ? Math.max(requiredTopPad, topPadPx) : requiredTopPad;
        topPadPx = topPad;

        int popupWidth = Math.max(anchor.getWidth(), 1);
        int popupHeight = Math.max(1, topPad + maxBottom + dp(4));
        overlayContainer.setMinimumWidth(popupWidth);
        overlayContainer.setMinimumHeight(popupHeight);

        for (int i = 0; i < specs.size(); i++) {
            TextView overlay = ensureOverlay(i);
            PreviewOverlaySpec spec = specs.get(i);
            applySpec(overlay, spec);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(spec.width, spec.height);
            params.leftMargin = spec.x;
            params.topMargin = spec.y + topPad;
            overlay.setLayoutParams(params);
            overlay.setVisibility(View.VISIBLE);
        }
        for (int i = specs.size(); i < overlayPool.size(); i++) {
            overlayPool.get(i).setVisibility(View.GONE);
        }

        int popupX = windowLocation[0];
        int popupY = windowLocation[1] - topPad;
        if (popup.isShowing()) {
            popup.update(popupX, popupY, popupWidth, popupHeight);
        } else {
            popup.setWidth(popupWidth);
            popup.setHeight(popupHeight);
            popup.showAtLocation(anchor, Gravity.NO_GRAVITY, popupX, popupY);
        }
    }

    void dismiss() {
        if (popup.isShowing()) {
            popup.dismiss();
        }
        for (TextView overlay : overlayPool) {
            overlay.setVisibility(View.GONE);
        }
        topPadPx = 0;
    }

    private TextView ensureOverlay(int index) {
        while (overlayPool.size() <= index) {
            TextView overlay = new TextView(context);
            overlay.setGravity(Gravity.CENTER);
            overlay.setSingleLine(true);
            overlay.setIncludeFontPadding(false);
            overlay.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            overlay.setVisibility(View.VISIBLE);
            overlayPool.add(overlay);
            overlayContainer.addView(overlay, new FrameLayout.LayoutParams(1, 1));
        }
        return overlayPool.get(index);
    }

    private void applySpec(TextView overlay, PreviewOverlaySpec spec) {
        overlay.setText(spec.label);
        overlay.setTextColor(spec.textColor);
        overlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, spec.textSizePx);
        overlay.setAlpha(spec.alpha);
        overlay.setPivotX(spec.width / 2f);
        overlay.setPivotY(spec.height);
        overlay.setScaleX(spec.scale);
        overlay.setScaleY(spec.scale);
        overlay.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                context,
                settings.fontFamily,
                settings.primaryTextBold,
                settings.primaryTextItalic));
        if (spec.angularBubble) {
            int tailHeight = dp(ANGULAR_TAIL_DP);
            overlay.setPadding(0, 0, 0, tailHeight);
            overlay.setBackground(new PreviewBubbleDrawable(
                    spec.backgroundColor,
                    spec.borderColor,
                    spec.borderWidthPx,
                    spec.cornerRadiusPx,
                    tailHeight));
        } else {
            overlay.setPadding(0, 0, 0, 0);
            GradientDrawable background = new GradientDrawable();
            background.setColor(spec.backgroundColor);
            background.setCornerRadius(spec.cornerRadiusPx);
            if (spec.borderWidthPx > 0) {
                background.setStroke(spec.borderWidthPx, spec.borderColor);
            }
            overlay.setBackground(background);
        }
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
