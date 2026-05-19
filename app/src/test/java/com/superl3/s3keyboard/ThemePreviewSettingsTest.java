package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public final class ThemePreviewSettingsTest {
    @Test
    public void previewSettingsDisableHintsAndNumberRows() {
        ThemeOption option = ThemeOption.buildOptions(null, false)[0];
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withHintVisibility(true, true, true)
                .withHangulNumberRow(true)
                .withEnglishNumberRow(true);

        KeyboardSettings preview = ThemePreviewSettings.forOption(
                option,
                settings,
                KeyboardMode.ENGLISH);

        assertEquals(KeyboardMode.ENGLISH, preview.keyboardMode);
        assertFalse(preview.showHangulSlideHints);
        assertFalse(preview.showEnglishSlideHints);
        assertFalse(preview.showBeginnerTooltipPreview);
        assertFalse(preview.showHangulNumberRow);
        assertFalse(preview.showEnglishNumberRow);
    }
}
