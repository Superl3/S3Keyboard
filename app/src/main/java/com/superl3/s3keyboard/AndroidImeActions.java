package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

final class AndroidImeActions {
    private AndroidImeActions() {
    }

    static void openInputSettings(Activity activity) {
        if (activity != null) {
            activity.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        }
    }

    static void showInputPicker(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager manager = activity.getSystemService(InputMethodManager.class);
        if (manager != null) {
            manager.showInputMethodPicker();
        }
    }
}
