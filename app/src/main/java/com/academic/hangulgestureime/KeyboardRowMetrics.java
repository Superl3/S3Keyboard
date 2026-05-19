package com.academic.hangulgestureime;

final class KeyboardRowMetrics {
    private KeyboardRowMetrics() {
    }

    static int contentUnits(KeyboardRow row) {
        int units = 0;
        for (GestureKey key : row.keys) {
            units += key.widthUnits;
        }
        return units;
    }

    static float contentWidth(KeyboardRow row, float unitWidth, float gap) {
        int units = contentUnits(row);
        if (units <= 0) {
            return 0f;
        }
        return unitWidth * units + gap * Math.max(0, row.keys.size() - 1);
    }

    static float maxContentWidth(KeyboardRow row, float unitWidth, float gap) {
        return unitWidth * row.baseUnits + gap * Math.max(0, row.keys.size() - 1);
    }

    static float keyWidth(GestureKey key, float unitWidth, float gap) {
        return unitWidth * key.widthUnits;
    }

    static float keyLeft(KeyboardRow row, int keyIndex, float rowLeft, float unitWidth, float gap) {
        float left = rowLeft;
        for (int i = 0; i < keyIndex; i++) {
            left += keyWidth(row.keys.get(i), unitWidth, gap) + gap;
        }
        return left;
    }

    static float keyRight(KeyboardRow row, int keyIndex, float rowLeft, float unitWidth, float gap) {
        GestureKey key = row.keys.get(keyIndex);
        return keyLeft(row, keyIndex, rowLeft, unitWidth, gap) + keyWidth(key, unitWidth, gap);
    }

    static float widthForUnits(int units, float unitWidth, float gap) {
        if (units <= 0) {
            return 0f;
        }
        return unitWidth * units;
    }
}
