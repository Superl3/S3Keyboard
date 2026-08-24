package com.superl3.s3keyboard;

import android.view.inputmethod.InputConnection;

final class S3KeyboardCommandTarget extends KeyboardCommandDispatcher.Target {
    private final S3KeyboardService service;

    S3KeyboardCommandTarget(S3KeyboardService service) {
        this.service = service;
    }

    @Override
    void delete() {
        InputConnection inputConnection = service.commandInputConnection();
        service.resetDoubleSpacePeriodState();
        service.delete(inputConnection);
    }

    @Override
    void deleteWord() {
        InputConnection inputConnection = service.commandInputConnection();
        service.resetDoubleSpacePeriodState();
        service.deleteWord(inputConnection);
    }

    @Override
    void space() {
        service.commitSpace(service.commandInputConnection());
    }

    @Override
    void enter() {
        InputConnection inputConnection = service.commandInputConnection();
        service.resetDoubleSpacePeriodState();
        service.performEnter(inputConnection);
    }

    @Override
    void newline() {
        InputConnection inputConnection = service.commandInputConnection();
        service.resetDoubleSpacePeriodState();
        service.commitExplicitNewline(inputConnection);
    }

    @Override
    void moveLeft() {
        moveCursor(false);
    }

    @Override
    void moveRight() {
        moveCursor(true);
    }

    @Override
    void toggleLanguage() {
        service.toggleLanguage(service.commandInputConnection());
    }

    @Override
    void shiftOnce() {
        service.handleShiftOnce();
    }

    @Override
    void shiftLock() {
        service.handleShiftLock();
    }

    @Override
    void reservedPhrase(String command) {
        service.commitReservedPhrase(service.commandInputConnection(), command);
    }

    @Override
    void dingulCenterVowel() {
        service.inputDingulContextualVowel(service.commandInputConnection(), true);
    }

    @Override
    void dingulWideVowel() {
        service.inputDingulContextualVowel(service.commandInputConnection(), false);
    }

    @Override
    void openOptions() {
        service.openOptions(service.commandInputConnection());
    }

    @Override
    void quickSettings() {
        service.showQuickSettings();
    }

    @Override
    void clipboardPanel() {
        service.toggleClipboardPanel();
    }

    @Override
    void voiceInput() {
        service.handleVoiceInput();
    }

    @Override
    void undo() {
        service.handleUndo();
    }

    @Override
    void tools() {
        service.showQuickSettings();
    }

    @Override
    void setHandedness(HandednessMode mode) {
        service.setHandedness(mode);
    }

    @Override
    void inputPicker() {
        service.showInputPicker();
    }

    @Override
    void settings() {
        service.openInputSettings();
    }

    @Override
    void hide() {
        InputConnection inputConnection = service.commandInputConnection();
        service.commitCurrent(inputConnection);
        service.requestHideSelf(0);
    }

    @Override
    void remote(String command) {
        service.handleRemoteCommand(service.commandInputConnection(), command);
    }

    @Override
    void text(String value) {
        service.inputText(service.commandInputConnection(), value);
    }

    private void moveCursor(boolean right) {
        InputConnection inputConnection = service.commandInputConnection();
        service.resetDoubleSpacePeriodState();
        service.moveCursor(inputConnection, right);
    }
}
