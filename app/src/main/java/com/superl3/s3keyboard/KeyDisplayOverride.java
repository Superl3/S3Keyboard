package com.superl3.s3keyboard;

final class KeyDisplayOverride {
    static final String TYPE_ICON = "icon";
    static final String TYPE_TEXT = "text";

    final String type;
    final String value;

    private KeyDisplayOverride(String type, String value) {
        this.type = normalizeType(type);
        this.value = value == null ? "" : value;
    }

    static KeyDisplayOverride icon(String value) {
        return new KeyDisplayOverride(TYPE_ICON, value);
    }

    static KeyDisplayOverride text(String value) {
        return new KeyDisplayOverride(TYPE_TEXT, value);
    }

    static KeyDisplayOverride create(String type, String value) {
        String normalizedType = normalizeType(type);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return new KeyDisplayOverride(normalizedType, value);
    }

    boolean isIcon() {
        return TYPE_ICON.equals(type);
    }

    boolean isText() {
        return TYPE_TEXT.equals(type);
    }

    private static String normalizeType(String type) {
        return TYPE_TEXT.equals(type) ? TYPE_TEXT : TYPE_ICON;
    }
}
