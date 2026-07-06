package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private KeyboardSettings settings;
    private KeyboardLayoutProfiles layoutProfiles;
    private KeyboardErgonomicsOptions ergonomicsOptions = KeyboardErgonomicsOptions.DEFAULT;
    private AndroidImeSettingsController androidImeSettingsController;
    private LayoutSettingsController layoutSettingsController;
    private InputAssistanceSettingsController inputAssistanceSettingsController;
    private InputFeelSettingsController inputFeelSettingsController;
    private RemoteWindowsSettingsController remoteWindowsSettingsController;
    private TypographySettingsController typographySettingsController;
    private DisplayStyleSettingsController displayStyleSettingsController;
    private AppearanceSettingsController appearanceSettingsController;
    private MotionEffectSettingsController motionEffectSettingsController;
    private boolean demoShowKeyboard;
    private DemoFieldProfile demoFieldProfile = DemoFieldProfile.STANDARD;
    private SettingsHubController settingsHubController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        loadCurrentPreferences();
        setContentView(createContentView());
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentPreferences();
        syncControls();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (settingsHubController != null) {
            settingsHubController.hideKeyboardWhenTouchingOutside(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void applyIntentOverrides(Intent intent) {
        DemoSettingsIntentOverrides.Result result = DemoSettingsIntentOverrides.apply(
                this,
                intent,
                settings,
                demoFieldProfile,
                demoShowKeyboard,
                isDebuggableBuild());
        settings = result.settings;
        demoFieldProfile = result.fieldProfile;
        demoShowKeyboard = result.showKeyboard;
    }

    private void loadCurrentPreferences() {
        settings = KeyboardPreferences.load(this);
        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        ergonomicsOptions = KeyboardPreferences.loadErgonomicsOptions(this);
        KeyboardPreferences.saveFloatingModeEnabled(this, false);
        applyIntentOverrides(getIntent());
    }

    private ScrollView createContentView() {
        int padding = SettingsRowBuilder.dp(this, 16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout root = SettingsRowBuilder.vertical(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, padding, padding, padding);
        scrollView.addView(root);

        TextView title = SettingsRowBuilder.label(this, R.string.app_name);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = SettingsRowBuilder.matchWrap();
        titleParams.bottomMargin = SettingsRowBuilder.dp(this, 12);
        root.addView(title, titleParams);

        LinearLayout inputSection = addExpandableSection(
                root,
                getString(R.string.settings_input_feel_section),
                true);
        inputFeelSettingsController = new InputFeelSettingsController(
                this,
                this::settings,
                this::saveSettings,
                this::syncControls);
        inputFeelSettingsController.addTo(inputSection);

        LinearLayout hubSection = addExpandableSection(root, getString(R.string.settings_hub_title), true);
        settingsHubController = new SettingsHubController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        settingsHubController.addTo(hubSection, demoFieldProfile, demoShowKeyboard);
        // Keep existing preference wiring alive while appearance editing moves to ThemeEditor.
        initializeHiddenAppearanceControls();

        LinearLayout layoutSection = addExpandableSection(
                root,
                getString(R.string.settings_layout_section),
                true);
        layoutSettingsController = new LayoutSettingsController(
                this,
                this::settings,
                this::ergonomicsOptions,
                this::markCurrentThemeCustom,
                this::saveSettings,
                this::saveHangulLayoutProfile,
                this::saveEnglishLayoutProfile,
                this::saveErgonomicsOptions,
                this::syncControls);
        layoutSettingsController.addTo(layoutSection);

        LinearLayout displaySection = addExpandableSection(
                root,
                getString(R.string.settings_display_section),
                true);
        addVisibleVisualControls(displaySection);

        LinearLayout reservedSection = addExpandableSection(
                root,
                getString(R.string.settings_reserved_phrase_section),
                false);
        new ReservedPhraseSettingsController(this).addTo(reservedSection);

        LinearLayout remoteSection = addExpandableSection(
                root,
                getString(R.string.settings_remote_windows_section),
                false);
        remoteWindowsSettingsController = new RemoteWindowsSettingsController(
                this,
                this::settings,
                this::saveSettings,
                this::syncControls);
        remoteWindowsSettingsController.addTo(remoteSection);

        LinearLayout androidSection = addExpandableSection(
                root,
                getString(R.string.settings_android_ime_section),
                false);
        androidImeSettingsController =
                new AndroidImeSettingsController(this, this::isDebuggableBuild, this::syncControls);
        androidImeSettingsController.addTo(androidSection);

        return scrollView;
    }

    private void addVisibleVisualControls(LinearLayout root) {
        displayStyleSettingsController = new DisplayStyleSettingsController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        displayStyleSettingsController.addPackControlsTo(root);

        typographySettingsController = new TypographySettingsController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        typographySettingsController.addTo(root);

        displayStyleSettingsController.addPointKeycapControlTo(root);

        inputAssistanceSettingsController = new InputAssistanceSettingsController(
                this,
                this::settings,
                this::ergonomicsOptions,
                this::isDebuggableBuild,
                this::markCurrentThemeCustom,
                this::saveSettings,
                this::saveSettingsAndErgonomics,
                this::syncControls);
        inputAssistanceSettingsController.addTo(root);

        motionEffectSettingsController = new MotionEffectSettingsController(this, this::syncControls);
        motionEffectSettingsController.addTo(root);
    }

    private void initializeHiddenAppearanceControls() {
        appearanceSettingsController = new AppearanceSettingsController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        appearanceSettingsController.initializeHiddenControls();
    }

    private KeyboardSettings settings() {
        return settings;
    }

    private KeyboardErgonomicsOptions ergonomicsOptions() {
        return ergonomicsOptions;
    }

    private void saveSettings(KeyboardSettings newSettings) {
        settings = newSettings;
        saveAndSync();
    }

    private void saveSettingsAndErgonomics(
            KeyboardSettings newSettings,
            KeyboardErgonomicsOptions newErgonomicsOptions) {
        settings = newSettings;
        ergonomicsOptions = newErgonomicsOptions;
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        saveAndSync();
    }

    private void saveHangulLayoutProfile(KeyboardLayoutProfile profile) {
        layoutProfiles = layoutProfiles.withHangulLayout(profile);
        KeyboardPreferences.saveHangulLayoutProfile(this, profile);
    }

    private void saveEnglishLayoutProfile(KeyboardLayoutProfile profile) {
        layoutProfiles = layoutProfiles.withEnglishLayout(profile);
        KeyboardPreferences.saveEnglishLayoutProfile(this, profile);
    }

    private void saveErgonomicsOptions(KeyboardErgonomicsOptions options) {
        ergonomicsOptions = options;
        saveErgonomicsAndSync();
    }

    private void saveAndSync() {
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void saveErgonomicsAndSync() {
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        syncControls();
    }

    private void syncControls() {
        if (layoutSettingsController == null) {
            return;
        }

        layoutSettingsController.sync(settings, layoutProfiles);
        if (androidImeSettingsController != null) {
            androidImeSettingsController.sync();
        }
        if (inputAssistanceSettingsController != null) {
            inputAssistanceSettingsController.sync(settings);
        }
        if (inputFeelSettingsController != null) {
            inputFeelSettingsController.sync(settings);
        }
        if (remoteWindowsSettingsController != null) {
            remoteWindowsSettingsController.sync(settings);
        }
        if (typographySettingsController != null) {
            typographySettingsController.sync(settings);
        }
        if (displayStyleSettingsController != null) {
            displayStyleSettingsController.sync(settings);
        }
        if (appearanceSettingsController != null) {
            appearanceSettingsController.sync(settings);
        }
        if (motionEffectSettingsController != null) {
            motionEffectSettingsController.sync();
        }
    }

    private LinearLayout addExpandableSection(LinearLayout root, String text, boolean expandedByDefault) {
        SettingsSectionCard card = SettingsSectionCard.create(this, text, expandedByDefault);
        root.addView(card.container, SettingsRowBuilder.matchWrapWithTop(this, 12));
        return card.content;
    }

    private boolean isDebuggableBuild() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void markCurrentThemeCustom() {
        KeyboardPreferences.saveSelectedThemeId(this, "");
    }

}
