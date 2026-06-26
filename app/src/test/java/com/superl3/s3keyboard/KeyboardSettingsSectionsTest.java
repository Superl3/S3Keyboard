package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;

public final class KeyboardSettingsSectionsTest {
    @Test
    public void typedSectionsExposeAppearanceLayoutInputAndRemoteFields() throws Exception {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("background:enter", 0xFF112233);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withExtendedThemeColors(
                        0xFF101010,
                        0xFF202020,
                        0xFF303030,
                        0xFF404040,
                        0xFF505050,
                        0xFF606060,
                        0xFF707070,
                        0xFF808080,
                        true,
                        0xFF909090)
                .withHangulHeight(280)
                .withEnglishHeight(235)
                .withHangulSidePadding(7, 9)
                .withGestureThreshold(18)
                .withHintVisibility(false, true, false)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.WIN_SPACE)
                .withTypography(
                        KeyboardSettings.FONT_D2CODING,
                        90,
                        82,
                        true,
                        false,
                        true,
                        true)
                .withFollowThemeTypography(true)
                .withKeyColorOverrides(overrides)
                .withVisualEffects(new KeyboardVisualEffects(
                        true,
                        8,
                        true,
                        30,
                        false,
                        true,
                        40,
                        0xFF111111,
                        0xFF222222,
                        KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_TOP_GLOW,
                        true,
                        0xFF333333,
                        0xFF444444));

        KeyboardSettingsSections sections = KeyboardSettingsSections.from(settings);
        JSONObject appearance = sections.appearance.toJson();
        JSONObject input = sections.input.toJson();

        assertEquals(KeyboardSettings.FONT_D2CODING, sections.appearance.fontFamily);
        assertEquals("#FF101010", appearance.getString("keyIdleColor"));
        assertEquals("#FF808080", appearance.getString("borderColor"));
        assertEquals("#FF909090", appearance.getString("depthColor"));
        assertTrue(appearance.getBoolean("customDepthColorEnabled"));
        assertTrue(appearance.getBoolean("secondaryTextItalic"));
        assertTrue(appearance.getBoolean("followThemeTypography"));
        assertEquals(1, appearance.getInt("keyColorOverrideCount"));
        assertEquals(true, appearance.getJSONObject("visualEffects").getBoolean("blurEnabled"));
        assertEquals("top_glow", appearance.getJSONObject("visualEffects").getString("keyFaceGradientCurve"));
        assertEquals("#FF333333", appearance.getJSONObject("visualEffects").getString("panelGradientStartColor"));
        assertEquals(KeyboardMode.ENGLISH, sections.layout.keyboardMode);
        assertEquals(280, sections.layout.hangulKeyboardHeightDp);
        assertEquals(235, sections.layout.keyboardHeightDp);
        assertEquals(7, sections.layout.hangulLeftPaddingDp);
        assertEquals(true, sections.layout.showNumberRow);
        assertEquals(18, sections.input.gestureThresholdDp);
        assertEquals(settings.enterKeyLabel, sections.input.enterKeyLabel);
        assertEquals(settings.enterKeyLabel, input.getString("enterKeyLabel"));
        assertFalse(input.getBoolean("showHangulSlideHints"));
        assertTrue(sections.remote.remoteModeEnabled);
        assertEquals(RemoteImeShortcut.WIN_SPACE, sections.remote.remoteImeShortcut);
        assertFalse(sections.ergonomics.mainKeyCenteringEnabled);
        assertEquals("none", sections.ergonomics.toJson().getString("visualConsistencyLevel"));
    }

    @Test
    public void typedSectionsExposeErgonomicsFields() throws Exception {
        KeyboardErgonomicsOptions options = KeyboardErgonomicsPreset.AGGRESSIVE.options;

        KeyboardSettingsSections sections = KeyboardSettingsSections.from(
                KeyboardSettings.defaults(),
                options);
        JSONObject ergonomics = sections.ergonomics.toJson();

        assertTrue(ergonomics.getBoolean("mainKeyCenteringEnabled"));
        assertTrue(ergonomics.getBoolean("compactFunctionRailEnabled"));
        assertTrue(ergonomics.getBoolean("ergonomicHitboxEnabled"));
        assertTrue(ergonomics.getBoolean("leftAssistRailEnabled"));
        assertTrue(ergonomics.getBoolean("uniformGridGapEnabled"));
        assertEquals(
                options.visualConsistencyLevel.preferenceValue,
                ergonomics.getString("visualConsistencyLevel"));
    }
}
