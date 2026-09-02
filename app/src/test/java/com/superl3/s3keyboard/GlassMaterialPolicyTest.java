package com.superl3.s3keyboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class GlassMaterialPolicyTest {
    @Test
    public void frostedPanelTintRemainsLightweightAndThemeDominant() {
        int panelAlpha = GlassMaterialPolicy.panelTintAlpha(54);
        assertTrue(panelAlpha >= 200);
        assertTrue(panelAlpha < 255);
    }

    @Test
    public void glassBordersStayLowContrast() {
        assertTrue(GlassMaterialPolicy.keyBorderAlpha(100) <= 58);
        assertTrue(GlassMaterialPolicy.panelBorderAlpha(100) <= 42);
        assertTrue(GlassMaterialPolicy.keyBorderAlpha(62) > 0);
    }
}
