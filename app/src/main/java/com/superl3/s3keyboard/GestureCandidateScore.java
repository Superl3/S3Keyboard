package com.superl3.s3keyboard;

final class GestureCandidateScore {
    static final GestureCandidateScore NONE = new GestureCandidateScore("", GestureAction.TAP, 0f, false);

    final String keyId;
    final GestureAction action;
    final float score;
    final boolean applied;

    GestureCandidateScore(String keyId, GestureAction action, float score, boolean applied) {
        this.keyId = RuntimeDefaults.stringOrDefault(keyId, "");
        this.action = action == null ? GestureAction.TAP : action;
        this.score = score;
        this.applied = applied;
    }

    boolean isPresent() {
        return !keyId.isEmpty();
    }
}
