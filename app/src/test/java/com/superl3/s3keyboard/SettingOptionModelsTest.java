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
        assertEquals(0, ColorOption.basicIndexOf(ColorOption.BASIC_OPTIONS[0].color));
        assertEquals(0, ColorOption.editorIndexOf(ColorOption.EDITOR_OPTIONS[0].color));
    }

    @Test
    public void fontOptionsExposeSameValuesWithScreenSpecificLabels() {
        assertEquals(R.string.font_option_default, FontOption.BASIC_OPTIONS[0].labelResId);
        assertEquals(FontOption.BASIC_OPTIONS[0].labelResId, FontOption.EDITOR_OPTIONS[0].labelResId);
        assertEquals(KeyboardSettings.FONT_DEFAULT, FontOption.BASIC_OPTIONS[0].value);
        assertEquals(FontOption.BASIC_OPTIONS[0].value, FontOption.EDITOR_OPTIONS[0].value);
        assertEquals(0, FontOption.basicIndexOf(FontOption.BASIC_OPTIONS[0].value));
        assertEquals(0, FontOption.editorIndexOf(FontOption.EDITOR_OPTIONS[0].value));
    }

    @Test
    public void themePackModelsOwnPositionSelection() {
        assertEquals(
                ModifierIconCatalog.PACK_THEME_DEFAULT,
                ModifierIconCatalog.selectablePackIdAt(0, true));
        assertEquals(
                ModifierIconCatalog.PACK_LINE_MONO,
                ModifierIconCatalog.selectablePackIdAt(-1, false));
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_THEME_DEFAULT,
                KeyDisplayOverridePackCatalog.selectablePackIdAt(0, true));
        assertEquals(
                KeyDisplayOverridePackCatalog.PACK_NONE,
                KeyDisplayOverridePackCatalog.selectablePackIdAt(99, false));
        assertEquals(
                KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_SOFT,
                KeyboardVisualEffects.keyFaceGradientCurveAt(-1));
        assertEquals(
                KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_GLASS,
                KeyboardVisualEffects.normalizeKeyFaceGradientCurve("glass"));
        KeyboardVisualEffects blurred = KeyboardVisualEffects.DEFAULT.withBlur(true, 12);
        assertTrue(blurred.blurEnabled);
        assertEquals(12, blurred.blurRadiusDp);
        assertEquals(KeyboardVisualEffects.MATERIAL_FROSTED, blurred.materialStyle);
        assertTrue(blurred.usesPlatformBlur());
    }

    @Test
    public void materialPresetsKeepLiveRefractionExplicit() {
        KeyboardVisualEffects solid = KeyboardVisualEffects.DEFAULT.withMaterialPreset(
                KeyboardVisualEffects.MATERIAL_SOLID);
        KeyboardVisualEffects frosted = KeyboardVisualEffects.DEFAULT.withMaterialPreset(
                KeyboardVisualEffects.MATERIAL_FROSTED);
        KeyboardVisualEffects experimental = KeyboardVisualEffects.DEFAULT.withMaterialPreset(
                KeyboardVisualEffects.MATERIAL_EXPERIMENTAL_REFRACTION);

        assertEquals(KeyboardVisualEffects.MATERIAL_SOLID, solid.materialStyle);
        assertTrue(!solid.usesGlassSurface());
        assertTrue(frosted.usesGlassSurface());
        assertTrue(!frosted.usesLiveRefraction());
        assertEquals(
                KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_SOFT,
                frosted.keyFaceGradientCurve);
        assertTrue(!frosted.panelGradientEnabled);
        assertTrue(experimental.usesLiveRefraction());
    }

    @Test
    public void remoteAndNumberRowOptionsUseStringResourceLabels() {
        assertEquals(R.string.number_row_color_full_alpha, AdditionalNumberRowColorMode.FULL_ALPHA.labelResId);
        assertEquals(R.string.number_row_color_half_mod_4567, AdditionalNumberRowColorMode.HALF_MOD_4567.labelResId);
        assertEquals(R.string.remote_key_preset_pc_keyboard, RemoteKeyPreset.PC_KEYBOARD.labelResId);
        assertEquals(R.string.remote_ime_shortcut_alt_shift, RemoteImeShortcut.ALT_SHIFT.labelResId);
        assertEquals(R.string.remote_ime_shortcut_language_switch, RemoteImeShortcut.LANGUAGE_SWITCH.labelResId);
    }

    @Test
    public void accentPlacementTargetsOwnDisplayOrder() {
        AccentPlacementTarget[] order = AccentPlacementTarget.displayOrder();

        assertEquals(AccentPlacementTarget.SETTINGS_ENTER, order[0]);
        assertEquals(order.length, AccentPlacementTarget.allDisplayTargets().size());
        assertTrue(AccentPlacementTarget.allDisplayTargets().contains(AccentPlacementTarget.ESC_POINT));
    }
}
