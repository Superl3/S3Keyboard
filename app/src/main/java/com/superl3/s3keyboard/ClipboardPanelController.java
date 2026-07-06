package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ClipboardPanelController {
    private final Context context;
    private final FloatingModeController floatingModeController;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<EditorInputPolicy> editorPolicy;
    private final Consumer<String> clipboardTextCommitter;
    private final ClipboardStore store;
    private final ClipboardManager clipboardManager;
    private final ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    private LinearLayout toolbarLayout;
    private View dragHandle;
    private Button clipboardButton;
    private ClipboardView clipboardView;
    private TextView remoteIndicator;
    private boolean clipboardListenerRegistered;

    ClipboardPanelController(
            Context context,
            FloatingModeController floatingModeController,
            Supplier<KeyboardSettings> settings,
            Supplier<EditorInputPolicy> editorPolicy,
            Consumer<String> clipboardTextCommitter) {
        this.context = context;
        this.floatingModeController = floatingModeController;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.editorPolicy = RuntimeDefaults.editorInputPolicySupplier(editorPolicy);
        this.clipboardTextCommitter = RuntimeDefaults.stringConsumer(clipboardTextCommitter);
        this.store = new ClipboardStore(context);
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardListener = this::capturePrimaryClipboard;
    }

    LinearLayout createToolbar() {
        toolbarLayout = SettingsRowBuilder.horizontal(context);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setBackgroundColor(RuntimeDefaults.keyboardSettingsFrom(settings).keyboardBackgroundColor);

        dragHandle = createDragHandle();
        clipboardButton = createClipboardButton();
        remoteIndicator = createRemoteIndicator();

        toolbarLayout.addView(createSpacer());
        toolbarLayout.addView(remoteIndicator);
        toolbarLayout.addView(dragHandle);
        toolbarLayout.addView(createSpacer());
        toolbarLayout.addView(clipboardButton);
        updateVisibility();
        return toolbarLayout;
    }

    ClipboardView createOverlayView() {
        clipboardView = new ClipboardView(
                context,
                store,
                () -> clipboardView.setVisibility(View.GONE),
                clipboardTextCommitter);
        clipboardView.setVisibility(View.GONE);
        return clipboardView;
    }

    FrameLayout.LayoutParams overlayLayoutParams() {
        return SettingsRowBuilder.frameMatchMatch();
    }

    void toggle() {
        if (clipboardView == null) {
            return;
        }
        boolean show = clipboardView.getVisibility() != View.VISIBLE;
        if (show) {
            clipboardView.refresh();
        }
        clipboardView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    void updateVisibility() {
        if (toolbarLayout == null || floatingModeController == null) {
            return;
        }
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        EditorInputPolicy currentPolicy = RuntimeDefaults.editorInputPolicyFrom(editorPolicy);
        boolean floatingEnabled = false;
        boolean clipboardEnabled = store.isEnabled() && !currentPolicy.password;
        boolean showToolbar = floatingEnabled || clipboardEnabled;
        toolbarLayout.setVisibility(showToolbar ? View.VISIBLE : View.GONE);
        if (dragHandle != null) {
            dragHandle.setVisibility(floatingEnabled ? View.VISIBLE : View.INVISIBLE);
        }
        if (clipboardButton != null) {
            clipboardButton.setVisibility(clipboardEnabled ? View.VISIBLE : View.GONE);
        }
        if (remoteIndicator != null) {
            remoteIndicator.setVisibility(View.GONE);
            remoteIndicator.setTextColor(toolbarTextColor(currentSettings));
        }
        if (!clipboardEnabled && clipboardView != null) {
            clipboardView.setVisibility(View.GONE);
        }
    }

    void updateAppearance() {
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        if (toolbarLayout != null) {
            toolbarLayout.setBackgroundColor(currentSettings.keyboardBackgroundColor);
        }
        if (clipboardButton != null) {
            clipboardButton.setTextColor(currentSettings.keyIdleColor);
        }
        if (remoteIndicator != null) {
            remoteIndicator.setTextColor(toolbarTextColor(currentSettings));
        }
    }

    void updateClipboardListener() {
        if (clipboardManager == null) {
            return;
        }
        boolean shouldRegister = store.isEnabled()
                && !RuntimeDefaults.editorInputPolicyFrom(editorPolicy).password;
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
                || RuntimeDefaults.editorInputPolicyFrom(editorPolicy).password) {
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

    private View createDragHandle() {
        View handle = new View(context);
        LinearLayout.LayoutParams params = SettingsRowBuilder.fixedSize(context, 48, 8);
        params.weight = 0;
        params.gravity = Gravity.CENTER;
        params.setMargins(0, SettingsRowBuilder.dp(context, 8), 0, SettingsRowBuilder.dp(context, 8));
        handle.setLayoutParams(params);
        handle.setBackgroundColor(Color.LTGRAY);
        handle.setOnTouchListener((v, event) ->
                floatingModeController != null && floatingModeController.onHandleTouch(v, event));
        return handle;
    }

    private View createSpacer() {
        View spacer = new View(context);
        spacer.setLayoutParams(SettingsRowBuilder.weightedSpacer());
        return spacer;
    }

    private Button createClipboardButton() {
        Button button = SettingsRowBuilder.button(
                context,
                R.string.clipboard_toolbar_button,
                v -> toggle());
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setTextColor(RuntimeDefaults.keyboardSettingsFrom(settings).keyIdleColor);
        LinearLayout.LayoutParams params = SettingsRowBuilder.wrapContent();
        params.weight = 0;
        button.setLayoutParams(params);
        return button;
    }

    private TextView createRemoteIndicator() {
        TextView indicator = SettingsRowBuilder.label(context, "");
        indicator.setTextSize(12);
        indicator.setTypeface(Typeface.DEFAULT_BOLD);
        indicator.setTextColor(toolbarTextColor(RuntimeDefaults.keyboardSettingsFrom(settings)));
        indicator.setGravity(Gravity.CENTER);
        indicator.setPadding(
                SettingsRowBuilder.dp(context, 10),
                0,
                SettingsRowBuilder.dp(context, 10),
                0);
        return indicator;
    }

    private int toolbarTextColor(KeyboardSettings currentSettings) {
        return KeyboardColorMath.contrastTextColor(
                currentSettings.keyboardBackgroundColor,
                147);
    }

}
