package com.academic.hangulgestureime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class KeyboardLayoutFactory {
    private static final String[] NUMBER_ROW_SYMBOLS = {
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")"
    };

    private KeyboardLayoutFactory() {
    }

    static List<KeyboardRow> build(KeyboardSettings settings) {
        List<KeyboardRow> rows = new ArrayList<>();
        if (settings.showNumberRow) {
            rows.add(numberRow());
        }
        if (settings.keyboardMode == KeyboardMode.ENGLISH) {
            rows.addAll(englishRows());
        } else {
            rows.addAll(hangulRows(settings.hangulSpecialColumnPercent));
        }
        rows.add(bottomRow(settings));
        return rows;
    }

    private static KeyboardRow numberRow() {
        List<GestureKey> keys = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            String value = String.valueOf(i);
            keys.add(numberKey(value, NUMBER_ROW_SYMBOLS[i - 1]));
        }
        keys.add(numberKey("0", NUMBER_ROW_SYMBOLS[9]));
        return new KeyboardRow(keys, 10);
    }

    private static GestureKey numberKey(String number, String symbol) {
        return new GestureKey(number, number, null, symbol, null, null, symbol);
    }

    private static List<KeyboardRow> hangulRows(int specialColumnPercent) {
        int specialPercent = Math.max(
                KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                Math.min(KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT, specialColumnPercent));
        int mainUnits = 100 - specialPercent;
        int specialUnits = specialPercent * 3;
        int baseUnits = 300;
        return Arrays.asList(
                new KeyboardRow(Arrays.asList(
                        mainKey("ㄱ", "ㄱ", "ㄲ", "#", "ㅋ", "ㅋ", mainUnits),
                        mainKey("ㄴ", "ㄴ", "ㄸ", "ㄷ", "ㅌ", "ㅌ", mainUnits),
                        mainKey("ㅢ", "ㅢ", "ㅚ", "ㅟ", "ㅝ", "ㅘ", mainUnits),
                        specialKey("삭제", KeyboardCommands.CMD_DELETE, specialUnits)), baseUnits),
                new KeyboardRow(Arrays.asList(
                        mainKey("ㄹ", "ㄹ", "^", "~", "=", "-", mainUnits),
                        mainKey("ㅁ", "ㅁ", "ㅃ", "ㅂ", "ㅍ", "ㅍ", mainUnits),
                        mainKey(
                                "ㅣ.",
                                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                                "ㅗ",
                                "ㅜ",
                                "ㅓ",
                                "ㅏ",
                                mainUnits),
                        specialKey("?", "?", "!", "*", "+", KeyboardCommands.CMD_NOOP, specialUnits)), baseUnits),
                new KeyboardRow(Arrays.asList(
                        mainKey("ㅅ", "ㅅ", "ㅆ", "2", "1", "3", mainUnits),
                        mainKey("ㅇ", "ㅇ", "♥", "5", "4", "6", mainUnits),
                        mainKey(
                                "ㅡㅐ",
                                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                                "ㅙ",
                                "ㅞ",
                                "ㅔ",
                                "ㅐ",
                                mainUnits),
                        specialKey(".", ".", "\"", "`", ",", KeyboardCommands.CMD_NOOP, specialUnits)), baseUnits),
                new KeyboardRow(Arrays.asList(
                        mainKey("ㅈ", "ㅈ", "ㅉ", "~", "ㅊ", "ㅊ", mainUnits),
                        mainKey("ㅎ", "ㅎ", "0", "8", "7", "9", mainUnits),
                        mainKey(". .", ".", null, null, null, null, mainUnits),
                        specialKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, specialUnits)), baseUnits));
    }

    private static List<KeyboardRow> englishRows() {
        return Arrays.asList(
                englishRow("qwertyuiop", new String[] {
                        "!", "@", "#", "$", "%", "^", "&", "*", "(", ")"
                }),
                englishRow("asdfghjkl", new String[] {
                        "-", "_", "+", "=", "/", "\\", ":", ";", "\""
                }),
                new KeyboardRow(Arrays.asList(
                        GestureKey.command(
                                "Shift",
                                KeyboardCommands.CMD_SHIFT_ONCE,
                                KeyboardCommands.CMD_SHIFT_LOCK,
                                3,
                                KeyIcon.SHIFT),
                        englishKey('z', "'", 2),
                        englishKey('x', "`", 2),
                        englishKey('c', "~", 2),
                        englishKey('v', "<", 2),
                        englishKey('b', ">", 2),
                        englishKey('n', "[", 2),
                        englishKey('m', "]", 2),
                        GestureKey.command("삭제", KeyboardCommands.CMD_DELETE, 3)), 20));
    }

    private static KeyboardRow englishRow(String letters, String[] longPressValues) {
        List<GestureKey> keys = new ArrayList<>();
        for (int i = 0; i < letters.length(); i++) {
            keys.add(englishKey(letters.charAt(i), longPressValues[i], 2));
        }
        return new KeyboardRow(keys, 20);
    }

    private static GestureKey englishKey(char lower, String longPress, int widthUnits) {
        String lowercase = String.valueOf(lower);
        String uppercase = String.valueOf(Character.toUpperCase(lower));
        return new GestureKey(lowercase, lowercase, uppercase, longPress, null, null, longPress, widthUnits);
    }

    private static KeyboardRow bottomRow(KeyboardSettings settings) {
        List<GestureKey> rightHandOrder = Arrays.asList(
                new GestureKey(
                        "옵션",
                        KeyboardCommands.CMD_OPEN_OPTIONS,
                        KeyboardCommands.CMD_HAND_BALANCED,
                        null,
                        KeyboardCommands.CMD_HAND_LEFT,
                        KeyboardCommands.CMD_HAND_RIGHT,
                        null,
                        3,
                        KeyIcon.OPTIONS),
                new GestureKey(
                        "예약어",
                        KeyboardCommands.CMD_RESERVED_PHRASES,
                        KeyboardCommands.CMD_RESERVED_UP,
                        KeyboardCommands.CMD_NOOP,
                        KeyboardCommands.CMD_RESERVED_LEFT,
                        KeyboardCommands.CMD_RESERVED_RIGHT,
                        null,
                        2,
                        KeyIcon.RESERVED),
                spaceKey(settings),
                languageKey(settings),
                GestureKey.command(
                        settings.enterKeyLabel,
                        KeyboardCommands.CMD_ENTER,
                        null,
                        3,
                        KeyIcon.enterForLabel(settings.enterKeyLabel)));

        if (settings.handednessMode != HandednessMode.LEFT) {
            return new KeyboardRow(rightHandOrder, 20);
        }

        List<GestureKey> leftHandOrder = new ArrayList<>(rightHandOrder);
        Collections.reverse(leftHandOrder);
        return new KeyboardRow(leftHandOrder, 20);
    }

    private static GestureKey spaceKey(KeyboardSettings settings) {
        return new GestureKey(
                "스페이스",
                KeyboardCommands.CMD_SPACE,
                settings.keyboardMode == KeyboardMode.ENGLISH ? ".com" : null,
                null,
                KeyboardCommands.CMD_MOVE_LEFT,
                KeyboardCommands.CMD_MOVE_RIGHT,
                null,
                10,
                KeyIcon.SPACE);
    }

    private static GestureKey languageKey(KeyboardSettings settings) {
        if (settings.keyboardMode == KeyboardMode.ENGLISH) {
            return new GestureKey(
                    "한/영",
                    KeyboardCommands.CMD_TOGGLE_LANGUAGE,
                    KeyboardCommands.CMD_NOOP,
                    KeyboardCommands.CMD_NOOP,
                    ",",
                    ",",
                    ".",
                    2,
                    KeyIcon.LANGUAGE);
        }
        return GestureKey.command("한/영", KeyboardCommands.CMD_TOGGLE_LANGUAGE, 2);
    }

    private static GestureKey mainKey(
            String label,
            String tap,
            String up,
            String down,
            String left,
            String right,
            int widthUnits) {
        return new GestureKey(label, tap, up, down, left, right, null, widthUnits, KeyIcon.forCommand(tap, label));
    }

    private static GestureKey specialKey(
            String label,
            String tap,
            String up,
            String down,
            String left,
            String right) {
        return specialKey(label, tap, up, down, left, right, 1);
    }

    private static GestureKey specialKey(
            String label,
            String tap,
            String up,
            String down,
            String left,
            String right,
            int widthUnits) {
        return new GestureKey(label, tap, up, down, left, right, null, widthUnits, KeyIcon.forCommand(tap, label));
    }

    private static GestureKey specialKey(String label, String command) {
        return GestureKey.command(label, command, 1);
    }

    private static GestureKey specialKey(String label, String command, int widthUnits) {
        return GestureKey.command(label, command, widthUnits);
    }
}
