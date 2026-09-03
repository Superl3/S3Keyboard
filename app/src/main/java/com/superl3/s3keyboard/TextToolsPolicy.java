package com.superl3.s3keyboard;

final class TextToolsPolicy {
    private TextToolsPolicy() {
    }

    static boolean allows(EditorInputPolicy policy, boolean remoteModeEnabled) {
        EditorInputPolicy current = policy == null ? EditorInputPolicy.DEFAULT : policy;
        return current.allowTextConveniences
                && !current.password
                && !current.rawKeyInput
                && !current.replacesMainRows()
                && !remoteModeEnabled;
    }

    static boolean allowsInsertion(
            EditorInputPolicy policy,
            boolean remoteModeEnabled,
            String text) {
        return text != null && !text.isEmpty() && allows(policy, remoteModeEnabled);
    }
}
