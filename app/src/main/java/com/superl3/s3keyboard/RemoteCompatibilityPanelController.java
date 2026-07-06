package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;

final class RemoteCompatibilityPanelController {
    static final int SEND_SKIPPED = -1;
    private static final IntBinaryOperator SKIPPED_KEY_SENDER =
            (keyCode, metaState) -> SEND_SKIPPED;

    private final Context context;
    private final Supplier<String> currentPackageName;
    private final IntBinaryOperator keySender;
    private TextView historyView;
    private String lastLabel = "";

    RemoteCompatibilityPanelController(
            Context context,
            Supplier<String> currentPackageName,
            IntBinaryOperator keySender) {
        this.context = context;
        this.currentPackageName = RuntimeDefaults.emptyStringSupplier(currentPackageName);
        this.keySender = keySender == null ? SKIPPED_KEY_SENDER : keySender;
    }

    void addTo(LinearLayout panel) {
        if (panel == null) {
            return;
        }
        QuickPanelUi.addWithTop(
                context,
                panel,
                QuickPanelUi.sectionLabel(context, R.string.remote_key_test_title),
                10);

        addCaseRow(
                panel,
                RemoteCompatibilityMatrix.ESC,
                RemoteCompatibilityMatrix.TAB,
                RemoteCompatibilityMatrix.SHIFT_TAB,
                RemoteCompatibilityMatrix.CTRL_TAB,
                RemoteCompatibilityMatrix.ALT_TAB);
        addCaseRow(
                panel,
                RemoteCompatibilityMatrix.CTRL_A,
                RemoteCompatibilityMatrix.ALT_SHIFT,
                RemoteCompatibilityMatrix.CTRL_SPACE,
                RemoteCompatibilityMatrix.WIN_SPACE,
                RemoteCompatibilityMatrix.LANGUAGE_SWITCH);
        addFunctionCaseRow(panel, 0, 4);
        addFunctionCaseRow(panel, 4, 8);
        addFunctionCaseRow(panel, 8, 12);
        addResultRow(panel);
        addReportRow(panel);

        historyView = createHistoryView();
        refreshHistory();
        QuickPanelUi.addWithTop(context, panel, historyView, 6);
    }

    void reset() {
        lastLabel = "";
        refreshHistory();
    }

    private void addCaseRow(
            LinearLayout panel,
            RemoteCompatibilityMatrix.Case... testCases) {
        LinearLayout row = QuickPanelUi.row(context);
        for (int i = 0; i < testCases.length; i++) {
            addCaseButton(row, testCases[i], i == testCases.length - 1 ? 0 : 4);
        }
        QuickPanelUi.addWithTop(context, panel, row, 4);
    }

    private void addFunctionCaseRow(LinearLayout panel, int startInclusive, int endExclusive) {
        LinearLayout row = QuickPanelUi.row(context);
        RemoteCompatibilityMatrix.Case[] functionCases =
                RemoteCompatibilityMatrix.group(RemoteCompatibilityMatrix.Group.FUNCTION);
        for (int i = startInclusive; i < endExclusive && i < functionCases.length; i++) {
            addCaseButton(row, functionCases[i], i == endExclusive - 1 ? 0 : 4);
        }
        QuickPanelUi.addWithTop(context, panel, row, 4);
    }

    private void addResultRow(LinearLayout panel) {
        LinearLayout row = QuickPanelUi.row(context);
        QuickPanelUi.addCompactButton(
                context,
                row,
                context.getString(R.string.mark_remote_test_pass),
                v -> markResult(RemoteCompatibilityLog.RESULT_PASS),
                4);
        QuickPanelUi.addCompactButton(
                context,
                row,
                context.getString(R.string.mark_remote_test_fail),
                v -> markResult(RemoteCompatibilityLog.RESULT_FAIL),
                0);
        QuickPanelUi.addWithTop(context, panel, row, 4);
    }

    private void addReportRow(LinearLayout panel) {
        LinearLayout row = QuickPanelUi.row(context);
        QuickPanelUi.addCompactButton(
                context,
                row,
                context.getString(R.string.copy_remote_compatibility_report),
                v -> copyReportToClipboard(),
                4);
        QuickPanelUi.addCompactButton(
                context,
                row,
                context.getString(R.string.clear_remote_compatibility_log),
                v -> clearReportLog(),
                0);
        QuickPanelUi.addWithTop(context, panel, row, 4);
    }

    private TextView createHistoryView() {
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        TextView view = SettingsRowBuilder.secondaryLabel(context, "");
        view.setTextSize(11);
        view.setTypeface(Typeface.MONOSPACE);
        view.setPadding(
                QuickPanelUi.dp(context, 8),
                QuickPanelUi.dp(context, 6),
                QuickPanelUi.dp(context, 8),
                QuickPanelUi.dp(context, 6));
        GradientDrawable historyBackground = new GradientDrawable();
        historyBackground.setColor(ui.controlFill);
        historyBackground.setCornerRadius(QuickPanelUi.dp(context, 8));
        historyBackground.setStroke(Math.max(1, QuickPanelUi.dp(context, 1)), ui.border);
        view.setBackground(historyBackground);
        return view;
    }

    private void clearReportLog() {
        RemoteCompatibilityLog.clear(context);
        reset();
    }

    private void addCaseButton(
            LinearLayout row,
            RemoteCompatibilityMatrix.Case testCase,
            int rightMarginDp) {
        if (row == null || testCase == null) {
            return;
        }
        QuickPanelUi.addCompactButton(
                context,
                row,
                testCase.label,
                v -> sendCase(testCase),
                rightMarginDp);
    }

    private void sendCase(RemoteCompatibilityMatrix.Case testCase) {
        if (testCase == null) {
            return;
        }
        int eventCount = keySender.applyAsInt(testCase.keyCode, testCase.metaState);
        if (eventCount == SEND_SKIPPED) {
            return;
        }
        lastLabel = RuntimeDefaults.stringOrDefault(testCase.label, "");
        RemoteCompatibilityLog.record(
                context,
                currentPackageName(),
                testCase.label,
                testCase.keyCode,
                testCase.metaState,
                eventCount);
        refreshHistory();
    }

    private void markResult(String manualResult) {
        boolean marked = RemoteCompatibilityLog.markLatestResult(
                context,
                currentPackageName(),
                lastLabel,
                manualResult);
        Toast.makeText(
                context,
                marked ? R.string.remote_test_result_marked : R.string.remote_test_result_missing,
                Toast.LENGTH_SHORT).show();
        refreshHistory();
    }

    private void copyReportToClipboard() {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            Toast.makeText(context, R.string.clipboard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        String report = RemoteCompatibilityReport.toJson(
                currentPackageName(),
                RemoteCompatibilityLog.load(context));
        clipboard.setPrimaryClip(ClipData.newPlainText(
                context.getString(R.string.remote_compatibility_report_clip_label),
                report));
        Toast.makeText(context, R.string.remote_compatibility_report_copied, Toast.LENGTH_SHORT).show();
    }

    private void refreshHistory() {
        if (historyView == null) {
            return;
        }
        String packageName = currentPackageName();
        String summary = RemoteCompatibilityReport.describe(
                context,
                packageName,
                RemoteCompatibilityLog.load(context));
        historyView.setText(summary + "\n\n" + RemoteCompatibilityLog.describeRecent(context, 6));
    }

    private String currentPackageName() {
        return AppPackageCatalog.normalizePackageName(currentPackageName.get());
    }

}
