package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public final class KeyboardLayoutFactoryTest {
    @Test
    public void englishKeyHasTapUpperSlideAndNoLongPressInput() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        GestureKey a = findKey(KeyboardLayoutFactory.build(settings), "a");

        assertEquals("a", a.valueFor(GestureAction.TAP));
        assertEquals("A", a.valueFor(GestureAction.UP));
        assertEquals("@", a.valueFor(GestureAction.DOWN));
        assertNull(a.valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void hangulLongPressSlotDefaultsToEmpty() {
        GestureKey giyeok = findKey(KeyboardLayoutFactory.build(KeyboardSettings.defaults()), "ㄱ");

        assertNull(giyeok.valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void hangulReferenceGridMatchesRowsAboveBottomControls() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(KeyboardSettings.defaults());

        assertEquals("ㄱ,ㄴ,ㅢ,삭제", labels(rows.get(0)));
        assertEquals("ㄹ,ㅁ,ㅣ.,?", labels(rows.get(1)));
        assertEquals("ㅅ,ㅇ,ㅡㅐ,.", labels(rows.get(2)));
        assertEquals("ㅈ,ㅎ,. .,/", labels(rows.get(3)));
        assertEquals("83,83,83,51", widths(rows.get(0)));
        assertEquals("83,83,83,51", widths(rows.get(1)));
        assertEquals("83,83,83,51", widths(rows.get(2)));
        assertEquals("83,83,83,51", widths(rows.get(3)));
    }

    @Test
    public void hangulSpecialColumnDefaultsNearFiveToOneComparisonLayout() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(KeyboardSettings.defaults());

        for (int i = 0; i < 4; i++) {
            KeyboardRow row = rows.get(i);
            assertEquals(300, row.baseUnits);
            assertEquals(83, row.keys.get(0).widthUnits);
            assertEquals(83, row.keys.get(1).widthUnits);
            assertEquals(83, row.keys.get(2).widthUnits);
            assertEquals(51, row.keys.get(3).widthUnits);
        }
    }

    @Test
    public void hangulSpecialColumnCanUseFourToOneComparisonLayout() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(
                KeyboardSettings.defaults().withHangulSpecialColumnPercent(20));

        for (int i = 0; i < 4; i++) {
            KeyboardRow row = rows.get(i);
            assertEquals(300, row.baseUnits);
            assertEquals("80,80,80,60", widths(row));
        }
    }

    @Test
    public void hangulReferenceDirectionalHintsMatchVisibleKeyText() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(KeyboardSettings.defaults());

        assertDirections(findKey(rows, "ㄱ"), "ㄲ", "#", "ㅋ", "ㅋ");
        assertDirections(findKey(rows, "ㄴ"), "ㄸ", "ㄷ", "ㅌ", "ㅌ");
        assertDirections(findKey(rows, "ㄹ"), "^", "~", "=", "-");
        assertDirections(findKey(rows, "ㅁ"), "ㅃ", "ㅂ", "ㅍ", "ㅍ");
        assertDirections(findKey(rows, "ㅅ"), "ㅆ", "2", "1", "3");
        assertDirections(findKey(rows, "ㅇ"), "♥", "5", "4", "6");
        assertDirections(findKey(rows, "ㅈ"), "ㅉ", "~", "ㅊ", "ㅊ");
        assertDirections(findKey(rows, "ㅎ"), "0", "8", "7", "9");
        assertDirections(findKey(rows, "ㅢ"), "ㅚ", "ㅟ", "ㅝ", "ㅘ");
        assertDirections(findKey(rows, "ㅣ."), "ㅗ", "ㅜ", "ㅓ", "ㅏ");
        assertDirections(findKey(rows, "ㅡㅐ"), "ㅙ", "ㅞ", "ㅔ", "ㅐ");
        assertDirections(findKey(rows, ". ."), "\u315B", "\u3160", "\u3155", "\u3151");
        assertEquals(" ", findKey(rows, ". .").valueFor(GestureAction.TAP));
        assertDirections(findKey(rows, "?"), "!", "*", "+", KeyboardCommands.CMD_NOOP);
        assertDirections(findKey(rows, "."), "\"", "`", ",", KeyboardCommands.CMD_NOOP);
        assertDirections(findKey(rows, "/"), ":", ";", "@", KeyboardCommands.CMD_NOOP);
    }

    @Test
    public void dingulContextualVowelKeysDoNotEmitLiteralTapVowels() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(KeyboardSettings.defaults());

        assertEquals(
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                findKey(rows, "ㅣ.").valueFor(GestureAction.TAP));
        assertEquals(
                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                findKey(rows, "ㅡㅐ").valueFor(GestureAction.TAP));
    }

    @Test
    public void hangulReferenceUsesSlashKeyInsteadOfHideCommandInMainGrid() {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(KeyboardSettings.defaults());
        GestureKey slash = findKey(rows, "/");

        assertEquals("/", slash.valueFor(GestureAction.TAP));
        assertEquals("@", slash.leftSlide);
        assertEquals(":", slash.upSlide);
        assertEquals(";", slash.downSlide);
        assertEquals(KeyboardCommands.CMD_NOOP, slash.rightSlide);
        assertFalse(containsKey(rows, "숨김"));
    }

    @Test
    public void hangulDeleteKeyDoesNotRenderCursorSlideHints() {
        GestureKey delete = findKey(KeyboardLayoutFactory.build(KeyboardSettings.defaults()), "삭제");

        assertNull(delete.upSlide);
        assertNull(delete.downSlide);
        assertNull(delete.leftSlide);
        assertNull(delete.rightSlide);
    }

    @Test
    public void rightAndBalancedModesPlacePrimaryControlsNearRightThumb() {
        assertEquals(
                "옵션,예약어,스페이스,한/영,전송",
                bottomLabels(KeyboardSettings.defaults()));
        assertEquals(
                "3,2,10,2,3",
                bottomWidths(KeyboardSettings.defaults()));
        assertEquals(
                "옵션,예약어,스페이스,한/영,전송",
                bottomLabels(KeyboardSettings.defaults().withHandednessPreset(HandednessMode.RIGHT)));
        assertEquals(
                "3,2,10,2,3",
                bottomWidths(KeyboardSettings.defaults().withHandednessPreset(HandednessMode.RIGHT)));
    }

    @Test
    public void leftModeMirrorsBottomControls() {
        assertEquals(
                "전송,한/영,스페이스,예약어,옵션",
                bottomLabels(KeyboardSettings.defaults().withHandednessPreset(HandednessMode.LEFT)));
        assertEquals(
                "3,2,10,2,3",
                bottomWidths(KeyboardSettings.defaults().withHandednessPreset(HandednessMode.LEFT)));
    }

    @Test
    public void spacebarSlidesMoveCursorInsteadOfCommittingSpace() {
        GestureKey space = findKey(KeyboardLayoutFactory.build(KeyboardSettings.defaults()), "스페이스");

        assertEquals(KeyboardCommands.CMD_SPACE, space.valueFor(GestureAction.TAP));
        assertEquals(".com", space.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_MOVE_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_MOVE_RIGHT, space.valueFor(GestureAction.RIGHT));
    }

    @Test
    public void languageKeyOffersCommaAndPeriodGestures() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        GestureKey space = findKey(rows, "스페이스");
        GestureKey language = findKey(rows, "한/영");

        assertEquals(".com", space.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_MOVE_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_MOVE_RIGHT, space.valueFor(GestureAction.RIGHT));
        assertEquals(KeyboardCommands.CMD_TOGGLE_LANGUAGE, language.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_NOOP, language.valueFor(GestureAction.UP));
        assertEquals(",", language.valueFor(GestureAction.LEFT));
        assertEquals(".", language.valueFor(GestureAction.RIGHT));
        assertNull(language.valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void enterLabelUsesResolvedImeActionLabel() {
        KeyboardSettings settings = KeyboardSettings.defaults().withEnterKeyLabel("검색");

        assertEquals(
                "옵션,예약어,스페이스,한/영,검색",
                bottomLabels(settings));
    }

    @Test
    public void numberRowAddsTopRowAndMeasuredHeight() {
        KeyboardSettings withoutNumberRow = KeyboardSettings.defaults();
        KeyboardSettings withNumberRow = withoutNumberRow.withNumberRow(true);

        List<KeyboardRow> rows = KeyboardLayoutFactory.build(withNumberRow);
        assertEquals(
                KeyboardLayoutFactory.build(withoutNumberRow).size() + 1,
                rows.size());
        assertEquals("1,2,3,4,5,6,7,8,9,0", labels(rows.get(0)));
        assertEquals(
                withoutNumberRow.measuredHeightDp()
                        + KeyboardSettings.NUMBER_ROW_HEIGHT_DP
                        + KeyboardSettings.DEFAULT_NUMBER_ROW_BOTTOM_GAP_DP,
                withNumberRow.measuredHeightDp());
    }

    @Test
    public void numpadSurfaceReplacesMainRowsEvenWhenNumberRowIsEnabled() {
        KeyboardSettings settings = KeyboardSettings.defaults().withNumberRow(true);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings, KeyboardSurface.NUMPAD);

        assertEquals(5, rows.size());
        assertEquals("1,2,3,Del", labels(rows.get(0)));
        assertEquals("4,5,6,-", labels(rows.get(1)));
        assertEquals("7,8,9,.", labels(rows.get(2)));
        assertEquals("+,0,00,/", labels(rows.get(3)));
        assertEquals(KeyboardCommands.CMD_DELETE, findKey(rows, "Del").valueFor(GestureAction.TAP));
    }

    @Test
    public void fieldSpecificSurfacesUsePhoneDateAndPinSymbols() {
        List<KeyboardRow> phone = KeyboardLayoutFactory.build(
                KeyboardSettings.defaults(),
                KeyboardSurface.PHONEPAD);
        List<KeyboardRow> date = KeyboardLayoutFactory.build(
                KeyboardSettings.defaults(),
                KeyboardSurface.DATEPAD);
        List<KeyboardRow> pin = KeyboardLayoutFactory.build(
                KeyboardSettings.defaults(),
                KeyboardSurface.PINPAD);

        assertEquals("+", phone.get(1).keys.get(3).tap);
        assertEquals("*", phone.get(2).keys.get(3).tap);
        assertEquals("#", phone.get(3).keys.get(2).tap);
        assertEquals("-", date.get(1).keys.get(3).tap);
        assertEquals("/", date.get(2).keys.get(3).tap);
        assertEquals(":", date.get(3).keys.get(0).tap);
        assertEquals("Enter", pin.get(3).keys.get(2).label);
        assertEquals(KeyboardCommands.CMD_ENTER, pin.get(3).keys.get(2).tap);
    }

    @Test
    public void remoteModeIgnoresFieldSurfaceLayouts() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings, KeyboardSurface.NUMPAD);

        assertEquals(10, rows.get(0).keys.size());
        assertEquals(KeyboardCommands.CMD_REMOTE_F1, rows.get(0).keys.get(0).downSlide);
    }

    @Test
    public void numberRowCanBeConfiguredPerLanguageWithEnglishOnByDefault() {
        KeyboardSettings defaults = KeyboardSettings.defaults();

        assertFalse(defaults.showHangulNumberRow);
        assertEquals(true, defaults.showEnglishNumberRow);
        assertEquals(4 + 1, KeyboardLayoutFactory.build(defaults.withKeyboardMode(KeyboardMode.ENGLISH)).size());
        assertEquals(4 + 1, KeyboardLayoutFactory.build(defaults).size());
    }

    @Test
    public void numberRowDownSlideCommitsSymbols() {
        KeyboardSettings settings = KeyboardSettings.defaults().withNumberRow(true);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals("!", rows.get(0).keys.get(0).valueFor(GestureAction.DOWN));
        assertEquals("@", rows.get(0).keys.get(1).valueFor(GestureAction.DOWN));
        assertEquals(")", rows.get(0).keys.get(9).valueFor(GestureAction.DOWN));
        assertNull(rows.get(0).keys.get(0).valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void remoteModeForcesNumberRowAndMapsFunctionKeys() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulNumberRow(false)
                .withEnglishNumberRow(false)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals(true, settings.showNumberRow);
        assertEquals("1", rows.get(0).keys.get(0).valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_ESC, rows.get(0).keys.get(0).valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_F1, rows.get(0).keys.get(0).valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_F5, rows.get(0).keys.get(4).valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_F10, rows.get(0).keys.get(9).valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_F11, rows.get(0).keys.get(8).valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_F12, rows.get(0).keys.get(9).valueFor(GestureAction.UP));
        assertNull(rows.get(0).keys.get(0).valueFor(GestureAction.LONG_PRESS));
        assertNull(rows.get(0).keys.get(9).valueFor(GestureAction.LONG_PRESS));
        assertNull(rows.get(0).keys.get(0).leftSlide);
        assertNull(rows.get(0).keys.get(9).rightSlide);
    }

    @Test
    public void remoteModeDistributesNavigationClusterAcrossAlphaKeys() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals(KeyboardCommands.CMD_REMOTE_TAB, findKey(rows, "q").valueFor(GestureAction.UP));
        assertNull(findKey(rows, "w").valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_REMOTE_SHIFT_TAB, findKey(rows, "r").valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_CTRL_TAB, findKey(rows, "t").valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_ALT_TAB, findKey(rows, "y").valueFor(GestureAction.UP));
        assertEquals("U", findKey(rows, "u").valueFor(GestureAction.UP));
        assertNull(findKey(rows, "q").valueFor(GestureAction.LONG_PRESS));
        assertNull(findKey(rows, "u").valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_REMOTE_INSERT, findKey(rows, "i").valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_FORWARD_DELETE, findKey(rows, "i").valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_HOME, findKey(rows, "o").valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_END, findKey(rows, "o").valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_PAGE_UP, findKey(rows, "p").valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_PAGE_DOWN, findKey(rows, "p").valueFor(GestureAction.DOWN));
        assertEquals("@", findKey(rows, "a").valueFor(GestureAction.DOWN));
        assertEquals("/", findKey(rows, "d").valueFor(GestureAction.DOWN));
    }

    @Test
    public void runtimeNumberRowForceSupportsPasswordNumpadWithoutChangingStoredToggles() {
        KeyboardSettings forced = KeyboardSettings.defaults().withRuntimeNumberRowForced(true);

        assertEquals(true, forced.showNumberRow);
        assertFalse(forced.showHangulNumberRow);
        assertEquals(true, forced.showEnglishNumberRow);
    }

    @Test
    public void englishQwertyUsesTwentyUnitRowsWithShiftAndBackspace() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals(20, rows.get(0).baseUnits);
        assertEquals("q,w,e,r,t,y,u,i,o,p", labels(rows.get(0)));
        assertEquals("2,2,2,2,2,2,2,2,2,2", widths(rows.get(0)));

        assertEquals(20, rows.get(1).baseUnits);
        assertEquals("a,s,d,f,g,h,j,k,l", labels(rows.get(1)));
        assertEquals("2,2,2,2,2,2,2,2,2", widths(rows.get(1)));

        assertEquals(20, rows.get(2).baseUnits);
        assertEquals("Shift,z,x,c,v,b,n,m,삭제", labels(rows.get(2)));
        assertEquals("3,2,2,2,2,2,2,2,3", widths(rows.get(2)));
    }

    @Test
    public void englishQwertySlideSymbolsMatchCompactSampleLayout() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals("1", findKey(rows, "q").downSlide);
        assertEquals("0", findKey(rows, "p").downSlide);
        assertDirections(findKey(rows, "s"), "S", null, "#", "%");
        assertNull(findKey(rows, "s").valueFor(GestureAction.LONG_PRESS));
        assertEquals("/", findKey(rows, "d").downSlide);
        assertDirections(findKey(rows, "g"), "G", null, "~", "^");
        assertDirections(findKey(rows, "h"), "H", null, "_", "-");
        assertDirections(findKey(rows, "j"), "J", null, "+", "=");
        assertDirections(findKey(rows, "k"), "K", null, "<", ">");
        assertEquals("♥", findKey(rows, "l").downSlide);
        assertDirections(findKey(rows, "z"), "Z", null, "(", ")");
        assertDirections(findKey(rows, "x"), "X", null, "[", "]");
        assertDirections(findKey(rows, "c"), "C", null, ";", ":");
        assertDirections(findKey(rows, "v"), "V", null, "'", "\"");
        assertDirections(findKey(rows, "b"), "B", null, "&", "|");
        assertEquals("!", findKey(rows, "n").downSlide);
        assertEquals("?", findKey(rows, "m").downSlide);
        assertNull(findKey(rows, "m").valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void englishShiftAndBackspaceHaveCommandIcons() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);

        assertEquals(KeyIcon.SHIFT, findKey(rows, "Shift").icon);
        assertEquals(KeyIcon.BACKSPACE, findKey(rows, "삭제").icon);
        assertEquals(KeyIcon.NONE, findKey(rows, "a").icon);
    }

    @Test
    public void bottomCommandsHaveIconsButLetterKeysKeepText() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        KeyboardRow bottom = rows.get(rows.size() - 1);

        assertEquals(
                "1,1,1,1,1",
                bottom.keys.stream()
                        .map(key -> key.icon == KeyIcon.NONE ? "0" : "1")
                        .collect(Collectors.joining(",")));
        assertEquals(KeyIcon.NONE, findKey(rows, "q").icon);
    }

    @Test
    public void reservedBottomKeyOffersConfigurablePhraseGestures() {
        GestureKey reserved = findKey(KeyboardLayoutFactory.build(KeyboardSettings.defaults()), "예약어");

        assertEquals(KeyboardCommands.CMD_RESERVED_PHRASES, reserved.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_RESERVED_LEFT, reserved.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_RESERVED_RIGHT, reserved.valueFor(GestureAction.RIGHT));
        assertEquals(KeyboardCommands.CMD_RESERVED_UP, reserved.valueFor(GestureAction.UP));
    }

    @Test
    public void optionsKeySlidesSwitchHandednessModes() {
        GestureKey options = findKey(KeyboardLayoutFactory.build(KeyboardSettings.defaults()), "옵션");

        assertEquals(KeyboardCommands.CMD_OPEN_OPTIONS, options.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_HAND_LEFT, options.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_HAND_RIGHT, options.valueFor(GestureAction.RIGHT));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, options.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_HAND_BALANCED, options.valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, options.valueFor(GestureAction.LONG_PRESS));
    }

    @Test
    public void remoteModeUsesPlainPcBottomRowWithStickyModifierLongPresses() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        KeyboardRow bottom = rows.get(rows.size() - 1);

        assertEquals("Ctrl,Win,Alt,스페이스,한/영,옵션,전송", labels(bottom));
        assertEquals("2,2,2,8,2,2,2", widths(bottom));
        assertEquals(
                "Ctrl,Win,Alt,스페이스,한/영,옵션,전송",
                bottomLabels(settings.withHandednessPreset(HandednessMode.LEFT)));
        assertEquals(
                "0,0,0,1,1,1,1",
                bottom.keys.stream()
                        .map(key -> key.icon == KeyIcon.NONE ? "0" : "1")
                        .collect(Collectors.joining(",")));

        GestureKey ctrl = bottom.keys.get(0);
        GestureKey win = bottom.keys.get(1);
        GestureKey alt = bottom.keys.get(2);
        GestureKey space = bottom.keys.get(3);
        GestureKey language = bottom.keys.get(4);
        GestureKey menu = bottom.keys.get(5);
        GestureKey enter = bottom.keys.get(6);

        assertEquals(KeyboardCommands.CMD_REMOTE_CTRL_LATCH, ctrl.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_CTRL_LOCK, ctrl.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_REMOTE_WIN_LATCH, win.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_WIN_LOCK, win.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_REMOTE_ALT_LATCH, alt.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_ALT_LOCK, alt.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_SPACE, space.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_ARROW_UP, space.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_REMOTE_ARROW_DOWN, space.valueFor(GestureAction.DOWN));
        assertEquals(KeyboardCommands.CMD_REMOTE_ARROW_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_REMOTE_ARROW_RIGHT, space.valueFor(GestureAction.RIGHT));
        assertNull(space.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_REMOTE_IME_TOGGLE, language.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_TOGGLE_LANGUAGE, language.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_OPEN_OPTIONS, menu.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, menu.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, menu.valueFor(GestureAction.LONG_PRESS));
        assertEquals(KeyboardCommands.CMD_ENTER, enter.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_REMOTE_CTRL_ENTER, enter.valueFor(GestureAction.LONG_PRESS));
    }

    private GestureKey findKey(List<KeyboardRow> rows, String label) {
        for (KeyboardRow row : rows) {
            for (GestureKey key : row.keys) {
                if (label.equals(key.label)) {
                    return key;
                }
            }
        }
        throw new AssertionError("Key not found: " + label);
    }

    private boolean containsKey(List<KeyboardRow> rows, String label) {
        for (KeyboardRow row : rows) {
            for (GestureKey key : row.keys) {
                if (label.equals(key.label)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void assertDirections(
            GestureKey key,
            String up,
            String down,
            String left,
            String right) {
        assertEquals(up, key.upSlide);
        assertEquals(down, key.downSlide);
        assertEquals(left, key.leftSlide);
        assertEquals(right, key.rightSlide);
    }

    private String bottomLabels(KeyboardSettings settings) {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        return labels(rows.get(rows.size() - 1));
    }

    private String bottomWidths(KeyboardSettings settings) {
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        return widths(rows.get(rows.size() - 1));
    }

    private String labels(KeyboardRow row) {
        return row.keys.stream()
                .map(key -> key.label)
                .collect(Collectors.joining(","));
    }

    private String widths(KeyboardRow row) {
        return row.keys.stream()
                .map(key -> String.valueOf(key.widthUnits))
                .collect(Collectors.joining(","));
    }
}
