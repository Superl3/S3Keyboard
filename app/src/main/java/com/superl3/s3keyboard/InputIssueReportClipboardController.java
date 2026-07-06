package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.util.function.Supplier;

final class InputIssueReportClipboardController {
    private final Context context;
    private final Runnable prepareIssueReport;
    private final Supplier<String> currentPackageName;
    private final Supplier<AppInputProfile> inputProfile;
    private final Supplier<KeyboardSettings> currentSettings;
    private final Supplier<EditorInputPolicy> currentEditorPolicy;

    InputIssueReportClipboardController(
            Context context,
            Runnable prepareIssueReport,
            Supplier<String> currentPackageName,
            Supplier<AppInputProfile> inputProfile,
            Supplier<KeyboardSettings> currentSettings,
            Supplier<EditorInputPolicy> currentEditorPolicy) {
        this.context = context;
        this.prepareIssueReport = RuntimeDefaults.runnable(prepareIssueReport);
        this.currentPackageName = RuntimeDefaults.emptyStringSupplier(currentPackageName);
        this.inputProfile = RuntimeDefaults.appInputProfileSupplier(inputProfile);
        this.currentSettings = RuntimeDefaults.keyboardSettingsSupplier(currentSettings);
        this.currentEditorPolicy = RuntimeDefaults.editorInputPolicySupplier(currentEditorPolicy);
    }

    void copyToClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        prepareIssueReport.run();
        String report = InputIssueReport.build(
                context,
                currentPackageName.get(),
                inputProfile.get(),
                currentSettings.get(),
                currentEditorPolicy.get());
        clipboard.setPrimaryClip(ClipData.newPlainText(
                context.getString(R.string.input_issue_report_clip_label),
                report));
        Toast.makeText(context, R.string.input_issue_report_copied, Toast.LENGTH_SHORT).show();
    }
}
