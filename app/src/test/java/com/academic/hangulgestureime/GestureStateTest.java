package com.academic.hangulgestureime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GestureStateTest {
    @Test
    public void locksDirectionAfterThreshold() {
        GestureState state = new GestureState();

        assertEquals(GestureAction.RIGHT, state.update(30, 0, 28));
        assertEquals(GestureAction.RIGHT, state.update(0, -40, 28));
        assertEquals(GestureAction.RIGHT, state.release(0, -40, 28));
    }

    @Test
    public void returnsTapWhenNeverPastThreshold() {
        GestureState state = new GestureState();

        assertEquals(GestureAction.TAP, state.update(5, 4, 28));
        assertEquals(GestureAction.TAP, state.release(5, 4, 28));
    }
}
