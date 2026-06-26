package com.superl3.s3keyboard;

final class KeyboardErgonomicsOptions {
    static final KeyboardErgonomicsOptions DEFAULT = new KeyboardErgonomicsOptions(
            false,
            false,
            false,
            false,
            false,
            false,
            VisualConsistencyLevel.NONE);

    final boolean mainKeyCenteringEnabled;
    final boolean compactFunctionRailEnabled;
    final boolean ergonomicHitboxEnabled;
    final boolean ergonomicPositionAdjustEnabled;
    final boolean leftAssistRailEnabled;
    final boolean uniformGridGapEnabled;
    final VisualConsistencyLevel visualConsistencyLevel;

    KeyboardErgonomicsOptions(
            boolean mainKeyCenteringEnabled,
            boolean compactFunctionRailEnabled,
            boolean ergonomicHitboxEnabled,
            boolean ergonomicPositionAdjustEnabled,
            boolean leftAssistRailEnabled,
            boolean uniformGridGapEnabled,
            VisualConsistencyLevel visualConsistencyLevel) {
        this.mainKeyCenteringEnabled = mainKeyCenteringEnabled;
        this.compactFunctionRailEnabled = compactFunctionRailEnabled;
        this.ergonomicHitboxEnabled = ergonomicHitboxEnabled;
        this.ergonomicPositionAdjustEnabled = ergonomicPositionAdjustEnabled;
        this.leftAssistRailEnabled = leftAssistRailEnabled;
        this.uniformGridGapEnabled = uniformGridGapEnabled;
        this.visualConsistencyLevel = visualConsistencyLevel == null
                ? VisualConsistencyLevel.NONE
                : visualConsistencyLevel;
    }

    KeyboardErgonomicsOptions withMainKeyCentering(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                enabled,
                compactFunctionRailEnabled,
                ergonomicHitboxEnabled,
                ergonomicPositionAdjustEnabled,
                enabled && leftAssistRailEnabled,
                enabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withCompactFunctionRail(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                enabled,
                ergonomicHitboxEnabled,
                ergonomicPositionAdjustEnabled,
                leftAssistRailEnabled,
                uniformGridGapEnabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withErgonomicHitbox(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                compactFunctionRailEnabled,
                enabled,
                ergonomicPositionAdjustEnabled,
                leftAssistRailEnabled,
                uniformGridGapEnabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withErgonomicPositionAdjust(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                compactFunctionRailEnabled,
                ergonomicHitboxEnabled,
                enabled,
                leftAssistRailEnabled,
                uniformGridGapEnabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withLeftAssistRail(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                compactFunctionRailEnabled,
                ergonomicHitboxEnabled,
                ergonomicPositionAdjustEnabled,
                enabled,
                enabled || uniformGridGapEnabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withUniformGridGap(boolean enabled) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                compactFunctionRailEnabled,
                ergonomicHitboxEnabled,
                ergonomicPositionAdjustEnabled,
                leftAssistRailEnabled,
                enabled,
                visualConsistencyLevel);
    }

    KeyboardErgonomicsOptions withVisualConsistencyLevel(VisualConsistencyLevel level) {
        return new KeyboardErgonomicsOptions(
                mainKeyCenteringEnabled,
                compactFunctionRailEnabled,
                ergonomicHitboxEnabled,
                ergonomicPositionAdjustEnabled,
                leftAssistRailEnabled,
                uniformGridGapEnabled,
                level);
    }

    boolean affectsLayout() {
        return mainKeyCenteringEnabled
                || compactFunctionRailEnabled
                || ergonomicHitboxEnabled
                || (mainKeyCenteringEnabled && leftAssistRailEnabled)
                || (mainKeyCenteringEnabled && uniformGridGapEnabled)
                || (ergonomicPositionAdjustEnabled
                && visualConsistencyLevel.maxMainShiftRatio > 0f);
    }

    boolean sameValues(KeyboardErgonomicsOptions other) {
        return other != null
                && mainKeyCenteringEnabled == other.mainKeyCenteringEnabled
                && compactFunctionRailEnabled == other.compactFunctionRailEnabled
                && ergonomicHitboxEnabled == other.ergonomicHitboxEnabled
                && ergonomicPositionAdjustEnabled == other.ergonomicPositionAdjustEnabled
                && leftAssistRailEnabled == other.leftAssistRailEnabled
                && uniformGridGapEnabled == other.uniformGridGapEnabled
                && visualConsistencyLevel == other.visualConsistencyLevel;
    }
}
