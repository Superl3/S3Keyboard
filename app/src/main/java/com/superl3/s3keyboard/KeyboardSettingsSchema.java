package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class KeyboardSettingsSchema {
    enum Section {
        APPEARANCE,
        LAYOUT,
        INPUT_FEEL,
        REMOTE,
        ERGONOMICS,
        THEME,
        RESERVED_PHRASES,
        PRIVACY_DEBUG,
        LEGACY
    }

    enum StorageRisk {
        NONE,
        LOCAL_TEXT,
        LOCAL_DIAGNOSTIC,
        COMPATIBILITY
    }

    static final class Entry {
        final String key;
        final Section section;
        final StorageRisk risk;
        final boolean userFacing;

        private Entry(String key, Section section, StorageRisk risk, boolean userFacing) {
            this.key = key;
            this.section = section;
            this.risk = risk;
            this.userFacing = userFacing;
        }
    }

    private static final List<Entry> ENTRIES = buildEntries();

    private KeyboardSettingsSchema() {
    }

    static List<Entry> entries() {
        return ENTRIES;
    }

    static Entry find(String key) {
        if (key == null) {
            return null;
        }
        for (Entry entry : ENTRIES) {
            if (key.equals(entry.key)) {
                return entry;
            }
        }
        return null;
    }

    static boolean contains(String key) {
        return find(key) != null;
    }

    static List<Entry> entriesFor(Section section) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            if (entry.section == section) {
                result.add(entry);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static List<Entry> buildEntries() {
        List<Entry> entries = new ArrayList<>();

        add(entries, Section.LAYOUT,
                KeyboardPreferences.KEYBOARD_MODE_LAST,
                KeyboardPreferences.HANGUL_LAYOUT_PROFILE,
                KeyboardPreferences.ENGLISH_LAYOUT_PROFILE,
                KeyboardPreferences.HANDEDNESS_MODE,
                KeyboardPreferences.LEFT_MARGIN_DP,
                KeyboardPreferences.RIGHT_MARGIN_DP,
                KeyboardPreferences.HANGUL_LEFT_PADDING_DP,
                KeyboardPreferences.HANGUL_RIGHT_PADDING_DP,
                KeyboardPreferences.ENGLISH_LEFT_PADDING_DP,
                KeyboardPreferences.ENGLISH_RIGHT_PADDING_DP,
                KeyboardPreferences.HANGUL_MAIN_SPECIAL_GAP_DP,
                KeyboardPreferences.KEYBOARD_TOP_PADDING_DP,
                KeyboardPreferences.KEYBOARD_BOTTOM_PADDING_DP,
                KeyboardPreferences.BOTTOM_ROW_TOP_PADDING_DP,
                KeyboardPreferences.NUMBER_ROW_BOTTOM_GAP_DP,
                KeyboardPreferences.KEYBOARD_HEIGHT_DP,
                KeyboardPreferences.HANGUL_KEYBOARD_HEIGHT_DP,
                KeyboardPreferences.ENGLISH_KEYBOARD_HEIGHT_DP,
                KeyboardPreferences.SHOW_NUMBER_ROW,
                KeyboardPreferences.SHOW_HANGUL_NUMBER_ROW,
                KeyboardPreferences.SHOW_ENGLISH_NUMBER_ROW,
                KeyboardPreferences.ADDITIONAL_NUMBER_ROW_COLOR_MODE,
                KeyboardPreferences.HANGUL_SPECIAL_COLUMN_PERCENT,
                KeyboardPreferences.HANGUL_MAIN_KEY_UNITS,
                KeyboardPreferences.FLOATING_MODE_ENABLED);

        add(entries, Section.APPEARANCE,
                KeyboardPreferences.KEY_IDLE_COLOR,
                KeyboardPreferences.KEY_PRESSED_COLOR,
                KeyboardPreferences.KEYBOARD_BACKGROUND_COLOR,
                KeyboardPreferences.ACCENT_COLOR,
                KeyboardPreferences.SECONDARY_COLOR,
                KeyboardPreferences.FUNCTION_KEY_COLOR,
                KeyboardPreferences.ACCENT_KEY_COLOR,
                KeyboardPreferences.BORDER_COLOR,
                KeyboardPreferences.KEY_BORDER_WIDTH_DP,
                KeyboardPreferences.KEY_ROUNDNESS_DP,
                KeyboardPreferences.KEY_GAP_DP,
                KeyboardPreferences.HANGUL_KEY_GAP_DP,
                KeyboardPreferences.ENGLISH_KEY_GAP_DP,
                KeyboardPreferences.KEY_DEPTH_ENABLED,
                KeyboardPreferences.KEY_DEPTH_DP,
                KeyboardPreferences.CUSTOM_DEPTH_COLOR_ENABLED,
                KeyboardPreferences.DEPTH_COLOR,
                KeyboardPreferences.FONT_FAMILY,
                KeyboardPreferences.PRIMARY_TEXT_SIZE_PERCENT,
                KeyboardPreferences.SECONDARY_TEXT_SIZE_PERCENT,
                KeyboardPreferences.PRIMARY_TEXT_BOLD,
                KeyboardPreferences.PRIMARY_TEXT_ITALIC,
                KeyboardPreferences.SECONDARY_TEXT_BOLD,
                KeyboardPreferences.SECONDARY_TEXT_ITALIC,
                KeyboardPreferences.FOLLOW_THEME_TYPOGRAPHY,
                KeyboardPreferences.POINT_KEYCAP_STYLE_ENABLED,
                KeyboardPreferences.MOTION_EFFECT_LEVEL);

        add(entries, Section.INPUT_FEEL,
                KeyboardPreferences.HAPTIC_FEEDBACK_ENABLED,
                KeyboardPreferences.HIT_SLOP_DP,
                KeyboardPreferences.GESTURE_THRESHOLD_DP,
                KeyboardPreferences.TOUCH_Y_OFFSET_DP,
                KeyboardPreferences.REPEAT_START_DELAY_MS,
                KeyboardPreferences.REPEAT_INTERVAL_MS,
                KeyboardPreferences.SINGLE_TAP_COMMIT_MODE_ENABLED,
                KeyboardPreferences.SINGLE_TAP_START_HOLD_MS,
                KeyboardPreferences.SINGLE_TAP_COMMIT_HOLD_MS,
                KeyboardPreferences.ENGLISH_DOUBLE_SPACE_PERIOD_ENABLED,
                KeyboardPreferences.SHOW_HANGUL_SLIDE_HINTS,
                KeyboardPreferences.SHOW_ENGLISH_SLIDE_HINTS,
                KeyboardPreferences.SHOW_HANGUL_CONSONANT_SLIDE_HINTS,
                KeyboardPreferences.SHOW_HANGUL_VOWEL_SLIDE_HINTS,
                KeyboardPreferences.SHOW_SPACEBAR_SLIDE_HINTS,
                KeyboardPreferences.SHOW_BEGINNER_TOOLTIP_PREVIEW,
                KeyboardPreferences.SHOW_CONSONANT_PREVIEW,
                KeyboardPreferences.SHOW_VOWEL_PREVIEW,
                KeyboardPreferences.HAPTIC_TICK_DURATION_MS,
                KeyboardPreferences.HAPTIC_TICK_GAP_MS,
                KeyboardPreferences.DIFFERENTIATED_HAPTIC_ENABLED,
                KeyboardPreferences.TOUCH_BIAS_AUTO_CORRECTION_ENABLED,
                KeyboardPreferences.PALM_REJECTION_ENABLED,
                KeyboardPreferences.DINGUL_VOWEL_GESTURE_PROFILE,
                KeyboardPreferences.INPUT_ASSISTANCE_MODE,
                KeyboardPreferences.SPACEBAR_CURSOR_DEAD_ZONE_DP);

        add(entries, Section.THEME,
                KeyboardPreferences.ACCENT_PLACEMENT_MODE,
                KeyboardPreferences.ACCENT_PLACEMENT_TARGETS,
                KeyboardPreferences.KEY_COLOR_OVERRIDES,
                KeyboardPreferences.LEGEND_STYLE_PRESET,
                KeyboardPreferences.MODIFIER_ICON_THEME_PACK_ID,
                KeyboardPreferences.MODIFIER_ICON_OVERRIDE_PACK_ID,
                KeyboardPreferences.KEY_DISPLAY_THEME_PACK_ID,
                KeyboardPreferences.KEY_DISPLAY_OVERRIDE_PACK_ID,
                KeyboardPreferences.KEY_DISPLAY_OVERRIDES,
                KeyboardPreferences.VISUAL_EFFECTS,
                KeyboardPreferences.SELECTED_THEME_ID);

        add(entries, Section.REMOTE, StorageRisk.COMPATIBILITY, true,
                KeyboardPreferences.REMOTE_MODE_ENABLED,
                KeyboardPreferences.REMOTE_KEY_PRESET,
                KeyboardPreferences.REMOTE_IME_SHORTCUT,
                KeyboardPreferences.REMOTE_AUTO_MODE_ENABLED,
                KeyboardPreferences.REMOTE_AUTO_MODE_PACKAGES,
                KeyboardPreferences.APP_PROFILE_ASCII_PACKAGES,
                KeyboardPreferences.APP_PROFILE_NUMBER_ROW_PACKAGES,
                KeyboardPreferences.APP_PROFILE_NO_COMPOSING_PACKAGES,
                KeyboardPreferences.APP_PROFILE_NO_TEXT_CONVENIENCES_PACKAGES);

        add(entries, Section.RESERVED_PHRASES, StorageRisk.LOCAL_TEXT, true,
                KeyboardPreferences.RESERVED_TAP_TEXT,
                KeyboardPreferences.RESERVED_LEFT_TEXT,
                KeyboardPreferences.RESERVED_RIGHT_TEXT,
                KeyboardPreferences.RESERVED_UP_TEXT);

        add(entries, Section.PRIVACY_DEBUG, StorageRisk.LOCAL_TEXT, true,
                KeyboardPreferences.CLIPBOARD_HISTORY_ENABLED);
        add(entries, Section.PRIVACY_DEBUG, StorageRisk.LOCAL_DIAGNOSTIC, true,
                KeyboardPreferences.DEBUG_KEY_BOUNDS_OVERLAY_ENABLED,
                KeyboardPreferences.DEBUG_SHOW_RESOLVER_SCORES,
                KeyboardPreferences.INPUT_LEARNING_EPOCH,
                KeyboardPreferences.SHOW_CURRENT_APP_PROFILE);
        add(entries, Section.PRIVACY_DEBUG, StorageRisk.LOCAL_TEXT, false,
                ClipboardStore.KEY_ENTRIES,
                TouchBiasStore.TYPING_EVENT_JOURNAL);
        add(entries, Section.PRIVACY_DEBUG, StorageRisk.LOCAL_DIAGNOSTIC, false,
                TouchBiasStore.TYPING_PATTERN_LOG);
        add(entries, Section.PRIVACY_DEBUG, StorageRisk.LOCAL_DIAGNOSTIC, false,
                TouchBiasStore.TOUCH_BIAS_STATS,
                TouchBiasStore.DINGUL_TOUCH_PROFILE);
        add(entries, Section.PRIVACY_DEBUG, StorageRisk.COMPATIBILITY, false,
                RemoteCompatibilityLog.KEY_ENTRIES);

        add(entries, Section.ERGONOMICS,
                KeyboardPreferences.ERGONOMIC_MAIN_KEY_CENTERING_ENABLED,
                KeyboardPreferences.ERGONOMIC_COMPACT_FUNCTION_RAIL_ENABLED,
                KeyboardPreferences.ERGONOMIC_HITBOX_ENABLED,
                KeyboardPreferences.ERGONOMIC_POSITION_ADJUST_ENABLED,
                KeyboardPreferences.ERGONOMIC_LEFT_ASSIST_RAIL_ENABLED,
                KeyboardPreferences.ERGONOMIC_UNIFORM_GRID_GAP_ENABLED,
                KeyboardPreferences.ERGONOMIC_VISUAL_CONSISTENCY_LEVEL);

        return Collections.unmodifiableList(entries);
    }

    private static void add(List<Entry> entries, Section section, String... keys) {
        add(entries, section, StorageRisk.NONE, true, keys);
    }

    private static void add(
            List<Entry> entries,
            Section section,
            StorageRisk risk,
            boolean userFacing,
            String... keys) {
        for (String key : keys) {
            entries.add(new Entry(key, section, risk, userFacing));
        }
    }
}
