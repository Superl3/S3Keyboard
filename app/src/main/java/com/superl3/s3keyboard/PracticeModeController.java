package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class PracticeModeController {
    private static final String[] TARGETS = {"ㅣ.", "ㅡㅐ", "ㅢ", "..", "?", ".", "/"};

    private PracticeModeController() {
    }

    static View createPanel(Context context) {
        LinearLayout root = SettingsRowBuilder.vertical(context);

        TextView status = SettingsRowBuilder.labelRow(
                context,
                root,
                R.string.practice_mode_idle,
                0);

        LinearLayout row = SettingsRowBuilder.horizontal(context);
        for (String target : TARGETS) {
            Button button = SettingsRowBuilder.button(
                    context,
                    target,
                    v -> status.setText(context.getString(
                            R.string.practice_mode_selected_format,
                            target)));
            row.addView(button, SettingsRowBuilder.weightedWrap(context, 0, 0));
        }
        root.addView(row, SettingsRowBuilder.matchWrapWithTop(context, 6));

        SettingsRowBuilder.bodyLabelRow(
                context,
                root,
                DingulInputDiagnostics.load(context).summaryText(context),
                6);
        return root;
    }
}
