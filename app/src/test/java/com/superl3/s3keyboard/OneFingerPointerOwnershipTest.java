package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OneFingerPointerOwnershipTest {
    @Test
    public void ordinaryMultiTouchIsNotSuppressedBeforeAFlowOwnsTheGesture() {
        OneFingerPointerOwnership ownership = new OneFingerPointerOwnership();

        assertFalse(ownership.suppressIfOwnedByOther(11));
        assertFalse(ownership.tryAcquire(11, 2));
        assertFalse(ownership.suppressIfOwnedByOther(12));
    }

    @Test
    public void oneFingerFlowSuppressesAndConsumesSecondaryPointers() {
        OneFingerPointerOwnership ownership = new OneFingerPointerOwnership();

        assertTrue(ownership.tryAcquire(11, 1));
        assertFalse(ownership.suppressIfOwnedByOther(11));
        assertTrue(ownership.suppressIfOwnedByOther(12));
        assertEquals(1, ownership.suppressedPointerCount());
        assertTrue(ownership.consumeSuppressedRelease(12));
        assertFalse(ownership.consumeSuppressedRelease(12));
    }

    @Test
    public void releasingOwnerClearsSuppressedPointersAndAllowsNextSession() {
        OneFingerPointerOwnership ownership = new OneFingerPointerOwnership();

        assertTrue(ownership.tryAcquire(3, 1));
        assertTrue(ownership.suppressIfOwnedByOther(4));
        ownership.releaseOwner(4);
        assertEquals(3, ownership.ownerPointerId());

        ownership.releaseOwner(3);
        assertEquals(-1, ownership.ownerPointerId());
        assertEquals(1, ownership.suppressedPointerCount());
        assertTrue(ownership.isDrainingSuppressedPointers());
        assertFalse(ownership.tryAcquire(7, 1));
        assertTrue(ownership.suppressIfOwnedByOther(7));
        assertTrue(ownership.consumeSuppressedRelease(4));
        assertTrue(ownership.consumeSuppressedRelease(7));
        assertFalse(ownership.isDrainingSuppressedPointers());
        assertTrue(ownership.tryAcquire(7, 1));
    }

    @Test
    public void resetHandlesCancelledGestures() {
        OneFingerPointerOwnership ownership = new OneFingerPointerOwnership();
        assertTrue(ownership.tryAcquire(1, 1));
        assertTrue(ownership.suppressIfOwnedByOther(2));

        ownership.reset();

        assertEquals(-1, ownership.ownerPointerId());
        assertEquals(0, ownership.suppressedPointerCount());
        assertFalse(ownership.consumeSuppressedRelease(2));
    }
}
