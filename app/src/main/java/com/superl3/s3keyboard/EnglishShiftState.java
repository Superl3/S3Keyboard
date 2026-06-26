package com.superl3.s3keyboard;

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

    void consumeOnce() {
        if (mode == Mode.ONCE) {
            mode = Mode.OFF;
        }
    }

    String applyToInput(String text) {
        if (!isAsciiLetters(text)) {
            return text;
        }

        String output = text;
        if (mode == Mode.LOCKED) {
            output = text.toUpperCase(Locale.US);
        } else if (mode == Mode.ONCE) {
            output = text.length() == 1
                    ? text.toUpperCase(Locale.US)
                    : text.substring(0, 1).toUpperCase(Locale.US) + text.substring(1);
        }
        if (mode == Mode.ONCE) {
            mode = Mode.OFF;
        }
        return output;
    }

    private boolean isAsciiLetters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return false;
            }
        }
        return true;
    }
}
