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
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView status = SettingsRowBuilder.label(context, context.getString(R.string.practice_mode_idle));
        root.addView(status, matchWrap());

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (String target : TARGETS) {
            Button button = new Button(context);
            button.setText(target);
            SettingsViewStyler.button(button, context, false);
            button.setOnClickListener(v -> status.setText(
                    context.getString(R.string.practice_mode_selected_format, target)));
            row.addView(button, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f));
        }
        root.addView(row, matchWrapWithTop(context, 6));

        TextView hint = SettingsRowBuilder.label(context, DingulInputDiagnostics.load(context).summaryText(context));
        hint.setLineSpacing(dp(context, 2), 1.0f);
        root.addView(hint, matchWrapWithTop(context, 6));
        return root;
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private static LinearLayout.LayoutParams matchWrapWithTop(Context context, int topDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(context, topDp);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
