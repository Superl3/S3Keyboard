package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class ClipboardPanelController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Supplier<EditorInputPolicy> editorPolicy;
    private final Consumer<String> clipboardTextCommitter;
    private final ClipboardStore store;
    private final ClipboardManager clipboardManager;
    private final ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    private LinearLayout toolbarLayout;
    private ImageButton clipboardButton;
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
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardListener = this::capturePrimaryClipboard;
    }

    LinearLayout createToolbar() {
        toolbarLayout = SettingsRowBuilder.horizontal(context);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setBackgroundColor(RuntimeDefaults.keyboardSettingsFrom(settings).keyboardBackgroundColor);

        clipboardButton = createClipboardButton();

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
        EditorInputPolicy currentPolicy = RuntimeDefaults.editorInputPolicyFrom(editorPolicy);
        ClipboardPanelAccess.Result result = ClipboardPanelAccess.resolve(
                clipboardView != null,
                store.isEnabled(),
                currentPolicy.password,
                clipboardView != null && clipboardView.getVisibility() == View.VISIBLE);
        if (result != ClipboardPanelAccess.Result.SHOW
                && result != ClipboardPanelAccess.Result.HIDE) {
            if (clipboardView != null) {
                clipboardView.setVisibility(View.GONE);
            }
            showUnavailableMessage(result);
            return;
        }
        if (result == ClipboardPanelAccess.Result.HIDE) {
            clipboardView.setVisibility(View.GONE);
            return;
        }
        clipboardView.refresh();
        clipboardView.setVisibility(View.VISIBLE);
    }

    private void showUnavailableMessage(ClipboardPanelAccess.Result result) {
        int messageResId;
        if (result == ClipboardPanelAccess.Result.DISABLED) {
            messageResId = R.string.clipboard_history_disabled;
        } else if (result == ClipboardPanelAccess.Result.SECURE_FIELD) {
            messageResId = R.string.clipboard_secure_field;
        } else {
            messageResId = R.string.clipboard_unavailable;
        }
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
    }

    void updateVisibility() {
        if (toolbarLayout == null) {
            return;
        }
        EditorInputPolicy currentPolicy = RuntimeDefaults.editorInputPolicyFrom(editorPolicy);
        boolean clipboardEnabled = store.isEnabled() && !currentPolicy.password;
        toolbarLayout.setVisibility(clipboardEnabled ? View.VISIBLE : View.GONE);
        if (clipboardButton != null) {
            clipboardButton.setVisibility(clipboardEnabled ? View.VISIBLE : View.GONE);
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
            clipboardButton.setImageTintList(ColorStateList.valueOf(
                    toolbarForegroundColor(currentSettings)));
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

    private View createSpacer() {
        View spacer = new View(context);
        spacer.setLayoutParams(SettingsRowBuilder.weightedSpacer());
        return spacer;
    }

    private ImageButton createClipboardButton() {
        KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings);
        ImageButton button = new ImageButton(context);
        button.setImageResource(R.drawable.ic_keyboard_clipboard);
        button.setContentDescription(context.getString(R.string.clipboard_toolbar_button));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setImageTintList(ColorStateList.valueOf(toolbarForegroundColor(currentSettings)));
        button.setPadding(
                SettingsRowBuilder.dp(context, 12),
                SettingsRowBuilder.dp(context, 12),
                SettingsRowBuilder.dp(context, 12),
                SettingsRowBuilder.dp(context, 12));
        button.setOnClickListener(view -> toggle());
        LinearLayout.LayoutParams params = SettingsRowBuilder.fixedSize(context, 48, 48);
        params.weight = 0;
        button.setLayoutParams(params);
        return button;
    }

    private int toolbarForegroundColor(KeyboardSettings currentSettings) {
        return KeyboardColorMath.contrastTextColor(
                currentSettings.keyboardBackgroundColor,
                147);
    }

}
