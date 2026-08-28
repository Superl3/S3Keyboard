package com.superl3.s3keyboard;

import org.json.JSONException;
import org.json.JSONObject;

final class KeyboardSettingsSections {
    final Appearance appearance;
    final Layout layout;
    final Input input;
    final Remote remote;
    final Ergonomics ergonomics;

    private KeyboardSettingsSections(
            Appearance appearance,
            Layout layout,
            Input input,
            Remote remote,
            Ergonomics ergonomics) {
        this.appearance = appearance;
        this.layout = layout;
        this.input = input;
        this.remote = remote;
        this.ergonomics = ergonomics;
    }

    static KeyboardSettingsSections from(KeyboardSettings settings) {
        return from(settings, null);
    }

    static KeyboardSettingsSections from(
            KeyboardSettings settings,
            KeyboardErgonomicsOptions ergonomicsOptions) {
        KeyboardSettingsModel model = KeyboardSettingsModel.from(settings, ergonomicsOptions);
        return new KeyboardSettingsSections(
                Appearance.from(model.appearance),
                Layout.from(model.layout),
                Input.from(model.input),
                Remote.from(model.remote),
                Ergonomics.from(model.ergonomics));
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

        private Appearance(KeyboardSettingsModel.Appearance model) {
            keyIdleColor = model.keyIdleColor;
            keyPressedColor = model.keyPressedColor;
            keyboardBackgroundColor = model.keyboardBackgroundColor;
            accentColor = model.accentColor;
            secondaryColor = model.secondaryColor;
            functionKeyColor = model.functionKeyColor;
            accentKeyColor = model.accentKeyColor;
            borderColor = model.borderColor;
            fontFamily = model.fontFamily;
            primaryTextSizePercent = model.primaryTextSizePercent;
            secondaryTextSizePercent = model.secondaryTextSizePercent;
            primaryTextBold = model.primaryTextBold;
            primaryTextItalic = model.primaryTextItalic;
            secondaryTextBold = model.secondaryTextBold;
            secondaryTextItalic = model.secondaryTextItalic;
            followThemeTypography = model.followThemeTypography;
            keyRoundnessDp = model.keyRoundnessDp;
            keyBorderWidthDp = model.keyBorderWidthDp;
            keyGapDp = model.keyGapDp;
            hangulKeyGapDp = model.hangulKeyGapDp;
            englishKeyGapDp = model.englishKeyGapDp;
            keyDepthEnabled = model.keyDepthEnabled;
            keyDepthDp = model.keyDepthDp;
            customDepthColorEnabled = model.customDepthColorEnabled;
            depthColor = model.depthColor;
            legendStylePreset = model.legendStylePreset;
            pointKeycapStyleEnabled = model.pointKeycapStyleEnabled;
            modifierIconThemePackId = model.modifierIconThemePackId;
            modifierIconOverridePackId = model.modifierIconOverridePackId;
            keyDisplayThemePackId = model.keyDisplayThemePackId;
            keyDisplayOverridePackId = model.keyDisplayOverridePackId;
            keyColorOverrideCount = model.keyColorOverrideCount;
            keyDisplayOverrideCount = model.keyDisplayOverrideCount;
            visualEffects = model.visualEffects;
        }

        static Appearance from(KeyboardSettingsModel.Appearance model) {
            return new Appearance(model);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            put(object, "keyIdleColor", colorHex(keyIdleColor));
            put(object, "keyPressedColor", colorHex(keyPressedColor));
            put(object, "keyboardBackgroundColor", colorHex(keyboardBackgroundColor));
            put(object, "accentColor", colorHex(accentColor));
            put(object, "secondaryColor", colorHex(secondaryColor));
            put(object, "functionKeyColor", colorHex(functionKeyColor));
            put(object, "accentKeyColor", colorHex(accentKeyColor));
            put(object, "borderColor", colorHex(borderColor));
            put(object, "fontFamily", fontFamily);
            put(object, "primaryTextSizePercent", primaryTextSizePercent);
            put(object, "secondaryTextSizePercent", secondaryTextSizePercent);
            put(object, "primaryTextBold", primaryTextBold);
            put(object, "primaryTextItalic", primaryTextItalic);
            put(object, "secondaryTextBold", secondaryTextBold);
            put(object, "secondaryTextItalic", secondaryTextItalic);
            put(object, "followThemeTypography", followThemeTypography);
            put(object, "keyRoundnessDp", keyRoundnessDp);
            put(object, "keyBorderWidthDp", keyBorderWidthDp);
            put(object, "keyGapDp", keyGapDp);
            put(object, "hangulKeyGapDp", hangulKeyGapDp);
            put(object, "englishKeyGapDp", englishKeyGapDp);
            put(object, "keyDepthEnabled", keyDepthEnabled);
            put(object, "keyDepthDp", keyDepthDp);
            put(object, "customDepthColorEnabled", customDepthColorEnabled);
            put(object, "depthColor", colorHex(depthColor));
            put(object, "legendStylePreset", legendStylePreset.preferenceValue);
            put(object, "pointKeycapStyleEnabled", pointKeycapStyleEnabled);
            put(object, "modifierIconThemePackId", modifierIconThemePackId);
            put(object, "modifierIconOverridePackId", modifierIconOverridePackId);
            put(object, "keyDisplayThemePackId", keyDisplayThemePackId);
            put(object, "keyDisplayOverridePackId", keyDisplayOverridePackId);
            put(object, "keyColorOverrideCount", keyColorOverrideCount);
            put(object, "keyDisplayOverrideCount", keyDisplayOverrideCount);
            put(object, "visualEffects", visualEffectsToJson(visualEffects));
            return object;
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

        private Layout(KeyboardSettingsModel.Layout model) {
            keyboardMode = model.keyboardMode;
            handednessMode = model.handednessMode;
            leftMarginDp = model.leftMarginDp;
            rightMarginDp = model.rightMarginDp;
            keyboardHeightDp = model.keyboardHeightDp;
            hangulKeyboardHeightDp = model.hangulKeyboardHeightDp;
            englishKeyboardHeightDp = model.englishKeyboardHeightDp;
            hangulLeftPaddingDp = model.hangulLeftPaddingDp;
            hangulRightPaddingDp = model.hangulRightPaddingDp;
            englishLeftPaddingDp = model.englishLeftPaddingDp;
            englishRightPaddingDp = model.englishRightPaddingDp;
            hangulMainSpecialGapDp = model.hangulMainSpecialGapDp;
            keyboardTopPaddingDp = model.keyboardTopPaddingDp;
            keyboardBottomPaddingDp = model.keyboardBottomPaddingDp;
            bottomRowTopPaddingDp = model.bottomRowTopPaddingDp;
            numberRowBottomGapDp = model.numberRowBottomGapDp;
            hangulSpecialColumnPercent = model.hangulSpecialColumnPercent;
            showHangulNumberRow = model.showHangulNumberRow;
            showEnglishNumberRow = model.showEnglishNumberRow;
            forceNumberRow = model.forceNumberRow;
            showNumberRow = model.showNumberRow;
            additionalNumberRowColorMode = model.additionalNumberRowColorMode;
        }

        static Layout from(KeyboardSettingsModel.Layout model) {
            return new Layout(model);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            put(object, "keyboardMode", keyboardMode.name());
            put(object, "handednessMode", handednessMode.name());
            put(object, "leftMarginDp", leftMarginDp);
            put(object, "rightMarginDp", rightMarginDp);
            put(object, "keyboardHeightDp", keyboardHeightDp);
            put(object, "hangulKeyboardHeightDp", hangulKeyboardHeightDp);
            put(object, "englishKeyboardHeightDp", englishKeyboardHeightDp);
            put(object, "hangulLeftPaddingDp", hangulLeftPaddingDp);
            put(object, "hangulRightPaddingDp", hangulRightPaddingDp);
            put(object, "englishLeftPaddingDp", englishLeftPaddingDp);
            put(object, "englishRightPaddingDp", englishRightPaddingDp);
            put(object, "hangulMainSpecialGapDp", hangulMainSpecialGapDp);
            put(object, "keyboardTopPaddingDp", keyboardTopPaddingDp);
            put(object, "keyboardBottomPaddingDp", keyboardBottomPaddingDp);
            put(object, "bottomRowTopPaddingDp", bottomRowTopPaddingDp);
            put(object, "numberRowBottomGapDp", numberRowBottomGapDp);
            put(object, "hangulSpecialColumnPercent", hangulSpecialColumnPercent);
            put(object, "showHangulNumberRow", showHangulNumberRow);
            put(object, "showEnglishNumberRow", showEnglishNumberRow);
            put(object, "forceNumberRow", forceNumberRow);
            put(object, "showNumberRow", showNumberRow);
            put(object, "additionalNumberRowColorMode", additionalNumberRowColorMode.name());
            return object;
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

        private Input(KeyboardSettingsModel.Input model) {
            hapticFeedbackEnabled = model.hapticFeedbackEnabled;
            hitSlopDp = model.hitSlopDp;
            gestureThresholdDp = model.gestureThresholdDp;
            touchYOffsetDp = model.touchYOffsetDp;
            repeatStartDelayMs = model.repeatStartDelayMs;
            repeatIntervalMs = model.repeatIntervalMs;
            englishDoubleSpacePeriodEnabled = model.englishDoubleSpacePeriodEnabled;
            enterKeyLabel = model.enterKeyLabel;
            showHangulSlideHints = model.showHangulSlideHints;
            showEnglishSlideHints = model.showEnglishSlideHints;
            showBeginnerTooltipPreview = model.showBeginnerTooltipPreview;
        }

        static Input from(KeyboardSettingsModel.Input model) {
            return new Input(model);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            put(object, "hapticFeedbackEnabled", hapticFeedbackEnabled);
            put(object, "hitSlopDp", hitSlopDp);
            put(object, "gestureThresholdDp", gestureThresholdDp);
            put(object, "touchYOffsetDp", touchYOffsetDp);
            put(object, "repeatStartDelayMs", repeatStartDelayMs);
            put(object, "repeatIntervalMs", repeatIntervalMs);
            put(object, "englishDoubleSpacePeriodEnabled", englishDoubleSpacePeriodEnabled);
            put(object, "enterKeyLabel", enterKeyLabel);
            put(object, "showHangulSlideHints", showHangulSlideHints);
            put(object, "showEnglishSlideHints", showEnglishSlideHints);
            put(object, "showBeginnerTooltipPreview", showBeginnerTooltipPreview);
            return object;
        }
    }

    static final class Remote {
        final boolean remoteModeEnabled;
        final RemoteKeyPreset remoteKeyPreset;
        final RemoteImeShortcut remoteImeShortcut;

        private Remote(KeyboardSettingsModel.Remote model) {
            remoteModeEnabled = model.remoteModeEnabled;
            remoteKeyPreset = model.remoteKeyPreset;
            remoteImeShortcut = model.remoteImeShortcut;
        }

        static Remote from(KeyboardSettingsModel.Remote model) {
            return new Remote(model);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            put(object, "remoteModeEnabled", remoteModeEnabled);
            put(object, "remoteKeyPreset", remoteKeyPreset.preferenceValue);
            put(object, "remoteImeShortcut", remoteImeShortcut.preferenceValue);
            return object;
        }
    }

    static final class Ergonomics {
        final boolean mainKeyCenteringEnabled;
        final boolean compactFunctionRailEnabled;
        final boolean ergonomicHitboxEnabled;
        final boolean ergonomicPositionAdjustEnabled;
        final boolean leftAssistRailEnabled;
        final boolean uniformGridGapEnabled;
        final VisualConsistencyLevel visualConsistencyLevel;

        private Ergonomics(KeyboardErgonomicsOptions options) {
            KeyboardErgonomicsOptions safe = RuntimeDefaults.keyboardErgonomics(options);
            mainKeyCenteringEnabled = safe.mainKeyCenteringEnabled;
            compactFunctionRailEnabled = safe.compactFunctionRailEnabled;
            ergonomicHitboxEnabled = safe.ergonomicHitboxEnabled;
            ergonomicPositionAdjustEnabled = safe.ergonomicPositionAdjustEnabled;
            leftAssistRailEnabled = safe.leftAssistRailEnabled;
            uniformGridGapEnabled = safe.uniformGridGapEnabled;
            visualConsistencyLevel = safe.visualConsistencyLevel;
        }

        static Ergonomics from(KeyboardErgonomicsOptions options) {
            return new Ergonomics(options);
        }

        JSONObject toJson() {
            JSONObject object = new JSONObject();
            put(object, "mainKeyCenteringEnabled", mainKeyCenteringEnabled);
            put(object, "compactFunctionRailEnabled", compactFunctionRailEnabled);
            put(object, "ergonomicHitboxEnabled", ergonomicHitboxEnabled);
            put(object, "ergonomicPositionAdjustEnabled", ergonomicPositionAdjustEnabled);
            put(object, "leftAssistRailEnabled", leftAssistRailEnabled);
            put(object, "uniformGridGapEnabled", uniformGridGapEnabled);
            put(object, "visualConsistencyLevel", visualConsistencyLevel.preferenceValue);
            return object;
        }
    }

    private static void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode settings section.", exception);
        }
    }

