package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OneFingerInputSpeedPresetTest {
    @Test
    public void balancedPresetMatchesTheRuntimeDefaults() {
        assertEquals(
                OneFingerInputSpeedPreset.BALANCED,
                OneFingerInputSpeedPreset.findMatching(
                        KeyboardPreferences.DEFAULT_SINGLE_TAP_START_HOLD_MS,
                        KeyboardPreferences.DEFAULT_SINGLE_TAP_COMMIT_HOLD_MS));
    }

    @Test
    public void presetSpeedsStayOrderedFromStableToFast() {
        assertTrue(OneFingerInputSpeedPreset.STABLE.actionHoldMs
                > OneFingerInputSpeedPreset.BALANCED.actionHoldMs);
        assertTrue(OneFingerInputSpeedPreset.BALANCED.actionHoldMs
                > OneFingerInputSpeedPreset.FAST.actionHoldMs);
        assertTrue(OneFingerInputSpeedPreset.STABLE.targetDwellMs
                > OneFingerInputSpeedPreset.BALANCED.targetDwellMs);
        assertTrue(OneFingerInputSpeedPreset.BALANCED.targetDwellMs
                > OneFingerInputSpeedPreset.FAST.targetDwellMs);
    }

    @Test
    public void unmatchedTimingIsReportedAsCustom() {
        assertEquals(
                OneFingerInputSpeedPreset.CUSTOM,
                OneFingerInputSpeedPreset.findMatching(333, 177));
    }
}
