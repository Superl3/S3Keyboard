package com.superl3.s3keyboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

final class DemoSettingsIntentOverrides {
    private static final String EXTRA_HANGUL_MAIN_REGION_RATIO = "hangul_main_region_ratio";
    private static final String EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT = "hangul_special_column_percent";
    private static final String EXTRA_HANGUL_MAIN_KEY_UNITS = "hangul_main_key_units";
    private static final String EXTRA_DEMO_SHOW_KEYBOARD = "demo_show_keyboard";
    private static final String EXTRA_DEMO_OVERLAY_TESTBED = "demo_overlay_testbed";
    private static final String EXTRA_DEMO_WEAR_TESTBED = "demo_wear_testbed";
    private static final String EXTRA_DEMO_OVERLAY_STYLE = "demo_overlay_style";
    private static final String EXTRA_TRANSPARENT_OVERLAY_INPUT = "transparent_overlay_input";
    private static final String EXTRA_DEMO_FORCE_VISUAL_EFFECTS = "demo_force_visual_effects";
    private static final String EXTRA_DEMO_WATCH_RADIAL_INPUT = "demo_watch_radial_input";
    private static final String EXTRA_KEY_IDLE_COLOR = "key_idle_color";
    private static final String EXTRA_KEY_PRESSED_COLOR = "key_pressed_color";
    private static final String EXTRA_KEYBOARD_BACKGROUND_COLOR = "keyboard_background_color";
    private static final String EXTRA_ACCENT_COLOR = "accent_color";
    private static final String EXTRA_SECONDARY_COLOR = "secondary_color";
    private static final String EXTRA_FUNCTION_KEY_COLOR = "function_key_color";
    private static final String EXTRA_ACCENT_KEY_COLOR = "accent_key_color";
    private static final String EXTRA_BORDER_COLOR = "border_color";
    private static final String EXTRA_KEY_ROUNDNESS_DP = "key_roundness_dp";
    private static final String EXTRA_KEY_GAP_DP = "key_gap_dp";
    private static final String EXTRA_KEY_DEPTH_ENABLED = "key_depth_enabled";
    private static final String EXTRA_KEY_DEPTH_DP = "key_depth_dp";
    private static final String EXTRA_CUSTOM_DEPTH_COLOR_ENABLED = "custom_depth_color_enabled";
    private static final String EXTRA_DEPTH_COLOR = "depth_color";
    private static final String EXTRA_FONT_FAMILY = "font_family";
    private static final String EXTRA_SHOW_HANGUL_SLIDE_HINTS = "show_hangul_slide_hints";
    private static final String EXTRA_SHOW_ENGLISH_SLIDE_HINTS = "show_english_slide_hints";
    private static final String EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW = "show_beginner_tooltip_preview";
    private static final String EXTRA_SHOW_NUMBER_ROW = "show_number_row";
    private static final String EXTRA_SHOW_HANGUL_NUMBER_ROW = "show_hangul_number_row";
    private static final String EXTRA_SHOW_ENGLISH_NUMBER_ROW = "show_english_number_row";
    private static final String EXTRA_DEMO_SETTINGS = "demo_settings";
    private static final String EXTRA_DEMO_FIELD_PROFILE = "demo_field_profile";
    private static final String EXTRA_THEME_PRESET_ID = "theme_preset_id";

    private DemoSettingsIntentOverrides() {
    }

    static Result apply(
            Context context,
            Intent intent,
            KeyboardSettings settings,
            DemoFieldProfile fieldProfile,
            boolean showKeyboard,
            boolean debuggableBuild) {
        KeyboardSettings baseSettings = RuntimeDefaults.keyboardSettings(settings);
        if (intent == null) {
            return new Result(baseSettings, fieldProfile, showKeyboard, false, false);
        }

        boolean debugDemoIntent = debuggableBuild
                && intent.getBooleanExtra(EXTRA_DEMO_SETTINGS, false);
        boolean nextShowKeyboard = debugDemoIntent
                && intent.getBooleanExtra(EXTRA_DEMO_SHOW_KEYBOARD, showKeyboard);
        boolean overlayTestbed = debugDemoIntent
                && intent.getBooleanExtra(EXTRA_DEMO_OVERLAY_TESTBED, false);
        boolean wearTestbed = debugDemoIntent
                && intent.getBooleanExtra(EXTRA_DEMO_WEAR_TESTBED, false);
        if (debugDemoIntent && intent.hasExtra(EXTRA_DEMO_WATCH_RADIAL_INPUT)) {
            KeyboardPreferences.saveWatchRadialInputEnabled(
                    context,
                    intent.getBooleanExtra(EXTRA_DEMO_WATCH_RADIAL_INPUT, false));
        }
        DemoFieldProfile nextFieldProfile = fieldProfile;
        if (debugDemoIntent && intent.hasExtra(EXTRA_DEMO_FIELD_PROFILE)) {
            nextFieldProfile = DemoFieldProfile.fromName(intent.getStringExtra(EXTRA_DEMO_FIELD_PROFILE));
        }
        if (!debugDemoIntent || !hasDemoSettingOverride(intent)) {
            return new Result(
                    baseSettings,
                    nextFieldProfile,
                    nextShowKeyboard,
                    overlayTestbed,
                    wearTestbed);
        }

        KeyboardSettings nextSettings = applySettingsOverride(context, intent, baseSettings);
        KeyboardPreferences.saveSettings(context, nextSettings);
        return new Result(
                nextSettings,
                nextFieldProfile,
                nextShowKeyboard,
                overlayTestbed,
                wearTestbed);
    }

