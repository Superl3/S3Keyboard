package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class LocalDataControlsControllerTest {
    @Test
    public void countJsonArrayReturnsLengthOnlyForValidArrays() {
        assertEquals(0, LocalDataControlsController.countJsonArray(null));
        assertEquals(0, LocalDataControlsController.countJsonArray(""));
        assertEquals(0, LocalDataControlsController.countJsonArray("not json"));
        assertEquals(0, LocalDataControlsController.countJsonArray("{}"));
        assertEquals(2, LocalDataControlsController.countJsonArray("[{},{}]"));
    }

    @Test
    public void hasMeaningfulStoredValueTreatsEmptyJsonAsAbsent() {
        assertFalse(LocalDataControlsController.hasMeaningfulStoredValue(null));
        assertFalse(LocalDataControlsController.hasMeaningfulStoredValue(""));
        assertFalse(LocalDataControlsController.hasMeaningfulStoredValue("   "));
        assertFalse(LocalDataControlsController.hasMeaningfulStoredValue("{}"));
        assertFalse(LocalDataControlsController.hasMeaningfulStoredValue("[]"));

        assertTrue(LocalDataControlsController.hasMeaningfulStoredValue("{\"x\":1}"));
        assertTrue(LocalDataControlsController.hasMeaningfulStoredValue("[{\"x\":1}]"));
    }

    @Test
    public void summarySnapshotKeepsStoredLocalDataCounts() {
        LocalDataControlsController.Summary summary = new LocalDataControlsController.Summary(
                true,
                2,
                true,
                3,
                4,
                false,
                5);

        assertTrue(summary.clipboardHistoryEnabled);
        assertEquals(2, summary.clipboardEntryCount);
        assertTrue(summary.touchBiasStatsPresent);
        assertEquals(3, summary.typingPatternEventCount);
        assertEquals(4, summary.typingEventJournalEventCount);
        assertFalse(summary.dingulTouchProfilePresent);
        assertEquals(5, summary.remoteCompatibilityEntryCount);
    }

    @Test
    public void managedLocalDataKeysCoverInternalRiskySchemaEntries() {
        Set<String> managedKeys = new HashSet<>(Arrays.asList(
                LocalDataControlsController.managedLocalDataKeys()));

        for (KeyboardSettingsSchema.Entry entry : KeyboardSettingsSchema.entries()) {
            if (entry.userFacing || entry.risk == KeyboardSettingsSchema.StorageRisk.NONE) {
                continue;
            }
            assertTrue(
                    "Internal local data key must be owned by LocalDataControlsController: " + entry.key,
                    managedKeys.contains(entry.key));
        }
    }
}
