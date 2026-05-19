package com.superl3.s3keyboard;

final class DoubleSpacePeriodState {
    static final long DOUBLE_SPACE_WINDOW_MS = 1200;

    private long lastEnglishSpaceAtMs = -1;

    SpaceResult onSpace(KeyboardMode mode, boolean enabled, long nowMs) {
        if (enabled
                && mode == KeyboardMode.ENGLISH
                && lastEnglishSpaceAtMs >= 0
                && nowMs - lastEnglishSpaceAtMs <= DOUBLE_SPACE_WINDOW_MS) {
            lastEnglishSpaceAtMs = -1;
            return SpaceResult.REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE;
        }

        lastEnglishSpaceAtMs = mode == KeyboardMode.ENGLISH ? nowMs : -1;
        return SpaceResult.COMMIT_SPACE;
    }

    void reset() {
        lastEnglishSpaceAtMs = -1;
    }

    enum SpaceResult {
        COMMIT_SPACE,
        REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE
    }
}
