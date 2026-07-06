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
    private final Context context;

    private SettingsSectionCard(
            Context context,
            String title,
            boolean expandedByDefault) {
        this.context = context;
        this.title = title;
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        container = SettingsRowBuilder.vertical(context);
        container.setPadding(
                SettingsRowBuilder.dp(context, 16),
                SettingsRowBuilder.dp(context, 12),
                SettingsRowBuilder.dp(context, 16),
                SettingsRowBuilder.dp(context, 14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surface);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 14));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), ui.border);
        container.setBackground(background);

        header = SettingsRowBuilder.label(context, "");
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        container.addView(header, SettingsRowBuilder.matchWrap());

        content = SettingsRowBuilder.vertical(context);
        content.setVisibility(expandedByDefault ? View.VISIBLE : View.GONE);
        container.addView(
                content,
                SettingsRowBuilder.matchWrapWithTop(context, expandedByDefault ? 10 : 0));

        setExpanded(expandedByDefault);
        header.setOnClickListener(v -> setExpanded(content.getVisibility() != View.VISIBLE));
    }

    static SettingsSectionCard create(Context context, String title, boolean expandedByDefault) {
        return new SettingsSectionCard(context, title, expandedByDefault);
    }

    private void setExpanded(boolean expanded) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        header.setText(context.getString(
                expanded
                        ? R.string.expandable_section_title_expanded
                        : R.string.expandable_section_title_collapsed,
                title));
    }

}
