package com.superl3.s3keyboard;

import android.content.Context;

final class SettingsDisplayLabels {
    private SettingsDisplayLabels() {
    }

    static <T extends SettingsLabelOption> String[] labels(Context context, T[] values) {
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = label(context, values[i]);
        }
        return labels;
    }

    static String label(Context context, SettingsLabelOption value) {
        return value == null ? "" : label(context, value.labelResId());
    }

    private static String label(Context context, int labelResId) {
        return context == null ? "" : context.getString(labelResId);
    }
}
