package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

final class EnglishSuggestionStripController {
    interface Host {
        void acceptSuggestion(String suggestion);
    }

    private static final int SLOT_COUNT = 3;
    private final Context context;
    private final Host host;
    private final TextView[] slots = new TextView[SLOT_COUNT];
    private LinearLayout root;

    EnglishSuggestionStripController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    LinearLayout createView() {
        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(6), dp(3), dp(6), dp(3));
        for (int i = 0; i < SLOT_COUNT; i++) {
            final TextView slot = new TextView(context);
            slot.setGravity(Gravity.CENTER);
            slot.setSingleLine(true);
            slot.setTextSize(13);
            slot.setTypeface(Typeface.DEFAULT_BOLD);
            slot.setPadding(dp(8), 0, dp(8), 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    dp(32),
                    1f);
            if (i > 0) {
                params.setMargins(dp(4), 0, 0, 0);
            }
            root.addView(slot, params);
            slots[i] = slot;
        }
        root.setVisibility(View.GONE);
        return root;
    }

    void update(
            KeyboardSettings settings,
            boolean active,
            List<EnglishQwertyCorrectionEngine.Candidate> candidates) {
        if (root == null) {
            return;
        }
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        List<EnglishQwertyCorrectionEngine.Candidate> safeCandidates =
                candidates == null ? Collections.emptyList() : candidates;
        boolean visible = active && !safeCandidates.isEmpty();
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
        root.setBackgroundColor(safeSettings.keyboardBackgroundColor);
        for (int i = 0; i < slots.length; i++) {
            TextView slot = slots[i];
            if (!visible || i >= safeCandidates.size()) {
                slot.setText("");
                slot.setOnClickListener(null);
                slot.setVisibility(View.INVISIBLE);
                continue;
            }
            final String suggestion = safeCandidates.get(i).text;
            slot.setVisibility(View.VISIBLE);
            slot.setText(suggestion);
            slot.setTextColor(safeSettings.accentColor);
            slot.setBackground(pillBackground(safeSettings, safeCandidates.get(i).exactCorrection));
            slot.setOnClickListener(v -> {
                if (host != null) {
                    host.acceptSuggestion(suggestion);
                }
            });
        }
    }

    private GradientDrawable pillBackground(KeyboardSettings settings, boolean exact) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(exact ? settings.accentKeyColor : settings.functionKeyColor);
        background.setCornerRadius(dp(8));
        background.setStroke(Math.max(1, dp(1)), settings.borderColor);
        return background;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
