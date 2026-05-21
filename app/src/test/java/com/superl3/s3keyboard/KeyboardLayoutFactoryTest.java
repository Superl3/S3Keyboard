package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public final class KeyboardLayoutFactoryTest {
    @Test
    public void englishKeyHasTapUpperSlideAndLongPressSymbol() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        GestureKey a = findKey(KeyboardLayoutFactory.build(settings), "a");

        assertEquals("a", a.valueFor(GestureAction.TAP));
        assertEquals("A", a.valueFor(GestureAction.UP));
        assertEquals("@", a.valueFor(GestureAction.DOWN));
        assertEquals("@", a.valueFor(GestureAction.LONG_PRESS));
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
        assertEquals(KeyboardCommands.CMD_MOVE_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_MOVE_RIGHT, space.valueFor(GestureAction.RIGHT));
    }

    @Test
    public void englishBottomLanguageOffersPunctuationAndSpaceHidesUrlGesture() {
        KeyboardSettings settings = KeyboardSettings.defaults().withKeyboardMode(KeyboardMode.ENGLISH);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        GestureKey space = findKey(rows, "스페이스");
        GestureKey language = findKey(rows, "한/영");

        assertEquals(KeyboardCommands.CMD_NOOP, space.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_MOVE_LEFT, space.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_MOVE_RIGHT, space.valueFor(GestureAction.RIGHT));
        assertEquals(KeyboardCommands.CMD_TOGGLE_LANGUAGE, language.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_NOOP, language.valueFor(GestureAction.UP));
        assertEquals(",", language.valueFor(GestureAction.LEFT));
        assertEquals(",", language.valueFor(GestureAction.RIGHT));
        assertEquals(".", language.valueFor(GestureAction.LONG_PRESS));
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
        assertEquals("!", rows.get(0).keys.get(0).valueFor(GestureAction.LONG_PRESS));
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

        assertEquals(KeyboardCommands.CMD_QUICK_SETTINGS, options.valueFor(GestureAction.TAP));
        assertEquals(KeyboardCommands.CMD_HAND_LEFT, options.valueFor(GestureAction.LEFT));
        assertEquals(KeyboardCommands.CMD_HAND_RIGHT, options.valueFor(GestureAction.RIGHT));
        assertEquals(KeyboardCommands.CMD_HAND_BALANCED, options.valueFor(GestureAction.UP));
        assertEquals(KeyboardCommands.CMD_OPEN_OPTIONS, options.valueFor(GestureAction.LONG_PRESS));
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
