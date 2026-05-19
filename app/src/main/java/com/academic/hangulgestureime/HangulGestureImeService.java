package com.academic.hangulgestureime;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

public final class HangulGestureImeService extends InputMethodService
        implements HangulKeyboardView.OnKeyGestureListener {
    private final HangulAutomata automata = new HangulAutomata();
    private final DoubleSpacePeriodState doubleSpacePeriodState = new DoubleSpacePeriodState();
    private final EnglishShiftState englishShiftState = new EnglishShiftState();
    private KeyboardSettings settings = KeyboardSettings.defaults();
    private ResolvedImeAction enterAction = ImeActionLabelResolver.defaultAction();
    private EditorInputPolicy editorPolicy = EditorInputPolicy.DEFAULT;
    private HangulKeyboardView inputView;

    @Override
    public View onCreateInputView() {
        settings = KeyboardPreferences.load(this).withEnterKeyLabel(enterAction.label);
        inputView = new HangulKeyboardView(this);
        inputView.setSettings(settings);
        updateShiftStateView();
        inputView.setOnKeyGestureListener(this);
        return inputView;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        loadSettingsForEditor(info);
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        loadSettingsForEditor(attribute);
        automata.reset();
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        updateShiftStateView();
    }

    @Override
    public void onFinishInput() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            commitCurrent(inputConnection);
        }
        automata.reset();
        englishShiftState.reset();
        super.onFinishInput();
    }

    @Override
    public void onUpdateSelection(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        super.onUpdateSelection(
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd);
        if (automata.getComposingText().isEmpty()) {
            return;
        }
        if (isComposingSelectionMismatch(newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                commitCurrent(inputConnection);
            } else {
                automata.reset();
            }
            doubleSpacePeriodState.reset();
        }
    }

    @Override
    public void onKeyGesture(String value) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null || value == null || value.isEmpty()) {
            return;
        }

        switch (value) {
            case KeyboardCommands.CMD_NOOP:
                return;
            case KeyboardCommands.CMD_DELETE:
                doubleSpacePeriodState.reset();
                delete(inputConnection);
                return;
            case KeyboardCommands.CMD_SPACE:
                commitSpace(inputConnection);
                return;
            case KeyboardCommands.CMD_ENTER:
                doubleSpacePeriodState.reset();
                performEnter(inputConnection);
                return;
            case KeyboardCommands.CMD_MOVE_LEFT:
                doubleSpacePeriodState.reset();
                moveCursor(inputConnection, KeyEvent.KEYCODE_DPAD_LEFT);
                return;
            case KeyboardCommands.CMD_MOVE_RIGHT:
                doubleSpacePeriodState.reset();
                moveCursor(inputConnection, KeyEvent.KEYCODE_DPAD_RIGHT);
                return;
            case KeyboardCommands.CMD_TOGGLE_LANGUAGE:
                toggleLanguage(inputConnection);
                return;
            case KeyboardCommands.CMD_SHIFT_ONCE:
                handleShiftOnce();
                return;
            case KeyboardCommands.CMD_SHIFT_LOCK:
                handleShiftLock();
                return;
            case KeyboardCommands.CMD_RESERVED_PHRASES:
            case KeyboardCommands.CMD_RESERVED_LEFT:
            case KeyboardCommands.CMD_RESERVED_RIGHT:
            case KeyboardCommands.CMD_RESERVED_UP:
                commitReservedPhrase(inputConnection, value);
                return;
            case KeyboardCommands.CMD_DINGUL_CENTER_VOWEL:
                inputDingulContextualVowel(inputConnection, true);
                return;
            case KeyboardCommands.CMD_DINGUL_WIDE_VOWEL:
                inputDingulContextualVowel(inputConnection, false);
                return;
            case KeyboardCommands.CMD_OPEN_OPTIONS:
                openOptions(inputConnection);
                return;
            case KeyboardCommands.CMD_HAND_LEFT:
                setHandedness(HandednessMode.LEFT);
                return;
            case KeyboardCommands.CMD_HAND_RIGHT:
                setHandedness(HandednessMode.RIGHT);
                return;
            case KeyboardCommands.CMD_HAND_BALANCED:
                setHandedness(HandednessMode.BALANCED);
                return;
            case KeyboardCommands.CMD_INPUT_PICKER:
                showInputPicker();
                return;
            case KeyboardCommands.CMD_SETTINGS:
                openInputSettings();
                return;
            case KeyboardCommands.CMD_HIDE:
                commitCurrent(inputConnection);
                requestHideSelf(0);
                return;
            default:
                inputText(inputConnection, value);
        }
    }

    private void inputDingulContextualVowel(InputConnection inputConnection, boolean centerVowelKey) {
        doubleSpacePeriodState.reset();
        char fallback = centerVowelKey ? 'ㅣ' : 'ㅡ';
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            sendRawText(String.valueOf(fallback), inputConnection);
            return;
        }
        if (!editorPolicy.allowComposingText) {
            automata.reset();
            inputConnection.commitText(String.valueOf(fallback), 1);
            return;
        }

        char currentVowel = automata.currentVowelWithoutFinal();
        char nextVowel = centerVowelKey
                ? dingulCenterTapValue(currentVowel)
                : dingulWideTapValue(currentVowel);
        if (currentVowel == '\0'
                && combineWithPreviousOpenSyllableForDingulVowel(inputConnection, centerVowelKey)) {
            return;
        }
        inputText(inputConnection, String.valueOf(nextVowel));
    }

    private boolean combineWithPreviousOpenSyllableForDingulVowel(
            InputConnection inputConnection,
            boolean centerVowelKey) {
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }

        String decomposed = HangulAutomata.decomposeOpenSyllable(beforeCursor.charAt(0));
        if (decomposed == null || decomposed.length() != 2) {
            return false;
        }

        char previousVowel = decomposed.charAt(1);
        char nextVowel = centerVowelKey
                ? dingulCenterTapValue(previousVowel)
                : dingulWideTapValue(previousVowel);
        if (nextVowel == (centerVowelKey ? 'ㅣ' : 'ㅡ')) {
            return false;
        }

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
        }
        String committed = automata.input(nextVowel);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        updateComposing(inputConnection);
        return true;
    }

    private char dingulCenterTapValue(char currentVowel) {
        switch (currentVowel) {
            case 'ㅓ':
            case 'ㅏ':
            case 'ㅗ':
            case 'ㅜ':
                return currentVowel;
            default:
                return 'ㅣ';
        }
    }

    private char dingulWideTapValue(char currentVowel) {
        switch (currentVowel) {
            case 'ㅔ':
            case 'ㅐ':
                return currentVowel;
            default:
                return 'ㅡ';
        }
    }

    private void inputText(InputConnection inputConnection, String text) {
        doubleSpacePeriodState.reset();
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            String rawText = settings.keyboardMode == KeyboardMode.ENGLISH
                    ? englishShiftState.applyToInput(text)
                    : text;
            sendRawText(rawText, inputConnection);
            updateShiftStateView();
            return;
        }
        if (settings.keyboardMode == KeyboardMode.ENGLISH) {
            inputConnection.commitText(englishShiftState.applyToInput(text), 1);
            updateShiftStateView();
            return;
        }

        if (!editorPolicy.allowComposingText) {
            automata.reset();
            inputConnection.commitText(text, 1);
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (automata.isEmpty()
                    && HangulAutomata.isVowel(ch)
                    && combineWithPreviousStandaloneConsonant(inputConnection, ch)) {
                continue;
            }
            if (automata.isEmpty()
                    && HangulAutomata.canBeFinalConsonant(ch)
                    && combineWithPreviousOpenSyllable(inputConnection, ch)) {
                continue;
            }
            String committed = automata.input(ch);
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
            updateComposing(inputConnection);
        }
    }

    private boolean combineWithPreviousStandaloneConsonant(InputConnection inputConnection, char vowel) {
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        char previous = beforeCursor.charAt(0);
        if (!HangulAutomata.isInitialConsonant(previous)) {
            return false;
        }

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        String committed = automata.input(previous);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        committed = automata.input(vowel);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        updateComposing(inputConnection);
        return true;
    }

    private boolean combineWithPreviousOpenSyllable(InputConnection inputConnection, char finalConsonant) {
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        String decomposed = HangulAutomata.decomposeOpenSyllable(beforeCursor.charAt(0));
        if (decomposed == null) {
            return false;
        }

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
        }
        String committed = automata.input(finalConsonant);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        updateComposing(inputConnection);
        return true;
    }

    private void loadSettingsForEditor(EditorInfo info) {
        enterAction = ImeActionLabelResolver.resolve(info);
        editorPolicy = EditorInputPolicy.from(info);
        KeyboardSettings storedSettings = KeyboardPreferences.load(this);
        KeyboardMode runtimeMode = editorPolicy.initialKeyboardMode(storedSettings.keyboardMode);
        settings = storedSettings
                .withKeyboardMode(runtimeMode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
    }

    private void commitSpace(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (editorPolicy.rawKeyInput) {
            doubleSpacePeriodState.reset();
            sendKeyChar(' ');
            return;
        }
        DoubleSpacePeriodState.SpaceResult result = doubleSpacePeriodState.onSpace(
                settings.keyboardMode,
                settings.englishDoubleSpacePeriodEnabled && editorPolicy.allowTextConveniences,
                System.currentTimeMillis());
        if (result == DoubleSpacePeriodState.SpaceResult.REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE) {
            inputConnection.deleteSurroundingText(1, 0);
            inputConnection.commitText(". ", 1);
        } else {
            inputConnection.commitText(" ", 1);
        }
    }

    private void performEnter(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (editorPolicy.rawKeyInput) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
            return;
        }
        if (enterAction.performEditorAction) {
            inputConnection.performEditorAction(enterAction.editorActionId);
            return;
        }
        if (!inputConnection.commitText("\n", 1)) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        }
    }

    private void toggleLanguage(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        boolean lockedToEnglish = editorPolicy.locksLanguageToggle();
        KeyboardMode nextMode = lockedToEnglish ? KeyboardMode.ENGLISH : settings.keyboardMode.next();
        settings = settings.withKeyboardMode(nextMode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        if (!lockedToEnglish) {
            KeyboardPreferences.saveKeyboardMode(this, nextMode);
        }
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    private void handleShiftOnce() {
        if (settings.keyboardMode != KeyboardMode.ENGLISH) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftOnceCommand();
        updateShiftStateView();
    }

    private void handleShiftLock() {
        if (settings.keyboardMode != KeyboardMode.ENGLISH) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftLockCommand();
        updateShiftStateView();
    }

    private void updateShiftStateView() {
        if (inputView != null) {
            inputView.setEnglishShiftState(
                    settings.keyboardMode == KeyboardMode.ENGLISH && englishShiftState.isActive(),
                    settings.keyboardMode == KeyboardMode.ENGLISH && englishShiftState.isLocked());
        }
    }

    private void delete(InputConnection inputConnection) {
        if (editorPolicy.rawKeyInput) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            return;
        }
        if (editorPolicy.allowComposingText && automata.backspace()) {
            updateComposing(inputConnection);
        } else {
            deleteOneCodePoint(inputConnection);
        }
    }

    private void deleteOneCodePoint(InputConnection inputConnection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && inputConnection.deleteSurroundingTextInCodePoints(1, 0)) {
            return;
        }
        inputConnection.deleteSurroundingText(1, 0);
    }

    private void moveCursor(InputConnection inputConnection, int keyCode) {
        commitCurrent(inputConnection);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

    private void updateComposing(InputConnection inputConnection) {
        String composing = automata.getComposingText();
        if (composing.isEmpty()) {
            inputConnection.finishComposingText();
        } else {
            inputConnection.setComposingText(composing, 1);
        }
    }

    private boolean isComposingSelectionMismatch(
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        if (newSelStart != newSelEnd) {
            return true;
        }
        if (candidatesStart < 0 || candidatesEnd < 0) {
            return false;
        }
        return newSelStart < candidatesStart || newSelStart != candidatesEnd;
    }

    private void sendRawText(String text, InputConnection inputConnection) {
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '\n') {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
            } else if (codePoint >= 0x20 && codePoint <= 0x7E) {
                sendKeyChar((char) codePoint);
            } else {
                inputConnection.commitText(new String(Character.toChars(codePoint)), 1);
            }
        }
    }

    private void commitCurrent(InputConnection inputConnection) {
        String composing = automata.flush();
        if (composing.isEmpty()) {
            inputConnection.finishComposingText();
        } else {
            inputConnection.commitText(composing, 1);
        }
    }

    private void commitReservedPhrase(InputConnection inputConnection, String command) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        String phrase = KeyboardPreferences.loadReservedPhraseForCommand(this, command);
        if (phrase == null || phrase.isEmpty()) {
            return;
        }
        if (editorPolicy.rawKeyInput) {
            sendRawText(phrase, inputConnection);
        } else {
            inputConnection.commitText(phrase, 1);
        }
    }

    private void showInputPicker() {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    private void openOptions(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setHandedness(HandednessMode mode) {
        doubleSpacePeriodState.reset();
        settings = settings.withHandednessPreset(mode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        KeyboardPreferences.saveHandednessPreset(this, settings);
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    private void openInputSettings() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
