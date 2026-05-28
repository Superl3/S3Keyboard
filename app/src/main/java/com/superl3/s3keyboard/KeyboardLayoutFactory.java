package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class KeyboardLayoutFactory {
    private static final String[] NUMBER_ROW_SYMBOLS = {
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")"
    };
    private static final EnglishSlideSpec[] TOP_QWERTY_SLIDES = {
            single("1"), single("2"), single("3"), single("4"), single("5"),
            single("6"), single("7"), single("8"), single("9"), single("0")
    };
    private static final EnglishSlideSpec[] REMOTE_TOP_QWERTY_SLIDES = {
            up(KeyboardCommands.CMD_REMOTE_TAB),
            none(),
            none(),
            up(KeyboardCommands.CMD_REMOTE_SHIFT_TAB),
            up(KeyboardCommands.CMD_REMOTE_CTRL_TAB),
            up(KeyboardCommands.CMD_REMOTE_ALT_TAB),
            none(),
            vertical(KeyboardCommands.CMD_REMOTE_INSERT, KeyboardCommands.CMD_REMOTE_FORWARD_DELETE),
            vertical(KeyboardCommands.CMD_REMOTE_HOME, KeyboardCommands.CMD_REMOTE_END),
            vertical(KeyboardCommands.CMD_REMOTE_PAGE_UP, KeyboardCommands.CMD_REMOTE_PAGE_DOWN)
    };
    private static final EnglishSlideSpec[] HOME_QWERTY_SLIDES = {
            single("@"), pair("#", "%"), single("/"), single("*"), pair("~", "^"),
            pair("_", "-"), pair("+", "="), pair("<", ">"), single("♥")
    };

    private KeyboardLayoutFactory() {
    }

    static List<KeyboardRow> build(KeyboardSettings settings) {
        return build(settings, KeyboardSurface.NORMAL);
    }

    static List<KeyboardRow> build(KeyboardSettings settings, KeyboardSurface surface) {
        KeyboardSurface effectiveSurface = effectiveSurface(settings, surface);
        List<KeyboardRow> rows = new ArrayList<>();
        if (replacesMainRows(effectiveSurface)) {
            rows.addAll(surfaceRows(effectiveSurface));
        } else {
            if (settings.showNumberRow) {
                rows.add(numberRow(settings));
            }
            if (settings.keyboardMode == KeyboardMode.ENGLISH) {
                rows.addAll(englishRows(settings.remoteModeEnabled));
            } else {
                rows.addAll(hangulRows(settings.hangulSpecialColumnPercent));
            }
        }
        rows.add(bottomRow(settings));
        return rows;
    }

    private static KeyboardSurface effectiveSurface(KeyboardSettings settings, KeyboardSurface surface) {
        if (settings.remoteModeEnabled) {
            return KeyboardSurface.NORMAL;
        }
        return surface == null ? KeyboardSurface.NORMAL : surface;
    }

    private static boolean replacesMainRows(KeyboardSurface surface) {
        return surface == KeyboardSurface.NUMPAD
                || surface == KeyboardSurface.PHONEPAD
                || surface == KeyboardSurface.DATEPAD
                || surface == KeyboardSurface.PINPAD;
    }

    private static List<KeyboardRow> surfaceRows(KeyboardSurface surface) {
        switch (surface) {
            case PHONEPAD:
                return phonepadRows();
            case DATEPAD:
                return datepadRows();
            case PINPAD:
                return pinpadRows();
            case NUMPAD:
            default:
                return numpadRows();
        }
    }

    private static KeyboardRow numberRow(KeyboardSettings settings) {
        List<GestureKey> keys = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            String value = String.valueOf(i);
            keys.add(numberKey(value, NUMBER_ROW_SYMBOLS[i - 1], settings.remoteModeEnabled, i));
        }
        keys.add(numberKey("0", NUMBER_ROW_SYMBOLS[9], settings.remoteModeEnabled, 10));
        return new KeyboardRow(keys, 10);
    }

    private static GestureKey numberKey(String number, String symbol, boolean remoteModeEnabled, int index) {
        if (remoteModeEnabled) {
            return remoteNumberKey(number, index);
        }
        return new GestureKey(number, number, null, symbol, null, null, null);
    }

    private static GestureKey remoteNumberKey(String number, int index) {
        String up;
        String down = remoteFunctionCommand(index);
        switch (index) {
            case 1:
                up = KeyboardCommands.CMD_REMOTE_ESC;
                break;
            case 9:
                up = KeyboardCommands.CMD_REMOTE_F11;
                break;
            case 10:
                up = KeyboardCommands.CMD_REMOTE_F12;
                break;
            default:
                up = null;
                break;
        }
        return new GestureKey(number, number, up, down, null, null, null);
    }

    private static String remoteFunctionCommand(int index) {
        switch (index) {
            case 1:
                return KeyboardCommands.CMD_REMOTE_F1;
            case 2:
                return KeyboardCommands.CMD_REMOTE_F2;
            case 3:
                return KeyboardCommands.CMD_REMOTE_F3;
            case 4:
                return KeyboardCommands.CMD_REMOTE_F4;
            case 5:
                return KeyboardCommands.CMD_REMOTE_F5;
            case 6:
                return KeyboardCommands.CMD_REMOTE_F6;
            case 7:
                return KeyboardCommands.CMD_REMOTE_F7;
            case 8:
                return KeyboardCommands.CMD_REMOTE_F8;
            case 9:
                return KeyboardCommands.CMD_REMOTE_F9;
            case 10:
                return KeyboardCommands.CMD_REMOTE_F10;
            default:
                return null;
        }
    }

    private static List<KeyboardRow> numpadRows() {
        return Arrays.asList(
                surfaceRow(textKey("1"), textKey("2"), textKey("3"), deleteKey(4)),
                surfaceRow(textKey("4"), textKey("5"), textKey("6"), textKey("-")),
                surfaceRow(textKey("7"), textKey("8"), textKey("9"), textKey(".")),
                surfaceRow(textKey("+"), textKey("0"), textKey("00"), textKey("/")));
    }

    private static List<KeyboardRow> phonepadRows() {
        return Arrays.asList(
                surfaceRow(textKey("1"), textKey("2"), textKey("3"), deleteKey(4)),
                surfaceRow(textKey("4"), textKey("5"), textKey("6"), textKey("+")),
                surfaceRow(textKey("7"), textKey("8"), textKey("9"), textKey("*")),
                surfaceRow(textKey("-"), textKey("0"), textKey("#"), textKey(",")));
    }

    private static List<KeyboardRow> datepadRows() {
        return Arrays.asList(
                surfaceRow(textKey("1"), textKey("2"), textKey("3"), deleteKey(4)),
                surfaceRow(textKey("4"), textKey("5"), textKey("6"), textKey("-")),
                surfaceRow(textKey("7"), textKey("8"), textKey("9"), textKey("/")),
                surfaceRow(textKey(":"), textKey("0"), textKey("."), textKey(",")));
    }

    private static List<KeyboardRow> pinpadRows() {
        return Arrays.asList(
                surfaceRow(12, textKey("1", 4), textKey("2", 4), textKey("3", 4)),
                surfaceRow(12, textKey("4", 4), textKey("5", 4), textKey("6", 4)),
                surfaceRow(12, textKey("7", 4), textKey("8", 4), textKey("9", 4)),
                surfaceRow(12, deleteKey(4), textKey("0", 4), enterKey(4)));
    }

    private static KeyboardRow surfaceRow(GestureKey... keys) {
        return surfaceRow(16, keys);
    }

    private static KeyboardRow surfaceRow(int baseUnits, GestureKey... keys) {
        return new KeyboardRow(Arrays.asList(keys), baseUnits);
    }

    private static GestureKey textKey(String value) {
        return textKey(value, 4);
    }

    private static GestureKey textKey(String value, int widthUnits) {
        return new GestureKey(value, value, null, null, null, null, null, widthUnits);
    }

    private static GestureKey deleteKey(int widthUnits) {
        return GestureKey.command("Del", KeyboardCommands.CMD_DELETE, null, widthUnits, KeyIcon.BACKSPACE);
    }

    private static GestureKey enterKey(int widthUnits) {
        return GestureKey.command("Enter", KeyboardCommands.CMD_ENTER, null, widthUnits, KeyIcon.ENTER);
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
                        mainKey(". .", " ", "\u315B", "\u3160", "\u3155", "\u3151", mainUnits),
                        specialKey("/", "/", ":", ";", "@", KeyboardCommands.CMD_NOOP, specialUnits)), baseUnits));
    }

    private static List<KeyboardRow> englishRows(boolean remoteModeEnabled) {
        EnglishSlideSpec[] topSlides = remoteModeEnabled ? REMOTE_TOP_QWERTY_SLIDES : TOP_QWERTY_SLIDES;
        return Arrays.asList(
                englishRow("qwertyuiop", topSlides),
                englishRow("asdfghjkl", HOME_QWERTY_SLIDES),
                new KeyboardRow(Arrays.asList(
                        GestureKey.command(
                                "Shift",
                                KeyboardCommands.CMD_SHIFT_ONCE,
                                KeyboardCommands.CMD_SHIFT_LOCK,
                                3,
                                KeyIcon.SHIFT),
                        englishKey('z', pair("(", ")"), 2),
                        englishKey('x', pair("[", "]"), 2),
                        englishKey('c', pair(";", ":"), 2),
                        englishKey('v', pair("'", "\""), 2),
                        englishKey('b', pair("&", "|"), 2),
                        englishKey('n', single("!"), 2),
                        englishKey('m', single("?"), 2),
                        GestureKey.command(
                                "삭제",
                                KeyboardCommands.CMD_DELETE,
                                null,
                                3,
                                KeyIcon.BACKSPACE)), 20));
    }

    private static KeyboardRow englishRow(String letters, EnglishSlideSpec[] slides) {
        List<GestureKey> keys = new ArrayList<>();
        for (int i = 0; i < letters.length(); i++) {
            keys.add(englishKey(letters.charAt(i), slides[i], 2));
        }
        return new KeyboardRow(keys, 20);
    }

    private static GestureKey englishKey(char lower, EnglishSlideSpec slide, int widthUnits) {
        String lowercase = String.valueOf(lower);
        String uppercase = String.valueOf(Character.toUpperCase(lower));
        return new GestureKey(
                lowercase,
                lowercase,
                slide.up == null ? uppercase : slide.up,
                slide.down,
                slide.left,
                slide.right,
                slide.longPress,
                widthUnits);
    }

    private static EnglishSlideSpec single(String value) {
        return new EnglishSlideSpec(null, value, null, null, null);
    }

    private static EnglishSlideSpec pair(String left, String right) {
        return new EnglishSlideSpec(null, null, left, right, null);
    }

    private static EnglishSlideSpec up(String value) {
        return new EnglishSlideSpec(value, null, null, null, null);
    }

    private static EnglishSlideSpec vertical(String up, String down) {
        return new EnglishSlideSpec(up, down, null, null, null);
    }

    private static EnglishSlideSpec none() {
        return new EnglishSlideSpec(null, null, null, null, null);
    }

    private static KeyboardRow bottomRow(KeyboardSettings settings) {
        if (settings.remoteModeEnabled && settings.remoteKeyPreset == RemoteKeyPreset.PC_KEYBOARD) {
            return remoteBottomRow(settings);
        }
        List<GestureKey> rightHandOrder = Arrays.asList(
                new GestureKey(
                        "옵션",
                        KeyboardCommands.CMD_OPEN_OPTIONS,
                        KeyboardCommands.CMD_QUICK_SETTINGS,
                        KeyboardCommands.CMD_HAND_BALANCED,
                        KeyboardCommands.CMD_HAND_LEFT,
                        KeyboardCommands.CMD_HAND_RIGHT,
                        KeyboardCommands.CMD_QUICK_SETTINGS,
                        3,
                        KeyIcon.SETTINGS),
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

    private static KeyboardRow remoteBottomRow(KeyboardSettings settings) {
        List<GestureKey> rightHandOrder = Arrays.asList(
                new GestureKey(
                        "Ctrl",
                        KeyboardCommands.CMD_REMOTE_CTRL_LATCH,
                        null,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_REMOTE_CTRL_LOCK,
                        2,
                        KeyIcon.NONE),
                new GestureKey(
                        "Win",
                        KeyboardCommands.CMD_REMOTE_WIN_LATCH,
                        null,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_REMOTE_WIN_LOCK,
                        2,
                        KeyIcon.NONE),
                new GestureKey(
                        "Alt",
                        KeyboardCommands.CMD_REMOTE_ALT_LATCH,
                        null,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_REMOTE_ALT_LOCK,
                        2,
                        KeyIcon.NONE),
                new GestureKey(
                        "스페이스",
                        KeyboardCommands.CMD_SPACE,
                        KeyboardCommands.CMD_REMOTE_ARROW_UP,
                        KeyboardCommands.CMD_REMOTE_ARROW_DOWN,
                        KeyboardCommands.CMD_REMOTE_ARROW_LEFT,
                        KeyboardCommands.CMD_REMOTE_ARROW_RIGHT,
                        null,
                        8,
                        KeyIcon.SPACE),
                new GestureKey(
                        "한/영",
                        KeyboardCommands.CMD_REMOTE_IME_TOGGLE,
                        null,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_TOGGLE_LANGUAGE,
                        2,
                        KeyIcon.LANGUAGE),
                new GestureKey(
                        "옵션",
                        KeyboardCommands.CMD_OPEN_OPTIONS,
                        KeyboardCommands.CMD_QUICK_SETTINGS,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_QUICK_SETTINGS,
                        2,
                        KeyIcon.SETTINGS),
                new GestureKey(
                        settings.enterKeyLabel,
                        KeyboardCommands.CMD_ENTER,
                        null,
                        null,
                        null,
                        null,
                        KeyboardCommands.CMD_REMOTE_CTRL_ENTER,
                        2,
                        KeyIcon.enterForLabel(settings.enterKeyLabel)));

        return new KeyboardRow(rightHandOrder, 20);
    }

    private static GestureKey spaceKey(KeyboardSettings settings) {
        return new GestureKey(
                "스페이스",
                KeyboardCommands.CMD_SPACE,
                ".com",
                null,
                KeyboardCommands.CMD_MOVE_LEFT,
                KeyboardCommands.CMD_MOVE_RIGHT,
                null,
                10,
                KeyIcon.SPACE);
    }

    private static GestureKey languageKey(KeyboardSettings settings) {
        return new GestureKey(
                "한/영",
                KeyboardCommands.CMD_TOGGLE_LANGUAGE,
                KeyboardCommands.CMD_NOOP,
                KeyboardCommands.CMD_NOOP,
                ",",
                ".",
                null,
                2,
                KeyIcon.LANGUAGE);
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

    private static final class EnglishSlideSpec {
        final String down;
        final String left;
        final String right;
        final String longPress;

        final String up;

        EnglishSlideSpec(String up, String down, String left, String right, String longPress) {
            this.up = up;
            this.down = down;
            this.left = left;
            this.right = right;
            this.longPress = longPress;
        }
    }
}
