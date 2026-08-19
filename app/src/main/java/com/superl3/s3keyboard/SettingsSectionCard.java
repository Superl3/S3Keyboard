package com.superl3.s3keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class SettingsSectionCard {
    final LinearLayout container;
    final LinearLayout content;

    private final LinearLayout headerRow;
    private final TextView header;
    private final ImageView indicator;
    private final String title;
    private final Context context;
    private boolean expanded;
    private boolean toggleEnabled = true;

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
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), ui.border);
        container.setBackground(background);

        headerRow = SettingsRowBuilder.horizontal(context);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setMinimumHeight(SettingsRowBuilder.dp(context, 48));
        headerRow.setFocusable(true);
        applySelectableBackground(headerRow);

        header = SettingsRowBuilder.label(context, title);
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        headerRow.addView(header, SettingsRowBuilder.weightedWrap(context, 1f, 0, 4));

        indicator = new ImageView(context);
        indicator.setImageResource(R.drawable.ic_settings_chevron);
        indicator.setImageTintList(ColorStateList.valueOf(ui.textSecondary));
        indicator.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        indicator.setPadding(
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10));
        headerRow.addView(indicator, SettingsRowBuilder.fixedSize(context, 44, 44));
        container.addView(headerRow, SettingsRowBuilder.matchWrap());

        content = SettingsRowBuilder.vertical(context);
        container.addView(content, SettingsRowBuilder.matchWrap());

        setExpanded(expandedByDefault);
        headerRow.setOnClickListener(v -> {
            if (toggleEnabled) {
                expanded = !expanded;
                applyExpandedState(true);
            }
        });
    }

    static SettingsSectionCard create(Context context, String title, boolean expandedByDefault) {
        return new SettingsSectionCard(context, title, expandedByDefault);
    }

    void setExpanded(boolean expanded) {
        this.expanded = expanded;
        applyExpandedState(false);
    }

    void setToggleEnabled(boolean enabled) {
        toggleEnabled = enabled;
        headerRow.setClickable(enabled);
        indicator.setVisibility(enabled ? View.VISIBLE : View.GONE);
        updateAccessibilityDescription();
    }

    void setWizardTitle(int step, int total) {
        String wizardTitle = context.getString(
                R.string.settings_wizard_step_title_format,
                step,
                total,
                title);
        header.setText(wizardTitle);
        headerRow.setContentDescription(wizardTitle);
    }

    private void applyExpandedState(boolean animate) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) content.getLayoutParams();
        if (params != null) {
            params.topMargin = expanded ? SettingsRowBuilder.dp(context, 10) : 0;
            content.setLayoutParams(params);
        }
        float rotation = expanded ? 90f : 0f;
        if (animate) {
            indicator.animate().rotation(rotation).setDuration(140L).start();
        } else {
            indicator.setRotation(rotation);
        }
        header.setText(title);
        updateAccessibilityDescription();
    }

    private void updateAccessibilityDescription() {
        headerRow.setContentDescription(toggleEnabled
                ? context.getString(
                        expanded
                                ? R.string.settings_subsection_expanded_format
                                : R.string.settings_subsection_collapsed_format,
                        title)
                : title);
    }

    private void applySelectableBackground(View view) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                value,
                true)) {
            view.setBackgroundResource(value.resourceId);
        }
    }

}
