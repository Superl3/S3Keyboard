package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class EnglishShiftStateTest {
    @Test
    public void shiftTapUppercasesOneLetterAndResets() {
        EnglishShiftState state = new EnglishShiftState();

        state.onShiftOnceCommand();

        assertTrue(state.isActive());
        assertEquals("A", state.applyToInput("a"));
        assertFalse(state.isActive());
        assertEquals("b", state.applyToInput("b"));
    }

    @Test
    public void shiftLongPressTogglesCapsLock() {
        EnglishShiftState state = new EnglishShiftState();

        state.onShiftLockCommand();

        assertTrue(state.isActive());
        assertTrue(state.isLocked());
        assertEquals("A", state.applyToInput("a"));
        assertEquals("B", state.applyToInput("b"));

        state.onShiftLockCommand();

        assertFalse(state.isActive());
        assertFalse(state.isLocked());
    }

    @Test
    public void shiftTapClearsCapsLock() {
        EnglishShiftState state = new EnglishShiftState();

        state.onShiftLockCommand();
        state.onShiftOnceCommand();

        assertFalse(state.isActive());
        assertFalse(state.isLocked());
    }

    @Test
    public void symbolsDoNotConsumeOneShotShift() {
        EnglishShiftState state = new EnglishShiftState();

        state.onShiftOnceCommand();

        assertEquals("-", state.applyToInput("-"));
        assertTrue(state.isActive());
        assertEquals("A", state.applyToInput("a"));
        assertFalse(state.isActive());
    }

    @Test
    public void resetClearsShiftState() {
        EnglishShiftState state = new EnglishShiftState();

        state.onShiftLockCommand();
        state.reset();

        assertFalse(state.isActive());
        assertFalse(state.isLocked());
    }
}
