package com.superl3.s3keyboard;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

final class SettingsRowBuilder {
    private SettingsRowBuilder() {
    }

    static TextView label(Context context, String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextSize(14);
        label.setGravity(Gravity.START);
        SettingsViewStyler.label(label, context, false);
        return label;
    }

    static TextView secondaryLabel(Context context, String text) {
        TextView label = label(context, text);
        SettingsViewStyler.label(label, context, true);
        return label;
    }

    static LinearLayout horizontal(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }
}
