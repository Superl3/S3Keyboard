package com.superl3.s3keyboard;

final class KeyboardShapeMetrics {
    // Existing dp theme values are treated as authored values on a 50dp reference key.
    // Rendering then preserves the same visual ratio across differently sized layouts.
    static final float REFERENCE_KEY_SIZE_DP = 50f;

    private KeyboardShapeMetrics() {
    }

    static float cornerRadiusPx(int referenceRoundnessDp, float widthPx, float heightPx) {
        float shortSide = Math.max(0f, Math.min(widthPx, heightPx));
        if (referenceRoundnessDp <= 0 || shortSide <= 0f) {
            return 0f;
        }
        float ratio = referenceRoundnessDp / REFERENCE_KEY_SIZE_DP;
        return Math.min(shortSide * 0.5f, shortSide * ratio);
    }

    static float visualGapPx(int referenceGapDp, float widthPx, float heightPx) {
        if (referenceGapDp <= 0 || widthPx <= 0f || heightPx <= 0f) {
            return 0f;
        }
        float areaScale = (float) Math.sqrt(widthPx * heightPx);
        float requested = areaScale * referenceGapDp / REFERENCE_KEY_SIZE_DP;
        float shortSide = Math.min(widthPx, heightPx);
        return Math.min(requested, shortSide * 0.42f);
    }

    static float rowGapPx(
            int referenceGapDp,
            KeyboardRow row,
            float rowAvailableWidthPx,
            float rowHeightPx,
            boolean dingulCharacterRow) {
        if (referenceGapDp <= 0 || row == null || row.keys.size() <= 1) {
            return 0f;
        }
        float unitWidthNoGap = rowAvailableWidthPx / Math.max(1f, row.baseUnits);
        int keyCount = dingulCharacterRow ? Math.min(3, row.keys.size()) : row.keys.size();
        float widthUnits = 0f;
        for (int i = 0; i < keyCount; i++) {
            widthUnits += row.keys.get(i).widthUnits;
        }
        float representativeWidth = unitWidthNoGap * widthUnits / Math.max(1, keyCount);
        return visualGapPx(referenceGapDp, representativeWidth, rowHeightPx);
    }

    static float ratioForReferenceDp(int referenceDp) {
        return Math.max(0, referenceDp) / REFERENCE_KEY_SIZE_DP;
    }
}
