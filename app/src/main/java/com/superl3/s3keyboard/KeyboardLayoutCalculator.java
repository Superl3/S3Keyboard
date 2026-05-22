package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class KeyboardLayoutCalculator {
    private KeyboardLayoutCalculator() {
    }

    static List<Slot> layout(
            List<KeyboardRow> rows,
            KeyboardSettings settings,
            float width,
            float height,
            float density) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        float safeDensity = Math.max(0.1f, density);
        float leftInset = dp(settings.activeLeftPaddingDp(), safeDensity);
        float rightInset = dp(settings.activeRightPaddingDp(), safeDensity);
        float minimumKeyboardWidth = Math.min(width, dp(160, safeDensity));
        float availableWidth = width - leftInset - rightInset;
        if (availableWidth < minimumKeyboardWidth && leftInset + rightInset > 0) {
            float scale = Math.max(0f, (width - minimumKeyboardWidth) / (leftInset + rightInset));
            leftInset *= scale;
            rightInset *= scale;
            availableWidth = width - leftInset - rightInset;
        }
        availableWidth = Math.max(minimumKeyboardWidth, availableWidth);

        float topPadding = dp(settings.keyboardTopPaddingDp, safeDensity);
        float bottomPadding = dp(settings.keyboardBottomPaddingDp, safeDensity);
        float bottomRowTopPadding = rows.size() > 1 ? dp(settings.bottomRowTopPaddingDp, safeDensity) : 0f;
        float numberRowBottomGap = hasAdditionalNumberRow(settings, rows)
                ? dp(settings.numberRowBottomGapDp, safeDensity)
                : 0f;
        float keyGap = dp(settings.keyGapDp, safeDensity);
        boolean hasNumberRow = hasAdditionalNumberRow(settings, rows);
        float usableHeight = Math.max(rows.size(), height - topPadding - bottomPadding);
        float bottomRowHeight = bottomRowHeight(usableHeight, bottomRowTopPadding, rows.size(), safeDensity);
        float numberRowHeight = hasNumberRow ? bottomRowHeight : 0f;
        int characterRowCount = Math.max(1, rows.size() - (hasNumberRow ? 2 : 1));
        float nonBottomHeight = rows.size() > 1
                ? Math.max(characterRowCount,
                usableHeight - bottomRowTopPadding - bottomRowHeight - numberRowBottomGap - numberRowHeight)
                : usableHeight;
        float characterRowHeight = rows.size() > 1
                ? nonBottomHeight / (float) characterRowCount
                : usableHeight;

        List<Slot> slots = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            KeyboardRow row = rows.get(rowIndex);
            boolean bottomRow = rowIndex == rows.size() - 1;
            boolean hangulCharacterRow = isHangulCharacterRow(settings, row, bottomRow);
            boolean numberRow = hasNumberRow && rowIndex == 0;
            float rowHeight = bottomRow ? bottomRowHeight : (numberRow ? numberRowHeight : characterRowHeight);
            float rowSpecialGap = hangulCharacterRow
                    ? Math.min(dp(settings.hangulMainSpecialGapDp, safeDensity), Math.max(0f, availableWidth - 1f))
                    : 0f;
            float rowAvailableWidth = Math.max(1f, availableWidth - rowSpecialGap);
            float rowGap = rowGap(row, rowAvailableWidth, keyGap);
            float top = topPadding + topForRow(
                    rowIndex,
                    rows.size(),
                    hasNumberRow,
                    characterRowHeight,
                    numberRowHeight,
                    numberRowBottomGap,
                    bottomRowTopPadding);
            float unitWidth = Math.max(
                    0.1f,
                    (rowAvailableWidth - rowGap * Math.max(0, row.keys.size() - 1))
                            / (float) row.baseUnits);
            float contentWidth = KeyboardRowMetrics.contentWidth(row, unitWidth, rowGap) + rowSpecialGap;
            float maxContentWidth = KeyboardRowMetrics.maxContentWidth(row, unitWidth, rowGap) + rowSpecialGap;
            float left = leftInset + Math.max(0f, (maxContentWidth - contentWidth) / 2f);

            for (int keyIndex = 0; keyIndex < row.keys.size(); keyIndex++) {
                GestureKey key = row.keys.get(keyIndex);
                float right = left + KeyboardRowMetrics.keyWidth(key, unitWidth, rowGap);
                boolean primaryBottomControl = bottomRow
                        && (KeyboardCommands.CMD_SPACE.equals(key.tap)
                        || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)
                        || KeyboardCommands.CMD_ENTER.equals(key.tap));
                slots.add(new Slot(
                        key,
                        left,
                        top,
                        right,
                        top + rowHeight,
                        primaryBottomControl,
                        hangulCharacterRow && keyIndex == row.keys.size() - 1));
                left = right + rowGap;
                if (hangulCharacterRow && keyIndex == 2) {
                    left += rowSpecialGap;
                }
            }
        }
        return slots;
    }

    private static boolean hasAdditionalNumberRow(KeyboardSettings settings, List<KeyboardRow> rows) {
        return settings.showNumberRow
                && rows.size() > 2
                && !rows.get(0).keys.isEmpty()
                && "1".equals(rows.get(0).keys.get(0).tap);
    }

    private static float bottomRowHeight(
            float usableHeight,
            float bottomRowTopPadding,
            int rowCount,
            float density) {
        if (rowCount <= 1) {
            return usableHeight;
        }
        float desired = dp(KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP, density);
        float balanced = Math.max(1f, usableHeight / (float) rowCount * 1.05f);
        float maxHeight = Math.max(1f, usableHeight - bottomRowTopPadding - (rowCount - 1));
        return Math.min(Math.min(desired, balanced), maxHeight);
    }

    private static float topForRow(
            int rowIndex,
            int rowCount,
            boolean hasNumberRow,
            float characterRowHeight,
            float numberRowHeight,
            float numberRowBottomGap,
            float bottomRowTopPadding) {
        if (!hasNumberRow) {
            return rowIndex == rowCount - 1
                    ? characterRowHeight * (rowCount - 1) + bottomRowTopPadding
                    : rowIndex * characterRowHeight;
        }
        if (rowIndex == 0) {
            return 0f;
        }
        int characterIndex = rowIndex - 1;
        if (rowIndex == rowCount - 1) {
            return numberRowHeight
                    + numberRowBottomGap
                    + characterRowHeight * characterIndex
                    + bottomRowTopPadding;
        }
        return numberRowHeight + numberRowBottomGap + characterRowHeight * characterIndex;
    }

    private static boolean isHangulCharacterRow(
            KeyboardSettings settings,
            KeyboardRow row,
            boolean bottomRow) {
        return settings.keyboardMode == KeyboardMode.HANGUL
                && !bottomRow
                && row.keys.size() == 4;
    }

    private static float dp(int value, float density) {
        return value * density;
    }

    private static float rowGap(KeyboardRow row, float rowAvailableWidth, float requestedGap) {
        if (row.keys.size() <= 1 || requestedGap <= 0f) {
            return 0f;
        }
        float maxGap = rowAvailableWidth / (float) Math.max(1, row.keys.size() - 1);
        return Math.min(requestedGap, Math.max(0f, maxGap - 0.1f));
    }

    static final class Slot {
        final GestureKey key;
        final float left;
        final float top;
        final float right;
        final float bottom;
        final boolean primaryBottomControl;
        final boolean compactSpecialColumn;

        Slot(
                GestureKey key,
                float left,
                float top,
                float right,
                float bottom,
                boolean primaryBottomControl,
                boolean compactSpecialColumn) {
            this.key = key;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.primaryBottomControl = primaryBottomControl;
            this.compactSpecialColumn = compactSpecialColumn;
        }
    }
}
