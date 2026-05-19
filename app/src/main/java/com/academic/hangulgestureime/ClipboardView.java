package com.academic.hangulgestureime;

import android.content.Context;
import android.graphics.Color;
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
        setBackgroundColor(Color.parseColor("#EAEAEA"));
        setPadding(20, 20, 20, 20);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(context);
        title.setText("Clipboard");
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));

        Button closeBtn = new Button(context);
        closeBtn.setText("Close");
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
            emptyText.setText("Clipboard history is empty.");
            emptyText.setPadding(20, 40, 20, 40);
            emptyText.setGravity(Gravity.CENTER);
            listContent.addView(emptyText);
            return;
        }

        for (String entry : entries) {
            TextView item = new TextView(getContext());
            item.setText(entry);
            item.setTextSize(16);
            item.setTextColor(Color.BLACK);
            item.setPadding(20, 30, 20, 30);
            item.setBackgroundColor(Color.WHITE);
            LayoutParams itemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            itemParams.setMargins(0, 0, 0, 10);
            item.setLayoutParams(itemParams);

            item.setOnClickListener(v -> {
                onTextSelected.onTextSelected(entry);
                onClose.run();
            });

            listContent.addView(item);
        }
    }
}
