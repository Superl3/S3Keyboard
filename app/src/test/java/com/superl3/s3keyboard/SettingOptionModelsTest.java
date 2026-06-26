package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SettingOptionModelsTest {
    @Test
    public void colorOptionsKeepBasicAndEditorScopesSeparate() {
        assertEquals(R.string.color_option_default_button, ColorOption.BASIC_OPTIONS[0].labelResId);
        assertEquals(R.string.color_option_default_key, ColorOption.EDITOR_OPTIONS[0].labelResId);
        assertTrue(ColorOption.EDITOR_OPTIONS.length > ColorOption.BASIC_OPTIONS.length);
    }

    @Test
    public void fontOptionsExposeSameValuesWithScreenSpecificLabels() {
        assertEquals(R.string.font_option_default, FontOption.BASIC_OPTIONS[0].labelResId);
        assertEquals(FontOption.BASIC_OPTIONS[0].labelResId, FontOption.EDITOR_OPTIONS[0].labelResId);
        assertEquals(KeyboardSettings.FONT_DEFAULT, FontOption.BASIC_OPTIONS[0].value);
        assertEquals(FontOption.BASIC_OPTIONS[0].value, FontOption.EDITOR_OPTIONS[0].value);
    }

    @Test
    public void remoteAndNumberRowOptionsUseStringResourceLabels() {
        assertEquals(R.string.number_row_color_full_alpha, AdditionalNumberRowColorMode.FULL_ALPHA.labelResId);
        assertEquals(R.string.number_row_color_half_mod_4567, AdditionalNumberRowColorMode.HALF_MOD_4567.labelResId);
        assertEquals(R.string.remote_key_preset_pc_keyboard, RemoteKeyPreset.PC_KEYBOARD.labelResId);
        assertEquals(R.string.remote_ime_shortcut_alt_shift, RemoteImeShortcut.ALT_SHIFT.labelResId);
        assertEquals(R.string.remote_ime_shortcut_language_switch, RemoteImeShortcut.LANGUAGE_SWITCH.labelResId);
    }
}
