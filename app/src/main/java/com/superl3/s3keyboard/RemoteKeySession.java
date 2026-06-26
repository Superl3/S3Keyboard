package com.superl3.s3keyboard;

final class RemoteKeySession {
    private int pendingMetaState;
    private int lockedMetaState;

    int pendingMetaState() {
        return pendingMetaState;
    }

    int lockedMetaState() {
        return lockedMetaState;
    }

    void reset() {
        pendingMetaState = 0;
        lockedMetaState = 0;
    }

    void tapModifier(int metaState) {
        if (metaState == 0) {
            return;
        }
        if ((lockedMetaState & metaState) == metaState) {
            lockedMetaState &= ~metaState;
            pendingMetaState &= ~metaState;
        } else {
            pendingMetaState = toggle(pendingMetaState, metaState);
        }
    }

    void toggleLockedModifier(int metaState) {
        if (metaState == 0) {
            return;
        }
        pendingMetaState &= ~metaState;
        lockedMetaState = toggle(lockedMetaState, metaState);
    }

    int consumeForKey(int explicitMetaState) {
        int combined = explicitMetaState | pendingMetaState | lockedMetaState;
        pendingMetaState = 0;
        return combined;
    }

    private static int toggle(int state, int metaState) {
        return (state & metaState) == metaState
                ? state & ~metaState
                : state | metaState;
    }
}
