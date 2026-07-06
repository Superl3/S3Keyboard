package com.superl3.s3keyboard;

import android.app.Activity;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class SettingsHubController {
    private final Activity activity;
    private final Supplier<KeyboardSettings> settings;
    private final Runnable currentThemeCustomMarker;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final GesturePracticeInputController gesturePracticeInputController;

    SettingsHubController(
            Activity activity,
            Supplier<KeyboardSettings> settings,
            Runnable currentThemeCustomMarker,
            Consumer<KeyboardSettings> settingsSaver) {
        this.activity = activity;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.currentThemeCustomMarker = RuntimeDefaults.runnable(currentThemeCustomMarker);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.gesturePracticeInputController = new GesturePracticeInputController(activity);
    }

    void addTo(
            LinearLayout root,
            DemoFieldProfile demoFieldProfile,
            boolean demoShowKeyboard) {
        SettingsRowBuilder.bodyLabelRow(activity, root, R.string.gesture_practice_body, 0);
        SettingsRowBuilder.bodyLabelRow(activity, root, BuildInfoProvider.summary(activity), 6);

        EditText testInput = gesturePracticeInputController.createInput(
                demoFieldProfile,
                demoShowKeyboard);
        root.addView(testInput, SettingsRowBuilder.matchWrapWithTop(activity, 12));

        new ThemeHubSettingsController(
                activity,
                settings,
                currentThemeCustomMarker,
                settingsSaver).addTo(root);
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        gesturePracticeInputController.hideKeyboardWhenTouchingOutside(event);
    }
}
