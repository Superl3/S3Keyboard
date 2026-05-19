package com.academic.hangulgestureime;

import android.view.HapticFeedbackConstants;
import android.view.View;

final class KeyboardFeedback {
    private final View view;
    private boolean enabled;

    KeyboardFeedback(View view) {
        this.view = view;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void tap() {
        perform(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    void slideLock() {
        perform(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    void longPress() {
        perform(HapticFeedbackConstants.LONG_PRESS);
    }

    private void perform(int feedbackConstant) {
        if (enabled) {
            view.performHapticFeedback(feedbackConstant);
        }
    }
}
