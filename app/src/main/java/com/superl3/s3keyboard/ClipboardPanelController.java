package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

final class ClipboardPanelController {
    interface Host {
        KeyboardSettings settings();

        EditorInputPolicy editorPolicy();

        void commitClipboardText(String text);
    }

    private final Context context;
    private final FloatingModeController floatingModeController;
    private final Host host;
    private final ClipboardStore store;
    private final ClipboardManager clipboardManager;
    private final ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    private LinearLayout toolbarLayout;
    private View dragHandle;
    private Button clipboardButton;
    private ClipboardView clipboardView;
    private TextView remoteIndicator;
    private boolean clipboardListenerRegistered;

    ClipboardPanelController(Context context, FloatingModeController floatingModeController, Host host) {
        this.context = context;
        this.floatingModeController = floatingModeController;
        this.host = host;
        this.store = new ClipboardStore(context);
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardListener = this::capturePrimaryClipboard;
    }

    LinearLayout createToolbar() {
        toolbarLayout = new LinearLayout(context);
        toolbarLayout.setOrientation(LinearLayout.HORIZONTAL);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setBackgroundColor(settings().keyboardBackgroundColor);

        dragHandle = new View(context);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        48,
                        context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        8,
                        context.getResources().getDisplayMetrics()));
        handleParams.weight = 0;
        handleParams.gravity = Gravity.CENTER;
        handleParams.setMargins(0, 8, 0, 8);
        dragHandle.setLayoutParams(handleParams);
        dragHandle.setBackgroundColor(Color.LTGRAY);
        dragHandle.setOnTouchListener((v, event) ->
                floatingModeController != null && floatingModeController.onHandleTouch(v, event));

        View spacer1 = new View(context);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        View spacer2 = new View(context);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));

        clipboardButton = new Button(context);
        clipboardButton.setText(R.string.clipboard_toolbar_button);
        clipboardButton.setBackgroundColor(Color.TRANSPARENT);
        clipboardButton.setTextColor(settings().keyIdleColor);
        clipboardButton.setOnClickListener(v -> toggle());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.weight = 0;
        clipboardButton.setLayoutParams(buttonParams);

        remoteIndicator = new TextView(context);
        remoteIndicator.setText("");
        remoteIndicator.setTextSize(12);
        remoteIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        remoteIndicator.setTextColor(contrastColor(settings().keyboardBackgroundColor));
        remoteIndicator.setGravity(Gravity.CENTER);
        remoteIndicator.setPadding(dp(10), 0, dp(10), 0);

        toolbarLayout.addView(spacer1);
        toolbarLayout.addView(remoteIndicator);
        toolbarLayout.addView(dragHandle);
        toolbarLayout.addView(spacer2);
        toolbarLayout.addView(clipboardButton);
        updateVisibility();
        return toolbarLayout;
    }

    ClipboardView createOverlayView() {
        clipboardView = new ClipboardView(
                context,
                store,
                () -> clipboardView.setVisibility(View.GONE),
                text -> {
                    if (host != null) {
                        host.commitClipboardText(text);
                    }
                });
        clipboardView.setVisibility(View.GONE);
        return clipboardView;
    }

    FrameLayout.LayoutParams overlayLayoutParams() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
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
        boolean floatingEnabled = false;
        boolean clipboardEnabled = store.isEnabled() && !editorPolicy().password;
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
            remoteIndicator.setTextColor(contrastColor(settings().keyboardBackgroundColor));
        }
        if (!clipboardEnabled && clipboardView != null) {
            clipboardView.setVisibility(View.GONE);
        }
    }

    void updateAppearance() {
        if (toolbarLayout != null) {
            toolbarLayout.setBackgroundColor(settings().keyboardBackgroundColor);
        }
        if (clipboardButton != null) {
            clipboardButton.setTextColor(settings().keyIdleColor);
        }
        if (remoteIndicator != null) {
            remoteIndicator.setTextColor(contrastColor(settings().keyboardBackgroundColor));
        }
    }

    void updateClipboardListener() {
        if (clipboardManager == null) {
            return;
        }
        boolean shouldRegister = store.isEnabled() && !editorPolicy().password;
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
                || editorPolicy().password) {
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

    private KeyboardSettings settings() {
        KeyboardSettings settings = host == null ? null : host.settings();
        return settings == null ? KeyboardSettings.defaults() : settings;
    }

    private EditorInputPolicy editorPolicy() {
        EditorInputPolicy policy = host == null ? null : host.editorPolicy();
        return policy == null ? EditorInputPolicy.DEFAULT : policy;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private int contrastColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.58 ? 0xFF111827 : 0xFFFFFFFF;
    }
}