    private static KeyboardSettings applySettingsOverride(
            Context context,
            Intent intent,
            KeyboardSettings settings) {
        KeyboardSettings next = settings;
        if (intent.hasExtra(EXTRA_DEMO_OVERLAY_STYLE)) {
            KeyboardPreferences.saveTransparentOverlayStyle(
                    context,
                    TransparentOverlayStyle.fromPreference(
                            intent.getStringExtra(EXTRA_DEMO_OVERLAY_STYLE)));
        }
        if (intent.hasExtra(EXTRA_TRANSPARENT_OVERLAY_INPUT)) {
            KeyboardPreferences.saveTransparentOverlayInputEnabled(
                    context,
                    intent.getBooleanExtra(EXTRA_TRANSPARENT_OVERLAY_INPUT, true));
        }
        String themePresetId = intent.getStringExtra(EXTRA_THEME_PRESET_ID);
        KeyboardThemePreset themePreset = KeyboardThemePreset.find(themePresetId);
        if (themePreset != null) {
            next = next.withAppearanceFrom(themePreset.applyTo(KeyboardSettings.defaults()));
            KeyboardPreferences.saveSelectedThemeId(context, themePreset.id);
        }

        if (intent.hasExtra(EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT)) {
            next = next.withHangulSpecialColumnPercent(intent.getIntExtra(
                    EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT,
                    next.hangulSpecialColumnPercent));
        } else if (intent.hasExtra(EXTRA_HANGUL_MAIN_REGION_RATIO)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_KEY_UNITS)) {
            next = next.withHangulMainKeyUnits(intent.getIntExtra(
                    EXTRA_HANGUL_MAIN_REGION_RATIO,
                    intent.getIntExtra(
                            EXTRA_HANGUL_MAIN_KEY_UNITS,
                            5)));
        }
        boolean customDepthColorEnabled = intent.hasExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED)
                ? intent.getBooleanExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED, next.customDepthColorEnabled)
                : (next.customDepthColorEnabled || intent.hasExtra(EXTRA_DEPTH_COLOR));
        next = next
                .withExtendedThemeColors(
                        colorExtra(intent, EXTRA_KEY_IDLE_COLOR, next.keyIdleColor),
                        colorExtra(intent, EXTRA_KEY_PRESSED_COLOR, next.keyPressedColor),
                        colorExtra(intent, EXTRA_KEYBOARD_BACKGROUND_COLOR, next.keyboardBackgroundColor),
                        colorExtra(intent, EXTRA_ACCENT_COLOR, next.accentColor),
                        colorExtra(intent, EXTRA_SECONDARY_COLOR, next.secondaryColor),
                        colorExtra(intent, EXTRA_FUNCTION_KEY_COLOR, next.functionKeyColor),
                        colorExtra(intent, EXTRA_ACCENT_KEY_COLOR, next.accentKeyColor),
                        colorExtra(intent, EXTRA_BORDER_COLOR, next.borderColor),
                        customDepthColorEnabled,
                        colorExtra(intent, EXTRA_DEPTH_COLOR, next.depthColor))
                .withFontFamily(RuntimeDefaults.stringOrDefault(
                        intent.getStringExtra(EXTRA_FONT_FAMILY),
                        next.fontFamily))
                .withHintVisibility(
                        intent.getBooleanExtra(
                                EXTRA_SHOW_HANGUL_SLIDE_HINTS,
                                next.showHangulSlideHints),
                        intent.getBooleanExtra(
                                EXTRA_SHOW_ENGLISH_SLIDE_HINTS,
                                next.showEnglishSlideHints),
                        intent.getBooleanExtra(
                                EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW,
                                next.showBeginnerTooltipPreview))
                .withKeyRoundness(intent.getIntExtra(EXTRA_KEY_ROUNDNESS_DP, next.keyRoundnessDp))
                .withKeyGap(intent.getIntExtra(EXTRA_KEY_GAP_DP, next.keyGapDp))
                .withKeyDepth(
                        intent.getBooleanExtra(EXTRA_KEY_DEPTH_ENABLED, next.keyDepthEnabled),
                        intent.getIntExtra(EXTRA_KEY_DEPTH_DP, next.keyDepthDp));
        if (intent.hasExtra(EXTRA_SHOW_NUMBER_ROW)) {
            next = next.withNumberRow(intent.getBooleanExtra(EXTRA_SHOW_NUMBER_ROW, next.showNumberRow));
        }
        if (intent.getBooleanExtra(EXTRA_DEMO_FORCE_VISUAL_EFFECTS, false)) {
            next = next.withVisualEffects(next.visualEffects
                    .withBlur(true, Math.max(10, next.visualEffects.blurRadiusDp))
                    .withGlass(true, 86, 18, 42)
                    .withKeyFaceGradient(
                            true,
                            Math.max(22, next.visualEffects.keyFaceGradientStrengthPercent),
                            next.keyIdleColor,
                            next.keyPressedColor,
                            KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_TOP_GLOW)
                    .withPanelGradient(
                            true,
                            next.keyboardBackgroundColor,
                            next.keyIdleColor));
        }
        return next
                .withHangulNumberRow(intent.getBooleanExtra(
                        EXTRA_SHOW_HANGUL_NUMBER_ROW,
                        next.showHangulNumberRow))
                .withEnglishNumberRow(intent.getBooleanExtra(
                        EXTRA_SHOW_ENGLISH_NUMBER_ROW,
                        next.showEnglishNumberRow));
    }

    private static boolean hasDemoSettingOverride(Intent intent) {
        return intent.hasExtra(EXTRA_HANGUL_SPECIAL_COLUMN_PERCENT)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_REGION_RATIO)
                || intent.hasExtra(EXTRA_HANGUL_MAIN_KEY_UNITS)
                || intent.hasExtra(EXTRA_KEY_IDLE_COLOR)
                || intent.hasExtra(EXTRA_KEY_PRESSED_COLOR)
                || intent.hasExtra(EXTRA_KEYBOARD_BACKGROUND_COLOR)
                || intent.hasExtra(EXTRA_ACCENT_COLOR)
                || intent.hasExtra(EXTRA_SECONDARY_COLOR)
                || intent.hasExtra(EXTRA_FUNCTION_KEY_COLOR)
                || intent.hasExtra(EXTRA_ACCENT_KEY_COLOR)
                || intent.hasExtra(EXTRA_BORDER_COLOR)
                || intent.hasExtra(EXTRA_KEY_ROUNDNESS_DP)
                || intent.hasExtra(EXTRA_KEY_GAP_DP)
                || intent.hasExtra(EXTRA_KEY_DEPTH_ENABLED)
                || intent.hasExtra(EXTRA_KEY_DEPTH_DP)
                || intent.hasExtra(EXTRA_CUSTOM_DEPTH_COLOR_ENABLED)
                || intent.hasExtra(EXTRA_DEPTH_COLOR)
                || intent.hasExtra(EXTRA_FONT_FAMILY)
                || intent.hasExtra(EXTRA_SHOW_HANGUL_SLIDE_HINTS)
                || intent.hasExtra(EXTRA_SHOW_ENGLISH_SLIDE_HINTS)
                || intent.hasExtra(EXTRA_SHOW_BEGINNER_TOOLTIP_PREVIEW)
                || intent.hasExtra(EXTRA_SHOW_NUMBER_ROW)
                || intent.hasExtra(EXTRA_SHOW_HANGUL_NUMBER_ROW)
                || intent.hasExtra(EXTRA_SHOW_ENGLISH_NUMBER_ROW)
                || intent.hasExtra(EXTRA_DEMO_FIELD_PROFILE)
                || intent.hasExtra(EXTRA_THEME_PRESET_ID)
                || intent.hasExtra(EXTRA_DEMO_OVERLAY_STYLE)
                || intent.hasExtra(EXTRA_TRANSPARENT_OVERLAY_INPUT)
                || intent.hasExtra(EXTRA_DEMO_FORCE_VISUAL_EFFECTS);
    }

    private static int colorExtra(Intent intent, String name, int fallback) {
        if (!intent.hasExtra(name)) {
            return fallback;
        }
        try {
            String value = intent.getStringExtra(name);
            if (value != null && !value.startsWith("#")
                    && (value.length() == 6 || value.length() == 8)) {
                value = "#" + value;
            }
            return Color.parseColor(value);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    static final class Result {
        final KeyboardSettings settings;
        final DemoFieldProfile fieldProfile;
        final boolean showKeyboard;
        final boolean overlayTestbed;
        final boolean wearTestbed;

        Result(
                KeyboardSettings settings,
                DemoFieldProfile fieldProfile,
                boolean showKeyboard,
                boolean overlayTestbed,
                boolean wearTestbed) {
            this.settings = RuntimeDefaults.keyboardSettings(settings);
            this.fieldProfile = fieldProfile == null ? DemoFieldProfile.STANDARD : fieldProfile;
            this.showKeyboard = showKeyboard;
            this.overlayTestbed = overlayTestbed;
            this.wearTestbed = wearTestbed;
        }
    }
}
