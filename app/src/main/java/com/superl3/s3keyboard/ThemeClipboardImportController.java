package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

final class ThemeClipboardImportController {
    interface Host {
        KeyboardSettings currentSettings();

        String enterKeyLabel();

        boolean forceNumberRow();

        void applyImportedSettings(KeyboardSettings settings);

        void dismissQuickSettings();
    }

    interface ClipboardTextReader {
        String read();
    }

    interface SettingsStore {
        void save(KeyboardSettings settings);
    }

    interface Notifier {
        void show(int messageResId);
    }

    private final Host host;
    private final ClipboardTextReader clipboardTextReader;
    private final SettingsStore settingsStore;
    private final Notifier notifier;

    ThemeClipboardImportController(Context context, Host host) {
        this(
                host,
                new AndroidClipboardTextReader(context),
                settings -> {
                    KeyboardPreferences.saveSelectedThemeId(context, "");
                    KeyboardPreferences.saveSettings(context, settings);
                },
                messageResId -> Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show());
    }

    ThemeClipboardImportController(
            Host host,
            ClipboardTextReader clipboardTextReader,
            SettingsStore settingsStore,
            Notifier notifier) {
        this.host = host;
        this.clipboardTextReader = clipboardTextReader;
        this.settingsStore = settingsStore;
        this.notifier = notifier;
    }

    void importFromClipboard() {
        String text = clipboardTextReader.read();
        String json = text == null ? "" : text.trim();
        if (json.isEmpty()) {
            notifier.show(R.string.clipboard_theme_empty);
            return;
        }
        try {
            KeyboardSettings imported = KeyboardThemeJson.importTheme(host.currentSettings(), json)
                    .withEnterKeyLabel(host.enterKeyLabel())
                    .withRuntimeNumberRowForced(host.forceNumberRow());
            settingsStore.save(imported);
            host.applyImportedSettings(imported);
            notifier.show(R.string.clipboard_theme_imported);
            host.dismissQuickSettings();
        } catch (IllegalArgumentException exception) {
            notifier.show(R.string.clipboard_theme_invalid);
        }
    }

    private static final class AndroidClipboardTextReader implements ClipboardTextReader {
        private final Context context;

        AndroidClipboardTextReader(Context context) {
            this.context = context;
        }

        @Override
        public String read() {
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
            return text == null ? "" : text.toString().trim();
        }
    }
}
