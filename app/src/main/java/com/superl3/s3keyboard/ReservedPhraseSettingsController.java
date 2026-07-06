package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;

final class ReservedPhraseSettingsController {
    private final Context context;

    ReservedPhraseSettingsController(Context context) {
        this.context = context;
    }

    void addTo(LinearLayout root) {
        addField(root, R.string.reserved_phrase_tap, GestureAction.TAP);
        addField(root, R.string.reserved_phrase_left_slide, GestureAction.LEFT);
        addField(root, R.string.reserved_phrase_right_slide, GestureAction.RIGHT);
        addField(root, R.string.reserved_phrase_up_slide, GestureAction.UP);
    }

    private void addField(LinearLayout root, int labelResId, GestureAction action) {
        EditText input = SettingsRowBuilder.editText(
                context,
                KeyboardPreferences.loadReservedPhrase(context, action),
                () -> true,
                value -> KeyboardPreferences.saveReservedPhrase(context, action, value));
        input.setSingleLine(true);
        input.setHint(R.string.reserved_phrase_empty_hint);
        SettingsRowBuilder.labeledControl(context, root, labelResId, input, 8);
    }
}
