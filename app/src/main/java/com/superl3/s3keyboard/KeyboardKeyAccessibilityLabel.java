package com.superl3.s3keyboard;

import android.content.Context;

final class KeyboardKeyAccessibilityLabel {
    private KeyboardKeyAccessibilityLabel() {
    }

    static String describe(GestureKey key) {
        return describe(null, key);
    }

    static String describe(Context context, GestureKey key) {
        if (key == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String label = displayValue(context, key.label);
        String tap = displayValue(context, key.tap);
        if (!isEmpty(label)) {
            builder.append(label);
        } else if (!isEmpty(tap)) {
            builder.append(tap);
        } else {
            builder.append(stringFor(context, R.string.keyboard_accessibility_generic_key, "key"));
        }
        if (!isEmpty(tap) && !same(label, tap)) {
            builder.append(", ")
                    .append(stringFor(context, R.string.keyboard_accessibility_action_tap, "tap"))
                    .append(" ")
                    .append(tap);
        }
        appendGesture(
                builder,
                stringFor(context, R.string.keyboard_accessibility_action_up, "up"),
                context,
                key.upSlide,
                key.tap);
        appendGesture(
                builder,
                stringFor(context, R.string.keyboard_accessibility_action_down, "down"),
                context,
                key.downSlide,
                key.tap);
        appendGesture(
                builder,
                stringFor(context, R.string.keyboard_accessibility_action_left, "left"),
                context,
                key.leftSlide,
                key.tap);
        appendGesture(
                builder,
                stringFor(context, R.string.keyboard_accessibility_action_right, "right"),
                context,
                key.rightSlide,
                key.tap);
        appendGesture(
                builder,
                stringFor(context, R.string.keyboard_accessibility_action_long_press, "long press"),
                context,
                key.longPress,
                null);
        return builder.toString();
    }

    static String displayValue(String value) {
        return displayValue(null, value);
    }

    static String displayValue(Context context, String value) {
        if (value == null || value.isEmpty() || KeyboardCommands.CMD_NOOP.equals(value)) {
            return null;
        }
        String commandLabel = KeyboardCommandLabels.labelFor(context, value);
        if (commandLabel == null || commandLabel.isEmpty()) {
            return null;
        }
        return commandLabel;
    }

    static String actionDescription(Context context, GestureAction action, String value) {
        String display = displayValue(context, value);
        if (action == null || isEmpty(display)) {
            return null;
        }
        String actionLabel = actionLabel(context, action);
        if (isEmpty(actionLabel)) {
            return null;
        }
        return context == null
                ? actionLabel + " " + display
                : context.getString(
                        R.string.keyboard_accessibility_custom_action_format,
                        actionLabel,
                        display);
    }

    private static String actionLabel(Context context, GestureAction action) {
        switch (action) {
            case TAP:
                return stringFor(context, R.string.keyboard_accessibility_action_tap, "tap");
            case UP:
                return stringFor(context, R.string.keyboard_accessibility_action_up, "up");
            case DOWN:
                return stringFor(context, R.string.keyboard_accessibility_action_down, "down");
            case LEFT:
                return stringFor(context, R.string.keyboard_accessibility_action_left, "left");
            case RIGHT:
                return stringFor(context, R.string.keyboard_accessibility_action_right, "right");
            case LONG_PRESS:
                return stringFor(
                        context,
                        R.string.keyboard_accessibility_action_long_press,
                        "long press");
            default:
                return null;
        }
    }

    private static void appendGesture(
            StringBuilder builder,
            String actionLabel,
            Context context,
            String value,
            String fallback) {
        if (value == null || value.isEmpty() || KeyboardCommands.CMD_NOOP.equals(value) || same(value, fallback)) {
            return;
        }
        String display = displayValue(context, value);
        if (isEmpty(display)) {
            return;
        }
        builder.append(", ").append(actionLabel).append(" ").append(display);
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private static boolean same(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String stringFor(Context context, int resId, String fallback) {
        return context == null ? fallback : context.getString(resId);
    }
}
