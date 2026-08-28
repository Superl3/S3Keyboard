package com.superl3.s3keyboard;

final class KeyboardCommandDispatcher {
    private KeyboardCommandDispatcher() {
    }

    static void dispatch(String value, Target target) {
        if (target == null) {
            return;
        }
        switch (KeyboardCommandRouter.route(value)) {
            case NOOP:
                return;
            case DELETE:
                target.delete();
                return;
            case DELETE_WORD:
                target.deleteWord();
                return;
            case SPACE:
                target.space();
                return;
            case ENTER:
                target.enter();
                return;
            case CORRECT_TEXT:
                target.correctText();
                return;
            case NEWLINE:
                target.newline();
                return;
            case MOVE_LEFT:
                target.moveLeft();
                return;
            case MOVE_RIGHT:
                target.moveRight();
                return;
            case TOGGLE_LANGUAGE:
                target.toggleLanguage();
                return;
            case SHIFT_ONCE:
                target.shiftOnce();
                return;
            case SHIFT_LOCK:
                target.shiftLock();
                return;
            case RESERVED_PHRASE:
                target.reservedPhrase(value);
                return;
            case DINGUL_CENTER_VOWEL:
                target.dingulCenterVowel();
                return;
            case DINGUL_WIDE_VOWEL:
                target.dingulWideVowel();
                return;
            case OPEN_OPTIONS:
                target.openOptions();
                return;
            case QUICK_SETTINGS:
                target.quickSettings();
                return;
            case CLIPBOARD_PANEL:
                target.clipboardPanel();
                return;
            case VOICE_INPUT:
                target.voiceInput();
                return;
            case UNDO:
                target.undo();
                return;
            case TOOLS:
                target.tools();
                return;
            case HAND_LEFT:
                target.setHandedness(HandednessMode.LEFT);
                return;
            case HAND_RIGHT:
                target.setHandedness(HandednessMode.RIGHT);
                return;
            case HAND_BALANCED:
                target.setHandedness(HandednessMode.BALANCED);
                return;
            case INPUT_PICKER:
                target.inputPicker();
                return;
            case SETTINGS:
                target.settings();
                return;
            case HIDE:
                target.hide();
                return;
            case REMOTE:
                target.remote(value);
                return;
            case TEXT:
            default:
                target.text(value);
        }
    }

    abstract static class Target {
        abstract void delete();

        abstract void deleteWord();

        abstract void space();

        abstract void enter();

        abstract void correctText();

        abstract void newline();

        abstract void moveLeft();

        abstract void moveRight();

        abstract void toggleLanguage();

        abstract void shiftOnce();

        abstract void shiftLock();

        abstract void reservedPhrase(String command);

        abstract void dingulCenterVowel();

        abstract void dingulWideVowel();

        abstract void openOptions();

        abstract void quickSettings();

        abstract void clipboardPanel();

        abstract void voiceInput();

        abstract void undo();

        abstract void tools();

        abstract void setHandedness(HandednessMode mode);

        abstract void inputPicker();

        abstract void settings();

        abstract void hide();

        abstract void remote(String command);

        abstract void text(String value);
    }
}
