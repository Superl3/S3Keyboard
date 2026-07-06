package com.superl3.s3keyboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
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
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(SettingsRowBuilder.dp(context, 44));
        button.setPadding(SettingsRowBuilder.dp(context, 24), 0, SettingsRowBuilder.dp(context, 24), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? ui.selectedFill : ui.controlFill);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(
                Math.max(1, SettingsRowBuilder.dp(context, selected ? 2 : 1)),
                selected ? ui.selectedBorder : ui.border);
        button.setBackground(background);
    }

    static void buttonIcon(Button button, Context context, int drawableResId) {
        if (button == null || context == null) {
            return;
        }
        button.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
        button.setCompoundDrawablePadding(SettingsRowBuilder.dp(context, 8));
        int tint = SettingsUiPalette.from(context).controlText;
        for (Drawable drawable : button.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setTint(tint);
            }
        }
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
        input.setMinHeight(SettingsRowBuilder.dp(context, 44));
        input.setPadding(SettingsRowBuilder.dp(context, 14), 0, SettingsRowBuilder.dp(context, 14), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), ui.border);
        input.setBackground(background);
    }

    static void spinner(Spinner spinner, Context context) {
        if (spinner == null || context == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        spinner.setMinimumHeight(SettingsRowBuilder.dp(context, 44));
        spinner.setPadding(SettingsRowBuilder.dp(context, 8), 0, SettingsRowBuilder.dp(context, 8), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.controlFill);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), ui.border);
        spinner.setBackground(background);
    }
}
