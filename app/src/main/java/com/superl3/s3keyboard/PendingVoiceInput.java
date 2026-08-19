package com.superl3.s3keyboard;

final class PendingVoiceInput {
    final long requestId;
    final String targetPackage;
    final String text;
    final long createdAtMs;
    private int commitAttempts;

    PendingVoiceInput(long requestId, String targetPackage, String text, long createdAtMs) {
        this.requestId = requestId;
        this.targetPackage = RuntimeDefaults.stringOrDefault(targetPackage, "");
        this.text = RuntimeDefaults.stringOrDefault(text, "");
        this.createdAtMs = Math.max(0L, createdAtMs);
    }

    boolean targets(String packageName) {
        return !targetPackage.isEmpty() && targetPackage.equals(packageName);
    }

    boolean isExpired(long nowMs, long timeoutMs) {
        return Math.max(0L, nowMs) - createdAtMs > Math.max(0L, timeoutMs);
    }

    boolean recordFailedCommitAndShouldRetry(int maximumAttempts) {
        commitAttempts++;
        return commitAttempts < Math.max(1, maximumAttempts);
    }
}
