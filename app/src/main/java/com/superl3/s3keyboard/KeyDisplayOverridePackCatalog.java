package com.superl3.s3keyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class KeyDisplayOverridePackCatalog {
    static final String PACK_THEME_DEFAULT = "";
    static final String PACK_NONE = "none";
    static final String PACK_SIMPLE_TEXT = "simple-text";
    static final String PACK_GIT_COMMANDS = "git-commands";
    static final String PACK_GEO_POINTS = "geo-points";
    static final String PACK_SOFT_SYMBOLS = "soft-symbols";
    static final String PACK_TERMINAL_POINTS = "terminal-points";
    static final String PACK_PUNCTUATION_POINTS = "punctuation-points";
    static final String PACK_FULL_DECORATIVE = "full-decorative";
    static final String PACK_KEYBOARD_SYMBOLS = "keyboard-symbols";
    static final String PACK_KEYBOARD_NAVIGATION = "keyboard-navigation";
    static final String PACK_GMK_STYLE_POINTS = "gmk-style-points";
    static final String PACK_GMK_STYLE_NOVELTIES = "gmk-style-novelties";
    static final String PACK_GMK_STYLE_MACROS = "gmk-style-macros";
    static final String PACK_GMK_STYLE_CELESTIAL = "gmk-style-celestial";
    static final String PACK_GMK_STYLE_NATURE = "gmk-style-nature";
    static final String PACK_GMK_STYLE_SPACEBARS = "gmk-style-spacebars";
    static final String PACK_FONT_SYMBOLS = "font-symbols";
    static final String PACK_IMAGE_MASK_MARKS = "image-mask-marks";
    static final String PACK_TALL_MOD_GLYPHS = "tall-mod-glyphs";
    static final String PACK_MIXED_SOURCE_NOVELTIES = "mixed-source-novelties";
    static final String LEGACY_PACK_OLIVIA_SCRIPT_TEXT = "olivia-script-text";

    private static final Map<String, KeyDisplayOverride> SIMPLE_TEXT_OVERRIDES =
            createSimpleTextOverrides();
    private static final Map<String, KeyDisplayOverride> GIT_COMMAND_OVERRIDES =
            createGitCommandOverrides();
    private static final Map<String, KeyDisplayOverride> GEO_POINT_OVERRIDES =
            createGeoPointOverrides();
    private static final Map<String, KeyDisplayOverride> SOFT_SYMBOL_OVERRIDES =
            createSoftSymbolOverrides();
    private static final Map<String, KeyDisplayOverride> TERMINAL_POINT_OVERRIDES =
            createTerminalPointOverrides();
    private static final Map<String, KeyDisplayOverride> PUNCTUATION_POINT_OVERRIDES =
            createPunctuationPointOverrides();
    private static final Map<String, KeyDisplayOverride> FULL_DECORATIVE_OVERRIDES =
            createFullDecorativeOverrides();
    private static final Map<String, KeyDisplayOverride> KEYBOARD_SYMBOL_OVERRIDES =
            createKeyboardSymbolOverrides();
    private static final Map<String, KeyDisplayOverride> KEYBOARD_NAVIGATION_OVERRIDES =
            createKeyboardNavigationOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_POINT_OVERRIDES =
            createGmkStylePointOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_NOVELTY_OVERRIDES =
            createGmkStyleNoveltyOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_MACRO_OVERRIDES =
            createGmkStyleMacroOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_CELESTIAL_OVERRIDES =
            createGmkStyleCelestialOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_NATURE_OVERRIDES =
            createGmkStyleNatureOverrides();
    private static final Map<String, KeyDisplayOverride> GMK_STYLE_SPACEBAR_OVERRIDES =
            createGmkStyleSpacebarOverrides();
    private static final Map<String, KeyDisplayOverride> FONT_SYMBOL_OVERRIDES =
            createFontSymbolOverrides();
    private static final Map<String, KeyDisplayOverride> IMAGE_MASK_MARK_OVERRIDES =
            createImageMaskMarkOverrides();
    private static final Map<String, KeyDisplayOverride> TALL_MOD_GLYPH_OVERRIDES =
            createTallModGlyphOverrides();
    private static final Map<String, KeyDisplayOverride> MIXED_SOURCE_NOVELTY_OVERRIDES =
            createMixedSourceNoveltyOverrides();

    private KeyDisplayOverridePackCatalog() {
    }

    static String normalizePackId(String packId) {
        if (PACK_SIMPLE_TEXT.equals(packId) || LEGACY_PACK_OLIVIA_SCRIPT_TEXT.equals(packId)) {
            return PACK_SIMPLE_TEXT;
        }
        if (PACK_GIT_COMMANDS.equals(packId)) {
            return PACK_GIT_COMMANDS;
        }
        if (PACK_GEO_POINTS.equals(packId)) {
            return PACK_GEO_POINTS;
        }
        if (PACK_SOFT_SYMBOLS.equals(packId)) {
            return PACK_SOFT_SYMBOLS;
        }
        if (PACK_TERMINAL_POINTS.equals(packId)) {
            return PACK_TERMINAL_POINTS;
        }
        if (PACK_PUNCTUATION_POINTS.equals(packId)) {
            return PACK_PUNCTUATION_POINTS;
        }
        if (PACK_FULL_DECORATIVE.equals(packId)) {
            return PACK_FULL_DECORATIVE;
        }
        if (PACK_KEYBOARD_SYMBOLS.equals(packId)) {
            return PACK_KEYBOARD_SYMBOLS;
        }
        if (PACK_KEYBOARD_NAVIGATION.equals(packId)) {
            return PACK_KEYBOARD_NAVIGATION;
        }
        if (PACK_GMK_STYLE_POINTS.equals(packId)) {
            return PACK_GMK_STYLE_POINTS;
        }
        if (PACK_GMK_STYLE_NOVELTIES.equals(packId)) {
            return PACK_GMK_STYLE_NOVELTIES;
        }
        if (PACK_GMK_STYLE_MACROS.equals(packId)) {
            return PACK_GMK_STYLE_MACROS;
        }
        if (PACK_GMK_STYLE_CELESTIAL.equals(packId)) {
            return PACK_GMK_STYLE_CELESTIAL;
        }
        if (PACK_GMK_STYLE_NATURE.equals(packId)) {
            return PACK_GMK_STYLE_NATURE;
        }
        if (PACK_GMK_STYLE_SPACEBARS.equals(packId)) {
            return PACK_GMK_STYLE_SPACEBARS;
        }
        if (PACK_FONT_SYMBOLS.equals(packId)) {
            return PACK_FONT_SYMBOLS;
        }
        if (PACK_IMAGE_MASK_MARKS.equals(packId)) {
            return PACK_IMAGE_MASK_MARKS;
        }
        if (PACK_TALL_MOD_GLYPHS.equals(packId)) {
            return PACK_TALL_MOD_GLYPHS;
        }
        if (PACK_MIXED_SOURCE_NOVELTIES.equals(packId)) {
            return PACK_MIXED_SOURCE_NOVELTIES;
        }
        return PACK_NONE;
    }

    static String normalizeOverridePackId(String packId) {
        if (packId == null || packId.isEmpty() || PACK_THEME_DEFAULT.equals(packId)) {
            return PACK_THEME_DEFAULT;
        }
        return normalizePackId(packId);
    }

    static String effectivePackId(KeyboardSettings settings) {
        if (settings == null) {
            return PACK_NONE;
        }
        if (settings.keyDisplayOverridePackId != null && !settings.keyDisplayOverridePackId.isEmpty()) {
            return normalizePackId(settings.keyDisplayOverridePackId);
        }
        return normalizePackId(settings.keyDisplayThemePackId);
    }

    static boolean isKnownNonEmptyPackId(String packId) {
        String normalized = normalizePackId(packId);
        return PACK_SIMPLE_TEXT.equals(normalized)
                || PACK_GIT_COMMANDS.equals(normalized)
                || PACK_GEO_POINTS.equals(normalized)
                || PACK_SOFT_SYMBOLS.equals(normalized)
                || PACK_TERMINAL_POINTS.equals(normalized)
                || PACK_PUNCTUATION_POINTS.equals(normalized)
                || PACK_FULL_DECORATIVE.equals(normalized)
                || PACK_KEYBOARD_SYMBOLS.equals(normalized)
                || PACK_KEYBOARD_NAVIGATION.equals(normalized)
                || PACK_GMK_STYLE_POINTS.equals(normalized)
                || PACK_GMK_STYLE_NOVELTIES.equals(normalized)
                || PACK_GMK_STYLE_MACROS.equals(normalized)
                || PACK_GMK_STYLE_CELESTIAL.equals(normalized)
                || PACK_GMK_STYLE_NATURE.equals(normalized)
                || PACK_GMK_STYLE_SPACEBARS.equals(normalized)
                || PACK_FONT_SYMBOLS.equals(normalized)
                || PACK_IMAGE_MASK_MARKS.equals(normalized)
                || PACK_TALL_MOD_GLYPHS.equals(normalized)
                || PACK_MIXED_SOURCE_NOVELTIES.equals(normalized);
    }

    static boolean isSimpleTextPack(String packId) {
        return PACK_SIMPLE_TEXT.equals(normalizePackId(packId));
    }

    static boolean shouldRenderSimpleText(KeyboardSettings settings, KeyDisplayOverride override) {
        return override != null
                && override.isText()
                && isSimpleTextPack(effectivePackId(settings));
    }

    static Map<String, KeyDisplayOverride> overridesForEffectivePack(KeyboardSettings settings) {
        return overridesForPack(effectivePackId(settings));
    }

    static Map<String, KeyDisplayOverride> overridesForPack(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_SIMPLE_TEXT.equals(normalized)) {
            return SIMPLE_TEXT_OVERRIDES;
        }
        if (PACK_GIT_COMMANDS.equals(normalized)) {
            return GIT_COMMAND_OVERRIDES;
        }
        if (PACK_GEO_POINTS.equals(normalized)) {
            return GEO_POINT_OVERRIDES;
        }
        if (PACK_SOFT_SYMBOLS.equals(normalized)) {
            return SOFT_SYMBOL_OVERRIDES;
        }
        if (PACK_TERMINAL_POINTS.equals(normalized)) {
            return TERMINAL_POINT_OVERRIDES;
        }
        if (PACK_PUNCTUATION_POINTS.equals(normalized)) {
            return PUNCTUATION_POINT_OVERRIDES;
        }
        if (PACK_FULL_DECORATIVE.equals(normalized)) {
            return FULL_DECORATIVE_OVERRIDES;
        }
        if (PACK_KEYBOARD_SYMBOLS.equals(normalized)) {
            return KEYBOARD_SYMBOL_OVERRIDES;
        }
        if (PACK_KEYBOARD_NAVIGATION.equals(normalized)) {
            return KEYBOARD_NAVIGATION_OVERRIDES;
        }
        if (PACK_GMK_STYLE_POINTS.equals(normalized)) {
            return GMK_STYLE_POINT_OVERRIDES;
        }
        if (PACK_GMK_STYLE_NOVELTIES.equals(normalized)) {
            return GMK_STYLE_NOVELTY_OVERRIDES;
        }
        if (PACK_GMK_STYLE_MACROS.equals(normalized)) {
            return GMK_STYLE_MACRO_OVERRIDES;
        }
        if (PACK_GMK_STYLE_CELESTIAL.equals(normalized)) {
            return GMK_STYLE_CELESTIAL_OVERRIDES;
        }
        if (PACK_GMK_STYLE_NATURE.equals(normalized)) {
            return GMK_STYLE_NATURE_OVERRIDES;
        }
        if (PACK_GMK_STYLE_SPACEBARS.equals(normalized)) {
            return GMK_STYLE_SPACEBAR_OVERRIDES;
        }
        if (PACK_FONT_SYMBOLS.equals(normalized)) {
            return FONT_SYMBOL_OVERRIDES;
        }
        if (PACK_IMAGE_MASK_MARKS.equals(normalized)) {
            return IMAGE_MASK_MARK_OVERRIDES;
        }
        if (PACK_TALL_MOD_GLYPHS.equals(normalized)) {
            return TALL_MOD_GLYPH_OVERRIDES;
        }
        if (PACK_MIXED_SOURCE_NOVELTIES.equals(normalized)) {
            return MIXED_SOURCE_NOVELTY_OVERRIDES;
        }
        return Collections.emptyMap();
    }

    static String displayName(String packId) {
        String normalized = normalizePackId(packId);
        if (PACK_SIMPLE_TEXT.equals(normalized)) {
            return "Simple Text";
        }
        if (PACK_GIT_COMMANDS.equals(normalized)) {
            return "Git Commands";
        }
        if (PACK_GEO_POINTS.equals(normalized)) {
            return "Geo Points";
        }
        if (PACK_SOFT_SYMBOLS.equals(normalized)) {
            return "Soft Symbols";
        }
        if (PACK_TERMINAL_POINTS.equals(normalized)) {
            return "Terminal Points";
        }
        if (PACK_PUNCTUATION_POINTS.equals(normalized)) {
            return "Punctuation Points";
        }
        if (PACK_FULL_DECORATIVE.equals(normalized)) {
            return "Full Decorative";
        }
        if (PACK_KEYBOARD_SYMBOLS.equals(normalized)) {
            return "Keyboard Symbols";
        }
        if (PACK_KEYBOARD_NAVIGATION.equals(normalized)) {
            return "Keyboard Navigation";
        }
        if (PACK_GMK_STYLE_POINTS.equals(normalized)) {
            return "GMK Style Points";
        }
        if (PACK_GMK_STYLE_NOVELTIES.equals(normalized)) {
            return "GMK Style Novelties";
        }
        if (PACK_GMK_STYLE_MACROS.equals(normalized)) {
            return "GMK Style Macros";
        }
        if (PACK_GMK_STYLE_CELESTIAL.equals(normalized)) {
            return "GMK Style Celestial";
        }
        if (PACK_GMK_STYLE_NATURE.equals(normalized)) {
            return "GMK Style Nature";
        }
        if (PACK_GMK_STYLE_SPACEBARS.equals(normalized)) {
            return "GMK Style Spacebars";
        }
        if (PACK_FONT_SYMBOLS.equals(normalized)) {
            return "Font Symbols";
        }
        if (PACK_IMAGE_MASK_MARKS.equals(normalized)) {
            return "Image Mask Marks";
        }
        if (PACK_TALL_MOD_GLYPHS.equals(normalized)) {
            return "Tall Mod Glyphs";
        }
        if (PACK_MIXED_SOURCE_NOVELTIES.equals(normalized)) {
            return "Mixed Source Novelties";
        }
        return "None";
    }

    static String[] selectablePackIds(boolean includeThemeDefault) {
        if (includeThemeDefault) {
            return new String[] {
                    PACK_THEME_DEFAULT,
                    PACK_NONE,
                    PACK_SIMPLE_TEXT,
                    PACK_GIT_COMMANDS,
                    PACK_GEO_POINTS,
                    PACK_SOFT_SYMBOLS,
                    PACK_TERMINAL_POINTS,
                    PACK_PUNCTUATION_POINTS,
                    PACK_FULL_DECORATIVE,
                    PACK_KEYBOARD_SYMBOLS,
                    PACK_KEYBOARD_NAVIGATION,
                    PACK_GMK_STYLE_POINTS,
                    PACK_GMK_STYLE_NOVELTIES,
                    PACK_GMK_STYLE_MACROS,
                    PACK_GMK_STYLE_CELESTIAL,
                    PACK_GMK_STYLE_NATURE,
                    PACK_GMK_STYLE_SPACEBARS,
                    PACK_FONT_SYMBOLS,
                    PACK_IMAGE_MASK_MARKS,
                    PACK_TALL_MOD_GLYPHS,
                    PACK_MIXED_SOURCE_NOVELTIES
            };
        }
        return new String[] {
                PACK_NONE,
                PACK_SIMPLE_TEXT,
                PACK_GIT_COMMANDS,
                PACK_GEO_POINTS,
                PACK_SOFT_SYMBOLS,
                PACK_TERMINAL_POINTS,
                PACK_PUNCTUATION_POINTS,
                PACK_FULL_DECORATIVE,
                PACK_KEYBOARD_SYMBOLS,
                PACK_KEYBOARD_NAVIGATION,
                PACK_GMK_STYLE_POINTS,
                PACK_GMK_STYLE_NOVELTIES,
                PACK_GMK_STYLE_MACROS,
                PACK_GMK_STYLE_CELESTIAL,
                PACK_GMK_STYLE_NATURE,
                PACK_GMK_STYLE_SPACEBARS,
                PACK_FONT_SYMBOLS,
                PACK_IMAGE_MASK_MARKS,
                PACK_TALL_MOD_GLYPHS,
                PACK_MIXED_SOURCE_NOVELTIES
        };
    }

    private static Map<String, KeyDisplayOverride> createSimpleTextOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putText(overrides, "label:.", "hihihi");
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGitCommandOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putIconText(overrides, KeyIcon.ENTER, "exec");
        putIconText(overrides, KeyIcon.DONE, "exec");
        putIconText(overrides, KeyIcon.NEXT, "exec");
        putText(overrides, "enter", "exec");
        putIconText(overrides, KeyIcon.BACKSPACE, "reset");
        putText(overrides, "backspace", "reset");
        putIconText(overrides, KeyIcon.SHIFT, "rebase");
        putIconText(overrides, KeyIcon.CAPS_LOCK, "rebase");
        putText(overrides, "shift", "rebase");
        putIconText(overrides, KeyIcon.SPACE, "pull");
        putText(overrides, "space", "pull");
        putIconText(overrides, KeyIcon.LANGUAGE, "fetch");
        putText(overrides, "language", "fetch");
        putIconText(overrides, KeyIcon.OPTIONS, "stash");
        putText(overrides, "options", "stash");
        putIconText(overrides, KeyIcon.SETTINGS, "config");
        putText(overrides, "settings", "config");
        putIconText(overrides, KeyIcon.RESERVED, "commit");
        putText(overrides, "reserved", "commit");
        putIconText(overrides, KeyIcon.SEARCH, "grep");
        putIconText(overrides, KeyIcon.HIDE, "hide");
        putIconText(overrides, KeyIcon.KEYBOARD, "branch");
        putIconText(overrides, KeyIcon.MOVE_LEFT, "prev");
        putIconText(overrides, KeyIcon.MOVE_RIGHT, "next");
        putIconText(overrides, KeyIcon.RESET, "reset");
        putText(overrides, ".", "diff");
        putText(overrides, "/", "log");
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGeoPointOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_SPARK, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_SPARK);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_CHEVRON_LEFT, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_CHEVRON_UP, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_CHEVRON_UP);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_SPACE_DOTS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_ORBIT, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GRID_4, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GEAR_DOT, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_BOOKMARK_DOT, "reserved");
        putCommandIcons(overrides, KeyIcon.SEARCH, DecorativeGlyphCatalog.GLYPH_RING);
        putCommandIcons(overrides, KeyIcon.HIDE, DecorativeGlyphCatalog.GLYPH_CROSS);
        putCommandIcons(overrides, KeyIcon.KEYBOARD, DecorativeGlyphCatalog.GLYPH_SQUARE);
        putCommandIcons(overrides, KeyIcon.MOVE_LEFT, DecorativeGlyphCatalog.GLYPH_CHEVRON_LEFT);
        putCommandIcons(overrides, KeyIcon.MOVE_RIGHT, DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT);
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_RING);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createSoftSymbolOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_RING, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_RING);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_CROSS, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_PLUS, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_PLUS);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_SPACE_DOTS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_RING, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GRID_4, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_SQUARE, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_DIAMOND, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_DIAMOND);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createTerminalPointOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_TERMINAL, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_TERMINAL);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_CROSS, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_CHEVRON_UP, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_CHEVRON_UP);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_CURSOR, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_ORBIT, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_TERMINAL, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GRID_4, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_SQUARE, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_CURSOR);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createPunctuationPointOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_SPARK, "enter");
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_CHEVRON_LEFT, "backspace");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_RING);
        putIconOverride(overrides, "label:.", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "label:/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "label:?", DecorativeGlyphCatalog.GLYPH_RING);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createFullDecorativeOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>(createGeoPointOverrides());
        putIconOverride(overrides, "alpha", ModifierIconCatalog.GLYPH_DOT);
        putIconOverride(overrides, "modifiers", DecorativeGlyphCatalog.GLYPH_RING);
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_SPARK, "enter");
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_CHEVRON_LEFT, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_CHEVRON_UP, "shift");
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_SPACE_DOTS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_ORBIT, "language");
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createKeyboardSymbolOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_TAB);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_BACKSPACE, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_CAPSLOCK, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_KEYBOARD_CAPSLOCK);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_SPACE, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_LANGUAGE, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_KEYBOARD_OPTION, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_KEYBOARD_COMMAND, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_KEYBOARD_CONTROL, "reserved");
        putCommandIcons(overrides, KeyIcon.HIDE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_HIDE);
        putCommandIcons(overrides, KeyIcon.KEYBOARD, DecorativeGlyphCatalog.GLYPH_KEYBOARD_FULL);
        putCommandIcons(overrides, KeyIcon.MOVE_LEFT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_LEFT);
        putCommandIcons(overrides, KeyIcon.MOVE_RIGHT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_RIGHT);
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_TWO_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_SLASH_DOT);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_KEYBOARD_KEYS);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createKeyboardNavigationOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN, "enter");
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_TAB);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_LEFT, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_UP, "shift");
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_SPACE, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_KEYBOARD_LANGUAGE, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_LEFT, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_RIGHT, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_KEYBOARD_CONTROL, "reserved");
        putCommandIcons(overrides, KeyIcon.MOVE_LEFT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_LEFT);
        putCommandIcons(overrides, KeyIcon.MOVE_RIGHT, DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_RIGHT);
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_DOWN);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_UP);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_KEYBOARD_TAB);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStylePointOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_BAR, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_BAR);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_TWIN_TICKS);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_CORNER, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_STRIPE, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_STRIPE);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_ORBIT_STAR, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_STACK, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_TARGET, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_DIAMOND_CLUSTER, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_TRIPLE_DOT);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_TWIN_TICKS);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_TARGET);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStyleNoveltyOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_SUN, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_SUN);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_WAVE);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_MOON, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_MOUNTAIN, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_MOUNTAIN);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_LEAF, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_FLOWER, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_ORBIT_STAR, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_DROPLET, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_TRIPLE_DOT);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_WAVE);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_FLOWER);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStyleMacroOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_BRACKETS, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_BRACKETS);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_PIXEL_STEPS);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_PULSE, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_STACK, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_STACK);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_ORBIT_STAR, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_MACRO_STACK, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_TARGET, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_PIXEL_STEPS, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_TRIPLE_DOT);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_STRIPE);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_MACRO_BRACKETS);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStyleCelestialOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_PLANET_RING, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_PLANET_RING);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_COMET_TAIL);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_CRESCENT_STAR, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_CONSTELLATION, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_CONSTELLATION);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_ORBIT_STAR, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_SPARKLE_PAIR, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_COMPASS, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_SNOW, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_TRIPLE_DOT);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_COMET_TAIL);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_PLANET_RING);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStyleNatureOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_FLOWER_ALT, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_FLOWER_ALT);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_LEAF_PAIR);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_CLOUD, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_SPROUT, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_SPROUT);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_WAVE_DOUBLE, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_LEAF, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_PETALS, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_RAIN, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_FLAME, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_DROPLET);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_WAVE_DOUBLE);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_SPROUT);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createGmkStyleSpacebarOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_GMK_ISO_ENTER_MARK, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_GMK_ISO_ENTER_MARK);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_GMK_LONG_BAR_TICKS);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_GMK_SIDE_STRIPES, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_GMK_STEPPED_BAR, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_GMK_STEPPED_BAR);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_GMK_SPLIT_BAR, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_GMK_CORNER_DOTS, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_GMK_EQUALIZER, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_GMK_RISING_BLOCKS, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_GMK_ARCADE_DIAMOND, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_GMK_DOT_MATRIX);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_GMK_CENTER_CROSS);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_GMK_LAB_FLASK);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createFontSymbolOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_FONT_TAB_ARROW);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_FONT_DELETE_LEFT, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_FONT_KEYBOARD, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_FONT_COMMAND, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_FONT_OPTION, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_FONT_CONTROL, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_FONT_ESCAPE, "reserved");
        putCommandIcons(overrides, KeyIcon.MOVE_LEFT, DecorativeGlyphCatalog.GLYPH_FONT_REWIND);
        putCommandIcons(overrides, KeyIcon.MOVE_RIGHT, DecorativeGlyphCatalog.GLYPH_FONT_FAST_FORWARD);
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_FONT_STAR_OUTLINE);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_FONT_TRIANGLE_UP);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_FONT_POWER);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createImageMaskMarkOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_IMG_SIDE_NOTCH, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_IMG_TALL_ORBIT, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_IMG_PUNCH_CARD, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_IMG_LADDER, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_IMG_TICKET, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_IMG_CAPSULE_DOTS);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_IMG_WAVE_TILE);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_IMG_BLOB_STAR);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createTallModGlyphOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_IMG_TALL_BRACKET, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_IMG_VERTICAL_RIBBON, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_IMG_VERTICAL_RIBBON);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_IMG_DUAL_POSTS, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_IMG_STACKED_TILES, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_IMG_CORNER_FRAME, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_IMG_TALL_CAPSULE, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_IMG_DIAMOND_STACK);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_IMG_SOFT_CROSS);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_IMG_PIN_DROP);
        return Collections.unmodifiableMap(overrides);
    }

    private static Map<String, KeyDisplayOverride> createMixedSourceNoveltyOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        putCommandIcons(overrides, KeyIcon.ENTER, DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW, "enter");
        putCommandIcons(overrides, KeyIcon.DONE, DecorativeGlyphCatalog.GLYPH_FONT_RETURN_ARROW);
        putCommandIcons(overrides, KeyIcon.NEXT, DecorativeGlyphCatalog.GLYPH_FONT_FAST_FORWARD);
        putCommandIcons(overrides, KeyIcon.BACKSPACE, DecorativeGlyphCatalog.GLYPH_FONT_DELETE_LEFT, "backspace");
        putCommandIcons(overrides, KeyIcon.SHIFT, DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW, "shift");
        putCommandIcons(overrides, KeyIcon.CAPS_LOCK, DecorativeGlyphCatalog.GLYPH_FONT_SHIFT_ARROW);
        putCommandIcons(overrides, KeyIcon.SPACE, DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS, "space");
        putCommandIcons(overrides, KeyIcon.LANGUAGE, DecorativeGlyphCatalog.GLYPH_IMG_TALL_ORBIT, "language");
        putCommandIcons(overrides, KeyIcon.OPTIONS, DecorativeGlyphCatalog.GLYPH_IMG_PUNCH_CARD, "options");
        putCommandIcons(overrides, KeyIcon.SETTINGS, DecorativeGlyphCatalog.GLYPH_FONT_HOME, "settings");
        putCommandIcons(overrides, KeyIcon.RESERVED, DecorativeGlyphCatalog.GLYPH_IMG_BLOB_STAR, "reserved");
        putIconOverride(overrides, ".", DecorativeGlyphCatalog.GLYPH_FONT_STAR_SOLID);
        putIconOverride(overrides, "/", DecorativeGlyphCatalog.GLYPH_IMG_LEAF_SLAB);
        putIconOverride(overrides, "?", DecorativeGlyphCatalog.GLYPH_FONT_EJECT);
        return Collections.unmodifiableMap(overrides);
    }

    private static void putIconText(Map<String, KeyDisplayOverride> overrides, int icon, String text) {
        putText(overrides, "icon:" + icon, text);
    }

    private static void putText(Map<String, KeyDisplayOverride> overrides, String key, String text) {
        overrides.put(KeyboardSettings.normalizeKeyOverrideName(key), KeyDisplayOverride.text(text));
    }

    private static void putCommandIcons(
            Map<String, KeyDisplayOverride> overrides,
            int icon,
            String glyph,
            String... semanticKeys) {
        putIconOverride(overrides, "icon:" + icon, glyph);
        for (String key : semanticKeys) {
            putIconOverride(overrides, key, glyph);
        }
    }

    private static void putIconOverride(Map<String, KeyDisplayOverride> overrides, String key, String glyph) {
        overrides.put(KeyboardSettings.normalizeKeyOverrideName(key), KeyDisplayOverride.icon(glyph));
    }
}