    private static JSONObject visualEffectsToJson(KeyboardVisualEffects effects) {
        KeyboardVisualEffects safe = RuntimeDefaults.keyboardVisualEffects(effects);
        JSONObject object = new JSONObject();
        put(object, "materialStyle", safe.materialStyle);
        put(object, "blurEnabled", safe.blurEnabled);
        put(object, "blurRadiusDp", safe.blurRadiusDp);
        put(object, "metallicEnabled", safe.metallicEnabled);
        put(object, "metallicStrengthPercent", safe.metallicStrengthPercent);
        put(object, "angularPreviewBubble", safe.angularPreviewBubble);
        put(object, "keyFaceGradientEnabled", safe.keyFaceGradientEnabled);
        put(object, "keyFaceGradientStrengthPercent", safe.keyFaceGradientStrengthPercent);
        put(object, "keyFaceGradientStartColor", colorHex(safe.keyFaceGradientStartColor));
        put(object, "keyFaceGradientEndColor", colorHex(safe.keyFaceGradientEndColor));
        put(object, "keyFaceGradientCurve", safe.keyFaceGradientCurve);
        put(object, "panelGradientEnabled", safe.panelGradientEnabled);
        put(object, "panelGradientStartColor", colorHex(safe.panelGradientStartColor));
        put(object, "panelGradientEndColor", colorHex(safe.panelGradientEndColor));
        return object;
    }

    private static String colorHex(int color) {
        return String.format(java.util.Locale.ROOT, "#%08X", color);
    }
}
