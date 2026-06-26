package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class KeyboardLayoutCalculatorTest {
    @Test
    public void qwertyUsesEnglishSidePaddingOnly() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false)
                .withHangulSidePadding(40, 50)
                .withEnglishSidePadding(10, 30);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                300f,
                200f,
                1f);

        assertEquals(10f, slots.get(0).left, 0.001f);
        assertEquals(270f, slots.get(9).right, 0.001f);
    }

    @Test
    public void dingulMainSpecialGapAppliesOnlyAboveBottomControls() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulSidePadding(12, 18)
                .withLayoutSpacing(8, 4, 6);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                320f,
                250f,
                1f);

        KeyboardLayoutCalculator.Slot thirdMainKey = slots.get(2);
        KeyboardLayoutCalculator.Slot specialKey = slots.get(3);
        KeyboardLayoutCalculator.Slot bottomFirst = slots.get(16);
        KeyboardLayoutCalculator.Slot bottomSecond = slots.get(17);
        KeyboardLayoutCalculator.Slot bottomLast = slots.get(20);

        assertEquals(settings.hangulMainSpecialGapDp + settings.keyGapDp,
                specialKey.left - thirdMainKey.right,
                0.001f);
        assertEquals(0f, bottomSecond.left - bottomFirst.right, 0.001f);
        assertEquals(1, bottomFirst.bottomSpaceDirection);
        assertEquals(1, bottomSecond.bottomSpaceDirection);
        assertEquals(0, slots.get(18).bottomSpaceDirection);
        assertEquals(-1, slots.get(19).bottomSpaceDirection);
        assertEquals(-1, bottomLast.bottomSpaceDirection);
        assertEquals(200f, bottomFirst.top, 0.001f);
        assertEquals(246f, bottomLast.bottom, 0.001f);
    }

    @Test
    public void bottomControlRowHeightMatchesBetweenHangulAndQwerty() {
        KeyboardSettings hangul = KeyboardSettings.defaults().withHangulNumberRow(false);
        KeyboardSettings english = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> hangulSlots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(hangul),
                hangul,
                320f,
                hangul.measuredHeightDp(),
                1f);
        List<KeyboardLayoutCalculator.Slot> englishSlots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(english),
                english,
                320f,
                english.measuredHeightDp(),
                1f);

        KeyboardLayoutCalculator.Slot hangulBottom = hangulSlots.get(hangulSlots.size() - 1);
        KeyboardLayoutCalculator.Slot englishBottom = englishSlots.get(englishSlots.size() - 1);

        assertEquals(
                hangulBottom.bottom - hangulBottom.top,
                englishBottom.bottom - englishBottom.top,
                0.001f);
        assertEquals(
                KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP,
                englishBottom.bottom - englishBottom.top,
                0.001f);
    }

    @Test
    public void bottomControlRowShrinksForCompactThemePreviews() {
        KeyboardSettings english = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(english),
                english,
                320f,
                88f,
                1f);

        KeyboardLayoutCalculator.Slot topRow = slots.get(0);
        KeyboardLayoutCalculator.Slot bottomRow = slots.get(slots.size() - 1);
        float topRowHeight = topRow.bottom - topRow.top;
        float bottomRowHeight = bottomRow.bottom - bottomRow.top;

        assertEquals(20.475f, bottomRowHeight, 0.001f);
        assertEquals(true, bottomRowHeight <= topRowHeight * 1.1f);
    }

    @Test
    public void keyboardTopPaddingMovesRowsWithinMeasuredHeight() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulNumberRow(false)
                .withLayoutSpacing(8, 12, 4, 0);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                320f,
                250f,
                1f);

        assertEquals(12f, slots.get(0).top, 0.001f);
        assertEquals(246f, slots.get(slots.size() - 1).bottom, 0.001f);
    }

    @Test
    public void numberRowBottomGapSeparatesNumberRowFromMainRows() {
        KeyboardSettings noGap = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(true)
                .withNumberRowBottomGap(0);
        KeyboardSettings withGap = noGap.withNumberRowBottomGap(14);

        List<KeyboardLayoutCalculator.Slot> noGapSlots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(noGap),
                noGap,
                320f,
                noGap.measuredHeightDp(),
                1f);
        List<KeyboardLayoutCalculator.Slot> withGapSlots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(withGap),
                withGap,
                320f,
                withGap.measuredHeightDp(),
                1f);

        assertEquals(0f, noGapSlots.get(10).top - noGapSlots.get(0).bottom, 0.001f);
        assertEquals(14f, withGapSlots.get(10).top - withGapSlots.get(0).bottom, 0.001f);
        assertEquals(noGapSlots.get(0).top, withGapSlots.get(0).top, 0.001f);
    }

    @Test
    public void numberRowHeightMatchesBottomControlRowHeight() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(true);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                320f,
                settings.measuredHeightDp(),
                1f);

        KeyboardLayoutCalculator.Slot numberRow = slots.get(0);
        KeyboardLayoutCalculator.Slot bottomRow = slots.get(slots.size() - 1);

        assertEquals(
                bottomRow.bottom - bottomRow.top,
                numberRow.bottom - numberRow.top,
                0.001f);
        assertEquals(
                KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP,
                numberRow.bottom - numberRow.top,
                0.001f);
    }

    @Test
    public void keyGapCreatesPhysicalSpaceAndShrinksKeys() {
        KeyboardSettings withoutGap = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false)
                .withKeyGap(0);
        KeyboardSettings withGap = withoutGap.withKeyGap(18);

        List<KeyboardLayoutCalculator.Slot> first = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(withoutGap),
                withoutGap,
                320f,
                250f,
                1f);
        List<KeyboardLayoutCalculator.Slot> second = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(withGap),
                withGap,
                320f,
                250f,
                1f);

        float noGapWidth = first.get(0).right - first.get(0).left;
        float withGapWidth = second.get(0).right - second.get(0).left;

        assertEquals(18f, second.get(1).left - second.get(0).right, 0.001f);
        assertEquals(first.get(0).left, second.get(0).left, 0.001f);
        assertTrue(withGapWidth < noGapWidth);
        assertEquals(first.get(first.size() - 1).bottom, second.get(second.size() - 1).bottom, 0.001f);
    }

    @Test
    public void defaultErgonomicsKeepsLegacyDingulLayoutUnchanged() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> legacy = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                260f,
                1f);
        List<KeyboardLayoutCalculator.Slot> ergonomicDefault = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsOptions.DEFAULT,
                360f,
                260f,
                1f);

        assertEquals(legacy.size(), ergonomicDefault.size());
        for (int i = 0; i < legacy.size(); i++) {
            assertSlotEquals(legacy.get(i), ergonomicDefault.get(i));
        }
    }

    @Test
    public void stableErgonomicsCentersDingulMainKeysAndShrinksFunctionVisualOnly() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        KeyboardErgonomicsOptions options = new KeyboardErgonomicsOptions(
                true,
                true,
                false,
                false,
                false,
                true,
                VisualConsistencyLevel.NONE);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                options,
                360f,
                260f,
                1f);

        KeyboardLayoutCalculator.Slot firstMain = slots.get(0);
        KeyboardLayoutCalculator.Slot thirdMain = slots.get(2);
        KeyboardLayoutCalculator.Slot specialSecondRow = slots.get(7);

        assertEquals(180f, (firstMain.left + thirdMain.right) / 2f, 0.001f);
        assertTrue(specialSecondRow.right - specialSecondRow.left
                < specialSecondRow.hitRight - specialSecondRow.hitLeft);
        assertEquals(354f, specialSecondRow.hitRight, 0.001f);
    }

    @Test
    public void defaultErgonomicsDoesNotCreateLeftAssistRail() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsOptions.DEFAULT,
                360f,
                260f,
                1f);

        assertEquals(21, slots.size());
        assertEquals(0, countAssistRailKeys(slots));
    }

    @Test
    public void leftAssistRailCreatesOneAssistKeyPerDingulRow() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.ERGONOMIC.options,
                360f,
                260f,
                1f);

        assertEquals(25, slots.size());
        assertEquals(4, countAssistRailKeys(slots));
        assertEquals(KeyboardCommands.CMD_CLIPBOARD_PANEL, slots.get(0).key.tap);
        assertEquals(KeyboardCommands.CMD_VOICE_INPUT, slots.get(5).key.tap);
        assertEquals(KeyboardCommands.CMD_UNDO, slots.get(10).key.tap);
        assertEquals(KeyboardCommands.CMD_TOOLS, slots.get(15).key.tap);
    }

    @Test
    public void leftAssistMainAndRightRailShareRowCenters() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.ERGONOMIC.options,
                360f,
                260f,
                1f);

        for (int row = 0; row < 4; row++) {
            int offset = row * 5;
            float center = centerY(slots.get(offset + 1));
            assertEquals(center, centerY(slots.get(offset)), 0.001f);
            assertEquals(center, centerY(slots.get(offset + 2)), 0.001f);
            assertEquals(center, centerY(slots.get(offset + 3)), 0.001f);
            assertEquals(center, centerY(slots.get(offset + 4)), 0.001f);
        }
    }

    @Test
    public void uniformGridGapKeepsFiveColumnVisualGapsConsistent() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHangulNumberRow(false)
                .withKeyGap(6);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.ERGONOMIC.options,
                360f,
                260f,
                1f);

        for (int row = 0; row < 4; row++) {
            int offset = row * 5;
            float expectedGap = slots.get(offset + 2).left - slots.get(offset + 1).right;
            assertEquals(expectedGap, slots.get(offset + 1).left - slots.get(offset).right, 0.001f);
            assertEquals(expectedGap, slots.get(offset + 3).left - slots.get(offset + 2).right, 0.001f);
            assertEquals(expectedGap, slots.get(offset + 4).left - slots.get(offset + 3).right, 0.001f);
        }
    }

    @Test
    public void leftAssistRailDoesNotChangeMainDingulKeyOrder() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> base = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsOptions.DEFAULT,
                360f,
                260f,
                1f);
        List<KeyboardLayoutCalculator.Slot> assisted = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.ERGONOMIC.options,
                360f,
                260f,
                1f);

        assertEquals(mainKeySignature(base), mainKeySignature(assisted));
    }

    @Test
    public void compactAssistAndFunctionKeysKeepUsableHitRects() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.ERGONOMIC.options,
                360f,
                260f,
                1f);

        KeyboardLayoutCalculator.Slot assist = slots.get(0);
        KeyboardLayoutCalculator.Slot backspace = slots.get(4);

        assertTrue(width(assist) < hitWidth(assist));
        assertTrue(width(backspace) <= hitWidth(backspace));
        assertTrue(hitWidth(assist) >= 40f);
        assertTrue(hitWidth(backspace) >= 40f);
        assertTrue(assist.hitRight > assist.right);
        assertTrue(backspace.hitLeft < backspace.left);
    }

    @Test
    public void ergonomicsDoNotCenterQwertyLayouts() {
        KeyboardErgonomicsOptions aggressive = KeyboardErgonomicsPreset.AGGRESSIVE.options;
        KeyboardSettings english = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        assertLayoutUnchangedWithErgonomics(
                KeyboardLayoutFactory.build(english),
                english,
                aggressive);

        KeyboardSettings hangul = KeyboardSettings.defaults().withHangulNumberRow(false);
        KeyboardLayoutProfiles qwertyHangul = KeyboardLayoutProfiles.defaults()
                .withHangulLayout(KeyboardLayoutProfile.QWERTY);
        assertLayoutUnchangedWithErgonomics(
                KeyboardLayoutFactory.build(hangul, KeyboardSurface.NORMAL, qwertyHangul),
                hangul,
                aggressive);
    }

    @Test
    public void ergonomicHitboxShrinksBezelSideOfFunctionRailAndExpandsBackspaceInward() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        KeyboardErgonomicsOptions options = KeyboardErgonomicsPreset.STABLE.options;

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                options,
                360f,
                260f,
                1f);

        KeyboardLayoutCalculator.Slot backspace = slots.get(3);

        assertTrue(backspace.hitLeft < backspace.left);
        assertTrue(backspace.hitRight >= backspace.right);
        assertTrue(backspace.hitBottom > backspace.bottom);
        assertTrue(360f - backspace.hitRight >= 4f);
        assertTrue(backspace.hitLeft <= backspace.left);
        assertTrue(backspace.hitTop <= backspace.top);
        assertTrue(backspace.hitBottom >= backspace.bottom);
    }

    @Test
    public void backspaceHitRectContainsVisualRectWithCompactRailAndErgonomicHitbox() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.STABLE.options,
                360f,
                260f,
                1f);

        KeyboardLayoutCalculator.Slot backspace = slots.get(3);

        assertTrue(backspace.hitLeft <= backspace.left);
        assertTrue(backspace.hitTop <= backspace.top);
        assertTrue(backspace.hitRight >= backspace.right);
        assertTrue(backspace.hitBottom >= backspace.bottom);
        assertTrue(backspace.hitLeft < backspace.left);
        assertTrue(backspace.hitBottom > backspace.bottom);
    }

    @Test
    public void ergonomicSlideOriginUsesVisualCenterNotHitRectCenter() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.STABLE.options,
                360f,
                260f,
                1f);

        KeyboardLayoutCalculator.Slot backspace = slots.get(3);
        float visualCenterX = (backspace.left + backspace.right) / 2f;
        float hitCenterX = (backspace.hitLeft + backspace.hitRight) / 2f;

        assertEquals(visualCenterX, backspace.gestureOriginX, 0.001f);
        assertTrue(Math.abs(hitCenterX - backspace.gestureOriginX) > 0.1f);
    }

    @Test
    public void allDingulErgonomicOptionCombinationsStayInsideScreen() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        boolean[] flags = {false, true};

        for (boolean center : flags) {
            for (boolean compact : flags) {
                for (boolean hitbox : flags) {
                    for (boolean position : flags) {
                        for (boolean leftAssist : flags) {
                            for (boolean uniformGap : flags) {
                                for (VisualConsistencyLevel level : VisualConsistencyLevel.values()) {
                                    KeyboardErgonomicsOptions options = new KeyboardErgonomicsOptions(
                                            center,
                                            compact,
                                            hitbox,
                                            position,
                                            leftAssist,
                                            uniformGap,
                                            level);
                                    List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                                            KeyboardLayoutFactory.build(settings),
                                            settings,
                                            options,
                                            360f,
                                            260f,
                                            1f);
                                    for (KeyboardLayoutCalculator.Slot slot : slots) {
                                        assertInside(slot.left, 0f, 360f);
                                        assertInside(slot.right, 0f, 360f);
                                        assertInside(slot.top, 0f, 260f);
                                        assertInside(slot.bottom, 0f, 260f);
                                        assertInside(slot.hitLeft, 0f, 360f);
                                        assertInside(slot.hitRight, 0f, 360f);
                                        assertInside(slot.hitTop, 0f, 260f);
                                        assertInside(slot.hitBottom, 0f, 260f);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void ergonomicPositionAdjustKeepsDingulMainKeyOrderAndBoundedShift() {
        KeyboardSettings settings = KeyboardSettings.defaults().withHangulNumberRow(false);
        List<KeyboardLayoutCalculator.Slot> base = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                260f,
                1f);
        KeyboardErgonomicsOptions options = new KeyboardErgonomicsOptions(
                false,
                false,
                false,
                true,
                false,
                false,
                VisualConsistencyLevel.STRONG);
        List<KeyboardLayoutCalculator.Slot> adjusted = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                options,
                360f,
                260f,
                1f);

        assertTrue(adjusted.get(0).right < adjusted.get(1).left);
        assertTrue(adjusted.get(1).right < adjusted.get(2).left);
        for (int i = 0; i < 3; i++) {
            float baseCenter = (base.get(i).left + base.get(i).right) / 2f;
            float adjustedCenter = (adjusted.get(i).left + adjusted.get(i).right) / 2f;
            float maxShift = Math.min(
                    base.get(i).right - base.get(i).left,
                    base.get(i).bottom - base.get(i).top)
                    * VisualConsistencyLevel.STRONG.maxMainShiftRatio;
            assertTrue(Math.abs(adjustedCenter - baseCenter) <= maxShift + 0.001f);
        }
    }

    private static void assertLayoutUnchangedWithErgonomics(
            List<KeyboardRow> rows,
            KeyboardSettings settings,
            KeyboardErgonomicsOptions options) {
        List<KeyboardLayoutCalculator.Slot> base = KeyboardLayoutCalculator.layout(
                rows,
                settings,
                360f,
                260f,
                1f);
        List<KeyboardLayoutCalculator.Slot> adjusted = KeyboardLayoutCalculator.layout(
                rows,
                settings,
                options,
                360f,
                260f,
                1f);

        assertEquals(base.size(), adjusted.size());
        for (int i = 0; i < base.size(); i++) {
            assertSlotEquals(base.get(i), adjusted.get(i));
        }
    }

    private static void assertSlotEquals(
            KeyboardLayoutCalculator.Slot expected,
            KeyboardLayoutCalculator.Slot actual) {
        assertEquals(expected.left, actual.left, 0.001f);
        assertEquals(expected.top, actual.top, 0.001f);
        assertEquals(expected.right, actual.right, 0.001f);
        assertEquals(expected.bottom, actual.bottom, 0.001f);
        assertEquals(expected.hitLeft, actual.hitLeft, 0.001f);
        assertEquals(expected.hitTop, actual.hitTop, 0.001f);
        assertEquals(expected.hitRight, actual.hitRight, 0.001f);
        assertEquals(expected.hitBottom, actual.hitBottom, 0.001f);
        assertEquals(expected.gestureOriginX, actual.gestureOriginX, 0.001f);
        assertEquals(expected.gestureOriginY, actual.gestureOriginY, 0.001f);
        assertEquals(expected.primaryBottomControl, actual.primaryBottomControl);
        assertEquals(expected.compactSpecialColumn, actual.compactSpecialColumn);
        assertEquals(expected.bottomSpaceDirection, actual.bottomSpaceDirection);
    }

    private static void assertInside(float value, float min, float max) {
        assertTrue(value >= min - 0.001f);
        assertTrue(value <= max + 0.001f);
    }

    private static int countAssistRailKeys(List<KeyboardLayoutCalculator.Slot> slots) {
        int count = 0;
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            if (isAssistRailKey(slot.key)) {
                count++;
            }
        }
        return count;
    }

    private static String mainKeySignature(List<KeyboardLayoutCalculator.Slot> slots) {
        StringBuilder builder = new StringBuilder();
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            if (isAssistRailKey(slot.key)
                    || KeyboardCommands.CMD_DELETE.equals(slot.key.tap)
                    || "?".equals(slot.key.label)
                    || ".".equals(slot.key.label)
                    || "/".equals(slot.key.label)
                    || KeyboardCommands.CMD_SPACE.equals(slot.key.tap)
                    || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(slot.key.tap)
                    || KeyboardCommands.CMD_ENTER.equals(slot.key.tap)
                    || KeyboardCommands.CMD_OPEN_OPTIONS.equals(slot.key.tap)
                    || KeyboardCommands.CMD_RESERVED_PHRASES.equals(slot.key.tap)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(slot.key.label).append(':').append(slot.key.tap);
        }
        return builder.toString();
    }

    private static boolean isAssistRailKey(GestureKey key) {
        return KeyboardCommands.CMD_CLIPBOARD_PANEL.equals(key.tap)
                || KeyboardCommands.CMD_VOICE_INPUT.equals(key.tap)
                || KeyboardCommands.CMD_UNDO.equals(key.tap)
                || KeyboardCommands.CMD_TOOLS.equals(key.tap);
    }

    private static float centerY(KeyboardLayoutCalculator.Slot slot) {
        return (slot.top + slot.bottom) / 2f;
    }

    private static float width(KeyboardLayoutCalculator.Slot slot) {
        return slot.right - slot.left;
    }

    private static float hitWidth(KeyboardLayoutCalculator.Slot slot) {
        return slot.hitRight - slot.hitLeft;
    }
}
