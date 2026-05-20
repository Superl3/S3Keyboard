package com.superl3.s3keyboard;

final class ModifierIconCatalog {
    static final String PACK_THEME_DEFAULT = "";
    static final String PACK_LINE_MONO = "line-mono";
    static final String PACK_ACCENT_COLOR = "accent-color";
    static final String PACK_DOTS_LINES = "dots-lines";
    static final String PACK_METROPOLIS_POINTS = "metropolis-points";
    static final String GLYPH_DOT = "dot";

    private static final int INTRINSIC_ACCENT_COLOR = 0xFF06B6D4;

    private ModifierIconCatalog() {
    }

    static String normalizePackId(String packId) {
        if (PACK_ACCENT_COLOR.equals(packId)
                || PACK_DOTS_LINES.equals(packId)
                || PACK_METROPOLIS_POINTS.equals(packId)) {
            return packId;
        }
        return PACK_LINE_MONO;
    }

    static String effectivePackId(KeyboardSettings settings) {
        if (settings == null) {
            return PACK_LINE_MONO;
        }
        if (settings.modifierIconOverridePackId != null && !settings.modifierIconOverridePackId.isEmpty()) {
            return normalizePackId(settings.modifierIconOverridePackId);
        }
        return normalizePackId(settings.modifierIconThemePackId);
    }

    static int colorForEffectivePack(KeyboardSettings settings, int foregroundColor) {
        return colorForPack(effectivePackId(settings), foregroundColor);
    }

    static int colorForPack(String packId, int foregroundColor) {
        return isColoredPack(packId)
                ? INTRINSIC_ACCENT_COLOR
                : foregroundColor;
    }

    static boolean isColoredPack(String packId) {
        String normalized = normalizePackId(packId);
        return PACK_ACCENT_COLOR.equals(normalized) || PACK_METROPOLIS_POINTS.equals(normalized);
    }

    static boolean rendersCustomGlyphs(String packId) {
        String normalized = normalizePackId(packId);
        return PACK_DOTS_LINES.equals(normalized)
                || PACK_METROPOLIS_POINTS.equals(normalized);
    }

    static boolean isDotsLinePack(String packId) {
        return PACK_DOTS_LINES.equals(normalizePackId(packId));
    }

    static boolean isMetropolisPack(String packId) {
        return PACK_METROPOLIS_POINTS.equals(normalizePackId(packId));
    }

    static int metropolisColorFor(int icon) {
        switch (icon) {
            case KeyIcon.OPTIONS:
            case KeyIcon.BACKSPACE:
                return 0xFFFFB000;
            case KeyIcon.SHIFT:
            case KeyIcon.CAPS_LOCK:
            case KeyIcon.HIDE:
            case KeyIcon.MOVE_LEFT:
            case KeyIcon.MOVE_RIGHT:
            case KeyIcon.RESET:
            case KeyIcon.RESERVED:
                return 0xFFFF4B3E;
            case KeyIcon.ENTER:
            case KeyIcon.DONE:
            case KeyIcon.NEXT:
            case KeyIcon.LANGUAGE:
            case KeyIcon.SEARCH:
            case KeyIcon.SETTINGS:
            case KeyIcon.KEYBOARD:
                return 0xFF66E3C4;
            case KeyIcon.SPACE:
            default:
                return 0xFF70D7E8;
        }
    }

    static String displayName(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_ACCENT_COLOR.equals(normalized)) {
            return "Accent Color";
        }
        if (PACK_DOTS_LINES.equals(normalized)) {
            return "Dots Lines";
        }
        if (PACK_METROPOLIS_POINTS.equals(normalized)) {
            return "Metropolis Points";
        }
        return "Line Mono";
    }

    static String[] selectablePackIds(boolean includeThemeDefault) {
        if (includeThemeDefault) {
            return new String[] {
                    PACK_THEME_DEFAULT,
                    PACK_LINE_MONO,
                    PACK_ACCENT_COLOR,
                    PACK_DOTS_LINES,
                    PACK_METROPOLIS_POINTS
            };
        }
        return new String[] {
                PACK_LINE_MONO,
                PACK_ACCENT_COLOR,
                PACK_DOTS_LINES,
                PACK_METROPOLIS_POINTS
        };
    }
}
