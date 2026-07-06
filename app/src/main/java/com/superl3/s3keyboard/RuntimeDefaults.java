package com.superl3.s3keyboard;

import android.content.Context;
import android.os.SystemClock;

import java.util.function.BooleanSupplier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

final class RuntimeDefaults {
    static final Runnable NO_OP_RUNNABLE = () -> { };
    static final BooleanSupplier TRUE_BOOLEAN = () -> true;
    static final BooleanSupplier FALSE_BOOLEAN = () -> false;
    static final Supplier<String> EMPTY_STRING = () -> "";
    static final Supplier<String> NULL_STRING = () -> null;
    static final Supplier<KeyboardMode> NULL_KEYBOARD_MODE = () -> null;
    static final Supplier<KeyboardSettings> DEFAULT_KEYBOARD_SETTINGS = KeyboardSettings::defaults;
    static final Supplier<KeyboardLayoutProfiles> DEFAULT_KEYBOARD_LAYOUT_PROFILES =
            KeyboardLayoutProfiles::defaults;
    static final Supplier<KeyboardErgonomicsOptions> DEFAULT_KEYBOARD_ERGONOMICS =
            () -> KeyboardErgonomicsOptions.DEFAULT;
    static final Supplier<AppInputProfile> STANDARD_APP_INPUT_PROFILE = () -> AppInputProfile.STANDARD;
    static final Supplier<EditorInputPolicy> DEFAULT_EDITOR_INPUT_POLICY = () -> EditorInputPolicy.DEFAULT;
    static final Supplier<RemoteImeShortcut> DEFAULT_REMOTE_IME_SHORTCUT =
            () -> RemoteImeShortcut.ALT_SHIFT;
    static final LongSupplier UPTIME_CLOCK = SystemClock::uptimeMillis;
    static final Consumer<Boolean> NO_OP_BOOLEAN_CONSUMER = enabled -> { };
    static final Consumer<String> NO_OP_STRING_CONSUMER = value -> { };
    static final IntConsumer NO_OP_INT_CONSUMER = value -> { };
    static final Consumer<HandednessMode> NO_OP_HANDEDNESS_CONSUMER = mode -> { };
    static final Consumer<KeyboardSettings> NO_OP_KEYBOARD_SETTINGS_CONSUMER = settings -> { };
    static final Consumer<KeyboardErgonomicsOptions> NO_OP_KEYBOARD_ERGONOMICS_CONSUMER =
            options -> { };
    static final Consumer<KeyboardLayoutProfile> NO_OP_KEYBOARD_LAYOUT_PROFILE_CONSUMER =
            profile -> { };
    static final BiConsumer<Integer, Integer> NO_OP_INTEGER_PAIR_CONSUMER =
            (first, second) -> { };
    static final BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions>
            NO_OP_KEYBOARD_SETTINGS_ERGONOMICS_CONSUMER = (settings, options) -> { };

    private RuntimeDefaults() {
    }

    static Runnable runnable(Runnable value) {
        return value == null ? NO_OP_RUNNABLE : value;
    }

    static BooleanSupplier booleanSupplier(BooleanSupplier value) {
        return value == null ? FALSE_BOOLEAN : value;
    }

    static BooleanSupplier trueBooleanSupplier(BooleanSupplier value) {
        return value == null ? TRUE_BOOLEAN : value;
    }

    static LongSupplier longSupplier(LongSupplier value) {
        return value == null ? UPTIME_CLOCK : value;
    }

    static Supplier<String> emptyStringSupplier(Supplier<String> value) {
        return value == null ? EMPTY_STRING : value;
    }

    static Supplier<String> nullStringSupplier(Supplier<String> value) {
        return value == null ? NULL_STRING : value;
    }

    static Supplier<KeyboardMode> keyboardModeSupplier(Supplier<KeyboardMode> value) {
        return value == null ? NULL_KEYBOARD_MODE : value;
    }

    static Supplier<KeyboardSettings> keyboardSettingsSupplier(Supplier<KeyboardSettings> value) {
        return value == null ? DEFAULT_KEYBOARD_SETTINGS : value;
    }

    static Supplier<KeyboardLayoutProfiles> keyboardLayoutProfilesSupplier(
            Supplier<KeyboardLayoutProfiles> value) {
        return value == null ? DEFAULT_KEYBOARD_LAYOUT_PROFILES : value;
    }

    static Supplier<EditorInputPolicy> editorInputPolicySupplier(Supplier<EditorInputPolicy> value) {
        return value == null ? DEFAULT_EDITOR_INPUT_POLICY : value;
    }

    static Supplier<AppInputProfile> appInputProfileSupplier(Supplier<AppInputProfile> value) {
        return value == null ? STANDARD_APP_INPUT_PROFILE : value;
    }

    static Supplier<RemoteImeShortcut> remoteImeShortcutSupplier(Supplier<RemoteImeShortcut> value) {
        return value == null ? DEFAULT_REMOTE_IME_SHORTCUT : value;
    }

    static Supplier<LocalDataControlsController> localDataControlsSupplier(
            Context context,
            Supplier<LocalDataControlsController> value) {
        return () -> localDataControls(context, value);
    }

    static Consumer<String> stringConsumer(Consumer<String> value) {
        return value == null ? NO_OP_STRING_CONSUMER : value;
    }

    static <T> Consumer<T> consumer(Consumer<T> value) {
        return value == null ? ignored -> { } : value;
    }

    static Consumer<Boolean> booleanConsumer(Consumer<Boolean> value) {
        return value == null ? NO_OP_BOOLEAN_CONSUMER : value;
    }

    static IntConsumer intConsumer(IntConsumer value) {
        return value == null ? NO_OP_INT_CONSUMER : value;
    }

