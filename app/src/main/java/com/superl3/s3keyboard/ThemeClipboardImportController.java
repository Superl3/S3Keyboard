package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class ThemeClipboardImportController {
    private final Supplier<KeyboardSettings> storedSettings;
    private final Supplier<KeyboardSettings> currentSettings;
    private final Supplier<String> enterKeyLabel;
    private final BooleanSupplier forceNumberRow;
    private final Consumer<KeyboardSettings> runtimeSettingsApplier;
    private final Runnable dismissQuickSettings;
    private final Supplier<String> clipboardTextReader;
    private final Consumer<KeyboardSettings> settingsStore;
    private final IntConsumer notifier;

    ThemeClipboardImportController(
            Context context,
            Supplier<KeyboardSettings> currentSettings,
            Supplier<String> enterKeyLabel,
            BooleanSupplier forceNumberRow,
            Consumer<KeyboardSettings> runtimeSettingsApplier,
            Runnable dismissQuickSettings) {
        this(
                () -> KeyboardPreferences.load(context),
                currentSettings,
                enterKeyLabel,
                forceNumberRow,
                runtimeSettingsApplier,
                dismissQuickSettings,
                new AndroidClipboardTextReader(context),
                settings -> {
                    KeyboardPreferences.saveSelectedThemeId(context, "");
                    KeyboardPreferences.saveSettings(context, settings);
                },
                messageResId -> Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show());
    }

    ThemeClipboardImportController(
            Supplier<KeyboardSettings> currentSettings,
            Supplier<String> enterKeyLabel,
            BooleanSupplier forceNumberRow,
            Consumer<KeyboardSettings> runtimeSettingsApplier,
            Runnable dismissQuickSettings,
            Supplier<String> clipboardTextReader,
            Consumer<KeyboardSettings> settingsStore,
            IntConsumer notifier) {
        this(
                currentSettings,
                currentSettings,
                enterKeyLabel,
                forceNumberRow,
                runtimeSettingsApplier,
                dismissQuickSettings,
                clipboardTextReader,
                settingsStore,
                notifier);
    }

    ThemeClipboardImportController(
            Supplier<KeyboardSettings> storedSettings,
            Supplier<KeyboardSettings> currentSettings,
            Supplier<String> enterKeyLabel,
            BooleanSupplier forceNumberRow,
            Consumer<KeyboardSettings> runtimeSettingsApplier,
            Runnable dismissQuickSettings,
            Supplier<String> clipboardTextReader,
            Consumer<KeyboardSettings> settingsStore,
            IntConsumer notifier) {
        this.storedSettings = RuntimeDefaults.keyboardSettingsSupplier(storedSettings);
        this.currentSettings = RuntimeDefaults.keyboardSettingsSupplier(currentSettings);
        this.enterKeyLabel = RuntimeDefaults.nullStringSupplier(enterKeyLabel);
        this.forceNumberRow = RuntimeDefaults.booleanSupplier(forceNumberRow);
        this.runtimeSettingsApplier = RuntimeDefaults.keyboardSettingsConsumer(runtimeSettingsApplier);
        this.dismissQuickSettings = RuntimeDefaults.runnable(dismissQuickSettings);
        this.clipboardTextReader = RuntimeDefaults.emptyStringSupplier(clipboardTextReader);
        this.settingsStore = RuntimeDefaults.keyboardSettingsConsumer(settingsStore);
        this.notifier = RuntimeDefaults.intConsumer(notifier);
    }

    void importFromClipboard() {
        String json = RuntimeDefaults.trimmedStringOrEmptyFrom(clipboardTextReader);
        if (json.isEmpty()) {
            notifier.accept(R.string.clipboard_theme_empty);
            return;
        }
        try {
            KeyboardSettings persisted = RuntimeDefaults.keyboardSettingsFrom(storedSettings);
            KeyboardSettings runtime = RuntimeDefaults.keyboardSettingsFrom(currentSettings);
            KeyboardSettings imported = KeyboardThemeJson.importTheme(persisted, json);
            settingsStore.accept(imported);
            KeyboardSettings runtimeImported = RuntimeDefaults.withRuntimeImeState(
                    imported,
                    runtime,
                    enterKeyLabel.get(),
                    forceNumberRow.getAsBoolean());
            runtimeSettingsApplier.accept(runtimeImported);
            notifier.accept(R.string.clipboard_theme_imported);
            dismissQuickSettings.run();
        } catch (IllegalArgumentException exception) {
            notifier.accept(R.string.clipboard_theme_invalid);
        }
    }

    private static final class AndroidClipboardTextReader implements Supplier<String> {
        private final Context context;

        AndroidClipboardTextReader(Context context) {
            this.context = context;
        }

        @Override
        public String get() {
            ClipboardManager clipboard =
                    (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) {
                return "";
            }
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return "";
            }
            CharSequence text = clip.getItemAt(0).coerceToText(context);
            return RuntimeDefaults.trimmedStringOrEmpty(text);
        }
    }
}
