package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import java.util.List;

final class PreviewOverlayController {
    private static final int TOP_RESERVE_DP = 126;
    private static final int OUTER_PAD_DP = 4;

    private final PreviewOverlayCanvasView overlayView;
    private final PopupWindow popup;
    private final int[] windowLocation = new int[2];
    private final int minimumTopPadPx;
    private final int outerPadPx;

    private int topPadPx;
    private int lastPopupX = Integer.MIN_VALUE;
    private int lastPopupY = Integer.MIN_VALUE;
    private int lastPopupWidth = -1;
    private int lastPopupHeight = -1;

    PreviewOverlayController(Context context) {
        minimumTopPadPx = SettingsRowBuilder.dp(context, TOP_RESERVE_DP);
        outerPadPx = SettingsRowBuilder.dp(context, OUTER_PAD_DP);
        overlayView = new PreviewOverlayCanvasView(context);
        popup = new PopupWindow(overlayView, 1, 1);
        popup.setTouchable(false);
        popup.setFocusable(false);
        popup.setClippingEnabled(false);
        popup.setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    void setSettings(KeyboardSettings settings) {
        overlayView.setSettings(settings);
    }

    void show(View anchor, List<PreviewOverlaySpec> specs) {
        if (anchor == null || specs == null || specs.isEmpty()) {
            dismiss();
            return;
        }

        anchor.getLocationInWindow(windowLocation);
        int requiredTopPad = requiredTopPad(specs);
        topPadPx = popup.isShowing() ? Math.max(requiredTopPad, topPadPx) : requiredTopPad;
        int popupWidth = Math.max(anchor.getWidth(), 1);
        int popupHeight = Math.max(1, topPadPx + maxBottom(anchor, specs) + outerPadPx);
        int popupX = windowLocation[0];
        int popupY = windowLocation[1] - topPadPx;

        overlayView.setSpecs(specs, topPadPx);
        showOrUpdatePopup(anchor, popupX, popupY, popupWidth, popupHeight);
    }

    void dismiss() {
        overlayView.clearSpecs();
        if (popup.isShowing()) {
            popup.dismiss();
        }
        topPadPx = 0;
        lastPopupX = Integer.MIN_VALUE;
        lastPopupY = Integer.MIN_VALUE;
        lastPopupWidth = -1;
        lastPopupHeight = -1;
    }

    private int maxBottom(View anchor, List<PreviewOverlaySpec> specs) {
        int maxBottom = anchor.getHeight();
        for (PreviewOverlaySpec spec : specs) {
            maxBottom = Math.max(maxBottom, spec.y + spec.height);
        }
        return maxBottom;
    }

    private int requiredTopPad(List<PreviewOverlaySpec> specs) {
        int requiredTopPad = minimumTopPadPx;
        for (PreviewOverlaySpec spec : specs) {
            requiredTopPad = Math.max(requiredTopPad, Math.max(0, -spec.y) + outerPadPx);
        }
        return requiredTopPad;
    }

    private void showOrUpdatePopup(
            View anchor,
            int popupX,
            int popupY,
            int popupWidth,
            int popupHeight) {
        if (!popup.isShowing()) {
            popup.setWidth(popupWidth);
            popup.setHeight(popupHeight);
            popup.showAtLocation(anchor, Gravity.NO_GRAVITY, popupX, popupY);
            rememberGeometry(popupX, popupY, popupWidth, popupHeight);
            return;
        }
        if (popupX == lastPopupX
                && popupY == lastPopupY
                && popupWidth == lastPopupWidth
                && popupHeight == lastPopupHeight) {
            return;
        }
        popup.update(popupX, popupY, popupWidth, popupHeight);
        rememberGeometry(popupX, popupY, popupWidth, popupHeight);
    }

    private void rememberGeometry(int x, int y, int width, int height) {
        lastPopupX = x;
        lastPopupY = y;
        lastPopupWidth = width;
        lastPopupHeight = height;
    }
}
