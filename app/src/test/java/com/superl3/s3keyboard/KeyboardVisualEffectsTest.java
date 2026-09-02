package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class KeyboardVisualEffectsTest {
    @Test
    public void onlySculptedSoftKeycapRequiresPedestal() {
        assertTrue(KeyboardVisualEffects.DEFAULT
                .withMaterialStyle(KeyboardVisualEffects.MATERIAL_SOFT_KEYCAP)
                .requiresPedestal());
        assertFalse(KeyboardVisualEffects.DEFAULT
                .withMaterialStyle(KeyboardVisualEffects.MATERIAL_FROSTED)
                .requiresPedestal());
        assertFalse(KeyboardVisualEffects.DEFAULT
                .withMaterialStyle(KeyboardVisualEffects.MATERIAL_ACRYLIC)
                .requiresPedestal());
    }

    @Test
    public void solidMaterialKeepsLegacyFlatOption() {
        assertFalse(KeyboardVisualEffects.DEFAULT
                .withMaterialStyle(KeyboardVisualEffects.MATERIAL_SOLID)
                .requiresPedestal());
    }
}
