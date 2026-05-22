package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class ThemePromptTemplatesTest {
    @Test
    public void keyboardImagePromptUsesKeycapMapping() {
        String prompt = ThemePromptTemplates.keyboardImagePrompt("{\"schemaVersion\":1}");

        assertTrue(prompt.contains("keyboard or keycap image"));
        assertTrue(prompt.contains("alphaKey"));
        assertTrue(prompt.contains("accentPolicy"));
        assertTrue(prompt.contains("effects.panelGradient"));
        assertTrue(prompt.contains("\"schemaVersion\":1"));
        assertFalse(prompt.contains("```"));
    }

    @Test
    public void paletteImagePromptUsesGeneralImageMapping() {
        String prompt = ThemePromptTemplates.paletteImagePrompt("{\"schemaVersion\":1}");

        assertTrue(prompt.contains("drawing, illustration, photo"));
        assertTrue(prompt.contains("Extract 4 to 7 dominant colors"));
        assertTrue(prompt.contains("do not simulate the image texture"));
        assertTrue(prompt.contains("backplate gradient"));
        assertTrue(prompt.contains("\"schemaVersion\":1"));
        assertFalse(prompt.contains("```"));
    }
}
