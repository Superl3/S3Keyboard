package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SettingOptionModelsTest {
    @Test
    public void colorOptionsKeepBasicAndEditorScopesSeparate() {
        assertEquals("기본 버튼", ColorOption.BASIC_OPTIONS[0].label);
        assertEquals("기본 키", ColorOption.EDITOR_OPTIONS[0].label);
        assertTrue(ColorOption.EDITOR_OPTIONS.length > ColorOption.BASIC_OPTIONS.length);
    }

    @Test
    public void fontOptionsExposeSameValuesWithScreenSpecificLabels() {
        assertEquals("기본", FontOption.BASIC_OPTIONS[0].label);
        assertEquals(FontOption.BASIC_OPTIONS[0].label, FontOption.EDITOR_OPTIONS[0].label);
        assertEquals(KeyboardSettings.FONT_DEFAULT, FontOption.BASIC_OPTIONS[0].value);
        assertEquals(FontOption.BASIC_OPTIONS[0].value, FontOption.EDITOR_OPTIONS[0].value);
    }
}
