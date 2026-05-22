package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public final class KeyboardRowMetricsTest {
    @Test
    public void fullWidthRowsUseTheSameVisualContentWidthEvenWithDifferentKeyCounts() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        float availableWidth = 240f;
        float gap = 1f;

        float top = contentWidth(rows.get(0), availableWidth, gap);
        float lower = contentWidth(rows.get(2), availableWidth, gap);
        float bottom = contentWidth(rows.get(rows.size() - 1), availableWidth, gap);
        float max = maxContentWidth(rows.get(0), availableWidth, gap);

        assertEquals(max, top, 0.001f);
        assertEquals(max, lower, 0.001f);
        assertEquals(max, bottom, 0.001f);
    }

    @Test
    public void qwertyHomeRowIsTheOnlyShorterLetterRow() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        float availableWidth = 240f;
        float gap = 1f;

        assertEquals(18, KeyboardRowMetrics.contentUnits(rows.get(1)));
        assertEquals(216.8f, contentWidth(rows.get(1), availableWidth, gap), 0.001f);
        assertEquals(240f, maxContentWidth(rows.get(1), availableWidth, gap), 0.001f);
    }

    @Test
    public void qwertyShiftAndBackspaceAlignWithBottomEdgeCommands() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(false);
        List<KeyboardRow> rows = KeyboardLayoutFactory.build(settings);
        KeyboardRow qwertyBottom = rows.get(2);
        KeyboardRow bottomControls = rows.get(rows.size() - 1);
        float rowLeft = 1f;
        float availableWidth = 240f;
        float gap = 1f;
        float qwertyUnitWidth = unitWidth(qwertyBottom, availableWidth, gap);
        float bottomUnitWidth = unitWidth(bottomControls, availableWidth, gap);

        assertEquals("Shift", qwertyBottom.keys.get(0).label);
        assertEquals("옵션", bottomControls.keys.get(0).label);
        assertEquals("삭제", qwertyBottom.keys.get(qwertyBottom.keys.size() - 1).label);
        assertEquals("전송", bottomControls.keys.get(bottomControls.keys.size() - 1).label);

        assertEquals(
                KeyboardRowMetrics.keyLeft(bottomControls, 0, rowLeft, bottomUnitWidth, gap),
                KeyboardRowMetrics.keyLeft(qwertyBottom, 0, rowLeft, qwertyUnitWidth, gap),
                0.001f);
        assertEquals(
                KeyboardRowMetrics.keyWidth(bottomControls.keys.get(0), bottomUnitWidth, gap),
                KeyboardRowMetrics.keyWidth(qwertyBottom.keys.get(0), qwertyUnitWidth, gap),
                1.0f);
        assertEquals(
                KeyboardRowMetrics.keyRight(
                        bottomControls,
                        bottomControls.keys.size() - 1,
                        rowLeft,
                        bottomUnitWidth,
                        gap),
                KeyboardRowMetrics.keyRight(
                        qwertyBottom,
                        qwertyBottom.keys.size() - 1,
                        rowLeft,
                        qwertyUnitWidth,
                        gap),
                0.001f);
        assertEquals(
                KeyboardRowMetrics.keyWidth(
                        bottomControls.keys.get(bottomControls.keys.size() - 1),
                        bottomUnitWidth,
                        gap),
                KeyboardRowMetrics.keyWidth(
                        qwertyBottom.keys.get(qwertyBottom.keys.size() - 1),
                        qwertyUnitWidth,
                        gap),
                1.0f);
    }

    private float contentWidth(KeyboardRow row, float availableWidth, float gap) {
        return KeyboardRowMetrics.contentWidth(row, unitWidth(row, availableWidth, gap), gap);
    }

    private float maxContentWidth(KeyboardRow row, float availableWidth, float gap) {
        return KeyboardRowMetrics.maxContentWidth(row, unitWidth(row, availableWidth, gap), gap);
    }

    private float unitWidth(KeyboardRow row, float availableWidth, float gap) {
        return (availableWidth - gap * Math.max(0, row.keys.size() - 1)) / row.baseUnits;
    }
}
