package com.superl3.s3keyboard;

final class ClipboardPanelAccess {
    enum Result {
        SHOW,
        HIDE,
        DISABLED,
        SECURE_FIELD,
        UNAVAILABLE
    }

    private ClipboardPanelAccess() {
    }

    static Result resolve(
            boolean viewAvailable,
            boolean historyEnabled,
            boolean secureField,
            boolean currentlyVisible) {
        if (secureField) {
            return Result.SECURE_FIELD;
        }
        if (!historyEnabled) {
            return Result.DISABLED;
        }
        if (!viewAvailable) {
            return Result.UNAVAILABLE;
        }
        return currentlyVisible ? Result.HIDE : Result.SHOW;
    }
}
