package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;

final class KeyboardPreviewFactory {
    private KeyboardPreviewFactory() {
    }

    static HangulKeyboardView nonInteractive(Context context, KeyboardSettings settings) {
        HangulKeyboardView preview = new HangulKeyboardView(context, true);
        preview.setCompactPreviewRendering(true);
        preview.setSettings(RuntimeDefaults.keyboardSettings(settings));
        preview.setEnabled(false);
        preview.setFocusable(false);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return preview;
    }
}
