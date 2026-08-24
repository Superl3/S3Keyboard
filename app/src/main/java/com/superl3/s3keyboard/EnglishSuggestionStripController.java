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
import java.util.function.Consumer;

final class EnglishSuggestionStripController {
    private static final int SLOT_COUNT = 3;
    private final Context context;
    private final Consumer<String> onSuggestionAccepted;
    private final TextView[] slots = new TextView[SLOT_COUNT];
    private LinearLayout root;

    EnglishSuggestionStripController(Context context, Consumer<String> onSuggestionAccepted) {
        this.context = context;
        this.onSuggestionAccepted = RuntimeDefaults.stringConsumer(onSuggestionAccepted);
    }

    LinearLayout createView() {
        root = SettingsRowBuilder.horizontal(context);
        root.setGravity(Gravity.CENTER);
        root.setPadding(
                SettingsRowBuilder.dp(context, 6),
                SettingsRowBuilder.dp(context, 3),
                SettingsRowBuilder.dp(context, 6),
                SettingsRowBuilder.dp(context, 3));
        for (int i = 0; i < SLOT_COUNT; i++) {
            TextView slot = createSlot();
            LinearLayout.LayoutParams params =
                    SettingsRowBuilder.weightedHeight(context, 32, i > 0 ? 4 : 0);
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
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        List<EnglishQwertyCorrectionEngine.Candidate> safeCandidates =
                candidates == null ? Collections.emptyList() : candidates;
        boolean visible = active && !safeCandidates.isEmpty();
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
        root.setBackgroundColor(safeSettings.keyboardBackgroundColor);
        for (int i = 0; i < slots.length; i++) {
            if (!visible || i >= safeCandidates.size()) {
                resetSlot(slots[i]);
                continue;
            }
            applySuggestion(slots[i], safeSettings, safeCandidates.get(i));
        }
    }

    private TextView createSlot() {
        TextView slot = SettingsRowBuilder.label(context, "");
        slot.setGravity(Gravity.CENTER);
        slot.setSingleLine(true);
        slot.setTextSize(13);
        slot.setTypeface(Typeface.DEFAULT_BOLD);
        slot.setPadding(
                SettingsRowBuilder.dp(context, 8),
                0,
                SettingsRowBuilder.dp(context, 8),
                0);
        return slot;
    }

    private void resetSlot(TextView slot) {
        slot.setText("");
        slot.setOnClickListener(null);
        slot.setVisibility(View.INVISIBLE);
    }

    private void applySuggestion(
            TextView slot,
            KeyboardSettings settings,
            EnglishQwertyCorrectionEngine.Candidate candidate) {
        final String suggestion = candidate.text;
        int backgroundColor = candidate.exactCorrection
                ? settings.accentKeyColor
                : settings.functionKeyColor;
        slot.setVisibility(View.VISIBLE);
        slot.setText(suggestion);
        slot.setTextColor(KeyboardColorMath.contrastTextColor(backgroundColor, 147));
        slot.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                context,
                settings.fontFamily,
                true,
                false));
        slot.setBackground(pillBackground(settings, backgroundColor));
        slot.setOnClickListener(v -> onSuggestionAccepted.accept(suggestion));
    }

    private GradientDrawable pillBackground(KeyboardSettings settings, int backgroundColor) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(backgroundColor);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), settings.borderColor);
        return background;
    }
}
