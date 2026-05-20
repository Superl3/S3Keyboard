package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyboardKeyVisualClassifierTest {
    @Test
    public void spacebarUsesFunctionKeyRole() {
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
                KeyVisualRole.FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(KeyboardSettings.defaults(), space));
    }

    @Test
    public void primaryInputControlKeysUsePrimaryFunctionRole() {
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Delete", KeyboardCommands.CMD_DELETE, 3)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
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
    public void optionReservedAndLanguageUsePointKeyRole() {
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Reserved", KeyboardCommands.CMD_RESERVED_PHRASES, 2)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        KeyboardSettings.defaults(),
                        GestureKey.command("Language", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2)));
    }

    @Test
    public void keyRolesUseAlphaModifierAndAccentBackgrounds() {
        KeyboardSettings settings = KeyboardSettings.defaults();

        assertEquals(
                settings.primaryFunctionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Delete", KeyboardCommands.CMD_DELETE, 3)));
        assertEquals(
                settings.primaryFunctionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3)));
        assertEquals(
                settings.primaryFunctionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Options", KeyboardCommands.CMD_OPEN_OPTIONS, 3)));
        assertEquals(
                settings.primaryFunctionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        settings,
                        GestureKey.command("Language", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2)));
    }

    @Test
    public void hangulSemicolonAndSlashSpecialKeysUsePointKeyRole() {
        KeyboardSettings hangul = KeyboardSettings.defaults();

        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.PRIMARY_FUNCTION,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul,
                        new GestureKey(". .", ".", null, null, null, null, null)));
        assertEquals(
                KeyVisualRole.NORMAL,
                KeyboardKeyVisualClassifier.roleFor(
                        hangul.withKeyboardMode(KeyboardMode.ENGLISH),
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
        assertEquals(
                hangul.primaryFunctionKeyColor,
                KeyboardKeyVisualClassifier.colorFor(
                        hangul,
                        new GestureKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, null)));
    }

    @Test
    public void contextualVowelCommandKeysUseNormalBackground() {
        KeyboardSettings hangul = KeyboardSettings.defaults();

        assertEquals(
                KeyVisualRole.NORMAL,
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
                KeyVisualRole.NORMAL,
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

        assertEquals(KeyVisualRole.NORMAL, KeyboardKeyVisualClassifier.roleFor(settings, key));
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
        KeyboardSettings center = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.CENTER_DIMMED);
        KeyboardSettings fullDefault = KeyboardSettings.defaults()
                .withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.FULL_DEFAULT);

        assertEquals(center.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(center, three));
        assertEquals(center.accentColor, KeyboardKeyVisualClassifier.textColorFor(center, three));
        assertEquals(center.accentColor, KeyboardKeyVisualClassifier.hintColorFor(center, three));
        assertEquals(center.accentKeyColor, KeyboardKeyVisualClassifier.colorFor(center, five));
        assertEquals(center.secondaryColor, KeyboardKeyVisualClassifier.textColorFor(center, five));
        assertEquals(center.secondaryColor, KeyboardKeyVisualClassifier.hintColorFor(center, five));
        assertEquals(fullDefault.keyIdleColor, KeyboardKeyVisualClassifier.colorFor(fullDefault, five));
        assertEquals(fullDefault.accentColor, KeyboardKeyVisualClassifier.textColorFor(fullDefault, five));
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
    public void dingulPunctuationUsesAlphaForegroundUnlessThemeUsesPointIcons() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("alpha", 0x00010203);
        overrides.put("enter", 0x00040506);
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyColorOverrides(overrides);
        GestureKey question = new GestureKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, null);

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(settings, question));

        assertEquals(0xFF010203, KeyboardKeyVisualClassifier.textColorFor(
                settings.withModifierIconThemePack(ModifierIconCatalog.PACK_DOTS_LINES),
                question));
    }

    @Test
    public void keyDisplayPackCanReplaceCommandIconsWithText() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyDisplayThemePack(KeyDisplayOverridePackCatalog.PACK_SIMPLE_TEXT);

        KeyDisplayOverride override = KeyDisplayOverrideResolver.resolve(
                settings,
                GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, 3));

        assertEquals(KeyDisplayOverride.TYPE_TEXT, override.type);
        assertEquals("hihihi", override.value);
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
