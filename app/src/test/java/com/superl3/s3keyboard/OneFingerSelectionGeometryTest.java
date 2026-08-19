package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OneFingerSelectionGeometryTest {
    @Test
    public void selectionUsesASmallerEntryRadiusThanExitRadius() {
        assertFalse(OneFingerSelectionGeometry.contains(
                50f,
                50f,
                100f,
                100f,
                18f,
                90f,
                50f,
                false));
        assertTrue(OneFingerSelectionGeometry.contains(
                50f,
                50f,
                100f,
                100f,
                18f,
                90f,
                50f,
                true));
    }

    @Test
    public void exitRadiusStillRejectsMovementTowardAnotherKey() {
        assertFalse(OneFingerSelectionGeometry.contains(
                50f,
                50f,
                100f,
                100f,
                18f,
                96f,
                50f,
                true));
    }

    @Test
    public void compactKeysKeepTheMinimumSelectionRadius() {
        assertTrue(OneFingerSelectionGeometry.contains(
                20f,
                20f,
                30f,
                50f,
                18f,
                37f,
                20f,
                false));
    }

    @Test
    public void cosmeticFaceShrinkDoesNotNeedToShrinkTheStableTouchSelectionArea() {
        assertFalse(OneFingerSelectionGeometry.contains(
                50f,
                50f,
                40f,
                40f,
                0f,
                70f,
                50f,
                false));
        assertTrue(OneFingerSelectionGeometry.contains(
                50f,
                50f,
                64f,
                64f,
                0f,
                70f,
                50f,
                false));
    }

    @Test
    public void wideKeysUseTheirHorizontalTouchAreaWithoutGrowingVertically() {
        assertTrue(OneFingerSelectionGeometry.contains(
                80f,
                50f,
                120f,
                60f,
                18f,
                120f,
                50f,
                false));
        assertFalse(OneFingerSelectionGeometry.contains(
                80f,
                50f,
                120f,
                60f,
                18f,
                80f,
                78f,
                false));
    }

    @Test
    public void drawnSelectionRadiiMatchTheHitTestGeometry() {
        float radiusX = OneFingerSelectionGeometry.radiusX(120f, 18f, false);
        float radiusY = OneFingerSelectionGeometry.radiusY(60f, 18f, false);

        assertTrue(radiusX > radiusY);
        assertTrue(OneFingerSelectionGeometry.contains(
                0f,
                0f,
                120f,
                60f,
                18f,
                radiusX * 0.95f,
                0f,
                false));
        assertFalse(OneFingerSelectionGeometry.contains(
                0f,
                0f,
                120f,
                60f,
                18f,
                radiusX * 1.05f,
                0f,
                false));
    }
}
