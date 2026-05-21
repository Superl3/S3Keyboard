package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

final class SettingsSectionCard {
    final LinearLayout container;
    final LinearLayout content;

    private final TextView header;
    private final String title;

    private SettingsSectionCard(
            Context context,
            String title,
            boolean expandedByDefault) {
        this.title = title;
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surface);
        background.setCornerRadius(dp(context, 14));
        background.setStroke(Math.max(1, dp(context, 1)), ui.border);
        container.setBackground(background);

        header = SettingsRowBuilder.label(context, "");
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        container.addView(header, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setVisibility(expandedByDefault ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.topMargin = dp(context, expandedByDefault ? 10 : 0);
        container.addView(content, contentParams);

        setExpanded(expandedByDefault);
        header.setOnClickListener(v -> setExpanded(content.getVisibility() != View.VISIBLE));
    }

    static SettingsSectionCard create(Context context, String title, boolean expandedByDefault) {
        return new SettingsSectionCard(context, title, expandedByDefault);
    }

    private void setExpanded(boolean expanded) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        header.setText((expanded ? "▼ " : "▶ ") + title);
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
