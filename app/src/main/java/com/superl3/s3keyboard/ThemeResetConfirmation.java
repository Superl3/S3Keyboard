package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;

final class ThemeResetConfirmation {
    private ThemeResetConfirmation() {
    }

    static void show(Activity activity, Runnable onConfirm) {
        if (activity == null) {
            return;
        }
        Runnable safeConfirm = RuntimeDefaults.runnable(onConfirm);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.theme_reset_confirm_title)
                .setMessage(R.string.theme_reset_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_restore, (dialog, which) -> safeConfirm.run())
                .show();
    }
}
