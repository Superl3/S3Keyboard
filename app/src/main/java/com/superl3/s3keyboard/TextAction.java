package com.superl3.s3keyboard;

enum TextAction {
    CORRECT("correct"),
    POLISH("polish"),
    SHORTER("shorter"),
    POLITE("polite"),
    TRANSLATE("translate"),
    RESTORE_ORIGINAL("restore-original");

    final String id;

    TextAction(String id) {
        this.id = id;
    }

    boolean isLocallyAvailable() {
        return this == CORRECT || this == RESTORE_ORIGINAL;
    }
}
