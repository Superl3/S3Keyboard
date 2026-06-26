package com.superl3.s3keyboard;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class KeyboardVirtualKeyAccessibilityProvider extends AccessibilityNodeProvider {
    interface Host {
        List<HangulKeyboardView.KeySlot> accessibilityKeySlots();

        KeyboardSettings accessibilitySettings();

        KeyboardSurface accessibilityKeyboardSurface();

        boolean accessibilityDebugKeyBoundsOverlayEnabled();

        boolean performHostAccessibilityClick();

        boolean performKeyAccessibilityClick(int virtualViewId, GestureKey key);
    }

    private static final int FIRST_KEY_ID = 1;

    private final View view;
    private final Host host;

    KeyboardVirtualKeyAccessibilityProvider(View view, Host host) {
        this.view = view;
        this.host = host;
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
        if (virtualViewId == View.NO_ID) {
            return createHostNodeInfo();
        }
        HangulKeyboardView.KeySlot keySlot = keySlotForVirtualId(virtualViewId);
        if (keySlot == null) {
            return null;
        }
        return createKeyNodeInfo(virtualViewId, keySlot);
    }

    @Override
    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(
            String searched,
            int virtualViewId) {
        if (searched == null || searched.isEmpty()) {
            return Collections.emptyList();
        }
        String needle = searched.toLowerCase(Locale.ROOT);
        List<AccessibilityNodeInfo> matches = new ArrayList<>();
        List<HangulKeyboardView.KeySlot> slots = keySlots();
        for (int i = 0; i < slots.size(); i++) {
            String label = KeyboardKeyAccessibilityLabel.describe(view.getContext(), slots.get(i).key);
            if (label.toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(createKeyNodeInfo(FIRST_KEY_ID + i, slots.get(i)));
            }
        }
        return matches;
    }

    @Override
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        if (virtualViewId == View.NO_ID) {
            return action == AccessibilityNodeInfo.ACTION_CLICK
                    && host != null
                    && host.performHostAccessibilityClick();
        }
        if (action != AccessibilityNodeInfo.ACTION_CLICK) {
            return false;
        }
        HangulKeyboardView.KeySlot keySlot = keySlotForVirtualId(virtualViewId);
        if (keySlot == null || keySlot.key == null || host == null) {
            return false;
        }
        if (!host.performKeyAccessibilityClick(virtualViewId, keySlot.key)) {
            return false;
        }
        sendVirtualKeyAccessibilityEvent(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
        return true;
    }

    private AccessibilityNodeInfo createHostNodeInfo() {
        AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(view);
        info.setClassName(view.getClass().getName());
        info.setPackageName(view.getContext().getPackageName());
        String summary = summary();
        info.setContentDescription(summary);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            info.setStateDescription(summary);
        }
        List<HangulKeyboardView.KeySlot> slots = keySlots();
        for (int i = 0; i < slots.size(); i++) {
            info.addChild(view, FIRST_KEY_ID + i);
        }
        return info;
    }

    private AccessibilityNodeInfo createKeyNodeInfo(
            int virtualViewId,
            HangulKeyboardView.KeySlot keySlot) {
        AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
        info.setSource(view, virtualViewId);
        info.setParent(view);
        info.setClassName("android.inputmethodservice.Keyboard$Key");
        info.setPackageName(view.getContext().getPackageName());
        String description = KeyboardKeyAccessibilityLabel.describe(view.getContext(), keySlot.key);
        info.setContentDescription(description);
        info.setText(description);
        info.setEnabled(view.isEnabled());
        info.setVisibleToUser(view.isShown());
        info.setFocusable(true);
        info.setClickable(true);
        info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
        Rect parentBounds = rectFrom(keySlot.hitBounds());
        info.setBoundsInParent(parentBounds);
        Rect screenBounds = new Rect(parentBounds);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        screenBounds.offset(location[0], location[1]);
        info.setBoundsInScreen(screenBounds);
        return info;
    }

    private HangulKeyboardView.KeySlot keySlotForVirtualId(int virtualViewId) {
        int index = virtualViewId - FIRST_KEY_ID;
        List<HangulKeyboardView.KeySlot> slots = keySlots();
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    private List<HangulKeyboardView.KeySlot> keySlots() {
        return host == null ? Collections.emptyList() : host.accessibilityKeySlots();
    }

    private String summary() {
        KeyboardSettings settings = host == null
                ? KeyboardSettings.defaults()
                : host.accessibilitySettings();
        KeyboardSurface surface = host == null
                ? KeyboardSurface.NORMAL
                : host.accessibilityKeyboardSurface();
        boolean debugOverlay = host != null && host.accessibilityDebugKeyBoundsOverlayEnabled();
        return KeyboardAccessibilitySummary.describe(
                view.getContext(),
                settings,
                surface,
                keySlots().size(),
                debugOverlay);
    }

    private Rect rectFrom(RectF bounds) {
        return new Rect(
                Math.round(bounds.left),
                Math.round(bounds.top),
                Math.round(bounds.right),
                Math.round(bounds.bottom));
    }

    private void sendVirtualKeyAccessibilityEvent(int virtualViewId, int eventType) {
        HangulKeyboardView.KeySlot keySlot = keySlotForVirtualId(virtualViewId);
        if (keySlot == null || view.getParent() == null) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.setPackageName(view.getContext().getPackageName());
        event.setClassName("android.inputmethodservice.Keyboard$Key");
        event.setSource(view, virtualViewId);
        event.setEnabled(view.isEnabled());
        event.getText().add(KeyboardKeyAccessibilityLabel.describe(view.getContext(), keySlot.key));
        view.getParent().requestSendAccessibilityEvent(view, event);
    }
}
