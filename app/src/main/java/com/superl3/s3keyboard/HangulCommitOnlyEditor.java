package com.superl3.s3keyboard;

final class HangulCommitOnlyEditor {
    private int displayedComposingCodePoints;
    private int pendingOwnSelectionUpdates;

    interface Sink {
        void deleteBeforeCursorCodePoints(int count);
        void commitText(String text);
    }

    boolean hasDisplayedComposing() {
        return displayedComposingCodePoints > 0;
    }

    boolean shouldAcceptExternalSelectionChange(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        if (!hasDisplayedComposing()) {
            pendingOwnSelectionUpdates = 0;
            return false;
        }
        if (pendingOwnSelectionUpdates > 0) {
            pendingOwnSelectionUpdates--;
            return false;
        }
        if (newSelStart != newSelEnd) {
            return true;
        }
        if (candidatesStart >= 0 || candidatesEnd >= 0) {
            return newSelStart < candidatesStart || newSelStart != candidatesEnd;
        }
        return oldSelStart != newSelStart || oldSelEnd != newSelEnd;
    }

    void acceptDisplayedComposition(HangulAutomata automata) {
        if (automata != null) {
            automata.flush();
        }
        reset();
    }

    void input(HangulAutomata automata, String text, Sink sink) {
        if (automata == null || text == null || text.isEmpty() || sink == null) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (HangulAutomata.isInitialConsonant(ch) || HangulAutomata.isVowel(ch)) {
                inputHangulChar(automata, ch, sink);
            } else {
                finish(automata, sink);
                sink.commitText(String.valueOf(ch));
            }
        }
    }

    void refreshDisplayedComposing(HangulAutomata automata, Sink sink) {
        if (automata == null || sink == null) {
            return;
        }
        deleteDisplayedComposing(sink);
        commitDisplayedComposing(automata.getComposingText(), sink);
    }

    boolean backspace(HangulAutomata automata, Sink sink) {
        if (automata == null || sink == null || !automata.backspace()) {
            return false;
        }
        refreshDisplayedComposing(automata, sink);
        return true;
    }

    void finish(HangulAutomata automata, Sink sink) {
        if (automata == null || sink == null) {
            reset();
            return;
        }
        String composing = automata.flush();
        if (displayedComposingCodePoints > 0) {
            displayedComposingCodePoints = 0;
            return;
        }
        if (!composing.isEmpty()) {
            sink.commitText(composing);
        }
    }

    void reset() {
        displayedComposingCodePoints = 0;
        pendingOwnSelectionUpdates = 0;
    }

    private void inputHangulChar(HangulAutomata automata, char ch, Sink sink) {
        deleteDisplayedComposing(sink);
        String committed = automata.input(ch);
        if (!committed.isEmpty()) {
            sink.commitText(committed);
        }
        commitDisplayedComposing(automata.getComposingText(), sink);
    }

    private void deleteDisplayedComposing(Sink sink) {
        if (displayedComposingCodePoints <= 0) {
            return;
        }
        sink.deleteBeforeCursorCodePoints(displayedComposingCodePoints);
        displayedComposingCodePoints = 0;
        pendingOwnSelectionUpdates++;
    }

    private void commitDisplayedComposing(String composing, Sink sink) {
        if (composing == null || composing.isEmpty()) {
            return;
        }
        sink.commitText(composing);
        displayedComposingCodePoints = composing.codePointCount(0, composing.length());
        pendingOwnSelectionUpdates++;
    }
}
