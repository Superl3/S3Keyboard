package com.superl3.s3keyboard;

enum LeftAssistRailItem {
    CLIPBOARD(KeyboardCommands.CMD_CLIPBOARD_PANEL, null, KeyIcon.CLIPBOARD),
    VOICE(KeyboardCommands.CMD_VOICE_INPUT, null, KeyIcon.MICROPHONE),
    UNDO(KeyboardCommands.CMD_UNDO, null, KeyIcon.UNDO),
    TOOLS(KeyboardCommands.CMD_TOOLS, KeyboardCommands.CMD_SETTINGS, KeyIcon.TOOLS);

    final String tapCommand;
    final String longPressCommand;
    final int icon;

    LeftAssistRailItem(String tapCommand, String longPressCommand, int icon) {
        this.tapCommand = tapCommand;
        this.longPressCommand = longPressCommand;
        this.icon = icon;
    }

    GestureKey toKey() {
        return GestureKey.command(tapCommand, tapCommand, longPressCommand, 1, icon);
    }

    static GestureKey keyForRow(int rowIndex) {
        LeftAssistRailItem[] values = values();
        int index = Math.max(0, Math.min(values.length - 1, rowIndex));
        return values[index].toKey();
    }
}
