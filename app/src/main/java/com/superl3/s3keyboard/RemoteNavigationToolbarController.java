package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class RemoteNavigationToolbarController {
    private static final Item[] ITEMS = {
            item("Esc", KeyboardCommands.CMD_REMOTE_ESC),
            item("Tab", KeyboardCommands.CMD_REMOTE_TAB),
            item("←", KeyboardCommands.CMD_REMOTE_ARROW_LEFT),
            item("↑", KeyboardCommands.CMD_REMOTE_ARROW_UP),
            item("↓", KeyboardCommands.CMD_REMOTE_ARROW_DOWN),
            item("→", KeyboardCommands.CMD_REMOTE_ARROW_RIGHT),
            item("Home", KeyboardCommands.CMD_REMOTE_HOME),
            item("End", KeyboardCommands.CMD_REMOTE_END),
            item("PgUp", KeyboardCommands.CMD_REMOTE_PAGE_UP),
            item("PgDn", KeyboardCommands.CMD_REMOTE_PAGE_DOWN),
            item("Ins", KeyboardCommands.CMD_REMOTE_INSERT),
            item("Del", KeyboardCommands.CMD_REMOTE_FORWARD_DELETE),
            item("F1", KeyboardCommands.CMD_REMOTE_F1),
            item("F2", KeyboardCommands.CMD_REMOTE_F2),
            item("F3", KeyboardCommands.CMD_REMOTE_F3),
            item("F4", KeyboardCommands.CMD_REMOTE_F4),
            item("F5", KeyboardCommands.CMD_REMOTE_F5),
            item("F6", KeyboardCommands.CMD_REMOTE_F6),
            item("F7", KeyboardCommands.CMD_REMOTE_F7),
            item("F8", KeyboardCommands.CMD_REMOTE_F8),
            item("F9", KeyboardCommands.CMD_REMOTE_F9),
            item("F10", KeyboardCommands.CMD_REMOTE_F10),
            item("F11", KeyboardCommands.CMD_REMOTE_F11),
            item("F12", KeyboardCommands.CMD_REMOTE_F12)
    };

    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<String> commandHandler;
    private final Runnable clearModifiersHandler;
    private HorizontalScrollView root;

    RemoteNavigationToolbarController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<String> commandHandler,
            Runnable clearModifiersHandler) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.commandHandler = RuntimeDefaults.stringConsumer(commandHandler);
        this.clearModifiersHandler = RuntimeDefaults.runnable(clearModifiersHandler);
    }
    View createView() {
        root = new HorizontalScrollView(context);
        root.setHorizontalScrollBarEnabled(false);
        LinearLayout row = SettingsRowBuilder.horizontal(context);
        row.setPadding(
                SettingsRowBuilder.dp(context, 4), 0,
                SettingsRowBuilder.dp(context, 4), 0);
        Button clearModifiers = SettingsRowBuilder.button(
                context, "Clear Mods", view -> clearModifiersHandler.run());
        clearModifiers.setMinWidth(0);
        clearModifiers.setMinimumWidth(0);
        clearModifiers.setPadding(
                SettingsRowBuilder.dp(context, 9), 0,
                SettingsRowBuilder.dp(context, 9), 0);
        row.addView(clearModifiers, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                SettingsRowBuilder.dp(context, 38)));
        for (Item item : ITEMS) {
            Button button = SettingsRowBuilder.button(
                    context, item.label, view -> commandHandler.accept(item.command));
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(
                    SettingsRowBuilder.dp(context, 9), 0,
                    SettingsRowBuilder.dp(context, 9), 0);
            row.addView(button, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    SettingsRowBuilder.dp(context, 38)));
        }
        root.addView(row, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.WRAP_CONTENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT));
        updateVisibility();
        return root;
    }

    void updateVisibility() {
        if (root != null) {
            root.setVisibility(RuntimeDefaults.keyboardSettingsFrom(settings).remoteModeEnabled
                    ? View.VISIBLE : View.GONE);
        }
    }
    private static Item item(String label, String command) {
        return new Item(label, command);
    }

    private static final class Item {
        final String label;
        final String command;

        Item(String label, String command) {
            this.label = label;
            this.command = command;
        }
    }
}
