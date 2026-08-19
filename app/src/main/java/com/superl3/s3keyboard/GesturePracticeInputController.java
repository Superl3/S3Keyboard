package com.superl3.s3keyboard;

import android.app.Activity;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

final class GesturePracticeInputController {
    private final Activity activity;
    private EditText input;
    private View focusRetentionRegion;

    GesturePracticeInputController(Activity activity) {
        this.activity = activity;
    }

    EditText createInput(DemoFieldProfile profile, boolean showKeyboard) {
        input = SettingsRowBuilder.editText(activity);
        input.setHint(R.string.gesture_practice_hint);
        DemoFieldProfile safeProfile = profile == null ? DemoFieldProfile.STANDARD : profile;
        safeProfile.applyTo(input);
        input.setFocusableInTouchMode(true);
        maybeShowKeyboard(showKeyboard);
        return input;
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN
                || input == null
                || !input.hasFocus()
                || !isTouchOutsideInput(event)
                || isTouchInside(focusRetentionRegion, event)) {
            return;
        }
        clearFocusAndHideKeyboard();
    }

    void setFocusRetentionRegion(View region) {
        focusRetentionRegion = region;
    }

    void clearFocusAndHideKeyboard() {
        if (input == null) {
            return;
        }
        input.clearFocus();
        InputMethodManager imm = activity.getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    private void maybeShowKeyboard(boolean showKeyboard) {
        if (!showKeyboard) {
            return;
        }
        input.requestFocusFromTouch();
        input.postDelayed(() -> showSoftInput(InputMethodManager.SHOW_IMPLICIT), 350);
        input.postDelayed(() -> showSoftInput(InputMethodManager.SHOW_FORCED), 900);
    }

    private void showSoftInput(int flag) {
        input.requestFocusFromTouch();
        InputMethodManager imm = activity.getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.showSoftInput(input, flag);
        }
    }

    private boolean isTouchOutsideInput(MotionEvent event) {
        return !isTouchInside(input, event);
    }

    private boolean isTouchInside(View view, MotionEvent event) {
        if (view == null) {
            return false;
        }
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect.contains(Math.round(event.getRawX()), Math.round(event.getRawY()));
    }
}
