package com.superl3.s3keyboard;

import android.view.KeyEvent;

final class RemoteKeyStroke {
    final int keyCode;
    final int metaState;

    private RemoteKeyStroke(int keyCode, int metaState) {
        this.keyCode = keyCode;
        this.metaState = metaState;
    }

    static RemoteKeyStroke forText(String text) {
        if (text == null || text.length() != 1) {
            return null;
        }
        char ch = text.charAt(0);
        if (ch >= 'a' && ch <= 'z') {
            return new RemoteKeyStroke(KeyEvent.KEYCODE_A + (ch - 'a'), 0);
        }
        if (ch >= 'A' && ch <= 'Z') {
            return shifted(KeyEvent.KEYCODE_A + (ch - 'A'));
        }
        if (ch >= '1' && ch <= '9') {
            return new RemoteKeyStroke(KeyEvent.KEYCODE_1 + (ch - '1'), 0);
        }
        if (ch == '0') {
            return new RemoteKeyStroke(KeyEvent.KEYCODE_0, 0);
        }
        switch (ch) {
            case ' ':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_SPACE, 0);
            case '!':
                return shifted(KeyEvent.KEYCODE_1);
            case '@':
                return shifted(KeyEvent.KEYCODE_2);
            case '#':
                return shifted(KeyEvent.KEYCODE_3);
            case '$':
                return shifted(KeyEvent.KEYCODE_4);
            case '%':
                return shifted(KeyEvent.KEYCODE_5);
            case '^':
                return shifted(KeyEvent.KEYCODE_6);
            case '&':
                return shifted(KeyEvent.KEYCODE_7);
            case '*':
                return shifted(KeyEvent.KEYCODE_8);
            case '(':
                return shifted(KeyEvent.KEYCODE_9);
            case ')':
                return shifted(KeyEvent.KEYCODE_0);
            case '-':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_MINUS, 0);
            case '_':
                return shifted(KeyEvent.KEYCODE_MINUS);
            case '=':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_EQUALS, 0);
            case '+':
                return shifted(KeyEvent.KEYCODE_EQUALS);
            case '[':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_LEFT_BRACKET, 0);
            case '{':
                return shifted(KeyEvent.KEYCODE_LEFT_BRACKET);
            case ']':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_RIGHT_BRACKET, 0);
            case '}':
                return shifted(KeyEvent.KEYCODE_RIGHT_BRACKET);
            case '\\':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_BACKSLASH, 0);
            case '|':
                return shifted(KeyEvent.KEYCODE_BACKSLASH);
            case ';':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_SEMICOLON, 0);
            case ':':
                return shifted(KeyEvent.KEYCODE_SEMICOLON);
            case '\'':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_APOSTROPHE, 0);
            case '"':
                return shifted(KeyEvent.KEYCODE_APOSTROPHE);
            case ',':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_COMMA, 0);
            case '<':
                return shifted(KeyEvent.KEYCODE_COMMA);
            case '.':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_PERIOD, 0);
            case '>':
                return shifted(KeyEvent.KEYCODE_PERIOD);
            case '/':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_SLASH, 0);
            case '?':
                return shifted(KeyEvent.KEYCODE_SLASH);
            case '`':
                return new RemoteKeyStroke(KeyEvent.KEYCODE_GRAVE, 0);
            case '~':
                return shifted(KeyEvent.KEYCODE_GRAVE);
            default:
                return null;
        }
    }

    private static RemoteKeyStroke shifted(int keyCode) {
        return new RemoteKeyStroke(keyCode, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON);
    }
}
