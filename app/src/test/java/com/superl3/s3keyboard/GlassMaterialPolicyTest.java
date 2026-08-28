package com.superl3.s3keyboard;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class GlassMaterialPolicyTest {
    @Test
    public void defaultMaterialKeepsThemeDominantAndRefractionAtTheEdge() {
        float centerMix = GlassMaterialPolicy.keyCenterSourceMix(54);
        float edgeMix = GlassMaterialPolicy.keyEdgeSourceMix(54);

        assertEquals(0.097f, centerMix, 0.001f);
        assertEquals(0.251f, edgeMix, 0.001f);
        assertTrue(centerMix < edgeMix);
        assertTrue(centerMix <= 0.14f);
        assertTrue(edgeMix >= 0.20f && edgeMix <= 0.31f);
    }

    @Test
    public void fallbackKeepsKeyColorMoreOpaqueThanPanel() {
        int panelAlpha = GlassMaterialPolicy.panelTintAlpha(54);
        int keyAlpha = GlassMaterialPolicy.fallbackKeyTintAlpha(54, 0f);

        assertTrue(panelAlpha >= 200);
        assertTrue(keyAlpha >= 225);
        assertTrue(keyAlpha > panelAlpha);
    }

    @Test
    public void glassBordersStayLowContrast() {
        assertTrue(GlassMaterialPolicy.keyBorderAlpha(100) <= 58);
        assertTrue(GlassMaterialPolicy.panelBorderAlpha(100) <= 42);
        assertTrue(GlassMaterialPolicy.keyBorderAlpha(62) > 0);
    }
}
