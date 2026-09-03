package com.superl3.s3keyboard;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.function.Supplier;

final class AppProfileQuickSettingsController {
    private final Context context;
    private final Supplier<String> packageName;
    private final Supplier<AppInputProfile> profile;
    private final Supplier<EditorInputPolicy> policy;
    private final Runnable sessionReloader;
    private final Runnable dismissQuickSettings;

    AppProfileQuickSettingsController(
            Context context,
            Supplier<String> packageName,
            Supplier<AppInputProfile> profile,
            Supplier<EditorInputPolicy> policy,
            Runnable sessionReloader,
            Runnable dismissQuickSettings) {
        this.context = context;
        this.packageName = packageName;
        this.profile = profile;
        this.policy = policy;
        this.sessionReloader = RuntimeDefaults.runnable(sessionReloader);
        this.dismissQuickSettings = RuntimeDefaults.runnable(dismissQuickSettings);
    }

    void addTo(LinearLayout root) {
        String currentPackage = AppPackageCatalog.normalizePackageName(packageName.get());
        if (currentPackage.isEmpty()) {
            return;
        }
        AppInputProfile currentProfile = RuntimeDefaults.appInputProfile(profile.get());
        EditorInputPolicy currentPolicy = RuntimeDefaults.editorInputPolicy(policy.get());
        AppInputProfileOverrides overrides = KeyboardPreferences.loadAppInputProfileOverrides(context);
        AppInputProfileOverride current = overrides.forPackage(currentPackage);
        boolean hardRestricted = currentPolicy.password
                || currentPolicy.numberLike
                || currentPolicy.rawKeyInput;

        QuickPanelUi.addWithTop(
                context,
                root,
                QuickPanelUi.titleLabel(context, R.string.quick_app_profile_title),
                10);
        SettingsRowBuilder.bodyLabelRow(
                context,
                root,
                context.getString(
                        R.string.quick_app_profile_format,
                        currentProfile.id,
                        currentPackage),
                4);
        addOverrideButton(root, languageLabel(current.keyboardMode),
                v -> save(current.withKeyboardMode(nextLanguage(current.keyboardMode))));
        addBooleanOverrideButton(
                root,
                R.string.quick_app_profile_number_row,
                current.numberRowVisible,
                hardRestricted,
                v -> save(current.withNumberRowVisible(nextBoolean(current.numberRowVisible))));
        addBooleanOverrideButton(
                root,
                R.string.quick_app_profile_composing,
                current.allowComposingText,
                hardRestricted,
                v -> save(current.withAllowComposingText(nextBoolean(current.allowComposingText))));
        addBooleanOverrideButton(
                root,
                R.string.quick_app_profile_text_conveniences,
                current.allowTextConveniences,
                hardRestricted,
                v -> save(current.withAllowTextConveniences(
                        nextBoolean(current.allowTextConveniences))));
        addBooleanOverrideButton(
                root,
                R.string.quick_app_profile_remote,
                current.remoteMode,
                hardRestricted,
                v -> save(current.withRemoteMode(nextBoolean(current.remoteMode))));

        Button reset = QuickPanelUi.quickButton(
                context,
                context.getString(R.string.quick_app_profile_reset),
                false,
                v -> reset());
        reset.setEnabled(overrides.hasStoredOverride(currentPackage)
                || KeyboardPreferences.packageListContains(overrides.asciiPackages, currentPackage)
                || KeyboardPreferences.packageListContains(overrides.numberRowPackages, currentPackage)
                || KeyboardPreferences.packageListContains(overrides.noComposingPackages, currentPackage)
                || KeyboardPreferences.packageListContains(overrides.noTextConveniencesPackages, currentPackage));
        QuickPanelUi.addWithTop(context, root, reset, 6);
    }

    private void addBooleanOverrideButton(
            LinearLayout root,
            int labelResId,
            Boolean value,
            boolean locked,
            View.OnClickListener listener) {
        String state = locked
                ? context.getString(R.string.app_profile_value_locked)
                : booleanLabel(value);
        Button button = QuickPanelUi.quickButton(
                context,
                context.getString(labelResId, state),
                value != null,
                listener);
        button.setEnabled(!locked);
        QuickPanelUi.addWithTop(context, root, button, 6);
    }

    private void addOverrideButton(
            LinearLayout root,
            String label,
            View.OnClickListener listener) {
        QuickPanelUi.addWithTop(
                context,
                root,
                QuickPanelUi.quickButton(context, label, false, listener),
                6);
    }

    private void save(AppInputProfileOverride value) {
        String currentPackage = AppPackageCatalog.normalizePackageName(packageName.get());
        KeyboardPreferences.saveAppInputProfileOverride(context, currentPackage, value);
        sessionReloader.run();
        dismissQuickSettings.run();
    }

    private void reset() {
        String currentPackage = AppPackageCatalog.normalizePackageName(packageName.get());
        KeyboardPreferences.resetAppInputProfileOverride(context, currentPackage);
        sessionReloader.run();
        dismissQuickSettings.run();
    }

    private String languageLabel(KeyboardMode mode) {
        int valueRes = mode == KeyboardMode.HANGUL
                ? R.string.app_profile_value_hangul
                : mode == KeyboardMode.ENGLISH
                        ? R.string.app_profile_value_english
                        : R.string.app_profile_value_auto;
        return context.getString(
                R.string.quick_app_profile_language,
                context.getString(valueRes));
    }

    private String booleanLabel(Boolean value) {
        return context.getString(value == null
                ? R.string.app_profile_value_auto
                : value
                        ? R.string.app_profile_value_on
                        : R.string.app_profile_value_off);
    }

    private static KeyboardMode nextLanguage(KeyboardMode value) {
        if (value == null) {
            return KeyboardMode.HANGUL;
        }
        if (value == KeyboardMode.HANGUL) {
            return KeyboardMode.ENGLISH;
        }
        return null;
    }

    private static Boolean nextBoolean(Boolean value) {
        if (value == null) {
            return Boolean.TRUE;
        }
        if (value) {
            return Boolean.FALSE;
        }
        return null;
    }
}
