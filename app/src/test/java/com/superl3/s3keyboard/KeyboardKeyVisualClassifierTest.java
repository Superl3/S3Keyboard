package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyboardKeyVisualClassifierTest {
    @Test
    public void spacebarUsesModifierRole() {
        GestureKey space = new GestureKey(
                "Space",
                KeyboardCommands.CMD_SPACE,
                null,
                null,
                KeyboardCommands.CMD_MOVE_LEFT,
                KeyboardCommands.CMD_MOVE_RIGHT,
                null,
                10,
                KeyIcon.SPACE);

        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(KeyboardSettings.defaults(), space));
    }

    @Test
    public void destructiveInputControlKeysUseModifierRole() {
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Delete", KeyboardCommands.CMD_DELETE, 3)));
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH),
                        GestureKey.command(
                                "Shift",
                                KeyboardCommands.CMD_SHIFT_ONCE,
                                KeyboardCommands.CMD_SHIFT_LOCK,
                                3,
                                KeyIcon.SHIFT)));
    }

    @Test
    public void optionReservedAndLanguageUseModifierRole() {
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Reserved", KeyboardCommands.CMD_RESERVED_PHRASES, 2)));
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Language", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2)));
    }

    @Test
    public void keyRolesUseAlphaModifierAndAccentBackgrounds() {
        KeyboardSettings settings = KeyboardSettings.defaults();

        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                settings,
                GestureKey.command("Delete", KeyboardCommands.CMD_DELETE, 3)));
        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                settings,
                GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                settings,
                GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                settings,
                GestureKey.command("Language", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2)));
    }

    @Test
    public void hangulDotAndSlashSpecialKeysUsePointKeyRole() {
        KeyboardSettings hangul = KeyboardSettings.defaults();

        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.MODIFIER,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.ALPHA,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.ALPHA,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(". .", ".", null, null, null, null, null)));
        assertEquals(
                KeyVisualRole.ALPHA,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul.withKeyboardMode(KeyboardMode.ENGLISH),
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                hangul.functionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        hangul,
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
    }

    @Test
    public void contextualVowelCommandKeysUseNormalBackground() {
        KeyboardSettings hangul = KeyboardSettings.defaults();

        assertEquals(
                KeyVisualRole.ALPHA,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(
                                "ㅣ.",
                                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                                "ㅗ",
                                "ㅜ",
                                "ㅓ",
                                "ㅏ",
                                null)));
        assertEquals(
                KeyVisualRole.ALPHA,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(
                                "ㅡㅐ",
                                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                                "ㅙ",
                                "ㅞ",
                                "ㅔ",
                                "ㅐ",
                                null)));
    }

    @Test
    public void keyColorOverrideTintsTextWithoutChangingRoleOrBackground() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("tap:a", 0x00123456);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withKeyColorOverrides(overrides);
        GestureKey key = new GestureKey("a", "a", "A", "-", null, null, "-", 2);

        assertEquals(KeyVisualRole.ALPHA, KeyboardKeyVisualClassifier.roleFor(settings, key));
        assertEquals(settings.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(settings, key));
        assertEquals(0xFF123456, KeyboardKeyVisualClassifier.textColorFor(settings, key));
    }

    @Test
    public void semanticCommandOverridesApplyToOptionsAndReservedKeys() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("options", 0x00010203);
        overrides.put("background:options", 0x00040506);
        overrides.put("reserved", 0x00070809);
        overrides.put("background:reserved", 0x000A0B0C);
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyColorOverrides(overrides);

        assertEquals(
                0xFF010203,
                KeyboardKeyVisualClassifier.textColorFor(
                        settings,
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                0xFF040506,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                0xFF070809,
                KeyboardKeyVisualClassifier.textColorFor(
                        settings,
                        GestureKey.command("Reserved", KeyboardCommands.CMD_RESERVED_PHRASES, 2)));
        assertEquals(
                0xFF0A0B0C,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Reserved", KeyboardCommands.CMD_RESERVED_PHRASES, 2)));
    }

    @Test
    public void numberRowColorModeAppliesToBackgroundAndForeground() {
        GestureKey three = new GestureKey("3", "3", null, "#", null, null, "#");
        GestureKey five = new GestureKey("5", "5", null, "%", null, null, "%");
        KeyboardSettings halfMod = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.HALF_MOD_4567);
        KeyboardSettings fullAlpha = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.FULL_ALPHA);
        KeyboardSettings fullMod = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.FULL_MOD);
        KeyboardSettings alphaAccent = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.ALPHA_ACCENT);
        KeyboardSettings modAlpha = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.MOD_ALPHA);

        assertEquals(halfMod.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(halfMod, three));
        assertEquals(halfMod.accentColor, KeyboardKeyVisualClassifier.textColorFor(halfMod, three));
        assertEquals(halfMod.accentColor, KeyboardKeyVisualClassifier.hintColorFor(halfMod, three));
        assertEquals(halfMod.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(halfMod, five));
        assertEquals(halfMod.secondaryColor, KeyboardKeyVisualClassifier.textColorFor(halfMod, five));
        assertEquals(halfMod.secondaryColor, KeyboardKeyVisualClassifier.hintColorFor(halfMod, five));
        assertEquals(fullAlpha.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(fullAlpha, five));
        assertEquals(fullAlpha.accentColor, KeyboardKeyVisualClassifier.textColorFor(fullAlpha, five));
        assertEquals(fullMod.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(fullMod, three));
        assertEquals(fullMod.secondaryColor, KeyboardKeyVisualClassifier.textColorFor(fullMod, three));
        assertEquals(alphaAccent.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(alphaAccent, three));
        assertEquals(alphaAccent.accentKeyColor, KeyboardKeyVisualClassifier.colorFor(alphaAccent, five));
        assertEquals(alphaAccent.functionKeyColor, KeyboardKeyVisualClassifier.textColorFor(alphaAccent, five));
        assertEquals(modAlpha.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(modAlpha, three));
        assertEquals(modAlpha.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(modAlpha, five));
    }

    @Test
    public void numberRowColorModeUsesSemanticForegroundOverrides() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x000A0B0C);
        overrides.put("modifiers", 0x00040506);
        overrides.put("background:modifiers", 0x000D0E0F);
        overrides.put("modInv", 0x00070809);
        overrides.put("background:modInv", 0x00101112);
        GestureKey three = new GestureKey("3", "3", null, "#", null, null, "#");
        GestureKey five = new GestureKey("5", "5", null, "%", null, null, "%");
        KeyboardSettings alphaAccent = KeyboardSettings.defaults()
                .withKeyColorOverrides(overrides)
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.ALPHA_ACCENT);
        KeyboardSettings modAlpha = KeyboardSettings.defaults()
                .withKeyColorOverrides(overrides)
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.MOD_ALPHA);

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(alphaAccent, three));
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(alphaAccent, three));
        assertEquals(0xFF070809, KeyboardKeyVisualClassifier.textColorFor(alphaAccent, five));
        assertEquals(0xFF101112, KeyboardKeyVisualClassifier.colorFor(alphaAccent, five));
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.textColorFor(modAlpha, three));
        assertEquals(0xFF0D0E0F, KeyboardKeyVisualClassifier.colorFor(modAlpha, three));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(modAlpha, five));
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(modAlpha, five));
    }

    @Test
    public void numberRowColorModeImportsLegacyPreferences() {
        assertEquals(
                AdditionalNumberRowColorMode.FULL_ALPHA,
                AdditionalNumberRowColorMode.fromPreference("full_default"));
        assertEquals(
                AdditionalNumberRowColorMode.HALF_MOD_4567,
                AdditionalNumberRowColorMode.fromPreference("center_dimmed"));
        assertEquals(
                AdditionalNumberRowColorMode.FULL_MOD,
                AdditionalNumberRowColorMode.fromPreference("full_dimmed"));
    }

    @Test
    public void shiftIndicatorCanUseThemeOverride() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("shiftIndicator", 0x0000A676);
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyColorOverrides(overrides);

        assertEquals(0xFF00A676, KeyboardKeyVisualClassifier.shiftIndicatorColorFor(settings));
    }

    @Test
    public void alphaAndModifierGroupOverridesCanColorKeys() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put("modifiers", 0x00070809);
        overrides.put("background:modifiers", 0x000A0B0C);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withKeyColorOverrides(overrides);
        GestureKey alpha = new GestureKey("q", "q", "Q", "!", null, null, "!", 2);
        GestureKey enter = GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3);

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, alpha));
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(settings, alpha));
        assertEquals(0xFF070809, KeyboardKeyVisualClassifier.textColorFor(settings, enter));
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(settings, enter));
    }

    @Test
    public void alphaGroupOverridesApplyToSpecialActionAndPunctuationKeys() {
        Map<String, KeyDisplayOverride> displayOverrides = new HashMap<>();
        displayOverrides.put("alpha", KeyDisplayOverride.icon(ModifierIconCatalog.GLYPH_DOT));
        displayOverrides.put("modifiers", KeyDisplayOverride.text("mod"));
        displayOverrides.put("..", KeyDisplayOverride.text("two"));
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyDisplayOverrides(displayOverrides);

        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey(
                                "\u3163.",
                                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                                "\u3163",
                                ".",
                                "\u3163",
                                ".",
                                null)).value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey(
                                "\u3161\u3150",
                                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                                "\u3161",
                                "\u3150",
                                "\u3161",
                                "\u3150",
                                null)).value);
        assertEquals(
                "two",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey(". .", ".", null, null, null, null, null)).value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null)).value);
        assertEquals(
                "mod",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)).value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("5", "5", null, "%", null, null, "%")).value);
    }

    @Test
    public void remoteModeIgnoresThemeDisplayOverrides() {
        Map<String, KeyDisplayOverride> displayOverrides = new HashMap<>();
        displayOverrides.put("alpha", KeyDisplayOverride.icon(ModifierIconCatalog.GLYPH_DOT));
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyDisplayOverrides(displayOverrides)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);

        assertNull(KeyDisplayOverrideResolver.resolve(
                settings,
                new GestureKey("q", "q", "Q", "1", null, null, null)));
    }

    @Test
    public void dotAlphaOverrideCanOwnDingulPunctuationWhenNoModifierOverrideExists() {
        Map<String, KeyDisplayOverride> displayOverrides = new HashMap<>();
        displayOverrides.put("alpha", KeyDisplayOverride.icon(ModifierIconCatalog.GLYPH_DOT));
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyDisplayOverrides(displayOverrides);

        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null)).value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey(".", ".", ",", "!", "?", KeyboardCommands.CMD_NOOP, null)).value);
        assertEquals(
                ModifierIconCatalog.GLYPH_DOT,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)).value);
    }

    @Test
    public void alphaGroupColorOverridesApplyToSpecialActionAndPunctuationKeys() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put("background:..", 0x000A0B0C);
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyColorOverrides(overrides);

        GestureKey centerVowel = new GestureKey(
                "\u3163.",
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                "\u3163",
                ".",
                "\u3163",
                ".",
                null);
        GestureKey dotDot = new GestureKey(". .", ".", null, null, null, null, null);

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, centerVowel));
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(settings, centerVowel));
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(settings, dotDot));
    }

    @Test
    public void escPointKeycapDoesNotOverrideNumberRowLegendWhenVisible() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withPointKeycapStyle(true)
                .withNumberRow(true);

        assertEquals(
                null,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("1", "1", null, "!", null, null, "!")));
        assertEquals(
                null,
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("q", "q", "Q", "!", null, null, "!")));
    }

    @Test
    public void escPointKeycapDoesNotOverrideFirstAlphaKeyWithoutNumberRow() {
        KeyboardSettings qwerty = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false)
                .withPointKeycapStyle(true);
        KeyboardSettings dingul = KeyboardSettings.defaults()
                .withHangulNumberRow(false)
                .withPointKeycapStyle(true);

        assertEquals(
                null,
                KeyDisplayOverrideResolver.resolve(
                        qwerty,
                        new GestureKey("q", "q", "Q", "!", null, null, "!")));
        assertEquals(
                null,
                KeyDisplayOverrideResolver.resolve(
                        dingul,
                        new GestureKey("\u3131", "\u3131", "\u314B", "#", "\u314B", "\u314B", "#")));
    }

    @Test
    public void exactDisplayOverrideCanStillReplaceEscPointCandidate() {
        Map<String, KeyDisplayOverride> displayOverrides = new HashMap<>();
        displayOverrides.put("tap:q", KeyDisplayOverride.text("Cue"));
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false)
                .withPointKeycapStyle(true)
                .withKeyDisplayOverrides(displayOverrides);

        assertEquals(
                "Cue",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        new GestureKey("q", "q", "Q", "!", null, null, "!")).value);
    }

    @Test
    public void dingulDotDotUsesAlphaRoleWhenNoExactOverrideExists() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put("modifiers", 0x00070809);
        overrides.put("background:modifiers", 0x000A0B0C);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withKeyColorOverrides(overrides);
        GestureKey dotDot = new GestureKey(". .", ".", null, null, null, null, null);

        assertEquals(KeyVisualRole.ALPHA, KeyboardKeyVisualClassifier.roleFor(settings, dotDot));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, dotDot));
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(settings, dotDot));
    }

    @Test
    public void dingulDotDotIgnoresDotPunctuationAccentOverride() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put(".", 0x000A0B0C);
        overrides.put("background:.", 0x000D0E0F);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withKeyColorOverrides(overrides);
        GestureKey dotDot = new GestureKey(". .", ".", null, null, null, null, null);

        assertEquals(KeyVisualRole.ALPHA, KeyboardKeyVisualClassifier.roleFor(settings, dotDot));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, dotDot));
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(settings, dotDot));
    }

    @Test
    public void accentPlacementModeCanForceModifierAccentTargets() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("modInv", 0x00010203);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withExtendedThemeColors(
                        KeyboardSettings.DEFAULT_KEY_IDLE_COLOR,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR,
                                                0x00AA5500,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);
        KeyboardSettings accented = AccentPlacementMode.ENTER_SHIFT.applyTo(settings);

        assertEquals(0xFFAA5500, KeyboardKeyVisualClassifier.colorFor(
                accented,
                new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(
                accented,
                new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(false, accented.keyColorOverrides.containsKey("background:.."));
    }

    @Test
    public void accentPlacementPolicyCanCombineTargets() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("modInv", 0x00010203);
        overrides.put("background:modInv", 0x00D0D1D2);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withExtendedThemeColors(
                        0x00202020,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        0x00404040,
                                                0x00AA5500,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);
        KeyboardSettings accented = AccentPlacementPolicy.fromPreference("settings_enter,meta").applyTo(settings);

        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                accented,
                GestureKey.command("", KeyboardCommands.CMD_ENTER)));
        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                accented,
                GestureKey.command("", KeyboardCommands.CMD_QUICK_SETTINGS)));
        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                accented,
                GestureKey.command("", KeyboardCommands.CMD_RESERVED_PHRASES)));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(
                accented,
                GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE)));
        assertEquals(false, accented.keyColorOverrides.containsKey("background:shift"));
    }

    @Test
    public void accentPlacementPolicySkipsTwoToneThemes() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withExtendedThemeColors(
                        0x00101010,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        0x00202020,
                        0x00202020,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR);
        KeyboardSettings accented = AccentPlacementPolicy.fromPreference("settings_enter,meta").applyTo(settings);

        assertEquals(false, accented.keyColorOverrides.containsKey("background:enter"));
        assertEquals(false, accented.keyColorOverrides.containsKey("background:language"));
    }

    @Test
    public void accentPlacementPolicyKeepsVisualEnterAndShiftSeparate() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("modInv", 0x00010203);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withExtendedThemeColors(
                        KeyboardSettings.DEFAULT_KEY_IDLE_COLOR,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR,
                                                0x00AA5500,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);

        KeyboardSettings dotOnly = AccentPlacementPolicy.fromPreference("dingul_dot").applyTo(settings);

        assertEquals(0xFFAA5500, KeyboardKeyVisualClassifier.colorFor(
                dotOnly,
                new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                dotOnly,
                new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));

        KeyboardSettings slashOnly = AccentPlacementPolicy.fromPreference("dingul_slash").applyTo(settings);

        assertEquals(settings.functionKeyColor, KeyboardKeyVisualClassifier.colorFor(
                slashOnly,
                new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(0xFFAA5500, KeyboardKeyVisualClassifier.colorFor(
                slashOnly,
                new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));
    }

    @Test
    public void accentPlacementPolicyCanSetSpaceRoleIndependently() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put("modifiers", 0x00070809);
        overrides.put("background:modifiers", 0x000A0B0C);
        overrides.put("modInv", 0x000D0E0F);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withExtendedThemeColors(
                        KeyboardSettings.DEFAULT_KEY_IDLE_COLOR,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR,
                                                0x00AA5500,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);
        GestureKey space = GestureKey.command("Space", KeyboardCommands.CMD_SPACE, null, 10, KeyIcon.SPACE);

        KeyboardSettings alphaSpace = AccentPlacementPolicy.fromPreference("space:alpha").applyTo(settings);
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(alphaSpace, space));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(alphaSpace, space));

        KeyboardSettings modSpace = AccentPlacementPolicy.fromPreference("space:mod").applyTo(settings);
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(modSpace, space));
        assertEquals(0xFF070809, KeyboardKeyVisualClassifier.textColorFor(modSpace, space));

        KeyboardSettings accentSpace = AccentPlacementPolicy.fromPreference("space:accent").applyTo(settings);
        assertEquals(0xFFAA5500, KeyboardKeyVisualClassifier.colorFor(accentSpace, space));
        assertEquals(0xFF0D0E0F, KeyboardKeyVisualClassifier.textColorFor(accentSpace, space));
    }

    @Test
    public void accentPlacementPolicyCanSetQuestionRoleIndependently() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("background:alpha", 0x00040506);
        overrides.put("modifiers", 0x00070809);
        overrides.put("background:modifiers", 0x000A0B0C);
        overrides.put("modInv", 0x000D0E0F);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withExtendedThemeColors(
                        KeyboardSettings.DEFAULT_KEY_IDLE_COLOR,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        KeyboardSettings.DEFAULT_FUNCTION_KEY_COLOR,
                                                0x00AA5500,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);
        GestureKey question = new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null);

        KeyboardSettings alphaQuestion = AccentPlacementPolicy.fromPreference("question:alpha").applyTo(settings);
        assertEquals(0xFF040506, KeyboardKeyVisualClassifier.colorFor(alphaQuestion, question));
        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(alphaQuestion, question));

        KeyboardSettings modQuestion = AccentPlacementPolicy.fromPreference("question:mod").applyTo(settings);
        assertEquals(0xFF0A0B0C, KeyboardKeyVisualClassifier.colorFor(modQuestion, question));
        assertEquals(0xFF070809, KeyboardKeyVisualClassifier.textColorFor(modQuestion, question));

        KeyboardSettings accentQuestion = AccentPlacementPolicy.fromPreference("question:accent").applyTo(settings);
        assertEquals(0xFFAA5500, KeyboardKeyVisualClassifier.colorFor(accentQuestion, question));
        assertEquals(0xFF0D0E0F, KeyboardKeyVisualClassifier.textColorFor(accentQuestion, question));
    }

    @Test
    public void accentPlacementPolicyCanAccentEscPointKey() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("modInv", 0x00010203);
        overrides.put("background:modInv", 0x00D0D1D2);
        KeyboardSettings base = KeyboardSettings.defaults()
                .withExtendedThemeColors(
                        0x00202020,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        KeyboardSettings.DEFAULT_ACCENT_COLOR,
                        KeyboardSettings.DEFAULT_SECONDARY_COLOR,
                        0x00404040,
                        0x00000000,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR)
                .withKeyColorOverrides(overrides);

        KeyboardSettings english = AccentPlacementPolicy.fromPreference("esc_point")
                .applyTo(base.withKeyboardMode(KeyboardMode.ENGLISH).withEnglishNumberRow(false));
        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                english,
                new GestureKey("q", "q", "Q", "1", null, null, "1", 2)));

        KeyboardSettings hangul = AccentPlacementPolicy.fromPreference("esc_point")
                .applyTo(base.withKeyboardMode(KeyboardMode.HANGUL).withHangulNumberRow(false));
        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                hangul,
                new GestureKey("\u3131", "\u3131", "\u3132", "#", "\u314B", "\u314B", null)));

        KeyboardSettings numberRow = AccentPlacementPolicy.fromPreference("esc_point")
                .applyTo(base.withKeyboardMode(KeyboardMode.ENGLISH).withEnglishNumberRow(true));
        assertEquals(0xFFD0D1D2, KeyboardKeyVisualClassifier.colorFor(
                numberRow,
                new GestureKey("1", "1", null, "!", null, null, "!", 1)));
    }

    @Test
    public void accentPlacementPolicyKeepsLegacyTokensReadable() {
        AccentPlacementPolicy policy = AccentPlacementPolicy.fromPreference("enter_shift,command,space:mod,question:accent");

        assertEquals(true, policy.contains(AccentPlacementTarget.SETTINGS_ENTER));
        assertEquals(true, policy.contains(AccentPlacementTarget.DINGUL_DOT));
        assertEquals(true, policy.contains(AccentPlacementTarget.DINGUL_SLASH));
        assertEquals(true, policy.contains(AccentPlacementTarget.QWERTY_SHIFT));
        assertEquals(true, policy.contains(AccentPlacementTarget.BACKSPACE));
        assertEquals(true, policy.containsSpaceRole(AccentPlacementPolicy.SpaceRole.MOD));
        assertEquals(true, policy.containsQuestionRole(AccentPlacementPolicy.QuestionRole.ACCENT));
    }

    @Test
    public void accentPlacementPolicyKeepsNoneSeparateFromThemeDefault() {
        assertEquals("none", AccentPlacementPolicy.none().toPreferenceValue());
        assertEquals(false, AccentPlacementPolicy.fromPreference("none").themeDefault);
        assertEquals(true, AccentPlacementPolicy.fromPreference("theme_default").themeDefault);
    }

    @Test
    public void dingulQuestionUsesAlphaForeground() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("modifiers", 0x00040506);
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyColorOverrides(overrides);
        GestureKey question = new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null);

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, question));

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(
                settings.withModifierIconThemePack(ModifierIconCatalog.PACK_DOTS_LINES),
                question));
    }

    @Test
    public void hintColorFallsBackWhenSecondaryIsTooCloseToKeyBackground() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withExtendedThemeColors(
                        0x008C929D,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        0x00161820,
                        0x00A9ADB8,
                        0x001E2028,
                                                KeyboardSettings.DEFAULT_ACCENT_KEY_COLOR,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR);
        GestureKey alpha = new GestureKey("a", "a", "A", "!", null, null, "!", 2);

        assertEquals(
                KeyboardKeyVisualClassifier.textColorFor(settings, alpha),
                KeyboardKeyVisualClassifier.hintColorFor(settings, alpha));
    }

    @Test
    public void hintColorKeepsSecondaryWhenItContrastsWithKeyBackground() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withExtendedThemeColors(
                        0x008C929D,
                        KeyboardSettings.DEFAULT_KEY_PRESSED_COLOR,
                        KeyboardSettings.DEFAULT_KEYBOARD_BACKGROUND_COLOR,
                        0x00161820,
                        0x00A9ADB8,
                        0x001E2028,
                                                KeyboardSettings.DEFAULT_ACCENT_KEY_COLOR,
                        KeyboardSettings.DEFAULT_BORDER_COLOR,
                        false,
                        KeyboardSettings.DEFAULT_DEPTH_COLOR);
        GestureKey modifier = GestureKey.command("", KeyboardCommands.CMD_TOGGLE_LANGUAGE);

        assertEquals(settings.secondaryColor, KeyboardKeyVisualClassifier.hintColorFor(settings, modifier));
    }

    @Test
    public void simpleTextPackReplacesOnlyDingulVisualEnter() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyDisplayThemePack(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT);

        KeyDisplayOverride override = KeyDisplayOverrideResolver.resolve(
                settings,
                GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3));

        assertEquals(null, override);

        KeyDisplayOverride dingulVisualEnter = KeyDisplayOverrideResolver.resolve(
                settings.withKeyboardMode(KeyboardMode.HANGUL),
                new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null));

        assertEquals(KeyDisplayOverride.TYPE_TEXT, dingulVisualEnter.type);
        assertEquals("hihihi", dingulVisualEnter.value);
        assertEquals(null, KeyDisplayOverrideResolver.resolve(
                settings.withKeyboardMode(KeyboardMode.HANGUL),
                new GestureKey(". .", ".", null, null, null, null, null)));
    }

    @Test
    public void gitCommandPackCanReplaceCommandIconsWithGitText() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyDisplayThemePack(KeyDisplayOverridePackCatalog.PACK_GIT_COMMANDS);

        assertEquals(
                "exec",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)).value);
        assertEquals(
                "fetch",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        GestureKey.command(
                                "Language",
                                KeyboardCommands.CMD_TOGGLE_LANGUAGE,
                                null,
                                2,
                                KeyIcon.LANGUAGE)).value);
        assertEquals(
                "pull",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        GestureKey.command("Space", KeyboardCommands.CMD_SPACE, null, 10, KeyIcon.SPACE)).value);
        assertEquals(
                "rebase",
                KeyDisplayOverrideResolver.resolve(
                        settings,
                        GestureKey.command(
                                "Shift",
                                KeyboardCommands.CMD_SHIFT_ONCE,
                                KeyboardCommands.CMD_SHIFT_LOCK,
                                3,
                                KeyIcon.SHIFT)).value);
    }
}