    static Consumer<HandednessMode> handednessConsumer(Consumer<HandednessMode> value) {
        return value == null ? NO_OP_HANDEDNESS_CONSUMER : value;
    }

    static Consumer<KeyboardSettings> keyboardSettingsConsumer(Consumer<KeyboardSettings> value) {
        return value == null ? NO_OP_KEYBOARD_SETTINGS_CONSUMER : value;
    }

    static BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions> keyboardSettingsAndErgonomicsConsumer(
            BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions> value) {
        return value == null ? NO_OP_KEYBOARD_SETTINGS_ERGONOMICS_CONSUMER : value;
    }

    static BiConsumer<Integer, Integer> integerPairConsumer(BiConsumer<Integer, Integer> value) {
        return value == null ? NO_OP_INTEGER_PAIR_CONSUMER : value;
    }

    static Supplier<KeyboardErgonomicsOptions> keyboardErgonomicsSupplier(
            Supplier<KeyboardErgonomicsOptions> value) {
        return value == null ? DEFAULT_KEYBOARD_ERGONOMICS : value;
    }

    static Consumer<KeyboardErgonomicsOptions> keyboardErgonomicsConsumer(
            Consumer<KeyboardErgonomicsOptions> value) {
        return value == null ? NO_OP_KEYBOARD_ERGONOMICS_CONSUMER : value;
    }

    static Consumer<KeyboardLayoutProfile> keyboardLayoutProfileConsumer(
            Consumer<KeyboardLayoutProfile> value) {
        return value == null ? NO_OP_KEYBOARD_LAYOUT_PROFILE_CONSUMER : value;
    }

    static KeyboardSettings keyboardSettings(KeyboardSettings settings) {
        return settings == null ? KeyboardSettings.defaults() : settings;
    }

    static KeyboardSettings keyboardSettingsFrom(Supplier<KeyboardSettings> value) {
        return keyboardSettings(keyboardSettingsSupplier(value).get());
    }

    static KeyboardLayoutProfiles keyboardLayoutProfiles(KeyboardLayoutProfiles profiles) {
        return profiles == null ? KeyboardLayoutProfiles.defaults() : profiles;
    }

    static KeyboardLayoutProfiles keyboardLayoutProfilesFrom(Supplier<KeyboardLayoutProfiles> value) {
        return keyboardLayoutProfiles(keyboardLayoutProfilesSupplier(value).get());
    }

    static KeyboardErgonomicsOptions keyboardErgonomics(KeyboardErgonomicsOptions ergonomics) {
        return ergonomics == null ? KeyboardErgonomicsOptions.DEFAULT : ergonomics;
    }

    static KeyboardVisualEffects keyboardVisualEffects(KeyboardVisualEffects effects) {
        return effects == null ? KeyboardVisualEffects.DEFAULT : effects;
    }

    static KeyboardErgonomicsOptions keyboardErgonomicsFrom(
            Supplier<KeyboardErgonomicsOptions> value) {
        return keyboardErgonomics(keyboardErgonomicsSupplier(value).get());
    }

    static AppInputProfile appInputProfile(AppInputProfile profile) {
        return profile == null ? AppInputProfile.STANDARD : profile;
    }

    static AppInputProfileOverrides appInputProfileOverrides(AppInputProfileOverrides overrides) {
        return overrides == null ? AppInputProfileOverrides.EMPTY : overrides;
    }

    static EnglishQwertyCorrectionEngine englishQwertyCorrectionEngine(
            EnglishQwertyCorrectionEngine engine) {
        return engine == null ? EnglishQwertyCorrectionEngine.DEFAULT : engine;
    }

    static EditorInputPolicy editorInputPolicy(EditorInputPolicy policy) {
        return policy == null ? EditorInputPolicy.DEFAULT : policy;
    }

    static EditorInputPolicy editorInputPolicyFrom(Supplier<EditorInputPolicy> value) {
        return editorInputPolicy(editorInputPolicySupplier(value).get());
    }

    static KeyboardSurface keyboardSurface(KeyboardSurface surface) {
        return surface == null ? KeyboardSurface.NORMAL : surface;
    }

    static RemoteImeShortcut remoteImeShortcut(RemoteImeShortcut shortcut) {
        return shortcut == null ? RemoteImeShortcut.ALT_SHIFT : shortcut;
    }

    static LocalDataControlsController localDataControls(
            Context context,
            Supplier<LocalDataControlsController> value) {
        LocalDataControlsController controls = value == null ? null : value.get();
        return controls == null ? new LocalDataControlsController(context) : controls;
    }

    static KeyboardMode keyboardMode(KeyboardMode mode, KeyboardMode fallback) {
        return mode == null ? fallback : mode;
    }

    static KeyboardSettings withRuntimeImeState(
            KeyboardSettings settings,
            KeyboardMode keyboardMode,
            String enterKeyLabel,
            String fallbackEnterKeyLabel,
            boolean forceNumberRow) {
        KeyboardSettings safeSettings = keyboardSettings(settings);
        String safeEnterFallback = stringOrDefault(
                fallbackEnterKeyLabel,
                safeSettings.enterKeyLabel);
        return safeSettings
                .withKeyboardMode(keyboardMode(keyboardMode, safeSettings.keyboardMode))
                .withEnterKeyLabel(stringOrDefault(enterKeyLabel, safeEnterFallback))
                .withRuntimeNumberRowForced(forceNumberRow);
    }

    static String stringOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    static String stringOrEmpty(CharSequence value) {
        return value == null ? "" : value.toString();
    }

    static String trimmedStringOrEmpty(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }

    static String trimmedStringOrEmptyFrom(Supplier<String> value) {
        return trimmedStringOrEmpty(emptyStringSupplier(value).get());
    }
}
