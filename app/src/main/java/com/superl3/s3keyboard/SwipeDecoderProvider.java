package com.superl3.s3keyboard;

import android.content.Context;

final class SwipeDecoderProvider {
    private SwipeDecoderProvider() {
    }

    static SwipeDecoder create(Context context) {
        return new CompositeSwipeDecoder(
                new FutoSwipeDecoder(context),
                HeuristicEnglishSwipeDecoder.DEFAULT);
    }
}
