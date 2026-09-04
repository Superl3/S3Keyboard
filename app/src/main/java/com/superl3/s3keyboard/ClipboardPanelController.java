package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ClipboardPanelController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<EditorInputPolicy> editorPolicy;
    private final Consumer<String> clipboardTextCommitter;
    private final ClipboardStore store;
    private final TextToolsStore textToolsStore;
    private final ClipboardManager clipboardManager;
    private final ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    private ClipboardView clipboardView;
    private boolean clipboardListenerRegistered;

    ClipboardPanelController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Supplier<EditorInputPolicy> editorPolicy,
            Consumer<String> clipboardTextCommitter) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.editorPolicy = RuntimeDefaults.editorInputPolicySupplier(editorPolicy);
        this.clipboardTextCommitter = RuntimeDefaults.stringConsumer(clipboardTextCommitter);
        this.store = new ClipboardStore(context);
        this.textToolsStore = new TextToolsStore(context);
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardListener = this::capturePrimaryClipboard;
    }

    ClipboardView createOverlayView() {
        clipboardView = new ClipboardView(
                context,
                store,
                textToolsStore,
                () -> clipboardView.setVisibility(View.GONE),
                clipboardTextCommitter);
        clipboardView.setVisibility(View.GONE);
        return clipboardView;
    }

    FrameLayout.LayoutParams overlayLayoutParams() {
        return SettingsRowBuilder.frameMatchMatch();
    }

    void toggle() {
        boolean allowed = textToolsAllowed();
        if (!allowed || clipboardView == null) {
            if (clipboardView != null) {
                clipboardView.setVisibility(View.GONE);
            }
            Toast.makeText(
                    context,
                    allowed ? R.string.clipboard_unavailable : R.string.text_tools_sensitive_field,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (clipboardView.getVisibility() == View.VISIBLE) {
            clipboardView.setVisibility(View.GONE);
            return;
        }
        clipboardView.refresh();
        clipboardView.setVisibility(View.VISIBLE);
    }

    private boolean textToolsAllowed() {
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        return TextToolsPolicy.allows(
                RuntimeDefaults.editorInputPolicyFrom(editorPolicy),
                currentSettings.remoteModeEnabled);
    }

    void updateVisibility() {
        if (!textToolsAllowed() && clipboardView != null) {
            clipboardView.setVisibility(View.GONE);
        }
    }

    void updateClipboardListener() {
        if (clipboardManager == null) {
            return;
        }
        boolean shouldRegister = store.isEnabled() && textToolsAllowed();
        if (shouldRegister && !clipboardListenerRegistered) {
            clipboardManager.addPrimaryClipChangedListener(clipboardListener);
            clipboardListenerRegistered = true;
        } else if (!shouldRegister && clipboardListenerRegistered) {
            removeClipboardListener();
        }
    }

    void removeClipboardListener() {
        if (clipboardManager != null && clipboardListenerRegistered) {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener);
            clipboardListenerRegistered = false;
        }
    }

    private void capturePrimaryClipboard() {
        if (clipboardManager == null
                || !store.isEnabled()
                || !textToolsAllowed()) {
            return;
        }
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(context);
        if (text == null) {
            return;
        }
        store.add(text.toString());
        if (clipboardView != null && clipboardView.getVisibility() == View.VISIBLE) {
            clipboardView.refresh();
        }
    }

}
