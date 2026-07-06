package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class QuickPanelUi {
    private QuickPanelUi() {
    }

    static TextView sectionLabel(Context context, int labelResId) {
        TextView label = SettingsRowBuilder.secondaryLabel(context, labelResId);
        label.setTextSize(13);
        return label;
    }

    static TextView titleLabel(Context context, int labelResId) {
        TextView title = SettingsRowBuilder.label(context, labelResId);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        return title;
    }

    static Button quickButton(
            Context context,
            String text,
            boolean selected,
            View.OnClickListener listener) {
        return SettingsRowBuilder.button(context, text, selected, listener);
    }

    static Button compactButton(Context context, String text, View.OnClickListener listener) {
        Button button = SettingsRowBuilder.button(context, text, false, listener);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(context, 38));
        button.setPadding(dp(context, 8), 0, dp(context, 8), 0);
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        button.setTextColor(ui.controlText);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.controlFill);
        background.setCornerRadius(dp(context, 8));
        background.setStroke(Math.max(1, dp(context, 1)), ui.border);
        button.setBackground(background);
        return button;
    }

    static Button addCompactButton(
            Context context,
            LinearLayout row,
            String text,
            View.OnClickListener listener,
            int rightMarginDp) {
        Button button = compactButton(context, text, listener);
        row.addView(button, weightedParams(context, 0, rightMarginDp));
        return button;
    }

    static LinearLayout row(Context context) {
        return SettingsRowBuilder.horizontal(context);
    }

    static <T extends View> T addWithTop(
            Context context,
            LinearLayout root,
            T view,
            int topMarginDp) {
        return SettingsRowBuilder.addViewWithTop(context, root, view, topMarginDp);
    }

    static LinearLayout.LayoutParams weightedParams(Context context, int leftMarginDp, int rightMarginDp) {
        return SettingsRowBuilder.weightedWrap(context, leftMarginDp, rightMarginDp);
    }

    static int dp(Context context, int value) {
        return SettingsRowBuilder.dp(context, value);
    }
}
