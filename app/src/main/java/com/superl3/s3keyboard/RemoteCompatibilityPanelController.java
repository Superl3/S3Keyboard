package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

final class RemoteCompatibilityPanelController {
    static final int SEND_SKIPPED = -1;

    interface Host {
        String currentPackageName();

        int sendCompatibilityKey(int keyCode, int metaState);
    }

    private final Context context;
    private final Host host;
    private TextView historyView;
    private String lastLabel = "";

    RemoteCompatibilityPanelController(Context context, Host host) {
        this.context = context;
        this.host = host;
    }

    void addTo(LinearLayout panel) {
        if (panel == null) {
            return;
        }
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        TextView label = new TextView(context);
        label.setText(R.string.remote_key_test_title);
        label.setTextColor(ui.textSecondary);
        label.setTextSize(13);
        panel.addView(label, topWrap(10));

        LinearLayout row1 = row();
        addCaseButton(row1, RemoteCompatibilityMatrix.ESC, 4);
        addCaseButton(row1, RemoteCompatibilityMatrix.TAB, 4);
        addCaseButton(row1, RemoteCompatibilityMatrix.SHIFT_TAB, 4);
        addCaseButton(row1, RemoteCompatibilityMatrix.CTRL_TAB, 4);
        addCaseButton(row1, RemoteCompatibilityMatrix.ALT_TAB, 0);
        panel.addView(row1, topWrap(4));

        LinearLayout row2 = row();
        addCaseButton(row2, RemoteCompatibilityMatrix.CTRL_A, 4);
        addCaseButton(row2, RemoteCompatibilityMatrix.ALT_SHIFT, 4);
        addCaseButton(row2, RemoteCompatibilityMatrix.CTRL_SPACE, 4);
        addCaseButton(row2, RemoteCompatibilityMatrix.WIN_SPACE, 4);
        addCaseButton(row2, RemoteCompatibilityMatrix.LANGUAGE_SWITCH, 0);
        panel.addView(row2, topWrap(4));

        LinearLayout row3 = row();
        addCaseRange(row3, 0, 4);
        panel.addView(row3, topWrap(4));

        LinearLayout row4 = row();
        addCaseRange(row4, 4, 8);
        panel.addView(row4, topWrap(4));

        LinearLayout row5 = row();
        addCaseRange(row5, 8, 12);
        panel.addView(row5, topWrap(4));

        LinearLayout resultRow = row();
        resultRow.addView(button(
                        context.getString(R.string.mark_remote_test_pass),
                        v -> markResult(RemoteCompatibilityLog.RESULT_PASS)),
                weightedParams(0, 4));
        resultRow.addView(button(
                        context.getString(R.string.mark_remote_test_fail),
                        v -> markResult(RemoteCompatibilityLog.RESULT_FAIL)),
                weightedParams(0, 0));
        panel.addView(resultRow, topWrap(4));

        LinearLayout reportRow = row();
        reportRow.addView(button(
                        context.getString(R.string.copy_remote_compatibility_report),
                        v -> copyReportToClipboard()),
                weightedParams(0, 4));
        reportRow.addView(button(context.getString(R.string.clear_remote_compatibility_log), v -> {
            RemoteCompatibilityLog.clear(context);
            reset();
        }), weightedParams(0, 0));
        panel.addView(reportRow, topWrap(4));

        historyView = new TextView(context);
        historyView.setTextColor(ui.textSecondary);
        historyView.setTextSize(11);
        historyView.setTypeface(Typeface.MONOSPACE);
        historyView.setPadding(dp(8), dp(6), dp(8), dp(6));
        GradientDrawable historyBackground = new GradientDrawable();
        historyBackground.setColor(ui.controlFill);
        historyBackground.setCornerRadius(dp(8));
        historyBackground.setStroke(Math.max(1, dp(1)), ui.border);
        historyView.setBackground(historyBackground);
        refreshHistory();
        panel.addView(historyView, topWrap(6));
    }

    void reset() {
        lastLabel = "";
        refreshHistory();
    }

    private void addCaseRange(LinearLayout row, int startInclusive, int endExclusive) {
        RemoteCompatibilityMatrix.Case[] functionCases =
                RemoteCompatibilityMatrix.group(RemoteCompatibilityMatrix.Group.FUNCTION);
        for (int i = startInclusive; i < endExclusive && i < functionCases.length; i++) {
            addCaseButton(row, functionCases[i], i == endExclusive - 1 ? 0 : 4);
        }
    }

    private void addCaseButton(
            LinearLayout row,
            RemoteCompatibilityMatrix.Case testCase,
            int rightMarginDp) {
        if (row == null || testCase == null) {
            return;
        }
        row.addView(
                button(testCase.label, v -> sendCase(testCase)),
                weightedParams(0, rightMarginDp));
    }

    private void sendCase(RemoteCompatibilityMatrix.Case testCase) {
        if (testCase == null || host == null) {
            return;
        }
        int eventCount = host.sendCompatibilityKey(testCase.keyCode, testCase.metaState);
        if (eventCount == SEND_SKIPPED) {
            return;
        }
        lastLabel = testCase.label == null ? "" : testCase.label;
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
        if (host == null) {
            return "";
        }
        String packageName = host.currentPackageName();
        return packageName == null ? "" : packageName;
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button button = new Button(context);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(38));
        button.setPadding(dp(8), 0, dp(8), 0);
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        button.setTextColor(ui.controlText);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.controlFill);
        background.setCornerRadius(dp(8));
        background.setStroke(Math.max(1, dp(1)), ui.border);
        button.setBackground(background);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams topWrap(int topMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private LinearLayout.LayoutParams weightedParams(int leftMarginDp, int rightMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        params.leftMargin = dp(leftMarginDp);
        params.rightMargin = dp(rightMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
