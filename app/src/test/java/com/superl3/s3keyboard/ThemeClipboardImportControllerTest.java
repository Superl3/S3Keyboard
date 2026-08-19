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

    @Test
    public void sessionOnlyModeRemoteAndForcedNumberRowAreNotPersistedWithTheme() {
        KeyboardSettings persisted = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withRemoteOptions(false, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.ALT_SHIFT)
                .withHangulNumberRow(false)
                .withEnglishNumberRow(false)
                .withEnterKeyLabel("Persisted");
        KeyboardSettings runtime = persisted
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withRemoteOptions(true, RemoteKeyPreset.PC_KEYBOARD, RemoteImeShortcut.WIN_SPACE)
                .withRuntimeNumberRowForced(true)
                .withEnterKeyLabel("Runtime");
        KeyboardSettings theme = KeyboardSettings.defaults().withKeyRoundness(12);
        FakeRuntime target = new FakeRuntime();
        FakeSettingsStore store = new FakeSettingsStore();
        ThemeClipboardImportController controller = new ThemeClipboardImportController(
                () -> persisted,
                () -> runtime,
                () -> "Search",
                () -> true,
                target::applyImportedSettings,
                target::dismissQuickSettings,
                () -> KeyboardThemeJson.exportTheme(theme, "Round", "test", null),
                store,
                messageResId -> { });

        controller.importFromClipboard();

        assertEquals(KeyboardMode.HANGUL, store.saved.keyboardMode);
        assertEquals(false, store.saved.remoteModeEnabled);
        assertEquals(false, store.saved.forceNumberRow);
        assertEquals(false, store.saved.showHangulNumberRow);
        assertEquals(false, store.saved.showEnglishNumberRow);
        assertEquals("Persisted", store.saved.enterKeyLabel);
        assertEquals(12, store.saved.keyRoundnessDp);

        assertEquals(KeyboardMode.ENGLISH, target.applied.keyboardMode);
        assertEquals(true, target.applied.remoteModeEnabled);
        assertEquals(RemoteImeShortcut.WIN_SPACE, target.applied.remoteImeShortcut);
        assertEquals(true, target.applied.forceNumberRow);
        assertEquals(true, target.applied.showNumberRow);
        assertEquals("Search", target.applied.enterKeyLabel);
        assertEquals(12, target.applied.keyRoundnessDp);
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
