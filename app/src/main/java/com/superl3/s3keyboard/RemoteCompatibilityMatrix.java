package com.superl3.s3keyboard;

import android.view.KeyEvent;

final class RemoteCompatibilityMatrix {
    static final Case ESC = key("Esc", KeyEvent.KEYCODE_ESCAPE, 0, Group.BASIC);
    static final Case TAB = key("Tab", KeyEvent.KEYCODE_TAB, 0, Group.BASIC);
    static final Case SHIFT_TAB = key("Shift+Tab", KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON, Group.BASIC);
    static final Case CTRL_TAB = key("Ctrl+Tab", KeyEvent.KEYCODE_TAB, KeyEvent.META_CTRL_ON, Group.BASIC);
    static final Case ALT_TAB = key("Alt+Tab", KeyEvent.KEYCODE_TAB, KeyEvent.META_ALT_ON, Group.BASIC);
    static final Case CTRL_A = key("Ctrl+A", KeyEvent.KEYCODE_A, KeyEvent.META_CTRL_ON, Group.BASIC);
    static final Case ALT_SHIFT = key("Alt+Shift", KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.META_ALT_ON, Group.IME);
    static final Case CTRL_SPACE = key("Ctrl+Space", KeyEvent.KEYCODE_SPACE, KeyEvent.META_CTRL_ON, Group.IME);
    static final Case WIN_SPACE = key("Win+Space", KeyEvent.KEYCODE_SPACE, KeyEvent.META_META_ON, Group.IME);
    static final Case LANGUAGE_SWITCH =
            key("Lang", KeyEvent.KEYCODE_LANGUAGE_SWITCH, 0, Group.IME);

    private static final Case[] ALL = {
            ESC,
            TAB,
            SHIFT_TAB,
            CTRL_TAB,
            ALT_TAB,
            CTRL_A,
            key("F1", KeyEvent.KEYCODE_F1, 0, Group.FUNCTION),
            key("F2", KeyEvent.KEYCODE_F2, 0, Group.FUNCTION),
            key("F3", KeyEvent.KEYCODE_F3, 0, Group.FUNCTION),
            key("F4", KeyEvent.KEYCODE_F4, 0, Group.FUNCTION),
            key("F5", KeyEvent.KEYCODE_F5, 0, Group.FUNCTION),
            key("F6", KeyEvent.KEYCODE_F6, 0, Group.FUNCTION),
            key("F7", KeyEvent.KEYCODE_F7, 0, Group.FUNCTION),
            key("F8", KeyEvent.KEYCODE_F8, 0, Group.FUNCTION),
            key("F9", KeyEvent.KEYCODE_F9, 0, Group.FUNCTION),
            key("F10", KeyEvent.KEYCODE_F10, 0, Group.FUNCTION),
            key("F11", KeyEvent.KEYCODE_F11, 0, Group.FUNCTION),
            key("F12", KeyEvent.KEYCODE_F12, 0, Group.FUNCTION),
            ALT_SHIFT,
            CTRL_SPACE,
            WIN_SPACE,
            LANGUAGE_SWITCH
    };

    private RemoteCompatibilityMatrix() {
    }

    static Case[] all() {
        return ALL.clone();
    }

    static String[] labels() {
        String[] labels = new String[ALL.length];
        for (int i = 0; i < ALL.length; i++) {
            labels[i] = ALL[i].label;
        }
        return labels;
    }

    static Case[] group(Group group) {
        int count = 0;
        for (Case testCase : ALL) {
            if (testCase.group == group) {
                count++;
            }
        }
        Case[] result = new Case[count];
        int index = 0;
        for (Case testCase : ALL) {
            if (testCase.group == group) {
                result[index++] = testCase;
            }
        }
        return result;
    }

    static Case findByLabel(String label) {
        if (label == null) {
            return null;
        }
        for (Case testCase : ALL) {
            if (testCase.label.equals(label)) {
                return testCase;
            }
        }
        return null;
    }

    private static Case key(String label, int keyCode, int metaState, Group group) {
        return new Case(label, keyCode, metaState, group);
    }

    enum Group {
        BASIC,
        FUNCTION,
        IME
    }

    static final class Case {
        final String label;
        final int keyCode;
        final int metaState;
        final Group group;

        private Case(String label, int keyCode, int metaState, Group group) {
            this.label = label;
            this.keyCode = keyCode;
            this.metaState = metaState;
            this.group = group;
        }
    }
}
