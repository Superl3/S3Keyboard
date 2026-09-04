package com.superl3.s3keyboard;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;

final class SettingsSystemBars {
    private SettingsSystemBars() {
    }

    static void apply(Activity activity) {
        if (activity == null) {
            return;
        }
        SettingsUiPalette palette = SettingsUiPalette.from(activity);
        Window window = activity.getWindow();
        window.setStatusBarColor(palette.background);
        window.setNavigationBarColor(palette.background);

        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags = palette.dark
                ? flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                : flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = palette.dark
                    ? flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    : flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    static void applyTopInset(View root) {
        if (root == null) {
            return;
        }
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    view.getPaddingLeft(),
                    insets.getSystemWindowInsetTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
            return insets;
        });
        root.requestApplyInsets();
    }
}
