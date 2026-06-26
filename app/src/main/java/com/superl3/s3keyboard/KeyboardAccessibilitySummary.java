package com.superl3.s3keyboard;

import android.content.Context;

final class KeyboardAccessibilitySummary {
    private KeyboardAccessibilitySummary() {
    }

    static String describe(
            Context context,
            KeyboardSettings settings,
            KeyboardSurface surface,
            int keyCount,
            boolean debugKeyBoundsOverlayEnabled) {
        if (context == null) {
            return describe(settings, surface, keyCount, debugKeyBoundsOverlayEnabled);
        }
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        KeyboardSurface safeSurface = surface == null ? KeyboardSurface.NORMAL : surface;
        StringBuilder builder = new StringBuilder(context.getString(R.string.keyboard_accessibility_name));
        builder.append(", ");
        if (safeSurface != KeyboardSurface.NORMAL) {
            builder.append(surfaceLabel(context, safeSurface));
        } else if (safeSettings.keyboardMode == KeyboardMode.ENGLISH) {
            builder.append(context.getString(R.string.keyboard_accessibility_mode_qwerty));
        } else {
            builder.append(context.getString(R.string.keyboard_accessibility_mode_dingul));
        }
        builder.append(", ")
                .append(context.getString(
                        R.string.keyboard_accessibility_key_count,
                        Math.max(0, keyCount)));
        if (safeSettings.remoteModeEnabled) {
            builder.append(", ")
                    .append(context.getString(R.string.keyboard_accessibility_remote_mode));
        }
        if (safeSettings.showBeginnerTooltipPreview) {
            builder.append(", ")
                    .append(context.getString(R.string.keyboard_accessibility_preview_on));
        }
        if (debugKeyBoundsOverlayEnabled) {
            builder.append(", ")
                    .append(context.getString(R.string.keyboard_accessibility_debug_overlay_on));
        }
        return builder.toString();
    }

    static String describe(
            KeyboardSettings settings,
            KeyboardSurface surface,
            int keyCount,
            boolean debugKeyBoundsOverlayEnabled) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        KeyboardSurface safeSurface = surface == null ? KeyboardSurface.NORMAL : surface;
        StringBuilder builder = new StringBuilder("New Dingul keyboard");
        builder.append(", ");
        if (safeSurface != KeyboardSurface.NORMAL) {
            builder.append(surfaceLabel(safeSurface));
        } else if (safeSettings.keyboardMode == KeyboardMode.ENGLISH) {
            builder.append("English QWERTY");
        } else {
            builder.append("Hangul Dingul");
        }
        builder.append(", keys ").append(Math.max(0, keyCount));
        if (safeSettings.remoteModeEnabled) {
            builder.append(", remote mode");
        }
        if (safeSettings.showBeginnerTooltipPreview) {
            builder.append(", input preview on");
        }
        if (debugKeyBoundsOverlayEnabled) {
            builder.append(", key bounds overlay on");
        }
        return builder.toString();
    }

    private static String surfaceLabel(Context context, KeyboardSurface surface) {
        switch (surface) {
            case RAW:
                return context.getString(R.string.keyboard_surface_raw);
            case PASSWORD_SAFE:
                return context.getString(R.string.keyboard_surface_password);
            case NUMPAD:
            case PINPAD:
                return context.getString(R.string.keyboard_surface_number);
            case PHONEPAD:
                return context.getString(R.string.keyboard_surface_phone);
            case DATEPAD:
                return context.getString(R.string.keyboard_surface_date);
            case URL_EXTENDED:
                return context.getString(R.string.keyboard_surface_url);
            case EMAIL_EXTENDED:
                return context.getString(R.string.keyboard_surface_email);
            case WEB_EXTENDED:
                return context.getString(R.string.keyboard_surface_web);
            case SEARCH_EXTENDED:
                return context.getString(R.string.keyboard_surface_search);
            case MULTILINE_EXTENDED:
                return context.getString(R.string.keyboard_surface_multiline);
            case NORMAL:
            default:
                return context.getString(R.string.keyboard_surface_normal);
        }
    }

    private static String surfaceLabel(KeyboardSurface surface) {
        switch (surface) {
            case RAW:
                return "raw input";
            case PASSWORD_SAFE:
                return "password input";
            case NUMPAD:
            case PINPAD:
                return "number input";
            case PHONEPAD:
                return "phone input";
            case DATEPAD:
                return "date input";
            case URL_EXTENDED:
                return "URL input";
            case EMAIL_EXTENDED:
                return "email input";
            case WEB_EXTENDED:
                return "web input";
            case SEARCH_EXTENDED:
                return "search input";
            case MULTILINE_EXTENDED:
                return "multiline input";
            case NORMAL:
            default:
                return "normal input";
        }
    }
}
