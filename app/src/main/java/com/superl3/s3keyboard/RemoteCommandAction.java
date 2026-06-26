package com.superl3.s3keyboard;

final class RemoteCommandAction {
    enum Type {
        NONE,
        KEY,
        META_TAP,
        META_LOCK,
        IME_TOGGLE
    }

    static final RemoteCommandAction NONE = new RemoteCommandAction(Type.NONE, 0, 0);

    final Type type;
    final int keyCode;
    final int metaState;

    private RemoteCommandAction(Type type, int keyCode, int metaState) {
        this.type = type == null ? Type.NONE : type;
        this.keyCode = keyCode;
        this.metaState = metaState;
    }

    static RemoteCommandAction key(int keyCode, int metaState) {
        return keyCode == 0 ? NONE : new RemoteCommandAction(Type.KEY, keyCode, metaState);
    }

    static RemoteCommandAction metaTap(int metaState) {
        return new RemoteCommandAction(Type.META_TAP, 0, metaState);
    }

    static RemoteCommandAction metaLock(int metaState) {
        return new RemoteCommandAction(Type.META_LOCK, 0, metaState);
    }

    static RemoteCommandAction imeToggle() {
        return new RemoteCommandAction(Type.IME_TOGGLE, 0, 0);
    }
}
