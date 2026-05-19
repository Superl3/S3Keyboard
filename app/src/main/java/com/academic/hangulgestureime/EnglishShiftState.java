package com.academic.hangulgestureime;

import java.util.Locale;

final class EnglishShiftState {
    private enum Mode {
        OFF,
        ONCE,
        LOCKED
    }

    private Mode mode = Mode.OFF;

    void reset() {
        mode = Mode.OFF;
    }

    void onShiftOnceCommand() {
        switch (mode) {
            case OFF:
                mode = Mode.ONCE;
                break;
            case ONCE:
            case LOCKED:
                mode = Mode.OFF;
                break;
            default:
                mode = Mode.OFF;
                break;
        }
    }

    void onShiftLockCommand() {
        mode = mode == Mode.LOCKED ? Mode.OFF : Mode.LOCKED;
    }

    boolean isActive() {
        return mode != Mode.OFF;
    }

    boolean isLocked() {
        return mode == Mode.LOCKED;
    }

    String applyToInput(String text) {
        if (!isSingleAsciiLetter(text)) {
            return text;
        }

        String output = isActive() ? text.toUpperCase(Locale.US) : text;
        if (mode == Mode.ONCE) {
            mode = Mode.OFF;
        }
        return output;
    }

    private boolean isSingleAsciiLetter(String text) {
        if (text == null || text.length() != 1) {
            return false;
        }
        char c = text.charAt(0);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
}
