package com.superl3.s3keyboard;

import java.util.ArrayDeque;

final class HangulCommitOnlyEditor {
    private static final int MAX_PENDING_SELECTION_DELTAS = 12;

    private int displayedComposingCodePoints;
    private final ArrayDeque<Integer> pendingOwnSelectionDeltas = new ArrayDeque<>();

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
            pendingOwnSelectionDeltas.clear();
            return false;
        }
        if (newSelStart != newSelEnd) {
            return true;
        }
        if (candidatesStart >= 0 || candidatesEnd >= 0) {
            if (newSelStart < candidatesStart || newSelStart != candidatesEnd) {
                return true;
            }
        }
        if (oldSelStart != oldSelEnd && !pendingOwnSelectionDeltas.isEmpty()) {
            pendingOwnSelectionDeltas.clear();
            return false;
        }
        if (consumeExpectedSelectionDelta(
                newSelStart - oldSelStart,
                newSelEnd - oldSelEnd)) {
            return false;
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
            pendingOwnSelectionDeltas.clear();
            return;
        }
        pendingOwnSelectionDeltas.clear();
        if (!composing.isEmpty()) {
            sink.commitText(composing);
        }
    }

    void reset() {
        displayedComposingCodePoints = 0;
        pendingOwnSelectionDeltas.clear();
    }

    private void inputHangulChar(HangulAutomata automata, char ch, Sink sink) {
        deleteDisplayedComposing(sink);
        String committed = automata.input(ch);
        if (!committed.isEmpty()) {
            commitOwnText(sink, committed);
        }
        commitDisplayedComposing(automata.getComposingText(), sink);
    }

    private void deleteDisplayedComposing(Sink sink) {
        if (displayedComposingCodePoints <= 0) {
            return;
        }
        int deletedCodePoints = displayedComposingCodePoints;
        sink.deleteBeforeCursorCodePoints(deletedCodePoints);
        displayedComposingCodePoints = 0;
        recordOwnSelectionDelta(-deletedCodePoints);
    }

    private void commitDisplayedComposing(String composing, Sink sink) {
        if (composing == null || composing.isEmpty()) {
            return;
        }
        commitOwnText(sink, composing);
        displayedComposingCodePoints = composing.codePointCount(0, composing.length());
    }

    private void commitOwnText(Sink sink, String text) {
        sink.commitText(text);
        recordOwnSelectionDelta(text.length());
    }

    private void recordOwnSelectionDelta(int delta) {
        if (delta == 0) {
            return;
        }
        while (pendingOwnSelectionDeltas.size() >= MAX_PENDING_SELECTION_DELTAS) {
            pendingOwnSelectionDeltas.removeFirst();
        }
        pendingOwnSelectionDeltas.addLast(delta);
    }

    private boolean consumeExpectedSelectionDelta(int startDelta, int endDelta) {
        if (pendingOwnSelectionDeltas.isEmpty() || startDelta != endDelta) {
            return false;
        }
        int accumulated = 0;
        int consumeCount = 0;
        for (int pendingDelta : pendingOwnSelectionDeltas) {
            accumulated += pendingDelta;
            consumeCount++;
            if (accumulated == startDelta) {
                for (int index = 0; index < consumeCount; index++) {
                    pendingOwnSelectionDeltas.removeFirst();
                }
                return true;
            }
        }
        return false;
    }
}
