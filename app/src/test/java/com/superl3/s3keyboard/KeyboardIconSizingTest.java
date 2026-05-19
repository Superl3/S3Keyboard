package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class KeyboardIconSizingTest {
    @Test
    public void keyIconSizeIsFixedDpNotDerivedFromKeyBounds() {
        assertEquals(20f, KeyboardIconSizing.keyIconSizePx(1f), 0.001f);
        assertEquals(60f, KeyboardIconSizing.keyIconSizePx(3f), 0.001f);
    }

    @Test
    public void hintAndOverlayIconSizesAreAlsoFixedDp() {
        assertEquals(14f, KeyboardIconSizing.hintIconSizePx(1f), 0.001f);
        assertEquals(20f, KeyboardIconSizing.overlayIconSizePx(1f), 0.001f);
    }
}
