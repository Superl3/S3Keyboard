package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class ClipboardView extends LinearLayout {
    private final ClipboardStore store;
    private final Runnable onClose;
    private final OnTextSelectedListener onTextSelected;
    private final LinearLayout listContent;

    public interface OnTextSelectedListener {
        void onTextSelected(String text);
    }

    public ClipboardView(
            Context context,
            ClipboardStore store,
            Runnable onClose,
            OnTextSelectedListener onTextSelected) {
        super(context);
        this.store = store;
        this.onClose = onClose;
        this.onTextSelected = onTextSelected;

        setOrientation(VERTICAL);
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        setBackgroundColor(ui.background);
        setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(context);
        title.setText("클립보드");
        title.setTextSize(18);
        title.setTextColor(ui.textPrimary);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

        Button closeBtn = new Button(context);
        closeBtn.setText("닫기");
        SettingsViewStyler.button(closeBtn, context, false);
        closeBtn.setOnClickListener(v -> onClose.run());
        header.addView(closeBtn);

        addView(header);

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        listContent = new LinearLayout(context);
        listContent.setOrientation(VERTICAL);
        scroll.addView(listContent);
        addView(scroll);
        refresh();
    }

    void refresh() {
        listContent.removeAllViews();
        List<String> entries = store.getEntries();
        if (entries.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("클립보드 기록이 비어 있습니다.");
            emptyText.setTextColor(SettingsUiPalette.from(getContext()).textSecondary);
            emptyText.setPadding(dp(20), dp(40), dp(20), dp(40));
            emptyText.setGravity(Gravity.CENTER);
            listContent.addView(emptyText);
            return;
        }

        for (String entry : entries) {
            TextView item = new TextView(getContext());
            item.setText(entry);
            item.setTextSize(16);
            item.setTextColor(SettingsUiPalette.from(getContext()).textPrimary);
            item.setPadding(dp(20), dp(30), dp(20), dp(30));
            item.setBackground(itemBackground());
            LayoutParams itemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            itemParams.setMargins(0, 0, 0, dp(10));
            item.setLayoutParams(itemParams);

            item.setOnClickListener(v -> {
                onTextSelected.onTextSelected(entry);
                onClose.run();
            });

            listContent.addView(item);
        }
    }

    private GradientDrawable itemBackground() {
        SettingsUiPalette ui = SettingsUiPalette.from(getContext());
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(dp(10));
        background.setStroke(Math.max(1, dp(1)), ui.border);
        return background;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
