package com.academic.hangulgestureime;

final class GestureKey {
    final String label;
    final String tap;
    final String upSlide;
    final String downSlide;
    final String leftSlide;
    final String rightSlide;
    final String longPress;
    final int widthUnits;
    final int icon;

    GestureKey(
            String label,
            String tap,
            String upSlide,
            String downSlide,
            String leftSlide,
            String rightSlide,
            String longPress) {
        this(label, tap, upSlide, downSlide, leftSlide, rightSlide, longPress, 1);
    }

    GestureKey(
            String label,
            String tap,
            String upSlide,
            String downSlide,
            String leftSlide,
            String rightSlide,
            String longPress,
            int widthUnits) {
        this(label, tap, upSlide, downSlide, leftSlide, rightSlide, longPress, widthUnits, KeyIcon.NONE);
    }

    GestureKey(
            String label,
            String tap,
            String upSlide,
            String downSlide,
            String leftSlide,
            String rightSlide,
            String longPress,
            int widthUnits,
            int icon) {
        this.label = label;
        this.tap = tap;
        this.upSlide = upSlide;
        this.downSlide = downSlide;
        this.leftSlide = leftSlide;
        this.rightSlide = rightSlide;
        this.longPress = longPress;
        this.widthUnits = Math.max(1, widthUnits);
        this.icon = icon;
    }

    static GestureKey command(String label, String command) {
        return command(label, command, 1);
    }

    static GestureKey command(String label, String command, int widthUnits) {
        return command(label, command, null, widthUnits, KeyIcon.forCommand(command, label));
    }

    static GestureKey command(
            String label,
            String command,
            String longPress,
            int widthUnits,
            int icon) {
        return new GestureKey(label, command, null, null, null, null, longPress, widthUnits, icon);
    }

    String valueFor(GestureAction action) {
        switch (action) {
            case UP:
                return fallback(upSlide);
            case DOWN:
                return fallback(downSlide);
            case LEFT:
                return fallback(leftSlide);
            case RIGHT:
                return fallback(rightSlide);
            case LONG_PRESS:
                return longPress;
            case TAP:
            default:
                return tap;
        }
    }

    private String fallback(String directedValue) {
        return directedValue == null ? tap : directedValue;
    }
}
