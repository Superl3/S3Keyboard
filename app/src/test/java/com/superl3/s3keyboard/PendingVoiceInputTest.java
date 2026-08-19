package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PendingVoiceInputTest {
    @Test
    public void resultCanOnlyReturnToTheOriginalEditorPackage() {
        PendingVoiceInput pending = new PendingVoiceInput(7L, "com.example.notes", "안녕하세요", 1_000L);

        assertTrue(pending.targets("com.example.notes"));
        assertFalse(pending.targets("com.example.chat"));
        assertFalse(pending.targets(""));
    }

    @Test
    public void resultExpiresAndRetryCountIsBounded() {
        PendingVoiceInput pending = new PendingVoiceInput(7L, "com.example.notes", "hello", 1_000L);

        assertFalse(pending.isExpired(30_999L, 30_000L));
        assertTrue(pending.isExpired(31_001L, 30_000L));
        assertTrue(pending.recordFailedCommitAndShouldRetry(4));
        assertTrue(pending.recordFailedCommitAndShouldRetry(4));
        assertTrue(pending.recordFailedCommitAndShouldRetry(4));
        assertFalse(pending.recordFailedCommitAndShouldRetry(4));
    }
}
