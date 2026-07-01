package com.superl3.s3keyboard;

import android.content.Context;

final class RecentInputCorrectionController {
    private final TouchBiasStore store;

    RecentInputCorrectionController(Context context) {
        store = new TouchBiasStore(context);
    }

    TouchBiasStore.DingulTouchProfile markGeometryOnlyTypo(
            String keyCodePoints,
            GestureAction action,
            float offsetXDp,
            float offsetYDp) {
        if (keyCodePoints == null || keyCodePoints.isEmpty()) {
            return store.loadDingulTouchProfile();
        }
        return store.recordDingulCorrection(keyCodePoints, action, offsetXDp, offsetYDp);
    }
}
