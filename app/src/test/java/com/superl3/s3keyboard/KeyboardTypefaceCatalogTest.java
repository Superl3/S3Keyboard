package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class KeyboardTypefaceCatalogTest {
    @Test
    public void bundledFontAssetsMatchThemeFontOptions() {
        assertNull(KeyboardTypefaceCatalog.assetPathFor(KeyboardSettings.FONT_DEFAULT));
        assertEquals(
                KeyboardTypefaceCatalog.ASSET_NOTO_SANS_KR,
                KeyboardTypefaceCatalog.assetPathFor(KeyboardSettings.FONT_NOTO_SANS_KR));
        assertEquals(
                KeyboardTypefaceCatalog.ASSET_NOTO_SERIF_KR,
                KeyboardTypefaceCatalog.assetPathFor(KeyboardSettings.FONT_NOTO_SERIF_KR));
        assertEquals(
                KeyboardTypefaceCatalog.ASSET_D2CODING,
                KeyboardTypefaceCatalog.assetPathFor(KeyboardSettings.FONT_D2CODING));
    }
}
