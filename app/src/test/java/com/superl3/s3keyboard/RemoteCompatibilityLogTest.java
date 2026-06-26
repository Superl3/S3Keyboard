package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class RemoteCompatibilityLogTest {
    @Test
    public void entriesEncodeAndDecodeRoundTrip() {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        123L,
                        "com.limelight",
                        "Win+Space",
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON,
                        4),
                new RemoteCompatibilityLog.Entry(
                        456L,
                        "tv.parsec.client",
                        "F1",
                        KeyEvent.KEYCODE_F1,
                        0,
                        2));

        List<RemoteCompatibilityLog.Entry> decoded =
                RemoteCompatibilityLog.decode(RemoteCompatibilityLog.encode(entries));

        assertEquals(2, decoded.size());
        assertEquals("com.limelight", decoded.get(0).packageName);
        assertEquals("Win+Space", decoded.get(0).label);
        assertEquals(KeyEvent.KEYCODE_SPACE, decoded.get(0).keyCode);
        assertEquals(KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON, decoded.get(0).metaState);
        assertEquals(4, decoded.get(0).acceptedEventCount);
        assertEquals(RemoteCompatibilityLog.RESULT_UNKNOWN, decoded.get(0).manualResult);
        assertEquals("tv.parsec.client", decoded.get(1).packageName);
    }

    @Test
    public void encodedEntriesExposeAcceptedEventCountExplicitly() throws Exception {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(new RemoteCompatibilityLog.Entry(
                123L,
                "com.limelight",
                "F1",
                KeyEvent.KEYCODE_F1,
                0,
                0));

        JSONObject object = new JSONArray(RemoteCompatibilityLog.encode(entries)).getJSONObject(0);

        assertEquals(0, object.getInt("eventCount"));
        assertEquals(0, object.getInt("acceptedEventCount"));
        assertEquals(2, object.getInt("expectedEventCount"));
        assertEquals(false, object.getBoolean("localInputConnectionAccepted"));
    }

    @Test
    public void partialShortcutAcceptanceIsNotReportedAsLocallyAccepted() throws Exception {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(new RemoteCompatibilityLog.Entry(
                123L,
                "com.limelight",
                "Win+Space",
                KeyEvent.KEYCODE_SPACE,
                KeyEvent.META_META_ON,
                1));

        JSONObject object = new JSONArray(RemoteCompatibilityLog.encode(entries)).getJSONObject(0);

        assertEquals(1, object.getInt("acceptedEventCount"));
        assertEquals(4, object.getInt("expectedEventCount"));
        assertEquals(false, object.getBoolean("localInputConnectionAccepted"));
        assertEquals(false, entries.get(0).localInputConnectionAccepted());
    }

    @Test
    public void decodePrefersExplicitAcceptedEventCountWhenPresent() {
        String payload = "[{\"packageName\":\"com.limelight\",\"label\":\"F1\","
                + "\"keyCode\":131,\"metaState\":0,\"eventCount\":2,\"acceptedEventCount\":0}]";

        List<RemoteCompatibilityLog.Entry> decoded = RemoteCompatibilityLog.decode(payload);

        assertEquals(1, decoded.size());
        assertEquals(0, decoded.get(0).acceptedEventCount);
    }

    @Test
    public void entriesPreserveManualResultWhenPresent() {
        List<RemoteCompatibilityLog.Entry> entries = Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        123L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2,
                        RemoteCompatibilityLog.RESULT_PASS),
                new RemoteCompatibilityLog.Entry(
                        456L,
                        "com.limelight",
                        "Tab",
                        KeyEvent.KEYCODE_TAB,
                        0,
                        2,
                        RemoteCompatibilityLog.RESULT_FAIL));

        List<RemoteCompatibilityLog.Entry> decoded =
                RemoteCompatibilityLog.decode(RemoteCompatibilityLog.encode(entries));

        assertEquals(RemoteCompatibilityLog.RESULT_PASS, decoded.get(0).manualResult);
        assertEquals(RemoteCompatibilityLog.RESULT_FAIL, decoded.get(1).manualResult);
    }

    @Test
    public void markLatestResultUpdatesFirstMatchingPackageAndLabel() {
        List<RemoteCompatibilityLog.Entry> entries = new java.util.ArrayList<>(Arrays.asList(
                new RemoteCompatibilityLog.Entry(
                        456L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2),
                new RemoteCompatibilityLog.Entry(
                        123L,
                        "com.limelight",
                        "Esc",
                        KeyEvent.KEYCODE_ESCAPE,
                        0,
                        2)));

        boolean marked = RemoteCompatibilityLog.markLatestResult(
                entries,
                "com.limelight",
                "Esc",
                RemoteCompatibilityLog.RESULT_PASS);

        assertEquals(true, marked);
        assertEquals(RemoteCompatibilityLog.RESULT_PASS, entries.get(0).manualResult);
        assertEquals(RemoteCompatibilityLog.RESULT_UNKNOWN, entries.get(1).manualResult);
    }

    @Test
    public void invalidPayloadDecodesAsEmptyList() {
        assertEquals(0, RemoteCompatibilityLog.decode("not-json").size());
    }
}
