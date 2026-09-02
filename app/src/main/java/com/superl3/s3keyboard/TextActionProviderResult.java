package com.superl3.s3keyboard;

final class TextActionProviderResult {
    final String text;
    final TextActionProviderError error;

    private TextActionProviderResult(String text, TextActionProviderError error) {
        this.text = text;
        this.error = error;
    }

    static TextActionProviderResult success(String text) {
        return new TextActionProviderResult(text, null);
    }

    static TextActionProviderResult failure(TextActionProviderError error) {
        return new TextActionProviderResult(null,
                error == null ? TextActionProviderError.FAILED : error);
    }

    boolean succeeded() {
        return error == null;
    }
}
