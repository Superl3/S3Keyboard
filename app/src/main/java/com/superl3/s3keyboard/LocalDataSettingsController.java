package com.superl3.s3keyboard;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Supplier;

final class LocalDataSettingsController {
    private final Context context;
    private final Supplier<LocalDataControlsController> localDataControls;
    private final Runnable controlsSyncer;
    private TextView localDataSummaryValue;
    private TextView dingulInputDiagnosticsValue;

    LocalDataSettingsController(
            Context context,
            Supplier<LocalDataControlsController> localDataControls,
            Runnable controlsSyncer) {
        this.context = context;
        this.localDataControls = RuntimeDefaults.localDataControlsSupplier(context, localDataControls);
        this.controlsSyncer = RuntimeDefaults.runnable(controlsSyncer);
    }

    void addTo(LinearLayout root) {
        SettingsRowBuilder.bodyLabelRow(context, root, R.string.local_data_disclosure, 12);

        localDataSummaryValue = SettingsRowBuilder.bodyLabelRow(context, root, "", 6);

        dingulInputDiagnosticsValue = SettingsRowBuilder.bodyLabelRow(context, root, "", 6);

        SettingsRowBuilder.labelRow(context, root, R.string.practice_mode_section, 12);
        root.addView(
                PracticeModeController.createPanel(context),
                SettingsRowBuilder.matchWrapWithTop(context, 6));

        addResetButton(
                root,
                R.string.clear_all_local_data,
                () -> localDataControls.get().clearAllLocalData());
        addResetButton(
                root,
                R.string.clear_touch_correction_and_input_logs,
                () -> localDataControls.get().resetTouchCorrectionAndInputLogs());
        addResetButton(
                root,
                R.string.clear_input_logs_only,
                () -> localDataControls.get().clearInputLogsOnly());
        addResetButton(
                root,
                R.string.clear_touch_bias_only,
                () -> localDataControls.get().clearTouchBiasOnly());
        addResetButton(
                root,
                R.string.clear_clipboard_history,
                () -> localDataControls.get().clearClipboardHistory());
        addResetButton(
                root,
                R.string.clear_remote_test_log,
                () -> localDataControls.get().clearRemoteCompatibilityLog());
    }

    void sync() {
        if (localDataSummaryValue != null) {
            localDataSummaryValue.setText(localDataControls.get().summaryText());
        }
        if (dingulInputDiagnosticsValue != null) {
            dingulInputDiagnosticsValue.setText(DingulInputDiagnostics.load(context).summaryText(context));
        }
    }

    private void addResetButton(LinearLayout root, int textResId, Runnable action) {
        SettingsRowBuilder.iconButtonRow(
                context,
                root,
                textResId,
                R.drawable.ic_keyboard_reset,
                12,
                v -> {
                    action.run();
                    controlsSyncer.run();
                });
    }

}
