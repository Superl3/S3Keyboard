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
        hapticSettingsController = new HapticSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer);
        hapticSettingsController.addTo(root);

        gestureTouchSettingsController = new GestureTouchSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer);
        gestureTouchSettingsController.addTo(root);

        repeatSettingsController = new RepeatSettingsController(
                context,
                settings,
                settingsSaver);
        repeatSettingsController.addTo(root);

        inputConvenienceSettingsController = new InputConvenienceSettingsController(
                context,
                settings,
                settingsSaver,
                controlsSyncer,
                () -> localDataControlsController);
        inputConvenienceSettingsController.addTo(root);

        localDataSettingsController = new LocalDataSettingsController(
                context,
                () -> localDataControlsController,
                controlsSyncer);
        localDataSettingsController.addTo(root);
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
