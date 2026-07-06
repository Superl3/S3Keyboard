package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class RemoteCompatibilityLog {
    static final String RESULT_UNKNOWN = "unknown";
    static final String RESULT_PASS = "pass";
    static final String RESULT_FAIL = "fail";

    private static final String PREF_NAME = "keyboard_preferences";
    static final String KEY_ENTRIES = "remote_compatibility_test_log";
    private static final int MAX_ENTRIES = 32;

    private RemoteCompatibilityLog() {
    }

    static void record(
            Context context,
            String packageName,
            String label,
            int keyCode,
            int metaState,
            int acceptedEventCount) {
        List<Entry> entries = load(context);
        entries.add(0, new Entry(
                System.currentTimeMillis(),
                AppPackageCatalog.normalizePackageName(packageName),
                RuntimeDefaults.stringOrDefault(label, ""),
                keyCode,
                metaState,
                acceptedEventCount,
                RESULT_UNKNOWN));
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        save(context, entries);
    }

    static boolean markLatestResult(
            Context context,
            String packageName,
            String label,
            String manualResult) {
        String normalizedPackage = AppPackageCatalog.normalizePackageName(packageName);
        String normalizedLabel = RuntimeDefaults.stringOrDefault(label, "");
        if (normalizedLabel.isEmpty()) {
            return false;
        }
        List<Entry> entries = load(context);
        if (markLatestResult(entries, normalizedPackage, normalizedLabel, manualResult)) {
            save(context, entries);
            return true;
        }
        return false;
    }

    static boolean markLatestResult(
            List<Entry> entries,
            String packageName,
            String label,
            String manualResult) {
        if (entries == null || label == null || label.isEmpty()) {
            return false;
        }
        String normalizedPackage = AppPackageCatalog.normalizePackageName(packageName);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (label.equals(entry.label)
                    && AppPackageCatalog.normalizePackageName(entry.packageName).equals(normalizedPackage)) {
                entries.set(i, entry.withManualResult(manualResult));
                return true;
            }
        }
        return false;
    }

    static List<Entry> load(Context context) {
        return decode(prefs(context).getString(KEY_ENTRIES, ""));
    }

    static void clear(Context context) {
        prefs(context).edit().remove(KEY_ENTRIES).apply();
    }

    static String describeRecent(Context context, int maxEntries) {
        List<Entry> entries = load(context);
        if (entries.isEmpty()) {
            return context.getString(R.string.remote_test_history_empty);
        }
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        StringBuilder builder = new StringBuilder();
        int count = Math.min(Math.max(1, maxEntries), entries.size());
        for (int i = 0; i < count; i++) {
            Entry entry = entries.get(i);
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(format.format(new Date(entry.timestampMs)))
                    .append("  ")
                    .append(entry.label)
                    .append("  pkg=")
                    .append(entry.packageName.isEmpty() ? "unknown" : entry.packageName)
                    .append("  events=")
                    .append(entry.acceptedEventCount)
                    .append('/')
                    .append(entry.expectedEventCount())
                    .append("  result=")
                    .append(entry.manualResult);
        }
        return builder.toString();
    }

    static String encode(List<Entry> entries) {
        JSONArray array = new JSONArray();
        if (entries != null) {
            for (Entry entry : entries) {
                JSONObject object = new JSONObject();
                try {
                    object.put("timestampMs", entry.timestampMs);
                    object.put("packageName", entry.packageName);
                    object.put("label", entry.label);
                    object.put("keyCode", entry.keyCode);
                    object.put("metaState", entry.metaState);
                    object.put("eventCount", entry.acceptedEventCount);
                    object.put("acceptedEventCount", entry.acceptedEventCount);
                    object.put("expectedEventCount", entry.expectedEventCount());
                    object.put("localInputConnectionAccepted", entry.localInputConnectionAccepted());
                    object.put("manualResult", entry.manualResult);
                    array.put(object);
                } catch (JSONException ignored) {
                    // JSONObject should not reject primitive fields.
                }
            }
        }
        return array.toString();
    }

    static List<Entry> decode(String raw) {
        List<Entry> entries = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return entries;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                entries.add(new Entry(
                        object.optLong("timestampMs"),
                        object.optString("packageName"),
                        object.optString("label"),
                        object.optInt("keyCode"),
                        object.optInt("metaState"),
                        object.has("acceptedEventCount")
                                ? object.optInt("acceptedEventCount")
                                : object.optInt("eventCount"),
                        object.optString("manualResult", RESULT_UNKNOWN)));
            }
        } catch (JSONException ignored) {
            entries.clear();
        }
        return entries;
    }

    private static void save(Context context, List<Entry> entries) {
        prefs(context).edit().putString(KEY_ENTRIES, encode(entries)).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String normalizeResult(String manualResult) {
        if (RESULT_PASS.equals(manualResult) || RESULT_FAIL.equals(manualResult)) {
            return manualResult;
        }
        return RESULT_UNKNOWN;
    }

    static final class Entry {
        final long timestampMs;
        final String packageName;
        final String label;
        final int keyCode;
        final int metaState;
        final int acceptedEventCount;
        final String manualResult;

        Entry(
                long timestampMs,
                String packageName,
                String label,
                int keyCode,
                int metaState,
                int acceptedEventCount) {
            this(timestampMs, packageName, label, keyCode, metaState, acceptedEventCount, RESULT_UNKNOWN);
        }

        Entry(
                long timestampMs,
                String packageName,
                String label,
                int keyCode,
                int metaState,
                int acceptedEventCount,
                String manualResult) {
            this.timestampMs = timestampMs;
            this.packageName = RuntimeDefaults.stringOrDefault(packageName, "");
            this.label = RuntimeDefaults.stringOrDefault(label, "");
            this.keyCode = keyCode;
            this.metaState = metaState;
            this.acceptedEventCount = acceptedEventCount;
            this.manualResult = normalizeResult(manualResult);
        }

        Entry withManualResult(String result) {
            return new Entry(timestampMs, packageName, label, keyCode, metaState, acceptedEventCount, result);
        }

        int expectedEventCount() {
            return RemoteKeyEventSequence.eventCount(keyCode, metaState);
        }

        boolean localInputConnectionAccepted() {
            int expected = expectedEventCount();
            return expected > 0 && acceptedEventCount >= expected;
        }
    }
}
