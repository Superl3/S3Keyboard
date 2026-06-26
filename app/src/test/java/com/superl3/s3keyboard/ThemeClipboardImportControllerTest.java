package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public final class ThemeClipboardImportControllerTest {
    @Test
    public void validClipboardThemeImportsSavesAndDismisses() {
        KeyboardSettings theme = KeyboardSettings.defaults().withKeyRoundness(9);
        FakeHost host = new FakeHost();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                host,
                () -> KeyboardThemeJson.exportTheme(theme, "Round", "test", null),
                store,
                notifier);

        controller.importFromClipboard();

        assertEquals(9, host.applied.keyRoundnessDp);
        assertEquals(9, store.saved.keyRoundnessDp);
        assertEquals(host.enterKeyLabel(), host.applied.enterKeyLabel);
        assertEquals(true, host.applied.showNumberRow);
        assertEquals(R.string.clipboard_theme_imported, notifier.lastMessageResId);
        assertEquals(true, host.dismissed);
    }

    @Test
    public void emptyClipboardThemeReportsEmptyAndDoesNotSave() {
        FakeHost host = new FakeHost();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                host,
                () -> "   ",
                store,
                notifier);

        controller.importFromClipboard();

        assertNull(host.applied);
        assertNull(store.saved);
        assertEquals(R.string.clipboard_theme_empty, notifier.lastMessageResId);
        assertEquals(false, host.dismissed);
    }

    @Test
    public void invalidClipboardThemeReportsInvalidAndKeepsCurrentSettings() {
        FakeHost host = new FakeHost();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                host,
                () -> "{not json}",
                store,
                notifier);

        controller.importFromClipboard();

        assertNull(host.applied);
        assertNull(store.saved);
        assertEquals(R.string.clipboard_theme_invalid, notifier.lastMessageResId);
        assertEquals(false, host.dismissed);
    }

    private static final class FakeHost implements ThemeClipboardImportController.Host {
        KeyboardSettings applied;
        boolean dismissed;

        @Override
        public KeyboardSettings currentSettings() {
            return KeyboardSettings.defaults();
        }

        @Override
        public String enterKeyLabel() {
            return "검색";
        }

        @Override
        public boolean forceNumberRow() {
            return true;
        }

        @Override
        public void applyImportedSettings(KeyboardSettings settings) {
            applied = settings;
        }

        @Override
        public void dismissQuickSettings() {
            dismissed = true;
        }
    }

    private static final class FakeSettingsStore implements ThemeClipboardImportController.SettingsStore {
        KeyboardSettings saved;

        @Override
        public void save(KeyboardSettings settings) {
            saved = settings;
        }
    }

    private static final class FakeNotifier implements ThemeClipboardImportController.Notifier {
        int lastMessageResId;

        @Override
        public void show(int messageResId) {
            lastMessageResId = messageResId;
        }
    }
}
