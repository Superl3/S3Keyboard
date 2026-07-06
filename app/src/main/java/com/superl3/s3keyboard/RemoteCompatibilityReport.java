package com.superl3.s3keyboard;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RemoteCompatibilityReport {
    private RemoteCompatibilityReport() {
    }

    static String describe(
            Context context,
            String packageName,
            List<RemoteCompatibilityLog.Entry> entries) {
        if (context == null) {
            return describe(packageName, entries);
        }
        Summary summary = summarize(packageName, entries);
        StringBuilder builder = new StringBuilder();
        String displayPackage = summary.packageName.isEmpty()
                ? context.getString(R.string.remote_compatibility_package_unknown)
                : summary.packageName;
        builder.append(context.getString(
                        R.string.remote_compatibility_summary_header,
                        displayPackage,
                        summary.appFamily))
                .append('\n')
                .append(context.getString(
                        R.string.remote_compatibility_summary_counts,
                        summary.sentCount,
                        summary.totalCount,
                        summary.passCount,
                        summary.failCount,
                        summary.unknownCount));
        if (!summary.missingLabels().isEmpty()) {
            builder.append('\n')
                    .append(context.getString(
                            R.string.remote_compatibility_missing_cases,
                            join(summary.missingLabels())));
        }
        if (!summary.localIncompleteLabels().isEmpty()) {
            builder.append('\n')
                    .append(context.getString(
                            R.string.remote_compatibility_incomplete_cases,
                            join(summary.localIncompleteLabels())));
        }
        if (summary.manualRemoteResultRequired()) {
            builder.append('\n')
                    .append(context.getString(R.string.remote_compatibility_manual_required));
        }
        return builder.toString();
    }

    static String describe(String packageName, List<RemoteCompatibilityLog.Entry> entries) {
        Summary summary = summarize(packageName, entries);
        StringBuilder builder = new StringBuilder();
        builder.append("Remote compatibility: ")
                .append(summary.packageName.isEmpty() ? "unknown package" : summary.packageName)
                .append("  family ")
                .append(summary.appFamily)
                .append('\n')
                .append("sent ")
                .append(summary.sentCount)
                .append('/')
                .append(summary.totalCount)
                .append("  pass ")
                .append(summary.passCount)
                .append("  fail ")
                .append(summary.failCount)
                .append("  unknown ")
                .append(summary.unknownCount);
        if (!summary.missingLabels().isEmpty()) {
            builder.append('\n')
                    .append("missing: ")
                    .append(join(summary.missingLabels()));
        }
        if (!summary.localIncompleteLabels().isEmpty()) {
            builder.append('\n')
                    .append("local incomplete: ")
                    .append(join(summary.localIncompleteLabels()));
        }
        if (summary.manualRemoteResultRequired()) {
            builder.append('\n')
                    .append("Manual remote pass/fail confirmation required.");
        }
        return builder.toString();
    }

    static String toJson(String packageName, List<RemoteCompatibilityLog.Entry> entries) {
        Summary summary = summarize(packageName, entries);
        JSONObject object = new JSONObject();
        try {
            object.put("schemaVersion", 2);
            object.put("packageName", summary.packageName);
            object.put("appFamily", summary.appFamily);
            object.put("sentCount", summary.sentCount);
            object.put("totalCount", summary.totalCount);
            object.put("passCount", summary.passCount);
            object.put("failCount", summary.failCount);
            object.put("unknownCount", summary.unknownCount);
            object.put("localIncompleteCount", summary.localIncompleteCount);
            object.put("manualRemoteResultRequired",
                    summary.manualRemoteResultRequired());
            object.put("requiredLabels", stringArray(RemoteCompatibilityMatrix.labels()));
            object.put("requiredAppFamilies", stringArray(RemoteAppCatalog.reportFamilies()));
            object.put("missingLabels", stringArray(summary.missingLabels()));
            object.put("unknownLabels", stringArray(summary.unknownLabels()));
            object.put("failedLabels", stringArray(summary.failedLabels()));
            object.put("localIncompleteLabels", stringArray(summary.localIncompleteLabels()));
            object.put("groupSummaries", groupSummaries(summary));
            JSONArray cases = new JSONArray();
            for (CaseStatus status : summary.statuses.values()) {
                JSONObject item = new JSONObject();
                item.put("label", status.testCase.label);
                item.put("group", status.testCase.group.name());
                item.put("keyCode", status.testCase.keyCode);
                item.put("metaState", status.testCase.metaState);
                item.put("sent", status.entry != null);
                item.put("manualResult", status.manualResult());
                if (status.entry != null) {
                    item.put("timestampMs", status.entry.timestampMs);
                    item.put("eventCount", status.entry.acceptedEventCount);
                    item.put("acceptedEventCount", status.entry.acceptedEventCount);
                    item.put("expectedEventCount", status.entry.expectedEventCount());
                    item.put("localInputConnectionAccepted",
                            status.entry.localInputConnectionAccepted());
                    item.put("localTransportComplete",
                            status.entry.localInputConnectionAccepted());
                }
                cases.put(item);
            }
            object.put("cases", cases);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to build remote compatibility report.", exception);
        }
        return object.toString();
    }

    private static JSONArray groupSummaries(Summary summary) {
        JSONArray array = new JSONArray();
        if (summary == null) {
            return array;
        }
        for (RemoteCompatibilityMatrix.Group group : RemoteCompatibilityMatrix.Group.values()) {
            int total = 0;
            int sent = 0;
            int pass = 0;
            int fail = 0;
            int unknown = 0;
            int localIncomplete = 0;
            java.util.ArrayList<String> missingLabels = new java.util.ArrayList<>();
            java.util.ArrayList<String> unknownLabels = new java.util.ArrayList<>();
            java.util.ArrayList<String> failedLabels = new java.util.ArrayList<>();
            java.util.ArrayList<String> localIncompleteLabels = new java.util.ArrayList<>();
            for (CaseStatus status : summary.statuses.values()) {
                if (status.testCase.group != group) {
                    continue;
                }
                total++;
                if (status.entry == null) {
                    missingLabels.add(status.testCase.label);
                    continue;
                }
                sent++;
                if (!status.entry.localInputConnectionAccepted()) {
                    localIncomplete++;
                    localIncompleteLabels.add(status.testCase.label);
                }
                if (RemoteCompatibilityLog.RESULT_PASS.equals(status.entry.manualResult)) {
                    pass++;
                } else if (RemoteCompatibilityLog.RESULT_FAIL.equals(status.entry.manualResult)) {
                    fail++;
                    failedLabels.add(status.testCase.label);
                } else {
                    unknown++;
                    unknownLabels.add(status.testCase.label);
                }
            }
            JSONObject object = new JSONObject();
            try {
                object.put("group", group.name());
                object.put("totalCount", total);
                object.put("sentCount", sent);
                object.put("passCount", pass);
                object.put("failCount", fail);
                object.put("unknownCount", unknown);
                object.put("missingCount", missingLabels.size());
                object.put("localIncompleteCount", localIncomplete);
                object.put("missingLabels", stringArray(missingLabels));
                object.put("unknownLabels", stringArray(unknownLabels));
                object.put("failedLabels", stringArray(failedLabels));
                object.put("localIncompleteLabels", stringArray(localIncompleteLabels));
                array.put(object);
            } catch (JSONException exception) {
                throw new IllegalStateException("Failed to build remote group summary.", exception);
            }
        }
        return array;
    }

    static Summary summarize(String packageName, List<RemoteCompatibilityLog.Entry> entries) {
        String normalizedPackage = AppPackageCatalog.normalizePackageName(packageName);
        Map<String, CaseStatus> statuses = new LinkedHashMap<>();
        for (RemoteCompatibilityMatrix.Case testCase : RemoteCompatibilityMatrix.all()) {
            statuses.put(testCase.label, new CaseStatus(testCase, null));
        }
        if (entries != null) {
            for (RemoteCompatibilityLog.Entry entry : entries) {
                if (!samePackage(normalizedPackage, entry.packageName)) {
                    continue;
                }
                CaseStatus status = statuses.get(entry.label);
                if (status != null && status.entry == null) {
                    statuses.put(entry.label, new CaseStatus(status.testCase, entry));
                }
            }
        }
        return new Summary(normalizedPackage, statuses);
    }

    private static boolean samePackage(String expected, String actual) {
        String normalizedActual = AppPackageCatalog.normalizePackageName(actual);
        if (expected.isEmpty()) {
            return normalizedActual.isEmpty();
        }
        return expected.equals(normalizedActual);
    }

    static String appFamily(String packageName) {
        return RemoteAppCatalog.reportFamilyForPackage(packageName);
    }

    private static String join(List<String> labels) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(labels.get(i));
        }
        return builder.toString();
    }

    private static JSONArray stringArray(List<String> labels) {
        JSONArray array = new JSONArray();
        if (labels != null) {
            for (String label : labels) {
                array.put(label);
            }
        }
        return array;
    }

    private static JSONArray stringArray(String[] labels) {
        JSONArray array = new JSONArray();
        if (labels != null) {
            for (String label : labels) {
                array.put(label);
            }
        }
        return array;
    }

    static final class Summary {
        final String packageName;
        final String appFamily;
        final Map<String, CaseStatus> statuses;
        final int totalCount;
        final int sentCount;
        final int passCount;
        final int failCount;
        final int unknownCount;
        final int localIncompleteCount;

        private Summary(String packageName, Map<String, CaseStatus> statuses) {
            this.packageName = packageName;
            this.appFamily = appFamily(packageName);
            this.statuses = statuses;
            this.totalCount = statuses.size();
            int sent = 0;
            int pass = 0;
            int fail = 0;
            int unknown = 0;
            int localIncomplete = 0;
            for (CaseStatus status : statuses.values()) {
                if (status.entry == null) {
                    continue;
                }
                sent++;
                if (!status.entry.localInputConnectionAccepted()) {
                    localIncomplete++;
                }
                if (RemoteCompatibilityLog.RESULT_PASS.equals(status.entry.manualResult)) {
                    pass++;
                } else if (RemoteCompatibilityLog.RESULT_FAIL.equals(status.entry.manualResult)) {
                    fail++;
                } else {
                    unknown++;
                }
            }
            sentCount = sent;
            passCount = pass;
            failCount = fail;
            unknownCount = unknown;
            localIncompleteCount = localIncomplete;
        }

        List<String> missingLabels() {
            java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            for (CaseStatus status : statuses.values()) {
                if (status.entry == null) {
                    labels.add(status.testCase.label);
                }
            }
            return labels;
        }

        List<String> unknownLabels() {
            java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            for (CaseStatus status : statuses.values()) {
                if (status.entry != null
                        && RemoteCompatibilityLog.RESULT_UNKNOWN.equals(status.entry.manualResult)) {
                    labels.add(status.testCase.label);
                }
            }
            return labels;
        }

        List<String> failedLabels() {
            java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            for (CaseStatus status : statuses.values()) {
                if (status.entry != null
                        && RemoteCompatibilityLog.RESULT_FAIL.equals(status.entry.manualResult)) {
                    labels.add(status.testCase.label);
                }
            }
            return labels;
        }

        List<String> localIncompleteLabels() {
            java.util.ArrayList<String> labels = new java.util.ArrayList<>();
            for (CaseStatus status : statuses.values()) {
                if (status.entry != null && !status.entry.localInputConnectionAccepted()) {
                    labels.add(status.testCase.label);
                }
            }
            return labels;
        }

        boolean manualRemoteResultRequired() {
            return unknownCount > 0 || !missingLabels().isEmpty() || localIncompleteCount > 0;
        }
    }

    static final class CaseStatus {
        final RemoteCompatibilityMatrix.Case testCase;
        final RemoteCompatibilityLog.Entry entry;

        private CaseStatus(RemoteCompatibilityMatrix.Case testCase, RemoteCompatibilityLog.Entry entry) {
            this.testCase = testCase;
            this.entry = entry;
        }

        String manualResult() {
            return entry == null ? RemoteCompatibilityLog.RESULT_UNKNOWN : entry.manualResult;
        }
    }
}
