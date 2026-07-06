package com.superl3.s3keyboard;

import android.content.Context;
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
    private static final int TOP_RESERVE_DP = 126;
    private static final int ANGULAR_TAIL_DP = 70;
    private static final float TEXT_POP_BASELINE = 1f;
    private static final float SHADOW_RADIUS_BASE_DP = 1.52f;
    private static final float SHADOW_RADIUS_COMMIT_DP = 0.24f;
    private static final float SHADOW_RADIUS_PRESS_DP = 0.08f;
    private static final float SHADOW_RADIUS_TEXT_POP_DP = 0.20f;
    private static final float SHADOW_DY_BASE_DP = 0.92f;
    private static final float SHADOW_DY_COMMIT_DP = 0.08f;
    private static final float SHADOW_DY_PRESS_DP = 0.02f;
    private static final float SHADOW_DY_TEXT_POP_DP = 0.12f;
    private static final float TRANSLATION_Y_PRESS_DP = 0.4f;
    private static final float TRANSLATION_Y_COMMIT_DP = -1.4f;
    private static final float ELEVATION_BASE_DP = 7.0f;
    private static final float ELEVATION_COMMIT_DP = 1.0f;
    private static final float ELEVATION_PRESS_DP = -0.1f;
    private static final float TRANSLATION_Z_BASE_DP = 3.8f;
    private static final float TRANSLATION_Z_COMMIT_DP = 0.8f;
    private static final float TRANSLATION_Z_PRESS_DP = -0.1f;

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
        this.settings = RuntimeDefaults.keyboardSettings(settings);
    }

    void show(View anchor, List<PreviewOverlaySpec> specs) {
        if (anchor == null || specs == null || specs.isEmpty()) {
            dismiss();
            return;
        }

        int[] windowLocation = windowLocation(anchor);
        int maxBottom = maxBottom(anchor, specs);
        int requiredTopPad = requiredTopPad(specs);
        int topPad = popup.isShowing() ? Math.max(requiredTopPad, topPadPx) : requiredTopPad;
        topPadPx = topPad;

        int popupWidth = Math.max(anchor.getWidth(), 1);
        int popupHeight = Math.max(1, topPad + maxBottom + SettingsRowBuilder.dp(context, 4));
        overlayContainer.setMinimumWidth(popupWidth);
        overlayContainer.setMinimumHeight(popupHeight);

        applyOverlaySpecs(specs, topPad);
        showOrUpdatePopup(anchor, windowLocation, topPad, popupWidth, popupHeight);
    }

    private int[] windowLocation(View anchor) {
        int[] windowLocation = new int[2];
        anchor.getLocationInWindow(windowLocation);
        return windowLocation;
    }

    private int maxBottom(View anchor, List<PreviewOverlaySpec> specs) {
        int maxBottom = anchor.getHeight();
        for (PreviewOverlaySpec spec : specs) {
            maxBottom = Math.max(maxBottom, spec.y + spec.height);
        }
        return maxBottom;
    }

    private int requiredTopPad(List<PreviewOverlaySpec> specs) {
        int requiredTopPad = SettingsRowBuilder.dp(context, TOP_RESERVE_DP);
        for (PreviewOverlaySpec spec : specs) {
            requiredTopPad = Math.max(
                    requiredTopPad,
                    Math.max(0, -spec.y) + SettingsRowBuilder.dp(context, 4));
        }
        return requiredTopPad;
    }

    private void applyOverlaySpecs(List<PreviewOverlaySpec> specs, int topPad) {
        for (int i = 0; i < specs.size(); i++) {
            applyOverlaySpec(i, specs.get(i), topPad);
        }
        hideUnusedOverlays(specs.size());
    }

    private void applyOverlaySpec(int index, PreviewOverlaySpec spec, int topPad) {
        TextView overlay = ensureOverlay(index);
        applySpec(overlay, spec);
        overlay.setLayoutParams(overlayLayoutParams(spec, topPad));
        overlay.setVisibility(View.VISIBLE);
    }

    private FrameLayout.LayoutParams overlayLayoutParams(PreviewOverlaySpec spec, int topPad) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(spec.width, spec.height);
        params.leftMargin = spec.x;
        params.topMargin = spec.y + topPad;
        return params;
    }

    private void hideUnusedOverlays(int visibleCount) {
        for (int i = visibleCount; i < overlayPool.size(); i++) {
            overlayPool.get(i).setVisibility(View.GONE);
        }
    }

    private void showOrUpdatePopup(
            View anchor,
            int[] windowLocation,
            int topPad,
            int popupWidth,
            int popupHeight) {
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
            overlay.setVisibility(View.GONE);
            overlay.setElevation(SettingsRowBuilder.dp(context, ELEVATION_BASE_DP));
            overlay.setTranslationZ(SettingsRowBuilder.dp(context, TRANSLATION_Z_BASE_DP));
            overlayPool.add(overlay);
            overlayContainer.addView(overlay, new FrameLayout.LayoutParams(1, 1));
        }
        return overlayPool.get(index);
    }

    private void applySpec(TextView overlay, PreviewOverlaySpec spec) {
        applyTextStyle(overlay, spec);
        applyTransform(overlay, spec);
        applyTypeface(overlay);
        applyBackground(overlay, spec);
    }

    private void applyTextStyle(TextView overlay, PreviewOverlaySpec spec) {
        overlay.setText(spec.label);
        overlay.setTextColor(spec.textColor);
        overlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, spec.textSizePx * spec.textScale);
        overlay.setAlpha(spec.alpha);
        overlay.setShadowLayer(
                SettingsRowBuilder.dp(context, shadowRadiusDp(spec)),
                0f,
                SettingsRowBuilder.dp(context, shadowDyDp(spec)),
                shadowColorFor(spec.textColor));
    }

    private void applyTransform(TextView overlay, PreviewOverlaySpec spec) {
        overlay.setPivotX(spec.width / 2f);
        overlay.setPivotY(spec.height);
        overlay.setScaleX(spec.scaleX);
        overlay.setScaleY(spec.scaleY);
        overlay.setTranslationY(SettingsRowBuilder.dp(context, translationYDp(spec)));
        overlay.setElevation(SettingsRowBuilder.dp(context, elevationDp(spec)));
        overlay.setTranslationZ(SettingsRowBuilder.dp(context, translationZDp(spec)));
    }

    private float shadowRadiusDp(PreviewOverlaySpec spec) {
        return SHADOW_RADIUS_BASE_DP
                + SHADOW_RADIUS_COMMIT_DP * spec.commitGlowAlpha
                + SHADOW_RADIUS_PRESS_DP * spec.inputImpactAlpha
                + SHADOW_RADIUS_TEXT_POP_DP * textPop(spec);
    }

    private float shadowDyDp(PreviewOverlaySpec spec) {
        return SHADOW_DY_BASE_DP
                + SHADOW_DY_COMMIT_DP * spec.commitGlowAlpha
                + SHADOW_DY_PRESS_DP * spec.inputImpactAlpha
                + SHADOW_DY_TEXT_POP_DP * textPop(spec);
    }

    private float translationYDp(PreviewOverlaySpec spec) {
        return TRANSLATION_Y_PRESS_DP * spec.inputImpactAlpha
                + TRANSLATION_Y_COMMIT_DP * spec.commitGlowAlpha;
    }

    private float elevationDp(PreviewOverlaySpec spec) {
        return ELEVATION_BASE_DP
                + ELEVATION_COMMIT_DP * spec.commitGlowAlpha
                + ELEVATION_PRESS_DP * spec.inputImpactAlpha;
    }

    private float translationZDp(PreviewOverlaySpec spec) {
        return TRANSLATION_Z_BASE_DP
                + TRANSLATION_Z_COMMIT_DP * spec.commitGlowAlpha
                + TRANSLATION_Z_PRESS_DP * spec.inputImpactAlpha;
    }

    private float textPop(PreviewOverlaySpec spec) {
        return Math.max(0f, spec.textScale - TEXT_POP_BASELINE);
    }

    private void applyTypeface(TextView overlay) {
        overlay.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                context,
                settings.fontFamily,
                settings.primaryTextBold,
                settings.primaryTextItalic));
    }

    private void applyBackground(TextView overlay, PreviewOverlaySpec spec) {
        if (spec.angularBubble) {
            int tailHeight = SettingsRowBuilder.dp(context, ANGULAR_TAIL_DP);
            overlay.setPadding(0, 0, 0, tailHeight);
            overlay.setBackground(new PreviewBubbleDrawable(
                    spec.backgroundColor,
                    spec.borderColor,
                    spec.borderWidthPx,
                    spec.cornerRadiusPx,
                    tailHeight,
                    spec.commitGlowAlpha,
                    spec.inputImpactAlpha));
        } else {
            overlay.setPadding(0, 0, 0, 0);
            overlay.setBackground(roundedBackground(spec));
        }
    }

    private GradientDrawable roundedBackground(PreviewOverlaySpec spec) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(spec.backgroundColor);
        background.setCornerRadius(spec.cornerRadiusPx);
        if (spec.borderWidthPx > 0) {
            background.setStroke(spec.borderWidthPx, spec.borderColor);
        }
        return background;
    }

    private int shadowColorFor(int textColor) {
        return KeyboardColorMath.perceivedLuminance(textColor) > 150 ? 0x66000000 : 0x44FFFFFF;
    }
}
