package com.superl3.s3keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

final class SettingsViewStyler {
    private SettingsViewStyler() {
    }

    static void button(Button button, Context context, boolean selected) {
        if (button == null || context == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        button.setAllCaps(false);
        button.setTextColor(selected ? ui.selectedText : ui.controlText);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? ui.selectedFill : ui.controlFill);
        background.setCornerRadius(dp(context, 8));
        background.setStroke(Math.max(1, dp(context, selected ? 2 : 1)), selected ? ui.selectedBorder : ui.border);
        button.setBackground(background);
    }

    static void label(TextView view, Context context, boolean secondary) {
        if (view == null || context == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        view.setTextColor(secondary ? ui.textSecondary : ui.textPrimary);
    }

    static void compoundButton(CompoundButton button, Context context) {
        if (button == null || context == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        button.setTextColor(ui.textPrimary);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setButtonTintList(new ColorStateList(
                    new int[][] {
                            new int[] { android.R.attr.state_checked },
                            new int[] {}
                    },
                    new int[] {
                            ui.specialForeground,
                            ui.secondaryForeground
                    }));
        }
    }

    static void editText(EditText input, Context context) {
        if (input == null || context == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        input.setTextColor(ui.textPrimary);
        input.setHintTextColor(ui.textSecondary);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            input.setBackgroundTintList(new ColorStateList(
                    new int[][] {
                            new int[] { android.R.attr.state_focused },
                            new int[] {}
                    },
                    new int[] {
                            ui.specialForeground,
                            ui.border
                    }));
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
