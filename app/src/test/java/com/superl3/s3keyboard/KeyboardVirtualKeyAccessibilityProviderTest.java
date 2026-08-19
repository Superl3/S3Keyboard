package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.view.accessibility.AccessibilityNodeInfo;

import org.junit.Test;

public final class KeyboardVirtualKeyAccessibilityProviderTest {
    @Test
    public void customActionIdsMapToTheExactKeyboardGesture() {
        assertEquals(
                GestureAction.TAP,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        AccessibilityNodeInfo.ACTION_CLICK));
        assertEquals(
                GestureAction.UP,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        R.id.keyboard_accessibility_swipe_up));
        assertEquals(
                GestureAction.DOWN,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        R.id.keyboard_accessibility_swipe_down));
        assertEquals(
                GestureAction.LEFT,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        R.id.keyboard_accessibility_swipe_left));
        assertEquals(
                GestureAction.RIGHT,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        R.id.keyboard_accessibility_swipe_right));
        assertEquals(
                GestureAction.LONG_PRESS,
                KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(
                        AccessibilityNodeInfo.ACTION_LONG_CLICK));
        assertNull(KeyboardVirtualKeyAccessibilityProvider.gestureActionFor(-1));
    }
}
