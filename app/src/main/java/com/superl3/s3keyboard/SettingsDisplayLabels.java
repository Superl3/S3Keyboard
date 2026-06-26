package com.superl3.s3keyboard;

import android.content.Context;

final class SettingsDisplayLabels {
    private SettingsDisplayLabels() {
    }

    static String label(Context context, HandednessMode value) {
        return label(context, value == null ? HandednessMode.BALANCED.labelResId : value.labelResId);
    }

    static String label(Context context, KeyboardMode value) {
        return label(context, value == null ? KeyboardMode.HANGUL.labelResId : value.labelResId);
    }

    static String label(Context context, KeyboardLayoutProfile value) {
        return label(context, value == null ? KeyboardLayoutProfile.QWERTY.labelResId : value.labelResId);
    }

    static String label(Context context, KeyboardErgonomicsPreset value) {
        return label(context, value == null
                ? KeyboardErgonomicsPreset.LEGACY.labelResId
                : value.labelResId);
    }

    static String label(Context context, VisualConsistencyLevel value) {
        return label(context, value == null ? VisualConsistencyLevel.NONE.labelResId : value.labelResId);
    }

    static String label(Context context, MotionEffectLevel value) {
        return label(context, value == null ? MotionEffectLevel.NORMAL.labelResId : value.labelResId);
    }

    static String label(Context context, InputAssistanceMode value) {
        return label(context, value == null ? InputAssistanceMode.CUSTOM.labelResId : value.labelResId);
    }

    static String label(Context context, AdditionalNumberRowColorMode value) {
        return label(context, value == null
                ? AdditionalNumberRowColorMode.FULL_MOD.labelResId
                : value.labelResId);
    }

    static String label(Context context, RemoteKeyPreset value) {
        return label(context, value == null ? RemoteKeyPreset.PC_KEYBOARD.labelResId : value.labelResId);
    }

    static String label(Context context, RemoteImeShortcut value) {
        return label(context, value == null ? RemoteImeShortcut.ALT_SHIFT.labelResId : value.labelResId);
    }

    static String label(Context context, AccentPlacementMode value) {
        return label(context, value == null
                ? AccentPlacementMode.DEFAULT.labelResId
                : value.labelResId);
    }

    static String label(Context context, ColorOption value) {
        return label(context, value == null
                ? R.string.color_option_default_key
                : value.labelResId);
    }

    static String label(Context context, FontOption value) {
        return label(context, value == null ? R.string.font_option_default : value.labelResId);
    }

    static String label(Context context, AccentPlacementPolicy.SpaceRole value) {
        return label(context, value == null
                ? AccentPlacementPolicy.SpaceRole.DEFAULT.labelResId
                : value.labelResId);
    }

    static String label(Context context, AccentPlacementPolicy.QuestionRole value) {
        return label(context, value == null
                ? AccentPlacementPolicy.QuestionRole.DEFAULT.labelResId
                : value.labelResId);
    }

    static String[] labels(Context context, HandednessMode[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, KeyboardLayoutProfile[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, KeyboardErgonomicsPreset[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, VisualConsistencyLevel[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, MotionEffectLevel[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, InputAssistanceMode[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, AdditionalNumberRowColorMode[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, RemoteKeyPreset[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, RemoteImeShortcut[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, AccentPlacementMode[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, ColorOption[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, FontOption[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, AccentPlacementPolicy.SpaceRole[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String[] labels(Context context, AccentPlacementPolicy.QuestionRole[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    private static String label(Context context, int labelResId) {
        return context == null ? "" : context.getString(labelResId);
    }
}
