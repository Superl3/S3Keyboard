package com.superl3.s3keyboard;

import android.content.Context;

/** User-owned permission preference; intentionally separate from theme JSON. */
final class GlassBackdropPreferences {
    private static final String PREF_NAME = "keyboard_preferences";
    private static final String SOURCE_ENABLED = "accessibility_glass_source_enabled";

    private GlassBackdropPreferences() {
    }

    static boolean isSourceEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(SOURCE_ENABLED, false);
    }

    static void setSourceEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(SOURCE_ENABLED, enabled)
                .apply();
    }
}
