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
                    0xFF0F172A,
                    0xFF111827,
                    0xFF1F2937,
                    0xFFF8FAFC,
                    0xFFCBD5E1,
                    0xFF2DD4BF,
                    0xFF042F2E,
                    0xFF67E8F9,
                    0xFF334155,
                    0xFFF8FAFC,
                    0xFF475569,
                    0x99000000);
        }
        return new SettingsUiPalette(
                false,
                0xFFF8FAFC,
                0xFFFFFFFF,
                0xFFFFFFFF,
                0xFF111827,
                0xFF475569,
                0xFF0F766E,
                0xFFFFFFFF,
                0xFF0891B2,
                0xFFE5E7EB,
                0xFF111827,
                0xFFCBD5E1,
                0x66000000);
    }
}
