package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ExtremeFloatingKeycapGeometryTest {
    @Test
    public void standardKeycapKeepsACompactRectangularFace() {
        float width = ExtremeFloatingKeycapGeometry.width(100f, 34f, false);
        float height = ExtremeFloatingKeycapGeometry.height(60f, 34f);

        assertEquals(70f, width, 0.001f);
        assertEquals(43.2f, height, 0.001f);
        assertTrue(width < 100f);
        assertTrue(height < 60f);
    }

    @Test
    public void narrowFunctionRailKeepsMoreCoverageForLegibility() {
        float standard = ExtremeFloatingKeycapGeometry.width(50f, 34f, false);
        float compactRail = ExtremeFloatingKeycapGeometry.width(50f, 34f, true);

        assertEquals(35f, standard, 0.001f);
        assertEquals(41f, compactRail, 0.001f);
        assertTrue(compactRail > standard);
    }

    @Test
    public void minimumSizeNeverPushesTheFaceOutsideItsKey() {
        assertEquals(24f, ExtremeFloatingKeycapGeometry.width(24f, 34f, false), 0.001f);
        assertEquals(28f, ExtremeFloatingKeycapGeometry.height(28f, 34f), 0.001f);
    }
}
