package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OneFingerInputSessionTest {
    @Test
    public void initialSelectionUsesTheActualPointerAsGestureOrigin() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();

        session.selectKey("mieum", 42f, 81f);

        assertEquals(OneFingerInputSession.State.KEY_SELECTED, session.state);
        assertEquals(42f, session.downX, 0.001f);
        assertEquals(81f, session.downY, 0.001f);
        assertEquals(42f, session.currentX, 0.001f);
        assertEquals(81f, session.currentY, 0.001f);
    }

    @Test
    public void hoveredCandidateSelectionKeepsTheDwellPositionAsGestureOrigin() {
        OneFingerInputSession<String> session = committedSession("giyeok");
        session.allowLastCommittedReentry();
        session.hoverCandidate("nieun", 120f, 210f);
        session.updatePointer(124f, 214f);

        session.selectHoveredCandidate("nieun");

        assertEquals(OneFingerInputSession.State.KEY_SELECTED, session.state);
        assertEquals(124f, session.downX, 0.001f);
        assertEquals(214f, session.downY, 0.001f);
        assertEquals("nieun", session.targetSlot);
    }

    @Test
    public void committedKeyCannotRearmUntilTheFingerLeavesItsCenterZone() {
        OneFingerInputSession<String> session = committedSession("giyeok");

        assertTrue(session.blocksCandidate("giyeok"));
        assertFalse(session.blocksCandidate("nieun"));

        session.allowLastCommittedReentry();

        assertFalse(session.blocksCandidate("giyeok"));
    }

    @Test
    public void commitReturnsToFreeRoamWithoutKeepingASelectedTarget() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        session.selectKey("ieung", 10f, 20f);
        session.markSlideLocked();

        session.markCommitted("ieung");

        assertEquals(OneFingerInputSession.State.COMMITTED_FREE_ROAM, session.state);
        assertNull(session.targetSlot);
        assertNull(session.candidateSlot);
        assertTrue(session.hasCommitted);
    }

    @Test
    public void postCommitAnchorUsesTheActualPointerPositionAtCommitTimeNotTheKeyCenter() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        session.selectKey("giyeok", 10f, 20f);
        session.updatePointer(40f, 20f);

        session.markCommitted("giyeok");

        assertFalse(session.hasMovedBeyondPostCommitAnchor(42f, 21f, 6f));
        assertTrue(session.hasMovedBeyondPostCommitAnchor(50f, 20f, 6f));
    }

    @Test
    public void slideCommitIsNotReadyUntilTheArmDelayElapsesAfterCandidateSelection() {
        OneFingerInputSession<String> session = committedSession("giyeok");
        session.allowLastCommittedReentry();
        session.hoverCandidate("nieun", 120f, 210f);

        session.selectHoveredCandidate("nieun", 1_000L, 110L);

        assertFalse(session.slideCommitReady(1_050L));
        assertTrue(session.slideCommitReady(1_110L));
        assertTrue(session.slideCommitReady(1_200L));
    }

    @Test
    public void slideCommitIsAlwaysReadyForTheFirstKeyOfASession() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();

        session.selectKey("mieum", 42f, 81f);

        assertTrue(session.slideCommitReady(1_000L));
    }

    @Test
    public void pendingPhaseReportsProgressAndRemainingTime() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();

        session.beginPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                1_000L,
                200L);

        assertTrue(session.hasPending());
        assertEquals(0.5f, session.pendingProgress(1_100L), 0.001f);
        assertEquals(100L, session.pendingRemainingMs(1_100L));
        assertEquals("TARGET_SELECTION:100ms", session.debugPending(1_100L));
    }

    @Test
    public void stateTransitionClearsObsoletePendingTimer() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        session.hoverCandidate("nieun", 10f, 20f);
        session.beginPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                100L,
                140L);

        session.selectHoveredCandidate("nieun");

        assertFalse(session.hasPending());
        assertEquals(OneFingerInputSession.PendingPhase.NONE, session.pendingPhase());
    }

    @Test
    public void smallCandidateTremorKeepsTheCurrentDwellWindow() {
        OneFingerInputSession<String> session = committedSession("giyeok");
        session.hoverCandidate("nieun", 100f, 100f);

        assertFalse(session.candidateDriftedBeyond(103f, 104f, 6f));
        assertFalse(session.candidateDriftedBeyond(104f, 104f, 6f));
    }

    @Test
    public void traversingACandidateRestartsDwellFromTheLatestStablePoint() {
        OneFingerInputSession<String> session = committedSession("giyeok");
        session.hoverCandidate("nieun", 100f, 100f);

        assertTrue(session.candidateDriftedBeyond(107f, 100f, 6f));
        assertFalse(session.candidateDriftedBeyond(111f, 100f, 6f));
        assertTrue(session.candidateDriftedBeyond(114f, 100f, 6f));
    }

    @Test
    public void candidateStabilityTrackingStopsAfterSelection() {
        OneFingerInputSession<String> session = committedSession("giyeok");
        session.hoverCandidate("nieun", 100f, 100f);
        session.selectHoveredCandidate("nieun");

        assertFalse(session.candidateDriftedBeyond(120f, 100f, 6f));
    }

    @Test
    public void phaseSpecificCancelDoesNotClearAnotherTimer() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        session.beginPending(
                OneFingerInputSession.PendingPhase.ACTION_COMMIT,
                100L,
                300L);

        session.clearPending(OneFingerInputSession.PendingPhase.TARGET_SELECTION);

        assertEquals(OneFingerInputSession.PendingPhase.ACTION_COMMIT, session.pendingPhase());
    }

    @Test
    public void replacingATimerInvalidatesThePreviousCallbackGeneration() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        long first = session.beginPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                100L,
                140L);
        long second = session.beginPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                120L,
                140L);

        assertFalse(session.isCurrentPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                first));
        assertTrue(session.isCurrentPending(
                OneFingerInputSession.PendingPhase.TARGET_SELECTION,
                second));
    }

    @Test
    public void clearingATimerInvalidatesItsCallbackGeneration() {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        long generation = session.beginPending(
                OneFingerInputSession.PendingPhase.ACTION_COMMIT,
                100L,
                300L);

        session.clearPending();

        assertFalse(session.isCurrentPending(
                OneFingerInputSession.PendingPhase.ACTION_COMMIT,
                generation));
    }

    private static OneFingerInputSession<String> committedSession(String key) {
        OneFingerInputSession<String> session = new OneFingerInputSession<>();
        session.selectKey(key, 1f, 2f);
        session.markCommitted(key);
        return session;
    }
}
