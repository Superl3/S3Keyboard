package com.superl3.s3keyboard;

import android.content.Context;

final class SettingsValueFormatter {
    private SettingsValueFormatter() {
    }

    static String currentState(Context context, String state) {
        return context.getString(R.string.settings_current_state_format, state);
    }

    static String sharedPadding(Context context, int dp) {
        return context.getString(R.string.settings_shared_padding_format, dp);
    }

    static String hangulHeight(Context context, KeyboardSettings settings) {
        return context.getString(
                R.string.settings_hangul_height_format,
                settings.hangulKeyboardHeightDp,
                numberRowSuffix(context, settings.keyboardMode == KeyboardMode.HANGUL && settings.showNumberRow));
    }

    static String englishHeight(Context context, KeyboardSettings settings) {
        return context.getString(
                R.string.settings_english_height_format,
                settings.englishKeyboardHeightDp,
                numberRowSuffix(context, settings.keyboardMode == KeyboardMode.ENGLISH && settings.showNumberRow));
    }

    static String hangulSpecialColumn(Context context, int percent) {
        return context.getString(R.string.settings_hangul_special_column_format, percent);
    }

    static String keyboardTopPadding(Context context, int dp) {
        return context.getString(R.string.settings_keyboard_top_padding_format, dp);
    }

    static String keyboardBottomPadding(Context context, int dp) {
        return context.getString(R.string.settings_keyboard_bottom_padding_format, dp);
    }

    static String numberRowGap(Context context, int dp) {
        return context.getString(R.string.settings_number_row_gap_format, dp);
    }

    static String hangulKeyGap(Context context, int dp) {
        return context.getString(R.string.settings_hangul_key_gap_format, dp);
    }

    static String englishKeyGap(Context context, int dp) {
        return context.getString(R.string.settings_english_key_gap_format, dp);
    }

    static String roundness(Context context, int dp) {
        return context.getString(R.string.settings_roundness_format, dp);
    }

    static String borderWidth(Context context, int dp) {
        return context.getString(R.string.settings_border_width_format, dp);
    }

    static String visualGap(Context context, int dp) {
        return context.getString(R.string.settings_visual_gap_format, dp);
    }

    static String depthHeight(Context context, KeyboardSettings settings) {
        return context.getString(
                R.string.settings_depth_height_format,
                settings.keyDepthDp,
                settings.keyDepthEnabled ? "" : context.getString(R.string.settings_flat_suffix));
    }

    static String gestureThreshold(Context context, int dp) {
        return context.getString(R.string.settings_gesture_threshold_format, dp);
    }

    static String hitSlop(Context context, int dp) {
        return context.getString(R.string.settings_hit_slop_format, dp);
    }

    static String spacebarCursorDeadZone(Context context, int dp) {
        return context.getString(R.string.settings_spacebar_cursor_dead_zone_format, dp);
    }

    static String hapticDuration(Context context, int ms) {
        return context.getString(R.string.settings_haptic_duration_format, ms);
    }

    static String hapticGap(Context context, int ms) {
        return context.getString(R.string.settings_haptic_gap_format, ms);
    }

    static String primaryTextSize(Context context, int percent) {
        return context.getString(R.string.settings_primary_text_size_format, percent);
    }

    static String secondaryTextSize(Context context, int percent) {
        return context.getString(R.string.settings_secondary_text_size_format, percent);
    }

    static String touchYOffset(Context context, int dp) {
        return context.getString(R.string.settings_touch_y_offset_format, dp);
    }

    static String repeatStartDelay(Context context, int ms) {
        return context.getString(R.string.settings_repeat_start_delay_format, ms);
    }

    static String repeatInterval(Context context, int ms) {
        return context.getString(R.string.settings_repeat_interval_format, ms);
    }

    static String singleTapStartHold(Context context, int ms) {
        return context.getString(R.string.settings_single_tap_start_hold_format, ms);
    }

    static String singleTapCommitHold(Context context, int ms) {
        return context.getString(R.string.settings_single_tap_commit_hold_format, ms);
    }

    private static String numberRowSuffix(Context context, boolean visible) {
        return visible ? context.getString(R.string.settings_num_row_suffix) : "";
    }
}
