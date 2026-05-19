package com.superl3.s3keyboard;

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

    @Test
    public void directionalThresholdCanOnlyDelayOneDirection() {
        GestureState state = new GestureState();

        assertEquals(
                GestureAction.TAP,
                state.update(30, 0, 20, 20, 20, 20, 50));
        assertEquals(
                GestureAction.RIGHT,
                state.release(36, 0, 20, 20, 20, 20, 50));
    }

    @Test
    public void horizontalSlidesLockBeforeFullBaseDistance() {
        GestureState state = new GestureState();

        assertEquals(GestureAction.RIGHT, state.update(20, 7, 28));
    }

    @Test
    public void verticalSlidesAlsoLockBeforeFullBaseDistance() {
        GestureState state = new GestureState();

        assertEquals(GestureAction.DOWN, state.update(7, 22, 28));
    }
}
