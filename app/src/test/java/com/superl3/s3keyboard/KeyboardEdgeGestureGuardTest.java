package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardEdgeGestureGuardTest {
    @Test
    public void edgeTouchesAreIgnoredForSystemGestureOverlays() {
        assertTrue(KeyboardEdgeGestureGuard.shouldIgnoreTouch(5f, 360f, 1f));
        assertTrue(KeyboardEdgeGestureGuard.shouldIgnoreTouch(355f, 360f, 1f));
        assertFalse(KeyboardEdgeGestureGuard.shouldIgnoreTouch(40f, 360f, 1f));
    }

    @Test
    public void narrowSurfacesKeepUsableCenterArea() {
        assertTrue(KeyboardEdgeGestureGuard.shouldIgnoreTouch(5f, 45f, 3f));
        assertFalse(KeyboardEdgeGestureGuard.shouldIgnoreTouch(22.5f, 45f, 3f));
    }
}
