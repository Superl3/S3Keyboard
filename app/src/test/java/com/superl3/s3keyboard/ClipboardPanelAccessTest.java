package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ClipboardPanelAccessTest {
    @Test
    public void secureFieldTakesPriorityOverOtherPanelStates() {
        assertEquals(
                ClipboardPanelAccess.Result.SECURE_FIELD,
                ClipboardPanelAccess.resolve(false, false, true, false));
    }

    @Test
    public void disabledHistoryReturnsAnActionableState() {
        assertEquals(
                ClipboardPanelAccess.Result.DISABLED,
                ClipboardPanelAccess.resolve(true, false, false, false));
    }

    @Test
    public void availablePanelTogglesBetweenShowAndHide() {
        assertEquals(
                ClipboardPanelAccess.Result.SHOW,
                ClipboardPanelAccess.resolve(true, true, false, false));
        assertEquals(
                ClipboardPanelAccess.Result.HIDE,
                ClipboardPanelAccess.resolve(true, true, false, true));
    }

    @Test
    public void missingViewIsReportedInsteadOfSilentlyIgnored() {
        assertEquals(
                ClipboardPanelAccess.Result.UNAVAILABLE,
                ClipboardPanelAccess.resolve(false, true, false, false));
    }
}
