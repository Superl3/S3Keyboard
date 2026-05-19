package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DoubleSpacePeriodStateTest {
    @Test
    public void englishDoubleSpaceProducesPeriodSpace() {
        DoubleSpacePeriodState state = new DoubleSpacePeriodState();

        assertEquals(
                DoubleSpacePeriodState.SpaceResult.COMMIT_SPACE,
                state.onSpace(KeyboardMode.ENGLISH, true, 1000));
        assertEquals(
                DoubleSpacePeriodState.SpaceResult.REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE,
                state.onSpace(KeyboardMode.ENGLISH, true, 1500));
    }

    @Test
    public void hangulDoubleSpaceStaysAsSpaces() {
        DoubleSpacePeriodState state = new DoubleSpacePeriodState();

        assertEquals(
                DoubleSpacePeriodState.SpaceResult.COMMIT_SPACE,
                state.onSpace(KeyboardMode.HANGUL, true, 1000));
        assertEquals(
                DoubleSpacePeriodState.SpaceResult.COMMIT_SPACE,
                state.onSpace(KeyboardMode.HANGUL, true, 1500));
    }

    @Test
    public void disabledDoubleSpaceStaysAsSpaces() {
        DoubleSpacePeriodState state = new DoubleSpacePeriodState();

        state.onSpace(KeyboardMode.ENGLISH, false, 1000);
        assertEquals(
                DoubleSpacePeriodState.SpaceResult.COMMIT_SPACE,
                state.onSpace(KeyboardMode.ENGLISH, false, 1200));
    }
}
