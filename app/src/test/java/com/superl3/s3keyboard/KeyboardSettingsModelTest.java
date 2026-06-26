package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class KeyboardSettingsModelTest {
    @Test
    public void modelSplitsKeyboardSettingsIntoStableSections() {
        Map<String, Integer> overrides = new HashMap<>();
        overrides.put("background:enter", 0xFF445566);
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withHandednessPreset(HandednessMode.LEFT)
                .withHangulHeight(275)
                .withEnglishHeight(235)
                .withHangulSidePadding(4, 8)
                .withEnglishSidePadding(6, 10)
                .withLayoutSpacing(9, 3, 5, 11)
                .withNumberRowBottomGap(12)
                .withGestureThreshold(19)
                .withTouchYOffset(-3)
                .withEnterKeyLabel("검색")
                .withTypography(
                        KeyboardSettings.FONT_D2CODING,
                        91,
                        83,
                        true,
                        false,
                        false,
                        true)
                .withFollowThemeTypography(true)
                .withExtendedThemeColors(
                        0xFF111111,
                        0xFF222222,
                        0xFF333333,
                        0xFF444444,
                        0xFF555555,
                        0xFF666666,
                        0xFF777777,
                        0xFF888888,
                        true,
                        0xFF999999)
                .withKeyColorOverrides(overrides)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.CTRL_SPACE);

        KeyboardSettingsModel model = KeyboardSettingsModel.from(
                settings,
                KeyboardErgonomicsPreset.AGGRESSIVE.options);

        assertEquals(0xFF111111, model.appearance.keyIdleColor);
        assertEquals(0xFF888888, model.appearance.borderColor);
        assertEquals(0xFF999999, model.appearance.depthColor);
        assertTrue(model.appearance.customDepthColorEnabled);
        assertEquals(KeyboardSettings.FONT_D2CODING, model.appearance.fontFamily);
        assertTrue(model.appearance.followThemeTypography);
        assertEquals(1, model.appearance.keyColorOverrideCount);
        assertEquals(KeyboardMode.ENGLISH, model.layout.keyboardMode);
        assertEquals(HandednessMode.LEFT, model.layout.handednessMode);
        assertEquals(275, model.layout.hangulKeyboardHeightDp);
        assertEquals(235, model.layout.englishKeyboardHeightDp);
        assertEquals(4, model.layout.hangulLeftPaddingDp);
        assertEquals(10, model.layout.englishRightPaddingDp);
        assertEquals(9, model.layout.hangulMainSpecialGapDp);
        assertEquals(12, model.layout.numberRowBottomGapDp);
        assertEquals(19, model.input.gestureThresholdDp);
        assertEquals(-3, model.input.touchYOffsetDp);
        assertEquals("검색", model.input.enterKeyLabel);
        assertTrue(model.remote.remoteModeEnabled);
        assertEquals(RemoteImeShortcut.CTRL_SPACE, model.remote.remoteImeShortcut);
        assertTrue(model.ergonomics.leftAssistRailEnabled);
    }

    @Test
    public void nullModelUsesDefaults() {
        KeyboardSettingsModel model = KeyboardSettingsModel.from(null, null);

        assertEquals(KeyboardSettings.DEFAULT_KEY_IDLE_COLOR, model.appearance.keyIdleColor);
        assertEquals(KeyboardMode.HANGUL, model.layout.keyboardMode);
        assertEquals(KeyboardSettings.DEFAULT_GESTURE_THRESHOLD_DP, model.input.gestureThresholdDp);
        assertFalse(model.remote.remoteModeEnabled);
        assertFalse(model.ergonomics.mainKeyCenteringEnabled);
    }
}
