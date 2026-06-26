package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public final class KeyboardSettingsSnapshotTest {
    @Test
    public void snapshotSeparatesAppearanceLayoutInputAndRemoteSettings() throws Exception {
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
                        false);

        JSONObject snapshot = KeyboardSettingsSnapshot.from(settings).toJson();

        assertTrue(snapshot.has("appearance"));
        assertTrue(snapshot.has("layout"));
        assertTrue(snapshot.has("input"));
        assertTrue(snapshot.has("remote"));
        assertTrue(snapshot.has("ergonomics"));
        assertEquals("d2coding", snapshot.getJSONObject("appearance").getString("fontFamily"));
        assertEquals("#FF101010", snapshot.getJSONObject("appearance").getString("keyIdleColor"));
        assertEquals("#FF808080", snapshot.getJSONObject("appearance").getString("borderColor"));
        assertEquals("#FF909090", snapshot.getJSONObject("appearance").getString("depthColor"));
        assertEquals("ENGLISH", snapshot.getJSONObject("layout").getString("keyboardMode"));
        assertEquals(235, snapshot.getJSONObject("layout").getInt("keyboardHeightDp"));
        assertEquals(280, snapshot.getJSONObject("layout").getInt("hangulKeyboardHeightDp"));
        assertEquals(18, snapshot.getJSONObject("input").getInt("gestureThresholdDp"));
        assertEquals(false, snapshot.getJSONObject("input").getBoolean("showHangulSlideHints"));
        assertEquals(true, snapshot.getJSONObject("remote").getBoolean("remoteModeEnabled"));
        assertEquals("win_space", snapshot.getJSONObject("remote").getString("remoteImeShortcut"));
        assertEquals(false, snapshot.getJSONObject("ergonomics").getBoolean("mainKeyCenteringEnabled"));
    }

    @Test
    public void snapshotCanIncludeCurrentErgonomicsPreferences() throws Exception {
        JSONObject snapshot = KeyboardSettingsSnapshot.from(
                KeyboardSettings.defaults(),
                KeyboardErgonomicsPreset.ERGONOMIC.options).toJson();

        JSONObject ergonomics = snapshot.getJSONObject("ergonomics");
        assertEquals(true, ergonomics.getBoolean("mainKeyCenteringEnabled"));
        assertEquals(true, ergonomics.getBoolean("compactFunctionRailEnabled"));
        assertEquals(true, ergonomics.getBoolean("ergonomicHitboxEnabled"));
        assertEquals(true, ergonomics.getBoolean("ergonomicPositionAdjustEnabled"));
        assertEquals(true, ergonomics.getBoolean("leftAssistRailEnabled"));
    }
}
