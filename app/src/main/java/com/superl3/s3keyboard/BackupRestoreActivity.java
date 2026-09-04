package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** User-facing SAF backup/restore flow with selective preview before mutation. */
public final class BackupRestoreActivity extends Activity {
    private static final int REQUEST_EXPORT = 2201;
    private static final int REQUEST_IMPORT = 2202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) getActionBar().hide();
        ScrollView content = createContent();
        SettingsSystemBars.applyTopInset(content);
        setContentView(content);
    }

    private ScrollView createContent() {
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        int padding = SettingsRowBuilder.dp(this, 16);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(ui.background);

        TextView title = SettingsRowBuilder.sectionLabel(this, getString(R.string.settings_backup_restore));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, SettingsRowBuilder.matchWrap());
        SettingsRowBuilder.bodyLabelRow(this, root, R.string.backup_restore_description, 12);
        SettingsRowBuilder.buttonRow(this, root, R.string.backup_export, 16, v -> startExport());
        SettingsRowBuilder.buttonRow(this, root, R.string.backup_import, 8, v -> startImport());
        SettingsRowBuilder.buttonRow(this, root, R.string.backup_reset_selected, 20, v -> showResetDialog());
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(ui.background);
        scroll.addView(root);
        return scroll;
    }
    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "s3keyboard-backup-" + stamp + ".json");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQUEST_EXPORT) {
            exportTo(uri);
        } else if (requestCode == REQUEST_IMPORT) {
            importFrom(uri);
        }
    }

    private void exportTo(Uri uri) {
        try {
            String backup = BackupRestoreManager.exportBackup(this);
            OutputStream stream = getContentResolver().openOutputStream(uri, "wt");
            if (stream == null) throw new IOException("Cannot open backup destination.");
            try {
                stream.write(backup.getBytes(StandardCharsets.UTF_8));
                stream.flush();
            } finally {
                stream.close();
            }
            toast(R.string.backup_saved);
        } catch (IOException | RuntimeException exception) {
            showError(R.string.backup_export_failed, exception);
        }
    }

    private void importFrom(Uri uri) {
        try {
            String raw = readUtf8(uri);
            BackupRestoreManager.Preview preview = BackupRestoreManager.preview(raw);
            showRestorePreview(preview);
        } catch (IOException | RuntimeException exception) {
            showError(R.string.backup_read_failed, exception);
        }
    }
    private void showRestorePreview(BackupRestoreManager.Preview preview) {
        String[] labels = new String[]{
                getString(R.string.backup_section_settings_format, preview.settingCount),
                getString(R.string.backup_section_profiles_format, preview.appProfileCount),
                getString(R.string.backup_section_themes_format, preview.themeCount),
                getString(R.string.backup_section_text_tools_format, preview.textToolCount),
                getString(R.string.backup_section_local_preferences_format, preview.localPreferenceCount)};
        boolean[] checked = new boolean[]{true, true, true, true, true};
        String title = getString(R.string.backup_restore_select_title);
        String generated = preview.parsed.generatedAt.isEmpty()
                ? "" : getString(R.string.backup_restore_generated_format, preview.parsed.generatedAt);
        String message = getString(R.string.backup_restore_metadata_format,
                preview.parsed.schemaVersion, preview.parsed.appVersion, generated);
        LinearLayout selectionView = createSelectionView(message, labels, checked);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(selectionView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_restore, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                BackupRestoreManager.apply(this, preview, selectionFrom(checked));
                toast(R.string.backup_restore_done);
                dialog.dismiss();
            } catch (RuntimeException exception) {
                showError(R.string.backup_restore_failed, exception);
            }
        }));
        dialog.show();
    }

    private void showResetDialog() {
        String[] labels = new String[]{
                getString(R.string.backup_section_settings),
                getString(R.string.backup_section_profiles),
                getString(R.string.backup_section_themes),
                getString(R.string.backup_section_text_tools),
                getString(R.string.backup_section_local_preferences)};
        boolean[] checked = new boolean[]{false, false, false, false, false};
        LinearLayout selectionView = createSelectionView(
                getString(R.string.backup_reset_message), labels, checked);
        new AlertDialog.Builder(this)
                .setTitle(R.string.backup_reset_title)
                .setView(selectionView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.backup_reset_action, (d, which) -> {
                    BackupRestoreManager.resetPortableData(this, selectionFrom(checked));
                    toast(R.string.backup_reset_done);
                })
                .show();
    }

    private LinearLayout createSelectionView(String message, String[] labels, boolean[] checked) {
        LinearLayout root = SettingsRowBuilder.vertical(this);
        int horizontal = SettingsRowBuilder.dp(this, 24);
        int vertical = SettingsRowBuilder.dp(this, 8);
        root.setPadding(horizontal, vertical, horizontal, vertical);
        TextView messageView = SettingsRowBuilder.bodyLabel(this, message);
        root.addView(messageView, SettingsRowBuilder.matchWrap());
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            CheckBox box = new CheckBox(this);
            box.setText(labels[i]);
            box.setChecked(i < checked.length && checked[i]);
            box.setOnCheckedChangeListener((buttonView, isChecked) -> checked[index] = isChecked);
            root.addView(box, SettingsRowBuilder.matchWrap());
        }
        return root;
    }

    private static BackupRestoreManager.Selection selectionFrom(boolean[] checked) {
        return new BackupRestoreManager.Selection(
                checked.length > 0 && checked[0], checked.length > 1 && checked[1],
                checked.length > 2 && checked[2], checked.length > 3 && checked[3],
                checked.length > 4 && checked[4]);
    }
    private String readUtf8(Uri uri) throws IOException {
        InputStream stream = getContentResolver().openInputStream(uri);
        if (stream == null) throw new IOException("Cannot open backup file.");
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (output.size() + read > BackupRestoreCodec.MAX_BACKUP_CHARS) {
                    throw new IOException("Backup file exceeds the size limit.");
                }
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        } finally {
            stream.close();
        }
    }

    private void showError(int titleResId, Exception exception) {
        String fallback = getString(R.string.backup_unknown_error);
        String detail = exception == null ? fallback : exception.getMessage();
        new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setMessage(detail == null || detail.isEmpty() ? fallback : detail)
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }

    private void toast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }
}
