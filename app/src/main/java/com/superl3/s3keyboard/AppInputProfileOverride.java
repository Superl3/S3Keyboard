package com.superl3.s3keyboard;

final class AppInputProfileOverride {
    static final AppInputProfileOverride AUTO = new AppInputProfileOverride(
            null, null, null, null, null);

    final KeyboardMode keyboardMode;
    final Boolean numberRowVisible;
    final Boolean allowComposingText;
    final Boolean allowTextConveniences;
    final Boolean remoteMode;

    AppInputProfileOverride(
            KeyboardMode keyboardMode,
            Boolean numberRowVisible,
            Boolean allowComposingText,
            Boolean allowTextConveniences,
            Boolean remoteMode) {
        this.keyboardMode = keyboardMode;
        this.numberRowVisible = numberRowVisible;
        this.allowComposingText = allowComposingText;
        this.allowTextConveniences = allowTextConveniences;
        this.remoteMode = remoteMode;
    }

    boolean isAuto() {
        return keyboardMode == null
                && numberRowVisible == null
                && allowComposingText == null
                && allowTextConveniences == null
                && remoteMode == null;
    }

    AppInputProfileOverride withKeyboardMode(KeyboardMode value) {
        return new AppInputProfileOverride(
                value, numberRowVisible, allowComposingText, allowTextConveniences, remoteMode);
    }

    AppInputProfileOverride withNumberRowVisible(Boolean value) {
        return new AppInputProfileOverride(
                keyboardMode, value, allowComposingText, allowTextConveniences, remoteMode);
    }

    AppInputProfileOverride withAllowComposingText(Boolean value) {
        return new AppInputProfileOverride(
                keyboardMode, numberRowVisible, value, allowTextConveniences, remoteMode);
    }

    AppInputProfileOverride withAllowTextConveniences(Boolean value) {
        return new AppInputProfileOverride(
                keyboardMode, numberRowVisible, allowComposingText, value, remoteMode);
    }

    AppInputProfileOverride withRemoteMode(Boolean value) {
        return new AppInputProfileOverride(
                keyboardMode, numberRowVisible, allowComposingText, allowTextConveniences, value);
    }
}
