package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.KeyEvent;

import org.junit.Test;

public final class RemoteKeyStrokeTest {
    @Test
    public void mapsLettersDigitsAndShiftedSymbols() {
        assertStroke("a", KeyEvent.KEYCODE_A, 0);
        assertStroke("A", KeyEvent.KEYCODE_A, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON);
        assertStroke("7", KeyEvent.KEYCODE_7, 0);
        assertStroke("&", KeyEvent.KEYCODE_7, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON);
        assertStroke("/", KeyEvent.KEYCODE_SLASH, 0);
        assertStroke("?", KeyEvent.KEYCODE_SLASH, KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON);
    }

    private static void assertStroke(String text, int keyCode, int metaState) {
        RemoteKeyStroke stroke = RemoteKeyStroke.forText(text);
        assertNotNull(stroke);
        assertEquals(keyCode, stroke.keyCode);
        assertEquals(metaState, stroke.metaState);
    }
}
