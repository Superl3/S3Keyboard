package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
import java.util.function.Consumer;

public class ClipboardView extends LinearLayout {
    private final ClipboardStore store;
    private final Runnable onClose;
    private final Consumer<String> onTextSelected;
    private final LinearLayout listContent;

    public ClipboardView(
            Context context,
            ClipboardStore store,
            Runnable onClose,
            Consumer<String> onTextSelected) {
        super(context);
        this.store = store;
        this.onClose = onClose;
        this.onTextSelected = onTextSelected;

        setOrientation(VERTICAL);
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        setBackgroundColor(ui.background);
        setPadding(
                SettingsRowBuilder.dp(context, 20),
                SettingsRowBuilder.dp(context, 20),
                SettingsRowBuilder.dp(context, 20),
                SettingsRowBuilder.dp(context, 20));

        addView(createHeader());

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        listContent = SettingsRowBuilder.vertical(context);
        scroll.addView(listContent);
        addView(scroll);
        refresh();
    }

    void refresh() {
        listContent.removeAllViews();
        List<String> entries = store.getEntries();
        if (entries.isEmpty()) {
            listContent.addView(createEmptyText());
            return;
        }

        for (String entry : entries) {
            listContent.addView(createEntryItem(entry));
        }
    }

    private LinearLayout createHeader() {
        Context context = getContext();
        LinearLayout header = SettingsRowBuilder.horizontal(context);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView title = SettingsRowBuilder.label(context, R.string.clipboard_panel_title);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

        Button closeBtn = SettingsRowBuilder.button(context, R.string.action_close, v -> onClose.run());
        header.addView(closeBtn);
        return header;
    }

    private TextView createEmptyText() {
        TextView emptyText = SettingsRowBuilder.secondaryLabel(
                getContext(),
                R.string.clipboard_history_empty);
        emptyText.setPadding(
                SettingsRowBuilder.dp(getContext(), 20),
                SettingsRowBuilder.dp(getContext(), 40),
                SettingsRowBuilder.dp(getContext(), 20),
                SettingsRowBuilder.dp(getContext(), 40));
        emptyText.setGravity(Gravity.CENTER);
        return emptyText;
    }

    private TextView createEntryItem(String entry) {
        TextView item = SettingsRowBuilder.label(getContext(), entry);
        item.setTextSize(16);
        item.setPadding(
                SettingsRowBuilder.dp(getContext(), 20),
                SettingsRowBuilder.dp(getContext(), 30),
                SettingsRowBuilder.dp(getContext(), 20),
                SettingsRowBuilder.dp(getContext(), 30));
        item.setBackground(itemBackground());
        LayoutParams itemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        itemParams.setMargins(0, 0, 0, SettingsRowBuilder.dp(getContext(), 10));
        item.setLayoutParams(itemParams);
        item.setOnClickListener(v -> {
            onTextSelected.accept(entry);
            onClose.run();
        });
        return item;
    }

    private GradientDrawable itemBackground() {
        SettingsUiPalette ui = SettingsUiPalette.from(getContext());
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(SettingsRowBuilder.dp(getContext(), 10));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(getContext(), 1)), ui.border);
        return background;
    }
}
