package com.superl3.s3keyboard;

import java.util.HashSet;
import java.util.Set;

/** Keeps a one-finger flow isolated without disabling ordinary multi-touch typing. */
final class OneFingerPointerOwnership {
    private final Set<Integer> suppressedPointerIds = new HashSet<>();
    private int ownerPointerId = -1;

    boolean suppressIfOwnedByOther(int pointerId) {
        if ((ownerPointerId == -1 && suppressedPointerIds.isEmpty())
                || pointerId == ownerPointerId) {
            return false;
        }
        suppressedPointerIds.add(pointerId);
        return true;
    }

    boolean tryAcquire(int pointerId, int activeTouchCount) {
        if (ownerPointerId != -1
                || !suppressedPointerIds.isEmpty()
                || activeTouchCount != 1) {
            return false;
        }
        ownerPointerId = pointerId;
        return true;
    }

    boolean consumeSuppressedRelease(int pointerId) {
        return suppressedPointerIds.remove(pointerId);
    }

    void releaseOwner(int pointerId) {
        if (pointerId != ownerPointerId) {
            return;
        }
        ownerPointerId = -1;
    }

    void reset() {
        ownerPointerId = -1;
        suppressedPointerIds.clear();
    }

    int ownerPointerId() {
        return ownerPointerId;
    }

    int suppressedPointerCount() {
        return suppressedPointerIds.size();
    }

    boolean isDrainingSuppressedPointers() {
        return ownerPointerId == -1 && !suppressedPointerIds.isEmpty();
    }
}
