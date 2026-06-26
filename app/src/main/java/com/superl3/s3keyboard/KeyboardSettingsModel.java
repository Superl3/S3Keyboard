package com.superl3.s3keyboard;

final class KeyboardSettingsModel {
    final Appearance appearance;
    final Layout layout;
    final Input input;
    final Remote remote;
    final KeyboardErgonomicsOptions ergonomics;

    private KeyboardSettingsModel(
            Appearance appearance,
            Layout layout,
            Input input,
            Remote remote,
            KeyboardErgonomicsOptions ergonomics) {
        this.appearance = appearance;
        this.layout = layout;
        this.input = input;
        this.remote = remote;
        this.ergonomics = ergonomics == null ? KeyboardErgonomicsOptions.DEFAULT : ergonomics;
    }

    static KeyboardSettingsModel from(KeyboardSettings settings) {
        return from(settings, KeyboardErgonomicsOptions.DEFAULT);
    }

    static KeyboardSettingsModel from(
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions) {
        KeyboardSettings safe = settings == null ? KeyboardSettings.defaults() : settings;
        return new KeyboardSettingsModel(
                new Appearance(safe),
                new Layout(safe),
                new Input(safe),
                new Remote(safe),
                ergonomicsOptions);
    }

    static final class Appearance {
        final int keyIdleColor;
        final int keyPressedColor;
        final int keyboardBackgroundColor;
        final int accentColor;
        final int secondaryColor;
        final int functionKeyColor;
        final int accentKeyColor;
        final int borderColor;
        final String fontFamily;
        final int primaryTextSizePercent;
        final int secondaryTextSizePercent;
        final boolean primaryTextBold;
        final boolean primaryTextItalic;
        final boolean secondaryTextBold;
        final boolean secondaryTextItalic;
        final boolean followThemeTypography;
        final int keyRoundnessDp;
        final int keyBorderWidthDp;
        final int keyGapDp;
        final int hangulKeyGapDp;
        final int englishKeyGapDp;
        final boolean keyDepthEnabled;
        final int keyDepthDp;
        final boolean customDepthColorEnabled;
        final int depthColor;
        final LegendStylePreset legendStylePreset;
        final boolean pointKeycapStyleEnabled;
        final String modifierIconThemePackId;
        final String modifierIconOverridePackId;
        final String keyDisplayThemePackId;
        final String keyDisplayOverridePackId;
        final int keyColorOverrideCount;
        final int keyDisplayOverrideCount;
        final KeyboardVisualEffects visualEffects;

        private Appearance(KeyboardSettings settings) {
            keyIdleColor = settings.keyIdleColor;
            keyPressedColor = settings.keyPressedColor;
            keyboardBackgroundColor = settings.keyboardBackgroundColor;
            accentColor = settings.accentColor;
            secondaryColor = settings.secondaryColor;
            functionKeyColor = settings.functionKeyColor;
            accentKeyColor = settings.accentKeyColor;
            borderColor = settings.borderColor;
            fontFamily = settings.fontFamily;
            primaryTextSizePercent = settings.primaryTextSizePercent;
            secondaryTextSizePercent = settings.secondaryTextSizePercent;
            primaryTextBold = settings.primaryTextBold;
            primaryTextItalic = settings.primaryTextItalic;
            secondaryTextBold = settings.secondaryTextBold;
            secondaryTextItalic = settings.secondaryTextItalic;
            followThemeTypography = settings.followThemeTypography;
            keyRoundnessDp = settings.keyRoundnessDp;
            keyBorderWidthDp = settings.keyBorderWidthDp;
            keyGapDp = settings.keyGapDp;
            hangulKeyGapDp = settings.hangulKeyGapDp;
            englishKeyGapDp = settings.englishKeyGapDp;
            keyDepthEnabled = settings.keyDepthEnabled;
            keyDepthDp = settings.keyDepthDp;
            customDepthColorEnabled = settings.customDepthColorEnabled;
            depthColor = settings.depthColor;
            legendStylePreset = settings.legendStylePreset;
            pointKeycapStyleEnabled = settings.pointKeycapStyleEnabled;
            modifierIconThemePackId = settings.modifierIconThemePackId;
            modifierIconOverridePackId = settings.modifierIconOverridePackId;
            keyDisplayThemePackId = settings.keyDisplayThemePackId;
            keyDisplayOverridePackId = settings.keyDisplayOverridePackId;
            keyColorOverrideCount = settings.keyColorOverrides.size();
            keyDisplayOverrideCount = settings.keyDisplayOverrides.size();
            visualEffects = settings.visualEffects;
        }
    }

