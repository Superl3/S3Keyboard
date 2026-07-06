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
        return layout(rows, settings, null, width, height, density);
    }

    static List<Slot> layout(
            List<KeyboardRow> rows,
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions,
            float width,
            float height,
            float density) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        KeyboardErgonomicsOptions safeErgonomicsOptions =
                RuntimeDefaults.keyboardErgonomics(ergonomicsOptions);
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
            float rowGap = bottomRow ? 0f : rowGap(row, rowAvailableWidth, keyGap);
            int bottomSpaceIndex = bottomRow ? spaceIndex(row) : -1;
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

            List<Slot> rowSlots = new ArrayList<>();
            for (int keyIndex = 0; keyIndex < row.keys.size(); keyIndex++) {
                GestureKey key = row.keys.get(keyIndex);
                float right = left + KeyboardRowMetrics.keyWidth(key, unitWidth, rowGap);
                int bottomSpaceDirection = 0;
                if (bottomSpaceIndex >= 0 && keyIndex != bottomSpaceIndex) {
                    bottomSpaceDirection = keyIndex < bottomSpaceIndex ? 1 : -1;
                }
                boolean primaryBottomControl = bottomRow
                        && (KeyboardCommands.CMD_SPACE.equals(key.tap)
                        || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)
                        || KeyboardCommands.CMD_ENTER.equals(key.tap));
                rowSlots.add(new Slot(
                        key,
                        left,
                        top,
                        right,
                        top + rowHeight,
                        primaryBottomControl,
                        hangulCharacterRow && keyIndex == row.keys.size() - 1,
                        hangulCharacterRow && keyIndex == row.keys.size() - 1 ? 1 : 0,
                        bottomSpaceDirection));
                left = right + rowGap;
                if (hangulCharacterRow && keyIndex == 2) {
                    left += rowSpecialGap;
                }
            }
            if (hangulCharacterRow && safeErgonomicsOptions.affectsLayout()) {
                int characterRowIndex = hasNumberRow ? rowIndex - 1 : rowIndex;
                slots.addAll(applyDingulErgonomics(
                        rowSlots,
                        safeErgonomicsOptions,
                        Math.max(0, characterRowIndex),
                        leftInset,
                        leftInset + availableWidth,
                        safeDensity));
            } else {
                slots.addAll(rowSlots);
            }
        }
        return slots;
    }

    private static List<Slot> applyDingulErgonomics(
            List<Slot> rowSlots,
            KeyboardErgonomicsOptions options,
            int characterRowIndex,
            float rowLeft,
            float rowRight,
            float density) {
        if (rowSlots.size() != 4) {
            return rowSlots;
        }
        Slot firstMain = rowSlots.get(0);
        Slot thirdMain = rowSlots.get(2);
        Slot function = rowSlots.get(3);
        float mainLeft = firstMain.left;
        float mainRight = thirdMain.right;
        float mainWidth = mainRight - mainLeft;
        float functionHitWidth = function.right - function.left;
        float functionVisualWidth = functionHitWidth;
        boolean leftAssistRail = options.mainKeyCenteringEnabled && options.leftAssistRailEnabled;
        if (options.compactFunctionRailEnabled) {
            functionVisualWidth *= isBackspaceKey(function.key)
                    ? options.visualConsistencyLevel.backspaceVisualScale
                    : options.visualConsistencyLevel.functionVisualScale;
        }
        float mainColumnGap = rowSlots.get(1).left - rowSlots.get(0).right;
        float functionGap = options.uniformGridGapEnabled && leftAssistRail
                ? Math.max(0f, mainColumnGap)
                : Math.max(function.left - thirdMain.right, dp(8, density));
        float transformedMainLeft = mainLeft;
        float transformedMainWidth = mainWidth;
        float leftAssistHitLeft = function.left - functionHitWidth - functionGap;
        float leftAssistHitRight = leftAssistHitLeft + functionHitWidth;
        float functionHitLeft = function.left;
        float functionHitRight = function.right;

        if (options.mainKeyCenteringEnabled) {
            float availableWidth = Math.max(1f, rowRight - rowLeft);
            if (leftAssistRail) {
                float railWidth = Math.min(functionHitWidth, Math.max(1f, availableWidth * 0.22f));
                float baseGap = functionGap;
                float mainScale = Math.min(
                        1f,
                        Math.max(0.05f, (availableWidth - railWidth * 2f)
                                / Math.max(1f, mainWidth + baseGap * 2f)));
                float gap = baseGap * mainScale;
                transformedMainWidth = mainWidth * mainScale;
                float groupWidth = railWidth * 2f + gap * 2f + transformedMainWidth;
                float groupLeft = rowLeft + (availableWidth - groupWidth) / 2f;
                leftAssistHitLeft = groupLeft;
                leftAssistHitRight = leftAssistHitLeft + railWidth;
                transformedMainLeft = leftAssistHitRight + gap;
                functionHitLeft = transformedMainLeft + transformedMainWidth + gap;
                functionHitRight = functionHitLeft + railWidth;
                functionGap = gap;
                functionHitWidth = railWidth;
                functionVisualWidth = Math.min(functionVisualWidth, railWidth);
            } else {
                float minSideForFunction = functionHitWidth + functionGap;
                float maxCenteredMainWidth = Math.max(1f, availableWidth - minSideForFunction * 2f);
                float mainScale = Math.min(1f, maxCenteredMainWidth / Math.max(1f, mainWidth));
                transformedMainWidth = mainWidth * mainScale;
                transformedMainLeft = rowLeft + (availableWidth - transformedMainWidth) / 2f;
                functionHitRight = rowRight;
                functionHitLeft = functionHitRight - functionHitWidth;
            }
        }

        boolean applyPositionAdjust = options.ergonomicPositionAdjustEnabled
                && !(leftAssistRail && options.uniformGridGapEnabled);
        float railVisualYShift = 0f;
        if (applyPositionAdjust) {
            float firstMainWidth = transformedMainWidth * (firstMain.right - firstMain.left) / mainWidth;
            float maxShift = Math.min(firstMainWidth, firstMain.bottom - firstMain.top)
                    * options.visualConsistencyLevel.maxMainShiftRatio;
            railVisualYShift = ((characterRowIndex - 1.5f) / 1.5f) * 0.25f * maxShift;
        }

        List<Slot> result = new ArrayList<>(leftAssistRail ? 5 : 4);
        if (leftAssistRail) {
            result.add(assistSlot(
                    characterRowIndex,
                    function,
                    leftAssistHitLeft,
                    leftAssistHitRight,
                    rowLeft,
                    rowRight,
                    density,
                    railVisualYShift,
                    options));
        }
        for (int i = 0; i < 3; i++) {
            Slot slot = rowSlots.get(i);
            float left = transformedMainLeft + (slot.left - mainLeft) / mainWidth * transformedMainWidth;
            float right = transformedMainLeft + (slot.right - mainLeft) / mainWidth * transformedMainWidth;
            if (applyPositionAdjust) {
                float maxShift = Math.min(right - left, slot.bottom - slot.top)
                        * options.visualConsistencyLevel.maxMainShiftRatio;
                float xShift = (i == 0 ? 0.55f : (i == 2 ? -0.55f : 0f)) * maxShift;
                float yShift = ((characterRowIndex - 1.5f) / 1.5f) * 0.25f * maxShift;
                left += xShift;
                right += xShift;
                float top = slot.top + yShift;
                float bottom = slot.bottom + yShift;
                Rect rect = clampRect(
                        left,
                        top,
                        right,
                        bottom,
                        rowLeft,
                        slot.top - maxShift,
                        functionHitLeft - functionGap * 0.25f,
                        slot.bottom + maxShift);
                result.add(withHitRect(slot, rect, ergonomicHitRect(
                        rect,
                        rowLeft,
                        functionHitLeft,
                        false,
                        false,
                        density,
                        options.ergonomicHitboxEnabled)));
            } else {
                Rect rect = new Rect(left, slot.top, right, slot.bottom);
                result.add(withHitRect(slot, rect, ergonomicHitRect(
                        rect,
                        rowLeft,
                        functionHitLeft,
                        false,
                        false,
                        density,
                        options.ergonomicHitboxEnabled)));
            }
        }

        Slot functionSlot = rowSlots.get(3);
        boolean backspace = isBackspaceKey(functionSlot.key);
        float functionVisualHeight = functionSlot.bottom - functionSlot.top;
        if (options.compactFunctionRailEnabled) {
            functionVisualHeight *= backspace
                    ? options.visualConsistencyLevel.backspaceVisualScale
                    : options.visualConsistencyLevel.functionVisualScale;
        }
        if (options.ergonomicHitboxEnabled) {
            functionVisualWidth = Math.min(functionVisualWidth, Math.max(1f, functionHitWidth - dp(4, density)));
        }
        float functionCenterY = (functionSlot.top + functionSlot.bottom) / 2f + railVisualYShift;
        float functionVisualTop = functionCenterY - functionVisualHeight / 2f;
        float functionVisualBottom = functionCenterY + functionVisualHeight / 2f;
        float functionVisualLeft = functionHitLeft;
        float functionVisualRight = functionVisualLeft + functionVisualWidth;
        Rect functionVisualRect = new Rect(
                functionVisualLeft,
                functionVisualTop,
                functionVisualRight,
                functionVisualBottom);
        Rect functionHitRect = new Rect(functionHitLeft, functionSlot.top, functionHitRight, functionSlot.bottom);
        if (options.ergonomicHitboxEnabled) {
            functionHitRect = railHitRect(
                    functionHitRect,
                    thirdMain.right,
                    rowRight,
                    false,
                    backspace,
                    density,
                    true);
        }
        result.add(withHitRect(functionSlot, functionVisualRect, functionHitRect));
        return result;
    }

    private static Slot assistSlot(
            int characterRowIndex,
            Slot rowReference,
            float hitLeft,
            float hitRight,
            float rowLeft,
            float rowRight,
            float density,
            float visualYShift,
            KeyboardErgonomicsOptions options) {
        GestureKey key = LeftAssistRailItem.keyForRow(characterRowIndex);
        float hitTop = rowReference.top;
        float hitBottom = rowReference.bottom;
        float scale = options.compactFunctionRailEnabled
                ? options.visualConsistencyLevel.functionVisualScale
                : 1f;
        float visualWidth = (hitRight - hitLeft) * scale;
        float visualHeight = (hitBottom - hitTop) * scale;
        float centerY = (hitTop + hitBottom) / 2f + visualYShift;
        Rect visualRect = new Rect(
                hitRight - visualWidth,
                centerY - visualHeight / 2f,
                hitRight,
                centerY + visualHeight / 2f);
        Rect hitRect = new Rect(hitLeft, hitTop, hitRight, hitBottom);
        if (options.ergonomicHitboxEnabled) {
            hitRect = railHitRect(hitRect, rowLeft, rowRight, true, false, density, true);
        }
        return new Slot(
                key,
                visualRect.left,
                visualRect.top,
                visualRect.right,
                visualRect.bottom,
                hitRect.left,
                hitRect.top,
                hitRect.right,
                hitRect.bottom,
                visualRect.centerX(),
                visualRect.centerY(),
                false,
                true,
                -1,
                0);
    }

    private static Slot withHitRect(Slot slot, Rect visualRect, Rect hitRect) {
        return new Slot(
                slot.key,
                visualRect.left,
                visualRect.top,
                visualRect.right,
                visualRect.bottom,
                hitRect.left,
                hitRect.top,
                hitRect.right,
                hitRect.bottom,
                visualRect.centerX(),
                visualRect.centerY(),
                slot.primaryBottomControl,
                slot.compactSpecialColumn,
                slot.edgeRailDirection,
                slot.bottomSpaceDirection);
    }

    private static Rect ergonomicHitRect(
            Rect rect,
            float minLeft,
            float maxRight,
            boolean functionRail,
            boolean backspace,
            float density,
            boolean enabled) {
        if (!enabled) {
            return rect;
        }
        float expandX = dp(functionRail ? 3 : 4, density);
        float expandY = dp(functionRail ? 2 : 3, density);
        float left = rect.left - expandX;
        float right = rect.right + expandX;
        float top = rect.top - expandY;
        float bottom = rect.bottom + expandY;
        if (functionRail) {
            float bezelShrink = dp(4, density);
            left = rect.left - (backspace ? dp(12, density) : dp(7, density));
            right = rect.right - bezelShrink;
            bottom = rect.bottom + (backspace ? dp(7, density) : expandY);
        }
        return clampRect(left, top, right, bottom, minLeft, rect.top - dp(6, density), maxRight, rect.bottom + dp(8, density));
    }

    private static Rect railHitRect(
            Rect rect,
            float minLeft,
            float maxRight,
            boolean leftRail,
            boolean backspace,
            float density,
            boolean enabled) {
        if (!enabled) {
            return rect;
        }
        float expandY = dp(2, density);
        float bezelShrink = dp(4, density);
        float inwardExpand = backspace ? dp(12, density) : dp(7, density);
        float left = leftRail ? rect.left + bezelShrink : rect.left - inwardExpand;
        float right = leftRail ? rect.right + inwardExpand : rect.right - bezelShrink;
        float top = rect.top - expandY;
        float bottom = rect.bottom + (backspace ? dp(7, density) : expandY);
        return clampRect(left, top, right, bottom, minLeft, rect.top - dp(6, density), maxRight, rect.bottom + dp(8, density));
    }

    private static Rect clampRect(
            float left,
            float top,
            float right,
            float bottom,
            float minLeft,
            float minTop,
            float maxRight,
            float maxBottom) {
        float width = Math.max(1f, right - left);
        float height = Math.max(1f, bottom - top);
        float clampedLeft = Math.max(minLeft, Math.min(left, maxRight - width));
        float clampedTop = Math.max(minTop, Math.min(top, maxBottom - height));
        return new Rect(clampedLeft, clampedTop, clampedLeft + width, clampedTop + height);
    }

    private static boolean isBackspaceKey(GestureKey key) {
        return key != null && KeyboardCommands.CMD_DELETE.equals(key.tap);
    }

    private static boolean hasAdditionalNumberRow(KeyboardSettings settings, List<KeyboardRow> rows) {
        return settings.showNumberRow
                && rows.size() > 2
                && !rows.get(0).keys.isEmpty()
                && rows.get(0).keys.size() == 10
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

    private static int spaceIndex(KeyboardRow row) {
        for (int i = 0; i < row.keys.size(); i++) {
            if (KeyboardCommands.CMD_SPACE.equals(row.keys.get(i).tap)) {
                return i;
            }
        }
        return -1;
    }

    static final class Slot {
        final GestureKey key;
        final float left;
        final float top;
        final float right;
        final float bottom;
        final float hitLeft;
        final float hitTop;
        final float hitRight;
        final float hitBottom;
        final float gestureOriginX;
        final float gestureOriginY;
        final boolean primaryBottomControl;
        final boolean compactSpecialColumn;
        final int edgeRailDirection;
        final int bottomSpaceDirection;

        Slot(
                GestureKey key,
                float left,
                float top,
                float right,
                float bottom,
                boolean primaryBottomControl,
                boolean compactSpecialColumn,
                int edgeRailDirection,
                int bottomSpaceDirection) {
            this(
                    key,
                    left,
                    top,
                    right,
                    bottom,
                    left,
                    top,
                    right,
                    bottom,
                    (left + right) / 2f,
                    (top + bottom) / 2f,
                    primaryBottomControl,
                    compactSpecialColumn,
                    edgeRailDirection,
                    bottomSpaceDirection);
        }

        Slot(
                GestureKey key,
                float left,
                float top,
                float right,
                float bottom,
                float hitLeft,
                float hitTop,
                float hitRight,
                float hitBottom,
                float gestureOriginX,
                float gestureOriginY,
                boolean primaryBottomControl,
                boolean compactSpecialColumn,
                int edgeRailDirection,
                int bottomSpaceDirection) {
            this.key = key;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.hitLeft = hitLeft;
            this.hitTop = hitTop;
            this.hitRight = hitRight;
            this.hitBottom = hitBottom;
            this.gestureOriginX = gestureOriginX;
            this.gestureOriginY = gestureOriginY;
            this.primaryBottomControl = primaryBottomControl;
            this.compactSpecialColumn = compactSpecialColumn;
            this.edgeRailDirection = edgeRailDirection < 0 ? -1 : (edgeRailDirection > 0 ? 1 : 0);
            this.bottomSpaceDirection = bottomSpaceDirection;
        }
    }

    private static final class Rect {
        final float left;
        final float top;
        final float right;
        final float bottom;

        Rect(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        float centerX() {
            return (left + right) / 2f;
        }

        float centerY() {
            return (top + bottom) / 2f;
        }
    }
}
