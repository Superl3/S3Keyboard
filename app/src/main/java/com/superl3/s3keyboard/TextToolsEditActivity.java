package com.superl3.s3keyboard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Dialog-style editor launched from the IME Text Tools panel. */
public final class TextToolsEditActivity extends Activity {
    static final String EXTRA_MODE = "text_tools_edit_mode";
    static final String EXTRA_ITEM_ID = "text_tools_item_id";
    static final String EXTRA_GESTURE_ACTION = "text_tools_gesture_action";
    static final String MODE_SAVED_ITEM = "saved_item";
    static final String MODE_RESERVED_PHRASE = "reserved_phrase";

    private EditText input;
    private String mode;
    private String itemId;
    private GestureAction gestureAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        gestureAction = parseGestureAction(getIntent().getStringExtra(EXTRA_GESTURE_ACTION));
        if (!loadTarget()) {
            finish();
            return;
        }
        setContentView(buildContent());
    }

    private boolean loadTarget() {
        input = new EditText(this);
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveTarget();
                return true;
            }
            return false;
        });
        if (MODE_SAVED_ITEM.equals(mode)) {
            for (TextToolsStore.Item item : new TextToolsStore(this).getItems()) {
                if (item.id.equals(itemId)) {
                    input.setText(item.name);
                    input.setSelection(input.getText().length());
                    return true;
                }
            }
            return false;
        }
        if (MODE_RESERVED_PHRASE.equals(mode) && gestureAction != null) {
            input.setText(KeyboardPreferences.loadReservedPhrase(this, gestureAction));
            input.setSelection(input.getText().length());
            input.setHint(R.string.reserved_phrase_empty_hint);
            return true;
        }
        return false;
    }

    private LinearLayout buildContent() {
        LinearLayout root = SettingsRowBuilder.vertical(this);
        int padding = SettingsRowBuilder.dp(this, 20);
        root.setPadding(padding, padding, padding, padding);
        TextView title = SettingsRowBuilder.label(this,
                MODE_SAVED_ITEM.equals(mode) ? R.string.text_tools_rename : R.string.text_tools_edit);
        title.setTextSize(20);
        root.addView(title, SettingsRowBuilder.matchWrap());
        root.addView(input, SettingsRowBuilder.matchWrap());

        LinearLayout actions = SettingsRowBuilder.horizontal(this);
        actions.setGravity(Gravity.END);
        Button delete = SettingsRowBuilder.button(this, R.string.action_delete, v -> deleteTarget());
        Button cancel = SettingsRowBuilder.button(this, R.string.action_cancel, v -> finish());
        Button save = SettingsRowBuilder.button(this, R.string.action_save, v -> saveTarget());
        actions.addView(delete);
        actions.addView(cancel);
        actions.addView(save);
        root.addView(actions, SettingsRowBuilder.matchWrap());
        return root;
    }

    private void saveTarget() {
        String value = input.getText().toString();
        if (MODE_SAVED_ITEM.equals(mode)) {
            new TextToolsStore(this).rename(itemId, value);
        } else if (gestureAction != null) {
            KeyboardPreferences.saveReservedPhrase(this, gestureAction, value);
        }
        finish();
    }

    private void deleteTarget() {
        if (MODE_SAVED_ITEM.equals(mode)) {
            new TextToolsStore(this).delete(itemId);
        } else if (gestureAction != null) {
            KeyboardPreferences.saveReservedPhrase(this, gestureAction, "");
        }
        finish();
    }

    private static GestureAction parseGestureAction(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return GestureAction.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
