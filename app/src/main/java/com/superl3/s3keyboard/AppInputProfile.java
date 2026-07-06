package com.superl3.s3keyboard;

final class AppInputProfile {
    static final AppInputProfile STANDARD = new AppInputProfile(
            "standard",
            false,
            null,
            null,
            null,
            null,
            "default");

    final String id;
    final boolean remoteMode;
    final Boolean preferAsciiLayout;
    final Boolean forceNumberRow;
    final Boolean allowComposingText;
    final Boolean allowTextConveniences;
    final String source;

    AppInputProfile(
            String id,
            boolean remoteMode,
            Boolean preferAsciiLayout,
            Boolean forceNumberRow,
            Boolean allowComposingText,
            Boolean allowTextConveniences) {
        this(
                id,
                remoteMode,
                preferAsciiLayout,
                forceNumberRow,
                allowComposingText,
                allowTextConveniences,
                id);
    }

    AppInputProfile(
            String id,
            boolean remoteMode,
            Boolean preferAsciiLayout,
            Boolean forceNumberRow,
            Boolean allowComposingText,
            Boolean allowTextConveniences,
            String source) {
        this.id = id == null ? "standard" : id;
        this.remoteMode = remoteMode;
        this.preferAsciiLayout = preferAsciiLayout;
        this.forceNumberRow = forceNumberRow;
        this.allowComposingText = allowComposingText;
        this.allowTextConveniences = allowTextConveniences;
        this.source = source == null || source.trim().isEmpty() ? this.id : source;
    }

    EditorInputPolicy apply(EditorInputPolicy policy) {
        EditorInputPolicy base = RuntimeDefaults.editorInputPolicy(policy);
        return base.withOverrides(
                preferAsciiLayout,
                forceNumberRow,
                allowComposingText,
                allowTextConveniences);
    }

    AppInputProfile withPolicyOverrides(
            Boolean preferAsciiLayout,
            Boolean forceNumberRow,
            Boolean allowComposingText,
            Boolean allowTextConveniences,
            String overrideSource) {
        boolean changed = false;
        Boolean nextPreferAsciiLayout = this.preferAsciiLayout;
        Boolean nextForceNumberRow = this.forceNumberRow;
        Boolean nextAllowComposingText = this.allowComposingText;
        Boolean nextAllowTextConveniences = this.allowTextConveniences;
        if (preferAsciiLayout != null && !preferAsciiLayout.equals(nextPreferAsciiLayout)) {
            nextPreferAsciiLayout = preferAsciiLayout;
            changed = true;
        }
        if (forceNumberRow != null && !forceNumberRow.equals(nextForceNumberRow)) {
            nextForceNumberRow = forceNumberRow;
            changed = true;
        }
        if (allowComposingText != null && !allowComposingText.equals(nextAllowComposingText)) {
            nextAllowComposingText = allowComposingText;
            changed = true;
        }
        if (allowTextConveniences != null && !allowTextConveniences.equals(nextAllowTextConveniences)) {
            nextAllowTextConveniences = allowTextConveniences;
            changed = true;
        }
        if (!changed) {
            return this;
        }
        return new AppInputProfile(
                id,
                remoteMode,
                nextPreferAsciiLayout,
                nextForceNumberRow,
                nextAllowComposingText,
                nextAllowTextConveniences,
                source + "+" + (overrideSource == null ? "override" : overrideSource));
    }
}
