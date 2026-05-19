package com.superl3.s3keyboard;

import android.content.Context;
import android.content.res.Configuration;

final class SettingsUiPalette {
    final boolean dark;
    final int background;
    final int surface;
    final int surfaceRaised;
    final int primaryBackground;
    final int secondaryBackground;
    final int specialBackground;
    final int primaryForeground;
    final int secondaryForeground;
    final int specialForeground;
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
            int primaryBackground,
            int secondaryBackground,
            int specialBackground,
            int primaryForeground,
            int secondaryForeground,
            int specialForeground,
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
        this.primaryBackground = primaryBackground;
        this.secondaryBackground = secondaryBackground;
        this.specialBackground = specialBackground;
        this.primaryForeground = primaryForeground;
        this.secondaryForeground = secondaryForeground;
        this.specialForeground = specialForeground;
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
                    0xFF17191D,
                    0xFF20232A,
                    0xFF252932,
                    0xFF17191D,
                    0xFF20232A,
                    0xFF1D4E55,
                    0xFFF0ECE4,
                    0xFFC9C4BB,
                    0xFFB7F2EA,
                    0xFFF0ECE4,
                    0xFFC9C4BB,
                    0xFF1D4E55,
                    0xFFECFFFC,
                    0xFF5ED4D0,
                    0xFF20232A,
                    0xFFF0ECE4,
                    0xFF3A3F48,
                    0x99000000);
        }
        return new SettingsUiPalette(
                false,
                0xFFF2F0EA,
                0xFFFBF8F1,
                0xFFFFFFFF,
                0xFFF2F0EA,
                0xFFFBF8F1,
                0xFFDDEEEB,
                0xFF2D3036,
                0xFF626A73,
                0xFF14545A,
                0xFF2D3036,
                0xFF626A73,
                0xFFDDEEEB,
                0xFF14545A,
                0xFF42AEB0,
                0xFFE7E2D8,
                0xFF2D3036,
                0xFFCBC4B8,
                0x66000000);
    }
}
