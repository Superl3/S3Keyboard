package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class InputFeelSettingsController {
    private final Context context;
    private final Supplier<KeyboardSettings> settings;
    private final Consumer<KeyboardSettings> settingsSaver;
    private final Runnable controlsSyncer;
    private final LocalDataControlsController localDataControlsController;
    private HapticSettingsController hapticSettingsController;
    private GestureTouchSettingsController gestureTouchSettingsController;
    private RepeatSettingsController repeatSettingsController;
    private InputConvenienceSettingsController inputConvenienceSettingsController;
    private LocalDataSettingsController localDataSettingsController;

    InputFeelSettingsController(
            Context context,
            Supplier<KeyboardSettings> settings,
            Consumer<KeyboardSettings> settingsSaver,
            Runnable controlsSyncer) {
        this.context = context;
        this.settings = RuntimeDefaults.keyboardSettingsSupplier(settings);
        this.settingsSaver = RuntimeDefaults.keyboardSettingsConsumer(settingsSaver);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
        this.localDataControlsController = new LocalDataControlsController(context);
    }

    void addTo(LinearLayout root) {
        LinearLayout touchSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_input_touch_subsection,
                true).content;
        gestureTouchSettingsController = new GestureTouchSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer);
        gestureTouchSettingsController.addTo(touchSection);

        LinearLayout convenienceSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_input_convenience_subsection,
                false).content;
        inputConvenienceSettingsController = new InputConvenienceSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer,
                () -> localDataControlsController);
        inputConvenienceSettingsController.addTo(convenienceSection);

        LinearLayout hapticSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_input_haptic_subsection,
                false).content;
        hapticSettingsController = new HapticSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer);
        hapticSettingsController.addTo(hapticSection);

        LinearLayout repeatSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_input_repeat_subsection,
                false).content;
        repeatSettingsController = new RepeatSettingsController(
                context,
                settings,
                settingsSaver);
        repeatSettingsController.addTo(repeatSection);

        LinearLayout dataSection = SettingsSubsection.add(
                context,
                root,
                R.string.settings_input_data_subsection,
                false).content;
        localDataSettingsController = new LocalDataSettingsController(
                context,
                () -> localDataControlsController,
                controlsSyncer);
        localDataSettingsController.addTo(dataSection);
    }

    void sync(KeyboardSettings settings) {
        if (hapticSettingsController != null) {
            hapticSettingsController.sync(settings);
        }
        if (gestureTouchSettingsController != null) {
            gestureTouchSettingsController.sync(settings);
        }
        if (repeatSettingsController != null) {
            repeatSettingsController.sync(settings);
        }
        if (inputConvenienceSettingsController != null) {
            inputConvenienceSettingsController.sync(settings);
        }
        if (localDataSettingsController != null) {
            localDataSettingsController.sync();
        }
    }

}
