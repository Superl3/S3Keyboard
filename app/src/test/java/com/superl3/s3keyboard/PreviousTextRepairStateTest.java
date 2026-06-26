package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PreviousTextRepairStateTest {
    @Test
    public void deleteSuppressesExactlyOneRepairAttempt() {
        PreviousTextRepairState state = new PreviousTextRepairState();

        assertFalse(state.consumeSuppressNextRepair());

        state.markDelete();

        assertTrue(state.consumeSuppressNextRepair());
        assertFalse(state.consumeSuppressNextRepair());
    }

    @Test
    public void resetClearsPendingSuppression() {
        PreviousTextRepairState state = new PreviousTextRepairState();

        state.markDelete();
        state.reset();

        assertFalse(state.consumeSuppressNextRepair());
    }
}
