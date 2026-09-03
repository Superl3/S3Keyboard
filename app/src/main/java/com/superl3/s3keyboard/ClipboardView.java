package com.superl3.s3keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
import java.util.function.Consumer;

@SuppressLint("ViewConstructor")
public class ClipboardView extends LinearLayout {
    private final ClipboardStore clipboardStore;
    private final TextToolsStore textToolsStore;
    private final Runnable onClose;
    private final Consumer<String> onTextSelected;
    private final LinearLayout listContent;

    ClipboardView(
            Context context,
            ClipboardStore clipboardStore,
            TextToolsStore textToolsStore,
            Runnable onClose,
            Consumer<String> onTextSelected) {
        super(context);
        this.clipboardStore = clipboardStore;
        this.textToolsStore = textToolsStore;
        this.onClose = onClose;
        this.onTextSelected = onTextSelected;

        setOrientation(VERTICAL);
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        setBackgroundColor(ui.background);
        int padding = SettingsRowBuilder.dp(context, 14);
        setPadding(padding, padding, padding, padding);

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
        addSavedItems();
        addReservedPhrases();
        addRecentClipboard();
    }

    private LinearLayout createHeader() {
        LinearLayout header = SettingsRowBuilder.horizontal(getContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = SettingsRowBuilder.label(getContext(), R.string.text_tools_panel_title);
        title.setTextSize(18);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        Button clear = SettingsRowBuilder.button(
                getContext(), R.string.text_tools_clear_history, v -> {
                    clipboardStore.clear();
                    refresh();
                });
        header.addView(clear);
        header.addView(SettingsRowBuilder.button(
                getContext(), R.string.action_close, v -> onClose.run()));
        return header;
    }

    private void addSavedItems() {
        List<TextToolsStore.Item> items = textToolsStore.getItems();
        if (items.isEmpty()) return;
        addSectionLabel(R.string.text_tools_saved_section);
        for (TextToolsStore.Item item : items) {
            LinearLayout row = itemRow(item.name, item.text);
            row.addView(SettingsRowBuilder.button(
                    getContext(),
                    item.pinned ? R.string.text_tools_unpin : R.string.text_tools_pin,
                    v -> {
                        textToolsStore.setPinned(item.id, !item.pinned);
                        refresh();
                    }));
            row.addView(SettingsRowBuilder.button(
                    getContext(), R.string.text_tools_rename, v -> showRenameDialog(item)));
            row.addView(SettingsRowBuilder.button(
                    getContext(), R.string.action_delete, v -> {
                        textToolsStore.delete(item.id);
                        refresh();
                    }));
            listContent.addView(row, itemLayoutParams());
        }
    }

    private void addReservedPhrases() {
        addSectionLabel(R.string.text_tools_reserved_section);
        addReservedPhrase(GestureAction.TAP, R.string.reserved_phrase_tap);
        addReservedPhrase(GestureAction.LEFT, R.string.reserved_phrase_left_slide);
        addReservedPhrase(GestureAction.RIGHT, R.string.reserved_phrase_right_slide);
        addReservedPhrase(GestureAction.UP, R.string.reserved_phrase_up_slide);
    }

    private void addReservedPhrase(GestureAction action, int labelResId) {
        String text = KeyboardPreferences.loadReservedPhrase(getContext(), action);
        String label = getContext().getString(labelResId);
        LinearLayout row = itemRow(label, text);
        row.addView(SettingsRowBuilder.button(
                getContext(), R.string.text_tools_edit, v -> showReservedEditDialog(action, label)));
        if (!text.isEmpty()) {
            row.addView(SettingsRowBuilder.button(
                    getContext(), R.string.action_delete, v -> {
                        KeyboardPreferences.saveReservedPhrase(getContext(), action, "");
                        refresh();
                    }));
        }
        listContent.addView(row, itemLayoutParams());
    }

    private void addRecentClipboard() {
        if (!clipboardStore.isEnabled()) return;
        addSectionLabel(R.string.text_tools_recent_section);
        List<String> entries = clipboardStore.getEntries();
        if (entries.isEmpty()) {
            TextView empty = SettingsRowBuilder.secondaryLabel(
                    getContext(), R.string.clipboard_history_empty);
            listContent.addView(empty, itemLayoutParams());
            return;
        }
        for (String entry : entries) {
            LinearLayout row = itemRow("", entry);
            row.addView(SettingsRowBuilder.button(
                    getContext(), R.string.text_tools_pin, v -> {
                        textToolsStore.saveClipboardItem(entry);
                        refresh();
                    }));
            row.addView(SettingsRowBuilder.button(
                    getContext(), R.string.action_delete, v -> {
                        clipboardStore.remove(entry);
                        refresh();
                    }));
            listContent.addView(row, itemLayoutParams());
        }
    }

    private LinearLayout itemRow(String name, String text) {
        LinearLayout row = SettingsRowBuilder.horizontal(getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(
                SettingsRowBuilder.dp(getContext(), 10),
                SettingsRowBuilder.dp(getContext(), 8),
                SettingsRowBuilder.dp(getContext(), 8),
                SettingsRowBuilder.dp(getContext(), 8));
        row.setBackground(itemBackground());

        LinearLayout labels = SettingsRowBuilder.vertical(getContext());
        if (name != null && !name.isEmpty()) {
            TextView title = SettingsRowBuilder.label(getContext(), name);
            title.setTextSize(15);
            labels.addView(title, SettingsRowBuilder.matchWrap());
        }
        TextView value = SettingsRowBuilder.secondaryLabel(
                getContext(), text == null || text.isEmpty()
                        ? getContext().getString(R.string.reserved_phrase_empty_hint)
                        : text);
        value.setMaxLines(2);
        labels.addView(value, SettingsRowBuilder.matchWrap());
        if (text != null && !text.isEmpty()) {
            labels.setOnClickListener(v -> {
                onTextSelected.accept(text);
                onClose.run();
            });
        }
        row.addView(labels, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private void addSectionLabel(int titleResId) {
        TextView title = SettingsRowBuilder.secondaryLabel(getContext(), titleResId);
        title.setPadding(0, SettingsRowBuilder.dp(getContext(), 12), 0,
                SettingsRowBuilder.dp(getContext(), 6));
        listContent.addView(title, SettingsRowBuilder.matchWrap());
    }

    private void showRenameDialog(TextToolsStore.Item item) {
        Intent intent = new Intent(getContext(), TextToolsEditActivity.class)
                .putExtra(TextToolsEditActivity.EXTRA_MODE, TextToolsEditActivity.MODE_SAVED_ITEM)
                .putExtra(TextToolsEditActivity.EXTRA_ITEM_ID, item.id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        onClose.run();
    }

    private void showReservedEditDialog(GestureAction action, String label) {
        Intent intent = new Intent(getContext(), TextToolsEditActivity.class)
                .putExtra(TextToolsEditActivity.EXTRA_MODE, TextToolsEditActivity.MODE_RESERVED_PHRASE)
                .putExtra(TextToolsEditActivity.EXTRA_GESTURE_ACTION, action.name())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
        onClose.run();
    }

    private LayoutParams itemLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, SettingsRowBuilder.dp(getContext(), 8));
        return params;
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
