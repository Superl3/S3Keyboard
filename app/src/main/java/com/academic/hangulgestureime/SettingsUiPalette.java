package com.academic.hangulgestureime;

import android.content.Context;
import android.content.res.Configuration;

final class SettingsUiPalette {
    final boolean dark;
    final int background;
    final int surface;
    final int surfaceRaised;
    final int textPrimary;
    final int textSecondary;
    final int selectedFill;
    final int selectedText;
    final int selectedBorder;
    final int controlFill;
    final int controlText;
    final int border;
    final int scrim;

    private SettingsUiPalette(
            boolean dark,
            int background,
            int surface,
            int surfaceRaised,
            int textPrimary,
            int textSecondary,
            int selectedFill,
            int selectedText,
            int selectedBorder,
            int controlFill,
            int controlText,
            int border,
            int scrim) {
        this.dark = dark;
        this.background = background;
        this.surface = surface;
        this.surfaceRaised = surfaceRaised;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.selectedFill = selectedFill;
        this.selectedText = selectedText;
        this.selectedBorder = selectedBorder;
        this.controlFill = controlFill;
        this.controlText = controlText;
        this.border = border;
        this.scrim = scrim;
    }

    static SettingsUiPalette from(Context context) {
        int mode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean dark = mode == Configuration.UI_MODE_NIGHT_YES;
        if (dark) {
            return new SettingsUiPalette(
                    true,
                    0xFF171A20,
                    0xFF20242B,
                    0xFF252A32,
                    0xFFE5E7EB,
                    0xFFA7B0BE,
                    0xFF2F6F73,
                    0xFFEAFBFB,
                    0xFF5FCFD4,
                    0xFF2B3038,
                    0xFFE5E7EB,
                    0xFF3B4250,
                    0x99000000);
        }
        return new SettingsUiPalette(
                false,
                0xFFEEF1F5,
                0xFFF7F8FA,
                0xFFFDFDFE,
                0xFF2B3038,
                0xFF667085,
                0xFFD7EEF0,
                0xFF244247,
                0xFF4AA8AF,
                0xFFE4E8EE,
                0xFF2B3038,
                0xFFC7CED8,
                0x66000000);
    }
}
