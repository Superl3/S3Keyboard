package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

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

        assertEquals(8f, specialKey.left - thirdMainKey.right, 0.001f);
        assertEquals(0f, bottomSecond.left - bottomFirst.right, 0.001f);
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
    public void visualKeyGapDoesNotChangePhysicalLayoutSlots() {
        KeyboardSettings withoutGap = KeyboardSettings.defaults().withKeyGap(0);
        KeyboardSettings withGap = KeyboardSettings.defaults().withKeyGap(18);

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

        assertEquals(first.get(0).left, second.get(0).left, 0.001f);
        assertEquals(first.get(0).right, second.get(0).right, 0.001f);
        assertEquals(first.get(20).bottom, second.get(20).bottom, 0.001f);
    }
}
