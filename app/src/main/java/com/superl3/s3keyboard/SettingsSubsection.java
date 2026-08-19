package com.superl3.s3keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

final class SettingsSubsection {
    final LinearLayout container;
    final LinearLayout content;

    private final Context context;
    private final String titleText;
    private final ImageView indicator;
    private boolean expanded;
    private boolean expandedBeforeSearch;
    private boolean searchExpansionActive;

    private SettingsSubsection(Context context, int titleResId, boolean expandedByDefault) {
        this.context = context;
        this.titleText = context.getString(titleResId);
        this.expanded = expandedByDefault;

        container = SettingsRowBuilder.vertical(context);
        container.setTag(R.id.settings_subsection_tag, this);
        LinearLayout header = SettingsRowBuilder.horizontal(context);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setMinimumHeight(SettingsRowBuilder.dp(context, 48));
        header.setFocusable(true);
        header.setPadding(
                SettingsRowBuilder.dp(context, 4),
                SettingsRowBuilder.dp(context, 2),
                0,
                SettingsRowBuilder.dp(context, 2));
        applySelectableBackground(header);

        TextView title = SettingsRowBuilder.label(context, titleText);
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        header.addView(title, SettingsRowBuilder.weightedWrap(context, 1f, 0, 4));

        indicator = new ImageView(context);
        indicator.setImageResource(R.drawable.ic_settings_chevron);
        indicator.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        indicator.setImageTintList(ColorStateList.valueOf(SettingsUiPalette.from(context).textSecondary));
        indicator.setPadding(
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10),
                SettingsRowBuilder.dp(context, 10));
        header.addView(indicator, SettingsRowBuilder.fixedSize(context, 44, 44));
        header.setOnClickListener(view -> toggle());
        container.addView(header, SettingsRowBuilder.matchWrap());

        View divider = new View(context);
        divider.setBackgroundColor(SettingsUiPalette.from(context).border);
        container.addView(divider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Math.max(1, SettingsRowBuilder.dp(context, 1))));

        content = SettingsRowBuilder.vertical(context);
        content.setPadding(
                SettingsRowBuilder.dp(context, 4),
                SettingsRowBuilder.dp(context, 4),
                SettingsRowBuilder.dp(context, 4),
                SettingsRowBuilder.dp(context, 8));
        container.addView(content, SettingsRowBuilder.matchWrap());
        applyExpandedState(false);
    }

    static SettingsSubsection add(
            Context context,
            LinearLayout root,
            int titleResId,
            boolean expandedByDefault) {
        SettingsSubsection subsection = new SettingsSubsection(
                context,
                titleResId,
                expandedByDefault);
        root.addView(subsection.container, SettingsRowBuilder.matchWrapWithTop(context, 8));
        return subsection;
    }

    static void setSearchExpansion(View root, boolean active) {
        if (root == null) {
            return;
        }
        Object tag = root.getTag(R.id.settings_subsection_tag);
        if (tag instanceof SettingsSubsection) {
            ((SettingsSubsection) tag).setSearchExpansion(active);
        }
        if (!(root instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) root;
        for (int index = 0; index < group.getChildCount(); index++) {
            setSearchExpansion(group.getChildAt(index), active);
        }
    }

    private void toggle() {
        expanded = !expanded;
        applyExpandedState(true);
    }

    private void setSearchExpansion(boolean active) {
        if (active == searchExpansionActive) {
            return;
        }
        if (active) {
            expandedBeforeSearch = expanded;
            searchExpansionActive = true;
            expanded = true;
        } else {
            searchExpansionActive = false;
            expanded = expandedBeforeSearch;
        }
        applyExpandedState(false);
    }

    private void applyExpandedState(boolean animate) {
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        float rotation = expanded ? 90f : 0f;
        if (animate) {
            indicator.animate().rotation(rotation).setDuration(140L).start();
        } else {
            indicator.setRotation(rotation);
        }
        container.getChildAt(0).setContentDescription(context.getString(
                expanded
                        ? R.string.settings_subsection_expanded_format
                        : R.string.settings_subsection_collapsed_format,
                titleText));
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
