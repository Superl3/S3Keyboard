package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;

final class KeyboardPreviewFactory {
    private KeyboardPreviewFactory() {
    }

    static HangulKeyboardView nonInteractive(Context context, KeyboardSettings settings) {
        HangulKeyboardView preview = new HangulKeyboardView(context);
        preview.setCompactPreviewRendering(true);
        preview.setSettings(settings == null ? KeyboardSettings.defaults() : settings);
        preview.setClickable(true);
        preview.setFocusable(false);
        preview.setOnTouchListener((v, event) -> true);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }
}
