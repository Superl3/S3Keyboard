package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class DiagnosticsActivity extends Activity {
    private static final int REQUEST_EXPORT = 8108;
    private TextView reportView;
    private String currentReport = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) getActionBar().hide();
        ScrollView content = createContentView();
        SettingsSystemBars.applyTopInset(content);
        setContentView(content);
        refreshReport();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshReport();
    }
    private ScrollView createContentView() {
        int padding = SettingsRowBuilder.dp(this, 16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(ui.background);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setPadding(padding, padding, padding, padding);

        TextView title = SettingsRowBuilder.sectionLabel(this, getString(R.string.diagnostics_title));
        title.setGravity(Gravity.CENTER);
        root.addView(title, SettingsRowBuilder.matchWrap());
        SettingsRowBuilder.bodyLabelRow(this, root, R.string.diagnostics_disclosure, 12);

        reportView = SettingsRowBuilder.bodyLabel(this, "");
        reportView.setTextIsSelectable(true);
        root.addView(reportView, SettingsRowBuilder.matchWrapWithTop(this, 12));

        SettingsRowBuilder.buttonRow(this, root, R.string.diagnostics_copy, 12, v -> copyReport());
        SettingsRowBuilder.buttonRow(this, root, R.string.diagnostics_export, 8, v -> exportReport());
        SettingsRowBuilder.buttonRow(this, root, R.string.diagnostics_reset, 8, v -> confirmReset());
        scroll.addView(root);
        return scroll;
    }

    private void refreshReport() {
        currentReport = ReleaseSafeDiagnostics.buildReport(this);
        if (reportView != null) reportView.setText(currentReport);
    }

    private void copyReport() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(this, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        refreshReport();
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.diagnostics_clip_label), currentReport));
        Toast.makeText(this, R.string.diagnostics_copied, Toast.LENGTH_SHORT).show();
    }
    private void exportReport() {
        refreshReport();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "s3-keyboard-diagnostic.json");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.diagnostics_reset_title)
                .setMessage(R.string.diagnostics_reset_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.diagnostics_reset_confirm, (dialog, which) -> {
                    new LocalDataControlsController(this).resetDiagnosticsAndInputLearning();
                    refreshReport();
                    Toast.makeText(this, R.string.diagnostics_reset_done, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException("No document output stream");
            output.write(currentReport.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, R.string.diagnostics_exported, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
            Toast.makeText(this, R.string.diagnostics_export_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