    static final class Layout {
        final KeyboardMode keyboardMode;
        final HandednessMode handednessMode;
        final int leftMarginDp;
        final int rightMarginDp;
        final int keyboardHeightDp;
        final int hangulKeyboardHeightDp;
        final int englishKeyboardHeightDp;
        final int hangulLeftPaddingDp;
        final int hangulRightPaddingDp;
        final int englishLeftPaddingDp;
        final int englishRightPaddingDp;
        final int hangulMainSpecialGapDp;
        final int keyboardTopPaddingDp;
        final int keyboardBottomPaddingDp;
        final int bottomRowTopPaddingDp;
        final int numberRowBottomGapDp;
        final int hangulSpecialColumnPercent;
        final boolean showHangulNumberRow;
        final boolean showEnglishNumberRow;
        final boolean forceNumberRow;
        final boolean showNumberRow;
        final AdditionalNumberRowColorMode additionalNumberRowColorMode;

        private Layout(KeyboardSettings settings) {
            keyboardMode = settings.keyboardMode;
            handednessMode = settings.handednessMode;
            leftMarginDp = settings.leftMarginDp;
            rightMarginDp = settings.rightMarginDp;
            keyboardHeightDp = settings.keyboardHeightDp;
            hangulKeyboardHeightDp = settings.hangulKeyboardHeightDp;
            englishKeyboardHeightDp = settings.englishKeyboardHeightDp;
            hangulLeftPaddingDp = settings.hangulLeftPaddingDp;
            hangulRightPaddingDp = settings.hangulRightPaddingDp;
            englishLeftPaddingDp = settings.englishLeftPaddingDp;
            englishRightPaddingDp = settings.englishRightPaddingDp;
            hangulMainSpecialGapDp = settings.hangulMainSpecialGapDp;
            keyboardTopPaddingDp = settings.keyboardTopPaddingDp;
            keyboardBottomPaddingDp = settings.keyboardBottomPaddingDp;
            bottomRowTopPaddingDp = settings.bottomRowTopPaddingDp;
            numberRowBottomGapDp = settings.numberRowBottomGapDp;
            hangulSpecialColumnPercent = settings.hangulSpecialColumnPercent;
            showHangulNumberRow = settings.showHangulNumberRow;
            showEnglishNumberRow = settings.showEnglishNumberRow;
            forceNumberRow = settings.forceNumberRow;
            showNumberRow = settings.showNumberRow;
            additionalNumberRowColorMode = settings.additionalNumberRowColorMode;
        }
    }

    static final class Input {
        final boolean hapticFeedbackEnabled;
        final int hitSlopDp;
        final int gestureThresholdDp;
        final int touchYOffsetDp;
        final int repeatStartDelayMs;
        final int repeatIntervalMs;
        final boolean englishDoubleSpacePeriodEnabled;
        final String enterKeyLabel;
        final boolean showHangulSlideHints;
        final boolean showEnglishSlideHints;
        final boolean showBeginnerTooltipPreview;

        private Input(KeyboardSettings settings) {
            hapticFeedbackEnabled = settings.hapticFeedbackEnabled;
            hitSlopDp = settings.hitSlopDp;
            gestureThresholdDp = settings.gestureThresholdDp;
            touchYOffsetDp = settings.touchYOffsetDp;
            repeatStartDelayMs = settings.repeatStartDelayMs;
            repeatIntervalMs = settings.repeatIntervalMs;
            englishDoubleSpacePeriodEnabled = settings.englishDoubleSpacePeriodEnabled;
            enterKeyLabel = settings.enterKeyLabel;
            showHangulSlideHints = settings.showHangulSlideHints;
            showEnglishSlideHints = settings.showEnglishSlideHints;
            showBeginnerTooltipPreview = settings.showBeginnerTooltipPreview;
        }
    }

    static final class Remote {
        final boolean remoteModeEnabled;
        final RemoteKeyPreset remoteKeyPreset;
        final RemoteImeShortcut remoteImeShortcut;

        private Remote(KeyboardSettings settings) {
            remoteModeEnabled = settings.remoteModeEnabled;
            remoteKeyPreset = settings.remoteKeyPreset;
            remoteImeShortcut = settings.remoteImeShortcut;
        }
    }
}
