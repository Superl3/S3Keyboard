package com.superl3.s3keyboard;

final class OneFingerInputSession<K> {
    enum State {
        IDLE,
        KEY_SELECTED,
        ACTION_LOCKED,
        COMMITTED_FREE_ROAM,
        CANDIDATE_HOVER
    }

    enum PendingPhase {
        NONE,
        TARGET_SELECTION,
        ACTION_COMMIT
    }

    State state = State.IDLE;
    K targetSlot;
    K candidateSlot;
    K holdCommitSlot;
    K lastCommittedSlot;
    final GestureState gestureState = new GestureState();
    float downX;
    float downY;
    float currentX;
    float currentY;
    boolean hasCommitted;
    private float postCommitAnchorX;
    private float postCommitAnchorY;
    private boolean lastCommittedReentryBlocked;
    private PendingPhase pendingPhase = PendingPhase.NONE;
    private long pendingStartTimeMs;
    private long pendingDeadlineTimeMs;
    private long pendingGeneration;
    private float candidateAnchorX;
    private float candidateAnchorY;
    private boolean hasCandidateAnchor;
    private boolean slideArmPending;
    private long slideArmDeadlineMs;

    boolean active() {
        return state != State.IDLE;
    }

    boolean keySelected() {
        return state == State.KEY_SELECTED || state == State.ACTION_LOCKED;
    }

    void updatePointer(float x, float y) {
        currentX = x;
        currentY = y;
    }

    void selectKey(K keySlot, float x, float y) {
        clearPending();
        clearCandidateAnchor();
        state = State.KEY_SELECTED;
        targetSlot = keySlot;
        candidateSlot = null;
        holdCommitSlot = null;
        downX = x;
        downY = y;
        updatePointer(x, y);
        gestureState.reset();
        slideArmPending = false;
    }

    void hoverCandidate(K keySlot, float x, float y) {
        clearPending();
        state = State.CANDIDATE_HOVER;
        candidateSlot = keySlot;
        targetSlot = keySlot;
        holdCommitSlot = null;
        updatePointer(x, y);
        anchorCandidate(x, y);
    }

    void selectHoveredCandidate(K keySlot) {
        selectKey(keySlot, currentX, currentY);
    }

    void selectHoveredCandidate(K keySlot, long nowMs, long armDelayMs) {
        selectKey(keySlot, currentX, currentY);
        slideArmPending = true;
        slideArmDeadlineMs = nowMs + Math.max(0L, armDelayMs);
    }

    boolean slideCommitReady(long nowMs) {
        return !slideArmPending || nowMs >= slideArmDeadlineMs;
    }

    void markCommitted(K keySlot) {
        clearPending();
        clearCandidateAnchor();
        state = State.COMMITTED_FREE_ROAM;
        targetSlot = null;
        candidateSlot = null;
        lastCommittedSlot = keySlot;
        holdCommitSlot = null;
        hasCommitted = true;
        lastCommittedReentryBlocked = keySlot != null;
        postCommitAnchorX = currentX;
        postCommitAnchorY = currentY;
        gestureState.reset();
    }

    void markSlideLocked() {
        state = State.ACTION_LOCKED;
    }

    void enterFreeRoam() {
        if (state == State.IDLE) {
            return;
        }
        clearPending();
        clearCandidateAnchor();
        targetSlot = null;
        candidateSlot = null;
        holdCommitSlot = null;
        gestureState.reset();
        state = State.COMMITTED_FREE_ROAM;
    }

    void clearCandidate() {
        if (state == State.CANDIDATE_HOVER) {
            clearPending();
            clearCandidateAnchor();
            targetSlot = null;
            candidateSlot = null;
            holdCommitSlot = null;
            state = State.COMMITTED_FREE_ROAM;
        }
    }

    boolean candidateDriftedBeyond(float x, float y, float settleSlop) {
        if (state != State.CANDIDATE_HOVER || candidateSlot == null) {
            return false;
        }
        if (!hasCandidateAnchor) {
            anchorCandidate(x, y);
            return false;
        }
        float dx = x - candidateAnchorX;
        float dy = y - candidateAnchorY;
        float safeSlop = Math.max(0f, settleSlop);
        if (dx * dx + dy * dy <= safeSlop * safeSlop) {
            return false;
        }
        anchorCandidate(x, y);
        return true;
    }

    private void anchorCandidate(float x, float y) {
        candidateAnchorX = x;
        candidateAnchorY = y;
        hasCandidateAnchor = true;
    }

    private void clearCandidateAnchor() {
        candidateAnchorX = 0f;
        candidateAnchorY = 0f;
        hasCandidateAnchor = false;
    }

    boolean blocksCandidate(K keySlot) {
        return lastCommittedReentryBlocked && keySlot == lastCommittedSlot;
    }

    void allowLastCommittedReentry() {
        lastCommittedReentryBlocked = false;
    }

    boolean lastCommittedReentryBlocked() {
        return lastCommittedReentryBlocked;
    }

    boolean hasMovedBeyondPostCommitAnchor(float x, float y, float slop) {
        float dx = x - postCommitAnchorX;
        float dy = y - postCommitAnchorY;
        float safeSlop = Math.max(0f, slop);
        return dx * dx + dy * dy > safeSlop * safeSlop;
    }

    long beginPending(PendingPhase phase, long nowMs, long durationMs) {
        pendingGeneration++;
        pendingPhase = phase == null ? PendingPhase.NONE : phase;
        if (pendingPhase == PendingPhase.NONE) {
            pendingStartTimeMs = 0L;
            pendingDeadlineTimeMs = 0L;
            return pendingGeneration;
        }
        pendingStartTimeMs = Math.max(0L, nowMs);
        pendingDeadlineTimeMs = pendingStartTimeMs + Math.max(1L, durationMs);
        return pendingGeneration;
    }

    void clearPending(PendingPhase phase) {
        if (phase == null || pendingPhase == phase) {
            clearPending();
        }
    }

    void clearPending() {
        pendingGeneration++;
        pendingPhase = PendingPhase.NONE;
        pendingStartTimeMs = 0L;
        pendingDeadlineTimeMs = 0L;
    }

    PendingPhase pendingPhase() {
        return pendingPhase;
    }

    boolean isCurrentPending(PendingPhase phase, long generation) {
        return phase != null
                && phase != PendingPhase.NONE
                && pendingPhase == phase
                && pendingGeneration == generation;
    }

    boolean hasPending() {
        return pendingPhase != PendingPhase.NONE;
    }

    long pendingRemainingMs(long nowMs) {
        if (!hasPending()) {
            return 0L;
        }
        return Math.max(0L, pendingDeadlineTimeMs - nowMs);
    }

    float pendingProgress(long nowMs) {
        if (!hasPending()) {
            return 0f;
        }
        long duration = Math.max(1L, pendingDeadlineTimeMs - pendingStartTimeMs);
        float progress = (nowMs - pendingStartTimeMs) / (float) duration;
        return Math.max(0f, Math.min(1f, progress));
    }

    String debugPending(long nowMs) {
        if (!hasPending()) {
            return "-";
        }
        return pendingPhase.name() + ":" + pendingRemainingMs(nowMs) + "ms";
    }

    String debugState() {
        return state.name();
    }
}
