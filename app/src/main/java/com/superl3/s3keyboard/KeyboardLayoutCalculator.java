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
        float usableHeight = Math.max(rows.size(), height - topPadding - bottomPadding);
        float bottomRowHeight = bottomRowHeight(usableHeight, bottomRowTopPadding, rows.size(), safeDensity);
        float nonBottomHeight = rows.size() > 1
                ? Math.max(rows.size() - 1, usableHeight - bottomRowTopPadding - bottomRowHeight)
                : usableHeight;
        float characterRowHeight = rows.size() > 1
                ? nonBottomHeight / (float) (rows.size() - 1)
                : usableHeight;

        List<Slot> slots = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            KeyboardRow row = rows.get(rowIndex);
            boolean bottomRow = rowIndex == rows.size() - 1;
            boolean hangulCharacterRow = isHangulCharacterRow(settings, row, bottomRow);
            float rowHeight = bottomRow ? bottomRowHeight : characterRowHeight;
            float rowSpecialGap = hangulCharacterRow
                    ? Math.min(dp(settings.hangulMainSpecialGapDp, safeDensity), Math.max(0f, availableWidth - 1f))
                    : 0f;
            float rowAvailableWidth = Math.max(1f, availableWidth - rowSpecialGap);
            float top = topPadding + (bottomRow
                    ? characterRowHeight * (rows.size() - 1) + bottomRowTopPadding
                    : rowIndex * characterRowHeight);
            float unitWidth = Math.max(0.1f, rowAvailableWidth / (float) row.baseUnits);
            float contentWidth = KeyboardRowMetrics.contentWidth(row, unitWidth, 0f) + rowSpecialGap;
            float maxContentWidth = KeyboardRowMetrics.maxContentWidth(row, unitWidth, 0f) + rowSpecialGap;
            float left = leftInset + Math.max(0f, (maxContentWidth - contentWidth) / 2f);

            for (int keyIndex = 0; keyIndex < row.keys.size(); keyIndex++) {
                GestureKey key = row.keys.get(keyIndex);
                float right = left + KeyboardRowMetrics.keyWidth(key, unitWidth, 0f);
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
                left = right;
                if (hangulCharacterRow && keyIndex == 2) {
                    left += rowSpecialGap;
                }
            }
        }
        return slots;
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
