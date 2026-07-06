package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.junit.Test;

public final class ThemeClipboardImportControllerTest {
    @Test
    public void validClipboardThemeImportsSavesAndDismisses() {
        KeyboardSettings theme = KeyboardSettings.defaults().withKeyRoundness(9);
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                runtime::currentSettings,
                runtime::enterKeyLabel,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> KeyboardThemeJson.exportTheme(theme, "Round", "test", null),
                store,
                notifier);

        controller.importFromClipboard();

        assertEquals(9, runtime.applied.keyRoundnessDp);
        assertEquals(9, store.saved.keyRoundnessDp);
        assertEquals(runtime.enterKeyLabel(), runtime.applied.enterKeyLabel);
        assertEquals(true, runtime.applied.showNumberRow);
        assertEquals(R.string.clipboard_theme_imported, notifier.lastMessageResId);
        assertEquals(true, runtime.dismissed);
    }

    @Test
    public void emptyClipboardThemeReportsEmptyAndDoesNotSave() {
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                runtime::currentSettings,
                runtime::enterKeyLabel,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> "   ",
                store,
                notifier);

        controller.importFromClipboard();

        assertNull(runtime.applied);
        assertNull(store.saved);
        assertEquals(R.string.clipboard_theme_empty, notifier.lastMessageResId);
        assertEquals(false, runtime.dismissed);
    }

    @Test
    public void nullClipboardThemeReportsEmptyAndDoesNotSave() {
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                runtime::currentSettings,
                runtime::enterKeyLabel,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> null,
                store,
                notifier);

        controller.importFromClipboard();

        assertNull(runtime.applied);
        assertNull(store.saved);
        assertEquals(R.string.clipboard_theme_empty, notifier.lastMessageResId);
        assertEquals(false, runtime.dismissed);
    }

    @Test
    public void clipboardThemeTrimsBeforeImporting() {
        KeyboardSettings theme = KeyboardSettings.defaults().withKeyRoundness(11);
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                runtime::currentSettings,
                runtime::enterKeyLabel,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> "  \n" + KeyboardThemeJson.exportTheme(theme, "Round", "test", null) + "\n  ",
                store,
                notifier);

        controller.importFromClipboard();

        assertEquals(11, runtime.applied.keyRoundnessDp);
        assertEquals(11, store.saved.keyRoundnessDp);
        assertEquals(R.string.clipboard_theme_imported, notifier.lastMessageResId);
    }

    @Test
    public void missingRuntimeEnterLabelFallsBackToCurrentSettingsLabel() {
        KeyboardSettings current = KeyboardSettings.defaults().withEnterKeyLabel("FallbackEnter");
        KeyboardSettings theme = KeyboardSettings.defaults().withKeyRoundness(5);
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                () -> current,
                () -> null,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> KeyboardThemeJson.exportTheme(theme, "Round", "test", null),
                store,
                notifier);

        controller.importFromClipboard();

        assertEquals("FallbackEnter", runtime.applied.enterKeyLabel);
        assertEquals("FallbackEnter", store.saved.enterKeyLabel);
    }

    @Test
    public void invalidClipboardThemeReportsInvalidAndKeepsCurrentSettings() {
        FakeRuntime runtime = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        FakeNotifier notifier = new FakeNotifier();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                runtime::currentSettings,
                runtime::enterKeyLabel,
                runtime::forceNumberRow,
                runtime::applyImportedSettings,
                runtime::dismissQuickSettings,
                () -> "{not json}",
                store,
                notifier);

        controller.importFromClipboard();

        assertNull(runtime.applied);
        assertNull(store.saved);
        assertEquals(R.string.clipboard_theme_invalid, notifier.lastMessageResId);
        assertEquals(false, runtime.dismissed);
    }

    private static final class FakeRuntime {
        KeyboardSettings applied;
        boolean dismissed;

        KeyboardSettings currentSettings() {
            return KeyboardSettings.defaults();
        }

        String enterKeyLabel() {
            return "검색";
        }

        boolean forceNumberRow() {
            return true;
        }

        void applyImportedSettings(KeyboardSettings settings) {
            applied = settings;
        }

        void dismissQuickSettings() {
            dismissed = true;
        }
    }

    private static final class FakeSettingsStore implements Consumer<KeyboardSettings> {
        KeyboardSettings saved;

        @Override
        public void accept(KeyboardSettings settings) {
            saved = settings;
        }
    }

    private static final class FakeNotifier implements IntConsumer {
        int lastMessageResId;

        @Override
        public void accept(int messageResId) {
            lastMessageResId = messageResId;
        }
    }
}
