package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

final class InputIssueReportClipboardController {
    interface Host {
        void prepareIssueReport();

        String currentPackageName();

        AppInputProfile inputProfile();

        KeyboardSettings currentSettings();

        EditorInputPolicy currentEditorPolicy();
    }

    private final Context context;
    private final Host host;

    InputIssueReportClipboardController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void copyToClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        if (host != null) {
            host.prepareIssueReport();
        }
        String report = InputIssueReport.build(
                context,
                host == null ? "" : host.currentPackageName(),
                host == null ? AppInputProfile.STANDARD : host.inputProfile(),
                host == null ? KeyboardSettings.defaults() : host.currentSettings(),
                host == null ? EditorInputPolicy.DEFAULT : host.currentEditorPolicy());
        clipboard.setPrimaryClip(ClipData.newPlainText(
                context.getString(R.string.input_issue_report_clip_label),
                report));
        Toast.makeText(context, R.string.input_issue_report_copied, Toast.LENGTH_SHORT).show();
    }
}
